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
 * SimulationAgent
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Dec 13 11:42:32 2002
 * Updated : $Date: 2008-04-04 21:23:36 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3984 $
 */
package se.sics.tasim.sim;

import java.util.logging.Logger;

import com.botbox.util.ArrayUtils;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.AgentService;
import se.sics.tasim.aw.Message;
import se.sics.tasim.aw.TimeListener;

public class SimulationAgent extends AgentService {

	private Simulation simulation;
	private boolean isRunning = false;
	private boolean hasAgentBeenActive = false;
	private boolean isBlocked = false;

	private int index;
	private int role;

	private int participantID = -1;

	// Proxy handling
	private boolean isProxy = false;
	private AgentChannel channel;

	private MessageListener[] messageListeners;

	public SimulationAgent(Agent agent, String name) {
		super(agent, name);
	}

	final void setup(Simulation simulation, int index, String address,
			int role, int participantID) {
		if (simulation == null || address == null) {
			throw new NullPointerException();
		}
		this.index = index;
		this.simulation = simulation;
		this.role = role;
		this.participantID = participantID;
		isRunning = true;
		isBlocked = false;
		initializeAgent();
		simulationSetup(address);
	}

	final void stop() {
		try {
			simulationStopped();
		} finally {
			isRunning = false;
		}
	}

	final void shutdown() {
		try {
			simulationFinished();
		} finally {
			AgentChannel channel = this.channel;
			this.channel = null;
			if (channel != null) {
				// Deregister from the agent channel because this agent
				// service will never need to communicate again. A new agent
				// will be created in next simulation.
				channel.removeProxyAgent(this);
			}
			// Ease the job for the garbage collector + make sure the agent
			// does nothing more
			simulation = null;
		}
	}

	int getParticipantID() {
		return participantID;
	}

	// -------------------------------------------------------------------
	// Proxy handling
	// -------------------------------------------------------------------

	boolean isProxy() {
		return isProxy;
	}

	void setProxy(boolean isProxy) {
		this.isProxy = isProxy;
	}

	boolean isBlocked() {
		return isBlocked;
	}

