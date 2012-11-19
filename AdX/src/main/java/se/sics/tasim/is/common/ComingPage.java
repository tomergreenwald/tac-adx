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
 * ComingPage
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Jan 13 17:40:06 2003
 * Updated : $Date: 2008-06-10 19:51:44 -0500 (Tue, 10 Jun 2008) $
 *           $Revision: 4725 $
 */
package se.sics.tasim.is.common;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.botbox.html.HtmlWriter;
import com.botbox.util.ArrayQueue;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.util.ByteArrayISO8859Writer;
import se.sics.tasim.is.SimulationInfo;

public class ComingPage extends HttpPage {

	private static final Logger log = Logger.getLogger(ComingPage.class
			.getName());

	private final static int MAX_VISIBLE_SIMULATIONS = 100;
	private final static int TIMEOUT = 5000;

	private final InfoServer infoServer;
	private final SimServer simServer;

	private ArrayQueue eventQueue = new ArrayQueue();

	private String timeLimitedMessage;
	private long timeLimit;

	public ComingPage(InfoServer infoServer, SimServer simServer) {
		this.infoServer = infoServer;
		this.simServer = simServer;
	}

	public void setTimeLimitedMessage(String message, long timeLimit) {
		this.timeLimitedMessage = message;
		this.timeLimit = timeLimit;
	}

	public void handle(String pathInContext, String pathParams,
			HttpRequest request, HttpResponse response) throws HttpException,
			IOException {
		String userName = request.getAuthUser();
		int userID = infoServer.getUserID(userName);
		int agentID = 0;
		String message = null;
		String simType = infoServer.getDefaultSimulationType();

		if (HttpRequest.__POST.equals(request.getMethod())) {
			if (InfoServer.ALLOW_SIM_TYPE) {
				String type = request.getParameter("simType");
				if (type != null && type.length() > 0) {
					simType = type;
				}
			}

			if (request.getParameter("createsim") != null) {
				if (simServer.isWebJoinActive()) {
					Event event = new Event(simType);
					addEvent(event);
					// Add support for simulation parameters!!! FIX THIS!!! TODO
					simServer.createSimulation(simType, null);
					message = event.waitForResult();
					if (message == null) {
						removeEvent(event);
						message = "<font color=red>Could not create the game at "
								+ "this time. Please try again later.</font>\r\n";
					}
				} else {
					message = "<font color=red>Could not create the game at "
							+ "this time. Please try again later.</font>\r\n";
				}

				String agentNoStr = request.getParameter("agent_no");
				if (agentNoStr != null) {
					try {
						agentID = Integer.parseInt(request
								.getParameter("agent_no"))
								+ userID;
					} catch (Exception e) {
						// Ignore
					}
				}

			} else {
				Set names = request.getParameterNames();
				Iterator nameIterator = names.iterator();
				while (nameIterator.hasNext()) {
					String name = (String) nameIterator.next();
					if (name.startsWith("jg_")) {
						if (simServer.isWebJoinActive()) {
							SimulationInfo sim = null;
							try {
								int uniqSimID = Integer.parseInt(name
										.substring(3));
								int maxScheduled = simServer
										.getMaxAgentScheduled();
								int scheduledCount;
								agentID = Integer.parseInt(request
										.getParameter("agent_no"))
										+ userID;
								sim = simServer.getSimulationInfo(uniqSimID);
								if (sim == null) {
									message = "Could not find game with id "
											+ uniqSimID;
									break;
								} else if (sim.isParticipant(agentID)) {
									message = "Agent "
											+ simServer.getUserName(sim,
													agentID)
											+ " is already participating in game "
											+ getSimIDAsString(sim);
									break;
								} else if (sim.isFull()) {
									message = "Game " + getSimIDAsString(sim)
											+ " is already full";
									break;
								} else if (maxScheduled > 0
										&& ((scheduledCount = simServer
												.getAgentScheduledCount(agentID)) >= maxScheduled)) {
									message = "Your agent is already scheduled in "
											+ scheduledCount
											+ " games. "
											+ "Please do not schedule your agent in many games "
											+ "in advanced since it makes it hard for other "
											+ "teams to practice.";

								} else {
									Event event = new Event(sim.getID(),
											agentID);
									addEvent(event);
									// Add support for simulation roles. FIX
									// THIS!!! TODO
									simServer.joinSimulation(uniqSimID,
											agentID, null);
									message = event.waitForResult();
									if (message == null) {
										removeEvent(event);
										message = "Failed to join game "
												+ getSimIDAsString(sim);
									}
								}
							} catch (Exception e) {
								message = "Could not join game "
										+ getSimIDAsString(sim) + ": "
										+ e.getMessage();
								log.log(Level.WARNING, message, e);
							}
						} else {
							message = "<font color=red>Could not join the game at "
									+ "this time. Please try again later.</font>\r\n";
							break;
						}
					}
				}
			}
		}
		displayPage(userName, userID, agentID, response, simType, message);
	}

