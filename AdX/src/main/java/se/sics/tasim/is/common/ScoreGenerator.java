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
 * ScoreGenerator
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Sun Jun 15 17:18:45 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.io.File;

public abstract class ScoreGenerator {

	private String serverName;
	private String competitionPath;

	public final void init(String serverName, String competitionPath) {
		if (this.serverName != null) {
			throw new IllegalStateException("already initialized");
		}
		this.serverName = serverName;
		this.competitionPath = competitionPath;
	}

	protected String getServerName() {
		return serverName;
	}

	// protected String getCompetitionPath() {
	// return competitionPath;
	// }

	protected String getScoreFileName() {
		return competitionPath + File.separatorChar + "index.html";
	}

	/**
	 * Returns all participants in the specified competition with respect to any
	 * parent competition.
	 */
	protected CompetitionParticipant[] getCombinedParticipants(
			Competition competition) {

		CompetitionParticipant[] parts = competition.getParticipants();
		if (!competition.hasParentCompetition() || parts == null) {
			return parts;
		}

		CompetitionParticipant[] participants = new CompetitionParticipant[parts.length];
		// This "lowest" competition should shield all other parent
		// competitions with regard to the agents
		for (int i = 0, n = parts.length; i < n; i++) {
			// Create cache object so we can change it freely
			participants[i] = new CompetitionParticipant(parts[i]);
		}

		Competition parentCompetition = competition.getParentCompetition();
		while (parentCompetition != null) {
			for (int i = 0, n = participants.length; i < n; i++) {
				CompetitionParticipant cpart = parentCompetition
						.getParticipantByID(participants[i].getID());
				if (cpart != null) {
					participants[i].addScore(cpart);
				}
			}
			// Continue and add scores from all parent competitions
			parentCompetition = parentCompetition.getParentCompetition();
		}

		return participants;
	}

	public abstract boolean createScoreTable(Competition competition,
			int simulationID);

}
