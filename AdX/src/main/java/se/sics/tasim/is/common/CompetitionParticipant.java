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
 * CompetitionParticipant
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Sun Jun 15 17:07:06 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.util.Comparator;
import com.botbox.util.ArrayUtils;

/**
 */
public class CompetitionParticipant {

	private static Comparator avgWeightedComparator;
	private static Comparator avgComparator;
	private static Comparator minAvgZeroComparator;
	private static Comparator minAvgZeroWeightedComparator;

	public static Comparator getAvgWeightedComparator() {
		if (avgWeightedComparator == null) {
			avgWeightedComparator = new Comparator() {
				public int compare(Object o1, Object o2) {
					double diff = (((CompetitionParticipant) o2)
							.getAvgWeightedScore() - ((CompetitionParticipant) o1)
							.getAvgWeightedScore());
					return diff < 0 ? -1 : (diff > 0 ? 1 : 0);
				}

				public boolean equals(Object obj) {
					return obj == this;
				}
			};
		}
		return avgWeightedComparator;
	}

	public static Comparator getAvgComparator() {
		if (avgComparator == null) {
			avgComparator = new Comparator() {
				public int compare(Object o1, Object o2) {
					double diff = (((CompetitionParticipant) o2).getAvgScore() - ((CompetitionParticipant) o1)
							.getAvgScore());
					return diff < 0 ? -1 : (diff > 0 ? 1 : 0);
				}

				public boolean equals(Object obj) {
					return obj == this;
				}
			};
		}
		return avgComparator;
	}

	public static Comparator getMinAvgZeroComparator() {
		if (minAvgZeroComparator == null) {
			minAvgZeroComparator = new Comparator() {
				public int compare(Object o1, Object o2) {
					CompetitionParticipant cp1 = (CompetitionParticipant) o1;
					CompetitionParticipant cp2 = (CompetitionParticipant) o2;
					double avg1 = cp1.getAvgScore();
					double avgzero1 = cp1.getAvgScoreWithoutZeroGames();
					double avg2 = cp2.getAvgScore();
					double avgzero2 = cp2.getAvgScoreWithoutZeroGames();
					if (avgzero1 < avg1) {
						avg1 = avgzero1;
					}
					if (avgzero2 < avg2) {
						avg2 = avgzero2;
					}
					double diff = avg2 - avg1;
					return diff < 0 ? -1 : (diff > 0 ? 1 : 0);
				}

				public boolean equals(Object obj) {
					return obj == this;
				}
			};
		}
		return minAvgZeroComparator;
	}

	public static Comparator getMinAvgZeroWeightedComparator() {
		if (minAvgZeroWeightedComparator == null) {
			minAvgZeroWeightedComparator = new Comparator() {
				public int compare(Object o1, Object o2) {
					CompetitionParticipant cp1 = (CompetitionParticipant) o1;
					CompetitionParticipant cp2 = (CompetitionParticipant) o2;
					double avg1 = cp1.getAvgWeightedScore();
					double avgzero1 = cp1.getAvgWeightedScoreWithoutZeroGames();
					double avg2 = cp2.getAvgWeightedScore();
					double avgzero2 = cp2.getAvgWeightedScoreWithoutZeroGames();
					if (avgzero1 < avg1) {
						avg1 = avgzero1;
					}
					if (avgzero2 < avg2) {
						avg2 = avgzero2;
					}
					double diff = avg2 - avg1;
					return diff < 0 ? -1 : (diff > 0 ? 1 : 0);
				}

				public boolean equals(Object obj) {
					return obj == this;
				}
			};
		}
		return minAvgZeroWeightedComparator;
	}

	private int id;
	private int parentID;
	private String name;

	private int flags;

	private double totalScore;
	private double wTotalScore;
	private int gamesPlayed;
	private int zGamesPlayed;
	private double wGamesPlayed;
	private double zwGamesPlayed;

	private double avgScore1;
	private double avgScore2;
	private double avgScore3;
	private double avgScore4;