	private String getSimIDAsString(SimulationInfo info) {
		if (info == null) {
			return "";
		}

		int gid = info.getSimulationID();
		return gid <= 0 ? "starting at "
				+ infoServer.getServerTimeAsString(info.getStartTime())
				: Integer.toString(gid);
	}

	private void displayPage(String userName, int userID, int agentID,
			HttpResponse response, String simType, String message)
			throws HttpException, IOException {
		String serverMessage = simServer.getServerMessage();
		String timeLimitedMessage = this.timeLimitedMessage;
		Competition currentComp = simServer.getCurrentCompetition();
		boolean allowJoin = currentComp == null && simServer.isWebJoinActive();
		SimulationInfo[] simulations = simServer.getComingSimulations();
		int simulationsLen = simulations == null ? 0 : simulations.length;
		long currentTime = infoServer.getServerTimeMillis();
		String serverName = simServer.getServerName();

		String title = "Coming Games at " + serverName;
		HtmlWriter page = new HtmlWriter();
		page
				.pageStart(title)
				.h2(title)
				.text(
						"The coming game page is used "
								+ "to view and create TAC games. To view games click "
								+ "<a href='../viewer/' target=tacviewer>"
								+ "<b>Launch Game Viewer</b></a> (requires <a href='http://java.sun.com/plugin/' target='_top'>J2SDK 1.4 Plugin</a>). ");

		if (!InfoServer.ALLOW_SIM_TYPE || simType == null) {
			simType = infoServer.getDefaultSimulationType();
		}

		if (allowJoin && (simulationsLen <= MAX_VISIBLE_SIMULATIONS)) {
			// Do not show the create simulation button if there are too
			// many coming simulations (the newly created simulations will
			// not be visible anyway).
			page.text("To create a game, click on the "
					+ "<b>Create Game</b> button below.");
		}
		page.p();
		if (serverMessage != null) {
			page.text(serverMessage).p();
		}

		page.text("<hr noshade color='#202080'>\r\n");

		if (timeLimitedMessage != null) {
			if (timeLimit <= currentTime) {
				// Time to remove message
				this.timeLimitedMessage = null;
			} else {
				page.text(timeLimitedMessage).p();
			}
		}

		if (message != null) {
			page.h3(message);
		}

		if (allowJoin) {
			page.text("<form method=post>");
		}

		if (simulationsLen == 0) {
			page.text("<p>Current server time is ").text(
					infoServer.getServerTimeAsString(currentTime)).text(
					"<p>No games scheduled\r\n");
		} else {
			String columnStart;
			Competition nextCompetition = null;
			String nextCompetitionStarts = null;
			int numberOfSimulations = simulationsLen;
			int minAgentID = userID;
			int maxAgentID = minAgentID + 10;
			int startSimulation = -1, endSimulation = -1;
			if (currentComp != null) {
				page.text("<p><h3>Playing competition ").text(
						currentComp.getName());
				if (currentComp.hasSimulationID()) {
					page.text(" (game ").text(
							currentComp.getStartSimulationID()).text(" - ")
							.text(currentComp.getEndSimulationID()).text(')');
				}
				page.text("</h3>\r\n").text("<em>").text(
						"Competition started at ").text(
						infoServer.getServerTimeAsString(currentComp
								.getStartTime())).text(" and ends at ").text(
						infoServer.getServerTimeAsString(currentComp
								.getEndTime())).text(".</em>\r\n");
				endSimulation = currentComp.getEndUniqueID();
			} else {
				nextCompetition = simServer.getNextCompetition();
				if (nextCompetition != null) {
					nextCompetitionStarts = infoServer
							.getServerTimeAsString(nextCompetition
									.getStartTime());
					page.text("<b>Next competition '").text(
							nextCompetition.getName()).text("' begins at ")
							.text(nextCompetitionStarts).text("</b><p>\r\n");
					startSimulation = nextCompetition.getStartUniqueID();
					endSimulation = nextCompetition.getEndUniqueID();
				}
			}

			if (allowJoin) {
				page.text(
						"Select agent for joining: "
								+ "<select name=agent_no><option value=0>")
						.text(userName);
				int id = agentID;
				if (id > 0) {
					id = id - userID;
					if (id < 0 || id > 10) {
						id = 0;
					}
				}
				for (int i = 1; i < 11; i++) {
					page.text("<option value=").text(i);
					if (i == id) {
						page.text(" selected");
					}
					page.text('>').text(userName).text(i - 1).text(
							"</option>\r\n");
				}
				page.text("</select>");
			}
			page.text("<p>Current server time is ").text(
					infoServer.getServerTimeAsString(currentTime));
			page.text("\r\n<table border=1><tr><th>ID</th><th>Time</th>"
					+ "<th>Type</th>"
					+ "<th>Participants</th><th>Status</th><th>Join</th>"
					+ "</tr\r\n>");
			if (numberOfSimulations > MAX_VISIBLE_SIMULATIONS) {
				numberOfSimulations = MAX_VISIBLE_SIMULATIONS;
			}
			for (int i = 0, n = numberOfSimulations; i < n; i++) {
				SimulationInfo simulation = simulations[i];
				int uniqSimulationID = simulation.getID();
				int simulationID = simulation.getSimulationID();
				boolean isRunning = simulation.getStartTime() < currentTime;
				if (isRunning) {
					columnStart = "<td bgcolor='#e0e0ff'>";
				} else {
					columnStart = "<td>";

					// The simulation can only start a competition if the
					// simulation has not already started
					if (startSimulation == uniqSimulationID
							&& nextCompetition != null) {
						page
								.text(
										"<tr><td bgcolor='#e0e0ff' colspan=6>&nbsp;</td></tr>"
												+ "<tr><td colspan=6 align=center>"
												+ "<font size=+1 color='#800000'><b>Competition ")
								.text(nextCompetition.getName())
								.text(" begins");
						if (nextCompetitionStarts != null) {
							page.text(" (").text(nextCompetitionStarts).text(
									')');
						}
						page.text("</b></font></td></tr\r\n>"
								+ "<tr><td bgcolor='#e0e0ff' colspan=6>&nbsp;"
								+ "</td></tr>");
					}
				}
				page.text("<tr>").text(columnStart);
				if (simulationID > 0) {
					page.text(simulationID);
				} else {
					page.text("&nbsp;");
				}
				page.text("</td>").text(columnStart);
				appendTimeMillis(page, simulation.getStartTime()).text("- ");
				appendTimeMillis(page, simulation.getEndTime()).text("</td>")
						.text(columnStart);
				page
						.text(
								simServer.getSimulationTypeName(simulation
										.getType())).text("</td>").text(
								columnStart);
				for (int p = 0, np = simulation.getParticipantCount(); p < np; p++) {
					int participant = simulation.getParticipantID(p);
					if (p > 0) {
						page.text(", ");
					}
					if (participant >= minAgentID && participant <= maxAgentID) {
						page.text("<font size=+1 color='#800000'><b>").text(
								simServer.getUserName(simulation, participant))
								.text("</b></font>");
					} else if (participant < 0) {
						page.tag("em").text(
								simServer.getUserName(simulation, participant))
								.tagEnd("em");
					} else {
						page.text(simServer
								.getUserName(simulation, participant));
					}
				}
				page.text("&nbsp;</td>").text(columnStart).text(
						isRunning ? "Running" : "Coming").text("</td>").text(
						columnStart);
				if (isRunning || !allowJoin || simulation.isFull()) {
					page.text("&nbsp;");
				} else {
					page.text("<input type=submit value='Join' name='jg_")
							.text(uniqSimulationID).text("'>");
				}
				page.text("</td></tr\r\n>");
				if (endSimulation == uniqSimulationID) {
					page
							.text("<tr><td bgcolor='#e0e0ff' colspan=6>&nbsp;</td></tr>"
									+ "<tr><td colspan=6 align=center>"
									+ "<font size=+1 color='#800000'><b>Competition ");
					if (currentComp != null) {
						page.text(currentComp.getName());
					} else if (nextCompetition != null) {
						page.text(nextCompetition.getName());
					}
					page.text(" ends</b></font></td></tr\r\n>"
							+ "<tr><td bgcolor='#e0e0ff' colspan=6>&nbsp;"
							+ "</td></tr>");
				}
			}
			page.text("</table>");
			if (numberOfSimulations < simulationsLen) {
				page.text(
						"<br><em>(Only showing the first "
								+ MAX_VISIBLE_SIMULATIONS + " of the coming ")
						.text(simulationsLen).text(" games)</em>");
			}
		}

		// Only allow creation of new simulations if not in a competition
		if (allowJoin && (simulationsLen <= MAX_VISIBLE_SIMULATIONS)) {
			String[] simTypes = InfoServer.ALLOW_SIM_TYPE ? simServer
					.getSimulationTypes() : null;
			page.p();
			if (!InfoServer.ALLOW_SIM_TYPE || simTypes == null
					|| simTypes.length == 0) {
				page.text("<input type=hidden value='" + simType
						+ "' name='simType'>");
			} else {
				page.text("<select name='simType'>");
				for (int i = 0, n = simTypes.length; i < n; i++) {
					String type = simTypes[i];
					page.text("<option value='").text(type).text('\'');
					if (type.equals(simType)) {
						page.text(" selected");
					}
					page.text('>').text(simServer.getSimulationTypeName(type))
							.text("</option>\r\n");
				}
				page.text("</select>\r\n");
			}
			page.text("<input type=submit value='Create Game' "
					+ "name='createsim'>\r\n</form>\r\n");
		}
		page.text("<p><hr noshade color='#202080'>\r\n"
				+ "<center><font face='Arial,Helvetica,sans-serif' size='-2'>"
				+ infoServer.getServerType() + " " + infoServer.getVersion()
				+ "</font></center>\r\n");

		page.close();

		ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
		page.write(writer);
		response.setContentType(HttpFields.__TextHtml);
		response.setContentLength(writer.size());
		writer.writeTo(response.getOutputStream());
		response.commit();
	}

