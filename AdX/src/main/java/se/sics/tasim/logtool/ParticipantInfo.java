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
 * ParticipantInfo
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Feb 28 13:00:43 2003
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.logtool;

/**
 * Utility for parsing server logs (not used by the AgentWare).
 */
public class ParticipantInfo {

	private int index;
	private String address;
	private int id;
	private String name;
	private int role;

	public ParticipantInfo(int index, String address, int id, String name,
			int role) {
		this.index = index;
		this.address = address;
		this.id = id;
		this.name = name == null ? address : name;
		this.role = role;
	}

	public int getIndex() {
		return index;
	}

	public String getAddress() {
		return address;
	}

	public String getName() {
		return name;
	}

	public boolean isBuiltinAgent() {
		return id < 0;
	}

	public int getUserID() {
		return id;
	}

	public int getRole() {
		return role;
	}

} // ParticipantInfo
