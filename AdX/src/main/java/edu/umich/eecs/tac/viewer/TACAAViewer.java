/*
 * TACAAViewer.java
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

package edu.umich.eecs.tac.viewer;

import static tau.tac.adx.sim.TACAdxConstants.ROLE_NAME;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.viewer.SimulationViewer;
import se.sics.tasim.viewer.ViewerPanel;

import javax.swing.*;

import edu.umich.eecs.tac.props.RetailCatalog;

import java.util.logging.Logger;

/**
 * @author Patrick Jordan
 */
public class TACAAViewer extends SimulationViewer {

	private static final Logger log = Logger.getLogger(TACAAViewer.class
			.getName());

	private TACAASimulationPanel simulationPanel;

    private RetailCatalog catalog;

	public void init(ViewerPanel panel) {
        simulationPanel = new TACAASimulationPanel(panel);
	}

	public JComponent getComponent() {
		return simulationPanel;
	}

	public void setServerTime(long serverTime) {
	}

	public void simulationStarted(int realSimID, String type, long startTime,
			long endTime, String timeUnitName, int timeUnitCount) {
		simulationPanel.simulationStarted(startTime, endTime, timeUnitCount);
	}

	public void simulationStopped(int realSimID) {
		// This must be done with event dispatch thread. FIX THIS!!!
		simulationPanel.simulationStopped();
	}

	public void nextSimulation(int publicSimID, long startTime) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}// A cache with values for agent + type (bank account, etc)

	public void intCache(int agent, int type, int[] cache) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	public void participant(int agent, int role, String name, int participantID) {
		simulationPanel.participant(agent, role, name, participantID);
	}

	public void nextTimeUnit(int timeUnit) {
		simulationPanel.nextTimeUnit(timeUnit);
	}

	public void dataUpdated(int agent, int type, int value) {
		simulationPanel.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, long value) {
		simulationPanel.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, float value) {
		simulationPanel.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, double value) {
		simulationPanel.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, String value) {
		simulationPanel.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, Transportable value) {
		simulationPanel.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int type, Transportable value) {
		Class valueType = value.getClass();
		if (valueType == RetailCatalog.class) {
			this.catalog = (RetailCatalog) value;
		}
		simulationPanel.dataUpdated(type, value);
	}

	public void interaction(int fromAgent, int toAgent, int type) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	public void interactionWithRole(int fromAgent, int role, int type) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	// -------------------------------------------------------------------
	// API towards agent views
	// -------------------------------------------------------------------

	public String getRoleName(int role) {
		return role >= 0 && role < ROLE_NAME.length ? ROLE_NAME[role] : Integer
				.toString(role);
	}

	public String getAgentName(int agentIndex) {
		return simulationPanel.getAgentName(agentIndex);
	}

	public RetailCatalog getCatalog() {
		return catalog;
	}
}
