/**
 * TAC Supply Chain Management Simulator
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001-2005 SICS AB. All rights reserved.
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
 * Ping
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Jul 14 18:41:13 2004
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.props;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.isl.transport.Transportable;

/**
 * <code>Ping</code> is used to verify the liveness of agent connections and
 * also to measure network response times.
 * 
 * <p>
 * <b>Warning:</b> serialized objects of this class might not be compatible with
 * future versions. Only use serialization of this class for temporary storage
 * or RMI using the same version of the class.
 */
public class Ping implements Transportable, java.io.Serializable {

	private static final long serialVersionUID = 5214670517699777053L;

	public static final int PONG = 1 << 0;
	public static final int PING = 1 << 1;

	private int flags;

	public Ping() {
		this.flags = PING;
	}

	/**
	 * @deprecated use Ping() instead for ping and createPong for pong
	 */
	public Ping(int flags) {
		this.flags = flags;
	}

	public boolean isPing() {
		return (flags & PING) != 0;
	}

	public boolean isPong() {
		return (flags & PONG) != 0;
	}

	public Ping createPong() {
		return new Ping(PONG);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer().append(getTransportName())
				.append('[').append(flags);
		return buf.append(']').toString();
	}

	// -------------------------------------------------------------------
	// Transportable (externalization support)
	// -------------------------------------------------------------------

	/**
	 * Returns the transport name used for externalization.
	 */
	public String getTransportName() {
		return "ping";
	}

	public void read(TransportReader reader) throws ParseException {
		flags = reader.getAttributeAsInt("flags");
	}

	public void write(TransportWriter writer) {
		writer.attr("flags", flags);
	}

} // Ping
