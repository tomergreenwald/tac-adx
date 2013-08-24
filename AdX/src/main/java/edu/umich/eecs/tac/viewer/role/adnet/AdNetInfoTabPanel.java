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

package edu.umich.eecs.tac.viewer.role.adnet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.viewer.TickListener;
import tau.tac.adx.agents.CampaignData;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import edu.umich.eecs.tac.viewer.auction.ResultsPageModel;
import edu.umich.eecs.tac.viewer.role.SimulationTabPanel;
import edu.umich.eecs.tac.viewer.role.advertiser.AdvertiserMainTabPanel;
import edu.umich.eecs.tac.viewer.role.advertiser.AdvertiserQueryTabPanel;
import edu.umich.eecs.tac.viewer.role.advertiser.CampaignGrpahsTabPanel;

/**
 * @author Guha Balakrishnan
 */
public class AdNetInfoTabPanel extends SimulationTabPanel {

	private final int agent;
	private final String advertiser;
	private final TACAASimulationPanel simulationPanel;
	private JTabbedPane tabbedPane;
	private Map<Query, AdvertiserQueryTabPanel> advertiserQueryTabPanels;
	private final Map<Query, ResultsPageModel> models;
	private final Color legendColor;
	/**
	 * current day of simulation
	 */
	private int day;
	private CampaignData pendingCampaign;

	public AdNetInfoTabPanel(int agent, String advertiser,
			Map<Query, ResultsPageModel> models,
			TACAASimulationPanel simulationPanel, Color legendColor) {
		super(simulationPanel);
		this.agent = agent;
		this.advertiser = advertiser;
		this.simulationPanel = simulationPanel;
		this.models = models;
		this.legendColor = legendColor;

		simulationPanel.addViewListener(new CatalogListener());
		simulationPanel.addViewListener(new DataUpdateListener());
		simulationPanel.addTickListener(new DayListener());
		initialize();
	}

	protected class DayListener implements TickListener {

		@Override
		public void tick(long serverTime) {
		}

		@Override
		public void simulationTick(long serverTime, int simulationDate) {
			day = simulationDate;
		}
	}

	private void initialize() {
		setLayout(new BorderLayout());
		setBackground(TACAAViewerConstants.CHART_BACKGROUND);
		advertiserQueryTabPanels = new HashMap<Query, AdvertiserQueryTabPanel>();
		tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
		tabbedPane.setBackground(TACAAViewerConstants.CHART_BACKGROUND);
		tabbedPane.add("Main", new AdvertiserMainTabPanel(simulationPanel,
				agent, advertiser, legendColor));
	}

	private void handleCampaign(CampaignOpportunityMessage value) {
		advertiserQueryTabPanels.clear();
		// for (Product product : value) {
		// Create f0
		Query f0 = new Query();

		// // Create f1's
		// Query f1Manufacturer = new Query(product.getManufacturer(), null);
		// Query f1Component = new Query(null, product.getComponent());
		//
		// // Create f2
		// Query f2 = new Query(product.getManufacturer(),
		// product.getComponent());

		// createAdvertiserQueryTabPanels(f1Manufacturer);
		// createAdvertiserQueryTabPanels(f1Component);
		// createAdvertiserQueryTabPanels(f2);
		// }

		for (Query query : advertiserQueryTabPanels.keySet()) {
			tabbedPane.add(
					String.format("(%s,%s)", query.getManufacturer(),
							query.getComponent()),
					advertiserQueryTabPanels.get(query));
		}
		add(tabbedPane);
	}

	private class CatalogListener extends ViewAdaptor {

		@Override
		public void dataUpdated(int type, Transportable value) {
			if (value instanceof CampaignOpportunityMessage) {
				handleCampaign((CampaignOpportunityMessage) value);
			}
		}
	}

	protected void updateCampaigns(AdNetworkDailyNotification campaignMessage) {
		if ((pendingCampaign.getId() == campaignMessage.getCampaignId())
				&& (campaignMessage.getCost() != 0)) {
			CampaignGrpahsTabPanel campaignGrpahsTabPanel = new CampaignGrpahsTabPanel(
					simulationPanel, agent, advertiser, legendColor,
					campaignMessage.getCampaignId(),
					pendingCampaign.getReachImps());
			tabbedPane.add("Day " + (day + 1), campaignGrpahsTabPanel);
			tabbedPane.repaint();
			tabbedPane.revalidate();
		}
	}

	protected void updateCampaigns(InitialCampaignMessage campaignMessage) {
		tabbedPane.add("Day 0", new CampaignGrpahsTabPanel(simulationPanel,
				agent, advertiser, legendColor, campaignMessage.getId(),
				campaignMessage.getReachImps()));
		tabbedPane.repaint();
		tabbedPane.revalidate();
	}

	protected void handleCampaignOpportunityMessage(
			CampaignOpportunityMessage com) {
		pendingCampaign = new CampaignData(com);
	}

	private class DataUpdateListener extends ViewAdaptor {

		@Override
		public void dataUpdated(final int agentId, final int type,
				final Transportable value) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (agentId == agent) {
						switch (type) {
						case TACAdxConstants.DU_DEMAND_DAILY_REPORT:
							updateCampaigns((AdNetworkDailyNotification) value);
							break;
						case TACAdxConstants.DU_INITIAL_CAMPAIGN:
							if (agent == agentId) {
								updateCampaigns((InitialCampaignMessage) value);
							}
							break;
						}
					}
				}
			});

		}

		@Override
		public void dataUpdated(final int type, final Transportable value) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					switch (type) {
					case TACAdxConstants.DU_CAMPAIGN_OPPORTUNITY:
						handleCampaignOpportunityMessage((CampaignOpportunityMessage) value);
						break;
					}
				}
			});

		}
	}
}
