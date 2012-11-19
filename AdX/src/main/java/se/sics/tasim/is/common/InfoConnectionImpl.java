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
 * InfoConnectionImpl
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Jan 08 14:02:17 2003
 * Updated : $Date: 2008-04-11 20:26:24 -0500 (Fri, 11 Apr 2008) $
 *           $Revision: 4090 $
 */
package se.sics.tasim.is.common;

import java.io.IOException;

import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;
import se.sics.tasim.is.InfoConnection;
import se.sics.tasim.is.SimulationInfo;

public class InfoConnectionImpl extends InfoConnection {

	private SimServer simServer;

	public InfoConnectionImpl() {
	}

	public void init(ConfigManager config)
			throws IllegalConfigurationException, IOException {
	}

	public void close() {
		if (simServer != null) {
			simServer.close();
		}
	}

	public void setSimServer(SimServer simServer) {
		if (simServer == null) {
			throw new NullPointerException();
		}
		this.simServer = simServer;
	}

    public SimServer getSimServer() {
        return simServer;
    }

    // -------------------------------------------------------------------
	// Information
	// -------------------------------------------------------------------

	public void requestSuccessful(int operation, int id) {
		simServer.requestSuccessful(operation, id);
	}

	public void requestFailed(int operation, int id, String reason) {
		simServer.requestFailed(operation, id, reason);
	}

	public void checkUser(String userName) {
		simServer.checkUser(userName);
	}

	// MLB 20080411 - Added to allow non-pre-registered agents to play,
	// for use in experimental work
	public int addUser(String name, String password, String email) {
		return simServer.addUser(name, password, email);
	}

	public void dataUpdated(int type, int value) {
		simServer.dataUpdated(type, value);
	}

	public void simulationCreated(SimulationInfo info) {
		simServer.simulationCreated(info);
	}

	public void simulationCreated(SimulationInfo info, int competitionID) {
		simServer.simulationCreated(info, competitionID);
	}

	public void simulationRemoved(int simulationUniqID, String msg) {
		simServer.simulationRemoved(simulationUniqID, msg);
	}

	public void simulationJoined(int simulationUniqID, int agentID, int role) {
		simServer.simulationJoined(simulationUniqID, agentID, role);
	}

	public void simulationLocked(int simulationUniqID, int simID) {
		simServer.simulationLocked(simulationUniqID, simID);
	}

	public void simulationStarted(int simulationUniqID, String timeUnitName,
			int timeUnitCount) {
		simServer.simulationStarted(simulationUniqID, timeUnitName,
				timeUnitCount);
	}

	public void simulationStopped(int simulationUniqID, int simulationID,
			boolean error) {
		simServer.simulationStopped(simulationUniqID, simulationID, error);
	}

	public void sendChatMessage(long time, String message) {
		simServer.sendChatMessage(time, message);
	}

	// -------------------------------------------------------------------
	// Viewer information
	// -------------------------------------------------------------------

	public void nextTimeUnit(int timeUnit) {
		simServer.nextTimeUnit(timeUnit);
	}

	public void participant(int id, int role, String name, int participantID) {
		simServer.participant(id, role, name, participantID);
	}

	public void dataUpdated(int agent, int type, int value) {
		simServer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, long value) {
		simServer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, float value) {
		simServer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, double value) {
		simServer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, String value) {
		simServer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, Transportable value) {
		simServer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int type, Transportable value) {
		simServer.dataUpdated(type, value);
	}

	public void interaction(int fromAgent, int toAgent, int type) {
		simServer.interaction(fromAgent, toAgent, type);
	}

	public void interactionWithRole(int fromAgent, int role, int type) {
		simServer.interactionWithRole(fromAgent, role, type);
	}

	// Only from simServer to viewers... Ignore here????
	public void intCache(int agent, int type, int[] cache) {
	}

} // InfoConnectionImpl
