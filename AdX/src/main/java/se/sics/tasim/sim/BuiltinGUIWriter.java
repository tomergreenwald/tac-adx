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
 * BuiltinGUIWriter
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Dec 04 13:54:22 2002
 * Updated : $Date: 2008-04-04 21:23:36 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3984 $
 */
package se.sics.tasim.sim;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.is.EventWriter;
import se.sics.tasim.viewer.ViewerConnection;

public class BuiltinGUIWriter extends ViewerConnection {

	private ViewerConnection viewer;
	private EventWriter writer;

	public BuiltinGUIWriter(ViewerConnection viewer, EventWriter writer) {
		this.viewer = viewer;
		this.writer = writer;
	}

	public void nextTimeUnit(int timeUnit) {
		viewer.nextTimeUnit(timeUnit);
		writer.nextTimeUnit(timeUnit);
	}

	public void participant(int index, int role, String name, int participantID) {
		viewer.participant(index, role, name, participantID);
		writer.participant(index, role, name, participantID);
	}

	public void dataUpdated(int agent, int type, int value) {
		viewer.dataUpdated(agent, type, value);
		writer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, long value) {
		viewer.dataUpdated(agent, type, value);
		writer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, float value) {
		viewer.dataUpdated(agent, type, value);
		writer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, double value) {
		viewer.dataUpdated(agent, type, value);
		writer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, String value) {
		viewer.dataUpdated(agent, type, value);
		writer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, Transportable value) {
		viewer.dataUpdated(agent, type, value);
		writer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int type, Transportable value) {
		viewer.dataUpdated(type, value);
		writer.dataUpdated(type, value);
	}

	public void interaction(int fromAgent, int toAgent, int type) {
		viewer.interaction(fromAgent, toAgent, type);
		writer.interaction(fromAgent, toAgent, type);
	}

	public void interactionWithRole(int fromAgent, int role, int type) {
		viewer.interactionWithRole(fromAgent, role, type);
		writer.interactionWithRole(fromAgent, role, type);
	}

	public void setServerTime(long serverTime) {
		viewer.setServerTime(serverTime);
	}

	public void simulationStarted(int realSimID, String type, long startTime,
			long endTime, String timeUnitName, int timeUnitCount) {
		viewer.simulationStarted(realSimID, type, startTime, endTime,
				timeUnitName, timeUnitCount);
	}

	public void simulationStopped(int realSimID) {
		viewer.simulationStopped(realSimID);
	}

	// -1, 0 if no more simulations scheduled
	public void nextSimulation(int realSimID, long startTime) {
		viewer.nextSimulation(realSimID, startTime);
	}

	public void intCache(int agent, int type, int[] cache) {
		viewer.intCache(agent, type, cache);
	}

} // BuiltinGUIWriter
