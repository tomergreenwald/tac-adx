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
 * Simulation
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Oct 07 17:59:48 2002
 * Updated : $Date: 2008-04-11 19:05:05 -0500 (Fri, 11 Apr 2008) $
 *           $Revision: 4087 $
 */
package se.sics.tasim.sim;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

import com.botbox.util.ArrayUtils;
import com.botbox.util.ThreadPool;
import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;
import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.Message;
import se.sics.tasim.aw.TimeListener;
import se.sics.tasim.is.EventWriter;
import se.sics.tasim.is.SimulationInfo;
import tau.tac.adx.sim.TACAdxConstants;

//TODO-MODIFY CLASS
public abstract class Simulation {

	private final static Logger log = Logger.getLogger(Simulation.class
			.getName());

	public final static String COORDINATOR = Agent.COORDINATOR;
	public final static int COORDINATOR_INDEX = 0;

	private final static int INIT_STATUS = 0;
	private final static int STARTED_STATUS = 1;
	private final static int PREPARED_STOP_STATUS = 2;
	private final static int STOPPED_STATUS = 3;

	protected final static int RECOVERY_NONE = 0;
	protected final static int RECOVERY_IMMEDIATELY = 1;
	protected final static int RECOVERY_AFTER_NEXT_TICK = 2;

	private Random random = new Random();

	private Admin admin;
	private ConfigManager config;
	private SimulationInfo info;
	private EventWriter rootEventWriter;
	private EventWriter eventWriter;

	private ThreadPool simulationThreadPool;

	private Hashtable agentTable = new Hashtable();
	private SimulationAgent[] agentList;

	private int[] agentRoles;
	private SimulationAgent[][] agentsPerRole;
	private int agentRoleNumber;

	private int runtimeStatus = INIT_STATUS;
	private int currentTimeUnit;

	private String logFileName;
	private LogWriter logWriter;

	private TimeListener[] timeListeners;
	private MessageDispatcher dispatcher;

	private Timer timer;
	private TimerTask tickTask;

	private SimulationAgent[] agentsToRecover;
	private boolean hasAgentsToRecover = false;

	/**
	 * Counter for dummy agents in this simulation. Used to generation
	 * participant ids for the dummy agents.
	 */
	private int dummyAgentCounter = -1;

	protected Simulation(ConfigManager config) {
		this.config = config;
	}

	final void init(Admin admin, SimulationInfo info, String logFileName,
			ThreadPool simulationThreadPool) {
		this.admin = admin;
		this.info = info;
		this.logFileName = logFileName;
		this.simulationThreadPool = simulationThreadPool;
		this.rootEventWriter = admin.getEventWriter();
		if (this.config == null) {
			this.config = admin.getConfig();
		}

		this.dispatcher = new MessageDispatcher(admin, this, "sim"
				+ info.getSimulationID());
	}

	// Called by the admin when it is time to setup the simulation
	final void setup() throws IllegalConfigurationException {
		if (runtimeStatus == INIT_STATUS) {
			setupSimulation();

			// Now all agents must have joined the simulation
			getSimulationInfo().setFull();
		}
	}

