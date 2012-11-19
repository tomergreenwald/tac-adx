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
 * DefaultScoreGenerator
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Aug 05 13:35:28 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.util.FormatUtils;

/**
 */
public class DefaultScoreGenerator extends ScoreGenerator {

	private static final Logger log = Logger
			.getLogger(DefaultScoreGenerator.class.getName());

	private int agentsToAdvance = 0;
	private String advanceColor = null;
	private boolean isShowingCompetitionTimes = true;
	private boolean isShowingAllAgents = false;
	private boolean isShowingAvgScoreWhenWeighted = true;
	private boolean isShowingWeightedAvgScoreWithoutZeroGames = false;
	private boolean isShowingAvgScoreWithoutZeroGames = false;
	private boolean isShowingZeroGameAgents = true;
	private boolean isAddingLastUpdated = true;
	private boolean isAddingStatisticsLink = true;
	private boolean isIgnoringWeight = false;

	public DefaultScoreGenerator() {
	}

	// -------------------------------------------------------------------
	// Settings
	// -------------------------------------------------------------------

	public int getAgentsToAdvance() {
		return agentsToAdvance;
	}

	public void setAgentsToAdvance(int agentsToAdvance) {
		this.agentsToAdvance = agentsToAdvance;
	}

	public String getAdvanceColor() {
		return advanceColor;
	}

	public void setAdvanceColor(String advanceColor) {
		this.advanceColor = advanceColor;
	}

	public boolean isShowingCompetitionTimes() {
		return isShowingCompetitionTimes;
	}

	public void setShowingCompetitionTimes(boolean isShowingCompetitionTimes) {
		this.isShowingCompetitionTimes = isShowingCompetitionTimes;
	}

	public boolean isShowingAllAgents() {
		return isShowingAllAgents;
	}

	public void setShowingAllAgents(boolean isShowingAllAgents) {
		this.isShowingAllAgents = isShowingAllAgents;
	}

	public boolean isShowingAverageScoreWhenWeighted() {
		return isShowingAvgScoreWhenWeighted;
	}

	public void setShowingAverageScoreWhenWeighted(
			boolean isShowingAvgScoreWhenWeighted) {
		this.isShowingAvgScoreWhenWeighted = isShowingAvgScoreWhenWeighted;
	}

	public boolean isShowingAverageScoreWithoutZeroGames() {
		return isShowingAvgScoreWithoutZeroGames;
	}

	public void setShowingAverageScoreWithoutZeroGames(
			boolean isShowingAvgScoreWithoutZeroGames) {
		this.isShowingAvgScoreWithoutZeroGames = isShowingAvgScoreWithoutZeroGames;
	}

	public boolean isShowingWeightedAverageScoreWithoutZeroGames() {
		return isShowingWeightedAvgScoreWithoutZeroGames;
	}

	public void setShowingWeightedAverageScoreWithoutZeroGames(
			boolean isShowingWeightedAvgScoreWithoutZeroGames) {
		this.isShowingWeightedAvgScoreWithoutZeroGames = isShowingWeightedAvgScoreWithoutZeroGames;
	}

	public boolean isShowingZeroGameAgents() {
		return isShowingZeroGameAgents;
	}

	public void setShowingZeroGameAgents(boolean isShowingZeroGameAgents) {
		this.isShowingZeroGameAgents = isShowingZeroGameAgents;
	}

	public boolean isAddingLastUpdated() {
		return isAddingLastUpdated;
	}

	public void setAddingLastUpdated(boolean isAddingLastUpdated) {
		this.isAddingLastUpdated = isAddingLastUpdated;
	}

	public boolean isAddingStatisticsLink() {
		return isAddingStatisticsLink;
	}

	public void setAddingStatisticsLink(boolean isAddingStatisticsLink) {
		this.isAddingStatisticsLink = isAddingStatisticsLink;
	}

	public boolean isIgnoringWeight() {
		return isIgnoringWeight;
	}

	public void setIgnoringWeight(boolean isIgnoringWeight) {
		this.isIgnoringWeight = isIgnoringWeight;
	}

	// -------------------------------------------------------------------
	// Score generation
	// -------------------------------------------------------------------

