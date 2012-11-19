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
 * InetServer
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Jun 16 18:46:36 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.inet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public abstract class InetServer {

	private static final Logger log = Logger.getLogger(InetServer.class
			.getName());

	private String name;
	private String host;
	private Server server;
	private int port;

	public InetServer(String name, int port) {
		this(name, null, port);
	}

	public InetServer(String name, String host, int port) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.name = name;
		this.host = host;
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public String getBindAddress() {
		return (host == null ? "*" : host) + ':' + port;
	}

	public String getHost() {
		return host == null ? getLocalHostName() : host;
	}

	public int getPort() {
		return port;
	}

	public boolean isRunning() {
		return server != null;
	}

	public final void start() throws IOException {
		if (server == null) {
			server = new Server(this, host, port);
			server.start();
			serverStarted();
		}
	}

	public final void stop() {
		if (server != null) {
			server.shutdown();
			server = null;
			serverShutdown();
		}
	}

	protected abstract void serverStarted();

	protected abstract void serverShutdown();

	protected abstract void newConnection(Socket socket) throws IOException;

	// -------------------------------------------------------------------
	// Listening thread
	// -------------------------------------------------------------------

	private static class Server extends Thread {
		private boolean stopped = false;
		private ServerSocket socket;
		private InetServer inet;

		Server(InetServer inet, String host, int port) throws IOException {
			super(inet.name);
			if (host != null) {
				socket = new ServerSocket();
				socket.bind(new InetSocketAddress(host, port));
			} else {
				socket = new ServerSocket(port);
			}
			this.inet = inet;
		}

		void shutdown() {
			if (!stopped) {
				stopped = true;
				try {
					interrupt();
					socket.close();
				} catch (Exception e) {
				}
			}
		}

		public void run() {
			try {
				while (!stopped) {
					Socket connection = socket.accept();
					try {
						inet.newConnection(connection);
					} catch (ThreadDeath e) {
						throw e;
					} catch (Throwable e) {
						log.log(Level.SEVERE, inet.name
								+ ": failed to handle new connection", e);
						// Try to close the connection
						try {
							connection.close();
						} catch (Exception e2) {
						}
					}
				}

			} catch (Exception exception) {
				log.log(Level.SEVERE, inet.name + ": listening error",
						exception);

			} finally {
				stopped = true;
				try {
					socket.close();
					inet.stop();
				} catch (Exception e2) {
				}
			}
		}
	}

	// -------------------------------------------------------------------
	// The name of the local host
	// -------------------------------------------------------------------

	private static String localHostAddress, localHostAddress2;
	private static String localHostName, localHostName2;

	public static String getLocalHostName() {
		if (localHostAddress == null) {
			String address = null;
			try {
				InetAddress localHost = InetAddress.getLocalHost();
				address = localHost.getHostAddress();
				localHostName = localHost.getHostName();
			} catch (Exception e) {
				log.log(Level.WARNING, "could not retrieve local host", e);
			}
			if (address == null) {
				address = "127.0.0.1";
			}
			try {
				InetAddress localHost = InetAddress.getByName(address);
				localHostAddress2 = localHost.getHostAddress();
				localHostName2 = localHost.getHostName();
			} catch (Exception e) {
				log.log(Level.WARNING, "could not retrieve local host", e);
			}
			if (localHostName2 == null) {
				if (localHostAddress2 != null) {
					localHostName2 = localHostAddress2;
				} else if (localHostName != null) {
					localHostName2 = localHostName;
				} else {
					localHostName2 = address;
				}
			}
			// Minor optimizations (no need for duplicate information)
			if (localHostName != null
					&& localHostName.equalsIgnoreCase(localHostName2)) {
				localHostName = null;
			}
			if (localHostAddress2 != null
					&& localHostAddress2.equalsIgnoreCase(address)) {
				localHostAddress2 = null;
			}
			localHostAddress = address;
		}
		return localHostName2;
	}

} // InetServer
