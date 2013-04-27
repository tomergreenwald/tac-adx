/*
 * AdvertiserTabPanel.java
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

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTabbedPane;

import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import edu.umich.eecs.tac.viewer.auction.ResultsPageModel;
import edu.umich.eecs.tac.viewer.role.advertiser.AdvertiserInfoTabPanel;
import edu.umich.eecs.tac.viewer.role.advertiser.AdvertiserOverviewPanel;

/**
 * @author Patrick Jordan
 */
public class AdvertiserTabPanel extends SimulationTabPanel {
	private JTabbedPane tabbedPane;

	private final Map<String, AdvertiserInfoTabPanel> advertiserInfoPanels;
	private final Map<Query, ResultsPageModel> resultsPageModels;
	private int participantNum;
	private final TACAASimulationPanel simulationPanel;

	public AdvertiserTabPanel(TACAASimulationPanel simulationPanel) {
		super(simulationPanel);
		participantNum = 0;
		advertiserInfoPanels = new HashMap<String, AdvertiserInfoTabPanel>();
		resultsPageModels = new HashMap<Query, ResultsPageModel>();

		this.simulationPanel = simulationPanel;
		simulationPanel.addViewListener(new ParticipantListener());
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		setBackground(TACAAViewerConstants.CHART_BACKGROUND);
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBackground(TACAAViewerConstants.CHART_BACKGROUND);
		add(tabbedPane, BorderLayout.CENTER);

		AdvertiserOverviewPanel overviewPanel = new AdvertiserOverviewPanel(
				getSimulationPanel());
		tabbedPane.addTab("Overview", overviewPanel);
	}

	private class ParticipantListener extends ViewAdaptor {

		@Override
		public void participant(int agent, int role, String name,
				int participantID) {
			if (!advertiserInfoPanels.containsKey(name)
					&& role == TACAdxConstants.AD_NETOWRK_ROLE_ID) {
				AdvertiserInfoTabPanel infoPanel = new AdvertiserInfoTabPanel(
						agent, name, resultsPageModels, simulationPanel,
						TACAAViewerConstants.LEGEND_COLORS[participantNum]);

				advertiserInfoPanels.put(name, infoPanel);
				tabbedPane.addTab(name, infoPanel);
				participantNum++;
			}
		}
	}

}