	// Always create the score table... - only works with competitions!!!
	public boolean createScoreTable(Competition competition, int gameID) {
		String scoreFile = getScoreFileName();
		try {
			// Buffer the complete page first because we do not want to
			// overwrite the file in case something goes wrong.
			String serverName = getServerName();
			boolean isWeightUsed = !isIgnoringWeight
					&& competition.isWeightUsed();
			boolean isShowingZeroGameAgents = this.isShowingAllAgents
					|| this.isShowingZeroGameAgents;
			StringBuffer page = new StringBuffer();
            page.append("<html><head><title>TAC SIM - Score Page for ").append(
					competition.getName()).append(
					"</title></head>\r\n<body>\r\n");

			CompetitionParticipant[] users = getCombinedParticipants(competition);
			if (users != null) {
				users = (CompetitionParticipant[]) users.clone();
				Arrays.sort(users, getComparator(isWeightUsed));

				page.append("<h3>Scores");
				if (competition != null) {
					page.append(" for ").append(competition.getName());
					if (competition.hasSimulationID()) {
						page.append(" (game ");
						addCompetitionSimulationIDs(page, competition);
						page.append(')');
					}
				}
				if (serverName != null) {
					page.append(" at ").append(serverName);
				}
				page.append("</h3>\r\n");

				// Competition start and end time
				if (isShowingCompetitionTimes) {
					page.append("<em>");
					if (competition.hasSimulationID()
							&& (gameID >= competition.getStartSimulationID())) {
						page.append("Competition started at ");
					} else {
						page.append("Competition starts at ");
					}
					page.append(getRootStartTime(competition));
					if (competition.hasSimulationID()
							&& (gameID >= competition.getEndSimulationID())) {
						page.append(" and ended at ");
					} else {
						page.append(" and ends at ");
					}
					page.append(
							InfoServer.getServerTimeAsString(competition
									.getEndTime())).append(".</em><br>\r\n");
				}

				page.append("<table border=1>\r\n"
						+ "<tr><th>Position</th><th>Agent</th>");
				if (isWeightUsed) {
					page.append("<th>Average Weighted Score</th>");
					if (isShowingWeightedAvgScoreWithoutZeroGames) {
						page.append("<th>Average Weighted Score - Zero</th>");
					}
				}
				if (isShowingAvgScoreWhenWeighted || !isWeightUsed) {
					page.append("<th>Average Score</th>");
					if (isShowingAvgScoreWithoutZeroGames) {
						page.append("<th>Average Score - Zero</th>");
					}
				}
				page.append("<th>Games Played</th>");
				if (isShowingZeroGameAgents) {
					page.append("<th>Zero Games</th>");
				}
				page.append("</tr>\r\n");
				int pos = 1;
				for (int i = 0, n = users.length; i < n; i++) {
					CompetitionParticipant usr = users[i];
					if (isShowingAllAgents
							|| (isShowingZeroGameAgents ? (usr.getGamesPlayed() > 0)
									: (usr.getGamesPlayed() > usr
											.getZeroGamesPlayed()))) {
						String userName = createUserName(usr, pos, n);
						String rankColor = getRankColor(usr, pos, n);
						String color = getAgentColor(usr, pos, n);
						String tdrank, td, tdright;
						if (color != null) {
							td = "<td bgcolor='" + color + "'>";
							tdright = "<td bgcolor='" + color
									+ "' align=right>";
						} else {
							td = "<td>";
							tdright = "<td align=right>";
						}
						if (rankColor != null) {
							tdrank = "<td bgcolor='" + rankColor + "'>";
						} else {
							tdrank = td;
						}

						page.append("<tr>").append(tdrank).append(pos++)
								.append("</td>").append(td).append(userName);
						if (isWeightUsed) {
							page.append("</td>").append(tdright).append(
									FormatUtils.formatAmount((long) usr
											.getAvgWeightedScore()));
							if (isShowingWeightedAvgScoreWithoutZeroGames) {
								page
										.append("</td>")
										.append(tdright)
										.append(
												FormatUtils
														.formatAmount((long) usr
																.getAvgWeightedScoreWithoutZeroGames()));

							}
						}
						if (isShowingAvgScoreWhenWeighted || !isWeightUsed) {
							page.append("</td>").append(tdright).append(
									FormatUtils.formatAmount((long) usr
											.getAvgScore()));
							if (isShowingAvgScoreWithoutZeroGames) {
								page
										.append("</td>")
										.append(tdright)
										.append(
												FormatUtils
														.formatAmount((long) usr
																.getAvgScoreWithoutZeroGames()));
							}
						}
						page.append("</td>").append(tdright).append(
								usr.getGamesPlayed()).append("</td>");
						if (isShowingZeroGameAgents) {
							page.append(tdright).append(
									usr.getZeroGamesPlayed()).append("</td>");
						}
						page.append("</tr>\r\n");
					}
				}
				page.append("</table>\r\n");
				addPostInfo(page);
				if (isShowingZeroGameAgents) {
					page
							.append("<p>"
									+ "<b>Zero Games"
									+ "</b> is the number of games that resulted in a score "
									+ "of zero (probably due to inactivity).");
				}
				if (isShowingAvgScoreWithoutZeroGames
						|| (isWeightUsed && isShowingWeightedAvgScoreWithoutZeroGames)) {
					page.append(isShowingZeroGameAgents ? "<br>" : "<p>");
					page.append("<b>- Zero</b> is the score "
							+ "without zero score games");
				}
				page.append("<br>\r\n");
			} else {
				page.append("No TAC agents registered\r\n");
			}
			if (isAddingLastUpdated) {
				addLastUpdated(page);
			}
			page.append("</body>\r\n" + "</html>\r\n");

			FileWriter out = new FileWriter(scoreFile);
			out.write(page.toString());
			out.close();
			return true;

		} catch (Exception e) {
			log.log(Level.SEVERE, "could not create score page for game "
					+ gameID + " in " + scoreFile, e);
			return false;
		}
	}