	private HtmlWriter appendTimeMillis(HtmlWriter page, long td) {
		td /= 1000;
		long sek = td % 60;
		long minutes = (td / 60) % 60;
		long hours = (td / 3600) % 24;
		if (hours < 10)
			page.text('0');
		page.text(hours).text(':');
		if (minutes < 10)
			page.text('0');
		page.text(minutes).text(':');
		if (sek < 10)
			page.text('0');
		page.text(sek);
		return page;
	}

	/*********************************************************************
	 * API towards SimServer and time out handling
	 **********************************************************************/

	private synchronized void addEvent(Event event) {
		eventQueue.add(event);
	}

	private synchronized void removeEvent(Event event) {
		int index = eventQueue.indexOf(event);
		if (index >= 0) {
			eventQueue.remove(index);
		}
	}

	public synchronized void simulationCreated(SimulationInfo info) {
		String simType = info.getType();
		for (int i = 0, n = eventQueue.size(); i < n; i++) {
			Event event = (Event) eventQueue.get(i);
			if (simType.equals(event.simType)) {
				String message = "A new game starting at "
						+ infoServer.getServerTimeAsString(info.getStartTime())
						+ " was created";
				eventQueue.remove(i);
				event.notifyResult(message);
				break;
			}
		}
	}

	public synchronized void simulationJoined(int uniqSimID, int agentID) {
		for (int i = 0, n = eventQueue.size(); i < n; i++) {
			Event event = (Event) eventQueue.get(i);
			if ((uniqSimID == event.simID) && (agentID == event.agentID)
					&& (event.simType == null)) {
				// Remove the HTTP request and the user information from the
				// queue
				eventQueue.remove(i);
				i--;
				n--;
				event.notifyResult(simServer.getUserName(null, agentID)
						+ " successfully joined coming game.");
			}
		}
	}

	private static class Event {
		public String simType;
		public int simID;
		public int agentID;

		private String message;

		public Event(String simType) {
			this.simType = simType;
		}

		public Event(int simID, int agentID) {
			this.simID = simID;
			this.agentID = agentID;
		}

		public synchronized void notifyResult(String message) {
			this.message = message;
			notify();
		}

		private synchronized String waitForResult() {
			if (message == null) {
				try {
					wait(TIMEOUT);
				} catch (Exception e) {
				}
			}
			return message;
		}
	}

} // ComingPage
