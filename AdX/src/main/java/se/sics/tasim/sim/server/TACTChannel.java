/**
 * TAC Supply Chain Management Simulator
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001-2005 SICS AB. All rights reserved.
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
 * TACTChannel
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Feb 12 17:57:24 2003
 * Updated : $Date: 2008-04-04 21:08:33 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3983 $
 */
package se.sics.tasim.sim.server;

import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.botbox.util.ThreadPool;
import se.sics.isl.tact.TACTConnection;
import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import se.sics.isl.transport.Context;
import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.aw.Message;
import se.sics.tasim.props.AdminContent;
import se.sics.tasim.props.Alert;
import se.sics.tasim.sim.AgentChannel;
import se.sics.tasim.props.Ping;
import se.sics.tasim.aw.Agent;

final class TACTChannel extends AgentChannel {

	private static final Logger log = Logger.getLogger(TACTChannel.class
			.getName());

	private static final String MESSAGE_NAME = new Message().getTransportName();

	private static int channelCounter = 0;

	private BinaryTransportReader reader = new BinaryTransportReader();
	private BinaryTransportWriter writer = new BinaryTransportWriter();

	private final TACTGateway gateway;
	private final TACTConnection connection;

	private boolean isActiveOrdersSupported = false;
	private boolean isPingSupported = false;

	TACTChannel(TACTGateway gateway, Socket socket) throws IOException {
		this.gateway = gateway;
		reader.setContext(gateway.getContext());

		this.connection = new TACTConnection(gateway.getName() + '-'
				+ (++channelCounter), socket) {

			protected void connectionOpened() {
			}

			protected void connectionClosed() {
				TACTChannel.this.gateway
						.removeAgentConnection(TACTChannel.this);
				TACTChannel.this.close();
			}

			protected void dataRead(byte[] buffer, int offset, int length) {
				TACTChannel.this.dataRead(buffer, offset, length);
			}

		};
		this.connection.setThreadPool(gateway.getThreadPool());
		this.connection.start();
		gateway.addAgentConnection(this);
	}

	protected void setSimulationThreadPool(ThreadPool threadPool) {
		if (threadPool == null) {
			this.connection.setThreadPool(gateway.getThreadPool());
		} else {
			this.connection.setThreadPool(threadPool);
		}
	}

	public boolean isSupported(String name) {
		if (ACTIVE_ORDERS.equals(name)) {
			return isActiveOrdersSupported;
		}
		if (PING.equals(name)) {
			return isPingSupported;
		}
		return false;
	}

	public synchronized void addTransportConstant(String name) {
		// It is ok to add constants here because the writer are cleared
		// only after sending current content, not before.
		writer.addConstant(name);
	}

	protected String getAddress() {
		return connection.getName();
	}

	public String getRemoteHost() {
		return connection.getRemoteHost();
	}

	public int getRemotePort() {
		return connection.getRemotePort();
	}

	protected boolean sendPingRequest() {
		if (isPingSupported && !super.isClosed()) {
			deliverToAgent(new Message(Agent.ADMIN, getName(), new Ping()));
			return true;
		} else {
			return false;
		}
	}

	protected void closeChannel() {
		connection.close();
	}

	// -------------------------------------------------------------------
	//
	// -------------------------------------------------------------------

	// public long getLastAliveTime() {
	// return connection.getLastAliveTime();
	// }

	public void deliverToAgent(Message message) {
		if (!connection.isClosed()) {
			byte[] messageData = getBytes(message);
			connection.write(messageData);
		}
	}

	private synchronized byte[] getBytes(Message message) {
		// Must send the current content in the writer and clear
		// afterwards because the writer may contain pending constants
		String node = message.getTransportName();
		writer.node(node);
		message.write(writer);
		writer.endNode(node);
		writer.finish();

		byte[] data = writer.getBytes();
		writer.clear();
		return data;
	}

	// -------------------------------------------------------------------
	// Message handling
	// -------------------------------------------------------------------

