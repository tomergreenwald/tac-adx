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
 * TickListener
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Feb 12 12:41:10 2003
 * Updated : $Date: 2004-10-28 14:24:41 -0500 (Thu, 28 Oct 2004) $
 *           $Revision: 1057 $
 * Purpose :
 *
 */
package se.sics.tasim.viewer;

public interface TickListener {

	/**
	 * Tick notification. Usually called once per second.
	 * 
	 * @param serverTime
	 *            the current server time in milliseconds
	 */
	public void tick(long serverTime);

	/**
	 * Simulation tick notification. Only called when a simulation is running
	 * and usually several times per second.
	 * 
	 * @param serverTime
	 *            the current server time
	 * @param simulationDate
	 *            the current simulation date
	 */
	public void simulationTick(long serverTime, int simulationDate);

} // TickListener
