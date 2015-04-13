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
 * SimServer
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Jan 09 14:30:13 2003
 * Updated : $Date: 2008-04-11 20:26:24 -0500 (Fri, 11 Apr 2008) $
 *           $Revision: 4090 $
 */

//Modified by BC, UMich.  DBFields for score have been changed to double
package se.sics.tasim.is.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.botbox.util.ArrayQueue;
import com.botbox.util.ArrayUtils;


import org.mortbay.http.SecurityConstraint;
import se.sics.isl.db.DBField;
import se.sics.isl.db.DBMatcher;
import se.sics.isl.db.DBObject;
import se.sics.isl.db.DBResult;
import se.sics.isl.db.DBTable;
import se.sics.isl.db.Database;
import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.is.AgentInfo;
import se.sics.tasim.is.CompetitionSchedule;
import se.sics.tasim.is.EventWriter;
import se.sics.tasim.is.InfoConnection;
import se.sics.tasim.is.SimConnection;
import se.sics.tasim.is.SimulationInfo;
import se.sics.tasim.is.TransportEventWriter;
import se.sics.tasim.logtool.LogReader;
import se.sics.tasim.logtool.ParticipantInfo;

public class SimServer {

	private static final Logger log = Logger.getLogger(SimServer.class
			.getName());

	private static final String GAME_LOG_NAME = "game.slg.gz";

	// Flags in the competition results and game results tables
	public static final int SIMULATION_SCRATCHED = 1 << 4;
	public static final int ZERO_GAME = 1 << 5;

	/**
	 * Remember if the database has been checked for the flags field (backward
	 * compability)
	 */
	private boolean hasVerifiedFlag = false;

	/** Web page handling */
	private final ComingPage comingPage;
	private final HttpPage historyPage;
	private final HttpPage scorePage;
	private final HttpPage viewerPage;

	// This pages are null if administration pages are disabled in the
	// server configuration
	private final AdminPage adminPage;
	private HttpPage schedulePage;

	private final InfoServer infoServer;
	private final String serverName;

	/** Simulation result pages */
	private final String resultsPath;
	private final String urlGamePath;
	private String simTablePrefix = "simtable-";
	private int simulationsPerPage = 20;

	private String serverMessageFile;
	private String serverMessage;

	private final Database database;
	private DBTable simulationTable;
	private DBTable participantTable;
	private DBTable playedTable;
	private DBTable resultTable;
	private boolean storeResults = false;

	/** State variables */
	private DBTable stateTable;
	private int lastSimulationID = 0;
	private int lastUniqueSimulationID = 0;
	private int lastPlayedSimulationID = -1;
	private int lastCompetitionID = 0;
	private int lastFinishedCompetitionID = -1;

	/** Coming simulations */
	private ArrayQueue comingQueue = new ArrayQueue();
	private SimulationInfo[] comingCache;
	private SimulationInfo nextComingSimulation = null;

	/** Competitions */
	private DBTable competitionTable;
	private DBTable competitionResultTable;
	private DBTable competitionParticipantTable;
	private ArrayQueue comingCompetitions = new ArrayQueue();
	private Competition[] competitions;
	private Competition currentCompetition;
	private Competition nextCompetition;
	private Competition[] pendingCompetitions;

	/** SCM server connections */
	private InfoConnectionImpl infoConnection;
	private SimConnection simConnection;

	private boolean isConnected = false;

	/** Current simulation */
	private SimulationInfo currentSimulation = null;
	private String currentTimeUnitName;
	private int currentTimeUnitCount;
	private ViewerCache currentViewerCache;
	private String[] currentNames = null;

	private BlockingViewerChannel[] viewerConnections;

	/** Chat */
	private final static int CHAT_CACHE_SIZE = 20;
	private final static int MAX_CHAT_CACHE_RESTORE_SIZE = 3072;

	private PrintWriter chatlog;
	private ChatMessage[] chatCache = new ChatMessage[CHAT_CACHE_SIZE];
	private int chatCacheNumber, chatCacheIndex;

	// Note: all viewer handling (viewer connection, chat message,
	// binary message generation) are using the transportWriter object
	// as lock.
	private BinaryTransportWriter transportWriter = new BinaryTransportWriter();
	private EventWriter transportEventWriter = new TransportEventWriter(
			transportWriter);

	/**
	 * This is a quick hack to limit the number of times an agent might schedule
	 * in advanced using the web interface. Configurable via the admin
	 * interface.
	 */
	private int maxAgentScheduled = 0;

	public SimServer(InfoServer infoServer, Database database,
			InfoConnectionImpl connection, String resultsPath,
			boolean storeResults) {
		this.infoServer = infoServer;
		this.database = database;
		this.serverName = connection.getServerName();
		this.resultsPath = resultsPath = resultsPath + "history"
				+ File.separatorChar;
		this.storeResults = storeResults;

		this.urlGamePath = "http://" + serverName + ':'
				+ infoServer.getHttpPort() + '/' + serverName + "/history/";

		String chatLogFileName = serverName + "_chat.log";
		restoreChatCache(chatLogFileName);
		try {
			chatlog = new PrintWriter(new FileWriter(chatLogFileName, true),
					true);
		} catch (IOException e) {
			log.log(Level.SEVERE, "could not open chat log '" + chatLogFileName
					+ '\'', e);
		}

		this.serverMessageFile = serverName + "_msg.txt";
		serverMessage = readFile(this.serverMessageFile);

		// Initialize the databases
		setupStateTable(database);

		participantTable = database.getTable("comingparticipants");
		if (participantTable == null) {
			participantTable = database.createTable("comingparticipants");
			participantTable.createField("id", DBField.INTEGER, 32, 0);
			participantTable.createField("participantid", DBField.INTEGER, 32,
					0);
			participantTable.createField("participantrole", DBField.INTEGER,
					32, 0);
			participantTable.flush();
		}
		simulationTable = database.getTable("comingsimulations");
		if (simulationTable == null) {
			simulationTable = database.createTable("comingsimulations");
			simulationTable.createField("id", DBField.INTEGER, 32,
					DBField.UNIQUE | DBField.PRIMARY | DBField.INDEX);
			// The simid field MUST NOT be unique because until it is
			// 'locked' it will be set to -1!!!
			simulationTable.createField("simid", DBField.INTEGER, 32, 0);
			simulationTable.createField("type", DBField.STRING, 32, 0);
			simulationTable.createField("params", DBField.STRING, 255,
					DBField.MAY_BE_NULL);
			simulationTable.createField("starttime", DBField.LONG, 64, 0);
			simulationTable.createField("length", DBField.INTEGER, 32, 0);
			simulationTable.createField("flags", DBField.INTEGER, 32, 0);
			simulationTable.flush();
		} else {
			DBMatcher dbm = new DBMatcher();
			DBMatcher dbm2 = new DBMatcher();
			DBResult res = simulationTable.select(dbm);
			long currentTime = infoServer.getServerTimeMillis();
			ArrayList removeList = null;
			boolean hasFlags;
			if (hasVerifiedFlag || simulationTable.hasField("flags")) {
				hasVerifiedFlag = true;
				hasFlags = true;
			} else {
				hasFlags = false;
			}

			while (res.next()) {
				int id = res.getInt("id");
				int simID = res.getInt("simid");
				String type = res.getString("type");
				String params = res.getString("params");
				long startTime = res.getLong("starttime");
				int length = res.getInt("length") * 1000;
				int flags = hasFlags ? res.getInt("flags") : 0;
				SimulationInfo info = new SimulationInfo(id, type, params,
						length);
				if (simID >= 0) {
					info.setSimulationID(simID);
				}
				info.setStartTime(startTime);
				dbm2.setInt("id", id);

				DBResult res2 = participantTable.select(dbm2);
				while (res2.next()) {
					info.addParticipant(res2.getInt("participantid"), res2
							.getInt("participantrole"));
				}
				res2.close();

				// The flags must be set last because they can mark the
				// simulation as 'full' i.e. not accepting any more
				// participants
				info.setFlags(flags);

				if (currentTime > info.getStartTime()) {
					// Simulation already started
					if (removeList == null) {
						removeList = new ArrayList();
					}
					removeList.add(info);
				} else {
					addSimulation(info, null);
				}
			}
			res.close();

			if (removeList != null) {
				// These simulations should be removed from the database
				// (could not be removed above due to concurrent modifications
				// not allowed in DBTable)
				for (int i = 0, n = removeList.size(); i < n; i++) {
					SimulationInfo info = (SimulationInfo) removeList.get(i);
					dbm.clear();
					dbm.setInt("id", info.getID());
					participantTable.remove(dbm);
					dbm.setLimit(1);
					simulationTable.remove(dbm);
				}
				participantTable.flush();
				simulationTable.flush();
			}
		}

		if (storeResults) {
			playedTable = database.getTable("playedsimulations");
			if (playedTable == null) {
				playedTable = database.createTable("playedsimulations");
				playedTable.createField("id", DBField.INTEGER, 32,
						DBField.UNIQUE | DBField.PRIMARY | DBField.INDEX);
				playedTable.createField("simid", DBField.INTEGER, 32,
						DBField.UNIQUE);
				playedTable.createField("type", DBField.STRING, 32, 0);
				playedTable.createField("starttime", DBField.LONG, 64, 0);
				playedTable.createField("length", DBField.INTEGER, 32, 0);
				playedTable.createField("flags", DBField.INTEGER, 32, 0);
				playedTable.flush();
			}

			resultTable = database.getTable("results");
			if (resultTable == null) {
				resultTable = database.createTable("results");
				resultTable.createField("id", DBField.INTEGER, 32, 0);
				resultTable
						.createField("participantid", DBField.INTEGER, 32, 0);
				resultTable.createField("participantrole", DBField.INTEGER, 32,
						0);
				//Modded by BC, UMich
				resultTable.createField("score", DBField.DOUBLE, 64, 0);
				resultTable.flush();
			}
		}

		setupCompetitionTable(database);

		/** Set up web handling */
		PageHandler pageHandler = infoServer.getPageHandler();

		SecurityConstraint security = new SecurityConstraint(infoServer.getServerType(), "*");
		infoServer.getHttpContext().addSecurityConstraint(
				"/" + serverName + "/games/*", security);
		infoServer.getHttpContext().addSecurityConstraint(
				"/" + serverName + "/viewer/*", security);

		security = new SecurityConstraint(infoServer.getServerType(), AgentRealm.ADMIN_ROLE);
		infoServer.getHttpContext().addSecurityConstraint(
				"/" + serverName + "/admin/*", security);
		infoServer.getHttpContext().addSecurityConstraint(
				"/" + serverName + "/schedule/*", security);

		comingPage = new ComingPage(infoServer, this);
		pageHandler.addPage("/" + serverName + "/games/", comingPage);
		viewerPage = new ViewerPage(infoServer, this);
		pageHandler.addPage("/" + serverName + "/viewer/", viewerPage);
		scorePage = new ScorePage(this, null);
		pageHandler.addPage("/" + serverName + "/scores/", scorePage);

		String path = "/" + serverName + "/history/";
		historyPage = new HistoryPage(path, this, resultsPath, simTablePrefix,
				simulationsPerPage);
		pageHandler.addPage(path, historyPage);

		if (infoServer.getConfig().getPropertyAsBoolean("admin.pages", true)) {
			String adminHeader = "<table border=0 bgcolor=black cellspacing=0 "
					+ "cellpadding=1 width='100%'>"
					+ "<tr><td>"
					+ "<table border=0 bgcolor='#e0e0e0' "
					+ "cellspacing=0 width='100%'><tr><td align=center>"
					+ "<font face=arial>"
					+ "<a href='/"
					+ serverName
					+ "/admin/'>Administration</a> | "
					+ "<a href='/"
					+ serverName
					+ "/admin/games/'>Game Manager</a> | "
					+ "<a href='/"
					+ serverName
					+ "/admin/competition/'>"
					+ "Competition Manager</a> | "
					+ "<a href='/"
					+ serverName
					+ "/schedule/'>Competition Scheduler</a>"
					+ " | "
					+ "<a href='http://www.sics.se/tac/docs/scm/server/0.8.8/admin.html' "
					+ "target='sadmin'>Help</a>" + "</font>"
					+ "</td></tr></table></td></tr></table>\r\n<p>";

			adminPage = new AdminPage(infoServer, this, path, adminHeader);
			pageHandler.addPage("/" + serverName + "/admin/*", adminPage);
			String className = infoServer.getConfig().getProperty("pages.gamescheduler.class", null);
            try {
				Class sp = Class.forName(className);
				Constructor spc = sp.getDeclaredConstructor(InfoServer.class, SimServer.class, String.class);
				schedulePage = (HttpPage)spc.newInstance(infoServer, this, adminHeader);
			} catch (Exception e) {
				e.printStackTrace();
				schedulePage = new GameScheduler(infoServer, this, adminHeader);
			}
			pageHandler.addPage("/" + serverName + "/schedule/", schedulePage);
		} else {
			adminPage = null;
			schedulePage = null;
		}

		// Setup the transport writer
		// transportWriter
		// .setSupported(BinaryTransportWriter.SUPPORT_CONSTANTS, true);
		transportWriter
				.setSupported(BinaryTransportWriter.SUPPORT_TABLES, true);
		setInfoConnection(connection);
	}

