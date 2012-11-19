/**
 * TAC Supply Chain Management Simulator
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
 * AppletConnection
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Mar 12 17:55:03 2003
 * Updated : $Date: 2004-10-28 14:24:41 -0500 (Thu, 28 Oct 2004) $
 *           $Revision: 1057 $
 */
package se.sics.tasim.viewer.applet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.viewer.ViewerConnection;

public class AppletConnection implements Runnable {

	private static final byte[] TACT_HEADER = { (byte) 'T', (byte) 'A',
			(byte) 'C', (byte) 'T', 0, 0, 0, 0 };

	private int MAX_BUFFER = 512000;
	private Socket socket;

	private DataInputStream in;
	private DataOutputStream out;

	private BinaryTransportWriter transportWriter;
	private BinaryTransportReader transportReader;

	private ViewerApplet applet;
	private ViewerConnection viewer;

	private Thread connectionThread;
	private boolean finish = false;

	private String serverHost;

	private static final Logger log = Logger.getLogger(AppletConnection.class
			.getName());

	public AppletConnection(ViewerApplet applet, ViewerConnection viewer) {
		this.applet = applet;
		this.viewer = viewer;
	}

	public boolean connect() {
		try {
			disconnect();
			URL url = applet.getCodeBase();
			String serverName = applet.getServerName();
			String userName = applet.getUserName();

			int serverPort = applet.getServerPort();
			serverHost = url.getHost();

			String statusMessage = "Connecting to server " + serverName
					+ " at " + serverHost + ':' + serverPort;
			log.fine(statusMessage);
			applet.setStatusMessage(statusMessage);

			transportWriter = new BinaryTransportWriter();
			transportReader = new BinaryTransportReader();
			transportReader.setContext(applet.getContextFactory()
					.createContext());

			socket = new Socket(serverHost, serverPort);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			// Send the TACT protocol header
			out.write(TACT_HEADER);

			transportWriter.node("auth").attr("serverName", serverName).attr(
					"userName", userName).attr("version", ViewerApplet.VERSION)
					.endNode("auth");

			sendData(transportWriter);
			applet.setStatusMessage("Connected to server " + serverName);
			return true;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Connection to server failed", e);
			socket = null;
		}
		return false;
	}

	public void start() {
		(connectionThread = new Thread(this)).start();
	}

	public void stop() {
		if (!finish) {
			finish = true;
			disconnect();

			Thread t = connectionThread;
			if (t != null) {
				connectionThread = null;
				t.interrupt();
			}
		}
	}

