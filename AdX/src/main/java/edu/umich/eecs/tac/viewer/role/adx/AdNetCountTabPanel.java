/*
 * AdvertiserCountTabPanel.java
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

import static edu.umich.eecs.tac.viewer.ViewerChartFactory.createDaySeriesChartWithColors;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.viewer.TickListener;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import edu.umich.eecs.tac.viewer.role.SimulationTabPanel;

/**
 * @author Patrick Jordan
 */
public class AdNetCountTabPanel extends SimulationTabPanel {
	Map<Integer, String> agents;

	private int currentDay;
	private XYSeriesCollection targetedImpressions;
	private XYSeriesCollection serviceLevels;
	private XYSeriesCollection qualityRatings;
	private final Map<String, XYSeries> targetedImpressionsMap;
	private final Map<String, XYSeries> serviceLevelMap;
	private final Map<String, XYSeries> qualityRatingsMap;

	public AdNetCountTabPanel(TACAASimulationPanel simulationPanel) {
		super(simulationPanel);

		agents = new HashMap<Integer, String>();
		serviceLevelMap = new HashMap<String, XYSeries>();
		qualityRatingsMap = new HashMap<String, XYSeries>();
		targetedImpressionsMap = new HashMap<String, XYSeries>();

		simulationPanel.addViewListener(new DataUpdateListener());
		simulationPanel.addTickListener(new DayListener());
		initialize();
	}

	private void initialize() {
		setLayout(new GridLayout(3, 1));
		setBackground(TACAAViewerConstants.CHART_BACKGROUND);

		add(new ChartPanel(createTargetedImpressionsChart()));
		add(new ChartPanel(createServiceLevelChart()));
		add(new ChartPanel(createQualityRatingChart()));

		setBorder(BorderFactory.createTitledBorder("Counts"));

	}

	private JFreeChart createQualityRatingChart() {
		qualityRatings = new XYSeriesCollection();
		return createDaySeriesChartWithColors("Quality Rating", qualityRatings,
				true);
	}

	private JFreeChart createServiceLevelChart() {
		serviceLevels = new XYSeriesCollection();
		return createDaySeriesChartWithColors("Service Level", serviceLevels,
				false);
	}

	private JFreeChart createTargetedImpressionsChart() {
		targetedImpressions = new XYSeriesCollection();
		return createDaySeriesChartWithColors("Impressions",
				targetedImpressions, false);
	}

	protected void addTargetedImpressions(String advertiser, int impressions) {

		this.targetedImpressionsMap.get(advertiser).addOrUpdate(currentDay,
				impressions);
	}

	protected void addServiceLevel(String advertiser, double serviceLevel) {
		this.serviceLevelMap.get(advertiser).addOrUpdate(currentDay,
				serviceLevel);
	}

	protected void addQualityRating(String advertiser, double qualityRating) {
		this.qualityRatingsMap.get(advertiser).addOrUpdate(currentDay,
				qualityRating);
	}

	private class DataUpdateListener extends ViewAdaptor {

		@Override
		public void dataUpdated(final int agent, final int type, final int value) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					String agentAddress = agents.get(agent);

					if (agentAddress != null) {
						switch (type) {
						case TACAdxConstants.DU_AD_NETWORK_WIN_COUNT:
							addTargetedImpressions(agentAddress, value);
							break;
						}
					}
				}
			});
		}

		@Override
		public void dataUpdated(final int agent, final int type,
				final double value) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					String agentAddress = agents.get(agent);

					if (agentAddress != null) {
						switch (type) {
						case TACAdxConstants.DU_AD_NETWORK_QUALITY_RATING:
							addQualityRating(agentAddress, value);
							break;
						}
					}
				}
			});
		}

		@Override
		public void dataUpdated(final int agent, final int type,
				final Transportable content) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					String agentAddress = agents.get(agent);

					if (agentAddress != null) {
						switch (type) {
						case TACAdxConstants.DU_DEMAND_DAILY_REPORT:
							AdNetworkDailyNotification adNetworkDailyNotification = (AdNetworkDailyNotification) content;
							addServiceLevel(agentAddress,
									adNetworkDailyNotification
											.getServiceLevel());
							break;
						}
					}
				}
			});
		}

		@Override
		public void participant(final int agent, final int role,
				final String name, final int participantID) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					handleParticipant(agent, role, name, participantID);
				}
			});
		}
	}

	protected class DayListener implements TickListener {

		@Override
		public void tick(long serverTime) {
			AdNetCountTabPanel.this.tick(serverTime);
		}

		@Override
		public void simulationTick(long serverTime, int simulationDate) {
			AdNetCountTabPanel.this.simulationTick(serverTime, simulationDate);
		}
	}

	protected void tick(long serverTime) {
	}

	protected void simulationTick(long serverTime, int simulationDate) {
		currentDay = simulationDate;
	}

	private void handleParticipant(int agent, int role, String name,
			int participantID) {
		if (!agents.containsKey(agent)
				&& role == TACAdxConstants.AD_NETOWRK_ROLE_ID) {
			agents.put(agent, name);
			XYSeries targetedImpressionsSeries = new XYSeries(name);
			targetedImpressionsMap.put(name, targetedImpressionsSeries);
			targetedImpressions.addSeries(targetedImpressionsSeries);
			XYSeries serviceLevelSeries = new XYSeries(name);
			serviceLevelMap.put(name, serviceLevelSeries);
			serviceLevels.addSeries(serviceLevelSeries);
			XYSeries qualityRatingSeries = new XYSeries(name);
			qualityRatingsMap.put(name, qualityRatingSeries);
			qualityRatings.addSeries(qualityRatingSeries);
		}
	}
}