	// Called by the admin when it is time to start the simulation
	final void start() {
		if (runtimeStatus == INIT_STATUS) {
			runtimeStatus = STARTED_STATUS;

			// Log information about this simulation to the simulation log
			LogWriter writer = getLogWriter();
			synchronized (writer) {
				String params = info.getParams();
				writer.node("simulation").attr("simID", info.getSimulationID())
						.attr("id", info.getID()).attr("type", info.getType());
				if (params != null) {
					writer.attr("params", params);
				}
				writer.attr("startTime", info.getStartTime()).attr("length",
						info.getSimulationLength() / 1000).attr("serverName",
						admin.getServerName()).attr("version",
						Admin.SERVER_VERSION);

				SimulationAgent[] agents = getAgents();
				if (agents != null) {
					for (int i = 0, n = agents.length; i < n; i++) {
						SimulationAgent a = agents[i];
						int participantID = a.getParticipantID();
						String name = a.getName();
						String address = a.getAddress();
						int index = a.getIndex();
						int role = a.getRole();
						writer.node("participant");
						writer.attr("index", index);
						writer.attr("role", role);
						writer.attr("address", address);
						if (participantID >= 0) {
							writer.attr("name", name);
							writer.attr("id", participantID);
						}
						writer.endNode("participant");

						// Notify information/viewer system about participants
						rootEventWriter.participant(index, role, name,
								participantID);
					}
				}
				writer.endNode("simulation");
				writer.commit();
			}

			setupAgents();
			this.dispatcher.startDispatcher();
			startSimulation();
		}
	}

	// Associate all participating agents with their agent channel
	private void setupAgents() {
		SimulationAgent[] agents = getAgents();
		if (agents != null) {
			for (int i = 0; i < agents.length; i++) {
				SimulationAgent agent = agents[i];
				int participantID = agent.getParticipantID();
				if (participantID >= 0 && agent.isProxy()) {
					AgentChannel channel = agent.getAgentChannel();
					if (channel != null) {
						setupAgentChannel(channel);
					} else {
						channel = admin.getAgentChannel(participantID);
						if (channel != null) {
							setupAgentChannel(channel);
							agent.setAgentChannel(channel, false);
						}
					}
				}
			}
		}
	}

	private void setupAgentChannel(AgentChannel channel) {
		SimulationAgent[] agents = getAgents();
		if (agents != null) {
			for (int i = 0; i < agents.length; i++) {
				// Add the agent addresses as constants because they are
				// transfered a lot
				channel.addTransportConstant(agents[i].getAddress());
			}
		}
	}

	// Called by the message dispatcher when the simulation should
	// prepare to be stopped
	final void prepareStop() {
		if (runtimeStatus == STARTED_STATUS) {
			runtimeStatus = PREPARED_STOP_STATUS;
			SimulationInfo info = getSimulationInfo();
			log.fine("Simulation " + info.getSimulationID()
					+ " is preparing to stop");

			// Stop all agents
			SimulationAgent[] agents = getAgents();
			if (agents != null) {
				for (int i = 0; i < agents.length; i++) {
					try {
						agents[i].stop();
					} catch (ThreadDeath e) {
						log.log(Level.SEVERE, "could not stop agent "
								+ agents[i].getName(), e);
						throw e;
					} catch (Throwable e) {
						log.log(Level.SEVERE, "could not stop agent "
								+ agents[i].getName(), e);
					}
				}
			}

			try {
				prepareStopSimulation();
			} finally {
				dispatcher.stopDispatcher();
			}
		}
	}

	// Called by the admin at the time for the final stopping procedure
	final void completeStop() {
		if (runtimeStatus == PREPARED_STOP_STATUS
				|| runtimeStatus == STARTED_STATUS) {
			runtimeStatus = STOPPED_STATUS;

			SimulationInfo info = getSimulationInfo();
			log.fine("Simulation " + info.getSimulationID()
					+ " is being stopped");

			if (tickTask != null) {
				tickTask.cancel();
				tickTask = null;
			}

			if (timer != null) {
				timer.cancel();
				timer = null;
			}

			// Stop all agents
			SimulationAgent[] agents = getAgents();
			if (agents != null) {
				for (int i = 0; i < agents.length; i++) {
					try {
						agents[i].shutdown();
					} catch (ThreadDeath e) {
						log.log(Level.SEVERE, "could not shutdown agent "
								+ agents[i].getName(), e);
						throw e;
					} catch (Throwable e) {
						log.log(Level.SEVERE, "could not shutdown agent "
								+ agents[i].getName(), e);
					}
				}
			}

			completeStopSimulation();

			// Forget about all agents to ease the job for the garbage collector
			clearTimeListeners();
			synchronized (this) {
				agentTable.clear();
				agentList = null;
				agentRoleNumber = 0;
				agentRoles = null;
				agentsPerRole = null;
			}
		}
	}