	private String readFile(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			StringBuffer data = new StringBuffer();
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					data.append(line).append('\n');
				}
			} finally {
				reader.close();
			}
			return data.toString();
		} catch (FileNotFoundException e) {

		} catch (Exception e) {
			log.log(Level.WARNING, "could not load text from " + filename, e);
		}
		return null;
	}

	private void saveFile(String filename, String text) {
		try {
			FileWriter writer = new FileWriter(filename, false);
			try {
				writer.write(text);
			} finally {
				writer.close();
			}

		} catch (Exception e) {
			log.log(Level.SEVERE, "could not save text to " + filename + " ("
					+ text + ')', e);
		}
	}

	public String getServerName() {
		return serverName;
	}

	public String getSimulationTablePrefix() {
		return simTablePrefix;
	}

	// API towards history page. REMOVE THIS!!!
	public int getSecondsToNextSimulationEnd() {
		SimulationInfo info = this.currentSimulation;
		if (info != null) {
			long currentTime = infoServer.getServerTimeMillis();
			long endTime = info.getEndTime();
			if (currentTime < endTime) {
				return (int) ((endTime - currentTime) / 1000) + 10;
			}
		}
		return 60;
	}

	public int getSimulationsPerPage() {
		return simulationsPerPage;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setInfoConnection(InfoConnectionImpl connection) {
		synchronized (this) {
			if (this.infoConnection != null) {
				this.infoConnection.close();
			}
			if (this.simConnection != null) {
				this.simConnection.close();
			}
			this.isConnected = true;
			this.infoConnection = connection;
			this.simConnection = connection.getSimConnection();

			connection.setSimServer(this);
		}

		// Send all initial state (users, server time) to the sim server
		simConnection.setServerTime(infoServer.getServerTimeMillis());
		simConnection.dataUpdated(SimConnection.SIM_ID, lastSimulationID);
		simConnection.dataUpdated(SimConnection.UNIQUE_SIM_ID,
				lastUniqueSimulationID);

		AgentInfo[] agents = infoServer.getAgentInfos();
		if (agents != null) {
			for (int i = 0, n = agents.length; i < n; i++) {
				AgentInfo agent = agents[i];
				simConnection.setUser(agent.getName(), agent.getPassword(),
						agent.getID());
			}
		}

		// Send all coming simulations. Must use a cache because the
		// simulation server might cause some simulations to be removed
		// during notification if the information service and the
		// simulation server is running in the same process. SHOULD BE
		// OPTIMIZED TO ONLY SEND SIMULATIONS THE OTHER SIDE DOES NOT KNOW
		// ABOUT USING lastSimulationID!!! FIX THIS!!! TODO
		SimulationInfo[] sims = getComingSimulations();
		if (sims != null) {
			for (int i = 0, n = sims.length; i < n; i++) {
				simConnection.simulationInfo(sims[i]);
			}
		}

		simConnection.dataUpdated(SimConnection.STATUS,
				SimConnection.STATUS_READY);
		// Add the chat history
		ChatMessage[] messages = getChatMessages();
		if (messages != null) {
			for (int i = 0, n = messages.length; i < n; i++) {
				ChatMessage chat = messages[i];
				simConnection
						.addChatMessage(chat.getTime(), chat.getServerName(),
								chat.getUserName(), chat.getMessage());
			}
		}
	}

	public void close() {
		if (isConnected) {
			InfoConnection iConnection = null;
			SimConnection sConnection = null;
			synchronized (this) {
				if (isConnected) {
					isConnected = false;
					if (this.infoConnection != null) {
						iConnection = this.infoConnection;
						this.infoConnection = null;
						sConnection = this.simConnection;
						this.simConnection = null;
					}
				}
			}
			if (iConnection != null) {
				iConnection.close();
				if (sConnection != null) {
					sConnection.close();
				}
			}
		}
	}

	public void setUser(String name, String password, int userID) {
		if (isConnected) {
			simConnection.setUser(name, password, userID);
		}
	}

	// -------------------------------------------------------------------
	// Coming simulation handling (and API towards ComingPage)
	// -------------------------------------------------------------------

	synchronized SimulationInfo getSimulationInfo(int uid) {
		for (int i = 0, n = comingQueue.size(); i < n; i++) {
			SimulationInfo sim = (SimulationInfo) comingQueue.get(i);
			if (sim.getID() == uid) {
				return sim;
			}
		}
		return null;
	}

	synchronized int getAgentScheduledCount(int agentid) {
		int count = 0;
		Competition next = this.nextCompetition;
		int startGame = next == null ? Integer.MAX_VALUE : next
				.getStartUniqueID();
		for (int i = 0, n = comingQueue.size(); i < n; i++) {
			SimulationInfo sim = (SimulationInfo) comingQueue.get(i);
			if (sim.getID() == startGame) {
				break;
			}
			if (sim.isParticipant(agentid)) {
				count++;
			}
		}
		return count;
	}

	// -------------------------------------------------------------------
	// Interface towards InfoConnection
	// -------------------------------------------------------------------

	public synchronized void requestSuccessful(int operation, final int id) {
		if (operation == InfoConnection.SCHEDULE_COMPETITION) {
			int index = Competition.indexOf(pendingCompetitions, id);
			if (index >= 0) {
				Competition competition = pendingCompetitions[index];
				pendingCompetitions = (Competition[]) ArrayUtils.remove(
						pendingCompetitions, index);

				// Add the finished competition but only if it contains at
				// least one simulation
				if (competition.getSimulationCount() > 0) {
					addCompetition(competition, true);

					// Use separate thread to generate initial score pages
					(new Thread("generate.comp." + id) {
						public void run() {
							try {
								generateCompetitionResults(id);
							} catch (Exception e) {
								log.log(Level.SEVERE,
										"could not generate results for competition "
												+ id, e);
							}
						}
					}).start();
				}
			}
		}
	}

	public synchronized void requestFailed(int operation, int id, String reason) {
		if (operation == InfoConnection.SCHEDULE_COMPETITION) {
			int index = Competition.indexOf(pendingCompetitions, id);
			if (index >= 0) {
				pendingCompetitions = (Competition[]) ArrayUtils.remove(
						pendingCompetitions, index);
			}
		}
	}

	public void checkUser(String userName) {
		infoServer.updateUser(userName);
	}

	// MLB 20080411 - Added to allow non-pre-registered agents to play,
	// for use in experimental work
	public int addUser(String name, String password, String email) {
		// returns the new userID
		return infoServer.createUser(name, password, email);
	}

	public void dataUpdated(int type, int value) {
		if (type == SimConnection.STATUS) {
			if (value == SimConnection.STATUS_READY) {
				// Simulation server is ready!
			}

		} else if (type == SimConnection.UNIQUE_SIM_ID) {
			if (value > lastUniqueSimulationID) {
				lastUniqueSimulationID = value;

				setStateTable("lastUniqueSimulationID", lastUniqueSimulationID,
						null, null);
			}

		} else if (type == SimConnection.SIM_ID) {
			if (value > lastSimulationID) {
				lastSimulationID = value;
				setStateTable("lastSimulationID", lastSimulationID, null, null);
			}
		}
	}

	public synchronized void simulationCreated(SimulationInfo info) {
		long startTime = info.getStartTime();
		long currentTime = infoServer.getServerTimeMillis();
		int index = 0;
		int ugid = info.getID();
		DBObject o = new DBObject();

		// Add to database
		if (ugid > lastUniqueSimulationID) {
			lastUniqueSimulationID = ugid;

			setStateTable("lastUniqueSimulationID", lastUniqueSimulationID,
					null, o);
			o.clear();
		}

		// Insert into the simulation queue but only add the simulation
		// into the database if not already added
		addSimulation(info, o);

		comingPage.simulationCreated(info);
	}

	public void simulationCreated(SimulationInfo info, int competitionID) {
		simulationCreated(info);

		Competition[] pending = this.pendingCompetitions;
		int index = Competition.indexOf(pending, competitionID);
		if (index >= 0) {
			pending[index].addSimulation(info);
		}
	}

	// Add the simulation to the simulation queue but only add the
	// simulation to the database if the database object is non-NULL.
	// Note: MAY ONLY BE CALLED SYNCHRONIZED ON THIS OBJECT!
	private boolean addSimulation(SimulationInfo info, DBObject o) {
		int index = 0;
		int ugid = info.getID();
		long startTime = info.getStartTime();
		for (int n = comingQueue.size(); index < n; index++) {
			SimulationInfo sim = (SimulationInfo) comingQueue.get(index);
			if (sim.getID() == ugid) {
				// simulation already exists
				if (o != null) {
					// Only add any participants not already added to the
					// database
					o.setInt("id", ugid);
					try {
						for (int j = 0, m = info.getParticipantCount(); j < m; j++) {
							int pid = info.getParticipantID(j);
							int role = info.getParticipantRole(j);
							if (sim.addParticipant(pid, role)) {
								o.setInt("participantid", pid);
								o.setInt("participantrole", role);
								participantTable.insert(o);
							}
						}
						participantTable.flush();
					} catch (Exception e) {
						log.log(Level.SEVERE,
								"could not save coming participants for "
										+ info, e);
					}
				}
				return false;
			} else if (sim.getStartTime() > startTime) {
				// Simulation should be inserted here
				break;
			}
		}
		comingQueue.add(index, info);
		comingCache = null;
		if (o != null) {
			try {
				o.setInt("id", ugid);
				o.setInt("simid", info.getSimulationID());
				o.setString("type", info.getType());
				if (info.getParams() != null) {
					o.setString("params", info.getParams());
				}
				o.setLong("starttime", info.getStartTime());
				o.setInt("length", info.getSimulationLength() / 1000);
				if (hasVerifiedFlag || simulationTable.hasField("flags")) {
					hasVerifiedFlag = true;
					o.setInt("flags", info.getFlags());
				}
				simulationTable.insert(o);
				// Add any participants to the database

				o.clear();
				o.setInt("id", ugid);
				for (int i = 0, n = info.getParticipantCount(); i < n; i++) {
					o.setInt("participantid", info.getParticipantID(i));
					o.setInt("participantrole", info.getParticipantRole(i));
					participantTable.insert(o);
				}
				simulationTable.flush();
				participantTable.flush();
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not store coming simulation "
						+ info, e);
			}
		}
		checkNextSimulation();
		return true;
	}

	public synchronized void simulationRemoved(int simulationUniqID, String msg) {
		try {
			DBMatcher dbm = new DBMatcher();
			dbm.setInt("id", simulationUniqID);
			participantTable.remove(dbm);
			dbm.setLimit(1);
			simulationTable.remove(dbm);
			participantTable.flush();
			simulationTable.flush();
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not remove simulation "
					+ simulationUniqID, e);
		}

		// remove from the simulation queue
		for (int i = 0, n = comingQueue.size(); i < n; i++) {
			SimulationInfo sim = (SimulationInfo) comingQueue.get(i);
			if (sim.getID() == simulationUniqID) {
				comingQueue.remove(i);
				comingCache = null;
				checkNextSimulation();
				break;
			}
		}
	}

	// Note: MAY ONLY BE CALLED SYNCHRONIZED ON THIS OBJECT!
	private void checkNextSimulation() {
		boolean notify = false;

		if (comingQueue.size() > 0) {
			SimulationInfo info = (SimulationInfo) comingQueue.get(0);
			if (info != nextComingSimulation) {
				nextComingSimulation = info;
				notify = true;
			}
		} else if (nextComingSimulation != null) {
			nextComingSimulation = null;
			notify = true;
		}

		if (notify && viewerConnections != null) {
			byte[] data;
			synchronized (transportWriter) {
				transportWriter.clear();
				addNextSimulation(nextComingSimulation, transportWriter);
				transportWriter.finish();
				data = transportWriter.getBytes();
			}
			sendToViewers(data);
		}
	}

	private void addNextSimulation(SimulationInfo info,
			BinaryTransportWriter writer) {
		writer.node("nextSimulation");
		if (info != null) {
			if (info.hasSimulationID()) {
				writer.attr("id", info.getSimulationID());
			}
			writer.attr("startTime", info.getStartTime());
		}
		writer.endNode("nextSimulation");
	}

	public synchronized void simulationJoined(int simulationUniqID,
			int agentID, int role) {
		SimulationInfo sim = getSimulationInfo(simulationUniqID);
		if (sim != null) {
			// Must always add the agent even if the agent already is in the
			// simulation info because the simulation info object is shared
			// between the simulator server and the information system when
			// they are in the same process.
			sim.addParticipant(agentID, role);
			try {
				DBObject o = new DBObject();
				o.setInt("id", simulationUniqID);
				o.setInt("participantid", agentID);
				o.setInt("participantrole", role);
				participantTable.insert(o);
				participantTable.flush();
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not join simulation "
						+ simulationUniqID, e);
			}

			comingPage.simulationJoined(simulationUniqID, agentID);
		}
	}

	public void simulationLocked(int simulationUniqID, int simID) {
		SimulationInfo sim = getSimulationInfo(simulationUniqID);
		if (sim != null) {
			DBMatcher dbm = new DBMatcher();
			DBObject o = new DBObject();

			if (simID > lastSimulationID) {
				lastSimulationID = simID;

				// State has changed
				setStateTable("lastSimulationID", lastSimulationID, dbm, o);
				dbm.clear();
				o.clear();
			}
			// Update the simulation id in the simulation in the database
			dbm.setInt("id", simulationUniqID);
			dbm.setLimit(1);
			o.setInt("simid", simID);
			try {
				simulationTable.update(dbm, o);
				simulationTable.flush();
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not store simulation id " + simID,
						e);
			}

			sim.setSimulationID(simID);
		}

		checkCompetitionSimulationID(simulationUniqID, simID);
	}

	public void simulationStarted(int simulationUniqID, String timeUnitName,
			int timeUnitCount) {
		SimulationInfo sim = getSimulationInfo(simulationUniqID);
		if (sim != null) {
			currentSimulation = sim;
			currentTimeUnitName = timeUnitName;
			currentTimeUnitCount = timeUnitCount;

			String simType = sim.getType();
			InfoManager infoManager = infoServer.getInfoManager(simType);
			ViewerCache cache = null;
			if (infoManager != null) {
				cache = infoManager.createViewerCache(simType);
				if (cache != null) {
					this.currentViewerCache = cache;
				}
			}
			if (cache == null) {
				cache = new ViewerCache();
			}

			if (viewerConnections != null) {
				byte[] data;
				synchronized (transportWriter) {
					transportWriter.clear();
					addSimulationStarted(sim, timeUnitName, timeUnitCount,
							transportWriter);
					transportWriter.finish();
					data = transportWriter.getBytes();
				}
				sendToViewers(data);
			}
			checkCompetitionStart(simulationUniqID, sim.getSimulationID());
		}
	}

	private void addSimulationStarted(SimulationInfo info, String timeUnitName,
			int timeUnitCount, BinaryTransportWriter writer) {
		writer.node("simulationStarted").attr("id", info.getSimulationID())
				.attr("type", info.getType()).attr("startTime",
						info.getStartTime()).attr("endTime", info.getEndTime());
		if (timeUnitName != null) {
			writer.attr("timeUnitName", timeUnitName);
		}
		if (timeUnitCount > 0) {
			writer.attr("timeUnitCount", timeUnitCount);
		}
		writer.endNode("simulationStarted");
	}

	public void simulationStopped(int simulationUniqID, int simulationID,
			boolean error) {
		SimulationInfo sim = getSimulationInfo(simulationUniqID);
		if (sim != null) {
			// Time to remove the simulation from the database and simulation
			// queue
			simulationRemoved(simulationUniqID, null);

			// Remember the last played simulation for the history
			int id = sim.getSimulationID();
			if (id > lastPlayedSimulationID) {
				lastPlayedSimulationID = id;

				setStateTable("lastPlayedSimulationID", lastPlayedSimulationID,
						null, null);
			}

			if (currentSimulation == sim) {
				currentSimulation = null;
				currentTimeUnitName = null;
				currentTimeUnitCount = 0;
				currentViewerCache = null;
				currentNames = null;
			}

			checkCompetitionEnd(simulationUniqID + 1);
		}

		// Time to generate results for this simulation
		if (!error) {
			// Add the task of generating the result pages to the simulation
			// archiver (because it uses its own low priority thread for the
			// job)
			infoServer.getSimulationArchiver()
					.addSimulation(this, simulationID);
		}

		if (viewerConnections != null) {
			byte[] data;
			synchronized (transportWriter) {
				transportWriter.clear();
				transportWriter.node("simulationStopped").attr("id",
						simulationID).endNode("simulationStopped");
				transportWriter.finish();
				data = transportWriter.getBytes();
			}
			sendToViewers(data);
		}
	}

	public void sendChatMessage(long time, String message) {
		// Chat messages from server are always from admin
		if (message != null) {
			if (message.startsWith("!")) {
				// The message is a command
				String commandResult = handleChatCommand(message);
				if (commandResult != null) {
					SimConnection simc = this.simConnection;
					if (simc != null) {
						simc.addChatMessage(infoServer.getServerTimeMillis(),
								serverName, "" + '[' + serverName + ']',
								commandResult);
					}
				}

			} else {
				sendChatMessage(time, "admin", message);
			}
		}
	}

	public void nextTimeUnit(int timeUnit) {
		ViewerCache cache = this.currentViewerCache;
		if (cache != null) {
			cache.nextTimeUnit(timeUnit);
		}

		if (viewerConnections != null) {
			byte[] data;
			synchronized (transportWriter) {
				transportWriter.clear();
				transportEventWriter.nextTimeUnit(timeUnit);
				transportWriter.finish();
				data = transportWriter.getBytes();
			}
			sendToViewers(data);
		}
	}

	public void participant(int id, int role, String name, int participantID) {
		SimulationInfo info = this.currentSimulation;
		int index;
		if ((info != null)
				&& ((index = info.indexOfParticipant(participantID)) >= 0)) {
			String[] names = this.currentNames;
			int count = info.getParticipantCount();
			if (names == null) {
				names = new String[count];
			} else if (names.length < count) {
				names = (String[]) ArrayUtils.setSize(names, count);
			}
			names[index] = name;
			this.currentNames = names;
		}

		ViewerCache cache = this.currentViewerCache;
		if (cache != null) {
			cache.participant(id, role, name, participantID);
		}

		if (viewerConnections != null) {
			byte[] data;
			synchronized (transportWriter) {
				transportWriter.clear();
				transportEventWriter.participant(id, role, name, participantID);
				transportWriter.finish();
				data = transportWriter.getBytes();
			}
			sendToViewers(data);
		}
	}

	public void dataUpdated(int agent, int type, int value) {
		ViewerCache cache = this.currentViewerCache;
		if (cache != null) {
			cache.dataUpdated(agent, type, value);
		}
		if (viewerConnections != null) {
			byte[] data;
			synchronized (transportWriter) {
				transportWriter.clear();
				transportEventWriter.dataUpdated(agent, type, value);
				transportWriter.finish();
				data = transportWriter.getBytes();
			}
			sendToViewers(data);
		}
	}

	public void dataUpdated(int agent, int type, long value) {
		ViewerCache cache = this.currentViewerCache;
		if (cache != null) {
			cache.dataUpdated(agent, type, value);
		}
		if (viewerConnections != null) {
			byte[] data;
			synchronized (transportWriter) {
				transportWriter.clear();
				transportEventWriter.dataUpdated(agent, type, value);
				transportWriter.finish();
				data = transportWriter.getBytes();
			}
			sendToViewers(data);
		}
	}

	public void dataUpdated(int agent, int type, float value) {
		ViewerCache cache = this.currentViewerCache;
		if (cache != null) {
			cache.dataUpdated(agent, type, value);
		}
		if (viewerConnections != null) {
			byte[] data;
			synchronized (transportWriter) {
				transportWriter.clear();
				transportEventWriter.dataUpdated(agent, type, value);
				transportWriter.finish();
				data = transportWriter.getBytes();
			}
			sendToViewers(data);
		}
	}

	public void dataUpdated(int agent, int type, double value) {
		ViewerCache cache = this.currentViewerCache;
		if (cache != null) {
			cache.dataUpdated(agent, type, value);
		}
		if (viewerConnections != null) {
			byte[] data;
			synchronized (transportWriter) {
				transportWriter.clear();
				transportEventWriter.dataUpdated(agent, type, value);
				transportWriter.finish();
				data = transportWriter.getBytes();
			}
			sendToViewers(data);
		}
	}

	public void dataUpdated(int agent, int type, String value) {
		ViewerCache cache = this.currentViewerCache;
		if (cache != null) {
			cache.dataUpdated(agent, type, value);
		}
		if (viewerConnections != null) {
			byte[] data;
			synchronized (transportWriter) {
				transportWriter.clear();
				transportEventWriter.dataUpdated(agent, type, value);
				transportWriter.finish();
				data = transportWriter.getBytes();
			}
			sendToViewers(data);
		}
	}

	public void dataUpdated(int agent, int type, Transportable value) {
		ViewerCache cache = this.currentViewerCache;
		if (cache != null) {
			cache.dataUpdated(agent, type, value);
		}
		if (viewerConnections != null) {
			byte[] data;
			synchronized (transportWriter) {
				transportWriter.clear();
				transportEventWriter.dataUpdated(agent, type, value);
				transportWriter.finish();
				data = transportWriter.getBytes();
			}
			sendToViewers(data);
		}
	}

	public void dataUpdated(int type, Transportable value) {
		ViewerCache cache = this.currentViewerCache;
		if (cache != null) {
			cache.dataUpdated(type, value);
		}
		if (viewerConnections != null) {
			byte[] data;
			synchronized (transportWriter) {
				transportWriter.clear();
				transportEventWriter.dataUpdated(type, value);
				transportWriter.finish();
				data = transportWriter.getBytes();
			}
			sendToViewers(data);
		}
	}

	public void interaction(int fromAgent, int toAgent, int type) {
		ViewerCache cache = this.currentViewerCache;
		if (cache != null) {
			cache.interaction(fromAgent, toAgent, type);
		}

		if (viewerConnections != null) {
			byte[] data;
			synchronized (transportWriter) {
				transportWriter.clear();
				transportEventWriter.interaction(fromAgent, toAgent, type);
				transportWriter.finish();
				data = transportWriter.getBytes();
			}
			sendToViewers(data);
		}
	}

	public void interactionWithRole(int fromAgent, int role, int type) {
		ViewerCache cache = this.currentViewerCache;
		if (cache != null) {
			cache.interactionWithRole(fromAgent, role, type);
		}

		if (viewerConnections != null) {
			byte[] data;
			synchronized (transportWriter) {
				transportWriter.clear();
				transportEventWriter.interactionWithRole(fromAgent, role, type);
				transportWriter.finish();
				data = transportWriter.getBytes();
			}
			sendToViewers(data);
		}
	}

	// -------------------------------------------------------------------
	// Chat handling
	// -------------------------------------------------------------------

	// Note: may only be called from the constructor
	private void restoreChatCache(String filename) {
		try {
			RandomAccessFile fp = new RandomAccessFile(filename, "r");
			try {
				long length = fp.length();
				if (length > 0) {
					String line;
					long seek = length - MAX_CHAT_CACHE_RESTORE_SIZE;
					if (seek > 0) {
						fp.seek(seek);
					}
					// Ignore the first line that might be half
					if ((seek <= 0) || (fp.readLine() != null)) {
						// Add all lines from this point forward
						while ((line = fp.readLine()) != null) {
							int index = (chatCacheIndex + chatCacheNumber)
									% CHAT_CACHE_SIZE;
							// Parse the log entry
							int i0 = line.indexOf(',');
							int i1 = line.indexOf(',', i0 + 1);
							int i2 = line.indexOf(',', i1 + 1);
							long time = Long.parseLong(line.substring(0, i0));
							String serverName = line.substring(i0 + 1, i1);
							String userName = line.substring(i1 + 1, i2);
							String message = line.substring(i2 + 1);

							if (chatCache[index] == null) {
								chatCache[index] = new ChatMessage(time,
										serverName, userName, message);
							} else {
								chatCache[index].setMessage(time, serverName,
										userName, message);
							}
							if (chatCacheNumber < CHAT_CACHE_SIZE) {
								chatCacheNumber++;
							} else {
								chatCacheIndex = (chatCacheIndex + 1)
										% CHAT_CACHE_SIZE;
							}
						}
					}
				}
			} finally {
				fp.close();
			}
		} catch (FileNotFoundException e) {
			// No chat log file to read from
		} catch (Exception e) {
			log.log(Level.WARNING, "could not restore chat messages from "
					+ filename, e);
		}
	}

	private ChatMessage[] getChatMessages() {
		synchronized (transportWriter) {
			if (chatCacheNumber == 0) {
				return null;
			}
			ChatMessage[] messages = new ChatMessage[chatCacheNumber];
			for (int i = 0; i < chatCacheNumber; i++) {
				messages[i] = chatCache[(chatCacheIndex + i) % CHAT_CACHE_SIZE];
			}
			return messages;
		}
	}

	// -------------------------------------------------------------------
	// Viewer Channel handling
	// -------------------------------------------------------------------

	public void addViewerConnection(BlockingViewerChannel connection) {
		try {
			synchronized (transportWriter) {
				viewerConnections = (BlockingViewerChannel[]) ArrayUtils.add(
						BlockingViewerChannel.class, viewerConnections,
						connection);
				// Must initialize the connection
				byte[] buffer = transportWriter.getInitBytes();
				if (buffer != null) {
					connection.write(buffer);
				}
				transportWriter.clear();
				transportWriter.node("serverTime").attr("time",
						infoServer.getServerTimeMillis()).endNode("serverTime");
				// The server time must be sent as soon as possible after
				// generation
				transportWriter.finish();
				connection.write(transportWriter.getBytes());

				transportWriter.clear();
				addNextSimulation(nextComingSimulation, transportWriter);
				// Add any chat history
				for (int i = 0; i < chatCacheNumber; i++) {
					chatCache[(chatCacheIndex + i) % CHAT_CACHE_SIZE]
							.writeMessage(transportWriter);
				}
				transportWriter.finish();
				connection.write(transportWriter.getBytes());

				// Send data about any running simulation
				String currentTimeUnitName = this.currentTimeUnitName;
				int currentTimeUnitCount = this.currentTimeUnitCount;
				SimulationInfo currentSimulation = this.currentSimulation;
				if (currentSimulation != null) {
					ViewerCache currentViewerCache = this.currentViewerCache;
					transportWriter.clear();
					addSimulationStarted(currentSimulation,
							currentTimeUnitName, currentTimeUnitCount,
							transportWriter);
					if (currentViewerCache != null) {
						currentViewerCache.writeCache(transportEventWriter);
					}
					transportWriter.finish();
					connection.write(transportWriter.getBytes());
				}
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "could not send init to "
					+ connection.getName(), e);
			connection.close();
		}
	}

	public void removeViewerConnection(BlockingViewerChannel connection) {
		synchronized (transportWriter) {
			viewerConnections = (BlockingViewerChannel[]) ArrayUtils.remove(
					viewerConnections, connection);
		}
	}

	public void viewerDataReceived(BlockingViewerChannel connection,
			BinaryTransportReader reader) {
		try {
			while (reader.nextNode(false)) {
				if (reader.isNode("chat")) {
					String message = reader.getAttribute("message");
					if (message.startsWith("!")) {
						// This is a command
						String commandResult = handleChatCommand(message);
						if (commandResult != null) {
							byte[] data;
							synchronized (transportWriter) {
								transportWriter.clear();
								transportWriter.node("chat").attr("time",
										infoServer.getServerTimeMillis()).attr(
										"server", serverName).attr("user",
										"" + '[' + serverName + ']').attr(
										"message", commandResult).endNode(
										"chat");
								transportWriter.finish();
								data = transportWriter.getBytes();
							}
							connection.write(data);
						}

					} else {
						sendChatMessage(infoServer.getServerTimeMillis(),
								connection.getUserName(), reader
										.getAttribute("message"));
					}
				}
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not parse message from viewer "
					+ connection.getName(), e);
		}
	}

	private String handleChatCommand(String command) {
		if (command.equals("!who")) {
			// List the connected viewers
			StringBuffer sb = new StringBuffer();
			BlockingViewerChannel[] viewers = this.viewerConnections;
			if (viewers == null) {
				sb.append("No viewers connected");
			} else {
				for (int i = 0, n = viewers.length; i < n; i++) {
					if (i > 0) {
						sb.append(", ");
					}
					sb.append(viewers[i].getUserName());
				}
			}
			return sb.toString();

		} else if (command.equals("!ip")) {
			// List the connected viewers and their remote hosts
			StringBuffer sb = new StringBuffer();
			BlockingViewerChannel[] viewers = this.viewerConnections;
			if (viewers == null) {
				sb.append("No viewers connected");
			} else {
				for (int i = 0, n = viewers.length; i < n; i++) {
					if (i > 0) {
						sb.append(", ");
					}
					sb.append(viewers[i].getUserName()).append(" (").append(
							viewers[i].getRemoteHost()).append(')');
				}
			}
			return sb.toString();
		}
		return null;
	}

	private void sendChatMessage(long time, String userName, String message) {
		String chatMessage = "" + time + ',' + serverName + ',' + userName
				+ ',' + message;
		log.info("CHAT: " + chatMessage);
		if (chatlog != null) {
			chatlog.println(chatMessage);
		}

		byte[] data;
		synchronized (transportWriter) {
			int index = (chatCacheIndex + chatCacheNumber) % CHAT_CACHE_SIZE;
			ChatMessage chat = chatCache[index];
			if (chat == null) {
				chat = chatCache[index] = new ChatMessage(time, serverName,
						userName, message);
			} else {
				chat.setMessage(time, serverName, userName, message);
			}
			if (chatCacheNumber < CHAT_CACHE_SIZE) {
				chatCacheNumber++;
			} else {
				chatCacheIndex = (chatCacheIndex + 1) % CHAT_CACHE_SIZE;
			}

			if (viewerConnections != null) {
				transportWriter.clear();
				chat.writeMessage(transportWriter);
				transportWriter.finish();
				data = transportWriter.getBytes();
			} else {
				data = null;
			}
		}

		if (data != null) {
			sendToViewers(data);
		}

		SimConnection simc = this.simConnection;
		if (simc != null) {
			simc.addChatMessage(time, serverName, userName, message);
		}
	}

	private void sendToViewers(byte[] data) {
		BlockingViewerChannel[] channels = this.viewerConnections;
		if (channels != null) {
			for (int i = 0, n = channels.length; i < n; i++) {
				try {
					// log.severe("sending to " + channels[i].getName() + " "
					// + writer.size() + " bytes");
					channels[i].write(data);
				} catch (Exception e) {
					log.log(Level.WARNING, "could not send to "
							+ channels[i].getName(), e);
					channels[i].close();
				}
			}
		}
	}

	// -------------------------------------------------------------------
	// Competition handling
	// -------------------------------------------------------------------

	private void setupCompetitionTable(Database database) {
		competitionParticipantTable = database.getTable("competitionparts");
		if (competitionParticipantTable == null) {
			competitionParticipantTable = database
					.createTable("competitionparts");
			competitionParticipantTable.createField("competition",
					DBField.INTEGER, 32, 0);
			competitionParticipantTable.createField("participantid",
					DBField.INTEGER, 32, 0);
			competitionParticipantTable.createField("flags", DBField.INTEGER,
					32, 0);
			competitionParticipantTable.createField("score", DBField.DOUBLE, 64,
					0);
			competitionParticipantTable.createField("wscore", DBField.DOUBLE,
					64, 0);
			competitionParticipantTable.createField("gamesplayed",
					DBField.INTEGER, 32, 0);
			competitionParticipantTable.createField("zgamesplayed",
					DBField.INTEGER, 32, 0);
			competitionParticipantTable.createField("wgamesplayed",
					DBField.DOUBLE, 64, 0);
			competitionParticipantTable.createField("zwgamesplayed",
					DBField.DOUBLE, 64, 0);

			competitionParticipantTable.createField("avgsc1", DBField.DOUBLE,
					64, 0);
			competitionParticipantTable.createField("avgsc2", DBField.DOUBLE,
					64, 0);
			competitionParticipantTable.createField("avgsc3", DBField.DOUBLE,
					64, 0);
			competitionParticipantTable.createField("avgsc4", DBField.DOUBLE,
					64, 0);
			competitionParticipantTable.flush();
		}
		competitionTable = database.getTable("competitions");
		if (competitionTable == null) {
			competitionTable = database.createTable("competitions");
			competitionTable.createField("id", DBField.INTEGER, 32,
					DBField.UNIQUE | DBField.PRIMARY | DBField.INDEX);
			competitionTable.createField("parent", DBField.INTEGER, 32, 0,
					new Integer(0));
			competitionTable.createField("name", DBField.STRING, 80, 0);
			competitionTable.createField("flags", DBField.INTEGER, 32, 0);
			competitionTable.createField("starttime", DBField.LONG, 64, 0);
			competitionTable.createField("endtime", DBField.LONG, 64, 0);
			competitionTable.createField("startuniqid", DBField.INTEGER, 32, 0,
					new Integer(-1));
			competitionTable.createField("startsimid", DBField.INTEGER, 32, 0,
					new Integer(-1));
			competitionTable.createField("simulations", DBField.INTEGER, 32, 0);
			competitionTable.createField("startweight", DBField.DOUBLE, 64, 0);
			competitionTable.createField("scoreclass", DBField.STRING, 80,
					DBField.MAY_BE_NULL);
			competitionTable.flush();
		} else {
			// Check that the parent field exists for backward compability
			if (!competitionTable.hasField("parent")) {
				competitionTable.createField("parent", DBField.INTEGER, 32, 0,
						new Integer(0));
			}

			loadCompetitions(false);
		}

		competitionResultTable = database.getTable("competitionresults");
		if (competitionResultTable == null) {
			competitionResultTable = database.createTable("competitionresults");
			competitionResultTable.createField("id", DBField.INTEGER, 32, 0);
			competitionResultTable.createField("simid", DBField.INTEGER, 32, 0);
			competitionResultTable.createField("competition", DBField.INTEGER,
					32, 0);
			competitionResultTable.createField("participantid",
					DBField.INTEGER, 32, 0);
			competitionResultTable.createField("participantrole",
					DBField.INTEGER, 32, 0);
			competitionResultTable.createField("flags", DBField.INTEGER, 32, 0);
			competitionResultTable.createField("score", DBField.DOUBLE, 64, 0);
			competitionResultTable.createField("weight", DBField.DOUBLE, 64, 0);
			competitionResultTable.flush();
		}
	}

	// Note: MAY ONLY BE CALLED SYNCHRONIZED OR FROM CONSTRUCTOR!!!
	private void loadCompetitions(boolean checkAlreadyLoaded) {
		boolean hasCompetitionChain = false;
		DBMatcher dbm = new DBMatcher();
		DBResult res = competitionTable.select(dbm);

		while (res.next()) {
			int id = res.getInt("id");
			// Only load competitions newer than the last finished competition
			if (id <= lastFinishedCompetitionID) {
				continue;
			}
			if (checkAlreadyLoaded
					&& Competition.indexOf(competitions, id) >= 0) {
				// Competition already loaded
				continue;
			}
			int parentID = res.getInt("parent");
			String name = res.getString("name");
			int flags = res.getInt("flags");
			long startTime = res.getLong("starttime");
			long endTime = res.getLong("endtime");
			int startUniqueID = res.getInt("startuniqid");
			int startPublicID = res.getInt("startsimid");
			int simulationCount = res.getInt("simulations");
			double startWeight = res.getDouble("startweight");
			String scoreClass = res.getString("scoreclass");
			Competition competition = new Competition(id, name, startTime,
					endTime, startUniqueID, simulationCount, startWeight);
			if (startPublicID >= 0) {
				competition.setStartSimulationID(startPublicID);
			}
			if (scoreClass != null) {
				competition.setScoreClassName(scoreClass);
			}
			competition.setFlags(flags);

			if (parentID > 0) {
				// Competition is chained (a continuation of another
				// competition)
				competition.setParentCompetitionID(parentID);
				hasCompetitionChain = true;
			}

			DBMatcher dbm2 = new DBMatcher();
			dbm2.setInt("competition", id);

			DBResult res2 = competitionParticipantTable.select(dbm2);
			while (res2.next()) {
				int pid = res2.getInt("participantid");
				String uname = infoServer.getUserName(pid);
				CompetitionParticipant cp = new CompetitionParticipant(pid,
						uname == null ? "unknown" : uname);
				cp.setFlags(res2.getInt("flags"));
				cp.setScores(res2.getDouble("score"), res2.getDouble("wscore"),
						res2.getInt("gamesplayed"),
						res2.getInt("zgamesplayed"), res2
								.getDouble("wgamesplayed"), res2
								.getDouble("zwgamesplayed"));
				cp.setAvgScores(res2.getDouble("avgsc1"), res2
						.getDouble("avgsc2"), res2.getDouble("avgsc3"), res2
						.getDouble("avgsc4"));
				competition.addParticipant(cp);
			}
			res2.close();

			addCompetition(competition, false);
		}
		res.close();

		if (hasCompetitionChain) {
			Competition[] competitions = getCompetitions();
			if (competitions != null) {
				for (int i = 0, n = competitions.length; i < n; i++) {
					Competition comp = competitions[i];
					if (comp.hasParentCompetition()) {
						int parentID = comp.getParentCompetitionID();
						Competition parentCompetition = getCompetitionByID(parentID);
						if (parentCompetition == null) {
							log.log(Level.SEVERE,
									"could not find parent competition "
											+ parentID + " for competition "
											+ comp.getName(),
									new IllegalStateException(
											"competition not found"));
						} else if (parentCompetition.isParentCompetition(comp)) {
							log.log(Level.SEVERE,
									"circular dependencies for competition "
											+ comp.getName(),
									new IllegalStateException(
											"circular competition chain"));
						} else {
							comp.setParentCompetition(parentCompetition);
						}
					}
				}
			}
		}
	}

	// NOTE: MAY ONLY BE CALLED SYNCHRONIZED OR FROM CONSTRUCTOR
	private void addCompetition(Competition competition, boolean addToDatabase) {
		competitions = (Competition[]) ArrayUtils.add(Competition.class,
				competitions, competition);

		if (addToDatabase) {
			try {
				DBObject object = new DBObject();
				object.setInt("id", competition.getID());
				if (competition.hasParentCompetition()) {
					object.setInt("parent", competition
							.getParentCompetitionID());
				}
				object.setString("name", competition.getName());
				object.setInt("flags", competition.getFlags());
				object.setLong("starttime", competition.getStartTime());
				object.setLong("endtime", competition.getEndTime());
				object.setInt("startuniqid", competition.getStartUniqueID());
				object.setInt("startsimid", competition.getStartSimulationID());
				object.setInt("simulations", competition.getSimulationCount());
				object.setDouble("startweight", competition.getStartWeight());
				String scoreClass = competition.getScoreClassName();
				if (scoreClass != null) {
					object.setString("scoreclass", scoreClass);
				}
				competitionTable.insert(object);
				competitionTable.flush();

				// Add any participants to the competitionParticipantTable
				CompetitionParticipant[] participants = competition
						.getParticipants();
				if (participants != null) {
					object.clear();
					object.setInt("competition", competition.getID());
					for (int i = 0, n = participants.length; i < n; i++) {
						CompetitionParticipant cp = participants[i];
						object.setInt("participantid", cp.getID());
						competitionParticipantTable.insert(object);
					}
					competitionParticipantTable.flush();
				}

			} catch (Exception e) {
				log.log(Level.SEVERE, "could not add competition '"
						+ competition.getName() + "' to database", e);
			}
		}

		long endTime = competition.getEndTime();
		long currentTime = infoServer.getServerTimeMillis();
		if (endTime <= currentTime) {
			// This competition has already ended and will not be added to
			// the coming competition list
		} else {
			long startTime = competition.getStartTime();
			int index = comingCompetitions.size() - 1;
			for (; index >= 0; index--) {
				Competition comp = (Competition) comingCompetitions.get(index);
				if (comp.getStartTime() < startTime) {
					// Insert the new competition after this competition
					break;
				}
			}
			index++;
			comingCompetitions.add(index, competition);

			if (index == 0) {
				// The competitions was added first i.e. is the
				// competition with the lowest start time
				nextCompetition = competition;

				if (startTime < currentTime) {
					// This competition has already started
					currentCompetition = competition;
				}
			}
		}
	}

	// Note: MAY ONLY BE CALLED SYNCHRONIZED
	private void removeCompetition(int competitionID, boolean removeFromDatabase) {
		for (int i = 0, n = comingCompetitions.size(); i < n; i++) {
			Competition comp = (Competition) comingCompetitions.get(i);
			if (comp.getID() == competitionID) {
				// Remove this competition
				comingCompetitions.remove(i);
				if (i == 0) {
					nextCompetition = n > 1 ? (Competition) comingCompetitions
							.get(0) : null;
				}

				if (currentCompetition == comp) {
					currentCompetition = null;
				}
				break;
			}
		}

		int index = Competition.indexOf(competitions, competitionID);
		if (index >= 0) {
			competitions = (Competition[]) ArrayUtils.remove(competitions,
					index);
		}
		if (removeFromDatabase) {
			try {
				DBMatcher dbm = new DBMatcher();
				dbm.setInt("id", competitionID);
				dbm.setLimit(1);
				competitionTable.remove(dbm);
				competitionTable.flush();

				// Should remove the competition participants also. FIX THIS!!!
				// TODO
				// dbm.clear();
				// dbm.setInt("competition", competitionID);

				// competitionParticipantTable.remove(dbm);
				// competitionParticipantTable.flush();

				// Should remove the competition results also.
				// competitionResultTable.remove(dbm);
				// competitionResultTable.flush();

			} catch (Exception e) {
				log.log(Level.SEVERE, "could not remove competition '"
						+ competitionID + "' from database", e);
			}
		}
	}

	private void checkCompetitionStart(int simulationUniqID, int simID) {
		checkCompetitionEnd(simulationUniqID);
		checkCompetitionSimulationID(simulationUniqID, simID);
		if (currentCompetition == null) {
			Competition nextCompetition = this.nextCompetition;
			if (nextCompetition != null
					&& nextCompetition.isSimulationIncluded(simulationUniqID)) {
				this.currentCompetition = nextCompetition;

				// Must make sure all simulations in the competition has
				// been locked to avoid potential problems
				lockCompetitionSimulations(nextCompetition, simID);
			}
		}
	}

	private synchronized void checkCompetitionEnd(int simulationUniqID) {
		if (currentCompetition != null
				&& !currentCompetition.isSimulationIncluded(simulationUniqID)) {
			if (comingCompetitions.size() > 0
					&& comingCompetitions.get(0) == currentCompetition) {
				comingCompetitions.remove(0);
			}

			this.currentCompetition = null;
			this.nextCompetition = comingCompetitions.size() > 0 ? (Competition) comingCompetitions
					.get(0)
					: null;
		}
	}

	private void checkCompetitionSimulationID(int simulationUniqID, int simID) {
		Competition competition = this.nextCompetition;
		if (competition != null && !competition.hasSimulationID()
				&& competition.isSimulationIncluded(simulationUniqID)) {
			int startID = competition.getStartUniqueID();
			int startSimID = simID + startID - simulationUniqID;
			competition.setStartSimulationID(startSimID);

			// Update competition database
			try {
				DBMatcher dbm = new DBMatcher();
				DBObject object = new DBObject();
				dbm.setInt("id", competition.getID());
				dbm.setLimit(1);
				object.setInt("startsimid", startSimID);

				competitionTable.update(dbm, object);
				competitionTable.flush();

			} catch (Exception e) {
				log.log(Level.SEVERE, "could not set simulation id " + simID
						+ " in competition '" + competition.getName()
						+ "' in database", e);
			}

			// Must make sure all simulations in the competition has
			// been locked to avoid potential problems
			lockCompetitionSimulations(competition, simID);
		}
	}

	private void lockCompetitionSimulations(Competition competition, int simID) {
		if (competition.hasSimulationID()) {
			int numSimulations = competition.getSimulationCount()
					- (simID - competition.getStartSimulationID());
			if (numSimulations > 0) {
				log.finer("requesting lock of " + numSimulations
						+ " simulations due to start of competition "
						+ competition.getName());
				SimConnection connection = this.simConnection;
				if (connection != null) {
					connection.lockNextSimulations(numSimulations);
				}
			}
		}
	}

	// Called by coming game page
	public Competition getCurrentCompetition() {
		return currentCompetition;
	}

	// Called by coming game page
	public Competition getNextCompetition() {
		return nextCompetition;
	}

	public Competition getCompetitionBySimulation(int simID) {
		Competition[] comps = this.competitions;
		if (comps != null) {
			for (int i = 0, n = comps.length; i < n; i++) {
				if (comps[i].isSimulationIncluded(simID)) {
					return comps[i];
				}
			}
		}
		return null;
	}

	public Competition getCompetitionByID(int competitionID) {
		Competition[] competitions = getCompetitions();
		int index = competitions == null ? -1 : Competition.indexOf(
				competitions, competitionID);
		return index < 0 ? null : competitions[index];
	}

	public Competition[] getCompetitions() {
		return competitions;
	}

	public void setCompetitionInfo(int competitionID, String newName,
			String scoreGenerator) {
		Competition competition = getCompetitionByID(competitionID);
		if (competition == null) {
			throw new IllegalArgumentException("competition " + competitionID
					+ " not found");
		}

		if (scoreGenerator != null) {
			// Test if score generator is valid
			try {
				ScoreGenerator generator = (ScoreGenerator) Class.forName(
						scoreGenerator).newInstance();
			} catch (ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				throw (IllegalArgumentException) new IllegalArgumentException(
						"could not create score generator " + "of type '"
								+ scoreGenerator + '\'').initCause(e);
			}
		}

		competition.setName(newName);
		competition.setScoreClassName(scoreGenerator);

		try {
			DBMatcher dbm = new DBMatcher();
			dbm.setInt("id", competition.getID());
			dbm.setLimit(1);

			DBObject object = new DBObject();
			object.setString("name", newName);
			object.setString("scoreclass", scoreGenerator);

			competitionTable.update(dbm, object);
			competitionTable.flush();

		} catch (Exception e) {
			log.log(Level.SEVERE,
					"could not update info for " + "competition '"
							+ competition.getName() + "' in database", e);
		}
	}

	// Called by the admin page
	public void scheduleCompetition(CompetitionSchedule schedule) {
		SimConnection connection = this.simConnection;
		if (connection == null) {
			throw new IllegalStateException("no connection with aa server");
		}
		synchronized (this) {
			int competitionID = lastCompetitionID + 1;
			schedule.setID(competitionID);
			schedule.validate();

			Competition competition = new Competition(competitionID, schedule
					.getName());
			competition.setStartWeight(schedule.getStartWeight());
			competition.setFlags(schedule.getFlags());
			competition.setScoreClassName(schedule.getScoreClassName());

			int[] participants = schedule.getParticipants();
			int parentID = schedule.getParentCompetitionID();
			if (parentID > 0) {
				Competition parentCompetition = getCompetitionByID(parentID);
				if (parentCompetition == null) {
					throw new IllegalArgumentException("parent competition "
							+ parentID + " not found");
				}
				if (parentCompetition.isParentCompetition(competition)) {
					throw new IllegalStateException(
							"circular competition chain");
				}
				if (parentCompetition.getStartTime() >= schedule.getStartTime()) {
					throw new IllegalStateException(
							"parent competition must start "
									+ "before child competition");
				}

				competition.setParentCompetitionID(parentCompetition.getID());
				competition.setParentCompetition(parentCompetition);
			}

			for (int i = 0, n = participants.length; i < n; i++) {
				String userName = infoServer.getUserName(participants[i]);
				if (userName == null) {
					throw new IllegalArgumentException("participant "
							+ participants[i] + " not found");
				}

				CompetitionParticipant cp = new CompetitionParticipant(
						participants[i], userName);
				competition.addParticipant(cp);
			}

			pendingCompetitions = (Competition[]) ArrayUtils.add(
					Competition.class, pendingCompetitions, competition);
			setStateTable("lastCompetitionID", ++lastCompetitionID, null, null);
		}
		connection.scheduleCompetition(schedule);
	}

	// -------------------------------------------------------------------
	// API towards the admin page
	// -------------------------------------------------------------------

	public void addTimeReservation(long startTime, int lengthInMillis) {
		SimConnection connection = this.simConnection;
		if (connection == null) {
			throw new IllegalStateException("no connection with aa server");
		}
		connection.addTimeReservation(startTime, lengthInMillis);
	}

	public synchronized void removeCompetition(int competitionID) {
		// Check to avoid removing parent competitions
		Competition[] competitions = getCompetitions();
		if (competitions != null) {
			for (int i = 0, n = competitions.length; i < n; i++) {
				if (competitions[i].getParentCompetitionID() == competitionID) {
					throw new IllegalArgumentException("can not remove parent "
							+ "competition " + competitionID
							+ " of child competition "
							+ competitions[i].getName());
				}
			}
		}
		removeCompetition(competitionID, true);
	}

	/**
	 * Scratches a simulation if not already scratched.
	 * 
	 * @param simulationID
	 *            the simulation public id
	 * @throws IllegalArgumentException
	 *             if something was wrong
	 */
	public synchronized void scratchSimulation(int simulationID) {
		if (simulationID <= 0) {
			throw new IllegalArgumentException("illegal simulation id: "
					+ simulationID);
		}
		DBMatcher dbm = new DBMatcher();
		dbm.setInt("simid", simulationID);
		dbm.setLimit(1);
		DBResult res = playedTable.select(dbm);
		if (!res.next()) {
			res.close();
			throw new IllegalArgumentException("simulation " + simulationID
					+ " not found");
		}

		int uniqueSimulationID = res.getInt("id");
		int flags = res.getInt("flags");
		res.close();

		if ((flags & SIMULATION_SCRATCHED) != 0) {
			// Simulation was earlier scratched.
			throw new IllegalArgumentException("simulation already scratched!");
		}

		// Mark the simulation as scratched
		DBObject object = new DBObject();
		object.setInt("flags", flags | SIMULATION_SCRATCHED);
		playedTable.update(dbm, object);
		playedTable.flush();
		log.info("scratching simulation " + simulationID
				+ ": flag set to scratched");

		// Find any competition results
		int[] crParticipantIDs = new int[10];
		int[] crFlags = new int[10];
		double[] crScore = new double[10];
		double[] crWeight = new double[10];
		int competitionID = -1;
		int count = 0;
		dbm.clear();
		dbm.setInt("id", uniqueSimulationID);
		res = competitionResultTable.select(dbm);
		while (res.next()) {
			if (count == crParticipantIDs.length) {
				crParticipantIDs = ArrayUtils.setSize(crParticipantIDs,
						count + 10);
				crFlags = ArrayUtils.setSize(crFlags, count + 10);
				crScore = ArrayUtils.setSize(crScore, count + 10);
				crWeight = ArrayUtils.setSize(crWeight, count + 10);
			}
			// This competition id will be the same for all results
			// since it is only one simulation
			if (competitionID < 0) {
				competitionID = res.getInt("competition");
			}
			crParticipantIDs[count] = res.getInt("participantid");
			crFlags[count] = res.getInt("flags");
			crScore[count] = res.getDouble("score");
			crWeight[count] = res.getDouble("weight");
			count++;
		}
		res.close();

		// Mark all competition results as scratched
		dbm.clear();
		dbm.setInt("id", uniqueSimulationID);
		dbm.setLimit(1);
		object.clear();
		for (int i = 0; i < count; i++) {
			dbm.setInt("participantid", crParticipantIDs[i]);
			object.setInt("flags", crFlags[i] | SIMULATION_SCRATCHED);
			competitionResultTable.update(dbm, object);
			log.info("scratching simulation " + simulationID
					+ ": flag set to scratched in competition result for "
					+ "participant " + crParticipantIDs[i]);
		}
		competitionResultTable.flush();

		// Update all competition scores
		Competition competition = getCompetitionBySimulation(uniqueSimulationID);
		if (competition != null) {
			dbm.clear();
			dbm.setInt("competition", competition.getID());
			dbm.setLimit(1);
			object.clear();
			for (int i = 0; i < count; i++) {
				CompetitionParticipant cp = competition
						.getParticipantByID(crParticipantIDs[i]);
				if (cp != null) {
					// SHOULD ALSO UPDATE AVG SCORES 1 - 4!!! FIX THIS!!! TODO
					// Perhaps use the simulation type to retrieve a
					// ResultManager and use it to update the avg scores!
					cp.removeScore(simulationID, crScore[i], crWeight[i],
							(crScore[i] == 0)
									|| ((crFlags[i] & ZERO_GAME) != 0));

					dbm.setInt("participantid", crParticipantIDs[i]);
					object.setDouble("score", cp.getTotalScore());
					object.setDouble("wscore", cp.getTotalWeightedScore());
					object.setInt("gamesplayed", cp.getGamesPlayed());
					object.setInt("zgamesplayed", cp.getZeroGamesPlayed());
					object.setDouble("wgamesplayed", cp
							.getWeightedGamesPlayed());
					object.setDouble("zwgamesplayed", cp
							.getZeroWeightedGamesPlayed());
					if (competitionParticipantTable.update(dbm, object) == 0) {
						log.severe("scratching simulation " + simulationID
								+ ": failed to update scores for "
								+ cp.getName() + " in competition "
								+ competition.getName());
					} else {
						log.info("scratching simulation " + simulationID
								+ ": updated score for " + cp.getName()
								+ " in competition " + competition.getName());
					}
				} else { // cp == null
					log.severe("scratching simulation " + simulationID
							+ ": failed to update scores for "
							+ crParticipantIDs[i] + " in competition "
							+ competition.getName()
							+ " (participants not loaded?)");
				}
			}
			competitionParticipantTable.flush();

		} else if (competitionID >= 0) {
			// Competition not loaded => must update database
			dbm.clear();
			dbm.setInt("competition", competitionID);
			dbm.setLimit(1);
			for (int i = 0; i < count; i++) {
				dbm.setInt("participantid", crParticipantIDs[i]);
				res = competitionParticipantTable.select(dbm);
				if (res.next()) {
					object.clear();
					object.setDouble("score", res.getDouble("score") - crScore[i]);
					object.setDouble("wscore", res.getDouble("wscore")
							- crScore[i] * crWeight[i]);
					object.setInt("gamesplayed", res.getInt("gamesplayed") - 1);
					if ((crScore[i] == 0L) || ((crFlags[i] & ZERO_GAME) != 0)) {
						object.setInt("zgamesplayed", res
								.getInt("zgamesplayed") - 1);
					}
					object.setDouble("wgamesplayed", res
							.getDouble("wgamesplayed")
							- crWeight[i]);
					if ((crScore[i] == 0L) || ((crFlags[i] & ZERO_GAME) != 0)) {
						object.setDouble("zwgamesplayed", res
								.getDouble("zwgamesplayed")
								- crWeight[i]);
					}
					if (competitionParticipantTable.update(dbm, object) > 0) {
						log.info("scratching simulation " + simulationID
								+ ": updated score for " + crParticipantIDs[i]
								+ " in competition " + competitionID);
					} else {
						log
								.severe("scratching simulation " + simulationID
										+ ": failed to update scores for "
										+ crParticipantIDs[i]
										+ " in non-loaded competition "
										+ competitionID);
					}
				}
				res.close();
			}
			competitionParticipantTable.flush();
		} else {
			log.info("scratching simulation " + simulationID
					+ ": no competition for simulation");
		}
	}

	// -------------------------------------------------------------------
	// API towards the game page
	// -------------------------------------------------------------------

	public String getUserName(SimulationInfo info, int userID) {
		// Note: info may be NULL
		if (userID < 0) {
			String[] names = this.currentNames;
			int index;
			if ((names != null) && (info == this.currentSimulation)
					&& (info != null)
					&& ((index = info.indexOfParticipant(userID)) >= 0)
					&& (index < names.length) && (names[index] != null)) {
				return names[index];
			} else {
				return "dummy" + (userID + 1);
			}
		} else {
			return infoServer.getUserName(userID);
		}
	}

	public synchronized SimulationInfo[] getComingSimulations() {
		if (comingQueue.size() == 0) {
			return null;
		}

		SimulationInfo[] infos = this.comingCache;
		if (infos == null) {
			infos = this.comingCache = (SimulationInfo[]) comingQueue
					.toArray(new SimulationInfo[comingQueue.size()]);
		}
		return infos;
	}

	public String[] getSimulationTypes() {
		return null;
	}

	public String getSimulationTypeName(String type) {
		return type;
	}

	public int getLastPlayedSimulationID() {
		return lastPlayedSimulationID;
	}

	public int getLastFinishedCompetitionID() {
		return lastFinishedCompetitionID;
	}

	public void setLastFinishedCompetitionID(int competitionID) {
		if (this.lastFinishedCompetitionID != competitionID) {
			int oldID = this.lastFinishedCompetitionID;
			this.lastFinishedCompetitionID = competitionID;
			setStateTable("lastFinishedCompetitionID", competitionID, null,
					null);

			if (oldID > competitionID) {
				// Need to reload the competitions
				synchronized (this) {
					loadCompetitions(true);
				}

			} else {
				// Might need to remove some competitions
				synchronized (this) {
					Competition[] competitions = getCompetitions();
					if (competitions != null) {
						for (int i = 0, n = competitions.length; i < n; i++) {
							if (competitions[i].getID() <= this.lastFinishedCompetitionID) {
								removeCompetition(competitions[i].getID(),
										false);
							}
						}
					}
				}
			}
		}
	}

	public void createSimulation(String type, String params) {
		SimConnection connection = this.simConnection;
		if (connection != null) {
			connection.createSimulation(type, params);
		}
	}

	public void removeSimulation(int uniqueSimID) {
		SimConnection connection = this.simConnection;
		if (connection == null) {
			throw new IllegalStateException("no connection with aa server");
		}
		synchronized (this) {
			// Check that the simulation is not part of a competition
			for (int i = 0, n = comingCompetitions.size(); i < n; i++) {
				Competition competition = (Competition) comingCompetitions
						.get(i);
				if (competition.isSimulationIncluded(uniqueSimID)) {
					throw new IllegalStateException(
							"can not remove simulation " + uniqueSimID
									+ " (part of competition "
									+ competition.getName() + ')');
				}
			}
		}
		connection.removeSimulation(uniqueSimID);
	}

	public void joinSimulation(int uniqueSimID, int agentID, String role) {
		SimConnection connection = this.simConnection;
		if (connection != null) {
			connection.joinSimulation(uniqueSimID, agentID, role);
		}
	}

	public String getServerMessage() {
		return serverMessage;
	}

	public void setServerMessage(String serverMessage) {
		if (serverMessage == null) {
			if (this.serverMessage != null) {
				this.serverMessage = null;
				new File(serverMessageFile).delete();
			}

		} else if (!serverMessage.equals(this.serverMessage)) {
			this.serverMessage = serverMessage;
			saveFile(serverMessageFile, serverMessage);

			infoServer.serverMessageChanged(this);
		}
	}

	public boolean isWebJoinActive() {
		return true;
	}

	public int getMaxAgentScheduled() {
		return maxAgentScheduled;
	}

	public void setMaxAgentScheduled(int max) {
		this.maxAgentScheduled = max;
	}

	// -------------------------------------------------------------------
	// Simulation Result handling
	// -------------------------------------------------------------------

	// Called by the SimulationArchiver when the results should be
	// generated
	public void generateResults(int simulationID, boolean addToTable) {
		generateResults(simulationID, addToTable, false);
	}

	public void generateResults(int simulationID, boolean addToTable,
			boolean regenerateResults) {
		String path = resultsPath + simulationID + File.separatorChar;
		String filename = path + GAME_LOG_NAME;

		LogReader reader = null;
		try {
			File destPath = new File(path);
			if (!destPath.exists() && !destPath.mkdirs()) {
				throw new IOException(
						"could not create simulation result directory '" + path
								+ "' for simulation " + simulationID);
			}

			// Copy the simulation log from the server
			if (!regenerateResults) {
				archiveSimulationLog(simulationID, filename);
			}

			reader = new LogReader(getSimulationLogStream(filename));
			if (reader.getSimulationID() != simulationID) {
				throw new IOException("log file " + filename
						+ " did not contain log for simulation " + simulationID);
			}

			String simType = reader.getSimulationType();
			InfoManager infoManager = infoServer.getInfoManager(simType);
			if (infoManager == null) {
				throw new IOException("could not find information manager for "
						+ "simulation " + simulationID + " of type " + simType);
			}
			ResultManager resultManager = infoManager
					.createResultManager(simType);
			resultManager.init(this, addToTable, GAME_LOG_NAME);

			if (playedTable != null) {
				try {
					DBObject object = new DBObject();
					object.setInt("id", reader.getUniqueID());
					object.setInt("simid", simulationID);
					object.setString("type", simType);
					object.setLong("starttime", reader.getStartTime());
					object
							.setInt("length",
									reader.getSimulationLength() / 1000);
					// MLB - 20080404 - SQLite doesn't appear to like null /
					// empty
					// fields, so we simply set flags to 0. We don't know what
					// this
					// field means, so 0 seems like a good choice.
					object.setInt("flags", 0);

					playedTable.insert(object);
					playedTable.flush();
				} catch (Exception e) {
					log.log(Level.SEVERE,
							"could not store results for simulation "
									+ simulationID, e);
				}
			}

			resultManager.generateResult(reader, path);
			reader.close();

			if (!regenerateResults) {
				simConnection.resultsGenerated(simulationID);
			}

			// Create score page
			Competition competition = getCompetitionBySimulation(reader
					.getUniqueID());
			if (competition != null) {
				// Make sure the competition has its simulation id set
				if (!competition.hasSimulationID()) {
					checkCompetitionSimulationID(reader.getUniqueID(), reader
							.getSimulationID());
				}

				generateCompetitionResults(competition, reader, urlGamePath);
			}

		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "could not find log for simulation "
					+ simulationID, e);
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not generate results for simulation "
					+ simulationID, e);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private void generateCompetitionResults(Competition competition,
			LogReader reader, String urlGamePath) {
		try {
			String path = resultsPath + "competition" + File.separatorChar
					+ competition.getID() + File.separatorChar;
			File destPath = new File(path);
			if (!destPath.exists() && !destPath.mkdirs()) {
				throw new IOException(
						"could not create competition result directory '"
								+ path + "' for competition "
								+ competition.getName() + " ("
								+ competition.getID() + ')');
			}

			ParticipantInfo[] participants = reader.getParticipants();
			if (participants != null) {
				for (int i = 0, n = participants.length; i < n; i++) {
					ParticipantInfo info = participants[i];
					CompetitionParticipant cp = competition
							.getParticipantByID(info.getUserID());
					if (cp != null) {
						StatPageGenerator.generateStatisticsPage(
								competitionResultTable, path, urlGamePath,
								competition, cp, true);
					}
				}
			}

			ScoreGenerator gen = null;
			String scoreClassName = competition.getScoreClassName();
			if (scoreClassName != null) {
				try {
					gen = (ScoreGenerator) Class.forName(scoreClassName)
							.newInstance();
				} catch (Throwable t) {
					log.log(Level.SEVERE,
							"could not create score generator of type "
									+ scoreClassName, t);
				}
			}

			if (gen == null) {
				// String simulationType = reader.getSimulationType();
				// if (simulationType != null) {
				// InfoManager infoManager =
				// infoServer.getInfoManager(simulationType);
				// if (infoManager != null) {
				// gen = infoManager.createScoreGenerator(simulationType);
				// }
				// }
				// if (gen == null) {
				gen = new DefaultScoreGenerator();
				// }
			}
			gen.init(getServerName(), path);
			gen.createScoreTable(competition, reader.getSimulationID());

		} catch (ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			log.log(Level.SEVERE, "could not generate score page", e);
		}
	}

	/**
	 * Regenerates the scores and statistics for the specified competition
	 * 
	 * @param competitionID
	 *            the competition id
	 * @throws IllegalArgumentException
	 *             if something went wrong
	 * @throws IOException
	 *             if something went wrong
	 */
	public void generateCompetitionResults(int competitionID)
			throws IOException {
		Competition competition = getCompetitionByID(competitionID);
		if (competition == null) {
			throw new IllegalArgumentException("competition " + competitionID
					+ " not found in memory");
		}
		String path = resultsPath + "competition" + File.separatorChar
				+ competition.getID() + File.separatorChar;
		File destPath = new File(path);
		if (!destPath.exists() && !destPath.mkdirs()) {
			throw new IOException(
					"could not create competition result directory '" + path
							+ "' for competition " + competition.getName()
							+ " (" + competition.getID() + ')');
		}

		for (int i = 0, n = competition.getParticipantCount(); i < n; i++) {
			CompetitionParticipant cp = competition.getParticipant(i);
			StatPageGenerator.generateStatisticsPage(competitionResultTable,
					path, urlGamePath, competition, cp, true);
		}

		ScoreGenerator gen = null;
		String scoreClassName = competition.getScoreClassName();
		if (scoreClassName != null) {
			try {
				gen = (ScoreGenerator) Class.forName(scoreClassName)
						.newInstance();
			} catch (Throwable t) {
				log.log(Level.SEVERE,
						"could not create score generator of type "
								+ scoreClassName, t);
			}
		}
		if (gen == null) {
			gen = new DefaultScoreGenerator();
		}
		gen.init(getServerName(), path);
		gen.createScoreTable(competition, lastPlayedSimulationID);
	}

	private InputStream getServerLogStream(int simulationID) throws IOException {
		// Simple hack for result generation!!!! FIX THIS!!! TODO
		return new FileInputStream("sim" + simulationID + ".slg");
		// End of simple hack for result generation!!!! FIX THIS!!! TODO
	}

	private InputStream getSimulationLogStream(String filename)
			throws IOException {
		boolean gzip = filename.endsWith(".gz");
		try {
			InputStream input = new FileInputStream(filename);
			return gzip ? new GZIPInputStream(input) : input;
		} catch (FileNotFoundException e) {
			return gzip ? (InputStream) new FileInputStream(filename.substring(
					0, filename.length() - 3)) : new GZIPInputStream(
					new FileInputStream(filename + ".gz"));
		}
	}

	private void archiveSimulationLog(int simulationID, String targetFile)
			throws IOException {
		InputStream input = getServerLogStream(simulationID);
		try {
			OutputStream output = new GZIPOutputStream(new FileOutputStream(
					targetFile));
			try {
				byte[] buffer = new byte[4096];
				int n;
				while ((n = input.read(buffer)) > 0) {
					output.write(buffer, 0, n);
				}
			} finally {
				output.close();
			}
		} finally {
			input.close();
		}
	}

	final void addSimulationToHistory(LogReader logReader,
			ParticipantInfo[] participants, String[] participantColors) {
		int simulationID = logReader.getSimulationID();
		try {
			String filename = resultsPath + simTablePrefix
					+ (((simulationID - 1) / simulationsPerPage) + 1) + ".html";
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new FileWriter(filename, true)));
			log.finest("adding simulation " + simulationID + " to " + filename);
			try {
				long startTime = logReader.getStartTime();
				int length = logReader.getSimulationLength() / 1000;
				int minutes = length / 60;
				int seconds = length % 60;
				out.print("<tr><td>&nbsp;<a href=\""
						+ simulationID
						+ "/\">"
						+ simulationID
						+ "</a>&nbsp;</td><td>"
						+ infoServer.getServerTimeAsString(startTime)
						+ " ("
						+ minutes
						+ "&nbsp;min"
						+ (seconds > 0 ? ("&nbsp;" + seconds + "&nbsp;sec")
								: "") + ")</td><td>");
				// if (logReader.isScratched()) {
				// // Simulation was scratched
				// out.print("<font color=red>Simulation was scratched!</font>");

				// } else if (!logReader.isFinished()) {
				// out.print("<font color=red>Simulation was never finished!</font>");

				// } else {
				if (participants != null) {
					String currentColor = null;
					boolean isEm = false;
					for (int i = 0, n = participants.length; i < n; i++) {
						ParticipantInfo info = participants[i];
						if (i > 0) {
							out.print(' ');
						}
						if (participantColors != null
								&& currentColor != participantColors[i]) {
							if (isEm) {
								out.print("</em>");
								isEm = false;
							}
							currentColor = setHtmlColor(out, currentColor,
									participantColors[i]);
						}
						if (isEm != info.isBuiltinAgent()) {
							out.print(isEm ? "</em>" : "<em>");
							isEm = !isEm;
						}
						out.print(info.getName());
					}
					if (isEm) {
						out.print("</em>");
					}
					setHtmlColor(out, currentColor, null);
				}
				out.print(" (<a href=\"" + simulationID + '/' + GAME_LOG_NAME
						+ "\">data</a>)");
				// }
				out.println("</td></tr>");
			} finally {
				out.close();
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not add simulation " + simulationID
					+ " to simulation table", e);
		}
	}

	private String setHtmlColor(PrintWriter out, String currentColor,
			String newColor) {
		if (currentColor == newColor) {
			return currentColor;
		}
		if (currentColor != null) {
			out.print("</font>");
		}
		if (newColor != null) {
			out.print("<font color='" + newColor + "'>");
		}
		return newColor;
	}

	final void addSimulationResult(LogReader logReader,
			ParticipantInfo[] participants, long[] scores, boolean update) {
		if (participants == null) {
			return;
		}

		int simulationUniqID = logReader.getUniqueID();
		DBObject object = null;
		if (resultTable != null) {
			try {
				object = new DBObject();
				object.setInt("id", simulationUniqID);
				for (int i = 0, n = participants.length; i < n; i++) {
					ParticipantInfo info = participants[i];
					if (!info.isBuiltinAgent()) {
						object.setInt("participantid", info.getUserID());
						object.setInt("participantrole", info.getRole());
						object.setDouble("score", scores[i]);
						resultTable.insert(object);
					}
				}
				resultTable.flush();
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not store results for simulation "
						+ logReader.getSimulationID(), e);
			}
		}

		Competition competition;
		if (competitionResultTable != null
				&& ((competition = getCompetitionBySimulation(simulationUniqID)) != null)) {
			try {
				boolean lowestScoreForZero = (competition.getFlags() & Competition.LOWEST_SCORE_FOR_ZERO) != 0;
				double weight = competition.getWeight(simulationUniqID);
				long lowestScore = 0L;
				if (object == null) {
					object = new DBObject();
				} else {
					object.clear();
				}
				if (lowestScoreForZero) {
					for (int i = 0, n = scores.length; i < n; i++) {
						if (scores[i] < lowestScore) {
							lowestScore = scores[i];
						}
					}
				}

				if (!update) {
					DBMatcher dbm = new DBMatcher();
					dbm.setInt("competition", competition.getID());
					for (int i = 0, n = participants.length; i < n; i++) {
						ParticipantInfo info = participants[i];
						CompetitionParticipant cp;
						if (!info.isBuiltinAgent()
								&& ((cp = competition.getParticipantByID(info
										.getUserID())) != null)) {
							boolean isZeroGame = scores[i] == 0L;
							dbm.setInt("participantid", info.getUserID());
							cp
									.addScore(
											logReader.getSimulationID(),
											(lowestScoreForZero && isZeroGame) ? lowestScore
													: scores[i], weight,
											isZeroGame);

							object.setDouble("score", cp.getTotalScore());
							object.setDouble("wscore", cp
									.getTotalWeightedScore());
							object.setInt("gamesplayed", cp.getGamesPlayed());
							object.setInt("zgamesplayed", cp
									.getZeroGamesPlayed());
							object.setDouble("wgamesplayed", cp
									.getWeightedGamesPlayed());
							object.setDouble("zwgamesplayed", cp
									.getZeroWeightedGamesPlayed());
							if (competitionParticipantTable.update(dbm, object) == 0) {
								log
										.severe("failed to update scores for simulation "
												+ logReader.getSimulationID()
												+ " in competition "
												+ competition.getName()
												+ " for " + cp.getName());
							}
						}
					}
					competitionParticipantTable.flush();
					object.clear();
				}

				object.setInt("id", simulationUniqID);
				object.setInt("simid", logReader.getSimulationID());
				object.setInt("competition", competition.getID());
				for (int i = 0, n = participants.length; i < n; i++) {
					ParticipantInfo info = participants[i];
					if (!info.isBuiltinAgent()) {
						boolean isZeroGame = scores[i] == 0L;
						object.setInt("participantid", info.getUserID());
						object.setInt("participantrole", info.getRole());
						object
								.setDouble(
										"score",
										(lowestScoreForZero && isZeroGame) ? lowestScore
												: scores[i]);
						object.setDouble("weight", weight);
						object.setInt("flags", isZeroGame ? ZERO_GAME : 0);
						competitionResultTable.insert(object);
					}
				}
				competitionResultTable.flush();
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not store results for simulation "
						+ logReader.getSimulationID(), e);
			}
		}
	}

	final void addSimulationResult(LogReader logReader,
			ParticipantInfo[] participants, double[] scores, boolean update) {
		if (participants == null) {
			return;
		}

		int simulationUniqID = logReader.getUniqueID();
		DBObject object = null;
		if (resultTable != null) {
			try {
				object = new DBObject();
				object.setInt("id", simulationUniqID);
				for (int i = 0, n = participants.length; i < n; i++) {
					ParticipantInfo info = participants[i];
					if (!info.isBuiltinAgent()) {
						object.setInt("participantid", info.getUserID());
						object.setInt("participantrole", info.getRole());
						object.setDouble("score", scores[i]);
						resultTable.insert(object);
					}
				}
				resultTable.flush();
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not store results for simulation "
						+ logReader.getSimulationID(), e);
			}
		}

		Competition competition;
		if (competitionResultTable != null
				&& ((competition = getCompetitionBySimulation(simulationUniqID)) != null)) {
			try {
				boolean lowestScoreForZero = (competition.getFlags() & Competition.LOWEST_SCORE_FOR_ZERO) != 0;
				double weight = competition.getWeight(simulationUniqID);
				double lowestScore = 0.0;
				if (object == null) {
					object = new DBObject();
				} else {
					object.clear();
				}
				if (lowestScoreForZero) {
					for (int i = 0, n = scores.length; i < n; i++) {
						if (scores[i] < lowestScore) {
							lowestScore = scores[i];
						}
					}
				}

				if (!update) {
					DBMatcher dbm = new DBMatcher();
					dbm.setInt("competition", competition.getID());
					for (int i = 0, n = participants.length; i < n; i++) {
						ParticipantInfo info = participants[i];
						CompetitionParticipant cp;
						if (!info.isBuiltinAgent()
								&& ((cp = competition.getParticipantByID(info
										.getUserID())) != null)) {
							boolean isZeroGame = scores[i] == 0.0;
							dbm.setInt("participantid", info.getUserID());
							cp
									.addScore(
											logReader.getSimulationID(),
											(lowestScoreForZero && isZeroGame) ? lowestScore
													: scores[i], weight,
											isZeroGame);

							object.setDouble("score", cp.getTotalScore());
							object.setDouble("wscore", cp
									.getTotalWeightedScore());
							object.setInt("gamesplayed", cp.getGamesPlayed());
							object.setInt("zgamesplayed", cp
									.getZeroGamesPlayed());
							object.setDouble("wgamesplayed", cp
									.getWeightedGamesPlayed());
							object.setDouble("zwgamesplayed", cp
									.getZeroWeightedGamesPlayed());
							if (competitionParticipantTable.update(dbm, object) == 0) {
								log
										.severe("failed to update scores for simulation "
												+ logReader.getSimulationID()
												+ " in competition "
												+ competition.getName()
												+ " for " + cp.getName());
							}
						}
					}
					competitionParticipantTable.flush();
					object.clear();
				}

				object.setInt("id", simulationUniqID);
				object.setInt("simid", logReader.getSimulationID());
				object.setInt("competition", competition.getID());
				for (int i = 0, n = participants.length; i < n; i++) {
					ParticipantInfo info = participants[i];
					if (!info.isBuiltinAgent()) {
						boolean isZeroGame = scores[i] == 0L;
						object.setInt("participantid", info.getUserID());
						object.setInt("participantrole", info.getRole());
						object
								.setDouble(
										"score",
										(lowestScoreForZero && isZeroGame) ? lowestScore
												: scores[i]);
						object.setDouble("weight", weight);
						object.setInt("flags", isZeroGame ? ZERO_GAME : 0);
						competitionResultTable.insert(object);
					}
				}
				competitionResultTable.flush();
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not store results for simulation "
						+ logReader.getSimulationID(), e);
			}
		}
	}

	// -------------------------------------------------------------------
	// State table handling
	// -------------------------------------------------------------------

	private void setupStateTable(Database database) {
		stateTable = database.getTable("state");
		if (stateTable == null) {
			stateTable = database.createTable("state");
			stateTable.createField("name", DBField.STRING, 32, DBField.UNIQUE
					| DBField.INDEX | DBField.PRIMARY);
			stateTable.createField("value", DBField.INTEGER, 32, 0);

			// Insert empty fields for easier updating
			DBObject o = new DBObject();
			o.setString("name", "lastSimulationID");
			o.setInt("value", lastSimulationID);
			stateTable.insert(o);
			o.setString("name", "lastUniqueSimulationID");
			o.setInt("value", lastUniqueSimulationID);
			stateTable.insert(o);
			o.setString("name", "lastPlayedSimulationID");
			o.setInt("value", lastPlayedSimulationID);
			stateTable.insert(o);
			o.setString("name", "lastCompetitionID");
			o.setInt("value", lastCompetitionID);
			stateTable.insert(o);
			o.setString("name", "lastFinishedCompetitionID");
			o.setInt("value", lastFinishedCompetitionID);
			stateTable.insert(o);
			stateTable.flush();
		} else {
			DBMatcher dbm = new DBMatcher();
			dbm.setString("name", "lastSimulationID");
			DBResult res = stateTable.select(dbm);
			if (res.next()) {
				lastSimulationID = res.getInt("value");
			}
			res.close();

			dbm.setString("name", "lastUniqueSimulationID");
			res = stateTable.select(dbm);
			if (res.next()) {
				lastUniqueSimulationID = res.getInt("value");
			}
			res.close();

			dbm.setString("name", "lastPlayedSimulationID");
			res = stateTable.select(dbm);
			if (res.next()) {
				lastPlayedSimulationID = res.getInt("value");
			}
			res.close();

			dbm.setString("name", "lastCompetitionID");
			res = stateTable.select(dbm);
			if (res.next()) {
				lastCompetitionID = res.getInt("value");
			}
			res.close();

			dbm.setString("name", "lastFinishedCompetitionID");
			res = stateTable.select(dbm);
			if (res.next()) {
				lastFinishedCompetitionID = res.getInt("value");
			}
			res.close();
		}
	}

	private void setStateTable(String name, int value, DBMatcher dbm,
			DBObject object) {
		try {
			if (dbm == null) {
				dbm = new DBMatcher();
			}
			if (object == null) {
				object = new DBObject();
			}
			dbm.setString("name", name);
			dbm.setLimit(1);
			object.setInt("value", value);
			int updateCount = stateTable.update(dbm, object);
			if (updateCount == 0) {
				// Nothing was updated. Perhaps this attribute has not yet been
				// added
				object.setString("name", name);
				stateTable.insert(object);
			}
			stateTable.flush();
		} catch (Exception e) {
			log
					.log(Level.SEVERE, "could not set '" + name + "' to "
							+ value, e);
		}
	}

	// -------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------

	public static int indexOf(SimServer[] array, String serverName) {
		if (array != null) {
			for (int i = 0, n = array.length; i < n; i++) {
				if (serverName.equals(array[i].serverName)) {
					return i;
				}
			}
		}
		return -1;
	}

} // SimServer
