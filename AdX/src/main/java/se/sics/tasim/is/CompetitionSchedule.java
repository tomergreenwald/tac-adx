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
 * CompetitionSchedule
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Jun 13 21:58:45 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is;

import com.botbox.util.ArrayUtils;
import se.sics.isl.transport.Transportable;

/**
 */
public class CompetitionSchedule { // implements Transportable {

	private int id = -1;
	private int parentID = 0;
	private String name;
	private long startTime;
	private String simulationType;
	private String simulationParams;

	private int timeBetweenSimulations;

	private long[] reservationStartTime;
	private int[] reservationLength;
	private int reservationCount;

	private int simulationsBeforeReservation = 0;
	private int simulationsReservationLength;

	private boolean isSimulationsClosed;
	private int[][] simulationParticipants;
	private int[][] simulationRoles;
	private int simulationCount;

	private int[] participants;

	private float startWeight;
	private int flags;

	private String scoreClassName;

	public CompetitionSchedule(String name) {
		setName(name);
	}

	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public int getParentCompetitionID() {
		return parentID;
	}

	public void setParentCompetitionID(int parentID) {
		this.parentID = parentID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null || (name = name.trim()).length() < 2) {
			throw new IllegalArgumentException(
					"name must be at least 2 characters");
		}
		this.name = name;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public String getSimulationType() {
		return simulationType;
	}

	public void setSimulationType(String simulationType) {
		this.simulationType = simulationType;
	}

	public String getSimulationParams() {
		return simulationParams;
	}

	public void setSimulationParams(String simulationParams) {
		this.simulationParams = simulationParams;
	}

	public int getTimeBetweenSimulations() {
		return timeBetweenSimulations;
	}

	public void setTimeBetweenSimulations(int timeBetweenSimulations) {
		this.timeBetweenSimulations = timeBetweenSimulations;
	}

	public float getStartWeight() {
		return startWeight;
	}

	public void setStartWeight(float startWeight) {
		this.startWeight = startWeight;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public void validate() {
		if (startTime <= 0L) {
			throw new IllegalStateException("no start time");
		}
		if (name == null) {
			throw new IllegalStateException("no name specified");
		}
		if (id < 0) {
			throw new IllegalStateException("no id specified");
		}
		if (simulationCount == 0) {
			throw new IllegalStateException("no simulations specified");
		}
		if (participants == null || participants.length == 0) {
			throw new IllegalStateException("no participants specified");
		}
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
	// Time reservation handling
	// -------------------------------------------------------------------

	public void addTimeReservation(long startTime, int lengthInMillis) {
		if (reservationStartTime == null) {
			reservationStartTime = new long[10];
			reservationLength = new int[10];
		} else if (reservationStartTime.length == reservationCount) {
			reservationStartTime = ArrayUtils.setSize(reservationStartTime,
					reservationCount + 10);
			reservationLength = ArrayUtils.setSize(reservationLength,
					reservationCount + 10);
		}
		reservationStartTime[reservationCount] = startTime;
		reservationLength[reservationCount++] = lengthInMillis;
	}

	public int getReservationCount() {
		return reservationCount;
	}

	// Time in milliseconds
	public long getReservationStartTime(int index) {
		if (index < 0 || index >= reservationCount) {
			throw new IndexOutOfBoundsException("Index: " + index + " Size: "
					+ reservationCount);
		}
		return reservationStartTime[index];
	}

	// Length in milliseconds
	public int getReservationLength(int index) {
		if (index < 0 || index >= reservationCount) {
			throw new IndexOutOfBoundsException("Index: " + index + " Size: "
					+ reservationCount);
		}
		return reservationLength[index];
	}

	public int getSimulationsBeforeReservation() {
		return simulationsBeforeReservation;
	}

	public int getSimulationsReservationLength() {
		return simulationsReservationLength;
	}

	public void setReservationBetweenSimulations(
			int simulationsBeforeReservation, int reservationLength) {
		this.simulationsBeforeReservation = simulationsBeforeReservation;
		this.simulationsReservationLength = reservationLength;

	}

	// -------------------------------------------------------------------
	// Participants
	// -------------------------------------------------------------------

	public void setParticipants(int[] participants) {
		this.participants = participants;
	}

	public int[] getParticipants() {
		return participants;
	}

	// -------------------------------------------------------------------
	// Simulation handling
	// -------------------------------------------------------------------

	public boolean isSimulationsClosed() {
		return isSimulationsClosed;
	}

	public void setSimulationsClosed(boolean isSimulationsClosed) {
		this.isSimulationsClosed = isSimulationsClosed;
	}

	public void addSimulation(int[] participants) {
		addSimulation(participants, null);
	}

	public void addSimulation(int[] participants, int[] roles) {
		if (participants != null && roles != null
				&& participants.length != roles.length) {
			throw new IllegalArgumentException(
					"participants and roles must be " + "of equal size");
		}

		if (simulationParticipants == null) {
			simulationParticipants = new int[10][];
			simulationRoles = new int[10][];
		} else if (simulationParticipants.length == simulationCount) {
			simulationParticipants = (int[][]) ArrayUtils.setSize(
					simulationParticipants, simulationCount + 100);
			simulationRoles = (int[][]) ArrayUtils.setSize(simulationRoles,
					simulationCount + 100);
		}
		simulationParticipants[simulationCount] = participants;
		simulationRoles[simulationCount++] = roles;
	}

	public int getSimulationCount() {
		return simulationCount;
	}

	public int[] getParticipants(int index) {
		if (index < 0 || index >= simulationCount) {
			throw new IndexOutOfBoundsException("Index: " + index + " Size: "
					+ simulationCount);
		}
		return simulationParticipants[index];
	}

	public int[] getRoles(int index) {
		if (index < 0 || index >= simulationCount) {
			throw new IndexOutOfBoundsException("Index: " + index + " Size: "
					+ simulationCount);
		}
		return simulationRoles[index];
	}

} // CompetitionSchedule
