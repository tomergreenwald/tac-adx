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
 * AdminPage
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Apr 28 17:55:15 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.util.ByteArrayISO8859Writer;
import se.sics.tasim.is.SimulationInfo;

/**
 */
public class AdminPage extends HttpPage {

	private static final boolean SERVER_TIME = false;
	private static final boolean AGENT_LOOKUP = false;

	private final InfoServer infoServer;
	private final SimServer simServer;
	private final String historyPath;
	private final String serverName;
	private final String header;

	public AdminPage(InfoServer infoServer, SimServer simServer,
			String historyPath, String header) {
		this.infoServer = infoServer;
		this.simServer = simServer;
		this.historyPath = historyPath;
		this.serverName = simServer.getServerName();
		this.header = header;
	}

	public void handle(String pathInContext, String pathParams,
			HttpRequest request, HttpResponse response) throws HttpException,
			IOException {
		String userName = request.getAuthUser();
		int userID = infoServer.getUserID(userName);
		if (!infoServer.isAdministrator(userID)) {
			return;
		}
		String name = getName(pathInContext);
		// System.out.println("URL=" + pathInContext + " name=" + name);
		StringBuffer page = null;
		int error = 0;
		try {
			if ("admin".equals(name) || "".equals(name)) {
				page = generateAdmin(request);
			} else if ("competition".equals(name)) {
				page = generateCompetition(request);
			} else if ("games".equals(name)) {
				page = generateGames(request);
			} else {
				error = 404;
			}
		} catch (Exception e) {
			Logger.global.log(Level.WARNING,
					"AdminPage: could not generate page " + name, e);
		} finally {
			if (error > 0) {
				response.sendError(error);
				request.setHandled(true);
			} else if (page == null) {
				response.sendError(500);
				request.setHandled(true);
			} else {
				ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
				writer.write(page.toString());
				response.setContentType(HttpFields.__TextHtml);
				response.setContentLength(writer.size());
				writer.writeTo(response.getOutputStream());
				response.commit();
				request.setHandled(true);
			}
		}
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

	private String getName(String url) {
		int start = url.indexOf('/', 1);
		if (start > 1) {
			start = url.indexOf('/', start + 1);
			if (start > 1 && url.length() > (start + 1)) {
				int end = url.indexOf('/', start + 1);
				return end > 0 ? url.substring(start + 1, end) : url
						.substring(start + 1);
			}
		}
		return "admin";
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
	// Main administration page
	// -------------------------------------------------------------------

	private StringBuffer generateAdmin(HttpRequest req) {
		StringBuffer page = pageStart("Administration");
		int gameID = -1;
		int competitionID = -1;
		if (req.getParameter("generateResults") != null) {
			try {
				String id = req.getParameter("gameID");
				if (id == null || id.length() == 0) {
					throw new IllegalArgumentException("no game id specified");
				}
				gameID = Integer.parseInt(id);
				simServer.generateResults(gameID, false, true);
				page.append("<hr><p>Result page generated for game ").append(
						gameID).append("<p><hr><p>\r\n");
			} catch (Exception e) {
				Logger.global.log(Level.WARNING,
						"AdminPage: could not generate score page", e);
				page
						.append(
								"<hr><p><font color=red>could not generate score page: ")
						.append(e).append("</font>").append("<p><hr><p>\r\n");
			}

		} else if (req.getParameter("generateCompetition") != null) {
			try {
				String id = req.getParameter("competitionID");
				if (id == null || id.length() == 0) {
					throw new IllegalArgumentException(
							"no competition id specified");
				}
				competitionID = Integer.parseInt(id);
				simServer.generateCompetitionResults(competitionID);
				page.append("<hr><p>Result page generated for competition ")
						.append(competitionID).append("<p><hr><p>\r\n");
			} catch (Exception e) {
				Logger.global.log(Level.WARNING,
						"AdminPage: could not generate competition results", e);
				page.append(
						"<hr><p><font color=red>"
								+ "could not generate competition results: ")
						.append(e).append("</font>").append("<p><hr><p>\r\n");
			}
		} else if (req.getParameter("scratchGame") != null) {
			try {
				String id = req.getParameter("scratchID");
				if (id == null || id.length() == 0) {
					throw new IllegalArgumentException("no game id specified");
				}
				int scratchID = Integer.parseInt(id);
				simServer.scratchSimulation(scratchID);
				page.append("<hr><p>Game ").append(scratchID).append(
						" has been scratched!<p><hr><p>\r\n");
			} catch (Exception e) {
				Logger.global.log(Level.WARNING,
						"AdminPage: could not scratch game", e);
				page.append(
						"<hr><p><font color=red>" + "could not scratch game: ")
						.append(e).append("</font>").append("<p><hr><p>\r\n");
			}

			// } else if (req.getParameter("setautojoin") != null) {
			// try {
			// boolean join = req.getParameter("autojoin") != null;
			// String delay = req.getParameter("autojoindelay");
			// if (delay == null) {
			// throw new IllegalArgumentException("no delay specified");
			// }
			// int autoDelay = Integer.parseInt(delay);
			// infoServer.setAutoJoin(join, autoDelay);
			// page.append("<hr><p>Auto join values has been set.<p><hr><p>\r\n");
			// } catch (Exception e) {
			// Logger.global.log(Level.WARNING,
			// "AdminPage: could not set auto join", e);
			// page.append("<hr><p><font color=red>could not set auto join values: ")
			// .append(e).append("</font>").append("<p><hr><p>\r\n");
			// }
			// } else if (req.getParameter("setwebjoin") != null) {
			// infoServer.setWebJoinActive(req.getParameter("webjoin") != null);
			// page.append("<hr><p>Web join values has been set.<p><hr><p>\r\n");

		} else if (req.getParameter("reserve") != null) {
			String len = req.getParameter("reserveLength");
			String tim = req.getParameter("reserveTime");
			if (len != null && len.length() > 0 && tim != null
					&& tim.length() > 0) {
				try {
					int length = Integer.parseInt(len);
					long startTime = GameScheduler.parseServerTimeDate(tim);
					simServer.addTimeReservation(startTime, length * 60 * 1000);
					page.append(
							"<hr><p>Requested time reservation starting at ")
							.append(
									GameScheduler
											.formatServerTimeDate(startTime))
							.append(" lasting for ").append(length).append(
									" minutes.").append("<p><hr><p>\r\n");
				} catch (Exception e) {
					Logger.global.log(Level.WARNING,
							"AdminPage: could not reserve time", e);
					page.append(
							"<hr><p><font color=red>Could not reserve time: ")
							.append(e).append("</font><hr><p>\r\n");
				}
			}

		} else if (req.getParameter("Force") != null) {
			String w = req.getParameter("forceWeight");
			if (w != null && w.length() > 0) {
				try {
					float weight = Float.parseFloat(w);
					Competition.setForcedWeight(weight, weight >= 0);
					page.append("<hr><p>Weight force to " + weight + "<hr><p>");
				} catch (Exception e) {
					Logger.global.log(Level.WARNING,
							"AdminPage: could not force weight", e);
					page
							.append(
									"<hr><p><font color=red>Could not set forced weight: ")
							.append(e).append("</font><hr><p>\r\n");
				}
			}

		} else if (req.getParameter("maxSchedule") != null) {
			String m = req.getParameter("maxScheduleCount");
			if (m != null && m.length() > 0) {
				try {
					int max = Integer.parseInt(m);
					simServer.setMaxAgentScheduled(max);
					page.append("<hr><p>Agents are limited to be scheduled in "
							+ max + " games in advanced<hr><p>");
				} catch (Exception e) {
					Logger.global.log(Level.WARNING,
							"AdminPage: could not limit agent scheduling", e);
					page
							.append(
									"<hr><p><font color=red>Could not limit the number "
											+ "of games an agent can schedule in advanced: ")
							.append(e).append("</font><hr><p>\r\n");
				}
			}

		} else if (req.getParameter("setServerMessage") != null) {
			String message = trim(req.getParameter("message"));
			simServer.setServerMessage(message);
			if (message == null) {
				page.append("<hr><p>Server message removed");
			} else {
				page.append("<hr><p>Server message set to '").append(message)
						.append('\'');
			}
			page.append("<p><hr><p>\r\n");
		}

		// Server time
		long serverTime = 0;
		String serverTimeAsString = null;
		String timeMessage = null;

		if (SERVER_TIME) {
			if (req.getParameter("timeToString") != null) {
				try {
					String t = req.getParameter("serverTime");
					if (t == null || t.length() == 0) {
						timeMessage = "<font color=red>no time specified</font>";
					} else {
						serverTime = Long.parseLong(t);
						serverTimeAsString = InfoServer
								.getServerTimeAsString(serverTime);
					}
				} catch (Exception e) {
					timeMessage = "<font color=red>ERROR: " + e + "</font>";
				}
			}
		}
		if (serverTime == 0) {
			serverTime = infoServer.getServerTimeMillis();
		}
		if (SERVER_TIME) {
			page
					.append(
							"<font face=arial size='+1'>Server time</font><p>\r\n"
									+ "<form method=post>"
									+ "Server time: <input name=serverTime type=text value='")
					.append(serverTime).append("'> &nbsp; ");
			if (serverTimeAsString != null) {
				page.append(" => ").append(serverTimeAsString).append(
						" &nbsp; ");
			}
			page
					.append("<input type=submit name=timeToString value='Convert'>");
			if (timeMessage != null) {
				page.append(" &nbsp; ").append(timeMessage);
			}
			page.append("</form><p>\r\n");
		}

		// Agent information
		if (AGENT_LOOKUP) {
			String agentName = null;
			String agentID = null;
			String agentMessage = null;
			if (req.getParameter("agentToID") != null) {
				try {
					agentName = req.getParameter("agentName");
					if (agentName == null || agentName.length() == 0) {
						agentMessage = "<font color=red>no agent name specified</font>";
					} else {
						int id = infoServer.getUserID(agentName);
						if (id < 0) {
							// Perhaps an agent id???
							try {
								id = Integer.parseInt(agentName);
								agentID = infoServer.getUserName(id);
							} catch (Exception e) {
								agentMessage = "<font color=red>ERROR: could not find user '"
										+ agentName + "'</font>";
							}
						} else {
							agentID = Integer.toString(id);
						}
					}
				} catch (Exception e) {
					agentMessage = "<font color=red>ERROR: " + e + "</font>";
				}
			}

			page.append("<font face=arial size='+1'>Agent Info</font><p>\r\n"
					+ "<form method=post>"
					+ "Agent Name: <input name=agentName type=text value='");
			if (agentName != null) {
				page.append(agentName);
			}
			page.append("'> &nbsp; ");
			if (agentID != null) {
				page.append(" => ").append(agentID).append(" &nbsp; ");
			}
			page.append("<input type=submit name=agentToID value='Convert'>");
			if (agentMessage != null) {
				page.append(" &nbsp; ").append(agentMessage);
			}
			page.append("</form><p>\r\n");
		}

		// Result page generation
		page
				.append("<font face=arial size='+1'>Generate Result Page</font><p>\r\n"
						+ "<form method=post>"
						+ "Game ID: <input name=gameID type=text");
		if (gameID > 0) {
			page.append(" value='").append(gameID).append('\'');
		}
		page.append("> \r\n" + "<input type=submit name=generateResults "
				+ "value='Generate Results'>" + "</form>\r\n");

		// Competition result generation
		page.append("<font face=arial size='+1'>Generate Competition Results"
				+ "</font><p>\r\n" + "<form method=post>"
				+ "Competition ID: <input name=competitionID type=text");
		if (competitionID > 0) {
			page.append(" value='").append(competitionID).append('\'');
		}
		page.append("> \r\n" + "<input type=submit name=generateCompetition "
				+ "value='Generate Competition Results'>" + "</form>\r\n");

		// Force
		page
				.append("<font face=arial size='+1'>Competition Force Weight</font>"
						+ "\r\n"
						+ "<form method=post>"
						+ "Force weight <input type=text name=forceWeight>"
						+ " &nbsp; <input type='submit' value='Force' name='Force'> ");
		if (Competition.isWeightForced()) {
			page.append("Weight is currently forced to ").append(
					Competition.getForcedWeight()).append(
					". Set to <code>-1</code> to disable forced weight.");
		} else {
			page.append("Weight is currently not forced.");
		}
		page.append("</form>\r\n");

		// Limit agent scheduling in future games
		page
				.append("<font face=arial size='+1'>Web Join</font>"
						+ "\r\n"
						+ "<form method=post>"
						+ "Limit number of games in advanced <input type=text name=maxScheduleCount>"
						+ " &nbsp; <input type='submit' value='Set' "
						+ "name='maxSchedule'> ");
		int maxSchedule = simServer.getMaxAgentScheduled();
		if (maxSchedule > 0) {
			page.append("Currently limited to ").append(maxSchedule).append(
					". Set to <code>0</code> for unlimited games.");
		} else {
			page.append("No limit.");
		}
		page.append("</form>\r\n");

		// Scratch game
		page.append("<font face=arial size='+1'>Scratch Game"
				+ "</font><p>\r\n" + "<form method=post>"
				+ "<font color=red size='-1'>"
				+ "WARNING: MAKE SURE YOU KNOW WHAT YOU ARE "
				+ "DOING WHEN SCRATCHING GAMES!!!</font>" + "<br>"
				+ "Game ID: <input name=scratchID type=text" + ">\r\n"
				+ "<input type=submit name=scratchGame "
				+ "value='Scratch Game'>" + "<br>"
				+ "<font color=red size='-1'>"
				+ "WARNING: MAKE SURE YOU KNOW WHAT YOU ARE "
				+ "DOING WHEN SCRATCHING GAMES!!!</font>" + "</form>\r\n");

		// Auto join feature
		// page.append("<p><font face=arial size='+1'>Game Join Settings</font>"
		// + "<p>\r\n"
		// + "<form method=post>"
		// + "Auto Join: <input name='autojoin' type='checkbox'");
		// if (infoServer.isAutoJoinActive()) {
		// page.append(" checked");
		// }
		// page.append("> &nbsp; Auto Join Delay (seconds): "
		// + "<input name='autojoindelay' type='text' size='6' value='")
		// .append(infoServer.getAutoJoinDelay())
		// .append("'> &nbsp; <input type='submit' value='Set' name='setautojoin'>")
		// .append("<br>"
		// + "Web Join: <input name='webjoin' type='checkbox'");
		// if (infoServer.isWebJoinActive()) {
		// page.append(" checked");
		// }
		// page.append("> &nbsp; <input type='submit' value='Set'
		// name='setwebjoin'>"
		// + "</form>\r\n");

		// Time reservation
		page
				.append(
						"<p><font face=arial size='+1'>Time Reservation</font><p>\r\n"
								+ "<form method=post>"
								+ "Reserve <input type=text name=reserveLength> minutes "
								+ "starting at <input type=text name=reserveTime value='")
				.append(GameScheduler.formatServerTimeDate(serverTime)).append(
						"'> &nbsp; <input type='submit' value='Reserve' name='reserve'>"
								+ "</form>\r\n");

		String message = simServer.getServerMessage();
		page
				.append("<p><font face=arial size='+1'>Server Message</font><p>\r\n"
						+ "<form method=post>"
						+ "<textarea cols=30 rows=6 name=message wrap=soft style='width: 90%;'>");
		if (message != null) {
			page.append(message);
		}
		page.append("</textarea><br> <input type='submit' "
				+ "value='Set Server Message' "
				+ "name='setServerMessage'></form>\r\n");
		return pageEnd(page);
	}

	// -------------------------------------------------------------------
	// Game manager page
	// -------------------------------------------------------------------

	private StringBuffer generateGames(HttpRequest req) {
		StringBuffer page = pageStart("Game Administration");
		Set params = req.getParameterNames();
		Iterator paramIterator = params.iterator();
		while (paramIterator.hasNext()) {
			String p = paramIterator.next().toString();
			if (p.startsWith("remove")) {
				p = p.substring(6);
				try {
					int uniqSimID = Integer.parseInt(p);
					simServer.removeSimulation(uniqSimID);
					page.append("<b>Requested that game ").append(uniqSimID)
							.append(" should be removed</b><p>\r\n");
					p = null;
				} catch (Exception e) {
					p += ": " + e;
				} finally {
					if (p != null) {
						page.append("<font color=red>Could not remove game ")
								.append(p).append("</font><p>\r\n");
					}
				}
				break;
			}
		}

		long currentTime = infoServer.getServerTimeMillis();
		SimulationInfo[] simulations = simServer.getComingSimulations();
		page.append("<p>Current server time is ").append(
				InfoServer.getServerTimeAsString(currentTime)).append(
				"<p><form method=post>\r\n" + "<table border=1>\r\n"
						+ "<tr><th>Game</th><th>Start Time (Duration)</th>"
						+ "<th>Type</th>" + "<th>Participants</th>"
						+ "<th>Status</th>" + "<th>&nbsp;</th>\r\n");
		if (simulations != null) {
			for (int i = 0, n = simulations.length; i < n; i++) {
				SimulationInfo g = simulations[i];
				int length = g.getSimulationLength() / 1000;
				int minutes = length / 60;
				int seconds = length % 60;
				page.append("<tr><td>");
				if (g.hasSimulationID()) {
					page.append(g.getSimulationID());
				} else {
					page.append('?');
				}
				page
						.append(" (<em>")
						.append(g.getID())
						.append("</em>)</td><td>")
						.append(
								InfoServer.getServerTimeAsString(g
										.getStartTime()))
						.append(" (")
						.append(minutes)
						.append("&nbsp;min")
						.append(
								seconds > 0 ? ("&nbsp;" + seconds + "&nbsp;sec")
										: "").append(")</td><td>").append(
								simServer.getSimulationTypeName(g.getType()))
						.append("</td><td>");
				for (int j = 0, m = g.getParticipantCount(); j < m; j++) {
					if (j > 0) {
						page.append(", ");
					}
					page
							.append(simServer.getUserName(g, g
									.getParticipantID(j)));
				}
				page.append("&nbsp;").append("</td><td>").append(
						g.getStartTime() <= currentTime ? "Running" : "Coming")
						.append("</td><td>");
				if (g.hasSimulationID()) {
					page.append("&nbsp;");
				} else {
					page.append("<input type=submit name='remove").append(
							g.getID()).append("' value=Remove>");
				}
				page.append("</td></tr>\r\n");
			}
		}
		page.append("</table>\r\n" + "</form>\r\n");
		return pageEnd(page);
	}

	// -------------------------------------------------------------------
	// Competition administration
	// -------------------------------------------------------------------

	private StringBuffer generateCompetition(HttpRequest req) {
		StringBuffer page = pageStart("Competition Administration");
		Competition[] competitions = simServer.getCompetitions();
		if (req.getParameter("setLastFinished") != null) {
			try {
				int lastID = Integer.parseInt(req.getParameter("lastFinished"));
				simServer.setLastFinishedCompetitionID(lastID);
				competitions = simServer.getCompetitions();
				page
						.append(
								"<b>Only "
										+ " competitions newer than competition id ")
						.append(lastID).append(" will be loaded</b><p>\r\n");
			} catch (Exception e) {
				page.append(
						"<font color=red><b>could not parse competition id: ")
						.append(e).append("</b></font><p>\r\n");
			}

		} else if (req.getParameter("changeComp") != null) {
			// Change a competition
			try {
				int id = Integer.parseInt(req.getParameter("compid"));
				String name = trim(req.getParameter("compname"));
				String generator = trim(req.getParameter("compgen"));
				Competition competition = simServer.getCompetitionByID(id);
				if (name == null) {
					throw new IllegalArgumentException("no name specified");
				}
				simServer.setCompetitionInfo(id, name, generator);

				page.append("<b>Competition ").append(name).append(" (")
						.append(id).append(") has been changed!");
			} catch (Exception e) {
				page
						.append(
								"<font color=red><b>could not change competition: ")
						.append(e).append("</b></font><p>\r\n");
			}

		} else {
			Set params = req.getParameterNames();
			Iterator paramIterator = params.iterator();
			while (paramIterator.hasNext()) {
				String p = paramIterator.next().toString();
				if (p.startsWith("remove")) {
					p = p.substring(6);
					try {
						int index = Competition.indexOf(competitions, Integer
								.parseInt(p));
						if (index >= 0) {
							Competition c = competitions[index];
							simServer.removeCompetition(c.getID());
							competitions = simServer.getCompetitions();
							p = null;
							page.append("<b>Competition ").append(c.getName())
									.append(" has been removed</b><p>\r\n");
						} else {
							p += ": not found";
						}

					} catch (Exception e) {
						p += ": " + e;
					} finally {
						if (p != null) {
							page
									.append(
											"<font color=red>Could not remove competition ")
									.append(p).append("</font><p>\r\n");
						}
					}
					break;
				}
			}
		}

		page.append("<form method=post>\r\n" + "<table border=1>\r\n"
				+ "<tr><th>ID</th><th>Name</th>" + "<th>Start Time</th>"
				+ "<th>End time</th>" + "<th>Game IDs</th>"
				+ "<th>Agents/Games</th>" + "<th>&nbsp;</th>\r\n");
		if (competitions == null) {
			page.append("<tr><td colspan=7><em>No competitions found</em>"
					+ "</td></tr>\r\n");
		} else {
			for (int i = 0, n = competitions.length; i < n; i++) {
				Competition comp = competitions[i];
				page.append("<tr><td>");
				if (comp.hasParentCompetition()) {
					page.append(comp.getParentCompetitionID())
							.append(" -&gt; ");
				}
				if (historyPath != null) {
					page.append("<a href='").append(historyPath).append(
							"competition/").append(comp.getID()).append("/'>")
							.append(comp.getID()).append("</a>");
				} else {
					page.append(comp.getID());
				}
				page.append("</td><td>" + "<a href='?edit=").append(
						comp.getID()).append("'>").append(comp.getName())
						.append("</a></td><td>").append(
								InfoServer.getServerTimeAsString(comp
										.getStartTime())).append("</td><td>")
						.append(
								InfoServer.getServerTimeAsString(comp
										.getEndTime())).append("</td><td>");
				if (comp.hasSimulationID()) {
					page.append(comp.getStartSimulationID()).append(" - ")
							.append(comp.getEndSimulationID());
				} else {
					page.append("? - ?");
				}
				page.append(" (<em>").append(comp.getStartUniqueID()).append(
						" - ").append(comp.getEndUniqueID()).append(
						"</em>)</td><td>").append(comp.getParticipantCount())
						.append(" / ").append(comp.getSimulationCount())
						.append("</td><td><input type=submit name='remove")
						.append(comp.getID()).append(
								"' value=Remove></td></tr>\r\n");
			}
		}
		page.append(
				"</table>\r\n"
						+ "<p>Do not load competitions with this id or older: "
						+ "<input type=text name=lastFinished value='").append(
				simServer.getLastFinishedCompetitionID()).append(
				"'> <input type=submit name='setLastFinished' value='Set'>\r\n"
						+ "</form>\r\n");

		String editID = req.getParameter("edit");
		if (editID != null) {
			try {
				int id = Integer.parseInt(editID);
				Competition competition = simServer.getCompetitionByID(id);
				if (competition == null) {
					throw new IllegalArgumentException(
							"could not find competition " + id);
				}
				page
						.append(
								"<font face='arial' size='+1'>"
										+ "Edit competition ")
						.append(competition.getName())
						.append(
								"</font>"
										+ "<p>\r\n<form method=post>"
										+ "<input type=hidden name=compid value='")
						.append(id)
						.append(
								"'>\r\n<p><table borde='0'>"
										+ "<tr><td>Competition name:</td><td>"
										+ "<input type=text size=32 name=compname value='")
						.append(competition.getName())
						.append(
								"'></td></tr><td>Competition score table generator</td>"
										+ "<td><input type=text size=32 name=compgen value='");
				String generator = competition.getScoreClassName();
				if (generator != null) {
					page.append(generator);
				}
				page.append(
						"'></td></tr></table>\r\n<p>"
								+ "<font face='arial' size='+1'>"
								+ "Agents in competition ").append(
						competition.getName()).append("</font>" + "<p>\r\n");
				CompetitionParticipant[] pUsers = competition.getParticipants();
				if (pUsers != null) {
					for (int i = 0, n = pUsers.length; i < n; i++) {
						if (i > 0)
							page.append(", ");
						page.append(pUsers[i].getName());
					}
				}
				page.append("<p><input type=submit name=changeComp "
						+ "value='Set Changes!'>" + "</form>\r\n");

			} catch (Exception e) {
				page.append("<p><font color=red>Could not view competition ")
						.append(editID).append(": ").append(e)
						.append("</font>");
			}

		}
		return pageEnd(page);
	}

	private String trim(String text) {
		return text != null && (text = text.trim()).length() > 0 ? text : null;
	}

} // AdminPage