	public CompetitionParticipant(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public CompetitionParticipant(CompetitionParticipant user) {
		this.id = user.id;
		this.name = user.name;
		this.parentID = user.parentID;
		this.flags = user.flags;
		addScore(user);
	}

	public int getID() {
		return id;
	}

	public boolean hasParent() {
		return parentID >= 0;
	}

	public int getParent() {
		return parentID;
	}

	void setParent(int parentID) {
		this.parentID = parentID;
	}

	public String getName() {
		return name;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public double getTotalScore() {
		return totalScore;
	}

	public double getAvgScore() {
		return gamesPlayed == 0 ? 0.0 : ((double) totalScore / gamesPlayed);
	}

	public double getAvgScoreWithoutZeroGames() {
		int games = gamesPlayed - zGamesPlayed;
		if (games <= 0) {
			return 0.0;
		}
		return ((double) totalScore / games);
	}

	public double getTotalWeightedScore() {
		return wTotalScore;
	}

	public double getAvgWeightedScore() {
		return wGamesPlayed == 0.0 ? 0.0 : (wTotalScore / wGamesPlayed);
	}

	public double getAvgWeightedScoreWithoutZeroGames() {
		double wgames = wGamesPlayed - zwGamesPlayed;
		return (wgames <= 0.0) ? 0.0 : (wTotalScore / wgames);
	}

	public int getGamesPlayed() {
		return gamesPlayed;
	}

	public int getZeroGamesPlayed() {
		return zGamesPlayed;
	}

	public double getWeightedGamesPlayed() {
		return wGamesPlayed;
	}

	public double getZeroWeightedGamesPlayed() {
		return zwGamesPlayed;
	}

	public double getAvgScore1() {
		return avgScore1;
	}

	public double getAvgScore2() {
		return avgScore2;
	}

	public double getAvgScore3() {
		return avgScore3;
	}

	public double getAvgScore4() {
		return avgScore4;
	}

	public void addScore(CompetitionParticipant user) {
		totalScore += user.totalScore;
		wTotalScore += user.wTotalScore;
		gamesPlayed += user.gamesPlayed;
		zGamesPlayed += user.zGamesPlayed;
		wGamesPlayed += user.wGamesPlayed;
		zwGamesPlayed += user.zwGamesPlayed;
	}

	public void addScore(int simulationID, double score, double weight,
			boolean isZeroGame) {
		totalScore += score;
		wTotalScore += score * weight;
		gamesPlayed++;
		wGamesPlayed += weight;
		if (isZeroGame) {
			zGamesPlayed++;
			zwGamesPlayed += weight;
		}
	}

	public void removeScore(int simulationID, double score, double weight,
			boolean isZeroGame) {
		totalScore -= score;
		wTotalScore -= score * weight;
		gamesPlayed--;
		wGamesPlayed -= weight;
		if (isZeroGame) {
			zGamesPlayed--;
			zwGamesPlayed -= weight;
		}
	}

	void setScores(double totalScore, double wTotalScore, int gamesPlayed,
			int zGamesPlayed, double wGamesPlayed, double zwGamesPlayed) {
		this.totalScore = totalScore;
		this.wTotalScore = wTotalScore;
		this.gamesPlayed = gamesPlayed;
		this.zGamesPlayed = zGamesPlayed;
		this.wGamesPlayed = wGamesPlayed;
		this.zwGamesPlayed = zwGamesPlayed;
	}

	void setAvgScores(double a1, double a2, double a3, double a4) {
		this.avgScore1 = a1;
		this.avgScore2 = a2;
		this.avgScore3 = a3;
		this.avgScore4 = a4;
	}

	void clearScores() {
		totalScore = 0L;
		wTotalScore = 0.0;
		gamesPlayed = 0;
		zGamesPlayed = 0;
		wGamesPlayed = 0.0;
		zwGamesPlayed = 0.0;

		avgScore1 = 0.0;
		avgScore2 = 0.0;
		avgScore3 = 0.0;
		avgScore4 = 0.0;
	}

	// -------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------

	public static int indexOf(CompetitionParticipant[] participants, int start,
			int end, int userID) {
		for (int i = start; i < end; i++) {
			if (participants[i].id == userID) {
				return i;
			}
		}
		return -1;
	}

} // CompetitionParticipant