	/**
	 * Called after a simulation has stopped to ensure all resources has been
	 * cleaned up (simulation log closed, etc).
	 */
	final void close() {
		if (this.logWriter != null) {
			this.logWriter.close();
			this.eventWriter = this.rootEventWriter;
		}
	}

	final void agentChannelAvailable(AgentChannel channel) {
		SimulationAgent agent;
		if (runtimeStatus != STOPPED_STATUS
				&& ((agent = getAgent(channel.getUserID())) != null)
				&& agent.isProxy()) {
			setupAgentChannel(channel);
			agent.setAgentChannel(channel, runtimeStatus == STARTED_STATUS);
		}
	}

	final void requestAgentRecovery(SimulationAgent agent) {
		int mode = getAgentRecoverMode(agent);
		switch (mode) {
		case RECOVERY_NONE:
			// No recovery call needed
			break;
		case RECOVERY_IMMEDIATELY:
			if (dispatcher != null) {
				agent.setBlocked(true);
				dispatcher.callAgentRecovery(agent);
			}
			break;
		case RECOVERY_AFTER_NEXT_TICK:
			agent.setBlocked(true);
			synchronized (this) {
				if (ArrayUtils.indexOf(agentsToRecover, agent) < 0) {
					agentsToRecover = (SimulationAgent[]) ArrayUtils.add(
							SimulationAgent.class, agentsToRecover, agent);
				}
				hasAgentsToRecover = true;
			}
			break;
		default:
			log.warning("unknown agent recovery mode " + mode + " for agent "
					+ agent.getName());
			break;
		}
	}

	final void callRecoverAgent(SimulationAgent agent) {
		try {
			if (agent.isBlocked()) {
				if (dispatcher == null) {
					agent.setBlocked(false);
				} else {
					dispatcher.callAgentUnblock(agent);
				}
			}
			recoverAgent(agent);
		} catch (ThreadDeath e) {
			log.log(Level.SEVERE, "could not recover agent " + agent.getName(),
					e);
			throw e;
		} catch (Throwable e) {
			log.log(Level.SEVERE, "could not recover agent " + agent.getName(),
					e);
		}
	}

	protected void requestStopSimulation() {
		log.finest("***** END OF SIMULATION REQUESTED *****");

		// No more ticks and timer events because the simulation is going to
		// stop
		if (tickTask != null) {
			tickTask.cancel();
			tickTask = null;
		}

		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		clearTimeListeners();
		if (runtimeStatus == STARTED_STATUS) {
			SimulationInfo info = getSimulationInfo();
			log.fine("Simulation " + info.getSimulationID()
					+ " is requested to stop");
			dispatcher.callPrepareStop();
		}
	}

	// -------------------------------------------------------------------
	//
	// -------------------------------------------------------------------

	public ConfigManager getConfig() {
		return config;
	}

	public long getServerTime() {
		return admin.getServerTime();
	}

	public SimulationInfo getSimulationInfo() {
		return info;
	}

	public Random getRandom() {
		return random;
	}

	// public Random getRandom(int val) {
	// walkRandom;
	// }

	public ThreadPool getSimulationThreadPool() {
		return simulationThreadPool;
	}

	// Should this be public??? FIX THIS!!!
	public EventWriter getEventWriter() {
		if (eventWriter == null) {
			// Initialize the logging and log event writer
			getLogWriter();
		}
		return eventWriter;
	}

