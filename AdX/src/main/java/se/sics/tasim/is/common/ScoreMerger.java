/**
 * TAC Supply Chain Management Simulator
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001-2004 SICS AB. All rights reserved.
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
 * ScoreMerger
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Jun 27 14:42:30 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.botbox.util.ArrayUtils;
import se.sics.isl.db.DBMatcher;
import se.sics.isl.db.DBResult;
import se.sics.isl.db.DBTable;
import se.sics.isl.db.Database;
import se.sics.isl.util.ArgumentManager;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.FormatUtils;
import se.sics.isl.util.LogFormatter;
import se.sics.tasim.is.AgentLookup;

/**
 * ScoreMerger is an utility to merge scores from several competitions, running
 * at several servers, to one score page. This makes it possible to run a
 * competition on several servers to have more games in the same time (often
 * needed due to the length of TAC SCM games).
 * <p>
 * 
 * <pre>
 * # CONFIG FILE FOR TAC04 Qualifying rounds
 * competition.name=TAC 2004 Qualifying
 * competition.shortDescription=qualifying
 * competition.addSourceInfo=true
 * competition.shortServerName=true
 * competition.destination=.
 * competition.useWeight=false
 * competition.1.config=tac3_simulator.conf
 * competition.1.id=1
 * competition.1.url=http://tac3.sics.se:8080/tac3.sics.se/history/competition/1/
 * competition.2.config=tac4_simulator.conf
 * competition.2.id=1
 * competition.2.url=http://tac4.sics.se:8080/tac4.sics.se/history/competition/4/
 * is.database.sql.url=jdbc:mysql://localhost:3306/mysql
 * log.consoleLevel=0
 * </pre>
 */
public class ScoreMerger extends DefaultScoreGenerator {

	private final static String DEFAULT_CONFIG = "merge.conf";
	private final static String CONF = "is.";

	private static final Logger log = Logger.getLogger(ScoreMerger.class
			.getName());

	private String[] statPath;
	private String[] statName;
	private String[] statShortName;
	private boolean isAddingSourceInfo = false;
	private boolean isUsingShortStatName = false;
	private String shortDescription = null;

	public ScoreMerger() {
	}

	public boolean isAddingSourceInfo() {
		return isAddingSourceInfo;
	}

	public void setAddingSourceInfo(boolean isAddingSourceInfo) {
		this.isAddingSourceInfo = isAddingSourceInfo;
	}

	public boolean isUsingShortStatName() {
		return isUsingShortStatName;
	}

