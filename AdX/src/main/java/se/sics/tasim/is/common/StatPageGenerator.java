/**
 * SICS TAC Server - InfoServer
 * http://www.sics.se/tac/	  tac-dev@sics.se
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
 * StatPageGenerator
 *
 * Generates the statistics page for specific agents
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : 12 April, 2002
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *	     $Revision: 3981 $
 */

//Modded by BC, UMich to turn longs into doubles (pertaining to score)

package se.sics.tasim.is.common;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import com.botbox.html.HtmlWriter;
import com.botbox.util.ArrayUtils;
import se.sics.isl.db.DBMatcher;
import se.sics.isl.db.DBResult;
import se.sics.isl.db.DBTable;
import se.sics.isl.util.FormatUtils;

public class StatPageGenerator {

	private static final Logger log = Logger.getLogger(StatPageGenerator.class
			.getName());

	private final static int WIDTH = 580;
	private final static int HEIGHT = 320;
	private final static int MX = 24;
	private final static int MY = 20;
	private final static int TXT = 40;
	private final static Color LIGHTGRAY = new Color(210, 210, 210);

	private static boolean hasGUI = true;
	private static int averageCount = 15;

	public static boolean createImage(String file, double[] results) {
		return createImage(file, results, 0, results.length);
	}

	public static boolean createImage(String file, double[] scores, int start,
			int end) {
		if (!hasGUI || end <= start) {
			return false;
		}

		float avgPos = averageCount / 2f;
		int noLines = 10;

		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		int len = end - start;
		for (int i = start; i < end; i++) {
			double score = scores[i];
			if (max < score)
				max = score;
			if (min > score)
				min = score;
		}

		if (max < 100)
			max = 100;
		if (min > 0)
			min = 0;

		long tagSpacing = 2000;
		double interval = max - min;
		int zeroLine = min < 0 ? (int) (20 + ((HEIGHT - 40) * max) / interval)
				: HEIGHT - 20;
		// log.finest("Max: " + max + " Min: " + min + " Interval: " + interval
		// +
		// " Zero: " + zeroLine);

		int noZeros = (int) (Math.log(interval) / Math.log(10));

		int ceil = (int) Math.ceil(interval / Math.pow(10, noZeros));
		long newInterval = (long) (ceil * Math.pow(10, noZeros));
		// System.out.println("OldInterval: " + interval);

		if (ceil > 4) {
			noLines = ceil;
		} else if (ceil > 2) {
			noLines = ceil * 2;
		} else {
			noLines = ceil * 4;
		}

		// Cut down unused areas (e.g. if newInterval is 20000 due to old was
		// 10010
		// then cut down to something reasonalby nice...
		tagSpacing = newInterval / noLines;
		while ((newInterval - tagSpacing) > interval) {
			newInterval = newInterval - tagSpacing;
			noLines--;
		}
		interval = newInterval;
		// System.out.println("NewInterval: " + interval);
		// System.out.println("Ceil: " + ceil);
		// System.out.println("No Lines: " + noLines);

		float resolution = (HEIGHT - 40) / (1.0f * noLines * tagSpacing);
		float xResolution = 1.0f * (WIDTH - 2 * MX - TXT) / len;
		BufferedImage image;
		Graphics2D g2d;
		// Try to create the buffered image. If no graphics environment is
		// available, this will fail with an internal error which means
		// that no statistics images can be generated.
		try {
			image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			g2d = image.createGraphics();
		} catch (InternalError e) {
			log.log(Level.SEVERE, "could not access graphics environment", e);
			hasGUI = false;
			return false;
		}

		g2d.setBackground(Color.white);
		g2d.setColor(Color.black);
		g2d.clearRect(0, 0, WIDTH, HEIGHT);

		int low = (int) (((noLines + 1) * min) / interval);
		int hi = (int) (((noLines + 1) * max) / interval);

		for (long i = low; i <= hi; i++) {
			long score = i * tagSpacing;
			int ypos = zeroLine - (int) (resolution * score);
			g2d.setColor(LIGHTGRAY);
			g2d.drawLine(TXT + MX, ypos, WIDTH - MX, ypos);
			g2d.setColor(Color.black);
			g2d.drawString(FormatUtils.formatAmount((long) i * tagSpacing), 4,
					4 + ypos);
		}

		g2d.drawLine(TXT + MX, MY, TXT + MX, HEIGHT - MY);
		g2d.drawLine(TXT + MX, zeroLine, WIDTH - MX, zeroLine);

		g2d.drawLine(TXT + MX, MY, TXT + MX + 4, MY + 6);
		g2d.drawLine(TXT + MX, MY, TXT + MX - 4, MY + 6);

		g2d.drawLine(WIDTH - MX, zeroLine, WIDTH - MX - 6, zeroLine + 4);
		g2d.drawLine(WIDTH - MX, zeroLine, WIDTH - MX - 6, zeroLine - 4);

		// This should probably generate maximum 100 games (100 latest???)
		double sc;
		float med = 0;
		float oldMed = 0;
		int medP = -1;
		int oldMedP = -1;
		for (int i = 0, n = len; i < n; i++) {
			int xp = (int) (TXT + MX + i * xResolution);
			sc = scores[start + i];
			med += sc;

			if (i >= averageCount) {
				oldMed = med;
				med -= scores[start + i - averageCount];
				oldMedP = medP;
				medP = zeroLine - (int) ((resolution * med) / averageCount);
				if (oldMedP == -1)
					oldMedP = medP;
				g2d.setColor(Color.red);
				g2d.drawLine(xp - (int) ((avgPos + 1) * xResolution), oldMedP,
						xp - (int) (avgPos * xResolution), medP);
			}
			if (sc < min) {
				sc = min;
			}
			int yp = zeroLine - (int) (resolution * sc);

			// System.out.println("Score " + i + " = " + sc);
			g2d.setColor(Color.black);
			g2d.drawLine(xp - 1, yp - 1, xp + 1, yp + 1);
			g2d.drawLine(xp + 1, yp - 1, xp - 1, yp + 1);
		}

		try {
			return ImageIO.write(image, "png", new File(file));
		} catch (Exception ioe) {
			log.log(Level.SEVERE, "could not write statistics image " + file,
					ioe);
			return false;
		}
	}