	protected LogWriter getLogWriter() {
		if (logWriter == null) {
			synchronized (this) {
				if (logWriter == null) {
					try {
						FileOutputStream out = new FileOutputStream(logFileName);
						logWriter = new LogWriter(rootEventWriter, out);
						// Use the log writer as the default event writer
						eventWriter = logWriter;
					} catch (Exception e) {
						log.log(Level.SEVERE,
								"could not open simulation log for simulation "
										+ info.getSimulationID(), e);
					}
					if (logWriter == null) {
						logWriter = new LogWriter(rootEventWriter);
						// Use the log writer as the default event writer
						eventWriter = logWriter;
					}
					// Allow the log file name to be garbaged
					logFileName = null;
				}
			}
		}
		return logWriter;
	}

	public int agentIndex(String name) {
		return ((SimulationAgent) agentTable.get(name)).getIndex();
	}

	protected SimulationAgent getAgent(String name) {
		return (SimulationAgent) agentTable.get(name);
	}

	protected SimulationAgent getAgent(int participantID) {
		SimulationAgent[] agents = getAgents();
		int index = SimulationAgent.indexOf(agents, participantID);
		return index >= 0 ? agents[index] : null;
	}

	protected SimulationAgent[] getAgents() {
		SimulationAgent[] agents = this.agentList;
		if (agents == null) {
			synchronized (this) {
				if ((agents = this.agentList) == null) {
					agents = new SimulationAgent[agentTable.size()];

					Enumeration e = agentTable.elements();
					while (e.hasMoreElements()) {
						SimulationAgent a = (SimulationAgent) e.nextElement();
						// Index 0 is reserved for coordinator
						agents[a.getIndex() - 1] = a;
					}
					this.agentList = agents;
				}
			}
		}
		return agents;
	}

	public SimulationAgent[] getAgents(int role) {
		//FIXME this code shouldn't be here after the final seperation of the two games (tac and adx).
		if (role == TACAdxConstants.PUBLISHER || role == TACAdxConstants.USERS) {
			return new SimulationAgent[] {};
		}
		int index = ArrayUtils.indexOf(agentRoles, 0, agentRoleNumber, role);
		if (index < 0) {
			synchronized (this) {
				index = ArrayUtils
						.indexOf(agentRoles, 0, agentRoleNumber, role);
				if (index < 0) {
					if (agentRoles == null) {
						agentRoles = new int[5];
						agentsPerRole = new SimulationAgent[5][];
					} else if (agentRoleNumber == agentRoles.length) {
						agentRoles = ArrayUtils.setSize(agentRoles,
								agentRoleNumber + 5);
						agentsPerRole = (SimulationAgent[][]) ArrayUtils
								.setSize(agentsPerRole, agentRoleNumber + 5);
					}

					ArrayList list = new ArrayList();
					SimulationAgent[] agents = getAgents();
					if (agents != null) {
						for (int i = 0, n = agents.length; i < n; i++) {
							SimulationAgent a = agents[i];
							if ((a != null) && (a.getRole() == role)) {
								list.add(a);
							}
						}
					}

					index = agentRoleNumber;
					if (list.size() > 0) {
						agentsPerRole[agentRoleNumber] = (SimulationAgent[]) list
								.toArray(new SimulationAgent[list.size()]);
					} else {
						agentsPerRole[agentRoleNumber] = null;
					}
					agentRoles[agentRoleNumber++] = role;
				}
			}
		}
		return agentsPerRole[index];
	}

	/**
	 * Registers the specified agent in this simulation and initializes it. The
	 * agent will be registered with the specified name as address. If the agent
	 * is an registered agent it will be given the registered agent name instead
	 * of the specified name.
	 * 
	 * @param agent
	 *            the agent to be registered
	 * @param name
	 *            the name of the agent
	 * @param role
	 *            the role of the agent
	 * @param participantID
	 *            the id of the agent or -1 if the agent is not a registered
	 *            agent
	 */
	protected SimulationAgent registerAgent(Agent agent, String name, int role,
			int participantID) {
		// Give the agent the name of the user account if it is a
		// registered agent.
		String agentName = name;
		if (participantID >= 0) {
			String userName = admin.getUserName(participantID);
			if (userName != null) {
				agentName = userName;
			}
		}

		SimulationAgent simAgent = new SimulationAgent(agent, agentName);
		int index;
		synchronized (this) {
			// Index 0 is reserved for the coordinator
			index = agentTable.size() + 1;
			agentTable.put(name, simAgent);
			// Clear the agent list and roles because a new agent was added
			agentList = null;
			agentRoleNumber = 0;
		}
		simAgent.setup(this, index, name, role, participantID);
		return simAgent;
	}

