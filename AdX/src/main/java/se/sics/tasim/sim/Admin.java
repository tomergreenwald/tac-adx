/**
 * TAC Supply Chain Management Simulator
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001-2006 SICS AB. All rights reserved.
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
 * Admin
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Oct 10 14:14:43 2002
 * Updated : $Date: 2008-06-12 07:31:52 -0500 (Thu, 12 Jun 2008) $
 *           $Revision: 4728 $
 */
package se.sics.tasim.sim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.inet.InetServer;
import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;
import se.sics.isl.util.LogFormatter;
import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.Message;
import se.sics.tasim.is.AgentLookup;
import se.sics.tasim.is.CompetitionSchedule;
import se.sics.tasim.is.EventWriter;
import se.sics.tasim.is.InfoConnection;
import se.sics.tasim.is.SimConnection;
import se.sics.tasim.is.SimulationInfo;
import se.sics.tasim.props.AdminContent;
import se.sics.tasim.props.Alert;
import se.sics.tasim.viewer.ViewerConnection;

import com.botbox.util.ArrayQueue;
import com.botbox.util.ArrayUtils;
import com.botbox.util.ThreadPool;

public final class Admin {

	private static final Logger log = Logger.getLogger(Admin.class.getName());

	/** Version information for the server */
	public final static String SERVER_VERSION = "0.8.19";

	public final static String CONF = Main.CONF;

	/** The address of the administration unit in the simulation server */
	public final static String ADMIN = Agent.ADMIN;

	/** Minimum time between each simulation for management */
	private final static int SIM_DELAY = 2 * 60 * 1000; // two minutes

	/**
	 * Time before simulation start when automatically locking of the simulation
	 * is allowed (after a simulation has been locked it can no longer be
	 * removed)
	 */
	private final static int MAX_TIME_BEFORE_LOCK = 2 * 60000 + SIM_DELAY;

	private final static String SIM_FILE_PREFIX = "sim";
	private final static String SIM_FILE_SUFFIX = ".slg";

	private final ConfigManager config;
	private final AgentLookup lookup;
	private final SimConnectionImpl simConnection;
	private final InfoConnection infoConnection;
	private final ISClient isClient;
	private final EventWriter eventWriter;
	private final ViewerConnection viewerConnection;

	// MLB 20080411 - This will be set in the config file, and if true,
	// will allow users to connect without pre-registering, which will
	// be very handy for experimentation.
	private final boolean allowUnregisteredAgents;

	// MLB 20080517 - This will store the offset in seconds from :00 that
	// we want to start the game at. The theory behind this is that we
	// will be using multiple virtual hosts on the same hardware, and we
	// want to stagger the wall-clock times that each server's day starts,
	// to avoid having network congestion.
	private final int gameStartOffset;

	private final String defaultSimulationType;

	/** Thread pool to use by agent connections during simulations */
	private final ThreadPool simulationThreadPool;

	private final Hashtable simulationManagerTable = new Hashtable();
	private final Hashtable channelTable = new Hashtable();
	private AgentChannel[] channelList;
	private int channelNumber = 0;

	/** Contains a pointer to the current simulation if such is running */
	private Simulation currentSimulation;

	/** Information about coming simulations */
	private final ArrayQueue simQueue = new ArrayQueue();

	/** Time zone difference */
	private long timeDiff = 0L;

	/** Logging */
	private final LogFormatter formatter;
	private FileHandler rootFileHandler;

	private String serverName;
	private final String configDirectory;
	private final String runAfterSimulation;

	private final String logName;
	private final String logPrefix;
	private final String simPrefix;
	private FileHandler simLogHandler;
	private String simLogName;

	private int startDelay;
	private boolean allowEmptySimulations = false;

	// added by WG20071030
	private int maxgames = -1;
	private int startsimid = -1;

	private int lastSimulationID = 0;
	private int lastUniqueSimulationID = 0;

	/**
	 * Timer handling: optimize this by using common timer instead of using as
	 * many threads. FIX THIS!!! TODO
	 */
	private final Timer timer = new Timer();