	private void dataRead(byte[] buffer, int offset, int length) {
		reader.setMessage(buffer, offset, length);

		Message message;
		while ((message = parseMessage(reader)) != null) {
			try {
				deliverFromAgent(message);
			} catch (Exception e) {
				log.log(Level.SEVERE, connection.getName()
						+ " could not deliver message from agent: " + message,
						e);
			}
		}
	}

	private Message parseMessage(BinaryTransportReader reader) {
		try {
			if (!reader.nextNode(MESSAGE_NAME, false)) {
				return null;
			}
			Message message = new Message();
			reader.enterNode();
			message.read(reader);
			reader.exitNode();
			return message;
		} catch (Exception e) {
			log.log(Level.WARNING, "could not parse message", e);
			try {
				// Only logs to standard out for now. FIX THIS!!! \TODO
				reader.printMessage();
			} catch (ParseException e2) {
			}
			// Should return error message to the agent. FIX THIS!!! \TODO
			return null;
		}
	}

	protected void deliverFromAgent(Message message) {
		Transportable content = message.getContent();

		if (super.isClosed()) {
			// Not yet initialized i.e. logged in
			AdminContent reply = new AdminContent(AdminContent.AUTH,
					AdminContent.NOT_AUTH);
			if (content.getClass() == AdminContent.class) {
				AdminContent admin = (AdminContent) content;
				int type = admin.getType();
				if (type == AdminContent.AUTH) {
					// Should check that the receiver is admin. FIX THIS!!!
					// \TODO
					String clientVersion = admin.getAttribute("client.version");
					String serverVersion = gateway.getServerVersion();

					// ----------------------------------------------------------------
					// Allow both clients 0.7, 0.8, 0.9 to connect to this
					// server
					// ----------------------------------------------------------------

					// Allow all client versions larger than 0.7 to connect
					if (ConfigManager.compareVersion("0.7", clientVersion) > 0) {
						String messageText = "You seem to use an incompatible version of AgentWare ("
								+ clientVersion
								+ ").\n"
								+ "During alpha testing the system might change between each\n"
								+ "release and you need an AgentWare compatible with \n"
								+ "the current server version ("
								+ serverVersion + ").";
						Alert alert = new Alert("Wrong Version", messageText);
						deliverToAgent(message.createReply(alert));

						reply.setError(AdminContent.NOT_SUPPORTED,
								"incompatible client version");

					} else {
						if (ConfigManager
								.compareVersion(clientVersion, "0.9.6") >= 0) {
							isActiveOrdersSupported = true;
							writer.setSupported(
									BinaryTransportWriter.SUPPORT_CONSTANTS,
									true);
							writer.setSupported(
									BinaryTransportWriter.SUPPORT_TABLES, true);
						} else {
							isActiveOrdersSupported = false;
						}

						isPingSupported = ConfigManager.compareVersion(
								clientVersion, "0.9.7") >= 0;

						String name = admin.getAttribute("name");
						String password = admin.getAttribute("password");
						try {
							gateway.loginAgentChannel(this, name, password);
							reply.setError(AdminContent.NO_ERROR);
							connection.setUserName(name);
							log.info("user " + name + " logged in as "
									+ connection.getName() + " with version "
									+ clientVersion + " from "
									+ connection.getRemoteHost());
							reply.setAttribute("server.version", serverVersion);
							// reply.setAttribute("server.info",
							// gateway.getServerInfo());
						} catch (Exception e) {
							log.log(Level.WARNING, "could not login user "
									+ name, e);
							reply.setError(AdminContent.NOT_AUTH, e
									.getMessage());
						}
					}
				} else if (type == AdminContent.QUIT) {
					reply = new AdminContent(AdminContent.QUIT);
				}
			}

			deliverToAgent(message.createReply(reply));
			if (reply.getType() == AdminContent.QUIT) {
				close();
			}

		} else if (content instanceof Ping) {
			Ping ping = (Ping) content;
			if (ping.isPong()) {
				pongReceived();
			} else if (ping.isPing()) {
				deliverToAgent(message.createReply(ping.createPong()));
			}

		} else {
			super.deliverFromAgent(message);
		}
	}

} // TACTChannel
