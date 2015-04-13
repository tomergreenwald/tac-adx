/*
 * TACAAGameScheduler.java
 *
 * COPYRIGHT  2008
 * THE REGENTS OF THE UNIVERSITY OF MICHIGAN
 * ALL RIGHTS RESERVED
 *
 * PERMISSION IS GRANTED TO USE, COPY, CREATE DERIVATIVE WORKS AND REDISTRIBUTE THIS
 * SOFTWARE AND SUCH DERIVATIVE WORKS FOR NONCOMMERCIAL EDUCATION AND RESEARCH
 * PURPOSES, SO LONG AS NO FEE IS CHARGED, AND SO LONG AS THE COPYRIGHT NOTICE
 * ABOVE, THIS GRANT OF PERMISSION, AND THE DISCLAIMER BELOW APPEAR IN ALL COPIES
 * MADE; AND SO LONG AS THE NAME OF THE UNIVERSITY OF MICHIGAN IS NOT USED IN ANY
 * ADVERTISING OR PUBLICITY PERTAINING TO THE USE OR DISTRIBUTION OF THIS SOFTWARE
 * WITHOUT SPECIFIC, WRITTEN PRIOR AUTHORIZATION.
 *
 * THIS SOFTWARE IS PROVIDED AS IS, WITHOUT REPRESENTATION FROM THE UNIVERSITY OF
 * MICHIGAN AS TO ITS FITNESS FOR ANY PURPOSE, AND WITHOUT WARRANTY BY THE
 * UNIVERSITY OF MICHIGAN OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT
 * LIMITATION THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE REGENTS OF THE UNIVERSITY OF MICHIGAN SHALL NOT BE LIABLE FOR ANY
 * DAMAGES, INCLUDING SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WITH
 * RESPECT TO ANY CLAIM ARISING OUT OF OR IN CONNECTION WITH THE USE OF THE SOFTWARE,
 * EVEN IF IT HAS BEEN OR IS HEREAFTER ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package edu.umich.eecs.tac.is;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.util.ByteArrayISO8859Writer;

import com.botbox.util.ArrayUtils;

import se.sics.tasim.is.AgentInfo;
import se.sics.tasim.is.CompetitionSchedule;
import se.sics.tasim.is.common.Competition;
import se.sics.tasim.is.common.CompetitionParticipant;
import se.sics.tasim.is.common.GameScheduler;
import se.sics.tasim.is.common.HttpPage;
import se.sics.tasim.is.common.InfoServer;
import se.sics.tasim.is.common.ScoreGenerator;
import se.sics.tasim.is.common.SimServer;
import tau.tac.adx.sim.TACAdxConstants;

/**
 * @author Ben Cassell, based on GameScheduler by SICS
 */

public class TACAAGameScheduler extends HttpPage {
	
	private static final Logger log = Logger.getLogger(GameScheduler.class
			.getName());

	private final InfoServer infoServer;
	private final SimServer simServer;
	private final String serverName;
	private final String header;

	private static SimpleDateFormat dateFormat = null;
	private int agentsPerGame = 8;

	// private int delayBetweenGames = 60 * 1000;
	// private int minGameLength = 5;
	// private int maxGameLength = 60;
	// private int defaultGameLength = 55;

	public TACAAGameScheduler(InfoServer infoServer, SimServer simServer,
			String header) {
		this.infoServer = infoServer;
		this.simServer = simServer;
		this.serverName = simServer.getServerName();
		this.header = header;
	}

	public void handle(String pathInContext, String pathParams,
			HttpRequest req, HttpResponse response) throws HttpException,
			IOException {
		String userName = req.getAuthUser();
		int httpUserID = infoServer.getUserID(userName);
		if (!infoServer.isAdministrator(httpUserID)) {
			return;
		}

		// Administrator is included in this ???
		StringBuffer page = pageStart("Competition Scheduler");

		if (req.getParameter("submit") != null) {
			handlePreview(req, page);

		} else if (req.getParameter("execute") != null) {
			// Create the games!!!
			handleExecute(req, page);

		} else {
			handleConfiguration(req, page);
		}

		page = pageEnd(page);

		ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
		writer.write(page.toString());
		response.setContentType(HttpFields.__TextHtml);
		response.setContentLength(writer.size());
		writer.writeTo(response.getOutputStream());
		response.commit();
		req.setHandled(true);
	}

