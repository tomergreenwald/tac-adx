/*
 * AgentRevCostPanel.java
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

import static edu.umich.eecs.tac.viewer.ViewerChartFactory.createDifferenceChart;

import java.awt.GridLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import se.sics.tasim.viewer.TickListener;
import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;

/**
 * @author Guha Balakrishnan
 */
public class AdNetRevCostPanel extends JPanel {
	private final int agent;
	private final String advertiser;
	private final XYSeriesCollection seriescollection;
	private final XYSeries revSeries;
	private final XYSeries costSeries;
	private double aggregatedRevenue;
	private double aggregatedCost;
	private int currentDay;
	private Set<Query> queries;
	private final boolean showBorder;

	public AdNetRevCostPanel(int agent, String advertiser,
			TACAASimulationPanel simulationPanel, boolean showBorder) {

		setBackground(TACAAViewerConstants.CHART_BACKGROUND);
		revSeries = new XYSeries("Revenue");
		costSeries = new XYSeries("Cost");
		seriescollection = new XYSeriesCollection();

		this.showBorder = showBorder;
		this.agent = agent;
		this.advertiser = advertiser;
		simulationPanel.addTickListener(new DayListener());
		simulationPanel.addViewListener(new DataUpdateListener());
		initialize();
	}

	private void initialize() {
		setLayout(new GridLayout(1, 1));
		if (showBorder) {
			setBorder(BorderFactory.createTitledBorder("Revenue and Cost"));
		}
		queries = new HashSet<Query>();
		seriescollection.addSeries(revSeries);
		seriescollection.addSeries(costSeries);

		JFreeChart chart = createDifferenceChart(
				showBorder ? null : advertiser, seriescollection);
		ChartPanel chartpanel = new ChartPanel(chart, false);
		chartpanel.setMouseZoomable(true, false);
		add(chartpanel);
	}

	protected class DayListener implements TickListener {

		@Override
		public void tick(long serverTime) {
			AdNetRevCostPanel.this.tick(serverTime);
		}

		@Override
		public void simulationTick(long serverTime, int simulationDate) {
			AdNetRevCostPanel.this.simulationTick(serverTime, simulationDate);
		}
	}

	protected void tick(long serverTime) {
	}

	protected void simulationTick(long serverTime, int simulationDate) {
		if (currentDay != simulationDate) {
			synchronized (this) {
				currentDay = simulationDate;
			}
		}
	}

	private class DataUpdateListener extends ViewAdaptor {

		@Override
		public void dataUpdated(final int agent, final int type,
				final double value) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (AdNetRevCostPanel.this.agent == agent
							&& type == TACAdxConstants.DU_AD_NETWORK_EXPENSE) {
						synchronized (this) {
							aggregatedCost += value;
							costSeries.addOrUpdate(currentDay, aggregatedCost);
						}
					} else if (AdNetRevCostPanel.this.agent == agent
							&& type == TACAdxConstants.DU_AD_NETWORK_REVENUE) {
						synchronized (this) {
							aggregatedRevenue += value;
							revSeries
									.addOrUpdate(currentDay, aggregatedRevenue);
						}
					}
				}
			});
		}
	}
}
// if (type == TACAdxConstants.DU_AD_NETWORK_REVENUE) {
// costSeries.add(currentDay, value);
// }
// else if (type ==
// TACAdxConstants.DU_AD_NETWORK_REVENUE) {
// revSeries.add(currentDay, value);
// }