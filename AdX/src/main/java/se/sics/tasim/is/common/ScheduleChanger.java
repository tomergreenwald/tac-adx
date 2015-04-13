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
 * ScheduleChanger
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Jun 27 14:42:30 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.botbox.util.ArrayQueue;
import se.sics.isl.db.DBMatcher;
import se.sics.isl.db.DBResult;
import se.sics.isl.db.DBTable;
import se.sics.isl.db.Database;
import se.sics.isl.util.ArgumentManager;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.LogFormatter;
import se.sics.tasim.is.AgentLookup;
import se.sics.tasim.is.SimulationInfo;

/**
 * ScheduleChanger is an utility to add or remove an agent in some games during
 * a competition by replacing with other agents.
 * <p>
 * 
 * <pre>
 * # CONFIG FILE FOR TAC qualifying
 * competition.config=tac3_simulator.conf
 * competition.id=1
 * # Add agent
 * agentToAdd=AgentName
 * firstGame=3198
 * gamesToSchedule=23
 * # Remove agent
 * agentToRemove=AgentName
 * firstGame=3198
 * is.database.sql.url=jdbc:mysql://localhost:3306/mysql
 * log.consoleLevel=0
 * </pre>
 */
public class ScheduleChanger {

	private final static String DEFAULT_CONFIG = "schedule.conf";
	private final static String CONF = "is.";

	private static final Logger log = Logger.getLogger(ScheduleChanger.class
			.getName());

	public ScheduleChanger() {
	}

	// -------------------------------------------------------------------
	// Main
	// -------------------------------------------------------------------