	private void handlePreview(HttpRequest req, StringBuffer page) {
		AgentInfo[] users = infoServer.getAgentInfos();
		int nrUsers = 0;
		int[] agentIDs = new int[] { -1 };
		String[] agentNames = new String[] { "dummy" };

		String addIDs = trim(req.getParameter("agents"));
		String message = null;

		page.append("<font face='arial' size='+1'>Agents in competition</font>"
				+ "<p>\r\n");

		for (int i = 0, n = users.length; i < n; i++) {
			if (req.getParameter("join-" + users[i].getID()) != null) {
				if (nrUsers > 0)
					page.append(", ");
				page.append(users[i].getName());
				nrUsers++;
				agentIDs = ArrayUtils.add(agentIDs, users[i].getID());
				agentNames = (String[]) ArrayUtils.add(agentNames, users[i]
						.getName());
			}
		}
		if (addIDs != null) {
			StringTokenizer stok = new StringTokenizer(addIDs, "\r\n, ");
			while (stok.hasMoreTokens()) {
				String token = stok.nextToken();
				AgentInfo usr = getUser(token, users);
				if (usr != null) {
					if (nrUsers > 0)
						page.append(", ");
					page.append(usr.getName());
					nrUsers++;
					agentIDs = ArrayUtils.add(agentIDs, usr.getID());
					agentNames = (String[]) ArrayUtils.add(agentNames, usr
							.getName());
				} else if (message == null) {
					message = token;
				} else {
					message += ", " + token;
				}
			}
		}

		if (message != null) {
			if (nrUsers == 0) {
				page.append("<font color=red>No agents in competition</font>");
			}

			page.append(
					"<p>\r\n" + "<font face='arial' size='+1'>"
							+ "Agents that could not be found</font>"
							+ "<p>\r\n<font color=red>").append(message)
					.append("</font>\r\n");
		}

		try {
			Competition parentCompetition = null;
			String parentIDStr = trim(req.getParameter("parent"));
			if (parentIDStr != null) {
				int parentID = Integer.parseInt(parentIDStr);
				parentCompetition = simServer.getCompetitionByID(parentID);
				if (parentCompetition == null) {
					throw new IllegalArgumentException("could not find parent "
							+ "competition " + parentID);
				}

				page.append("<p><font face='arial' size='+1'>Agents in parent "
						+ "competition " + parentCompetition.getName()
						+ "</font>" + "<p>\r\n");

				CompetitionParticipant[] pUsers = parentCompetition
						.getParticipants();
				if (pUsers != null) {
					for (int i = 0, n = pUsers.length; i < n; i++) {
						if (i > 0)
							page.append(", ");
						page.append(pUsers[i].getName());
					}
				}
			}
			page.append("<p>\r\n");

			String totalGamesStr = req.getParameter("games");
			if (totalGamesStr == null || totalGamesStr.length() == 0) {
				throw new IllegalArgumentException(
						"total number of games not specified");
			}
			int totalGames = Integer.parseInt(totalGamesStr);
			if(totalGames/4 == 0){
				throw new IllegalArgumentException("Fewer than 4 games requested");
			}
			long time = parseServerTimeDate(req.getParameter("time"));
			// int gameLength =
			// Integer.parseInt(req.getParameter("gameLength"));
			// int gameLengthMillis = gameLength * 60000;
			float weight = Float.parseFloat(req.getParameter("weight"));
			boolean startWeightDuringWeekends = req.getParameter("weekend") != null;
			boolean noWeights = req.getParameter("useweights") == null;
			boolean lowestScoreAsZero = req.getParameter("lowestscore") != null;
			String name = trim(req.getParameter("name"));
			int timeBetween = Integer.parseInt(req.getParameter("timeBetween"));
			int reserveBetween = Integer.parseInt(req
					.getParameter("reserveBetween"));
			int reserveTime = Integer.parseInt(req.getParameter("reserveTime"));
			boolean totalAgent = "agent".equals(req.getParameter("type"));
			String scoreGenerator = trim(req.getParameter("scoregen"));
			if (nrUsers == 0) {
				throw new IllegalArgumentException("No agents in competition");
			}
			if (name == null || name.length() == 0) {
				throw new IllegalArgumentException("No competition name");
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

			// if (gameLength < minGameLength || gameLength > maxGameLength) {
			// throw new IllegalArgumentException("game length must be between "
			// +
			// minGameLength +
			// " and " +
			// maxGameLength + " minutes");
			// }
			int[][] games = scheduleGames(nrUsers, agentsPerGame);
			int minGames = 4*games.length;
			int perAgent = 4*gamesPerAgent(nrUsers);

			int rounds;
			if (totalAgent) {
				rounds = totalGames / perAgent;
			} else {
				rounds = totalGames / minGames;
			}

			page.append(
					"<font face='arial' size='+1'>Competition Data</font>"
							+ "<p>\r\n<table border='0'>"
							+ "<tr><td>Competition name:</td><td>")
					.append(name);

			if (parentCompetition != null) {
				page
						.append(
								"</td></tr\r\n>"
										+ "<tr><td>Continuation of competition:</td><td>")
						.append(parentCompetition.getName());
			}

			if (scoreGenerator != null) {
				page
						.append(
								"</td></tr\r\n>"
										+ "<tr><td>Competition score table generator</td><td>")
						.append(scoreGenerator);
			}
			page
					.append(
							"</td></tr\r\n>"
									+ "<tr><td>Total number of players:</td><td>")
					.append(nrUsers)
					.append(
							"</td></tr\r\n>"
									+ "<tr><td>Requested number of games:</td><td>")
					.append(totalGames);
			if (totalAgent) {
				page.append(" per agent");
			}
			page
					.append(
							"</td></tr\r\n>"
									+ "<tr><td>Number of games scheduled:</td><td>")
					.append(rounds * minGames);
					if(rounds == 0)
						page.append("</td><td> WARNING: If number of players is > 8 and not divisible by 2, request at least 8 games per agent");
					page.append(
							"</td></tr\r\n>"
									+ "<tr><td>Number of games per round:</td><td>")
					.append(minGames)
					.append(
							"</td></tr>"
									+ "<tr><td>Number of rounds:</td><td>"
									+ rounds
									+ "</td></tr\r\n>"
									+ "<tr><td>Number of games per agent/round:</td><td>"
									+ perAgent
									+ "</td></tr\r\n>"
									+ "<tr><td>Number of games per agent:</td><td>")
					.append(perAgent * rounds)
					.append("</td></tr\r\n>"
					// + "<tr><td>Game Length:</td><td>")
							// .append(gameLength)
							// .append(" min</td></tr\r\n>"
							+ "<tr><td>Start Time:</td><td>")
					.append(formatServerTimeDate(time))
					.append(
							"</td></tr\r\n>"
									+ "<tr><td>Approx End Time:</td><td>");
			long endTime = time + (rounds * minGames) * (10 + timeBetween)
					* 60000L;
			if (reserveTime > 0 && reserveBetween > 0) {
				endTime += reserveTime * ((rounds * minGames) / reserveBetween)
						* 60000L;
			}
			page.append(formatServerTimeDate(endTime)).append(
					"</td></tr\r\n>" + "<tr><td>Start Weight:</td><td>")
					.append(weight);
			if (noWeights) {
				page.append(" (does not use weighted scores");
				if (startWeightDuringWeekends) {
					page.append("; start weight during weekends");
				}
				page.append(')');
			} else if (startWeightDuringWeekends) {
				page.append(" (use start weight during weekends)");
			}
			if (lowestScoreAsZero) {
				page.append("</td></tr\r\n>"
						+ "<tr><td>Score for zero games</td>"
						+ "<td>Use lowest score if smaller than zero");
			}
			page
					.append(
							"</td></tr\r\n>"
									+ "<tr><td>Delay between games (minutes)</td><td>")
					.append(timeBetween)
					.append(
							"</td></tr\r\n>"
									+ "<tr><td>Reserve time for admin (minutes)</td><td>")
					.append(reserveTime)
					.append(
							"</td></tr\r\n>"
									+ "<tr><td>Played games between time reservation:"
									+ "</td><td>")
					.append(reserveBetween)
					.append(
							"</td></tr\r\n>"
									+ "</table>\r\n<p>\r\n"
									+ "<font face='arial' size='+1'>Example round</font><p>\r\n"
									+ "<table border=1><tr><th>Game</th><th>Agents</th></tr>");
			for (int i = 0; i < minGames/4; i++) {
				for(int b = 0; b < 4; b++){
				page.append("<tr><td>").append(i + 1).append("</td><td>");
				for (int a = 0; a < agentsPerGame; a++) {
					page.append(agentNames[games[i][a]]).append(' ');
				}
				page.append("</td></tr>");
				}
			}
			page.append(
					"</table>\r\n<p>\r\n" + "<form method=post>"
							+ "<input type=hidden name=agentNo value=").append(
					nrUsers).append("><input type=hidden name=rounds value=")
					.append(rounds).append('>');
			for (int i = 0; i < nrUsers; i++) {
				page.append("<input type=hidden name=agent").append(i).append(
						" value=").append(agentIDs[i + 1]).append('>');
			}
			page.append("<input type=hidden name=time value=").append(time)
					.append('>');
			// page.append("<input type=hidden name=gameLength value=")
			// .append(gameLength).append('>');
			page.append("<input type=hidden name=weight value=").append(weight)
					.append('>');
			if (!noWeights) {
				page.append("<input type=hidden name=useweights value='true'>");
			}
			if (startWeightDuringWeekends) {
				page.append("<input type=hidden name=weekend value='true'>");
			}
			if (lowestScoreAsZero) {
				page
						.append("<input type=hidden name=lowestscore value='true'>");
			}
			page.append("<input type=hidden name=timeBetween value=").append(
					timeBetween).append('>');
			page.append("<input type=hidden name=reserveTime value=").append(
					reserveTime).append('>');
			page.append("<input type=hidden name=reserveBetween value=")
					.append(reserveBetween).append('>');
			page.append("<input type=hidden name=name value='").append(name)
					.append("'>");
			if (parentCompetition != null) {
				page.append("<input type=hidden name=parent value='").append(
						parentCompetition.getID()).append("'>");
			}
			if (scoreGenerator != null) {
				page.append("<input type=hidden name=scoregen value='").append(
						scoreGenerator).append("'>");
			}
			page.append("\r\n<input type=submit name=execute "
					+ "value='Create Competition'> &nbsp; "
					+ "<input type=submit name=cancel " + "value='Cancel'>"
					+ "</form>\r\n");
		} catch (Exception e) {
			log.log(Level.WARNING, "could not schedule games", e);
			page
					.append("Could not schedule games: <font color=red>")
					.append(e)
					.append(
							"</font><p>Try to go back and enter correct information");
		}
	}

	private void handleExecute(HttpRequest req, StringBuffer page) {
		AgentInfo[] users = infoServer.getAgentInfos();
		// Create the games!!!
		try {
			int rounds = Integer.parseInt(req.getParameter("rounds"));
			long startTime = Long.parseLong(req.getParameter("time"));
			// int gameLength =
			// Integer.parseInt(req.getParameter("gameLength"));
			// int gameLengthMillis = gameLength * 60000;
			float startWeight = Float.parseFloat(req.getParameter("weight"));
			boolean noWeights = req.getParameter("useweights") == null;
			boolean startWeightDuringWeekends = req.getParameter("weekend") != null;
			boolean lowestScoreAsZero = req.getParameter("lowestscore") != null;
			String name = req.getParameter("name");
			Competition parentCompetition = null;
			String parentIDStr = trim(req.getParameter("parent"));
			if (parentIDStr != null) {
				int parentID = Integer.parseInt(parentIDStr);
				parentCompetition = simServer.getCompetitionByID(parentID);
				if (parentCompetition == null || parentID <= 0) {
					throw new IllegalArgumentException("could not find parent "
							+ "competition " + parentIDStr);
				}
			}
			int timeBetween = Integer.parseInt(req.getParameter("timeBetween")) * 60000;
			int reserveTimeMillis = Integer.parseInt(req
					.getParameter("reserveTime")) * 60000;
			int reserveBetween = Integer.parseInt(req
					.getParameter("reserveBetween"));
			String scoreGenerator = trim(req.getParameter("scoregen"));

			int nrUsers = Integer.parseInt(req.getParameter("agentNo"));
			int[] participantIDs = new int[nrUsers];
			int[] idMap = new int[nrUsers + 1];
			idMap[0] = -1;

			for (int i = 0; i < nrUsers; i++) {
				int userID = Integer.parseInt(req.getParameter("agent" + i));
				AgentInfo usr = getUser(userID, users);
				if ((usr == null) || (usr.getID() != userID)) {
					throw new IllegalStateException("user " + userID
							+ " not found");
				}
				idMap[i + 1] = userID;
				participantIDs[i] = userID;
			}

			long currentTime = infoServer.getServerTimeMillis();
			if (currentTime > startTime) {
				throw new IllegalStateException("start time already passed "
						+ "or too close into the future");
			}
			if (startWeight == 0f) {
				throw new IllegalStateException("start weight may not be 0");
			}

			// Creating Games...
			int[][] scheduledGames = null;
			int nextGame = 0;
			int scheduledAgentsPerGame = nrUsers < agentsPerGame ? nrUsers
					: agentsPerGame;
			for (int i = 0; i < rounds; i++) {
				int[][] games = scheduleGames(nrUsers, agentsPerGame);
				if (scheduledGames == null) {
					scheduledGames = new int[games.length * rounds][scheduledAgentsPerGame];
				}
				for (int g = 0, m = games.length; g < m; g++) {
					int index = 0;
					for (int a = 0; a < agentsPerGame; a++) {
						if (games[g][a] != 0) {
							scheduledGames[nextGame][index++] = idMap[games[g][a]];
							// } else {
							// scheduledGames[nextGame][a] = dummy--;
						}
					}
					nextGame++;
				}
			}
			// After each "round" there can be downtime scheduled???

			if (scheduledGames == null) {
				page.append("No games created");
			} else {
				CompetitionSchedule schedule = new CompetitionSchedule(name);
				schedule.setStartTime(startTime);
				schedule.setParticipants(participantIDs);
				if (parentCompetition != null) {
					schedule.setParentCompetitionID(parentCompetition.getID());
				}
				// Do not allow any more participants in the games
				schedule.setTimeBetweenSimulations(timeBetween);
				schedule.setReservationBetweenSimulations(reserveBetween,
						reserveTimeMillis);
				schedule.setStartWeight(startWeight);
				if (noWeights) {
					schedule.setFlags(schedule.getFlags()
							| Competition.NO_WEIGHT);
				}
				if (startWeightDuringWeekends) {
					schedule.setFlags(schedule.getFlags()
							| Competition.WEEKEND_LOW);
				}
				if (lowestScoreAsZero) {
					schedule.setFlags(schedule.getFlags()
							| Competition.LOWEST_SCORE_FOR_ZERO);
				}
				if (scoreGenerator != null) {
					schedule.setScoreClassName(scoreGenerator);
				}
				schedule.setSimulationsClosed(true);

				for (int i = 0, n = scheduledGames.length; i < n; i++) {
					int [] roles = new int [scheduledGames[i].length];
					for(int j = 0; j < scheduledGames[i].length; j++)
						roles[j] = TACAdxConstants.AD_NETOWRK_ROLE_ID;
					for(int k = 0; k < 4; k++){
						schedule.addSimulation(scheduledGames[i], roles);
					}
				}
                schedule.setSimulationParams("perm1=12345678,perm2=78123456,perm3=56781234,perm4=34567812");
				simServer.scheduleCompetition(schedule);
				page.append("Requested ").append(scheduledGames.length).append(
						" scheduled games in competition ").append(name)
						.append(".<p>");
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "could not create competition", e);
			page.append("Competition could not be created: <font color=red>")
					.append(e).append("</font>");
		}
	}

	private void handleConfiguration(HttpRequest req, StringBuffer page) {
		// No submission: simply web page access
		Competition[] comps = simServer.getCompetitions();
		if (comps != null) {
			Competition currentComp = simServer.getCurrentCompetition();
			page.append("<table border=1 width='100%'>"
					+ "<tr><th colspan=6>Existing Competitions");
			if (currentComp != null) {
				page.append(" (now running: ").append(currentComp.getName())
						.append(')');
			}
			page.append("</th></tr>" + "<tr><th>ID</th><th>Name</th>"
					+ "<th>Start Time</th><th>End Time</th>"
					+ "<th>Games IDs</th><th>Agents/Games</th></tr>");
			for (int i = 0, n = comps.length; i < n; i++) {
				Competition comp = comps[i];
				int numAgents = comp.getParticipantCount();
				int numGames = comp.getSimulationCount();
				page.append("<tr><td>");
				if (comp.hasParentCompetition()) {
					page.append(comp.getParentCompetitionID())
							.append(" -&gt; ");
				}
				page.append(comp.getID()).append("</td><td>").append(
						comp.getName()).append("</td><td>").append(
						formatServerTimeDate(comp.getStartTime())).append(
						"</td><td>").append(
						formatServerTimeDate(comp.getEndTime())).append(
						"</td><td>");
				if (comp.hasSimulationID()) {
					page.append(comp.getStartSimulationID()).append(" - ")
							.append(comp.getEndSimulationID());
				} else {
					page.append("? - ?");
				}
				page.append(" (<em>").append(comp.getStartUniqueID()).append(
						" - ").append(comp.getEndUniqueID()).append(
						"</em>)</td><td>").append(numAgents).append(" / ")
						.append(numGames).append("</td></tr>\r\n");
			}
			page.append("</table><p>\r\n");
		}

		page
				.append(
						"<p><font face='arial' size='+1'>"
								+ "Create new competition:</font>\r\n"
								+ "<form method=post>\r\n")
				.append(
						"<table border='0'>\r\n"
								+ "<tr><td>Name of competition (unique)</td>"
								+ "<td><input type=text name=name size=32></td></tr\r\n>"
								+ "<tr><td>Continuation of competition</td>"
								+ "<td><input type=text name=parent size=32></td></tr>\r\n"

								// +
								// "<tr><td>Select type of scheduling</td><td>"
								// + "<select name=type><option
								// value=total>Total number of
								// games"
								// +
								// "<option value=agent>Number of games per agent</select>"
								+ "</td></tr\r\n><tr><td>"
								+ "<select name=type>"
								+ "<option value=total>Total number of games (int)"
								+ "<option value=agent>Number of games per agent (int)"
								+ "</select>"
								// + "Number of games (total or per agent)"
								+ "</td><td><input type=text name=games size=32></td><td>Must be a multiple of 4</td></tr>"
								+ "<tr><td>Start Time (YYYY-MM-DD HH:mm)</td>"
								+ "<td><input type=text name=time size=32 value='")
				.append(formatServerTimeDate(infoServer.getServerTimeMillis()))
				.append(
						"'></td></tr>"
								// + "<tr><td>Game Length (minutes)</td>"
								// +
								// "<td><input type=text name=gameLength value='"
								// +
								// defaultGameLength +
								// "' size=32></td></tr\r\n>"
								+ "<tr><td>Start Weight (float)</td>"
								+ "<td><input type=text name=weight value='1.0' size=32></td></tr\r\n>"
								+ "<tr><td>&nbsp;</td><td><input type=checkbox name=useweights> "
								+ "Use weighted scores</td></tr>"
								+ "<tr><td>&nbsp;</td><td><input type=checkbox name=weekend> "
								+ "Use start weight during weekends</td></tr>"

								// Lowest score or zero for zero games (agent
								// missing games)
								+ "<tr><td>Score for zero games</td>"
								+ "<td><input type=checkbox name=lowestscore> "
								+ "Use lowest score if smaller than zero</td></tr>"

								+ "<tr><td>Delay between games (minutes)</td>"
								+ "<td><input type=text name=timeBetween value=5 size=32></td></tr\r\n>"
								+ "<tr><td>Time to reserve for admin (minutes)</td>"
								+ "<td><input type=text name=reserveTime value=0 size=32></td></tr\r\n>"
								+ "<tr><td>Played games between time reservations (int)</td>"
								+ "<td><input type=text name=reserveBetween value=0 size=32></td></tr\r\n>"
								+ "<tr><td>Competition score table generator</td>"
								+ "<td><input type=text name=scoregen size=32></td></tr\r\n>"
								+ "<tr><td colspan=2>&nbsp;</td></tr\r\n>"
								+ "<tr><td colspan=2>"
								+ "Specify agents that should be scheduled as comma separated "
								+ "list of agent names<br>"
								+ "(you can also select agents in the list below)"
								+ "</td></tr><tr><td colspan=2>"
								+ "<textarea name=agents cols=75 rows=6></textarea>"
								+ "</td></tr>"
								+ "<tr><td colspan=2>"
								+ "<input type=submit name=submit value='Preview Schedule!'>"
								+ "</td></tr><tr><td colspan=2>&nbsp;</td></tr>\r\n"
								// Agents
								+ "<tr><td colspan=2>"
								+ "<table border=0 width='100%' bgcolor=black "
								+ "cellspacing=0 cellpadding=1><tr><td>"
								+ "<table border=0 bgcolor='#f0f0f0' align=left width='100%' "
								+ "cellspacing=0>"
								+ "<tr><td colspan=5><b>Available agents:</b></td></tr\r\n>"
								+ "<tr>");

		AgentInfo[] users = infoServer.getAgentInfos();
		for (int i = 0, n = users.length; i < n; i++) {
			if (i % 7 == 0 && i > 0) {
				page.append("</tr><tr>");
			}
			page.append("<td><input type=checkbox name=join-").append(
					users[i].getID()).append('>').append(users[i].getName())
					.append("</td>");
		}
		if ((users.length % 7) > 0) {
			page.append("<td colspan=").append(7 - (users.length % 7)).append(
					">&nbsp;</td>");
		}
		page.append("</tr></table></td></tr></table></td></tr></table><p>\r\n"
				+ "</form>\r\n");
	}

	// private String getBase(String url) {
	// int start = url.indexOf('/', 1);
	// if (start > 1) {
	// return url.substring(0, start + 1);
	// } else if (!url.endsWith("/")) {
	// return url + '/';
	// } else {
	// return url;
	// }
	// }

	private AgentInfo getUser(String user, AgentInfo[] users) {
		for (int i = 0, n = users.length; i < n; i++) {
			if (users[i].getName().equals(user))
				return users[i];
		}
		return null;
	}

	private AgentInfo getUser(int userID, AgentInfo[] users) {
		for (int i = 0, n = users.length; i < n; i++) {
			if (users[i].getID() == userID)
				return users[i];
		}
		return null;
	}

	private String trim(String text) {
		return text != null && (text = text.trim()).length() > 0 ? text : null;
	}

	// Returns a matrix of games to play (and which agents that are
	// going to play them
	public int gamesPerAgent(int noAgents) {
		if (noAgents < agentsPerGame + 1) {
			return 1;
		}
		return agentsPerGame / findLargestDivisor(noAgents, agentsPerGame);
	}

	private static int findLargestDivisor(int a, int b) {
		if (a == 1 || b == 1)
			return 1;
		int primes[] = new int[] { 2, 3, 5, 7, 11 };
		int div = 1;
		int pos = 0;
		int max = primes.length;
		while (a > 1 && b > 1 && pos < max) {
			int prime = primes[pos];
			if ((a % prime) == 0 && (b % prime) == 0) {
				a = a / prime;
				b = b / prime;
				div = div * prime;
			} else {
				pos++;
			}
		}
		return div;
	}

	public static int[][] scheduleGames(int noAgents, int agentsPerGame) {
		int[][] games = null;
		if (noAgents <= agentsPerGame) {
			games = new int[1][agentsPerGame];
			for (int i = 0; i < noAgents; i++) {
				games[0][i] = i + 1;
			}
		} else {
			int perAgent = agentsPerGame/(findLargestDivisor(noAgents, agentsPerGame));
			int totalGames = (perAgent * noAgents)/agentsPerGame;
			games = new int[totalGames][agentsPerGame];
			int agent = 0;
			for (int game = 0; game < totalGames; game++) {
				for (int a = 0; a < agentsPerGame; a++) {
					games[game][a] = (agent++ % noAgents) + 1;
				}
			}

			//Where is this 48 coming from?
			for (int i = 0; i < totalGames * 48; i++) {
				int pos1 = (int) (Math.random() * agentsPerGame);
				int pos2 = (int) (Math.random() * agentsPerGame);
				int game1 = (int) (Math.random() * totalGames);
				int game2 = i % totalGames;

				int agent1 = games[game1][pos1];
				int agent2 = games[game2][pos2];

				boolean found = false;
				for (int a = 0; a < agentsPerGame && !found; a++) {
					found = (games[game1][a] == agent2)
							|| (games[game2][a] == agent1);
				}
				if (!found) {
					/*
					 * System.out.println("Swapping agent " + agent1 +
					 * " in game " + game1 + " with " + agent2 + " in " +
					 * game2);
					 */
					games[game1][pos1] = agent2;
					games[game2][pos2] = agent1;
				}
			}
		}
		return games;
	}

	// -------------------------------------------------------------------
	// Web page utilities
	// -------------------------------------------------------------------

	private StringBuffer pageStart(String title) {
		StringBuffer page = new StringBuffer();
		page.append("<html><head><title>").append(infoServer.getServerType()).append(" - ").append(title).append('@')
				.append(serverName)
				.append("</title></head>\r\n" + "<body>\r\n").append(header)
				.append("<font face='arial' size='+2'>").append(title).append(
						" at ").append(serverName).append("</font><p>\r\n");
		return page;
	}

	private StringBuffer pageEnd(StringBuffer page) {
		return page.append("</body>\r\n</html>\r\n");
	}

	// -------------------------------------------------------------------
	// Date handling
	// -------------------------------------------------------------------

	public static synchronized String formatServerTimeDate(long time) {
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			dateFormat.setTimeZone(new java.util.SimpleTimeZone(0, "UTC"));
		}
		return dateFormat.format(new Date(time));
	}

