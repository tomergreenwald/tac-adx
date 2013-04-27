/*
 * AdvertiserRateMetricsPanel.java
 * 
 * Copyright (C) 2006-2009 Patrick R. Jordan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.umich.eecs.tac.viewer.role.advertiser;

import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import se.sics.isl.transport.Transportable;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;

/**
 * @author Patrick R. Jordan
 */
public class AdvertiserRateMetricsPanel extends JPanel {
	private final int agent;
	private final String advertiser;
	private final Set<AdNetworkDailyNotification> campaigns;

	private final boolean advertiserBorder;
	private JTextArea area;

	public AdvertiserRateMetricsPanel(int agent, String advertiser,
			TACAASimulationPanel simulationPanel, boolean advertiserBorder) {
		this.agent = agent;
		this.advertiser = advertiser;
		this.advertiserBorder = advertiserBorder;
		initialize();

		simulationPanel.addViewListener(new DataUpdateListener());
		campaigns = new HashSet<AdNetworkDailyNotification>();
	}

	private void initialize() {
		setLayout(new GridLayout(6, 2));
		setBackground(TACAAViewerConstants.CHART_BACKGROUND);

		area = new JTextArea();
		add(area);

	}

	public int getAgent() {
		return agent;
	}

	public String getAdvertiser() {
		return advertiser;
	}

	protected void updateCampaigns(AdNetworkDailyNotification campaignMessage) {
		if (!campaigns.contains(campaignMessage)
				&& advertiser.equals(campaignMessage.getWinner())
				&& campaignMessage.getCost() > 0) {
			String campaignAllocatedTo = " allocated to "
					+ campaignMessage.getWinner();

			campaignAllocatedTo = " WON at cost " + campaignMessage.getCost();

			String message = "Day " + campaignMessage.getEffectiveDay() + ": "
					+ campaignAllocatedTo + ". UCS Level set to "
					+ campaignMessage.getServiceLevel() + " at price "
					+ campaignMessage.getPrice() + " Quality Score is: "
					+ campaignMessage.getQualityScore();
			area.append(message);
			area.append("\r\n");
		}
	}

	protected void updateCampaigns(InitialCampaignMessage campaignMessage) {
		String message = "Day 0: Beginning at " + campaignMessage.getDayStart()
				+ ", ending at " + campaignMessage.getDayEnd()
				+ ", market segments: " + campaignMessage.getTargetSegment();
		area.append(message);
		area.append("\r\n");
	}

	private class DataUpdateListener extends ViewAdaptor {

		@Override
		public void dataUpdated(final int agentId, final int type,
				final Transportable value) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					switch (type) {
					case TACAdxConstants.DU_UCS_REPORT:
						updateCampaigns((AdNetworkDailyNotification) value);
						break;
					case TACAdxConstants.DU_INITIAL_CAMPAIGN:
						if (agent == agentId) {
							updateCampaigns((InitialCampaignMessage) value);
						}
						break;
					}

				}
			});

		}
	}
}