	/**
	 * Creates a new <code>Admin</code> instance.
	 * 
	 * @param config
	 *            the configuration to use
	 * @exception IllegalConfigurationException
	 *                if a configuration error is detected
	 * @exception IOException
	 *                if an IO error occurs during setup
	 */
	public Admin(ConfigManager config) throws IllegalConfigurationException,
			IOException {
		this.config = config;
		this.serverName = config.getProperty(CONF + "server.name",
				config.getProperty("server.name"));
		if (this.serverName == null) {
			this.serverName = generateServerName();
		}
		this.defaultSimulationType = config.getProperty(CONF
				+ "simulation.defaultType",
				config.getProperty("simulation.defaultType", "tac13adx"));
		this.runAfterSimulation = config.getProperty(CONF
				+ "runAfterSimulation");
		this.configDirectory = config.getProperty(CONF + "config.directory",
				"config");
		this.logName = getLogDirectory(CONF, "log.directory", serverName);
		this.logPrefix = getLogDirectory(CONF, "log.simlogs", serverName);
		this.simPrefix = getLogDirectory(CONF, "log.sims", serverName + '_'
				+ SIM_FILE_PREFIX);
		this.allowEmptySimulations = config.getPropertyAsBoolean(CONF
				+ "allowEmptySimulations", false);

		// MLB 20080411 - Added to allow non-pre-registered agents to play,
		// for use in experimental work
		this.allowUnregisteredAgents = config.getPropertyAsBoolean(CONF
				+ "allowUnregisteredAgents", false);

		// MLB 20080517 - units of milliseconds
		this.gameStartOffset = config.getPropertyAsInt(
				CONF + "gameStartOffset", 0) * 1000;

		this.startDelay = config.getPropertyAsInt(CONF + "startDelay", 60) * 1000;
		// startDelay is in units of milliseconds
		if (this.startDelay <= 0) {
			this.startDelay = 0;
		} else {
			// Round the start delay to full minutes if it is larger than zero
			this.startDelay = ((this.startDelay + 59000) / 60000) * 60000;
		}

		// added by WG20071030
		this.startsimid = config.getPropertyAsInt(CONF + "sim.startsimid",
				config.getPropertyAsInt("sim.startsimid", 0));
		this.maxgames = config.getPropertyAsInt(CONF + "sim.gamestorun",
				config.getPropertyAsInt("sim.gamestorun", -1));

		// The server name may not contain any suspicious characters
		// because it is used in file names
		for (int i = 0, n = serverName.length(); i < n; i++) {
			char c = serverName.charAt(i);
			if (!Character.isLetterOrDigit(c) && c != '.' && c != '_') {
				throw new IllegalConfigurationException(
						"server name may only contain "
								+ "letters and digits: " + serverName);
			}
		}

		// Set shorter names for the log
		formatter = new LogFormatter();
		formatter.setAliasLevel(2);
		LogFormatter.setFormatterForAllHandlers(formatter);

		setLogging(config);

		// Set up the information connection
		String infoClass = config.getProperty(CONF + "ic.class");
		if (infoClass == null) {
			throw new IllegalConfigurationException(
					"no InfoConnection specified");
		}
		try {
			infoConnection = (InfoConnection) Class.forName(infoClass)
					.newInstance();
		} catch (Exception e) {
			throw (IllegalConfigurationException) new IllegalConfigurationException(
					"could not create info " + "connection of type "
							+ infoClass).initCause(e);
		}
		infoConnection.init(config);

		// Setup the thread pool to use in simulations
		simulationThreadPool = ThreadPool.getThreadPool("simpool");
		simulationThreadPool.setMinThreads(6);
		simulationThreadPool.setMaxThreads(40);
		simulationThreadPool.setMaxIdleThreads(15);
		simulationThreadPool.setInterruptThreadsAfter(120000);

		// Create the user cache!
		// and connect the connections between the sub-systems
		lookup = new AgentLookup();
		simConnection = new SimConnectionImpl(this, lookup);

		if (config.getPropertyAsBoolean(CONF + "gui", false)) {
			isClient = new ISClient(this, infoConnection);
			eventWriter = isClient.getEventWriter();
			viewerConnection = isClient.getViewerConnection();
		} else {
			isClient = null;
			eventWriter = infoConnection;
			viewerConnection = null;
		}

		infoConnection.setSimConnection(simConnection);
		simConnection.setInfoConnection(infoConnection);

		if (isClient != null) {
			isClient.start();
		}

		infoConnection.auth(serverName, "password", SERVER_VERSION);

		// Start timer to check for next simulation. SHOULD BE
		// OPTIMIZED. FIX THIS!!! TODO

		// The simulation list should be checked once per minute on the minute,
		// but
		// potentially offset by this.gameStartOffset (MLB 20080517)
		long currentTime = System.currentTimeMillis();
		long nextTime = (currentTime / 60000) * 60000 + 60000
				+ this.gameStartOffset; // MLB 20080517
		long delay = nextTime - currentTime;
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					Admin.this.checkSimulation();
				} catch (ThreadDeath e) {
					log.log(Level.SEVERE, "could not handle timeout", e);
					throw e;
				} catch (Throwable e) {
					log.log(Level.SEVERE, "could not handle timeout", e);
				}
			}
		}, delay, 60000);
	}

	private String generateServerName() {
		return InetServer.getLocalHostName();
	}

	private String getLogDirectory(String base, String property, String name)
			throws IOException {
		String logDirectory = config.getProperty(base + property);
		if (logDirectory == null) {
			logDirectory = config.getProperty(property);
		}
		if (logDirectory != null) {
			// Create directories for logs
			File fp = new File(logDirectory);
			if ((!fp.exists() && !fp.mkdirs()) || !fp.isDirectory()) {
				throw new IOException("could not create directory '"
						+ logDirectory + '\'');
			}
			return name == null ? fp.getAbsolutePath() : (fp.getAbsolutePath()
					+ File.separatorChar + name);
		} else {
			return name;
		}
	}

	private long roundTimeToMinute(long time) {
		long delta = time % 60000;
		if (delta > 0) {
			time += 60000 - delta;
		}
		return time;
	}

	// -------------------------------------------------------------------
	// Information retrieval
	// -------------------------------------------------------------------

	public ConfigManager getConfig() {
		return config;
	}

	public String getServerName() {
		return serverName;
	}

	public String getConfigDirectory() {
		return configDirectory;
	}

	public EventWriter getEventWriter() {
		return eventWriter;
	}

	// public Simulation getSimulation(int id) {
	// Simulation sim = currentSimulation;
	// return sim != null && sim.getSimulationInfo().getSimulationID() == id
	// ? sim
	// : null;
	// }

	public long getServerTime() {
		return System.currentTimeMillis() + timeDiff;
	}

	public long getServerTimeSeconds() {
		return (System.currentTimeMillis() + timeDiff) / 1000;
	}

	public void setServerTime(long serverTime) {
		this.timeDiff = serverTime - System.currentTimeMillis();
		formatter.setLogTime(serverTime);
		if (viewerConnection != null) {
			viewerConnection.setServerTime(serverTime);
		}
	}

	// Returns the difference between system time and server time.
	// Used by simulations to synchronize their timers
	long getTimeDiff() {
		return timeDiff;
	}

	// -------------------------------------------------------------------
	// User handling
	// -------------------------------------------------------------------

	public String getUserName(int userID) {
		AgentChannel channel = getAgentChannel(userID);
		if (channel != null) {
			return channel.getName();
		}

		// Must lookup user in the lookup-cache
		return lookup.getAgentName(userID);
	}

	// -------------------------------------------------------------------
	// Simulation instance handling
	// -------------------------------------------------------------------

	synchronized int getNextUniqueSimulationID() {
		if ((this.startsimid - 1) > lastUniqueSimulationID) {
			lastUniqueSimulationID = (this.startsimid - 1);
		}
		return ++lastUniqueSimulationID;
	}

	private synchronized int getNextSimulationID() {
		if ((this.startsimid - 1) > lastSimulationID) {
			lastSimulationID = (this.startsimid - 1);
		}
		return ++lastSimulationID;
	}

	// simulationType == null or "*" => check for any simulations where
	// the agent participates. Possible to ask for patterns such as
	// "tacClassic*"
	public synchronized SimulationInfo nextSimulation(int userID,
			String simTypePattern) {
		for (int i = 0, n = simQueue.size(); i < n; i++) {
			SimulationInfo simulation = (SimulationInfo) simQueue.get(i);
			if (!simulation.hasSimulationID()) {
				// This simulation has not yet received a simulation id which
				// means
				// that no further simulation is initialized.
				break;
			}

			if (simulation.isParticipant(userID)
					&& ((simTypePattern == null) || matchSimulationType(
							simTypePattern, simulation.getType()))) {
				return simulation;
			}
		}
		return null;
	}

	private boolean matchSimulationType(String simTypePattern,
			String simulationType) {
		int len = simTypePattern != null ? simTypePattern.length() : 0;
		if (len == 0) {
			return false;
		} else if (simTypePattern.charAt(len - 1) == '*') {
			return simulationType != null
					&& simulationType.regionMatches(0, simTypePattern, 0,
							len - 1);
		} else {
			return simTypePattern.equals(simulationType);
		}
	}

	/**
	 * Joins the specified agent to a simulation of the specified type. Returns
	 * the simulation where the agent joined, creating new simulations as
	 * needed. Returns information about the joined simulations.
	 * 
	 * Note: the returned simulation info might not have been locked yet i.e. is
	 * still missing simulation id.
	 * 
	 * @param userID
	 *            the agent to join a simulation
	 * @param simType
	 *            the type of the simulation to join
	 * @param simParams
	 *            parameters for the simulation
	 * @param simRoleName
	 *            the role of the agent to join
	 * @return information about the joined simulation
	 * @exception NoSuchManagerException
	 *                if no manager for the type of simulation could be found
	 */
	public synchronized SimulationInfo joinSimulation(int userID,
			String simType, String simParams, String simRoleName)
			throws NoSuchManagerException {
		if (simType == null) {
			simType = defaultSimulationType;
		}

		SimulationManager manager = getSimulationManager(simType);
		int simulationLength = manager.getSimulationLength(simType, simParams)
				+ SIM_DELAY;
		int simRole = manager.getSimulationRoleID(simType, simRoleName);
		long time = ((getServerTime() + startDelay + 59000) / 60000) * 60000;
		if (time < getServerTime() + 2000) {
			// Do not start within the next two seconds
			time += 60000;
		}

		for (int i = 0, n = simQueue.size(); i < n; i++) {
			SimulationInfo simulation = (SimulationInfo) simQueue.get(i);
			int gid = simulation.getSimulationID();
			long startTime = simulation.getStartTime();
			if (gid >= 0) {
				if (simType.equals(simulation.getType())) {
					// Perhaps should compare parameters too? FIX THIS!!! TODO
					if (simulation.isParticipant(userID)) {
						return simulation;
					} else if (manager.join(userID, simRole, simulation)) {
						simulationJoined(simulation, userID);
						return simulation;
					}
				}

				// Next simulation has already been assigned an id and we cannot
				// create another simulation before it
				time = roundTimeToMinute(simulation.getEndTime() + SIM_DELAY);

			} else if ((startTime - time) > simulationLength) {
				// There is a free time lap to insert a simulation here
				simulation = createSimulationInfo(manager, simType, simParams);
				simulation.setStartTime(time + this.gameStartOffset);
				if (manager.join(userID, simRole, simulation)) {
					simQueue.add(i, simulation);
					simulationCreated(simulation);
					// simulationJoined(simulation, userID);
					return simulation;
				} else {
					// Could not add to newly created means that we can never
					// create
					// such a simulation
					return null;
				}

			} else { // we know that gid < 0
				if (!simulation.isReservation()
						&& simType.equals(simulation.getType())) {
					if (simulation.isParticipant(userID)) {
						return simulation;
					} else if (manager.join(userID, simRole, simulation)) {
						simulationJoined(simulation, userID);
						return simulation;
					}
				}
				time = roundTimeToMinute(simulation.getEndTime() + SIM_DELAY);
			}
		}

		// Could not insert a new simulation earlier so we add it last
		SimulationInfo simulation = createSimulationInfo(manager, simType,
				simParams);
		simulation.setStartTime(time + this.gameStartOffset);
		if (manager.join(userID, simRole, simulation)) {
			simQueue.add(simulation);
			simulationCreated(simulation);
			// simulationJoined(simulation, userID);
			return simulation;
		} else {
			// Could not create such a simulation
			return null;
		}
	}

	// -------------------------------------------------------------------
	// Info Server communication
	// -------------------------------------------------------------------

	private void simulationCreated(SimulationInfo info) {
		log.info("created simulation " + info.getID());
		infoConnection.simulationCreated(info);
	}

	private void simulationCreated(SimulationInfo info, int competitionID) {
		log.info("created simulation " + info.getID() + " for competition "
				+ competitionID);
		infoConnection.simulationCreated(info, competitionID);
	}

	private void simulationRemoved(SimulationInfo info, String message) {
		infoConnection.simulationRemoved(info.getID(), message);
	}

	/**
	 * Notify the information server about a new participant in a simulation.
	 * 
	 * @param info
	 *            the simulation that was joined
	 * @param userID
	 *            the agent joining the simulation (might be a dummy agent)
	 */
	private void simulationJoined(SimulationInfo info, int userID) {
		int index = info.indexOfParticipant(userID);
		if (index >= 0) {
			simulationJoined(info, userID, info.getParticipantRole(index));
		}
	}

	/**
	 * Notify the information server about a new participant in a simulation.
	 * Also called by the simulation when adding dummy agents.
	 * 
	 * @param info
	 *            the simulation that was joined
	 * @param userID
	 *            the agent joining the simulation (might be a dummy agent)
	 */
	final void simulationJoined(SimulationInfo info, int userID, int role) {
		infoConnection.simulationJoined(info.getID(), userID, role);
	}

	private void simulationLocked(SimulationInfo info) {
		int sid = info.getSimulationID();
		if (sid >= 0) {
			infoConnection.simulationLocked(info.getID(), sid);
		}
	}

	private void simulationStarted(Simulation simulation) {
		SimulationInfo info = simulation.getSimulationInfo();
		String timeUnitName = simulation.getTimeUnitName();
		int timeUnitCount = simulation.getTimeUnitCount();
		if (viewerConnection != null) {
			viewerConnection.simulationStarted(info.getSimulationID(),
					info.getType(), info.getStartTime(), info.getEndTime(),
					timeUnitName, timeUnitCount);
		}
		infoConnection.simulationStarted(info.getID(), timeUnitName,
				timeUnitCount);
	}

	private void simulationStopped(Simulation simulation, boolean error) {
		SimulationInfo info = simulation.getSimulationInfo();
		if (viewerConnection != null) {
			viewerConnection.simulationStopped(info.getSimulationID());
		}
		infoConnection.simulationStopped(info.getID(), info.getSimulationID(),
				error);
	}

	// -------------------------------------------------------------------
	// API towards SimConnection (and Info System)
	// -------------------------------------------------------------------

	void sendStateToInfoSystem() {
		infoConnection.dataUpdated(InfoConnection.SIM_ID, lastSimulationID);
		infoConnection.dataUpdated(InfoConnection.UNIQUE_SIM_ID,
				lastUniqueSimulationID);

		if (simQueue.size() > 0) {
			SimulationInfo[] sims;

			// Send all coming simulations. Must use a cache because the
			// simulation server might cause some simulations to be removed
			// during notification if the information service and the
			// simulation server is running in the same process. SHOULD BE
			// OPTIMIZED TO ONLY SEND SIMULATIONS THE OTHER SIDE DOES NOT KNOW
			// ABOUT USING lastSimulationID!!! FIX THIS!!!
			synchronized (this) {
				sims = (SimulationInfo[]) simQueue
						.toArray(new SimulationInfo[simQueue.size()]);
			}
			if (sims != null) {
				for (int i = 0, n = sims.length; i < n; i++) {
					infoConnection.simulationCreated(sims[i]);
				}
			}
		}
	}

	void dataUpdated(int type, int value) {
		if (type == SimConnection.STATUS) {
			if (value == SimConnection.STATUS_READY) {
				// Info System is ready!
			}

		} else if (type == SimConnection.UNIQUE_SIM_ID) {
			if (value > lastUniqueSimulationID) {
				lastUniqueSimulationID = value;
			}

		} else if (type == SimConnection.SIM_ID) {
			if (value > lastSimulationID) {
				lastSimulationID = value;
			}
		}
	}

	synchronized boolean addSimulation(SimulationInfo info) {
		long startTime = info.getStartTime();
		long currentTime = getServerTime();
		if (currentTime >= startTime) {
			// Simulation already started
			Simulation sim = this.currentSimulation;
			// Need to do better comparison of the simulation info when the
			// InfoServer is external. FIX THIS!!! TODO
			if ((sim == null) || !info.equals(sim.getSimulationInfo())) {
				simulationRemoved(info, null);
			}
			return false;
		}

		int index = 0;
		int ugid = info.getID();
		for (int n = simQueue.size(); index < n; index++) {
			SimulationInfo sim = (SimulationInfo) simQueue.get(index);
			if (sim.getID() == ugid) {
				// simulation already exists, only copy any participants
				sim.copyParticipants(info);
				return false;
			} else if (sim.getStartTime() > startTime) {
				// Simulation should be inserted here
				break;
			}
		}
		simQueue.add(index, info);
		log.finer("Added simulation " + info.getID() + " ("
				+ info.getSimulationID() + ')');
		return true;
	}

	void resultsGenerated(int simulationID) {
		// The results for this simulation has been generated and we
		// should remove the simulation log to avoid double generation.
		String simFileName = SIM_FILE_PREFIX + simulationID + SIM_FILE_SUFFIX;
		String targetName = simPrefix + simulationID + SIM_FILE_SUFFIX;
		File sourceFile = new File(simFileName);
		if (!sourceFile.renameTo(new File(targetName))) {
			// Could not simply rename the file => must copy/delete it
			log.warning("could not use simple rename to move simulation log "
					+ simulationID + " from " + simFileName + " to "
					+ targetName);
			try {
				FileInputStream input = new FileInputStream(simFileName);
				FileOutputStream fout = new FileOutputStream(targetName);
				byte[] buffer = new byte[4096];
				int n;
				while ((n = input.read(buffer)) > 0) {
					fout.write(buffer, 0, n);
				}
				fout.close();
				input.close();
				if (!sourceFile.delete()) {
					log.warning("could not remove old simulation file "
							+ simFileName);
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not move " + simFileName, e);
			}
		}

		if (runAfterSimulation != null) {
			// This should be queued in a thread pool! FIX THIS!!!
			final String simulationIDAsString = "" + simulationID;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						String command = format(
								runAfterSimulation,
								"sg",
								new String[] { serverName, simulationIDAsString });
						if (command != null) {
							log.fine("running '" + command + '\'');
							Runtime.getRuntime().exec(command);
						}
					} catch (Throwable e) {
						log.log(Level.SEVERE, "could not run '"
								+ runAfterSimulation + "' after simulation "
								+ simulationIDAsString, e);
					}
				}
			}, "afterSim" + simulationIDAsString).start();
		}
	}

	/**
	 * Formats a string according to the format string. Any occurrences of the
	 * specified attributes (%character) is replaced with corresponding data.
	 * 
	 * @param format
	 *            the format string
	 * @param fnames
	 *            the attribute names
	 * @param data
	 *            the attribute values
	 * @return the resulting string
	 */
	private String format(String format, String fnames, String[] data) {
		if (format == null)
			return null;

		int flen = format.length();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < flen; i++) {
			char c = format.charAt(i);
			if (c == '\\' && ((i + 1) < flen)) {
				i++;
				sb.append(format.charAt(i));

			} else if (c == '%' && ((i + 1) < flen)) {
				char c2 = format.charAt(i + 1);
				int index = fnames.indexOf(c2);
				if (index >= 0) {
					sb.append(data[index]);
					i++;
				} else {
					sb.append('%');
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	// -------------------------------------------------------------------
	// API towards ISClient
	// -------------------------------------------------------------------

	void sendChatMessage(String message) {
		infoConnection.sendChatMessage(getServerTime(), message);
	}

	// -------------------------------------------------------------------
	// API towards SimConnectionImpl
	// -------------------------------------------------------------------

	void addChatMessage(long time, String serverName, String userName,
			String message) {
		if (isClient != null) {
			isClient.addChatMessage(time, serverName, userName, message);
		}
	}

	synchronized void scheduleCompetition(CompetitionSchedule schedule,
			boolean notifyInfoServer) {
		int competitionID = schedule.getID();
		try {
			long startTime = schedule.getStartTime();
			int count = schedule.getSimulationCount();
			int startIndex = getInsertionIndex(startTime);
			if (startIndex < 0) {
				// It was not possible to schedule this competition due to
				// conflicting locked game
				if (notifyInfoServer) {
					infoConnection.requestFailed(
							InfoConnection.SCHEDULE_COMPETITION, competitionID,
							"conflicting locked game");
				}
			} else if (count == 0) {
				// No simulations to schedule
				if (notifyInfoServer) {
					infoConnection.requestSuccessful(
							InfoConnection.SCHEDULE_COMPETITION, competitionID);
				}

			} else {
				// It should be possible to schedule this competition
				String simType = schedule.getSimulationType();
				String simParams = schedule.getSimulationParams();
				if (simType == null) {
					simType = defaultSimulationType;
				}

				// Create all simulations first to give them a continuous id
				// span
				SimulationManager manager = getSimulationManager(simType);
				SimulationInfo[] simulations = new SimulationInfo[count];
				boolean isSimulationsClosed = schedule.isSimulationsClosed();
				for (int i = 0; i < count; i++) {
					int[] participants = schedule.getParticipants(i);
					int[] roles = schedule.getRoles(i);
					SimulationInfo info = createSimulationInfo(manager,
							simType, simParams);
					simulations[i] = info;
					if (participants != null) {
						int roleLen = roles == null ? 0 : roles.length;
						for (int j = 0, m = participants.length; j < m; j++) {
							int role = j < roleLen ? roles[j] : 0;
							if (!manager.join(participants[j], role, info)) {
								throw new IllegalStateException(
										"could not join participant "
												+ participants[j] + " as "
												+ role + " in simulation " + i
												+ " for competition "
												+ competitionID);
							}
						}
					}
					if (isSimulationsClosed) {
						// Do not allow any more participants
						info.setFull();
					}
				}

				// Now that all simulations was successfully created, they can
				// be scheduled.
				int timeBetween = schedule.getTimeBetweenSimulations();
				if (timeBetween < SIM_DELAY) {
					timeBetween = SIM_DELAY;
				}

				int reserveIndex = 0;
				long nextReservationTime = schedule.getReservationCount() > 0 ? schedule
						.getReservationStartTime(0) : Long.MAX_VALUE;
				long nextTime = startTime;
				long nextExisting = (startIndex < simQueue.size()) ? ((SimulationInfo) simQueue
						.get(startIndex)).getStartTime() : Long.MAX_VALUE;

				int gamesCounter = 0;
				int gamesBetweenReservations = schedule
						.getSimulationsBeforeReservation();
				int reservationLength = schedule
						.getSimulationsReservationLength();
				if (gamesBetweenReservations <= 0 || reservationLength < 60000) {
					gamesBetweenReservations = Integer.MAX_VALUE;
				}

				// Add the simulations
				for (int i = 0; i < count; i++) {
					SimulationInfo info = simulations[i];
					int length = info.getSimulationLength();
					long endTime = nextTime + length;
					if (endTime > nextReservationTime) {
						// Add a reserved time space here
						int reserveLength = schedule
								.getReservationLength(reserveIndex);
						SimulationInfo reservedInfo = createTimeReservation(
								nextReservationTime, reserveLength);
						simQueue.add(startIndex++, reservedInfo);
						simulationCreated(reservedInfo);

						reserveIndex++;
						nextReservationTime = (reserveIndex < schedule
								.getReservationCount()) ? schedule
								.getReservationStartTime(reserveIndex)
								: Long.MAX_VALUE;

						nextTime = roundTimeToMinute(reservedInfo.getEndTime()
								+ timeBetween);
						endTime = nextTime + length;
					}
					endTime += timeBetween;

					if (gamesCounter++ == gamesBetweenReservations) {
						SimulationInfo reservedInfo = createTimeReservation(
								nextTime, reservationLength);
						simQueue.add(startIndex++, reservedInfo);
						simulationCreated(reservedInfo);
						nextTime = roundTimeToMinute(reservedInfo.getEndTime()
								+ timeBetween);
						endTime = nextTime + length + timeBetween;
						// Reset the games counter to 1 since we will add a game
						// below
						gamesCounter = 1;
					}

					// Check if any existing simulation needs to be removed
					while (endTime > nextExisting) {
						// Must remove the simulation at index
						simulationRemoved(
								(SimulationInfo) simQueue.remove(startIndex),
								"by competition");

						nextExisting = (startIndex < simQueue.size()) ? ((SimulationInfo) simQueue
								.get(startIndex)).getStartTime()
								: Long.MAX_VALUE;
					}

					// Add the simulation
					info.setStartTime(nextTime + this.gameStartOffset);
					simQueue.add(startIndex++, info);
					simulationCreated(info, competitionID);

					nextTime = endTime;
				}

				if (notifyInfoServer) {
					infoConnection.requestSuccessful(
							InfoConnection.SCHEDULE_COMPETITION, competitionID);
				}
			}

		} catch (Exception e) {
			log.log(Level.SEVERE, "could not schedule competition "
					+ competitionID, e);
			if (notifyInfoServer) {
				infoConnection.requestFailed(
						InfoConnection.SCHEDULE_COMPETITION, competitionID,
						e.getMessage());
			}
		}
	}

	// Returns the index in the coming simulation when this simulation
	// should be inserted, ignoring any unlocked games after.
	// Will return -1 if the new game would conflict with an already locked game
	// NOTE: MAY ONLY BE CALLED SYNCHRONIZED!!!
	private int getInsertionIndex(long startTime) {
		for (int index = simQueue.size() - 1; index >= 0; index--) {
			SimulationInfo simulation = (SimulationInfo) simQueue.get(index);
			long simEnd = simulation.getEndTime() + SIM_DELAY;
			if (simEnd <= startTime) {
				// The new simulation should be inserted after this simulation
				return index + 1;
			} else if (simulation.hasSimulationID()) {
				// This simulation conflicts with the new simulation and since
				// it has been locked it can not be removed.
				return -1;
			} else {
				// This simulation conflicts with the new simulation but since
				// it is not locked it can be removed. The search for the
				// right index will continue.
			}
		}
		return 0;
	}

	synchronized void lockNextSimulations(int simulationCount) {
		for (int i = 0, len = simQueue.size(), n = (len < simulationCount ? len
				: simulationCount); i < n; i++) {
			SimulationInfo simulation = (SimulationInfo) simQueue.get(i);
			if (!simulation.hasSimulationID()
					&& !assignSimulationIDs(simulation)) {
				// Simulation had no simulation id and it was not possible to
				// assign an id at this time (perhaps simulation was empty)
				break;
			}
		}
	}

	synchronized void addTimeReservation(long startTime, int lengthInMillis,
			boolean notifyInfoServer) {
		long endTime = startTime + lengthInMillis + SIM_DELAY;
		int index = simQueue.size() - 1;

		for (; index >= 0; index--) {
			SimulationInfo simulation = (SimulationInfo) simQueue.get(index);
			long simEnd = simulation.getEndTime();
			if (simEnd <= startTime) {
				// Add the time reservation after this simulation
				break;

			} else if (simulation.getStartTime() < endTime) {
				// Conflicting game. Skip the time reservation!
				if (notifyInfoServer) {
					infoConnection.requestFailed(InfoConnection.RESERVE_TIME,
							0, "conflicting simulation " + simulation.getID());
					startTime = -1L;
				}
			}
		}

		if (startTime > 0) {
			// Add the time reservation at index + 1
			SimulationInfo reservedInfo = createTimeReservation(startTime,
					lengthInMillis);
			simQueue.add(index + 1, reservedInfo);
			simulationCreated(reservedInfo);
			if (notifyInfoServer) {
				infoConnection.requestSuccessful(InfoConnection.RESERVE_TIME,
						reservedInfo.getID());
			}
		}
	}

	synchronized boolean createSimulation(String simType, String simParams,
			boolean notifyInfoServer) {
		if (simType == null) {
			simType = defaultSimulationType;
		}

		try {
			// Added by WG20071030
			log.finer("the maxgames value is: " + this.maxgames + " ?");
			if (maxgames == 0) {
				log.finer("Maxgames exceeded, not adding game");
				return false;
			}
			if (maxgames > 0) {
				maxgames--;
				log.finer("added game, " + maxgames + " remaining in this run");
			}
			// end of add

			SimulationManager manager = getSimulationManager(simType);
			SimulationInfo newSimulation = createSimulationInfo(manager,
					simType, simParams);
			int simulationLength = newSimulation.getSimulationLength()
					+ SIM_DELAY;
			int index = 0;
			long time = ((getServerTime() + startDelay + 59000) / 60000) * 60000;
			if (time < getServerTime() + 2000) {
				// Do not start within the next two seconds
				time += 60000;
			}

			for (int n = simQueue.size(); index < n; index++) {
				SimulationInfo simulation = (SimulationInfo) simQueue
						.get(index);
				long startTime = simulation.getStartTime();
				if (simulation.hasSimulationID()) {
					// Next simulation has already been assigned an id and we
					// cannot
					// create another simulation before it
					time = roundTimeToMinute(simulation.getEndTime()
							+ SIM_DELAY);

				} else if ((startTime - time) > simulationLength) {
					// There is a free time lap to insert the simulation here
					break;
				} else {
					time = roundTimeToMinute(simulation.getEndTime()
							+ SIM_DELAY);
				}
			}

			// We have found the place to insert the new simulation
			// (last if no earlier time slot was available)
			newSimulation.setStartTime(time + this.gameStartOffset);
			simQueue.add(index, newSimulation);
			simulationCreated(newSimulation);
			if (notifyInfoServer) {
				infoConnection
						.requestSuccessful(InfoConnection.CREATE_SIMULATION,
								newSimulation.getID());
			}
			return true;
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not create simulation of type '"
					+ simType + "' (" + simParams + ')', e);
			if (notifyInfoServer) {
				infoConnection.requestFailed(InfoConnection.CREATE_SIMULATION,
						0, e.getMessage());
			}
			return false;
		}
	}

	// synchronized void createSimulation(String simType, String simParams,
	// long startTime,
	// int[] participants, int[] roles,
	// int competitionID)
	// throws NoSuchManagerException
	// {
	// if (simType == null) {
	// simType = defaultSimulationType;
	// }

	// SimulationManager manager = getSimulationManager(simType);
	// SimulationInfo newSimulation =
	// createSimulationInfo(manager, simType, simParams);
	// newSimulation.setStartTime(startTime);
	// if (participants != null) {
	// for (int i = 0, n = participants.length; i < n; i++) {
	// String roleName = manager.getSimulationRoleName(simType, roles[i]);
	// if (!manager.join(participants[i], roleName, newSimulation)) {
	// throw new IllegalArgumentException("could not add agent "
	// + participants[i]
	// + " in role "
	// + roles[i]
	// + " to " + newSimulation);
	// }
	// }
	// }

	// long endTime = newSimulation.getEndTime() + SIM_DELAY;
	// int index = simQueue.size() - 1;

	// for (; index >= 0; index--) {
	// SimulationInfo simulation = (SimulationInfo) simQueue.get(index);
	// long simStart = simulation.getStartTime();
	// if (simStart < endTime) {
	// // This game might conflict with the new simulation
	// long simEnd = simulation.getEndTime() + SIM_DELAY;
	// if (simEnd <= startTime) {
	// // Simulation is before the new simulation which is no
	// // problem. The new simulation should be inserted after this
	// // simulation.
	// break;
	// }

	// // The simulation conflicts with the new simulation
	// if (simulation.hasSimulationID()) {
	// // Next simulation has already been assigned an id and we cannot
	// // change it
	// throw new IllegalArgumentException("conflict: simulation "
	// + newSimulation
	// + " overlapps with "
	// + simulation);
	// }

	// // Must remove this simulation in order to add the new simulation
	// simQueue.remove(index);
	// simulationRemoved(simulation, "replaced");

	// // Must continue to check next simulation in case several
	// // simulations conflicts with the new simulation

	// } else {
	// // The simulation is beyond the new simulation and can be ignored

	// }
	// }

	// // We have found the place to insert the new simulation
	// // (last if no game was already scheduled later)
	// simQueue.add(index + 1, newSimulation);
	// simulationCreated(newSimulation);
	// }

	synchronized void removeSimulation(int simulationUniqID) {
		for (int i = simQueue.size() - 1; i >= 0; i--) {
			SimulationInfo sim = (SimulationInfo) simQueue.get(i);
			if (sim.hasSimulationID()) {
				// No futher simulations can be removed because they have been
				// locked
				break;
			}

			if (sim.getID() == simulationUniqID) {
				simQueue.remove(i);
				simulationRemoved(sim, "by request");
				break;
			}
		}
	}

	synchronized void joinSimulation(int simulationUniqID, int agentID,
			String simRoleName) throws NoSuchManagerException {
		SimulationInfo simulation = getSimulationInfo(simulationUniqID);
		if (simulation != null && !simulation.isParticipant(agentID)
				&& !simulation.isReservation()) {
			String simType = simulation.getType();
			SimulationManager manager = getSimulationManager(simType);
			int simRole = manager.getSimulationRoleID(simType, simRoleName);
			if (manager.join(agentID, simRole, simulation)) {
				simulationJoined(simulation, agentID);
			}
		}
	}

	synchronized void joinSimulation(int simulationUniqID, int agentID,
			int simRole) throws NoSuchManagerException {
		SimulationInfo simulation = getSimulationInfo(simulationUniqID);
		if (simulation != null && !simulation.isParticipant(agentID)
				&& !simulation.isReservation()) {
			String simType = simulation.getType();
			SimulationManager manager = getSimulationManager(simType);
			if (manager.join(agentID, simRole, simulation)) {
				simulationJoined(simulation, agentID);
			}
		}
	}

	private synchronized SimulationInfo getSimulationInfo(int uid) {
		for (int i = 0, n = simQueue.size(); i < n; i++) {
			SimulationInfo sim = (SimulationInfo) simQueue.get(i);
			if (sim.getID() == uid) {
				return sim;
			}
		}
		return null;
	}

	// -------------------------------------------------------------------
	// Simulation start/stop handling
	// -------------------------------------------------------------------

	private synchronized SimulationInfo getFirstSimulation() {
		return simQueue.size() > 0 ? (SimulationInfo) simQueue.get(0) : null;
	}

	private synchronized void removeFirstSimulation(SimulationInfo info,
			String message) {
		if ((simQueue.size() > 0) && (simQueue.get(0) == info)) {
			simQueue.remove(0);
			simulationRemoved(info, message);
		}
	}

	private void checkSimulation() {
		SimulationInfo info = getFirstSimulation();
		if (info != null) {
			long currentTime = getServerTime();
			long startTime = info.getStartTime();
			if (currentTime >= startTime) {
				// Time to start the simulation if not already running
				Simulation simulation = currentSimulation;
				if (simulation != null) {
					// Simulation is already running
					if (currentTime > (info.getEndTime() + 1000)) {
						// End time already passed but the simulation has not
						// ended itself.
						log.warning("requesting overdue simulation "
								+ simulation.getSimulationInfo()
										.getSimulationID() + " to end");
						simulation.requestStopSimulation();
					} else {
						// Nothing to do for now: let the simulation play
					}

				} else if (info.isReservation()) {
					// Time reservation
					if (currentTime >= info.getEndTime()) {
						// Time to end the time reservation
						removeFirstSimulation(info, null);
					}

				} else if (!info.hasSimulationID()
						&& !assignSimulationIDs(info)) {
					// It was not possible to assign a simulation id so the
					// simulation
					// should be scratched
					log.info("scratching started simulation without simulation id");
					removeFirstSimulation(info, null);

				} else if (info.isEmpty() && !allowEmptySimulations) {
					// No participants => the simulation should be scratched
					log.info("scratching simulation " + info.getSimulationID()
							+ " without participants");
					removeFirstSimulation(info, null);

				} else if (currentTime >= (startTime + 30000)) {
					// The game is already at least thirty seconds late. It must
					// be
					// scratched because simulations must not be started this
					// late.
					log.severe("scratching simulation "
							+ info.getSimulationID()
							+ " because it was started too late!!!");
					removeFirstSimulation(info, "delayed start");

				} else {
					// Time to start the simulation
					try {
						SimulationManager manager = getSimulationManager(info
								.getType());
						String simLogFile = SIM_FILE_PREFIX
								+ info.getSimulationID() + SIM_FILE_SUFFIX;
						int inError = 0;
						simulation = manager.createSimulation(info);
						enterSimulationLog(info.getSimulationID());
						try {
							simulation.init(this, info, simLogFile,
									simulationThreadPool);
							simulation.setup();
							inError = 1;
							simulationStarted(simulation);
							simulation.start();
							this.currentSimulation = simulation;
							inError = 2;
						} catch (Exception e) {
							int i = 0;
							log.log(Level.SEVERE, "could not start simulation "
									+ info.getSimulationID(), e);
						} finally {
							if (inError != 2) {
								simulation.close();
								if (inError == 1) {
									simulationStopped(simulation, true);
								}
								exitSimulationLog();
								// Remove the simulation log file because the
								// simulation was never played.
								new File(simLogFile).renameTo(new File("ERROR_"
										+ simLogFile));
							}
						}
					} catch (Exception e) {
						log.log(Level.SEVERE, "could not start simulation "
								+ info.getSimulationID(), e);
						removeFirstSimulation(info, "setup failed");
					}
				}
			} else if ((startTime - currentTime) < MAX_TIME_BEFORE_LOCK) {
				if (!info.hasSimulationID() && !info.isReservation()) {
					assignSimulationIDs(info);
				}
			}
		}
	}

	private boolean assignSimulationIDs(SimulationInfo info) {
		// Time to generate simulation ids for the first simulations
		if (info.isReservation()) {
			// Time reservation
			// info.setSimulationID(SimulationInfo.RESERVATION_ID);
			// simulationLocked(info);
			return true;
		} else if (!info.isEmpty() || allowEmptySimulations) {
			// Delay setting a simulation id until a participant joins
			// (otherwise we might want to scratch the simulation) unless
			// empty simulations are allowed (for dummies or builtin agents
			// to play with each others)
			info.setSimulationID(getNextSimulationID());
			simulationLocked(info);
			return true;
		} else {
			return false;
		}
	}

	// Called by the simulation message dispatcher when its time for the
	// simulations final stopping
	final void stopSimulation(Simulation simulation) {
		SimulationInfo info = simulation.getSimulationInfo();
		synchronized (this) {
			if ((simQueue.size() > 0) && (simQueue.get(0) == info)) {
				simQueue.remove(0);
			}
		}
		if (simulation == currentSimulation) {
			currentSimulation = null;
		}

		try {
			simulation.completeStop();
		} catch (Exception e) {
			log.log(Level.SEVERE,
					"could not stop simulation " + info.getSimulationID(), e);
		} finally {
			simulation.close();
			simulationStopped(simulation, false);
			exitSimulationLog();
		}
		// Should check simulation ids. FIX THIS!!!
	}

	// -------------------------------------------------------------------
	// Agent channel/message handling
	// -------------------------------------------------------------------

	final int loginAgentChannel(AgentChannel agent, String password) {
		String name = agent.getName();
		int userID = lookup.getAgentID(name);
		if (userID == -1) {
			// Force a recheck from the database
			infoConnection.checkUser(name);
			// Recheck the agent (only possible if the InfoServer is builtin
			// and otherwise the updated information should be available
			// when the agent tries to login next)
			userID = lookup.getAgentID(name);

			if (userID == -1) {
				// MLB 20080411 - Added to allow non-pre-registered agents to
				// play,
				// for use in experimental work
				if (allowUnregisteredAgents) {
					userID = infoConnection.addUser(name, password,
							"autocreated");
				}

				if (userID == -1) {
					throw new IllegalArgumentException("No user with name '"
							+ name + "' found");
				}
			}
		}

		if (!lookup.validateAgent(userID, password)) {
			throw new IllegalArgumentException("Password incorrect");
		}

		return userID;
	}

	final void agentChannelAvailable(AgentChannel agent) {
		String name = agent.getName();
		int userID = agent.getUserID();
		AgentChannel oldChannel;
		synchronized (this) {
			int index = AgentChannel.indexOf(channelList, 0, channelNumber,
					userID);
			if (index >= 0) {
				channelList[index] = agent;
			} else {
				if (channelList == null) {
					channelList = new AgentChannel[8];
				} else if (channelNumber == channelList.length) {
					channelList = (AgentChannel[]) ArrayUtils.setSize(
							channelList, channelNumber + 8);
				}
				channelList[channelNumber++] = agent;
			}

			oldChannel = (AgentChannel) channelTable.get(name);
			channelTable.put(name, agent);
		}
		// Close old channel if such exists
		if (oldChannel != null) {
			log.warning("closing old channel " + oldChannel.getName()
					+ " due to new connection " + agent.getName());
			String messageText = "The server connection was closed due to new connection\n"
					+ "from agent "
					+ name
					+ '@'
					+ oldChannel.getRemoteHost()
					+ '.';
			Alert alert = new Alert("Multiple Connections", messageText);
			oldChannel.deliverToAgent(new Message(ADMIN, name, alert));
			oldChannel.close();
		}

		Simulation sim = this.currentSimulation;
		if (sim != null) {
			sim.agentChannelAvailable(agent);
		}
	}

	synchronized void logoutAgentChannel(AgentChannel agent) {
		// The agent channel will self handle the deregistration with any
		// simulation agent and we only need to remove the agent channel
		// here.

		String name = agent.getName();
		AgentChannel oldChannel = (AgentChannel) channelTable.get(name);
		if (oldChannel == agent) {
			channelTable.remove(name);
		}

		int index = AgentChannel.indexOf(channelList, 0, channelNumber,
				agent.getUserID());
		if (index >= 0 && channelList[index] == agent) {
			channelNumber--;
			channelList[index] = channelList[channelNumber];
			channelList[channelNumber] = null;
		}
	}

	synchronized AgentChannel getAgentChannel(int userID) {
		int index = AgentChannel.indexOf(channelList, 0, channelNumber, userID);
		return index >= 0 ? channelList[index] : null;
	}

	// Used to send messages to agents before they participate in simulations
	void deliverMessageToAgent(Message message) {
		AgentChannel agent = (AgentChannel) channelTable.get(message
				.getReceiver());
		if (agent != null) {
			agent.deliverToAgent(message);
		}
	}

	void deliverMessageFromAgent(AgentChannel agent, Message message) {
		Transportable content = handleMessage(agent, message);
		if (content != null) {
			agent.deliverToAgent(message.createReply(content));
		}
	}

	/**
	 * Delivers a message to the administrator.
	 * 
	 * @param simulation
	 *            the simulation from which an agent sent the message
	 * @param message
	 *            the message
	 */
	final void messageReceived(Simulation simulation, Message message) {
		Transportable content = handleMessage(null, message);
		if (content != null) {
			// Deliver the reply if such has been returned
			simulation.deliverMessage(message.createReply(content));
		}
	}

	private Transportable handleMessage(AgentChannel channel, Message message) {
		Transportable content = message.getContent();
		// Admin only handles "ask" on AdminContent...
		if ((content.getClass() == AdminContent.class)
				&& "admin".equals(message.getReceiver())) {
			AdminContent adminContent = (AdminContent) content;
			if (adminContent.isError()) {
				// Error to the administration. Should never happen
				log.severe("received admin error: " + adminContent);
				return null;
			} else {

				switch (adminContent.getType()) {
				case AdminContent.SERVER_TIME:
					return replyServerTime(message);
				case AdminContent.NEXT_SIMULATION:
					return replyNextSimulation(channel, adminContent);
				case AdminContent.JOIN_SIMULATION:
					return replyJoinSimulation(channel, adminContent);
				default:
					log.warning("could not handle admin " + adminContent);
					return new AdminContent(adminContent.getType(),
							AdminContent.NOT_SUPPORTED);
				}
			}
		} else {
			AdminContent adminContent = new AdminContent(AdminContent.ERROR);
			adminContent.setError(AdminContent.NOT_SUPPORTED,
					content.getTransportName() + " not supported");
			adminContent.setAttribute("name", content.getTransportName());
			return adminContent;
		}
	}

	private Transportable replyServerTime(Message message) {
		AdminContent content = new AdminContent(AdminContent.SERVER_TIME);
		content.setAttribute("time", getServerTime());
		return content;
	}

	private Transportable replyNextSimulation(AgentChannel agent,
			AdminContent content) {
		if (agent == null) {
			// Message from a builtin agent
			return new AdminContent(AdminContent.NEXT_SIMULATION,
					AdminContent.NOT_SUPPORTED);
		}

		String simType = content.getAttribute("type", null);
		int userID = agent.getUserID();
		SimulationInfo info = nextSimulation(userID, simType);
		content = new AdminContent(AdminContent.NEXT_SIMULATION);
		if (info != null) {
			String role = getSimulationRoleName(info, userID);
			if (info.hasSimulationID()) {
				content.setAttribute("simulation", info.getSimulationID());
			}
			content.setAttribute("startTime", info.getStartTime());
			content.setAttribute("type", info.getType());
			if (role != null) {
				content.setAttribute("role", role);
			}

		} else {
			int delay = startDelay >= 30000 ? 30000 : startDelay;

			long currentTime = getServerTime();
			long time = currentTime + delay;
			Simulation simulation = this.currentSimulation;
			if (simulation != null) {
				long endTime = simulation.getSimulationInfo().getEndTime() + 60000;
				if (endTime > time) {
					time = endTime;
				}
			} else if ((info = getFirstSimulation()) != null) {
				long startTime = info.getStartTime() - 30000;
				if (startTime < time) {
					time = startTime;
				}
			}
			// Make sure the agent waits at least 10 seconds
			if (time < currentTime + 10000) {
				time = currentTime + 10000;
			}
			content.setAttribute("nextTime", time);
		}
		return content;
	}

	private Transportable replyJoinSimulation(AgentChannel agent,
			AdminContent content) {
		if (agent == null) {
			// Message from a builtin agent
			return new AdminContent(AdminContent.JOIN_SIMULATION,
					AdminContent.NOT_SUPPORTED);
		}

		String simType = content.getAttribute("type", null);
		String simParams = content.getAttribute("params", null);
		String simRole = content.getAttribute("role", null);
		int userID = agent.getUserID();
		try {
			SimulationInfo info = joinSimulation(userID, simType, simParams,
					simRole);
			if (info != null) {
				String role = getSimulationRoleName(info, userID);
				content = new AdminContent(AdminContent.JOIN_SIMULATION);
				content.setAttribute("type", info.getType());
				if (info.hasSimulationID()) {
					content.setAttribute("simulation", info.getSimulationID());
				}
				content.setAttribute("startTime", info.getStartTime());
				if (role != null) {
					content.setAttribute("role", role);
				}
				return content;
			} else {
				// Should set an error reason... FIX THIS!!!
				return new AdminContent(AdminContent.JOIN_SIMULATION,
						AdminContent.NO_SIMULATION_CREATED);
			}

		} catch (Exception e) {
			log.log(Level.SEVERE, "could not join simulation", e);
			return new AdminContent(AdminContent.JOIN_SIMULATION,
					AdminContent.NO_SIMULATION_CREATED, e.getMessage()); // Better
																			// error
			// message. FIX
			// THIS!!!
		}
	}

	// -------------------------------------------------------------------
	// Simulation manager handling
	// -------------------------------------------------------------------

	private SimulationManager getSimulationManager(String simulationType)
			throws NoSuchManagerException {
		SimulationManager manager = (SimulationManager) simulationManagerTable
				.get(simulationType);
		if (manager == null) {
			throw new NoSuchManagerException(simulationType);
		}
		return manager;
	}

	private String getSimulationRoleName(SimulationInfo info, int userID) {
		String type = info.getType();
		SimulationManager manager = (SimulationManager) simulationManagerTable
				.get(type);
		if (manager != null) {
			int index = info.indexOfParticipant(userID);
			if (index >= 0) {
				int role = info.getParticipantRole(index);
				return manager.getSimulationRoleName(type, role);
			}
		}
		return null;
	}

	private SimulationInfo createSimulationInfo(SimulationManager manager,
			String simulationType, String simulationParams)
			throws NoSuchManagerException {
		SimulationInfo info = manager.createSimulationInfo(simulationType,
				simulationParams);
		if (info == null) {
			throw new NoSuchManagerException(simulationType);
		}
		return info;
	}

	private SimulationInfo createTimeReservation(long startTime, int length) {
		SimulationInfo info = new SimulationInfo(getNextUniqueSimulationID(),
				SimulationInfo.RESERVED, null, length);
		info.setStartTime(startTime + this.gameStartOffset);
		// No one can ever join a time reservation
		info.setFull();
		return info;
	}

	void addSimulationManager(String type, SimulationManager manager) {
		log.info("adding manager for simulation type " + type);
		simulationManagerTable.put(type, manager);
	}

	public InfoConnection getInfoConnection() {
		return infoConnection;
	}

	// -------------------------------------------------------------------
	// Logging handling
	// -------------------------------------------------------------------

	private synchronized void setLogging(ConfigManager config)
			throws IOException {
		int consoleLevel = config.getPropertyAsInt(CONF + "log.consoleLevel",
				config.getPropertyAsInt("log.consoleLevel", 0));
		int fileLevel = config.getPropertyAsInt(CONF + "log.fileLevel",
				config.getPropertyAsInt("log.fileLevel", 0));
		Level consoleLogLevel = LogFormatter.getLogLevel(consoleLevel);
		Level fileLogLevel = LogFormatter.getLogLevel(fileLevel);
		Level logLevel = consoleLogLevel.intValue() < fileLogLevel.intValue() ? consoleLogLevel
				: fileLogLevel;
		boolean showThreads = config.getPropertyAsBoolean(CONF + "log.threads",
				config.getPropertyAsBoolean("log.threads", false));

		Logger root = Logger.getLogger("");
		// FIXME this code is very specific and was put here in order to remove
		// the swing logging (caused by the gui)
		root.setLevel(Level.OFF);
		Logger.getLogger("com.botbox").setLevel(logLevel);
		Logger.getLogger("edu").setLevel(logLevel);
		Logger.getLogger("se.sics").setLevel(logLevel);
		Logger.getLogger("tau").setLevel(logLevel);
		// ---------------------------------------------------------------------

		formatter.setShowingThreads(showThreads);
		LogFormatter.setConsoleLevel(consoleLogLevel);
		// LogFormatter.setLevelForAllHandlers(logLevel);

		if (fileLogLevel != Level.OFF) {
			if (rootFileHandler == null) {
				rootFileHandler = new FileHandler(logName + "%g.log", 1000000,
						10);
				rootFileHandler.setFormatter(formatter);
				root.addHandler(rootFileHandler);
			}
			rootFileHandler.setLevel(fileLogLevel);
			if (simLogHandler != null) {
				simLogHandler.setLevel(fileLogLevel);
			}
		} else if (rootFileHandler != null) {
			exitSimulationLog();
			root.removeHandler(rootFileHandler);
			rootFileHandler.close();
			rootFileHandler = null;
		}
	}

	synchronized void enterSimulationLog(int simulationID) {
		exitSimulationLog();

		if (rootFileHandler != null) {
			LogFormatter.separator(log, Level.FINE,
					"Entering log for simulation " + simulationID);
			try {
				Logger root = Logger.getLogger("");
				String name = logPrefix + "_SIM_" + simulationID + ".log";
				simLogHandler = new FileHandler(name, true);
				simLogHandler.setFormatter(formatter);
				simLogHandler.setLevel(rootFileHandler.getLevel());
				simLogName = name;
				root.addHandler(simLogHandler);
				root.removeHandler(rootFileHandler);
				LogFormatter.separator(log, Level.FINE, "Log for simulation "
						+ simulationID + " started");
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not open log file for simulation "
						+ simulationID, e);
			}
		}
	}

	synchronized void exitSimulationLog() {
		if (simLogHandler != null) {
			Logger root = Logger.getLogger("");
			LogFormatter.separator(log, Level.FINE, "Simulation log complete");

			root.addHandler(rootFileHandler);
			root.removeHandler(simLogHandler);
			simLogHandler.close();
			simLogHandler = null;
			// Try to remove the lock file since it is no longer needed
			if (simLogName != null) {
				new File(simLogName + ".lck").delete();
				simLogName = null;
			}
		}
	}

} // Admin
