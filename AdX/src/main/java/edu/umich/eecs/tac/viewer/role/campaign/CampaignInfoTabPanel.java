/*
 * AdvertiserInfoTabPanel.java
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

package edu.umich.eecs.tac.viewer.role.campaign;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.viewer.TickListener;
import tau.tac.adx.agents.CampaignData;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReport;
import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import edu.umich.eecs.tac.viewer.auction.AverageRankingPanel;
import edu.umich.eecs.tac.viewer.auction.ResultsPageModel;
import edu.umich.eecs.tac.viewer.role.SimulationTabPanel;
import edu.umich.eecs.tac.viewer.role.advertiser.AdvertiserMainTabPanel;
import edu.umich.eecs.tac.viewer.role.advertiser.AdvertiserQueryCountPanel;
import edu.umich.eecs.tac.viewer.role.advertiser.AdvertiserQueryInfoPanel;
import edu.umich.eecs.tac.viewer.role.advertiser.AdvertiserQueryPositionPanel;
import edu.umich.eecs.tac.viewer.role.advertiser.AdvertiserQueryTabPanel;
import edu.umich.eecs.tac.viewer.role.advertiser.AdvertiserQueryValuePanel;
import edu.umich.eecs.tac.viewer.role.advertiser.CampaignGrpahsTabPanel;

/**
 * @author Guha Balakrishnan
 */
public class CampaignInfoTabPanel extends SimulationTabPanel {

	private final TACAASimulationPanel simulationPanel;
	private JTabbedPane tabbedPane;
	/**
	 * current day of simulation
	 */
	private int day;
	private CampaignData pendingCampaign;
	/** Related {@link CampaignAuctionReport}.*/
	private CampaignAuctionReport campaignAuctionReport;

	public CampaignInfoTabPanel(CampaignAuctionReport campaignAuctionReport,
			TACAASimulationPanel simulationPanel) {
		super(simulationPanel);
		this.campaignAuctionReport = campaignAuctionReport;
		this.simulationPanel = simulationPanel;

		initialize();
	}

	private void initialize() {
		
		setLayout(new GridLayout(1, 1));
		setBackground(TACAAViewerConstants.CHART_BACKGROUND);

		GridBagConstraints c;

		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		CampaignAuctionReportPanel auctionReportTabPanel = new CampaignAuctionReportPanel(campaignAuctionReport, simulationPanel);
		panel.add(auctionReportTabPanel, c);

		add(panel);
	}
	
}