	private void addCompetitionSimulationIDs(StringBuffer page,
			Competition competition) {
		Competition parentCompetition = competition.getParentCompetition();
		if (parentCompetition != null && parentCompetition.hasSimulationID()) {
			addCompetitionSimulationIDs(page, parentCompetition);
			page.append(", ");
		}
		page.append(competition.getStartSimulationID()).append(" - ").append(
				competition.getEndSimulationID());
	}

	private String getRootStartTime(Competition competition) {
		Competition parentCompetition = competition.getParentCompetition();
		if (parentCompetition != null) {
			return getRootStartTime(parentCompetition);
		}
		return InfoServer.getServerTimeAsString(competition.getStartTime());
	}

	// -------------------------------------------------------------------
	// Utility methods - might be overriden by subclasses to give other
	// features
	// -------------------------------------------------------------------

	protected Comparator getComparator(boolean isWeightUsed) {
		return isWeightUsed ? CompetitionParticipant.getAvgWeightedComparator()
				: CompetitionParticipant.getAvgComparator();
	}

	protected String getRankColor(CompetitionParticipant agent, int pos,
			int numberOfAgents) {
		return null;
	}

	protected String getAgentColor(CompetitionParticipant agent, int pos,
			int numberOfAgents) {
		if (pos <= agentsToAdvance) {
			return advanceColor;
		}
		return null;
	}

	protected String createUserName(CompetitionParticipant usr, int pos,
			int numberOfAgents) {
		// Assume that statistics page exists!
		if (isAddingStatisticsLink) {
			return "<a href='" + usr.getID() + ".html'>"
					+ getAgentNameStyle(usr.getName(), pos, numberOfAgents)
					+ "</a>";
		}
		return usr.getName();
	}

	protected String getAgentNameStyle(String agentName, int pos,
			int numberOfAgents) {
		return agentName;
	}

	protected void addLastUpdated(StringBuffer page) {
		page.append("<p><hr>\r\n" + "<em>Table last updated ");
		SimpleDateFormat dFormat = new SimpleDateFormat("dd MMM HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		dFormat.setTimeZone(new java.util.SimpleTimeZone(0, "UTC"));
		page.append(dFormat.format(date));
		page.append("</em>\r\n");
	}

	protected void addPostInfo(StringBuffer page) {
	}

} // DefaultScoreGenerator
