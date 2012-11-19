/*
 * AgentSupport.java
 *
 * COPYRIGHT  2008
 * THE REGENTS OF THE UNIVERSITY OF MICHIGAN
 * ALL RIGHTS RESERVED
 *
 * PERMISSION IS GRANTED TO USE, COPY, CREATE DERIVATIVE WORKS AND REDISTRIBUTE THIS
 * SOFTWARE AND SUCH DERIVATIVE WORKS FOR NONCOMMERCIAL EDUCATION AND RESEARCH
 * PURPOSES, SO LONG AS NO FEE IS CHARGED, AND SO LONG AS THE COPYRIGHT NOTICE
 * ABOVE, THIS GRANT OF PERMISSION, AND THE DISCLAIMER BELOW APPEAR IN ALL COPIES
 * MADE; AND SO LONG AS THE NAME OF THE UNIVERSITY OF MICHIGAN IS NOT USED IN ANY
 * ADVERTISING OR PUBLICITY PERTAINING TO THE USE OR DISTRIBUTION OF THIS SOFTWARE
 * WITHOUT SPECIFIC, WRITTEN PRIOR AUTHORIZATION.
 *
 * THIS SOFTWARE IS PROVIDED AS IS, WITHOUT REPRESENTATION FROM THE UNIVERSITY OF
 * MICHIGAN AS TO ITS FITNESS FOR ANY PURPOSE, AND WITHOUT WARRANTY BY THE
 * UNIVERSITY OF MICHIGAN OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT
 * LIMITATION THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE REGENTS OF THE UNIVERSITY OF MICHIGAN SHALL NOT BE LIABLE FOR ANY
 * DAMAGES, INCLUDING SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WITH
 * RESPECT TO ANY CLAIM ARISING OUT OF OR IN CONNECTION WITH THE USE OF THE SOFTWARE,
 * EVEN IF IT HAS BEEN OR IS HEREAFTER ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */

package edu.umich.eecs.tac.viewer.role;

import com.botbox.util.ArrayUtils;
import edu.umich.eecs.tac.viewer.ViewAdaptor;

/**
 * @author Patrick Jordan
 */
public class AgentSupport extends ViewAdaptor {
	private int[] agents;
	private int[] roles;
	private int[] participants;
	private String[] names;
	private int agentCount;

	public AgentSupport() {
		agents = new int[0];
		roles = new int[0];
		participants = new int[0];
		names = new String[0];
	}

	public int indexOfAgent(int agent) {
		return ArrayUtils.indexOf(agents, 0, agentCount, agent);
	}

	public int size() {
		return agentCount;
	}

	public int agent(int index) {
		return agents[index];
	}

	public int role(int index) {
		return roles[index];
	}

	public int participant(int index) {
		return agents[index];
	}

	public String name(int index) {
		return names[index];
	}	

	public void participant(int agent, int role, String name, int participantID) {
		setAgent(agent, role, name, participantID);
	}

	protected void addAgent(int agent) {
		int index = ArrayUtils.indexOf(agents, 0, agentCount, agent);
		if (index < 0) {
			doAddAgent(agent);
		}
	}

	private int doAddAgent(int agent) {
		if (agentCount == participants.length) {
			int newSize = agentCount + 8;
			agents = ArrayUtils.setSize(agents, newSize);
			roles = ArrayUtils.setSize(roles, newSize);
			participants = ArrayUtils.setSize(participants, newSize);
			names = (String[]) ArrayUtils.setSize(names, newSize);
		}

		agents[agentCount] = agent;

		return agentCount++;
	}

	private void setAgent(int agent, int role, String name, int participantID) {
		addAgent(agent);

		int index = ArrayUtils.indexOf(agents, 0, agentCount, agent);
		roles[index] = role;
		names[index] = name;
		participants[index] = participantID;
	}
}