	/**
	 * Causes <i>target.run()</i> to be executed with the simulation thread.
	 */
	protected void invokeLater(final Runnable target) {
		dispatcher.callRunnable(target);
	}

	// -------------------------------------------------------------------
	// Time support handling
	// -------------------------------------------------------------------

	protected void startTickTimer(long startServerTime, int millisPerTimeUnit) {
		if (millisPerTimeUnit <= 0) {
			throw new IllegalArgumentException(
					"millisPerTimeUnit must be positive: " + millisPerTimeUnit);
		}
		if (runtimeStatus == STOPPED_STATUS) {
			throw new IllegalStateException("simulation has ended");
		}
		if (this.timer != null) {
			throw new IllegalStateException("timer already started");
		}

		timer = new Timer();
		tickTask = new TimerTask() {
			public void run() {
				performTick();
			}
		};
		// Must handle the difference between the server time and the system
		// time
		long startTime = startServerTime - admin.getTimeDiff();
		timer.scheduleAtFixedRate(tickTask, new Date(startTime),
				millisPerTimeUnit);
	}

	private void performTick() {
		int timeUnit = currentTimeUnit++;
		log.finest("***** START OF TIME " + timeUnit + " REQUESTED *****");
		dispatcher.callNextTimeUnit(timeUnit);
	}

	protected TimeListener[] getTimeListeners() {
		return timeListeners;
	}

	protected synchronized void addTimeListener(TimeListener listener) {
		timeListeners = (TimeListener[]) ArrayUtils.add(TimeListener.class,
				timeListeners, listener);
	}

	protected synchronized void removeTimeListener(TimeListener listener) {
		timeListeners = (TimeListener[]) ArrayUtils.remove(timeListeners,
				listener);
	}

	protected void clearTimeListeners() {
		timeListeners = null;
	}

	// Called by the message dispatcher when a new time arrives
	final void callNextTimeUnit(int timeUnit) {
		// Start of next time unit
		log.info("***** START OF TIME " + timeUnit + " *****");
		try {
			nextTimeUnitStarted(timeUnit);
		} catch (ThreadDeath e) {
			log.log(Level.SEVERE, "could not deliver time unit " + timeUnit
					+ " to simulation", e);
			throw e;
		} catch (Throwable e) {
			log.log(Level.SEVERE, "could not deliver time unit " + timeUnit
					+ " to simulation", e);
		}
		preNextTimeUnit(timeUnit);
		TimeListener[] listeners = getTimeListeners();
		if (listeners != null) {
			for (int i = 0, n = listeners.length; i < n; i++) {
				try {
					listeners[i].nextTimeUnit(timeUnit);
				} catch (ThreadDeath e) {
					log.log(Level.SEVERE, "could not deliver time unit "
							+ timeUnit + " to " + listeners[i], e);
					throw e;
				} catch (Throwable e) {
					log.log(Level.SEVERE, "could not deliver time unit "
							+ timeUnit + " to " + listeners[i], e);
				}
			}
		}
		try {
			nextTimeUnitFinished(timeUnit);
		} catch (ThreadDeath e) {
			log.log(Level.SEVERE, "could not deliver time unit " + timeUnit
					+ " to simulation", e);
			throw e;
		} catch (Throwable e) {
			log.log(Level.SEVERE, "could not deliver time unit " + timeUnit
					+ " to simulation", e);
		}
		log.info("***** START OF TIME " + timeUnit + " COMPLETE *****");

		if (hasAgentsToRecover) {
			SimulationAgent[] agents;
			synchronized (this) {
				agents = agentsToRecover;
				agentsToRecover = null;
				hasAgentsToRecover = false;
			}

			if (agents != null) {
				for (int i = 0, n = agents.length; i < n; i++) {
					dispatcher.callAgentRecovery(agents[i]);
				}
			}
		}
	}

