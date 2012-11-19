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
 * AgentService
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Dec 13 10:44:29 2002
 * Updated : $Date: 2008-04-04 20:25:04 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3980 $
 */
package se.sics.tasim.aw;

import se.sics.isl.transport.Transportable;

/**
 * <code>AgentService</code> is the abstract base class for the agent service
 * used by {@link Agent} to communicate with the SCM servers (not used directly
 * by agent implementations).
 */
public abstract class AgentService {

	private final Agent agent;
	private String name;
	private String address;

	protected AgentService(Agent agent, String name) {
		if (agent == null || name == null) {
			throw new NullPointerException();
		}
		this.agent = agent;
		this.address = this.name = name;
	}

	// Called after the agent service has been fully initialized
	protected void initializeAgent() {
		agent.init(this);
	}

	protected void simulationSetup(String address) {
		if (address != null) {
			this.address = address;
		}
		agent.simulationSetup();
	}

	protected void simulationStopped() {
		agent.simulationStopped();
	}

	protected void simulationFinished() {
		agent.simulationFinished();
	}

	// -------------------------------------------------------------------
	// Time support
	// -------------------------------------------------------------------

	protected abstract void addTimeListener(TimeListener listener);

	protected abstract void removeTimeListener(TimeListener listener);

	// -------------------------------------------------------------------
	// Information retrieval
	// -------------------------------------------------------------------

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public Agent getAgent() {
		return agent;
	}

	protected abstract long getServerTime();

	// -------------------------------------------------------------------
	// Message handling
	// -------------------------------------------------------------------

	// Should this be protected or package protected??? FIX THIS!!! TODO
	protected void sendMessage(Message message) {
		if (address == null) {
			throw new IllegalStateException("not initialized");
		}

		String sender = message.getSender();
		if (sender == null) {
			message.setSender(address);
		} else if (!sender.equals(address)) {
			throw new SecurityException(
					"Can not send message from other than self: " + "Self="
							+ address + ", Sender=" + sender);
		}
		deliverToServer(message);
	}

	protected abstract void deliverToServer(Message message);

	protected void sendToRole(int role, Transportable content) {
		if (address == null) {
			throw new IllegalStateException("not initialized");
		}

		deliverToServer(role, content);
	}

	protected abstract void deliverToServer(int role, Transportable content);

	protected void deliverToAgent(Message message) {
		agent.messageReceived(message);
	}

} // AgentService
