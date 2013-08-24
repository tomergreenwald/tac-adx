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

import static edu.umich.eecs.tac.viewer.ViewerChartFactory.createDifferenceChart;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.viewer.TickListener;
import tau.tac.adx.demand.CampaignStats;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.report.demand.CampaignReport;
import tau.tac.adx.report.demand.CampaignReportEntry;
import tau.tac.adx.report.demand.CampaignReportKey;
import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;

/**
 * @author Patrick R. Jordan
 */
public class CampaignGrpahsPanel extends JPanel {
	private static final double ERR_A = 4.08577;
	private static final double ERR_B = 3.08577;
	private final int agent;
	private final String advertiser;
	private final Set<AdNetworkDailyNotification> campaigns;

	private final boolean advertiserBorder;
	private final Map<Integer, XYSeries> campaignSeries;
	private int counter;
	private int campaignId = 0;
	private int currentDay;
	private final CampaignReportKey key;
	private XYSeries reachSeries;
	private final long expectedImpressionReach;
	private double err;
	private XYSeries maxSeries;

	public CampaignGrpahsPanel(int agent, String advertiser,
			TACAASimulationPanel simulationPanel, boolean advertiserBorder,
			int campaignId, long expectedImpressionReach) {
		this.agent = agent;
		this.advertiser = advertiser;
		this.advertiserBorder = advertiserBorder;
		campaignSeries = new HashMap<Integer, XYSeries>();
		this.campaignId = campaignId;
		key = new CampaignReportKey(campaignId);
		this.expectedImpressionReach = expectedImpressionReach;
		initialize();

		simulationPanel.addViewListener(new DataUpdateListener());
		campaigns = new HashSet<AdNetworkDailyNotification>();
		simulationPanel.addTickListener(new DayListener());
	}

	private void initialize() {
		setLayout(new GridLayout(1, 1));
		setBackground(TACAAViewerConstants.CHART_BACKGROUND);
	}

	private void createGraph(XYSeries reachSeries) {
		XYSeriesCollection seriescollection = new XYSeriesCollection(
				reachSeries);

		maxSeries = new XYSeries("Total");
		for (int i = 1; i <= 1000; i++) {
			double err = calcEffectiveReachRatio(expectedImpressionReach * i
					/ 1000, expectedImpressionReach);
			maxSeries.add(expectedImpressionReach * i / 1000, err);
		}

		seriescollection.addSeries(maxSeries);
		JFreeChart chart = createDifferenceChart(advertiserBorder ? null
				: advertiser, seriescollection, "Reach Count",
				"Reach percentage");
		ChartPanel chartpanel = new ChartPanel(chart, false);
		chartpanel.setMouseZoomable(true, false);
		add(chartpanel);
		this.repaint();
	}

	public int getAgent() {
		return agent;
	}

	public String getAdvertiser() {
		return advertiser;
	}

	protected void updateCampaigns(CampaignReport campaignReport) {

		CampaignReportEntry campaignReportEntry = campaignReport.getEntry(key);
		if (campaignReportEntry == null) {
			return;
		}
		if (!campaignSeries.containsKey(campaignId)) {
			String string = "Campaign " + campaignId;
			reachSeries = new XYSeries(string);
			campaignSeries.put(campaignId, reachSeries);
			createGraph(reachSeries);
		}
		CampaignStats campaignStats = campaignReportEntry.getCampaignStats();
		double targetedImps = campaignStats.getTargetedImps();
		if (targetedImps != 0) {
			for (int i = 1; i <= 100; i++) {
				double index = targetedImps * i / 100;
				double err = calcEffectiveReachRatio(index,
						expectedImpressionReach);
				reachSeries.add(index, err);
			}

			for (int i = 1; i <= 1000; i++) {
				long index = expectedImpressionReach * i / 1000;
				if (index < targetedImps) {
					maxSeries.remove(index);
					maxSeries.add(index, 0);
				}
			}
			this.repaint();
		}
	}

	private double calcEffectiveReachRatio(double currentReach,
			double expectedReach) {
		return 2
				/ ERR_A
				* (Math.atan(ERR_A * currentReach / expectedReach - ERR_B) - Math
						.atan(-ERR_B));
	}

	protected class DayListener implements TickListener {

		@Override
		public void simulationTick(long serverTime, int simulationDate) {
			CampaignGrpahsPanel.this.simulationTick(serverTime, simulationDate);
		}

		@Override
		public void tick(long serverTime) {
		}
	}

	protected void simulationTick(long serverTime, int simulationDate) {
		currentDay = simulationDate;
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
						case TACAdxConstants.DU_CAMPAIGN_REPORT:
							updateCampaigns((CampaignReport) value);
							break;
						// case TACAdxConstants.DU_INITIAL_CAMPAIGN:
						// if (agent == agentId) {
						// updateCampaigns((InitialCampaignMessage) value);
						// }
						// break;
						}
					}

				}
			});

		}
	}
}
