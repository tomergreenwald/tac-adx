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
 * BlockingViewerServer
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Mar 12 22:17:51 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

import com.botbox.util.ArrayUtils;
import com.botbox.util.ThreadPool;
import se.sics.isl.inet.InetServer;
import se.sics.isl.transport.Context;
import se.sics.isl.util.AMonitor;
import se.sics.isl.util.AdminMonitor;
import se.sics.isl.util.ConfigManager;

public class BlockingViewerServer extends InetServer implements AMonitor {

	private static final Logger log = Logger
			.getLogger(BlockingViewerServer.class.getName());

	private static final String STATUS_NAME = "Viewer";

	private static final String CONF = "is.viewer.";

	private InfoServer infoServer;
	private Context transportContext;
	private BlockingViewerChannel[] viewerConnections;

	private ThreadPool viewerThreadPool;

	public BlockingViewerServer(InfoServer infoServer) {
		super("viewer", infoServer.getConfig().getProperty(CONF + "host",
				infoServer.getConfig().getProperty("server.host")), infoServer
				.getConfig().getPropertyAsInt(CONF + "port", 4042));
		ConfigManager config = infoServer.getConfig();
		this.infoServer = infoServer;
		this.transportContext = new Context("viewer");

		int minThreads = config.getPropertyAsInt(CONF + "minThreads", 5);
		int maxThreads = config.getPropertyAsInt(CONF + "maxThreads", 50);
		int maxIdleThreads = config.getPropertyAsInt(CONF + "maxIdleThreads",
				25);
		this.viewerThreadPool = ThreadPool.getThreadPool("viewer");
		this.viewerThreadPool.setMinThreads(minThreads);
		this.viewerThreadPool.setMaxThreads(maxThreads);
		this.viewerThreadPool.setMaxIdleThreads(maxIdleThreads);
		this.viewerThreadPool.setInterruptThreadsAfter(120000);

		AdminMonitor adminMonitor = AdminMonitor.getDefault();
		if (adminMonitor != null) {
			adminMonitor.addMonitor(STATUS_NAME, this);
		}
	}

	// -------------------------------------------------------------------
	// Inet Server
	// -------------------------------------------------------------------

	protected void serverStarted() {
		log.info("viewer server started at " + getBindAddress());
	}

	protected void serverShutdown() {
		BlockingViewerChannel[] connections;
		synchronized (this) {
			connections = this.viewerConnections;
			this.viewerConnections = null;
		}

		if (connections != null) {
			for (int i = 0, n = connections.length; i < n; i++) {
				connections[i].close();
			}
		}
		log.severe("viewer server has closed");
		infoServer.serverClosed(this);
	}

	protected void newConnection(Socket socket) throws IOException {
		BlockingViewerChannel channel = new BlockingViewerChannel(this, socket,
				transportContext);
		channel.setThreadPool(viewerThreadPool);
		channel.start();
	}

	// -------------------------------------------------------------------
	// API towards Viewer Channels
	// -------------------------------------------------------------------

	SimServer getSimServer(BlockingViewerChannel connection, String serverName) {
		return infoServer.getSimServer(serverName);
	}

	synchronized void addViewerConnection(BlockingViewerChannel connection) {
		viewerConnections = (BlockingViewerChannel[]) ArrayUtils.add(
				BlockingViewerChannel.class, viewerConnections, connection);
	}

	synchronized void removeViewerConnection(BlockingViewerChannel connection) {
		viewerConnections = (BlockingViewerChannel[]) ArrayUtils.remove(
				viewerConnections, connection);
	}

	// -------------------------------------------------------------------
	// AMonitor API
	// -------------------------------------------------------------------

	public String getStatus(String propertyName) {
		if (propertyName != STATUS_NAME) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		sb.append("--- Viewer Connections ---");

		BlockingViewerChannel[] connections = this.viewerConnections;
		if (connections != null) {
			for (int i = 0, n = connections.length; i < n; i++) {
				BlockingViewerChannel channel = connections[i];
				sb.append('\n').append(i + 1).append(": ").append(
						channel.getName()).append(" (").append(
						channel.getRemoteHost()).append(':').append(
						channel.getRemotePort()).append(')');
			}
		} else {
			sb.append("\n<no connections>");
		}

		return sb.toString();
	}

} // BlockingViewerServer
