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
 * Agent
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Dec 13 10:44:08 2002
 * Updated : $Date: 2008-04-04 20:25:04 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3980 $
 */
package se.sics.tasim.aw;

import java.util.Enumeration;
import java.util.Hashtable;

import se.sics.isl.transport.Transportable;

/**
 * The abstract class <code>Agent</code> should be inherited by all
 * implementations of agents that wants to be able to participate in TAC Games
 * or other Trading Agent Simulations on the TAC SCM Simulator.
 * 
 * Agents can be run both within the Simulator as built-in agents or they can
 * run outside the Simulator (in the AgentWare) and connect to it via Internet.
 * 
 * Features of Agent:
 * <ul>
 * <li>Methods for sending messages to the current game
 * <li>Receives messages from the current game
 * <li>Exists only during a game/simulation
 * <li>Methods for time handling (server time and time listener)
 * </ul>
 * 
 * Note: the agent must wait for its initialization before calling any of the
 * inherited methods! The agent is initialized by a call to
 * <code>simulationSetup</code>.
 */
public abstract class Agent {

	/** The address to the simulation coordinator */
	public static final String COORDINATOR = "coordinator";

	/** The address to the server administrator */
	public static final String ADMIN = "admin";

	private static int lastID = 0;

	private synchronized int generateNextID() {
		return lastID++;
	}

	private AgentService service;

	protected Agent() {
	}

	/**
	 * Initializes this agent.
	 * <p>
	 * 
	 * This method is package protected to ensure that only AgentService can
	 * call it.
	 * 
	 * @param service
	 *            the <code>AgentService</code> to use for this agent
	 */
	final void init(AgentService service) {
		if (this.service != null) {
			throw new IllegalStateException("already initialized");
		}
		this.service = service;
	}

	// -------------------------------------------------------------------
	// Information retrieval
	// -------------------------------------------------------------------

	/**
	 * Returns the name of this agent
	 */
	public String getName() {
		return service.getName();
	}

	/**
	 * Returns the address of this agent
	 */
	public String getAddress() {
		return service.getAddress();
	}

	/**
	 * Returns the server time. Note that this is an approximation when agent is
	 * not executing as "built-in".
	 */
	protected long getServerTime() {
		return service.getServerTime();
	}

	/**
	 * Returns a unique ID for use in various messages
	 */
	protected int getNextID() {
		return generateNextID();
	}

	// -------------------------------------------------------------------
	// Time support
	// -------------------------------------------------------------------

	/**
	 * Adds the specified time listener to receive notifications about time
	 * units.
	 * 
	 * @param listener
	 *            the time listener
	 */
	protected void addTimeListener(TimeListener listener) {
		service.addTimeListener(listener);
	}

	/**
	 * Removes the specified time listener so that it no longer receives
	 * notifications about time units.
	 * 
	 * @param listener
	 *            the time listener
	 */
	protected void removeTimeListener(TimeListener listener) {
		service.removeTimeListener(listener);
	}

	// -------------------------------------------------------------------
	// Communication with server
	// -------------------------------------------------------------------

	/**
	 * Send a message to another agent in a game/simulation
	 * 
	 * @param message
	 *            to send
	 */
	protected void sendMessage(Message message) {
		service.sendMessage(message);
	}

	/**
	 * Create and send a message to another agent in a game/simulation
	 * 
	 * @param receiver
	 *            of the message
	 * @param content
	 *            of the message
	 */
	protected void sendMessage(String receiver, Transportable content) {
		service.sendMessage(new Message(receiver, content));
	}

	/**
	 * Extract all receivers and message contents from the hash table and sends
	 * them.
	 * 
	 * @param messageTable
	 *            a <code>Hashtable</code> mapping agent addresses (receivers)
	 *            with Transportable objects (message contents)
	 */
	protected void sendMessages(Hashtable messageTable) {
		Enumeration enumeration = messageTable.keys();
		while (enumeration.hasMoreElements()) {
			String receiver = (String) enumeration.nextElement();
			Transportable content = (Transportable) messageTable.get(receiver);
			sendMessage(receiver, content);
		}
	}

	protected void sendToRole(int role, Transportable content) {
		service.sendToRole(role, content);
	}

	/**
	 * messageReceived is called when a message to the agent is received.
	 * 
	 * @param message
	 *            the received message
	 */
	protected abstract void messageReceived(Message message);

	// -------------------------------------------------------------------
	// Agent management APIs
	// -------------------------------------------------------------------

	/**
	 * Called when a game/simulation is starting and the agent should initialize
	 */
	protected abstract void simulationSetup();

	/**
	 * Called when a game/simulation is in the process of being stopped. The
	 * agent might still receive messages after this call has been made but can
	 * not send messages itself.
	 */
	protected void simulationStopped() {
	}

	/**
	 * Called when a game/simulation is finished and the agent should free its
	 * resources.
	 */
	protected abstract void simulationFinished();

} // Agent
