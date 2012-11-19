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
 * InfoConnection
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Jan 07 13:56:20 2003
 * Updated : $Date: 2008-04-11 20:26:24 -0500 (Fri, 11 Apr 2008) $
 *           $Revision: 4090 $
 */
package se.sics.tasim.is;

import java.io.IOException;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;

/**
 * The connection to the information system/server from the simulation server.
 */
public abstract class InfoConnection extends EventWriter {

	public final static int STATUS = SimConnection.STATUS;
	public final static int UNIQUE_SIM_ID = SimConnection.UNIQUE_SIM_ID;
	public final static int SIM_ID = SimConnection.SIM_ID;

	public final static int STATUS_READY = SimConnection.STATUS_READY;

	/** Request types */
	public static final int CREATE_SIMULATION = 1;
	public static final int REMOVE_SIMULATION = 2;
	public static final int JOIN_SIMULATION = 3;
	public static final int RESERVE_TIME = 4;
	public static final int SCHEDULE_COMPETITION = 10;

	/** Information */
	private SimConnection sim;
	private String serverName;
	private String serverPassword;
	private String serverVersion;

	/**
	 * This method sets the corresponding connection "listener" that handles
	 * messages to the simulation server.
	 * 
	 * @param sim
	 *            the <code>SimConnection</code> to use for communication to the
	 *            simulation server
	 */
	public void setSimConnection(SimConnection sim) {
		if (this.sim != null) {
			throw new IllegalStateException("Connection already set");
		}
		this.sim = sim;
	}

	public SimConnection getSimConnection() {
		return sim;
	}

	public abstract void init(ConfigManager config)
			throws IllegalConfigurationException, IOException;

	public abstract void close();

	// -------------------------------------------------------------------
	// Information
	// -------------------------------------------------------------------

	public String getServerName() {
		return serverName;
	}

	public String getServerPassword() {
		return serverPassword;
	}

	public String getServerVersion() {
		return serverVersion;
	}

	public void auth(String serverName, String serverPassword,
			String serverVersion) {
		if (serverName == null || serverVersion == null) {
			throw new NullPointerException();
		}
		if (serverName.length() < 1) {
			throw new IllegalArgumentException("too short server name");
		}
		this.serverName = serverName;
		this.serverPassword = serverPassword;
		this.serverVersion = serverVersion;
	}

	public abstract void requestSuccessful(int operation, int id);

	public abstract void requestFailed(int operation, int id, String reason);

	public abstract void checkUser(String userName);

	// MLB 20080411 - Added to allow non-pre-registered agents to play,
	// for use in experimental work
	public abstract int addUser(String name, String password, String email);

	public abstract void dataUpdated(int type, int value);

	public abstract void simulationCreated(SimulationInfo info);

	public abstract void simulationCreated(SimulationInfo info,
			int competitionID);

	public abstract void simulationRemoved(int simulationUniqID, String msg);

	public abstract void simulationJoined(int simulationUniqID, int agentID,
			int role);

	public abstract void simulationLocked(int simulationUniqID, int simID);

	public abstract void simulationStarted(int simulationUniqID,
			String timeUnitName, int timeUnitCount);

	public abstract void simulationStopped(int simulationUniqID,
			int simulationID, boolean error);

	public abstract void sendChatMessage(long time, String message);

} // InfoConnection