	// -------------------------------------------------------------------
	// Simulation control
	// -------------------------------------------------------------------

	abstract protected void preNextTimeUnit(int timeUnit);

	protected abstract void setupSimulation()
			throws IllegalConfigurationException;

	protected String getTimeUnitName() {
		return null;
	}

	protected int getTimeUnitCount() {
		return 0;
	}

	protected abstract void startSimulation();

	/**
	 * Notification when this simulation is preparing to stop. Called after the
	 * agents have been stopped but still can receive messages.
	 */
	protected abstract void prepareStopSimulation();

	/**
	 * Notification when this simulation has been stopped. Called after the
	 * agents shutdown.
	 */
	protected abstract void completeStopSimulation();

	/**
	 * Called when entering a new time unit similar to time listeners but this
	 * method is guaranteed to be called before the time listeners.
	 * 
	 * @param timeUnit
	 *            the current time unit
	 */
	protected void nextTimeUnitStarted(int timeUnit) {
	}

	/**
	 * Called when a new time unit has begun similar to time listeners but this
	 * method is guaranteed to be called after the time listeners.
	 * 
	 * @param timeUnit
	 *            the current time unit
	 */
	protected void nextTimeUnitFinished(int timeUnit) {
	}

	/**
	 * Called whenever an external agent has logged in and needs to recover its
	 * state. The simulation should respond with the current recover mode (none,
	 * immediately, or after next time unit). This method should return
	 * <code>RECOVERY_NONE</code> if the simulation not yet have been started.
	 * <p>
	 * 
	 * The simulation might recover the agent using this method if recovering
	 * the agent can be done using the agent communication thread. In that case
	 * <code>RECOVERY_NONE</code> should be returned. If any other recover mode
	 * is returned, the simulation will later be asked to recover the agent
	 * using the simulation thread by a call to <code>recoverAgent</code>.
	 * 
	 * A common case might be when an agent reestablishing a lost connection to
	 * the server.
	 * 
	 * @param agent
	 *            the <code>SimulationAgent</code> to be recovered.
	 * @return the recovery mode for the agent
	 * @see #RECOVERY_NONE
	 * @see #RECOVERY_IMMEDIATELY
	 * @see #RECOVERY_AFTER_NEXT_TICK
	 * @see #recoverAgent(SimulationAgent)
	 */
	protected abstract int getAgentRecoverMode(SimulationAgent agent);

	/**
	 * Called whenever an external agent has logged in and needs to recover its
	 * state. The simulation should respond with the setup messages together
	 * with any other state information the agent needs to continue playing in
	 * the simulation (orders, inventory, etc). This method should not do
	 * anything if the simulation not yet have been started.
	 * <p>
	 * 
	 * A common case might be when an agent reestablishing a lost connection to
	 * the server.
	 * 
	 * @param agent
	 *            the <code>SimulationAgent</code> to be recovered.
	 */
	protected abstract void recoverAgent(SimulationAgent agent);

	// -------------------------------------------------------------------
	// Message handling
	// -------------------------------------------------------------------

	/**
	 * Sends the specified message from the simulation coordinator to the agent
	 * specified as the receiver in the message.
	 * 
	 * @param receiver
	 *            the message to send
	 */
	protected void sendMessage(String receiver, Transportable content) {
		sendMessage(new Message(receiver, content));
	}

