/*
 * RevCostPanel.java
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

package edu.umich.eecs.tac.viewer.role.adx;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import edu.umich.eecs.tac.viewer.role.SimulationTabPanel;

/**
 * @author Guha Balakrishnan
 */
public class AdxRevCostPanel extends SimulationTabPanel {

	private final Map<String, AdNetRevCostPanel> agentPanels;

	public AdxRevCostPanel(TACAASimulationPanel simulationPanel) {
		super(simulationPanel);

		agentPanels = new HashMap<String, AdNetRevCostPanel>();

		simulationPanel.addViewListener(new ParticipantListener());

		initialize();
	}

	private void initialize() {
		setLayout(new GridLayout(2, 4));
		setBackground(TACAAViewerConstants.CHART_BACKGROUND);
		setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Daily Revenue and Cost",
				TitledBorder.CENTER, TitledBorder.DEFAULT_JUSTIFICATION));
	}

	private class ParticipantListener extends ViewAdaptor {

		@Override
		public void participant(int agent, int role, String name,
				int participantID) {
			if (!agentPanels.containsKey(name)
					&& role == TACAdxConstants.AD_NETOWRK_ROLE_ID) {
				AdNetRevCostPanel agentRevCostPanel = new AdNetRevCostPanel(
						agent, name, getSimulationPanel(), false);

				agentPanels.put(name, agentRevCostPanel);

				add(agentRevCostPanel);
			}
		}
	}
}
