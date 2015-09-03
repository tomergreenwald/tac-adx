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
 * ViewerConnection
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Dec 03 17:24:23 2002
 * Updated : $Date: 2004-10-28 14:24:41 -0500 (Thu, 28 Oct 2004) $
 *           $Revision: 1057 $
 * Purpose :
 *
 */
package se.sics.tasim.viewer;

import se.sics.tasim.is.EventWriter;

public abstract class ViewerConnection extends EventWriter {

	// public void simulationStarted(int realSimID, String type,
	// long startTime, long endTime) {
	// simulationStarted(realSimID, type, startTime, endTime,
	// null, 0);
	// }

	public abstract void setServerTime(long serverTime);

	public abstract void simulationStarted(int realSimID, String type,
			long startTime, long endTime, String timeUnitName, int timeUnitCount);

	public abstract void simulationStopped(int realSimID);

	/**
	 * Notifies about the next scheduled simulation if such exists.
	 * 
	 * @param publicSimID
	 *            the public simulation id or -1 if no such id has been assigned
	 * @param startTime
	 *            the start time of the next simulation or 0 if no future
	 *            simulation has been scheduled
	 */
	public abstract void nextSimulation(int publicSimID, long startTime);

	// A cache with values for agent + type (bank account, etc)
	public abstract void intCache(int agent, int type, int[] cache);

} // ViewerConnection