	public static void main(String[] args) throws IOException {
		long scheduleStartTime = System.currentTimeMillis();

		ArgumentManager config = new ArgumentManager("ScheduleChanger", args);
		config.addOption("config", "configfile", "set the config file to use");
		config.addOption("is.database.sql.url",
				"jdbc:mysql://localhost:3306/mysql", "set the database url");
		config.addOption("log.consoleLevel", "level",
				"set the console log level");
		config.addOption("n",
				"Do not change any files or access any databases.");
		config.addHelp("h", "show this help message");
		config.addHelp("help");
		config.validateArguments();

		String configFile = config.getArgument("config", DEFAULT_CONFIG);
		try {
			config.loadConfiguration(configFile);
			config.removeArgument("config");
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			config.usage(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		String agentToAdd = config.getProperty("agentToAdd");
		String agentToRemove = config.getProperty("agentToRemove");
		String theAgentName;
		if (agentToAdd != null) {
			if (agentToRemove != null) {
				throw new IllegalArgumentException(
						"both agent to add and agent to remove specified");
			}
			theAgentName = agentToAdd;
		} else if (agentToRemove == null) {
			throw new IllegalArgumentException("no agent specified");
		} else {
			theAgentName = agentToRemove;
		}

		int firstGame = config.getPropertyAsInt("firstGame", 0);
		int gamesToSchedule = config.getPropertyAsInt("gamesToSchedule", -1);
		if (gamesToSchedule <= 0 && agentToAdd != null) {
			throw new IllegalArgumentException("no games to schedule specified");
		}

		String databaseURL = config.getProperty("is.database.sql.url");
		boolean check = config.getPropertyAsBoolean("n", false);

		// No more need for argument handling. Lets free the memory
		config.finishArguments();

		setLogging(config);

		// Competition competition = new Competition(0, "Dummy Competition");
		ScheduleChanger scoreGen = new ScheduleChanger();
		Random random = new Random();

		String serverConfigFileName = config.getProperty("competition.config");
		int competitionID = config.getPropertyAsInt("competition.id", 0);
		if (serverConfigFileName == null || competitionID <= 0) {
			throw new IllegalArgumentException("no server config or "
					+ "no competition id");
		}
		log.info("using server config '" + serverConfigFileName
				+ "' for competition " + competitionID);

		ConfigManager serverConfig = new ConfigManager();
		serverConfig.loadConfiguration(serverConfigFileName);

		if (databaseURL != null) {
			serverConfig.setProperty("is.database.sql.url", databaseURL);
			serverConfig.setProperty("is.user.database.sql.url", databaseURL);
		}

		String serverName = serverConfig.getProperty("server.name");
		if (serverName == null) {
			throw new IllegalStateException("no server name for "
					+ serverConfigFileName);
		}

		Database userDatabase = DatabaseUtils.createUserDatabase(serverConfig,
				CONF, null);
		DBTable userTable = userDatabase.getTable("users");
		if (userTable == null) {
			userDatabase.close();
			throw new IllegalStateException("could not find user database for "
					+ " competition " + competitionID);
		}

		CompetitionParticipant theAgent;
		{
			DBMatcher dbm2 = new DBMatcher();
			dbm2.setString("name", theAgentName);
			dbm2.setLimit(1);
			DBResult user = userTable.select(dbm2);
			if (user.next()) {
				int pid = user.getInt("id");
				log.finer("Using user " + theAgentName + " as " + pid);
				theAgent = new CompetitionParticipant(pid, theAgentName);
			} else {
				log.severe("Could not find user '" + theAgentName
						+ "' in user db");
				return;
			}
			user.close();
		}

		// Should also be different "configs..."
		Database database = DatabaseUtils.createDatabase(serverConfig, CONF);
		log.finer("DB Name: " + database.getName() + " class: " + database);
		Database serverDatabase = DatabaseUtils.createChildDatabase(
				serverConfig, CONF, serverName, database);
		log.finer("Server DB Name: " + serverDatabase.getName() + " class: "
				+ serverDatabase);

		Competition competition = loadCompetitionByParticipants(serverDatabase,
				userTable, competitionID);

		userDatabase.close();

		// The competition object is not "final" time to generate the stuff!

		log.info("Competition has " + competition.getParticipantCount()
				+ " participants");
		log.info("Competition starts at " + competition.getStartSimulationID()
				+ " and ends at " + competition.getEndSimulationID());
		// log.info("Competition is written to "
		// + scoreGen.getScoreFileName());

		// scoreGen.listTopAgents(competition, false);

		if (firstGame < competition.getStartSimulationID()) {
			firstGame = competition.getStartSimulationID();
		}

		int endGame = competition.getEndSimulationID();
		DBTable participantTable = serverDatabase
				.getTable("comingparticipants");
		String participantTableName = participantTable.getName();
		DBTable simulationTable = serverDatabase.getTable("comingsimulations");

		DBMatcher dbm = new DBMatcher();
		DBMatcher dbm2 = new DBMatcher();
		DBResult res = simulationTable.select(dbm);
		ArrayList list = new ArrayList();
		while (res.next()) {
			int id = res.getInt("id");
			int simID = res.getInt("simid");
			if (simID < 0 || simID < firstGame || simID > endGame) {
				continue;
			}
			String type = res.getString("type");
			String params = res.getString("params");
			long startTime = res.getLong("starttime");
			int length = res.getInt("length") * 1000;
			// int flags = hasFlags ? res.getInt("flags") : 0;
			SimulationInfo info = new SimulationInfo(id, type, params, length);
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
			// info.setFlags(flags);
			list.add(info);
		}
		res.close();

		// if (list.size() > 0) {
		// for (int i = 0, n = list.size(); i < n; i++) {
		// log.info("GAME: " + list.get(i));
		// }
		// }
		log.info("found " + list.size() + " games");
		serverDatabase.close();
		database.close();

		if (agentToAdd != null) {
			// Add an agent by replacing other agents in games
			Hashtable pTable = new Hashtable();
			while (gamesToSchedule > 0) {
				if (list.size() == 0) {
					log.log(Level.SEVERE,
							"COULD NOT COMPLETELY CHANGE SCHEDULE!!!");
					return;
				}
				boolean done = false;
				int index = random.nextInt(list.size());
				SimulationInfo info = (SimulationInfo) list.get(index);
				int plen = info.getParticipantCount();
				int[] p = new int[plen];
				for (int j = 0, m = plen; j < m; j++) {
					p[j] = info.getParticipantID(j);
				}

				while (plen > 0) {
					int pindex = random.nextInt(plen);
					Integer part = new Integer(p[pindex]);
					if (pTable.get(part) == null) {
						System.out.println("# "
								+ info.getID()
								+ " ("
								+ info.getSimulationID()
								+ ')'
								+ " REPL "
								+ p[pindex]
								+ " with "
								+ theAgent.getID()
								+ "  ("
								+ competition.getParticipantByID(p[pindex])
										.getName() + " with " + theAgentName
								+ ')');
						System.out.println("UPDATE " + participantTableName
								+ " SET " + "participantid='"
								+ theAgent.getID() + "' WHERE id='"
								+ info.getID() + "' AND participantid='"
								+ p[pindex] + "' LIMIT 1;");
						pTable.put(part, part);
						done = true;
						break;
					}
					plen--;
					p[pindex] = p[plen];
					p[plen] = -1;
				}

				list.remove(index);

				if (done) {
					gamesToSchedule--;
				}
			}
		} else {
			// Remove an agent by replacing it with other competitors
			Hashtable ignoreTable = new Hashtable();
			String[] ignoreAgents = config.getPropertyAsArray("ignoreAgents");
			if (ignoreAgents != null) {
				for (int i = 0, n = ignoreAgents.length; i < n; i++) {
					ignoreTable.put(ignoreAgents[i], ignoreAgents[i]);
				}
			}
			{
				int i = 1;
				String ia;
				while ((ia = config.getProperty("ignoreAgent." + i)) != null) {
					ignoreTable.put(ia, ia);
					i++;
				}
			}

			ignoreTable.put(theAgentName, theAgentName);

			// Randomize the participants
			CompetitionParticipant[] participants = competition
					.getParticipants();
			if (participants == null || participants.length < 2) {
				log
						.severe("no participants or too few participants in competition");
				return;
			}
			AgentChooser chooser = new AgentChooser(participants, ignoreTable,
					random);

			for (int i = 0, n = list.size(); i < n; i++) {
				SimulationInfo game = (SimulationInfo) list.get(i);
				if (game.isParticipant(theAgent.getID())) {
					// Must replace the agent in this game
					CompetitionParticipant replacer = chooser
							.getNextParticipant(game);
					if (replacer == null) {
						log
								.severe("could not find a agent to replace removed agent "
										+ "in game " + game.getSimulationID());
						return;
					} else {
						System.out.println("# " + game.getID() + " ("
								+ game.getSimulationID() + ')' + " REPL "
								+ theAgent.getID() + " with "
								+ replacer.getID() + "  (" + theAgentName
								+ " with " + replacer.getName() + ')');
						System.out.println("UPDATE " + participantTableName
								+ " SET " + "participantid='"
								+ replacer.getID() + "' WHERE id='"
								+ game.getID() + "' AND participantid='"
								+ theAgent.getID() + "' LIMIT 1;");
					}
				}
			}

			System.out.println();
			System.out.println();
			System.out.println("Agents to ignore next time:");
			StringBuffer sb = null;
			for (int i = 0, cnt = 0, n = chooser.participants.length; i < n; i++) {
				if (chooser.participants[i].count > 0) {
					if (chooser.participants[i].part.getName().indexOf(' ') >= 0) {
						cnt++;
						System.out.println("ignoreAgent." + cnt + "="
								+ chooser.participants[i].part.getName());
					} else {
						if (sb == null) {
							sb = new StringBuffer();
						} else {
							sb.append(',');
						}
						sb.append(chooser.participants[i].part.getName());
					}
				}
			}
			if (sb != null) {
				System.out.println("ignoreAgents=" + sb);
			}

			System.out.println();
			System.out.println("Agent counts (debug output)");
			for (int i = 0, n = chooser.participants.length; i < n; i++) {
				System.out.println("AGENT "
						+ chooser.participants[i].part.getName() + ": "
						+ chooser.participants[i].count);
			}
		}

		log.info("Schedule change generated in "
				+ (System.currentTimeMillis() - scheduleStartTime) + " msek");
	}

	private static void setLogging(ConfigManager config) {
		int consoleLevel = config.getPropertyAsInt("log.consoleLevel", 0);
		Level logLevel = LogFormatter.getLogLevel(consoleLevel);
		boolean showThreads = config.getPropertyAsBoolean("log.threads", false);

		Logger root = Logger.getLogger("");
		root.setLevel(logLevel);

		LogFormatter formatter = new LogFormatter();
		formatter.setAliasLevel(2);
		formatter.setShowingThreads(showThreads);
		LogFormatter.setConsoleLevel(logLevel);
		LogFormatter.setFormatterForAllHandlers(formatter);
	}

	// -------------------------------------------------------------------
	// Competition support
	// -------------------------------------------------------------------

	private static Competition loadCompetitionByParticipants(
			Database serverDatabase, DBTable userTable, int competitionID)
			throws IOException {
		AgentLookup agentLookup = new AgentLookup();
		DBTable competitionTable = serverDatabase.getTable("competitions");
		DBTable competitionParticipantTable = serverDatabase
				.getTable("competitionparts");
		Competition currentCompetition = loadCompetition(userTable,
				agentLookup, competitionTable, competitionParticipantTable,
				competitionID);
		if (currentCompetition == null) {
			IOException ioe = new IOException("competition not found");
			log.log(Level.SEVERE,
					"could not find competition " + competitionID, ioe);
			throw ioe;
		}

		log.finer("loaded competition " + currentCompetition.getName());

		// addScores(competition, currentCompetition, true);
		return currentCompetition;
		// while (currentCompetition.hasParentCompetition()) {
		// int parentID = currentCompetition.getParentCompetitionID();
		// currentCompetition =
		// loadCompetition(userTable, agentLookup,
		// competitionTable,
		// competitionParticipantTable,
		// parentID);
		// if (currentCompetition == null) {
		// IOException ioe = new IOException("competition not found");
		// log.log(Level.SEVERE, "could not find competition " + parentID, ioe);
		// throw ioe;
		// }
		// log.finer("loaded parent competition " +
		// currentCompetition.getName());
		// addScores(competition, currentCompetition, false);
		// }
	}

	private static void addScores(Competition targetComp, Competition source,
			boolean createParticipants) {
		CompetitionParticipant[] participants = source.getParticipants();
		if (participants == null) {
			log.warning("no participants found in competition "
					+ source.getName());
		} else {
			for (int j = 0, m = participants.length; j < m; j++) {
				CompetitionParticipant cp = participants[j];
				CompetitionParticipant targetParticipant = targetComp
						.getParticipantByID(cp.getID());
				if (targetParticipant != null) {
					targetParticipant.addScore(cp);
				} else if (createParticipants) {
					targetParticipant = new CompetitionParticipant(cp.getID(),
							cp.getName());
					targetComp.addParticipant(targetParticipant);
					targetParticipant.addScore(cp);
				} else {
					log.finer("ignoring parent participant " + cp.getName());
				}
			}
		}
	}

	private static Competition loadCompetition(DBTable userTable,
			AgentLookup agentLookup, DBTable competitionTable,
			DBTable competitionParticipantTable, int competitionID) {
		Competition theCompetition = null;
		DBMatcher dbm = new DBMatcher();
		dbm.setLimit(1);
		dbm.setInt("id", competitionID);
		DBResult res = competitionTable.select(dbm);

		if (res.next()) {
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
			Competition competition = new Competition(competitionID, name,
					startTime, endTime, startUniqueID, simulationCount,
					startWeight);
			if (startPublicID >= 0) {
				competition.setStartSimulationID(startPublicID);
			}
			if (scoreClass != null) {
				competition.setScoreClassName(scoreClass);
			}
			competition.setFlags(flags);

			// if (parentID > 0) {
			// // Competition is chained (a continuation of another competition)
			// competition.setParentCompetitionID(parentID);
			// }

			DBMatcher dbm2 = new DBMatcher();
			dbm2.setInt("competition", competitionID);

			DBResult res2 = competitionParticipantTable.select(dbm2);
			while (res2.next()) {
				int pid = res2.getInt("participantid");
				String uname = agentLookup.getAgentName(pid);
				if (uname == null) {
					DBMatcher userDbm = new DBMatcher();
					userDbm.setInt("id", pid);
					userDbm.setLimit(1);
					DBResult user = userTable.select(userDbm);
					if (user.next()) {
						int userID = user.getInt("id");
						int parentUserID = user.getInt("parent");
						uname = user.getString("name");
						String password = user.getString("password");
						if (uname != null) {
							log.finer("Adding user " + uname + " with id "
									+ userID);
							agentLookup.setUser(uname, password, userID,
									parentUserID);
						}
					}
					user.close();
				}
				if (uname == null) {
					log.warning("could not find user " + pid);
					uname = "unknown";
				}
				CompetitionParticipant cp = new CompetitionParticipant(pid,
						uname);
				cp.setFlags(res2.getInt("flags"));
				// cp.setScores(res2.getLong("score"),
				// res2.getDouble("wscore"),
				// res2.getInt("gamesplayed"),
				// res2.getInt("zgamesplayed"),
				// res2.getDouble("wgamesplayed"),
				// res2.getDouble("zwgamesplayed"));
				// cp.setAvgScores(res2.getDouble("avgsc1"),
				// res2.getDouble("avgsc2"),
				// res2.getDouble("avgsc3"),
				// res2.getDouble("avgsc4"));
				competition.addParticipant(cp);
			}
			res2.close();
			theCompetition = competition;
		}
		res.close();

		return theCompetition;
	}

	private static class AgentChooser {

		public AgentInfo[] participants;

		private ArrayQueue priority = new ArrayQueue();
		private ArrayQueue queue = new ArrayQueue();

		public AgentChooser(CompetitionParticipant[] p,
				Hashtable agentsToIgnore, Random random) {
			this.participants = new AgentInfo[p.length];
			for (int i = 0, n = participants.length; i < n; i++) {
				this.participants[i] = new AgentInfo(p[i]);
			}

			for (int i = 0, n = participants.length - 1; i < n; i++) {
				int index = i + random.nextInt(n + 1 - i);
				AgentInfo cp = participants[i];
				participants[i] = participants[index];
				participants[index] = cp;
			}

			for (int i = 0, n = participants.length; i < n; i++) {
				if (agentsToIgnore.get(participants[i].part.getName()) == null) {
					queue.add(participants[i]);
				}
			}
		}

		public CompetitionParticipant getNextParticipant(SimulationInfo game) {
			for (int i = 0, n = priority.size(); i < n; i++) {
				AgentInfo info = (AgentInfo) priority.get(i);
				if (!game.isParticipant(info.part.getID())) {
					priority.remove(i);
					info.count++;
					return info.part;
				}
			}

			for (int i = 0, n = queue.size(); i < n; i++) {
				AgentInfo info = (AgentInfo) queue.remove(0);
				queue.add(info);
				if (game.isParticipant(info.part.getID())) {
					priority.add(info);
				} else {
					info.count++;
					return info.part;
				}
			}
			return null;
		}
	}

	private static class AgentInfo {
		public CompetitionParticipant part;
		public int count;

		public AgentInfo(CompetitionParticipant part) {
			this.part = part;
		}
	}

} // ScheduleChanger
