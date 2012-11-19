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
 * StartInfo
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Oct 31 13:28:57 2002
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.props;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;

/**
 * <code>StartInfo</code> holds information about a game / simulation. It is
 * sent (wrapped in a Message) to all participating agents in the beginning of a
 * game.
 * 
 * <p>
 * <b>Warning:</b> serialized objects of this class might not be compatible with
 * future versions. Only use serialization of this class for temporary storage
 * or RMI using the same version of the class.
 */
public class StartInfo extends SimpleContent {

	private static final long serialVersionUID = 2985725711302603057L;

	private int simulationID;
	private long startTime;
	private int simulationLength;
	private int secondsPerDay;

	public StartInfo() {
	}

	public StartInfo(int simulationID, long startTime, int simulationLength,
			int secondsPerDay) {
		this.simulationID = simulationID;
		this.startTime = startTime;
		this.simulationLength = simulationLength;
		this.secondsPerDay = secondsPerDay;
		if (secondsPerDay < 1) {
			throw new IllegalArgumentException(
					"secondsPerDay must be positive: " + secondsPerDay);
		}
	}

	/**
	 * Returns the id of the simulation.
	 */
	public int getSimulationID() {
		return simulationID;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return startTime + simulationLength;
	}

	/**
	 * Returns the length of the simulation in milliseconds.
	 */
	public int getSimulationLength() {
		return simulationLength;
	}

	/**
	 * Returns the length of each simulated day in seconds.
	 */
	public int getSecondsPerDay() {
		return secondsPerDay;
	}

	/**
	 * Returns the number of days in the simulation.
	 */
	public int getNumberOfDays() {
		return simulationLength / (secondsPerDay * 1000);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer().append(getTransportName())
				.append('[').append(simulationID).append(',').append(startTime)
				.append(',').append(simulationLength).append(',').append(
						secondsPerDay).append(',');
		return params(buf).append(']').toString();
	}

	// -------------------------------------------------------------------
	// Transportable (externalization support)
	// -------------------------------------------------------------------

	/**
	 * Returns the transport name used for externalization.
	 */
	public String getTransportName() {
		return "startInfo";
	}

	public void read(TransportReader reader) throws ParseException {
		if (isLocked()) {
			throw new IllegalStateException("locked");
		}
		simulationID = reader.getAttributeAsInt("id");
		startTime = reader.getAttributeAsLong("startTime");
		// External format of simulation length is specified in seconds
		simulationLength = reader.getAttributeAsInt("length") * 1000;
		secondsPerDay = reader.getAttributeAsInt("secondsPerDay");
		super.read(reader);
	}

	public void write(TransportWriter writer) {
		writer.attr("id", simulationID).attr("startTime", startTime).attr(
				"length", simulationLength / 1000).attr("secondsPerDay",
				secondsPerDay);
		super.write(writer);
	}

} // StartInfo