	/**
	 * Sends the specified message from the simulation coordinator to the agent
	 * specified as the receiver in the message.
	 * 
	 * @param message
	 *            the message to send
	 */
	protected void sendMessage(Message message) {
		String sender = message.getSender();
		if (sender == null) {
			message.setSender(COORDINATOR);
		} else if (!sender.equals(COORDINATOR)) {
			throw new SecurityException(
					"Can not send message from other than self");
		}
		deliverMessage(message);
	}

	protected void sendToRole(int role, Transportable content) {
		// Coordinator is always permitted to send to agents and it is the
		// coordinator's task to validate and log this kind of messages
		dispatcher.deliverToRole(null, role, content);
	}

	final void deliverMessage(Message message) {
		dispatcher.deliver(message);
	}

	final void deliverMessageToRole(SimulationAgent senderAgent, int role,
			Transportable content) {
		dispatcher.deliverToRole(senderAgent, role, content);
	}

	/**
	 * Validates this message to ensure that it may be delivered to the agent.
	 * Messages to the coordinator and the administration are never validated.
	 * 
	 * @param receiver
	 *            the agent to deliver the message to
	 * @param message
	 *            the message to validate
	 * @return true if the message should be delivered and false otherwise
	 */
	protected abstract boolean validateMessage(SimulationAgent receiver,
			Message message);

	/**
	 * Validates this message to ensure that it may be broadcasted to all agents
	 * with the specified role.
	 * 
	 * This method can also be used to log messages
	 * 
	 * @param sender
	 *            the agent sender the message
	 * @param role
	 *            the role of all receiving agents
	 * @param content
	 *            the message content
	 * @return true if the message should be delivered and false otherwise
	 */
	protected abstract boolean validateMessageToRole(SimulationAgent sender,
			int role, Transportable content);

	/**
	 * Validates this message from the coordinator to ensure that it may be
	 * broadcasted to all agents with the specified role.
	 * 
	 * This method can also be used to log messages
	 * 
	 * @param role
	 *            the role of all receiving agents
	 * @param content
	 *            the message content
	 * @return true if the message should be delivered and false otherwise
	 */
	protected abstract boolean validateMessageToRole(int role,
			Transportable content);

	/**
	 * Delivers a message to the coordinator (the simulation). The coordinator
	 * must self validate the message.
	 * 
	 * @param message
	 *            the message
	 */
	protected abstract void messageReceived(Message message);

	// -------------------------------------------------------------------
	// Utility methods
	// -------------------------------------------------------------------

	protected void createExternalAgent(String name, int role, int participantID) {
		ExternalAgent agent = new ExternalAgent();
		SimulationAgent simAgent = registerAgent(agent, name, role,
				participantID);
		simAgent.setProxy(true);
	}

	/**
	 * Creates agents with the specified names and registers them in this
	 * simulation. The class name for the agents are found by constructing a
	 * property as base + '.' + name + '.class' and take its value from the
	 * configuration manager.
	 * 
	 * @param base
	 *            the base of the agent property
	 * @param role
	 *            the role of the agents
	 * @return the number of agents created
	 * @exception IllegalConfigurationException
	 *                if an error occurs
	 */
	protected int createBuiltinAgents(String base, int role)
			throws IllegalConfigurationException {
		return createBuiltinAgents(base, role, null);
	}