	void setBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}

	boolean hasAgentChannel() {
		return channel != null;
	}

	AgentChannel getAgentChannel() {
		return channel;
	}

	/**
	 * Sets the agent channel for this agent and registers this agent service
	 * with the channel.
	 * 
	 * @param channel
	 *            the communication channel to use with this agent
	 */
	// API towards Simulation
	synchronized void setAgentChannel(AgentChannel channel, boolean recover) {
		if (channel != this.channel) {
			if (!isProxy) {
				throw new IllegalStateException("proxy mode not supported in "
						+ getName());
			}
			if (channel == null) {
				throw new NullPointerException();
			}

			// Deregister from any old channel
			if (this.channel != null) {
				this.channel.removeProxyAgent(this);
			}
			this.channel = channel;
			// Register at the new agent channel
			channel.setProxyAgent(this);
			channel.setSimulationThreadPool(simulation
					.getSimulationThreadPool());

			if (simulation != null && recover) {
				// Since a simulation is running we need to do a recovery of
				// all needed information.
				simulation.requestAgentRecovery(this);
			}
		}
	}

	// API towards AgentChannel
	synchronized void removeAgentChannel(AgentChannel channel) {
		if (channel == this.channel) {
			this.channel = null;
		}
	}

	// -------------------------------------------------------------------
	// Internal Information access
	// -------------------------------------------------------------------

	public boolean isSupported(String name) {
		AgentChannel channel = this.channel;
		return (channel != null) ? channel.isSupported(name) : false;
	}

	public void requestPing() {
		AgentChannel channel = this.channel;
		if (channel != null) {
			channel.requestPing();
		}
	}

	public int getPingCount() {
		AgentChannel channel = this.channel;
		return (channel != null) ? channel.getPingCount() : 0;
	}

	public long getLastResponseTime() {
		AgentChannel channel = this.channel;
		return (channel != null) ? channel.getLastResponseTime() : 0L;
	}

	public long getAverageResponseTime() {
		AgentChannel channel = this.channel;
		return (channel != null) ? channel.getAverageResponseTime() : 0L;
	}

	/**
	 * Returns <code>true</code> if the agent been active in this simulation
	 * (i.e. sent messages) and <code>false</code> otherwise.
	 */
	public boolean hasAgentBeenActive() {
		return hasAgentBeenActive;
	}

	public int getIndex() {
		return index;
	}

	public int getRole() {
		return role;
	}

	// -------------------------------------------------------------------
	// Information retrieval
	// -------------------------------------------------------------------

	protected long getServerTime() {
		return simulation.getServerTime();
	}

	// -------------------------------------------------------------------
	// Time support
	// -------------------------------------------------------------------

	protected void addTimeListener(TimeListener listener) {
		simulation.addTimeListener(listener);
	}

	protected void removeTimeListener(TimeListener listener) {
		simulation.removeTimeListener(listener);
	}

	// -------------------------------------------------------------------
	// Message handling
	// -------------------------------------------------------------------

	protected void deliverFromAgent(Message message) {
		super.sendMessage(message);
	}

	protected void deliverToServer(Message message) {
		if (isRunning) {
			hasAgentBeenActive = true;
			fireMessageSent(message);
			simulation.deliverMessage(message);
		}
	}

	protected void deliverToServer(int role, Transportable content) {
		if (isRunning) {
			hasAgentBeenActive = true;
			fireMessageSent(role, content);
			simulation.deliverMessageToRole(this, role, content);
		}
	}

	final void messageReceived(Simulation simulation, Message message) {
		if (simulation != this.simulation) {
			throw new SecurityException("message from wrong simulation");
		}

		fireMessageReceived(message);

		if (isBlocked) {
			// All messages are blocked and will not be delivered to the agent

		} else {
			if (isProxy) {
				AgentChannel channel = this.channel;
				if (channel != null) {
					channel.deliverToAgent(message);
				}
			} else {
				deliverToAgent(message);
			}
		}
	}

	// -------------------------------------------------------------------
	// MessageListener support
	// -------------------------------------------------------------------

	public synchronized void addMessageListener(MessageListener listener) {
		messageListeners = (MessageListener[]) ArrayUtils.add(
				MessageListener.class, messageListeners, listener);
	}

	public synchronized void removeMessageListener(MessageListener listener) {
		messageListeners = (MessageListener[]) ArrayUtils.remove(
				messageListeners, listener);
	}

	private void fireMessageReceived(Message message) {
		MessageListener[] listeners = this.messageListeners;
		if (listeners != null) {
			String sender = message.getSender();
			Transportable content = message.getContent();
			for (int i = 0, n = listeners.length; i < n; i++) {
				listeners[i].messageReceived(this, sender, content);
			}
		}
	}

	// Called by Simulation whenever a message has been sent by this agent
	private void fireMessageSent(Message message) {
		MessageListener[] listeners = this.messageListeners;
		if (listeners != null) {
			String receiver = message.getReceiver();
			Transportable content = message.getContent();
			for (int i = 0, n = listeners.length; i < n; i++) {
				listeners[i].messageSent(this, receiver, content);
			}
		}
	}

	private void fireMessageSent(int role, Transportable content) {
		MessageListener[] listeners = this.messageListeners;
		if (listeners != null) {
			for (int i = 0, n = listeners.length; i < n; i++) {
				listeners[i].messageSent(this, role, content);
			}
		}
	}

	// -------------------------------------------------------------------
	// Utility methods
	// -------------------------------------------------------------------

	// static int indexOf(SimulationAgent[] array, String address) {
	// if (array != null) {
	// for (int i = 0, n = array.length; i < n; i++) {
	// if (address.equals(array[i].address)) {
	// return i;
	// }
	// }
	// }
	// return -1;
	// }

	static int indexOf(SimulationAgent[] array, int participantID) {
		if (array != null) {
			for (int i = 0, n = array.length; i < n; i++) {
				SimulationAgent a = array[i];
				if (a.participantID == participantID) {
					return i;
				}
			}
		}
		return -1;
	}

	// DEBUG FINALIZE REMOVE THIS!!! REMOVE THIS!!!
	protected void finalize() throws Throwable {
		Logger.global.info("SIMULATIONAGENT " + getName() + " (" + getAddress()
				+ ',' + participantID + ") IS BEING GARBAGED");
		super.finalize();
	}

} // SimulationAgent
