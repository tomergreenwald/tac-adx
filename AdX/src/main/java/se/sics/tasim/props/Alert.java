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
 * Alert
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Feb 21 15:27:26 2003
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.props;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.isl.transport.Transportable;

/**
 * <code>Alert</code> is used for sending administrative messages to the user
 * behind external agents. Example of usage is when the agent is using an
 * unsupported communication protocol with the server. An alert message is then
 * sent with information about the problem before the connection is terminated.
 * 
 * <p>
 * <b>Warning:</b> serialized objects of this class might not be compatible with
 * future versions. Only use serialization of this class for temporary storage
 * or RMI using the same version of the class.
 */
public class Alert implements Transportable, java.io.Serializable {

	private static final long serialVersionUID = 5611022906104201693L;

	private int priority;
	private String title;
	private String message;

	public Alert() {
	}

	public Alert(String title, String message) {
		this(0, title, message);
	}

	public Alert(int priority, String title, String message) {
		this.priority = priority;
		this.title = title;
		this.message = message;
	}

	public int getPriority() {
		return priority;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public String toString() {
		return getTransportName() + '[' + priority + ',' + title + ','
				+ message + ']';
	}

	/*****************************************************************************
	 * Transportable (externalization support)
	 ****************************************************************************/

	/**
	 * Returns the transport name used for externalization.
	 */
	public String getTransportName() {
		return "alert";
	}

	public void read(TransportReader reader) throws ParseException {
		this.priority = reader.getAttributeAsInt("priority", 0);
		this.title = reader.getAttribute("title");
		this.message = reader.getAttribute("message");
	}

	public void write(TransportWriter writer) {
		if (priority != 0) {
			writer.attr("priority", priority);
		}
		writer.attr("title", title);
		writer.attr("message", message);
	}

} // Alert
