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
 * AgentInfo.java
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Jan 08 12:45:23 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */

package se.sics.tasim.is;

public class AgentInfo {

	private int id;
	private int parentID;
	private String name;
	private String password;

	public AgentInfo(String name, String password, int id, int parentID) {
		if (id < 0) {
			throw new IllegalArgumentException("id can not be below zero");
		}
		if (password == null) {
			throw new NullPointerException("Password can not be null");
		}
		this.name = name;
		this.password = password;
		this.id = id;
		this.parentID = parentID;
	}

	public int getID() {
		return id;
	}

	public boolean hasParent() {
		return parentID >= 0;
	}

	public int getParent() {
		return parentID;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

}