	protected int createBuiltinAgents(String base, int role, Class baseClass)
			throws IllegalConfigurationException {
		int numberCreated = 0;
		String names = config.getProperty(base + ".names");
		if (names == null) {
			throw new IllegalConfigurationException("No specified " + base
					+ " in config");
		}

		String name = null;
		String className = null;
		String defaultClassName = config.getProperty(base + ".class");
		try {
			StringTokenizer tok = new StringTokenizer(names, ", \t");
			while (tok.hasMoreTokens()) {
				name = tok.nextToken();
				className = config.getProperty(base + '.' + name + ".class",
						defaultClassName);
				if (className == null) {
					throw new IllegalConfigurationException(
							"No class definition " + "for " + base + ' ' + name);
				}

				Agent agent = (Agent) Class.forName(className).newInstance();
				if (baseClass != null && !baseClass.isInstance(agent)) {
					throw new ClassCastException(className
							+ " is not an instance of " + baseClass);
				}

				registerAgent(agent, name, role, -1);
				numberCreated++;
			}
		} catch (IllegalConfigurationException e) {
			throw e;
		} catch (ClassCastException e) {
			throw (IllegalConfigurationException) new IllegalConfigurationException(
					name
							+ " of class "
							+ className
							+ " is not an object of type "
							+ (baseClass != null ? baseClass.getName()
									: "Agent")).initCause(e);
		} catch (Exception e) {
			throw (IllegalConfigurationException) new IllegalConfigurationException(
					"could not create agent " + name).initCause(e);
		}
		return numberCreated;
	}

	protected int createDummies(String base, int role, int numberOfAgents)
			throws IllegalConfigurationException {
		return createDummies(base, role, numberOfAgents, null);
	}

	protected int createDummies(String base, int role, int numberOfAgents,
			String namePrefix) throws IllegalConfigurationException {
		int numberCreated = 0;
		String names = config.getProperty(base + ".names");
		if (names == null) {
			throw new IllegalConfigurationException("No specified dummy "
					+ base + " in config");
		}

		String name = null;
		String className = null;
		String defaultClassName = config.getProperty(base + ".class");
		try {
			StringTokenizer tok = new StringTokenizer(names, ", \t");
			int tokCount = tok.countTokens();
			String[] nameSplit = new String[tokCount * 2];
			for (int i = 0, n = nameSplit.length; i < n; i += 2) {
				name = tok.nextToken();
				className = config.getProperty(base + '.' + name + ".class",
						defaultClassName);
				if (className == null) {
					throw new IllegalConfigurationException(
							"No class definition for " + "dummy " + base + ' '
									+ name);
				}
				nameSplit[i] = name;
				nameSplit[i + 1] = className;
			}

			for (int i = 0, index = 0, n = nameSplit.length; i < numberOfAgents; i++) {
				className = nameSplit[index + 1];

				Agent dummyAgent = (Agent) Class.forName(className)
						.newInstance();
				int agentID = --dummyAgentCounter;
				if (namePrefix == null) {
					// Use the name from configuration file
					int nameIndex = 2;
					String agentName = name = nameSplit[index];
					while (getAgent(agentName) != null) {
						agentName = name + '-' + nameIndex++;
					}
					registerAgent(dummyAgent, agentName, role, agentID);

				} else {
					registerAgent(dummyAgent, namePrefix + (i + 1), role,
							agentID);
				}
				numberCreated++;
				index = (index + 2) % n;

				// Dummy agents must also be registered in the simulation info
				// as participating agents
				info.addParticipant(agentID, role);
				admin.simulationJoined(info, agentID, role);
			}
		} catch (IllegalConfigurationException e) {
			throw e;
		} catch (ClassCastException e) {
			throw (IllegalConfigurationException) new IllegalConfigurationException(
					"dummy " + name + " of class " + className
							+ " is not an agent").initCause(e);
		} catch (Exception e) {
			throw (IllegalConfigurationException) new IllegalConfigurationException(
					"could not create agent " + name).initCause(e);
		}
		return numberCreated;
	}

	// \TODO DEBUG FINALIZE REMOVE THIS!!! REMOVE THIS!!!
	protected void finalize() throws Throwable {
		SimulationInfo info = this.info;
		if (info != null) {
			log.info("SIMULATION " + info.getSimulationID()
					+ " IS BEING GARBAGED");

		} else {
			log.info("SIMULATION WITHOUT INFO IS BEING GARBAGED");
		}
		super.finalize();
	}

} // Simulation