	public static synchronized long parseServerTimeDate(String date)
			throws ParseException {
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			dateFormat.setTimeZone(new java.util.SimpleTimeZone(0, "UTC"));
		}
		return dateFormat.parse(date).getTime();
	}

	// -------------------------------------------------------------------
	// Test main
	// -------------------------------------------------------------------

	public static void main(String[] args) {

		// for (int i = 0, n = 24; i < n; i++) {
		// System.out.println("LCD: 6 <?>" + i + " -> " + findLargestDivisor(6,
		// i));
		// }

		int gameNr = 0;
		if (args.length < 1) {
			System.out.println("Usage: GameScheduler <NoAgents>");
			System.exit(0);
		}

		try {
			gameNr = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("Error in nr");
		}
		int aPerGame = 8;

		int[][] games = scheduleGames(gameNr, aPerGame);
		int[] agentGames = new int[gameNr + 1];
		for (int i = 0, n = games.length; i < n; i++) {
			for(int k = 0; k < 4; k++){
				System.out.print("Game " + i + "." + k + " | ");
				for (int j = 0; j < aPerGame; j++) {
					System.out.print("" + games[i][j] + ' ');
					agentGames[games[i][j]]++;
				}
				System.out.println();
			}
		}
		for (int i = 1; i < gameNr + 1; i++) {
			System.out.println("Agent " + i + " played " + agentGames[i]);
		}
	}
	
}
