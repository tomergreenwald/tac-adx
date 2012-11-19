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
 * ViewerCache
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Mar 20 11:04:32 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import com.botbox.util.ArrayUtils;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.is.EventWriter;

public class ViewerCache extends EventWriter {

	private static final int AGENT = 0;
	private static final int ROLE = 1;
	private static final int PARTICIPANT_ID = 2;
	private static final int PARTS = 3;

	private int[] partData;
	private String[] partNames;
	private int partNumber;

	public ViewerCache() {
	}

	public void writeCache(EventWriter eventWriter) {
		for (int i = 0, index = 0, n = partNumber; i < n; i++, index += PARTS) {
			eventWriter.participant(partData[index + AGENT], partData[index
					+ ROLE], partNames[i], partData[index + PARTICIPANT_ID]);
		}
	}

	public void participant(int agent, int role, String name, int participantID) {
		if (partData == null) {
			partData = new int[8 * PARTS];
			partNames = new String[8];

		} else if (partNumber == partNames.length) {
			partData = ArrayUtils.setSize(partData, (partNumber + 8) * PARTS);
			partNames = (String[]) ArrayUtils
					.setSize(partNames, partNumber + 8);
		}
		int index = partNumber * PARTS;
		partData[index + AGENT] = agent;
		partData[index + ROLE] = role;
		partData[index + PARTICIPANT_ID] = participantID;
		partNames[partNumber++] = name;
	}

	public void nextTimeUnit(int timeUnit) {
	}

	public void dataUpdated(int agent, int type, int value) {
	}

	public void dataUpdated(int agent, int type, long value) {
	}

	public void dataUpdated(int agent, int type, float value) {
	}

	public void dataUpdated(int agent, int type, double value) {
	}

	public void dataUpdated(int agent, int type, String value) {
	}

	public void dataUpdated(int agent, int type, Transportable content) {
	}

	public void dataUpdated(int type, Transportable content) {
	}

	public void interaction(int fromAgent, int toAgent, int type) {
	}

	public void interactionWithRole(int fromAgent, int role, int type) {
	}

	public void intCache(int agent, int type, int[] cache) {
	}
} // ViewerCache
