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
 * Competition
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Jan 15 13:10:44 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.botbox.util.ArrayUtils;
import se.sics.tasim.is.SimulationInfo;

public class Competition {

	private static final Logger log = Logger.getLogger(Competition.class
			.getName());

	public static final int WEEKEND_LOW = 1;
	public static final int NO_WEIGHT = 2;
	public static final int LOWEST_SCORE_FOR_ZERO = 1 << 6;

	private int id;
	private String name;
	private int parentID;
	private Competition parentCompetition;

	private long startTime;
	private long endTime;

	private int startUniqueID = -1;
	private int startPublicID = -1;
	private int simulationCount;

	private int flags;

	private double startWeight = 1.0;
	private String scoreClassName = null;

	private int startDay = -1;

	private CompetitionParticipant[] participants;
	private int participantCount;

	// These are global flags to force a weight for all
	// competitions. This should be done in a nicer manner. FIX THIS!!! TODO
	private static boolean forceWeightFlag = false;
	private static double forcedWeight;

	public Competition(int id, String name) {
		if (name == null || (name = name.trim()).length() < 2) {
			throw new IllegalArgumentException(
					"name must be at least 2 characters");
		}
		this.id = id;
		this.name = name;
	}

	public Competition(int id, String name, long startTime, long endTime,
			int startUniqueID, int simulationCount, double startWeight) {
		this(id, name);
		this.startTime = startTime;
		this.endTime = endTime;
		this.startUniqueID = startUniqueID;
		this.simulationCount = simulationCount;
		this.startWeight = startWeight;
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	// Allow SimServer to change the competition name
	void setName(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.name = name;
	}

	public int getStartUniqueID() {
		return startUniqueID;
	}

	public int getEndUniqueID() {
		return startUniqueID + simulationCount - 1;
	}

	public boolean isSimulationIncluded(int simulationUniqID) {
		return this.startUniqueID <= simulationUniqID
				&& (this.startUniqueID + simulationCount - 1) >= simulationUniqID
				&& this.startUniqueID >= 0;
	}

	public int getSimulationCount() {
		return simulationCount;
	}

	public boolean hasSimulationID() {
		return startPublicID >= 0;
	}

	public int getStartSimulationID() {
		return startPublicID;
	}

	public void setStartSimulationID(int simulationID) {
		this.startPublicID = simulationID;
	}

	public int getEndSimulationID() {
		return startPublicID + simulationCount - 1;
	}

    public boolean containsSimulation(int simulationID) {
        return getStartSimulationID() <= simulationID &&
               getEndSimulationID()   >= simulationID;
    }
    
	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void addSimulation(SimulationInfo info) {
		int id = info.getID();
		if (id < startUniqueID || startUniqueID < 0) {
			startUniqueID = id;
		}
		if (info.hasSimulationID()) {
			int simID = info.getSimulationID();
			if (simID < startPublicID || startPublicID < 0) {
				startPublicID = simID;
			}
		}
		long startTime = info.getStartTime();
		if (startTime < this.startTime || this.startTime <= 0) {
			this.startTime = startTime;
		}
		long endTime = info.getEndTime();
		if (endTime > this.endTime) {
			this.endTime = endTime;
		}
		simulationCount++;
	}

	public void addParticipant(CompetitionParticipant part) {
		if (participants == null) {
			participants = new CompetitionParticipant[6];
		} else if (participantCount == participants.length) {
			participants = (CompetitionParticipant[]) ArrayUtils.setSize(
					participants, participantCount + 6);
		}
		participants[participantCount++] = part;
	}

	public int getParticipantCount() {
		return participantCount;
	}

	public CompetitionParticipant getParticipantByID(int userID) {
		int index = CompetitionParticipant.indexOf(participants, 0,
				participantCount, userID);
		return index >= 0 ? participants[index] : null;
	}

	public CompetitionParticipant getParticipant(int index) {
		if (index < 0 || index >= participantCount) {
			throw new IndexOutOfBoundsException("Index: " + index + " Size: "
					+ participantCount);
		}
		return participants[index];
	}

	public CompetitionParticipant[] getParticipants() {
		if (participants != null && participantCount < participants.length) {
			// Trim the length
			participants = (CompetitionParticipant[]) ArrayUtils.setSize(
					participants, participantCount);
		}
		return participants;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	// -------------------------------------------------------------------
	// Score generation
	// -------------------------------------------------------------------

	public String getScoreClassName() {
		return scoreClassName;
	}

	public void setScoreClassName(String scoreClassName) {
		this.scoreClassName = scoreClassName;
	}

	// -------------------------------------------------------------------
	// Weight handling
	// -------------------------------------------------------------------

	public double getStartWeight() {
		return startWeight;
	}

	public void setStartWeight(double startWeight) {
		this.startWeight = startWeight;
	}

	public boolean isWeightUsed() {
		return (flags & NO_WEIGHT) == 0;
	}

	public static void setForcedWeight(double weight, boolean force) {
		forcedWeight = weight;
		forceWeightFlag = force;
	}

	public static boolean isWeightForced() {
		return forceWeightFlag;
	}

	public static double getForcedWeight() {
		return forcedWeight;
	}

	public double getWeight(int gameID) {
		if (forceWeightFlag)
			return forcedWeight;

		// Increasing weights are not yet implemented. FIX THIS!!! TODO
		return startWeight;
	}

	// -------------------------------------------------------------------
	// Support for splitted competitions
	// -------------------------------------------------------------------

	public boolean hasParentCompetition() {
		return parentID > 0;
	}

	public int getParentCompetitionID() {
		return parentID;
	}

	void setParentCompetitionID(int parentID) {
		this.parentID = parentID;
	}

	public boolean isParentCompetition(Competition competition) {
		if (competition == this) {
			return true;
		}
		if (parentCompetition != null) {
			return parentCompetition.isParentCompetition(competition);
		}
		return false;
	}

	public Competition getParentCompetition() {
		return parentCompetition;
	}

	void setParentCompetition(Competition competition) {
		this.parentCompetition = competition;
	}

	// -------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------

	public static int indexOf(Competition[] competitions, int competitionID) {
		if (competitions != null) {
			for (int i = 0, n = competitions.length; i < n; i++) {
				if (competitions[i].id == competitionID) {
					return i;
				}
			}
		}
		return -1;
	}

	public static int indexOf(Competition[] competitions, int start, int end,
			int competitionID) {
		for (int i = start; i < end; i++) {
			if (competitions[i].id == competitionID) {
				return i;
			}
		}
		return -1;
	}

} // Competition
