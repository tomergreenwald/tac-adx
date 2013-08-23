/*
 * AdvertiserMainTabPanel.java
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

package edu.umich.eecs.tac.viewer.role.advertiser;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import tau.tac.adx.report.demand.CampaignReport;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.role.SimulationTabPanel;

/**
 * @author Guha Balakrishnan
 */
public class CampaignGrpahsTabPanel extends SimulationTabPanel {
	private final TACAASimulationPanel simulationPanel;
	private final int agent;
	private final String name;
	private final Color legendColor;
	private final int campaignId;
	private CampaignGrpahsPanel campaignGrpahsPanel;
	private final long expectedImpressionReach;

	public CampaignGrpahsTabPanel(TACAASimulationPanel simulationPanel,
			int agent, String advertiser, Color legendColor, int campaignId,
			long expectedImpressionReach) {
		super(simulationPanel);
		this.simulationPanel = simulationPanel;
		this.agent = agent;
		this.name = advertiser;
		this.legendColor = legendColor;
		this.campaignId = campaignId;
		this.expectedImpressionReach = expectedImpressionReach;

		initialize();
	}

	private void initialize() {
		setLayout(new GridBagLayout());

		campaignGrpahsPanel = new CampaignGrpahsPanel(agent, name,
				simulationPanel, false, campaignId, expectedImpressionReach);

		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = 0;
		c2.gridy = 0;
		c2.weightx = 2;
		c2.weighty = 2;
		c2.fill = GridBagConstraints.BOTH;
		add(campaignGrpahsPanel, c2);

	}

	public void update(CampaignReport campaignReport) {
		campaignGrpahsPanel.updateCampaigns(campaignReport);
	}
}
