/**
 * SICS ISL Java Utilities
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001-2003 SICS AB. All rights reserved.
 *
 * SICS grants you the right to use, modify, and redistribute this
 * software for noncommercial purposes, on the conditions that you:
 * (1) retain the original headers, including the copyright notice and
 * this text, (2) clearly document the difference between any derived
 * software and the original, and (3) acknowledge your use of this
 * software in pertaining publications and reports.  SICS provides
 * this software "as is", without any warranty of any kind.  IN NO
 * EVENT SHALL SICS BE LIABLE FOR ANY DIRECT, SPECIAL OR INDIRECT,
 * PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSSES OR DAMAGES ARISING OUT
 * OF THE USE OF THE SOFTWARE.
 *
 * -----------------------------------------------------------------
 *
 * TACTConnection
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Jun 17 17:56:22 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.tact;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.botbox.util.ArrayQueue;
import com.botbox.util.JobStatus;
import com.botbox.util.ThreadPool;

/**
 * - One thread for receiving and deliverying data - Threadpool for sending data
 */
public abstract class TACTConnection {

	private static final boolean DEBUG = false;
	private static final boolean VERBOSE_DEBUG = DEBUG && false;

	private static final Logger log = Logger.getLogger(TACTConnection.class
			.getName());

	private static final byte[] TACT_HEADER = { (byte) 'T', (byte) 'A',
			(byte) 'C', (byte) 'T', 0, // major version
			0, // minor version
			0, 0 // reserved
	};

	// Maximum - 2 megabyte of data in messages!!!
	private static final int MAX_BUFFER_SIZE = 2 * 1024 * 1024;

	private ThreadPool threadPool;

	private String name;
	private String fullName;
	private String userName;

	private long connectTime;

	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	private String remoteHost;
	private int remotePort;
	private boolean isServerConnection;

	// private int minorVersion = 0;
	// private int majorVersion = 0;
	// private boolean hasReadHeader;

	private int maxBuffer = MAX_BUFFER_SIZE;

	private long sentBytes;
	private long requestedSentBytes;

	private ArrayQueue outBuffer;
	private boolean writerRunning = false;
	private boolean isOpen = false;
	private boolean isClosed = false;

	private TACTWriter tactWriter;
	private TACTReader tactReader;

	// MUST NOT START EVERYTHING in HERE BECAUSE THE CHILD MUST BE
	// INITIALIZED BEFORE THE READER THREAD STARTS
	public TACTConnection(String name, String host, int port) {
		this.name = name;
		this.fullName = name;
		this.remoteHost = host;
		this.remotePort = port;
		this.isServerConnection = false;
		this.connectTime = System.currentTimeMillis();
	}

	// MUST NOT START EVERYTHING in HERE BECAUSE THE CHILD MUST BE
	// INITIALIZED BEFORE THE READER THREAD STARTS
	public TACTConnection(String name, Socket socket) {
		this.name = name;
		this.fullName = name;
		this.socket = socket;
		this.isServerConnection = true;
		this.connectTime = System.currentTimeMillis();
	}