	public void setUsingShortStatName(boolean isUsingShortStatName) {
		this.isUsingShortStatName = isUsingShortStatName;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public void addUserStatPage(String path, String name) {
		statPath = (String[]) ArrayUtils.add(String.class, statPath, path);
		statName = (String[]) ArrayUtils.add(String.class, statName, name);

		String shortName = name;
		int index = name.indexOf('.');
		if (index > 0) {
			shortName = name.substring(0, index);
		}
		statShortName = (String[]) ArrayUtils.add(String.class, statShortName,
				shortName);
	}

	protected String createUserName(CompetitionParticipant usr, int pos,
			int numberOfAgents) {
		// Assume that statistics page exists!
		StringBuffer sb = new StringBuffer().append("<b>")
				.append(usr.getName()).append("</b>");
		if (statPath != null) {
			sb.append(" (");
			for (int i = 0, n = statPath.length; i < n; i++) {
				if (i > 0) {
					sb.append(",&nbsp;");
				}
				sb.append("<a href='").append(statPath[i]).append(usr.getID())
						.append(".html'>").append(
								isUsingShortStatName ? statShortName[i]
										: statName[i]).append("</a>");
			}
			sb.append(')');
		}
		return sb.toString();
	}

	protected void addPostInfo(StringBuffer page) {
		if (isAddingSourceInfo && statPath != null) {
			page.append("<em>The scores have been combined from");
			if (shortDescription != null) {
				page.append(" the ").append(shortDescription).append(" at");
			}
			for (int i = 0, n = statPath.length; i < n; i++) {
				if (i > 0) {
					if (i >= n - 1) {
						page.append(i > 1 ? ", and" : " and");
					} else {
						page.append(',');
					}
				}
				page.append(" <a href='").append(statPath[i]).append("'>")
						.append(statName[i]).append("</a>");
			}
			page.append(".</em>");
		}
	}

	// -------------------------------------------------------------------
	// Main
	// -------------------------------------------------------------------

	public static void main(String[] args) throws IOException {
		long mergeStartTime = System.currentTimeMillis();

		ArgumentManager config = new ArgumentManager("ScoreMerger", args);
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

		String databaseURL = config.getProperty("is.database.sql.url");
		boolean check = config.getPropertyAsBoolean("n", false);

		// No more need for argument handling. Lets free the memory
		config.finishArguments();

		setLogging(config);

		String competitionName = config.getProperty("competition.name");
		if (competitionName == null) {
			throw new IllegalStateException("no competition name");
		}
		Competition competition = new Competition(0, competitionName);
		ScoreMerger scoreGen;
		String scoreClass = config.getProperty("competition.generator");
		if (scoreClass == null) {
			scoreGen = new ScoreMerger();
		} else {
			try {
				scoreGen = (ScoreMerger) Class.forName(scoreClass)
						.newInstance();
			} catch (ThreadDeath e) {
				throw e;
			} catch (Throwable e) {
				throw (IOException) new IOException(
						"could not create score merger of type '" + scoreClass
								+ '\'').initCause(e);
			}
		}

		boolean generateWeights = true;
		if (!config.getPropertyAsBoolean("competition.useWeight", false)) {
			generateWeights = false;
			competition.setFlags(Competition.NO_WEIGHT);
		}

		for (int i = 1, n = Integer.MAX_VALUE; i < n; i++) {
			String serverConfigFileName = config.getProperty("competition." + i
					+ ".config");
			int competitionID = config.getPropertyAsInt("competition." + i
					+ ".id", 0);
			if (serverConfigFileName == null || competitionID <= 0) {
				break;
			}
			log.info("using server config '" + serverConfigFileName
					+ "' for competition " + competitionID);

			ConfigManager serverConfig = new ConfigManager();
			serverConfig.loadConfiguration(serverConfigFileName);

			if (databaseURL != null) {
				serverConfig.setProperty("is.database.sql.url", databaseURL);
				serverConfig.setProperty("is.user.database.sql.url",
						databaseURL);
			}

			String serverName = serverConfig.getProperty("server.name");
			if (serverName == null) {
				throw new IllegalStateException("no server name for "
						+ serverConfigFileName);
			}

			scoreGen.addUserStatPage(config.getProperty("competition." + i
					+ ".url"), serverName);

			if (check) {
				continue;
			}

			Database userDatabase = DatabaseUtils.createUserDatabase(
					serverConfig, CONF, null);
			DBTable userTable = userDatabase.getTable("users");
			if (userTable == null) {
				userDatabase.close();
				throw new IllegalStateException(
						"could not find user database for " + " competition "
								+ competitionID);
			}

			// Should also be different "configs..."
			Database database = DatabaseUtils
					.createDatabase(serverConfig, CONF);
			log.finer("DB Name: " + database.getName() + " class: " + database);
			Database serverDatabase = DatabaseUtils.createChildDatabase(
					serverConfig, CONF, serverName, database);
			log.finer("Server DB Name: " + serverDatabase.getName()
					+ " class: " + serverDatabase);

			loadCompetitionByParticipants(competition, serverDatabase,
					userTable, competitionID);
			// loadCompetitionByResult(competition, serverDatabase, userTable,
			// competitionID, generateWeights);

			userDatabase.close();
			serverDatabase.close();
			database.close();
		}
		// The competition object is not "final" time to generate the stuff!

		scoreGen.init(null, config.getProperty("competition.destination", "."));
		scoreGen.setAddingSourceInfo(config.getPropertyAsBoolean("competition."
				+ "addSourceInfo", false));
		scoreGen.setUsingShortStatName(config.getPropertyAsBoolean(
				"competition." + "shortServerName", false));

		scoreGen.setShortDescription(config
				.getProperty("competition.shortDescription"));
		scoreGen.setShowingCompetitionTimes(false);

		log.info("Competition: " + competition.getName());
		log.info("Competition has " + competition.getParticipantCount()
				+ " participants");
		log.info("Competition is written to " + scoreGen.getScoreFileName());
		if (!check) {
			scoreGen.createScoreTable(competition, -1);
		}
		log.info("Competition merged in "
				+ (System.currentTimeMillis() - mergeStartTime) + " msek");

		// List the top agents (just for fun)
		scoreGen.listTopAgents(competition, generateWeights);
	}

	private void listTopAgents(Competition competition, boolean isWeightUsed) {
		CompetitionParticipant[] users = competition.getParticipants();
		if (users != null) {
			users = (CompetitionParticipant[]) users.clone();
			Arrays.sort(users, getComparator(isWeightUsed));
			System.err.println();
			System.err.println("Top Agents");
			System.err.println("----------");
			for (int i = 0, n = Math.min(10, users.length); i < n; i++) {
				CompetitionParticipant usr = users[i];
				if (i < 9)
					System.err.print(' ');
				System.err.print((i + 1) + " " + usr.getName() + '\t');
				if (usr.getName().length() < 13) {
					System.err.print('\t');
				}
				if (isWeightUsed) {
					System.err.print(FormatUtils.formatAmount((long) usr
							.getAvgWeightedScore()) + '\t');
				}
				System.err.println(FormatUtils.formatAmount((long) usr
						.getAvgScore())
						+ "\t Games: "
						+ usr.getGamesPlayed()
						+ '\t'
						+ usr.getZeroGamesPlayed());
			}
		}
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

	private static void loadCompetitionByResult(Competition competition,
			Database serverDatabase, DBTable userTable, int competitionID,
			boolean generateWeights) {
		Competition currentCompetition = null;
		DBTable competitionTable = serverDatabase.getTable("competitions");
		DBMatcher compDBM = new DBMatcher();
		compDBM.setInt("id", competitionID);
		compDBM.setLimit(1);
		DBResult compResult = competitionTable.select(compDBM);
		if (compResult.next()) {
			int parentID = compResult.getInt("parent");
			String name = compResult.getString("name");
			int flags = compResult.getInt("flags");
			long startTime = compResult.getLong("starttime");
			long endTime = compResult.getLong("endtime");
			int startUniqueID = compResult.getInt("startuniqid");
			int startPublicID = compResult.getInt("startsimid");
			int simulationCount = compResult.getInt("simulations");
			double startWeight = compResult.getDouble("startweight");
			String scoreClass = compResult.getString("scoreclass");
			currentCompetition = new Competition(competitionID, name,
					startTime, endTime, startUniqueID, simulationCount,
					startWeight);
			if (startPublicID >= 0) {
				currentCompetition.setStartSimulationID(startPublicID);
			}
			if (scoreClass != null) {
				currentCompetition.setScoreClassName(scoreClass);
			}
			currentCompetition.setFlags(flags);
			if (parentID > 0) {
				currentCompetition.setParentCompetitionID(parentID);
				log
						.warning("SCORE MERGER RESULT RECALCULATION DOES NOT SUPPORT "
								+ "PARENT COMPETITIONS!!!"); // FIX THIS!!! TODO
			}
		} else {
			log.warning("could not find competition " + competitionID);
		}
		compResult.close();
		log.finer("current competition: "
				+ (currentCompetition == null ? "<null>" : currentCompetition
						.getName()));

		DBTable competitionParticipantTable = serverDatabase
				.getTable("competitionparts");
		DBMatcher dbm = new DBMatcher();
		dbm.setInt("competition", competitionID);
		DBResult result = competitionParticipantTable.select(dbm);
		while (result.next()) {
			int pid = result.getInt("participantid");
			if (competition.getParticipantByID(pid) == null) {
				DBMatcher dbm2 = new DBMatcher();
				dbm2.setInt("id", pid);
				dbm2.setLimit(1);
				DBResult user = userTable.select(dbm2);
				if (user.next()) {
					String userName = user.getString("name");
					log.finer("Adding user " + userName);
					CompetitionParticipant cp = new CompetitionParticipant(pid,
							userName);
					// cp.setHandleWorst(10, 0.1f);
					competition.addParticipant(cp);
				} else {
					log.severe("Could not find user " + pid + " in user db");
					return;
				}
				user.close();
			}
		}
		result.close();

		// All users added to the competition, now go tru the games and
		// add up all the scores !!!
		DBTable competitionResult = serverDatabase
				.getTable("competitionresults");
		dbm.clear();
		dbm.setInt("competition", competitionID);
		result = competitionResult.select(dbm);
		while (result.next()) {
			CompetitionParticipant participant = competition
					.getParticipantByID(result.getInt("participantid"));
			if (participant != null) {
				// Should also check for scratch!!!
				int simID = result.getInt("simid");
				int flags = result.getInt("flags");
				long score = result.getLong("score");
				double weight = (generateWeights && currentCompetition != null) ? currentCompetition
						.getWeight(result.getInt("id"))
						: result.getDouble("weight");
				if (generateWeights) {
					log.finer("weight for game " + simID + ": " + weight);
				}
				participant.addScore(simID, score, weight, (score == 0L)
						|| ((flags & SimServer.ZERO_GAME) != 0));
			} else {
				log.severe("Can not find participant "
						+ result.getInt("participantid") + " in Competition");
				return;
			}
		}
		result.close();
	}

	private static void loadCompetitionByParticipants(Competition competition,
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

		addScores(competition, currentCompetition, true);

		while (currentCompetition.hasParentCompetition()) {
			int parentID = currentCompetition.getParentCompetitionID();
			currentCompetition = loadCompetition(userTable, agentLookup,
					competitionTable, competitionParticipantTable, parentID);
			if (currentCompetition == null) {
				IOException ioe = new IOException("competition not found");
				log.log(Level.SEVERE, "could not find competition " + parentID,
						ioe);
				throw ioe;
			}
			log.finer("loaded parent competition "
					+ currentCompetition.getName());
			addScores(competition, currentCompetition, false);
		}
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

			if (parentID > 0) {
				// Competition is chained (a continuation of another
				// competition)
				competition.setParentCompetitionID(parentID);
			}

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
				cp.setScores(res2.getLong("score"), res2.getDouble("wscore"),
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
			theCompetition = competition;
		}
		res.close();

		return theCompetition;
	}

} // ScoreMerger