	// Only works for competitions!!!
	public static boolean generateStatisticsPage(DBTable compResults,
			String path, String urlGamePath, Competition competition,
			CompetitionParticipant user, boolean generateImage) {
		if (competition == null)
			return false;

		log.fine("Generating statistics page for " + user.getName());
		try {
			String file = path + user.getID() + ".html";
			HtmlWriter out = new HtmlWriter(new BufferedWriter(new FileWriter(
					file)));

			ArrayList comps = null;
			if (competition.hasParentCompetition()) {
				// Calculate the full participant score for the competition
				// chain
				Competition parentCompetition = competition
						.getParentCompetition();
				// Create cache object for the user scores so we can change it
				// freely
				user = new CompetitionParticipant(user);
				// Create a competition stack to get the latest games in correct
				// order
				comps = new ArrayList();
				comps.add(competition);
				while (parentCompetition != null) {
					CompetitionParticipant u = parentCompetition
							.getParticipantByID(user.getID());
					if (u != null) {
						user.addScore(u);
					}
					comps.add(parentCompetition);
					parentCompetition = parentCompetition
							.getParentCompetition();
				}
			}

			int numGames = competition.getSimulationCount();
			int lastNumberOfGames = 0;
			int[] gameIDs = new int[numGames];
			double[] scores = new double[numGames];
			int[] gameFlags = new int[numGames];
			int[] scratchedGames = null;
			int scratchedCount = 0;
			long zeroScore = 0L;
			int zeroScoreCount = 0;
			do {
				Competition currentCompetition = comps != null ? (Competition) comps
						.remove(comps.size() - 1)
						: competition;

				numGames = currentCompetition.getSimulationCount();
				if (numGames > (gameIDs.length - lastNumberOfGames)) {
					gameIDs = ArrayUtils.setSize(gameIDs, lastNumberOfGames
							+ numGames);
					scores = ArrayUtils.setSize(scores, lastNumberOfGames
							+ numGames);
					gameFlags = ArrayUtils.setSize(gameFlags, lastNumberOfGames
							+ numGames);
				}

				// competitionResultTable:
				// id(int),simid(int),competition(int),participantid(int),
				// participantrole(int),flags(int),score(long),weight(double)
				DBMatcher dbm = new DBMatcher();
				dbm.setInt("competition", currentCompetition.getID());
				dbm.setInt("participantid", user.getID());
				dbm.setLimit(numGames);
				DBResult res = compResults.select(dbm);
				while (res.next()) {
					int gameID = res.getInt("simid");
					int flags = res.getInt("flags");
					double agentScore = res.getDouble("score");
					if ((flags & SimServer.SIMULATION_SCRATCHED) == 0) {
						gameIDs[lastNumberOfGames] = gameID;
						gameFlags[lastNumberOfGames] = flags;
						scores[lastNumberOfGames] = agentScore;
						lastNumberOfGames++;

						if ((agentScore == 0)
								|| (flags & SimServer.ZERO_GAME) != 0) {
							zeroScore += agentScore;
							zeroScoreCount++;
						}
					} else {
						// This simulation has been scratched.
						if (scratchedGames == null) {
							scratchedGames = new int[numGames
									- lastNumberOfGames];
						} else if (scratchedCount == scratchedGames.length) {
							scratchedGames = ArrayUtils.setSize(scratchedGames,
									scratchedCount + 10);
						}
						scratchedGames[scratchedCount++] = gameID;
					}
				}
			} while ((comps != null) && (comps.size() > 0));

			// scores = ArrayUtils.setSize(scores, lastNumberOfGames);

			String title = "Statistics for " + user.getName()
					+ " in competition " + competition.getName();
			boolean isWeightUsed = competition.isWeightUsed();

			out.pageStart(title);
			out.h3(title);
			if (generateImage) {
				if (lastNumberOfGames == 0) {
					// No image if no games
					generateImage = false;
				} else if (hasGUI) {
					out
							.table("border=0 cellpadding=1 cellspacing=0 bgcolor='#0'");
					out.tr().td(
							"<img src='" + user.getID()
									+ "_gst.png' alt='Agent Scores'>");
					out.tableEnd();
					out.text("<em>Scores of the last " + lastNumberOfGames
							+ " games played</em><p>");
				} else {
					generateImage = false;
					log
							.severe("No graphics environment available. Could not generate "
									+ "statistics image for agent "
									+ user.getName());
				}
			}

			out.table("border=1").tr();
			if (isWeightUsed) {
				out.th("Avg Weighted Score");
			}
			out.th("Avg Score").th("Avg Score - Zero").th("Games Played").th(
					"Zero Games");
			out.tr();

			if (isWeightUsed) {
				out.td(toString(user.getAvgWeightedScore()), "align=right");
			}
			out.td(toString(user.getAvgScore()), "align=right");
			// DEBUG OUTPUT!!!
			if (user.getZeroGamesPlayed() != zeroScoreCount) {
				log.log(Level.SEVERE, "Competition " + competition.getID()
						+ ", participant " + user.getName() + " has "
						+ user.getZeroGamesPlayed() + " zero games but "
						+ " found " + zeroScoreCount + " zero games",
						new IllegalStateException("mismatching zero games"));
			}
			if ((competition.getFlags() & Competition.LOWEST_SCORE_FOR_ZERO) != 0) {
				// Special calculation because the lowest scores used for zero
				// games are not stored in the competition participant
				double baseScore = user.getTotalScore() - zeroScore;
				int games = user.getGamesPlayed() - zeroScoreCount;
				double avgScore = (games <= 0) ? 0.0
						: (((double) baseScore) / games);
				out.td(toString(avgScore), "align=right");
			} else {
				out.td(toString(user.getAvgScoreWithoutZeroGames()),
						"align=right");
			}
			out.td(Integer.toString(user.getGamesPlayed()), "align=right");
			out.td(Integer.toString(user.getZeroGamesPlayed()), "align=right");
			out.tableEnd();
			// out.text("<font size='-1'><em>The scores were calculated after "
			// + user.getGamesPlayed() + " games of which "
			// + user.getZeroGamesPlayed()
			// + " resulted in a zero score<br>");
			// out.text("-10 = without the 10 worst games, ");
			out.text("-Zero = without zero score games</em></font><p>\r\n");

			if (lastNumberOfGames > 0) {
				// Only generate table if at least one game has been played
				out.text("<b>The last ").text(lastNumberOfGames).text(
						" games played</b><br>");
				out.table("border=1");
				for (int i = 0; i < 4; i++) {
					out.th("Game").th("Score");
					if (i % 4 != 3) {
						out.th("&nbsp;", "bgcolor='#e0e0e0'");
					}
				}

				for (int i = 0; i < lastNumberOfGames; i++) {
					if (i % 4 == 0) {
						out.tr();
					} else {
						out.td("&nbsp;", "bgcolor='#e0e0e0'");
					}
					if (gameIDs[i] > 0) {
						String gameIDStr = Integer.toString(gameIDs[i]);
						out.td("", "align=right").text("<a href='").text(
								urlGamePath).text(gameIDStr).text("/'>").text(
								gameIDStr).text("</a>");
					} else {
						out.td("*[NOID]*", "align=right");
					}
					out.td("", "align=right");
					if (((gameFlags[i] & SimServer.ZERO_GAME) != 0)
							&& scores[i] < 0L) {
						// Zero game but lowest score is used instead of zero
						out.text("0 (");
						formatDouble(out, scores[i]);
						out.text(')');
					} else {
						formatDouble(out, scores[i]);
					}
				}
				out.tableEnd();
			}

			if (scratchedCount > 0) {
				// Games has been scratched
				out.text("<p><em>Scratched games: ");

				for (int i = 0; i < scratchedCount; i++) {
					if (i > 0) {
						out.text(", ");
					}
					out.text(scratchedGames[i]);
				}
				out.text("</em>");
			}
			out.pageEnd();
			out.close();

			// // Mark the user that it has a statistics page
			// user.setStatisticsFlag(TACUser.STATISTICS_EXISTS);

			if (generateImage) {
				String imageFile = path + user.getID() + "_gst.png";
				if (!createImage(imageFile, scores, 0, lastNumberOfGames)) {
					log.severe("could not create statistics image for agent "
							+ user.getName());
				}
			}
			return true;
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not create statistics for agent "
					+ user.getName(), e);
			return false;
		}
	}

	private static HtmlWriter formatDouble(HtmlWriter out, double score) {
		if (score < 0) {
			out.text("<font color=red>").text(
					FormatUtils.formatDouble(score, "&nbsp;")).text("</font>");
		} else {
			out.text(FormatUtils.formatDouble(score, "&nbsp;"));
		}
		return out;
	}

	private static String toString(double score) {
		return score < 0 ? ("<font color=red>"
				+ FormatUtils.formatDouble(score, "&nbsp;") + "</font>")
				: FormatUtils.formatDouble(score, "&nbsp;");
	}

	// -------------------------------------------------------------------
	// Test Main
	// -------------------------------------------------------------------

	// public static void main(String[] args) throws NumberFormatException {
	// long results[] = new long[250];
	// for (int i = 0; i < 250; i++) {
	// results[i] = (long) ((Math.random() * 30000) -
	// (Math.random() * 30000));
	// if (i > 200) results[i] = 0;
	// }
	// createImage("puck_gst.png", results);
	// }

}