	public String getName() {
		return fullName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		if (userName == null) {
			throw new NullPointerException();
		}
		// User name can only be set once
		this.fullName = userName + '@' + this.name;
		this.userName = userName;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public long getConnectTime() {
		return connectTime;
	}

	public int getMaxBuffer() {
		return maxBuffer;
	}

	public void setMaxBuffer(int maxBuffer) {
		this.maxBuffer = maxBuffer;
	}

	public ThreadPool getThreadPool() {
		ThreadPool pool = this.threadPool;
		if (pool == null) {
			pool = this.threadPool = ThreadPool.getDefaultThreadPool();
		}
		return pool;
	}

	public void setThreadPool(ThreadPool threadPool) {
		this.threadPool = threadPool;
	}

	public final void start() throws IOException {
		if (this.input != null) {
			// Already initialized
			return;
		}

		if (isServerConnection) {
			InetAddress remoteAddress = socket.getInetAddress();
			this.remoteHost = remoteAddress.getHostAddress();
			this.remotePort = socket.getPort();
			log.finest(fullName + ": new connection from " + remoteHost + ':'
					+ remotePort);
		} else {
			this.socket = new Socket(remoteHost, remotePort);
		}

		this.input = new DataInputStream(socket.getInputStream());
		this.output = new DataOutputStream(socket.getOutputStream());
		if (!isServerConnection) {
			this.output.write(TACT_HEADER);
		}
		this.isOpen = true;

		outBuffer = new ArrayQueue();

		tactReader = new TACTReader(this);
		tactReader.start();

		connectionOpened();
	}

	public void write(byte[] data) {
		if (isOpen && data != null) {
			requestedSentBytes += data.length;
			if ((requestedSentBytes - sentBytes) > maxBuffer) {
				log.log(Level.SEVERE, fullName + ": could not send data",
						new IOException("out buffer overflow: "
								+ (requestedSentBytes - sentBytes)));
				closeImmediately();
			} else {
				addOutBuffer(data);
			}
		}
	}

	public boolean isClosed() {
		return !isOpen;
	}

	public void close() {
		if (isOpen) {
			isOpen = false;
			addOutBuffer(null);
		}
	}

	public void closeImmediately() {
		closeImmediately(true);
	}

	private void closeImmediately(boolean useThread) {
		if (!isClosed) {
			isOpen = false;
			isClosed = true;
			if (useThread) {
				getThreadPool().invokeLater(new ConnectionCloser(this));
			} else {
				doClose();
			}
		}
	}

	private void doClose() {
		log.finest(fullName + ": connection closed from " + remoteHost);
		try {
			connectionClosed();
		} catch (Exception e) {
			log
					.log(Level.WARNING, fullName
							+ ": failed to close connection", e);
		}
		try {
			tactReader.interrupt();
			output.close();
			input.close();
			socket.close();
		} catch (Exception e) {
			log.log(Level.SEVERE, fullName + ": could not close connection", e);
		}
	}

	protected abstract void connectionOpened();

	// Called when connection closed
	protected abstract void connectionClosed();

	protected abstract void dataRead(byte[] data, int start, int len);

	private void addOutBuffer(byte[] data) {
		synchronized (outBuffer) {
			outBuffer.add(data);
			if (!writerRunning) {
				if (tactWriter == null) {
					tactWriter = new TACTWriter(this);
				}
				writerRunning = true;
				getThreadPool().invokeLater(tactWriter);
			} else {
				outBuffer.notify();
			}
		}
	}

	// -------------------------------------------------------------------
	// Data delivery
	// -------------------------------------------------------------------

	private static class TACTWriter implements Runnable {
		private TACTConnection connection;

		TACTWriter(TACTConnection connection) {
			this.connection = connection;
		}

		public void run() {
			byte[] data = null;
			boolean ok = false;
			JobStatus jobStatus = ThreadPool.getJobStatus();

			try {
				// Only write if not closed!!!
				while (!connection.isClosed) {
					synchronized (connection.outBuffer) {
						if (connection.outBuffer.isEmpty()) {
							// Wait a short time for more data because data is
							// often
							// written in short intervals
							try {
								connection.outBuffer.wait(800);
							} catch (Exception e) {
							}
						}

						if (!connection.outBuffer.isEmpty()) {
							data = (byte[]) connection.outBuffer.remove(0);
						} else {
							connection.writerRunning = false;
							ok = true;
							return;
						}
					}
					if (data == null) {
						// Time to close the connection
						connection.closeImmediately(false);
						break;
					} else {
						if (jobStatus != null) {
							jobStatus.stillAlive();
						}
						connection.sentBytes += data.length;
						connection.output.writeInt(data.length);
						connection.output.write(data);
						connection.output.flush();
					}
				}

				// The connection will never write again so we never clear
				// the writer running flag
				// connection.writerRunning = false;
				ok = true;

			} catch (Throwable e) {
				log.log(Level.SEVERE, connection.fullName
						+ ": could not send data", e);
				// Close the connection if failed to send data
				connection.closeImmediately(false);

				if (e instanceof ThreadDeath) {
					throw (ThreadDeath) e;
				}
			} finally {
				if (!ok) {
					synchronized (connection.outBuffer) {
						if (!connection.outBuffer.isEmpty()
								&& !connection.isClosed) {
							log.warning("reinvoking writer for "
									+ connection.fullName);
							connection.getThreadPool().invokeLater(this);
						} else {
							log.warning("writer for " + connection.fullName
									+ " exiting");
							connection.writerRunning = false;
						}
					}
				}
				if (VERBOSE_DEBUG)
					log.severe("TACTCONNECTION " + connection.fullName
							+ " writer exited");
			}
		}

		public String toString() {
			return "TACTWriter[" + connection.fullName + ','
					+ connection.outBuffer.size() + ',' + connection.remoteHost
					+ ']';
		}
	}

	// -------------------------------------------------------------------
	// Data reception
	// -------------------------------------------------------------------

	private static class TACTReader extends Thread {

		private TACTConnection connection;

		TACTReader(TACTConnection connection) {
			super(connection.name);
			this.connection = connection;
		}

		public void run() {
			byte[] buffer = new byte[8192];
			int len;
			int lastPos;
			try {
				connection.input.readFully(buffer, 0, TACT_HEADER.length);
				// Only the four first bytes identifies the protocol
				// followed by two bytes describing the version and
				// two reserved bytes.
				for (int i = 0; i < 4; i++) {
					if (buffer[i] != TACT_HEADER[i]) {
						throw new IOException("illegal protocol header: "
								+ new String(buffer, 0, 8));
					}
				}
				// connection.majorVersion = buffer[4] & 0xff;
				// connection.minorVersion = buffer[5] & 0xff;
				// connection.hasReadHeader = true;

				while (connection.isOpen) {
					int size = connection.input.readInt();
					if (size > buffer.length) {
						if (size > connection.getMaxBuffer()) {
							throw new IOException("in buffer overflow: " + size);
						}
						buffer = new byte[size + 8192];
					}
					connection.input.readFully(buffer, 0, size);
					try {
						connection.dataRead(buffer, 0, size);
					} catch (Throwable e) {
						log.log(Level.SEVERE, connection.fullName
								+ ": could not deliver data: " + size, e);
					}
				}
			} catch (EOFException e) {
				// Connection was closed from other side
				log.severe(connection.fullName + ": closed from other side");
			} catch (Throwable e) {
				if (connection.isOpen || DEBUG) {
					log.log(Level.SEVERE, connection.fullName
							+ ": reading error ", e);
				}
			} finally {
				if (DEBUG)
					log.severe("TACTCONNECTION " + connection.fullName
							+ " reader exited with open=" + connection.isOpen);
				if (connection.isOpen) {
					connection.closeImmediately(false);
				}
			}
		}
	}

	// -------------------------------------------------------------------
	// ConnectionCloser
	// -------------------------------------------------------------------

	private static class ConnectionCloser implements Runnable {

		private final TACTConnection connection;

		public ConnectionCloser(TACTConnection connection) {
			this.connection = connection;
		}

		public void run() {
			connection.doClose();
		}

		public String toString() {
			return "ConnectionCloser[" + connection.fullName + ','
					+ connection.remoteHost + ']';
		}

	}

} // TACTConnection
