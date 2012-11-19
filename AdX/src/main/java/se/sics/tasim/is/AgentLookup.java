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
 * AgentLookup
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Jan 08 12:45:23 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import com.botbox.util.ArrayUtils;

public class AgentLookup {

	private final static int AGENTS_PER_USER = 11;

	private Hashtable nameLookup = new Hashtable();
	private AgentInfo[] agents = new AgentInfo[50];
	private AgentInfo[] agentCache;

	public AgentLookup() {
	}

	public AgentInfo getAgentInfo(int id) {
		int index = id / AGENTS_PER_USER;
		return (index < agents.length) ? agents[index] : null;
	}

	public String getAgentName(int id) {
		int index = id / AGENTS_PER_USER;
		AgentInfo info;
		if ((index < agents.length) && ((info = agents[index]) != null)) {
			int rest = id % 11;
			return rest == 0 ? info.getName() : (info.getName() + (rest - 1));
		}
		return null;
	}

	public int getAgentID(String name) {
		AgentInfo agent = (AgentInfo) nameLookup.get(name);
		int len;
		int add = 0;
		char c;
		if (agent == null && (len = name.length()) > 1
				&& ((c = name.charAt(len - 1)) >= '0') && (c <= '9')) {
			add = c - '0' + 1;
			agent = (AgentInfo) nameLookup.get(name.substring(0, len - 1));
		}
		return agent != null ? add + agent.getID() : -1;
	}

	// Needed by the Jetty User Realm
	public String getAgentPassword(int agentID) {
		int index = agentID / AGENTS_PER_USER;
		return index < agents.length && agents[index] != null ? agents[index]
				.getPassword() : null;
	}

	public boolean validateAgent(int id, String password) {
		int index = id / AGENTS_PER_USER;
		return index < agents.length && agents[index] != null
				&& agents[index].getPassword().equals(password);
	}

	public void setUser(String agentName, String password, int agentID) {
		setUser(agentName, password, agentID, -1);
	}

	public void setUser(String agentName, String password, int agentID,
			int parentID) {
		int index = agentID / AGENTS_PER_USER;
		if (index >= agents.length) {
			agents = (AgentInfo[]) ArrayUtils.setSize(agents, index + 50);
		}
		AgentInfo agent = new AgentInfo(agentName, password, agentID
				- (agentID % AGENTS_PER_USER), parentID);
		agents[index] = agent;
		nameLookup.put(agentName, agent);

		if (agentCache != null) {
			synchronized (this) {
				agentCache = null;
			}
		}
	}

	public AgentInfo[] getAgentInfos() {
		AgentInfo[] infos = this.agentCache;
		if (infos == null) {
			synchronized (this) {
				int index = 0;
				infos = new AgentInfo[agents.length];
				for (int i = 0, n = agents.length; i < n; i++) {
					if (agents[i] != null) {
						infos[index++] = agents[i];
					}
				}
				if (index < infos.length) {
					infos = (AgentInfo[]) ArrayUtils.setSize(infos, index);
				}
				this.agentCache = infos;
			}
		}
		return infos;
	}

	// public Enumeration agents() {
	// return new Enumeration() {
	// private int index = -1;

	// public boolean hasMoreElements() {
	// for (int i = index + 1, n = agents.length; i < n; i++) {
	// if (agents[i] != null) {
	// return true;
	// }
	// }
	// return false;
	// }

	// public Object nextElement() {
	// index++;
	// for (int n = agents.length; index < n; index++) {
	// if (agents[index] != null) {
	// return agents[index];
	// }
	// }
	// throw new NoSuchElementException();
	// }
	// };
	// }

	public String toString() {
		StringBuffer sb = new StringBuffer("AgentLookup[\n");
		for (int i = 0; i < agents.length; i++) {
			if (agents[i] != null) {
				sb.append(agents[i].getID()).append(',').append(
						agents[i].getName()).append(',').append(
						agents[i].getPassword()).append('\n');
			}
		}
		sb.append("]\n");
		return sb.toString();
	}

} // AgentLookup
