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
 * SimulationStatus
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Feb 20 13:53:05 2003
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.props;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;

/**
 * <code>SimulationStatus</code> contains in game / simulation status.
 * 
 * <p>
 * <b>Warning:</b> serialized objects of this class might not be compatible with
 * future versions. Only use serialization of this class for temporary storage
 * or RMI using the same version of the class.
 */
public class SimulationStatus extends SimpleContent {

	private static final long serialVersionUID = 7937789505047945874L;

	private int simDate;
	private int consumedMillis;
	private boolean isSimulationEnded;

	public SimulationStatus() {
	}

	public SimulationStatus(int currentDate, int consumedMillis) {
		this(currentDate, consumedMillis, false);
	}

	public SimulationStatus(int currentDate, int consumedMillis,
			boolean isSimulationEnded) {
		this.simDate = currentDate;
		this.consumedMillis = consumedMillis;
		this.isSimulationEnded = isSimulationEnded;
	}

	/**
	 * Returns <code>true</code> if the simulation has ended and
	 * <code>false</code> otherwise.
	 */
	public boolean isSimulationEnded() {
		return isSimulationEnded;
	}

	public int getCurrentDate() {
		return simDate;
	}

	/**
	 * Returns the number of milliseconds passed since start of day when this
	 * message was sent.
	 */
	public int getConsumedMillis() {
		return consumedMillis;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer().append(getTransportName())
				.append('[').append(simDate).append(',').append(consumedMillis)
				.append(',').append(isSimulationEnded).append(',');
		return params(buf).append(']').toString();
	}

	/*****************************************************************************
	 * Transportable (externalization support)
	 ****************************************************************************/

	/**
	 * Returns the transport name used for externalization.
	 */
	public String getTransportName() {
		return "simulationStatus";
	}

	public void read(TransportReader reader) throws ParseException {
		if (isLocked()) {
			throw new IllegalStateException("locked");
		}
		simDate = reader.getAttributeAsInt("date");
		consumedMillis = reader.getAttributeAsInt("consumedMillis");
		isSimulationEnded = reader.getAttributeAsInt("isSimulationEnded", 0) > 0;
		super.read(reader);
	}

	public void write(TransportWriter writer) {
		writer.attr("date", simDate).attr("consumedMillis", consumedMillis);
		if (isSimulationEnded) {
			writer.attr("isSimulationEnded", 1);
		}
		super.write(writer);
	}

} // SimulationStatus
