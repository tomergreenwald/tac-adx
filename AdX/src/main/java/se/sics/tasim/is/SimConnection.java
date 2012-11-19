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
 * SimConnection
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Jan 07 15:20:15 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is;

import se.sics.isl.util.ConfigManager;

/**
 * The connection to the simulation server from the information system/server.
 */
public abstract class SimConnection {

	public final static int STATUS = 1;
	public final static int UNIQUE_SIM_ID = 2;
	public final static int SIM_ID = 3;

	public final static int STATUS_READY = 1;

	private InfoConnection info;

	/**
	 * This method sets the corresponding connection "listener" that handles
	 * messages from the simulation server.
	 * 
	 * @param info
	 *            the <code>InfoConnection</code> to use when communicating with
	 *            the information system/server
	 */
	public void setInfoConnection(InfoConnection info) {
		if (this.info != null) {
			throw new IllegalStateException("Connection already set");
		}
		this.info = info;
	}

	protected InfoConnection getInfoConnection() {
		return info;
	}

	public abstract void init(ConfigManager config);

	public abstract void close();

	// -------------------------------------------------------------------
	// Information
	// -------------------------------------------------------------------

	public abstract void dataUpdated(int type, int value);

	// If agent was not found (after a request) agentID = -1
	public abstract void setUser(String agentName, String password, int agentID);

	public abstract void setServerTime(long time);

	public abstract void simulationInfo(SimulationInfo info);

	public abstract void resultsGenerated(int simulationID);

	// -------------------------------------------------------------------
	// Requests
	// -------------------------------------------------------------------

	public abstract void addChatMessage(long time, String serverName,
			String userName, String message);

	public abstract void scheduleCompetition(CompetitionSchedule schedule);

	public abstract void lockNextSimulations(int simulationCount);

	public abstract void addTimeReservation(long startTime, int lengthInMillis);

	public abstract void createSimulation(String type, String params);

	// Can only be done before the simulation is "locked" i.e. assigned
	// a public simulation id
	public abstract void removeSimulation(int simulationUniqID);

	public abstract void joinSimulation(int simulationUniqID, int agentID,
			String simRole);

	public abstract void joinSimulation(int simulationUniqID, int agentID,
			int simRole);

} // SimConnection