	public void run() {
		while (!finish) {
			try {
				while (!finish && !connect()) {
					applet.setStatusMessage("Failed to connect to "
							+ applet.getServerName() + " at " + serverHost
							+ " (will retry)");
					Thread.sleep(30000);
				}

				byte[] buffer = new byte[8192];
				int len;
				int lastPos;

				while (socket != null && !finish) {
					int size = in.readInt();
					if (size > MAX_BUFFER || size < 0) {
						throw new IOException("Illegal size of message " + size);
					}
					if (size > buffer.length) {
						buffer = new byte[size * 2];
					}
					in.readFully(buffer, 0, size);

					parseMessage(buffer, 0, size);
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not read message", e);
				disconnect();
				// If not finished do a wait before reconnecting
				if (!finish) {
					try {
						Thread.sleep(10000);
					} catch (Exception e2) {
					}
				}
			}
		}
		connectionThread = null;
		disconnect();
	}

	private void sendData(BinaryTransportWriter writer) throws IOException {
		writer.finish();
		out.writeInt(writer.size());
		writer.write(out);
		out.flush();
		writer.clear();
	}

	public synchronized void sendChatMessage(String message) {
		transportWriter.clear();
		transportWriter.node("chat").attr("message", message);
		transportWriter.endNode("chat");
		try {
			sendData(transportWriter);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Can not send chat message to server", e);
		}
	}

	private void parseMessage(byte[] buffer, int offset, int size) {
		try {
			BinaryTransportReader reader = this.transportReader;
			reader.setMessage(buffer, offset, size);
			while (reader.nextNode(false)) {
				if (reader.isNode("intUpdated")) {
					viewer.dataUpdated(reader.getAttributeAsInt("agent"),
							reader.getAttributeAsInt("type", 0), reader
									.getAttributeAsInt("value"));

				} else if (reader.isNode("longUpdated")) {
					viewer.dataUpdated(reader.getAttributeAsInt("agent"),
							reader.getAttributeAsInt("type", 0), reader
									.getAttributeAsLong("value"));

				} else if (reader.isNode("floatUpdated")) {
					viewer.dataUpdated(reader.getAttributeAsInt("agent"),
							reader.getAttributeAsInt("type", 0), reader
									.getAttributeAsFloat("value"));

				} else if (reader.isNode("doubleUpdated")) {
					viewer.dataUpdated(reader.getAttributeAsInt("agent"),
							reader.getAttributeAsInt("type", 0), reader
									.getAttributeAsDouble("value"));

				} else if (reader.isNode("stringUpdated")) {
					viewer.dataUpdated(reader.getAttributeAsInt("agent"),
							reader.getAttributeAsInt("type", 0), reader
									.getAttribute("value"));

				} else if (reader.isNode("objectUpdated")) {
					int agent = reader.getAttributeAsInt("agent", -1);
					int type = reader.getAttributeAsInt("type", 0);
					Transportable content;
					reader.enterNode();
					if (reader.nextNode(false)) {
						content = reader.readTransportable();
						if (agent < 0) {
							viewer.dataUpdated(type, content);
						} else {
							viewer.dataUpdated(agent, type, content);
						}
					} else {
						log.warning("no content for objectUpdated");
					}
					reader.exitNode();

				} else if (reader.isNode("interaction")) {
					viewer.interaction(reader.getAttributeAsInt("fromAgent"),
							reader.getAttributeAsInt("toAgent"), reader
									.getAttributeAsInt("type", 0));

				} else if (reader.isNode("interactionWithRole")) {
					viewer.interactionWithRole(reader
							.getAttributeAsInt("fromAgent"), reader
							.getAttributeAsInt("role"), reader
							.getAttributeAsInt("type", 0));

				} else if (reader.isNode("nextTimeUnit")) {
					viewer.nextTimeUnit(reader.getAttributeAsInt("unit"));

				} else if (reader.isNode("nextSimulation")) {
					viewer.nextSimulation(reader.getAttributeAsInt("id", -1),
							reader.getAttributeAsLong("startTime", 0L));

				} else if (reader.isNode("simulationStarted")) {
					viewer.simulationStarted(reader.getAttributeAsInt("id"),
							reader.getAttribute("type"), reader
									.getAttributeAsLong("startTime"), reader
									.getAttributeAsLong("endTime"), reader
									.getAttribute("timeUnitName", null), reader
									.getAttributeAsInt("timeUnitCount", 0));

				} else if (reader.isNode("participant")) {
					viewer.participant(reader.getAttributeAsInt("id"), reader
							.getAttributeAsInt("role"), reader
							.getAttribute("name"), reader
							.getAttributeAsInt("participantID"));

				} else if (reader.isNode("simulationStopped")) {
					viewer.simulationStopped(reader.getAttributeAsInt("id"));

				} else if (reader.isNode("chat")) {
					applet.addChatMessage(reader.getAttributeAsLong("time"),
							reader.getAttribute("server"), reader
									.getAttribute("user"), reader
									.getAttribute("message"));

				} else if (reader.isNode("serverTime")) {
					viewer.setServerTime(reader.getAttributeAsLong("time"));
				} else if (reader.isNode("intCache")) {
					viewer.intCache(reader.getAttributeAsInt("agent"), reader
							.getAttributeAsInt("type", 0), reader
							.getAttributeAsIntArray("cache"));
				} else {
					log.warning("ignoring message " + reader.getNodeName());
				}
			}

		} catch (Exception e) {
			log.log(Level.SEVERE, "Error while parsing message", e);
		}
	}

	public void disconnect() {
		Socket socket = this.socket;
		this.socket = null;
		if (socket != null) {
			try {
				DataInputStream in = this.in;
				DataOutputStream out = this.out;
				this.in = null;
				this.out = null;
				transportWriter = null;
				transportReader = null;

				if (out != null)
					out.close();
				if (in != null)
					in.close();
				socket.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error while disconnecting from server",
						e);
			}
			if (!finish) {
				applet.setStatusMessage("Disconnected from server... ");
			}
		}
	}

} // AppletConnection
