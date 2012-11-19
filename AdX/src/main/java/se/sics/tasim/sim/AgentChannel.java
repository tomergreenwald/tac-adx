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
 * AgentChannel
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Oct 11 13:26:43 2002
 * Updated : $Date: 2008-04-04 21:23:36 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3984 $
 */
package se.sics.tasim.sim;

import com.botbox.util.ThreadPool;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.AgentService;
import se.sics.tasim.aw.Message;
import se.sics.tasim.props.AdminContent;

public abstract class AgentChannel {

	public static final String ACTIVE_ORDERS = "activeOrders";
	public static final String PING = "ping";

	private Admin admin;
	private int userID;
	private String name;
	private SimulationAgent proxy;
	// Will not allow delivery of messages until correctly logged in
	private boolean isClosed = true;

	// ping handling
	private long pingRequested = 0L;
	private long lastResponeTime = 0L;

	private long totalResponseTime = 0L;
	private int pingCount = 0;

	protected AgentChannel() {
	}

	final void init(Admin admin, String name, String password) {
		if (this.admin != null) {
			throw new IllegalStateException("already initialized");
		}
		this.admin = admin;
		this.name = name;
		this.userID = admin.loginAgentChannel(this, password);

		// Do not allow delivery of messages until correctly logged in
		// which is when this point has been reached.
		isClosed = false;

		admin.agentChannelAvailable(this);
	}

	// API towards simulation agent
	final void setProxyAgent(SimulationAgent proxy) {
		this.proxy = proxy;
	}

	// API towards simulation agent
	final void removeProxyAgent(SimulationAgent proxy) {
		if (this.proxy == proxy) {
			this.proxy = null;
			// Never use the simulation thread pool when not participating
			// in a simulation
			setSimulationThreadPool(null);
		}
	}

	public final int getUserID() {
		return userID;
	}

	public final String getName() {
		return name;
	}

	public abstract boolean isSupported(String name);

	public void addTransportConstant(String name) {
	}

	// -------------------------------------------------------------------
	// Ping and response time handling
	// -------------------------------------------------------------------

	// public void resetResponseTime() {
	// pingRequested = 0L;
	// lastResponeTime = 0L;
	// totalResponseTime = 0L;
	// pingCount = 0;
	// }

	public void requestPing() {
		if (pingRequested > 0L) {
			// Already awaiting pong
		} else {
			pingRequested = System.currentTimeMillis();
			sendPingRequest();
		}
	}

	protected abstract boolean sendPingRequest();

	protected void pongReceived() {
		long requested = pingRequested;
		pingRequested = 0L;
		if (requested > 0L) {
			long responseTime = System.currentTimeMillis() - requested;
			lastResponeTime = responseTime;
			totalResponseTime += responseTime;
			pingCount++;
		}
	}

	public int getPingCount() {
		return pingCount;
	}

	public long getLastResponseTime() {
		return lastResponeTime;
	}

	public long getAverageResponseTime() {
		return pingCount == 0 ? 0L : (totalResponseTime / pingCount);
	}

	public abstract String getRemoteHost();

	protected abstract void setSimulationThreadPool(ThreadPool threadPool);

	protected void deliverFromAgent(Message message) {
		// Deliver to admin or simulation (when simulation exists)
		String receiver = message.getReceiver();
		SimulationAgent proxy;
		if (isClosed) {
			// closed channel
		} else if (Admin.ADMIN.equals(receiver)) {
			Transportable content = message.getContent();
			if (message.getSender() == null) {
				message.setSender(name);
			}
			if ((content instanceof AdminContent)
					&& (((AdminContent) content).getType() == AdminContent.QUIT)) {
				// Time to close this connection
				deliverToAgent(message.createReply(new AdminContent(
						AdminContent.QUIT)));
				close();

			} else {
				admin.deliverMessageFromAgent(this, message);
			}

		} else if ((proxy = this.proxy) != null) {
			proxy.deliverFromAgent(message);

		} else {
			// not a valid receiver because the agent is not in a game
		}
	}

	protected abstract void deliverToAgent(Message message);

	public final boolean isClosed() {
		return isClosed;
	}

	public final void close() {
		if (!isClosed) {
			isClosed = true;
			admin.logoutAgentChannel(this);

			SimulationAgent proxy = this.proxy;
			if (proxy != null) {
				this.proxy = null;
				proxy.removeAgentChannel(this);
			}
		}
		closeChannel();
	}

	protected abstract void closeChannel();

	// -------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------

	static int indexOf(AgentChannel[] array, int start, int end, int userID) {
		for (int i = start; i < end; i++) {
			if (array[i].userID == userID) {
				return i;
			}
		}
		return -1;
	}

} // AgentChannel
