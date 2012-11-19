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
 * SimulationInfo
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Oct 11 14:02:39 2002
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.botbox.util.ArrayUtils;
import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.isl.transport.Transportable;

public class SimulationInfo implements Transportable {

	private final static int IS_FULL = 1;
	private final static int IS_RESERVED = 2;

	public final static String RESERVED = "reserved";

	private static final Logger log = Logger.getLogger(SimulationInfo.class
			.getName());

	private int id;
	private String type;
	private String params;
	private int simulationLength;

	private String[] paramCache;
	private int paramCacheSize = -1;

	private int simulationID = -1;
	private int participantCount;
	private long startTime;

	private int[] participants;
	private int[] roles;

	private int flags;

	public SimulationInfo(int id, String type, String params,
			int simulationLength) {
		this.id = id;
		setType(type);
		this.params = params;
		this.simulationLength = simulationLength;
	}

	public SimulationInfo() {
	}

	public int getID() {
		return id;
	}

	public boolean isReservation() {
		return (flags & IS_RESERVED) != 0;
	}

	public String getType() {
		return type;
	}

	private void setType(String type) {
		if (type == null) {
			throw new NullPointerException();
		}
		if (RESERVED.equals(type)) {
			// This is a time reservation and as such, it can not accept any
			// participants
			this.flags |= IS_RESERVED | IS_FULL;
			// Reuse this string for memory conservation
			this.type = RESERVED;
		} else {
			// Reuse the global type string for memory conservation since
			// there are few simulation types
			this.type = type.intern();
		}
	}

	public String getParams() {
		return params;
	}

	public String getParameter(String name) {
		if (params == null) {
			return null;
		}

		if (paramCacheSize < 0) {
			int start = 0;
			int index = params.indexOf('&', start);
			paramCache = new String[8];
			paramCacheSize = 0;

			try {
				while (index >= 0) {
					// Try to add parameter between start -> index
					addParam(start, index);
					start = index + 1;
					index = params.indexOf('&', start);
				}

				if (start < params.length()) {
					addParam(start, params.length());
				}

				if (paramCache.length > paramCacheSize) {
					paramCache = (String[]) ArrayUtils.setSize(paramCache,
							paramCacheSize);
				}
			} catch (UnsupportedEncodingException e) {
				log.log(Level.WARNING, "could not parse params '" + params
						+ '\'', e);
				return null;
			}
		}

		int index = ArrayUtils.keyValuesIndexOf(paramCache, 2, 0,
				paramCacheSize, name);
		return index >= 0 ? paramCache[index + 1] : null;
	}

	private void addParam(int start, int end)
			throws UnsupportedEncodingException {
		int separator = params.indexOf('=', start);
		if (separator > start && separator < end) {
			if (paramCacheSize >= paramCache.length) {
				paramCache = (String[]) ArrayUtils.setSize(paramCache,
						paramCacheSize + 16);
			}
			paramCache[paramCacheSize] = URLDecoder.decode(params.substring(
					start, separator), "UTF-8");
			paramCache[paramCacheSize + 1] = URLDecoder.decode(params
					.substring(separator + 1, end), "UTF-8");
			paramCacheSize += 2;
		} else {
			log.warning("malformed parameters '" + params + "' after " + start);
		}
	}

