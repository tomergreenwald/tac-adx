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
 * TransportEventWriter
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Mar 20 16:54:29 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is;

import se.sics.isl.transport.TransportWriter;
import se.sics.isl.transport.Transportable;

public class TransportEventWriter extends EventWriter {

	private TransportWriter writer;

	public TransportEventWriter(TransportWriter writer) {
		this.writer = writer;
	}

	public void participant(int agent, int role, String name, int participantID) {
		writer.node("participant").attr("id", agent).attr("role", role).attr(
				"name", name).attr("participantID", participantID).endNode(
				"participant");
	}

	public void nextTimeUnit(int timeUnit) {
		writer.node("nextTimeUnit").attr("unit", timeUnit).endNode(
				"nextTimeUnit");
	}

	public void dataUpdated(int agent, int type, int value) {
		writer.node("intUpdated").attr("agent", agent);
		if (type != 0) {
			writer.attr("type", type);
		}
		writer.attr("value", value).endNode("intUpdated");
	}

	public void dataUpdated(int agent, int type, long value) {
		writer.node("longUpdated").attr("agent", agent);
		if (type != 0) {
			writer.attr("type", type);
		}
		writer.attr("value", value).endNode("longUpdated");
	}

	public void dataUpdated(int agent, int type, float value) {
		writer.node("floatUpdated").attr("agent", agent);
		if (type != 0) {
			writer.attr("type", type);
		}
		writer.attr("value", value).endNode("floatUpdated");
	}

	public void dataUpdated(int agent, int type, double value) {
		writer.node("doubleUpdated").attr("agent", agent);
		if (type != 0) {
			writer.attr("type", type);
		}
		writer.attr("value", value).endNode("doubleUpdated");
	}

	public void dataUpdated(int agent, int type, String value) {
		writer.node("stringUpdated").attr("agent", agent);
		if (type != 0) {
			writer.attr("type", type);
		}
		writer.attr("value", value).endNode("stringUpdated");
	}

	public void dataUpdated(int agent, int type, Transportable content) {
		writer.node("objectUpdated").attr("agent", agent);
		if (type != 0) {
			writer.attr("type", type);
		}
		writer.write(content);
		writer.endNode("objectUpdated");
	}

	public void dataUpdated(int type, Transportable content) {
		writer.node("objectUpdated");
		if (type != 0) {
			writer.attr("type", type);
		}
		writer.write(content);
		writer.endNode("objectUpdated");
	}

	public void interaction(int fromAgent, int toAgent, int type) {
		writer.node("interaction").attr("fromAgent", fromAgent).attr("toAgent",
				toAgent);
		if (type != 0) {
			writer.attr("type", type);
		}
		writer.endNode("interaction");
	}

	public void interactionWithRole(int fromAgent, int role, int type) {
		writer.node("interactionWithRole").attr("fromAgent", fromAgent).attr(
				"role", role);
		if (type != 0) {
			writer.attr("type", type);
		}
		writer.endNode("interactionWithRole");
	}

	// Should only be used for cahces to viewr.
	public void intCache(int agent, int type, int[] cache) {
		writer.node("intCache").attr("agent", agent).attr("type", type).attr(
				"cache", cache);
		writer.endNode("intCache");
	}

} // TransportEventWriter
