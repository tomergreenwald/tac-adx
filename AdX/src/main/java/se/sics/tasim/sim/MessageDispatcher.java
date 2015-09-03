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
 * MessageDispatcher
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Jan 30 20:33:08 2003
 * Updated : $Date: 2008-04-04 21:23:36 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3984 $
 */
package se.sics.tasim.sim;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.botbox.util.ArrayQueue;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Message;
import tau.tac.adx.report.demand.InitialCampaignMessage;

final class MessageDispatcher extends Thread {

	private static final boolean DEBUG = true;

	private static final Logger log = Logger.getLogger(MessageDispatcher.class
			.getName());

	private final Admin admin;
	private Simulation simulation;
	private ArrayQueue messageQueue = new ArrayQueue();
	private boolean isRunning = true;

	public MessageDispatcher(Admin admin, Simulation simulation, String name) {
		super(name);
		this.admin = admin;
		this.simulation = simulation;
	}

	public void startDispatcher() {
		start();
	}

	public void stopDispatcher() {
		doDeliver("stop");
	}

	private synchronized void doDeliver(Object message) {
		messageQueue.add(message);
		notify();
	}

	private synchronized Object getNext() {
		while (messageQueue.size() == 0) {
			try {
				wait(5000);
			} catch (InterruptedException e) {
				log.log(Level.WARNING, "*** interrupted", e);
			}
		}

		return messageQueue.remove(0);
	}

	public void run() {
		try {
			do {
				Object message = getNext();
				if (!isRunning) {
					// Should stop running immediately
					break;

				} else if (message instanceof Message) {
					if (((Message)message).getContent() instanceof InitialCampaignMessage) {
						int i =1;
					}
					deliverMessage((Message) message);

				} else if (message instanceof Delivery) {
					((Delivery) message).perform(this);

				} else if (message instanceof SimulationAgent) {
					simulation.callRecoverAgent((SimulationAgent) message);

				} else if (message instanceof Runnable) {
					call((Runnable) message);

				} else if (message == "prepareStop") {
					simulation.prepareStop();

				} else if (message == "stop") {
					// Time to stop this simulation (and this dispatcher)
					isRunning = false;
					admin.stopSimulation(simulation);
					break;

				} else {
					log.severe("*** unknown delivery type: " + message);
				}
			} while (isRunning);
		} finally {
			log.finer("message dispatcher " + getName() + " stopped");
			// Ease for the garbage collector
			simulation = null;
			messageQueue.clear();

			System.gc();
		}
	}

	// -------------------------------------------------------------------
	// Interface towards the simulation
	// -------------------------------------------------------------------

	final void deliver(Message message) {
		doDeliver(message);
	}

	final void deliverToRole(SimulationAgent senderAgent, int role,
			Transportable content) {
		doDeliver(new Delivery(senderAgent, role, content));
	}

	final void callRunnable(Runnable target) {
		doDeliver(target);
	}

	final void callAgentUnblock(SimulationAgent agentToUnblock) {
		doDeliver(new Delivery(Delivery.UNBLOCK, agentToUnblock));
	}

	final void callAgentRecovery(SimulationAgent agentToRecover) {
		doDeliver(agentToRecover);
	}

	final void callNextTimeUnit(int timeUnit) {
		doDeliver(new Delivery(Delivery.TIMECALL, timeUnit));
	}

	final void callPrepareStop() {
		doDeliver("prepareStop");
	}

	// -------------------------------------------------------------------
	// Delivery performers
	// -------------------------------------------------------------------

	private void nextTimeUnit(int timeUnit) {
		simulation.callNextTimeUnit(timeUnit);
	}

	private void call(Runnable target) {
		try {
			target.run();
		} catch (ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			log.log(Level.SEVERE, "could not invoke " + target, e);
		}
	}

	private void deliverMessage(Message message) {
		String receiver = message.getReceiver();
		SimulationAgent agent;
		try {
			if (Admin.ADMIN.equals(receiver)) {
				log.finest("delivering " + message);
				admin.messageReceived(simulation, message);

			} else if (Simulation.COORDINATOR.equals(receiver)) {
				// Message to this simulation
				log.finest("delivering " + message);
				simulation.messageReceived(message);

			} else if ((agent = simulation.getAgent(receiver)) != null) {
				if (simulation.validateMessage(agent, message)) {
					log.finest("delivering " + message);
					agent.messageReceived(simulation, message);
					if (DEBUG)
						log.finest("delivered to " + receiver);
				} else {
					log.warning("message not permitted: " + message);
				}

			} else {
				// Unknown receiver
				log.warning("unknown receiver '" + receiver + "' for "
						+ message);
			}
		} catch (ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			log.log(Level.SEVERE, "could not deliver message " + message, e);
		}
	}

