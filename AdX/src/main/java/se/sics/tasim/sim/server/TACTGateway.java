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
 * TACTGateway
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Feb 12 17:48:34 2003
 * Updated : $Date: 2008-04-04 21:08:33 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3983 $
 */
package se.sics.tasim.sim.server;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

import com.botbox.util.ArrayUtils;
import com.botbox.util.ThreadPool;
import se.sics.isl.inet.InetServer;
import se.sics.isl.transport.Context;
import se.sics.isl.transport.ContextFactory;
import se.sics.isl.util.AMonitor;
import se.sics.isl.util.AdminMonitor;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.props.AdminContent;
import se.sics.tasim.sim.Gateway;

public class TACTGateway extends Gateway implements AMonitor {

	private static final String CONF = "sim.gateway.";

	private static final Logger log = Logger.getLogger(TACTGateway.class
			.getName());

	private static final String STATUS_NAME = "TACT";

	private Context transportContext;
	private ThreadPool threadPool;
	private TACTServer server;
	private TACTChannel[] agentConnections;
	private boolean isRunning = true;

	public TACTGateway() {
	}

	protected void initGateway() {
		transportContext = AdminContent.createContext();

		/*
		 * Reflectively loading the context factory
		 */
		ConfigManager config = getConfig();

		String name = getName();

		String contextFactoryClassName = config.getProperty(CONF + name
				+ ".contextFactory", config
				.getProperty("server.contextFactory"));

		try {
			ContextFactory contextFactory = (ContextFactory) Class.forName(
					contextFactoryClassName).newInstance();
			transportContext = contextFactory.createContext(transportContext);
		} catch (ClassNotFoundException e) {
			log.severe("server " + getName()
					+ " unable to load context factory: Class not found");
		} catch (InstantiationException e) {
			log
					.severe("server "
							+ getName()
							+ " unable to load context factory: Class cannot be instantiated");
		} catch (IllegalAccessException e) {
			log
					.severe("server "
							+ getName()
							+ " unable to load context factory: Illegal access exception");
		}
	}

	protected void startGateway() throws IOException {
		if (!isRunning || server != null) {
			return;
		}

		ConfigManager config = getConfig();
		String name = getName();
		String host = config.getProperty(CONF + name + ".host", config
				.getProperty("server.host"));
		int port = config.getPropertyAsInt(CONF + name + ".port", 6502);

		int minThreads = config
				.getPropertyAsInt(CONF + name + ".minThreads", 5);
		int maxThreads = config.getPropertyAsInt(CONF + name + ".maxThreads",
				50);
		int maxIdleThreads = config.getPropertyAsInt(CONF + name
				+ ".maxIdleThreads", 25);
		this.threadPool = ThreadPool.getThreadPool("viewer");
		this.threadPool.setMinThreads(minThreads);
		this.threadPool.setMaxThreads(maxThreads);
		this.threadPool.setMaxIdleThreads(maxIdleThreads);
		this.threadPool.setInterruptThreadsAfter(120000);

		server = new TACTServer(this, "tact", host, port);
		server.start();
		log.info("TACT Server started at " + server.getBindAddress());

		AdminMonitor adminMonitor = AdminMonitor.getDefault();
		if (adminMonitor != null) {
			adminMonitor.addMonitor(STATUS_NAME, this);
		}
	}

	protected void stopGateway() {
		if (!isRunning) {
			return;
		}

		isRunning = false;

		TACTServer server = this.server;
		if (server != null) {
			this.server = null;
			server.stop();
		}

		TACTChannel[] connections = this.agentConnections;
		if (connections != null) {
			for (int i = 0, n = connections.length; i < n; i++) {
				connections[i].close();
			}
		}
	}

	// -------------------------------------------------------------------
	// AMonitor API
	// -------------------------------------------------------------------

	public String getStatus(String propertyName) {
		if (propertyName != STATUS_NAME) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("--- TACT Gateway ---");

		TACTChannel[] connections = this.agentConnections;
		if (connections != null) {
			for (int i = 0, n = connections.length; i < n; i++) {
				TACTChannel channel = connections[i];
				sb.append('\n').append(i + 1).append(": ").append(
						channel.getName()).append(" (").append(
						channel.getAddress()).append(',').append(
						channel.getRemoteHost()).append(':').append(
						channel.getRemotePort()).append(')');
			}
		} else {
			sb.append("\n<no connections>");
		}

		return sb.toString();
	}

	// -------------------------------------------------------------------
	// API towards TACTChannel
	// -------------------------------------------------------------------

	final ThreadPool getThreadPool() {
		return threadPool;
	}

	final Context getContext() {
		return transportContext;
	}

	final void loginAgentChannel(TACTChannel channel, String name,
			String password) {
		super.loginAgentChannel(channel, name, password);
	}

	public synchronized void addAgentConnection(TACTChannel connection) {
		agentConnections = (TACTChannel[]) ArrayUtils.add(TACTChannel.class,
				agentConnections, connection);
	}

	public synchronized void removeAgentConnection(TACTChannel connection) {
		agentConnections = (TACTChannel[]) ArrayUtils.remove(agentConnections,
				connection);
	}

	// -------------------------------------------------------------------
	// The TACT server
	// -------------------------------------------------------------------

	private static class TACTServer extends InetServer {

		private TACTGateway gateway;

		public TACTServer(TACTGateway gateway, String name, String host,
				int port) {
			super(name, host, port);
			this.gateway = gateway;
		}

		protected void serverStarted() {
		}

		protected void serverShutdown() {
			if (gateway.isRunning) {
				log.severe("server " + getName() + " died!!!");
			}
		}

		protected void newConnection(Socket socket) throws IOException {
			new TACTChannel(gateway, socket);
		}
	}

} // TACTGateway