	public int getParameter(String name, int defaultValue) {
		String value = getParameter(name);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				log.log(Level.WARNING, "could not parse param " + name + "='"
						+ value + '\'', e);
			}
		}
		return defaultValue;
	}

	public boolean hasSimulationID() {
		return simulationID >= 0;
	}

	public int getSimulationID() {
		return simulationID;
	}

	public void setSimulationID(int simulationID) {
		if (this.simulationID != simulationID) {
			if (this.simulationID >= 0) {
				throw new IllegalStateException("simulationID already set");
			}
			this.simulationID = simulationID;
		}
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		if (this.startTime > 0) {
			throw new IllegalStateException("start time already set");
		}
		this.startTime = startTime;
	}

	public long getEndTime() {
		return startTime + simulationLength;
	}

	/**
	 * Returns the simulation length in milliseconds
	 */
	public int getSimulationLength() {
		return simulationLength;
	}

	public boolean isEmpty() {
		return participantCount == 0;
	}

	public boolean isFull() {
		return (flags & IS_FULL) != 0;
	}

	public void setFull() {
		this.flags |= IS_FULL;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getParticipantCount() {
		return participantCount;
	}

	public int getParticipantID(int index) {
		if (index >= participantCount) {
			throw new IndexOutOfBoundsException("Index: " + index + " Size: "
					+ participantCount);
		}
		return participants[index];
	}

	public boolean isBuiltinParticipant(int index) {
		if (index >= participantCount) {
			throw new IndexOutOfBoundsException("Index: " + index + " Size: "
					+ participantCount);
		}
		return participants[index] < 0;
	}

	public int getParticipantRole(int index) {
		if (index >= participantCount) {
			throw new IndexOutOfBoundsException("Index: " + index + " Size: "
					+ participantCount);
		}
		return roles[index];
	}

	public int indexOfParticipant(int agentID) {
		// More efficient this way because often this check is made for
		// the last added participant (for example when the SimulationInfo
		// is shared between simulation server and information system in
		// same process).
		for (int i = participantCount - 1; i >= 0; i--) {
			if (participants[i] == agentID) {
				return i;
			}
		}
		return -1;
	}

	public boolean isParticipant(int agentID) {
		return indexOfParticipant(agentID) >= 0;
	}

	/**
	 * Adds the specified agent with the specified role.
	 * 
	 * @param agentID
	 *            the id of the participating agent to add
	 * @param role
	 *            the role of the agent (as specified by the simulation manager)
	 */
	public synchronized boolean addParticipant(int agentID, int role) {
		// Only add if not full and not already added
		if (isFull() || isParticipant(agentID)) {
			return false;
		}

		if (participants == null) {
			participants = new int[8];
			roles = new int[8];
		} else if (participants.length == participantCount) {
			participants = ArrayUtils.setSize(participants,
					participantCount + 8);
			roles = ArrayUtils.setSize(roles, participantCount + 8);
		}
		participants[participantCount] = agentID;
		roles[participantCount++] = role;
		return true;
	}

	public synchronized void copyParticipants(SimulationInfo info) {
		for (int i = 0, n = info.participantCount; i < n; i++) {
			addParticipant(info.getParticipantID(i), info.getParticipantRole(i));
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getTransportName()).append('[').append(id).append(',')
				.append(simulationID).append(',').append(startTime).append(',')
				.append(simulationLength / 1000).append(',').append(type);
		if (params != null) {
			sb.append('[').append(params).append(']');
		}
		sb.append(",[");
		for (int i = 0, n = participantCount; i < n; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(participants[i]).append('=').append(roles[i]);
		}
		return sb.append(']').toString();
	}

	// -------------------------------------------------------------------
	// Transportable (externalization support)
	// -------------------------------------------------------------------

	public String getTransportName() {
		return "simulationInfo";
	}

	public void read(TransportReader reader) throws ParseException {
		if (type != null) {
			throw new IllegalStateException("already initialized");
		}
		id = reader.getAttributeAsInt("id");
		setType(reader.getAttribute("type"));
		params = reader.getAttribute("params", null);
		simulationLength = reader.getAttributeAsInt("length") * 1000;
		simulationID = reader.getAttributeAsInt("simID", -1);
		startTime = reader.getAttributeAsLong("startTime", 0L);
		flags = reader.getAttributeAsInt("flags", 0);

		while (reader.nextNode("agent", false)) {
			addParticipant(reader.getAttributeAsInt("agentID"), reader
					.getAttributeAsInt("role"));
		}
	}

	public void write(TransportWriter writer) {
		writer.attr("id", id).attr("type", type);
		if (params != null) {
			writer.attr("params", params);
		}
		writer.attr("length", simulationLength / 1000);
		if (simulationID >= 0) {
			writer.attr("simID", simulationID);
		}
		if (startTime > 0) {
			writer.attr("startTime", startTime);
		}
		if (flags != 0) {
			writer.attr("flags", flags);
		}
		for (int i = 0, n = participantCount; i < n; i++) {
			writer.node("agent").attr("agentID", participants[i]).attr("role",
					roles[i]).endNode("agent");
		}
	}

} // SimulationInfo