	private void deliverMessageToRole(int role, Transportable content) {
		// From COORDINATOR to all agents with the specified role
		if (simulation.validateMessageToRole(role, content)) {
			SimulationAgent[] agents = simulation.getAgents(role);
			if (agents != null) {
				log.finest("delivering to role " + role + ": " + content);
				for (int i = 0, n = agents.length; i < n; i++) {
					SimulationAgent agent = agents[i];
					try {
						String receiver = agent.getAddress();
						if (DEBUG)
							log.finest("delivering to " + receiver);
						agent.messageReceived(simulation, new Message(
								Simulation.COORDINATOR, receiver, content));
						if (DEBUG)
							log.finest("delivered to " + receiver);
					} catch (ThreadDeath e) {
						throw e;
					} catch (Throwable e) {
						log.log(Level.SEVERE, "agent " + agent.getName()
								+ " could not handle message " + content, e);
					}
				}
			}
		} else {
			log.warning("message from coordinator to role " + role
					+ " not permitted: " + content);
		}
	}

	private void deliverMessageToRole(SimulationAgent senderAgent, int role,
			Transportable content) {
		String senderName = senderAgent.getName();
		if (simulation.validateMessageToRole(senderAgent, role, content)) {
			SimulationAgent[] agents = simulation.getAgents(role);
			if (agents != null) {
				String senderAddress = senderAgent.getAddress();
				log.finest("delivering from " + senderName + " ("
						+ senderAddress + ") to role " + role + ": " + content);
				for (int i = 0, n = agents.length; i < n; i++) {
					SimulationAgent a = agents[i];
					if (a != senderAgent) {
						try {
							String receiver = a.getAddress();
							if (DEBUG)
								log.finest("delivering to " + receiver);
							a.messageReceived(simulation, new Message(
									senderAddress, receiver, content));
							if (DEBUG)
								log.finest("delivered to " + receiver);
						} catch (ThreadDeath e) {
							throw e;
						} catch (Throwable e) {
							log
									.log(Level.SEVERE, "agent " + a.getName()
											+ " could not handle message "
											+ content, e);
						}
					}
				}
			}
		} else {
			log.warning("message from " + senderName + " to role " + role
					+ " not permitted: " + content);
		}
	}

	// -------------------------------------------------------------------
	// Data container for event delivery
	// -------------------------------------------------------------------

	private static class Delivery {

		public static final int MESSAGE = 0;
		public static final int TIMECALL = 1;
		public static final int UNBLOCK = 2;

		private final int flag;

		private final SimulationAgent senderAgent;
		private int role;
		private Transportable content;

		private int timeUnit;

		public Delivery(SimulationAgent senderAgent, int role,
				Transportable content) {
			this.flag = MESSAGE;
			this.senderAgent = senderAgent;
			this.role = role;
			this.content = content;
		}

		public Delivery(int flag, SimulationAgent agent) {
			this.flag = flag;
			this.senderAgent = agent;
		}

		public Delivery(int flag, int timeUnit) {
			this.flag = flag;
			this.senderAgent = null;
			this.timeUnit = timeUnit;
		}

		public void perform(MessageDispatcher dispatcher) {
			switch (flag) {
			case MESSAGE:
				if (senderAgent == null) {
					dispatcher.deliverMessageToRole(role, content);
				} else {
					dispatcher.deliverMessageToRole(senderAgent, role, content);
				}
				break;
			case TIMECALL:
				dispatcher.nextTimeUnit(timeUnit);
				break;
			case UNBLOCK:
				if (senderAgent != null) {
					senderAgent.setBlocked(false);
				}
				break;
			}
		}
	}

	// DEBUG FINALIZE REMOVE THIS!!! REMOVE THIS!!!
	protected void finalize() throws Throwable {
		log.info("Message dispatcher " + getName() + " IS BEING GARBAGED");
		super.finalize();
	}

} // MessageDispatcher
