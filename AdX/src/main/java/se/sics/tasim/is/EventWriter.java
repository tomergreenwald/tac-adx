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
 * EventWriter
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Dec 03 16:50:31 2002
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is;

import se.sics.isl.transport.Transportable;

public abstract class EventWriter {

	protected EventWriter() {
	}

	/**
	 * Specifies a participant in the current simulation. The agent field is a
	 * local id for this participant in the simulation.
	 * 
	 * The participantID field is the global user id for the participant. A
	 * negative participantID indicates that this participant is builtin
	 * (dummy).
	 * 
	 * @param agent
	 *            a simulation local id for this participant
	 * @param role
	 *            the role of the participant
	 * @param name
	 *            the name of the participant
	 * @param participantID
	 *            the global id of the participating agent or negative if this
	 *            participant is builtin (dummy)
	 */
	public abstract void participant(int agent, int role, String name,
			int participantID);

	/**
	 * Notifies about entering a new time unit for the running simulation. Time
	 * units can for example be simulated days, and similar. Only used in some
	 * simulations.
	 * 
	 * @param timeUnit
	 *            the current time unit
	 */
	public abstract void nextTimeUnit(int timeUnit);

	public abstract void dataUpdated(int agent, int type, int value);

	public abstract void dataUpdated(int agent, int type, long value);

	public abstract void dataUpdated(int agent, int type, float value);

	public abstract void dataUpdated(int agent, int type, double value);

	public abstract void dataUpdated(int agent, int type, String value);

	public abstract void dataUpdated(int agent, int type, Transportable content);

	public abstract void dataUpdated(int type, Transportable content);

	public abstract void interaction(int fromAgent, int toAgent, int type);

	public abstract void interactionWithRole(int fromAgent, int role, int type);

	// For viewers only...
	public abstract void intCache(int agent, int type, int[] cache);

} // EventWriter
