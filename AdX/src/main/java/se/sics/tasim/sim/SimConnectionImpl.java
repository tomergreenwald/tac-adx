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
 * SimConnectionImpl
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Jan 07 17:10:52 2003
 * Updated : $Date: 2008-04-04 21:23:36 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3984 $
 */
package se.sics.tasim.sim;

import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.util.ConfigManager;
import se.sics.tasim.is.AgentLookup;
import se.sics.tasim.is.CompetitionSchedule;
import se.sics.tasim.is.SimConnection;
import se.sics.tasim.is.SimulationInfo;

/**
 * This is the implementation of the SimConnection that is located in the Supply
 * Chain Simulator. It implements the "behaviour" for all methods that the
 * InfoSystem "calls"
 */
public class SimConnectionImpl extends SimConnection {

	private static final Logger log = Logger.getLogger(SimConnectionImpl.class.getName());

	private final Admin admin;
	private final AgentLookup lookup;
	private boolean isInitialized = false;

	public SimConnectionImpl(Admin admin, AgentLookup lookup) {
		this.admin = admin;
		this.lookup = lookup;
	}

	public void init(ConfigManager config) {
	}

	public void close() {
		isInitialized = false;
	}

	// -------------------------------------------------------------------
	// Information
	// -------------------------------------------------------------------

	public void dataUpdated(int type, int value) {
		admin.dataUpdated(type, value);
	}

	public void setUser(String agentName, String password, int agentID) {
		lookup.setUser(agentName, password, agentID);
	}

	public void setServerTime(long time) {
		admin.setServerTime(time);

		if (!isInitialized) {
			isInitialized = true;
			admin.sendStateToInfoSystem();
		}
	}

	public void simulationInfo(SimulationInfo info) {
		admin.addSimulation(info);
	}

	public void resultsGenerated(int simulationID) {
		admin.resultsGenerated(simulationID);
	}

	// -------------------------------------------------------------------
	// Requests
	// -------------------------------------------------------------------

	public void addChatMessage(long time, String serverName, String userName,
			String message) {
		admin.addChatMessage(time, serverName, userName, message);
	}

	public void scheduleCompetition(CompetitionSchedule schedule) {
		admin.scheduleCompetition(schedule, true);
	}

	public void lockNextSimulations(int simulationCount) {
		admin.lockNextSimulations(simulationCount);
	}

	public void addTimeReservation(long startTime, int lengthInMillis) {
		admin.addTimeReservation(startTime, lengthInMillis, true);
	}

	public void createSimulation(String type, String params) {
		admin.createSimulation(type, params, true);
	}

	// Can only be done before the simulation is "locked"
	public void removeSimulation(int simulationUniqID) {
		admin.removeSimulation(simulationUniqID);
	}

	public void joinSimulation(int simulationUniqID, int agentID, String simRole) {
		try {
			admin.joinSimulation(simulationUniqID, agentID, simRole);
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not join agent " + agentID
					+ " to simulation with id " + simulationUniqID, e);
		}
	}

	public void joinSimulation(int simulationUniqID, int agentID, int simRole) {
		try {
			admin.joinSimulation(simulationUniqID, agentID, simRole);
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not join agent " + agentID
					+ " to simulation with id " + simulationUniqID, e);
		}
	}

} // SimConnectionImpl
