/*
 * SeriesPanel.java
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

package edu.umich.eecs.tac.viewer.role.publisher;

import edu.umich.eecs.tac.props.BidBundle;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import static edu.umich.eecs.tac.viewer.ViewerChartFactory.createAuctionChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.viewer.TickListener;
import tau.tac.adx.sim.TACAdxConstants;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick Jordan
 */
public class SeriesPanel extends JComponent {
    private Query query;

    private int currentDay;
    private Map<String, XYSeries> bidSeries;
    private SeriesTabPanel seriesTabPanel;
    private JFreeChart chart;

    public SeriesPanel(Query query, SeriesTabPanel seriesTabPanel) {
        this.query = query;
        this.bidSeries = new HashMap<String, XYSeries>();
        this.seriesTabPanel = seriesTabPanel;
        this.currentDay = 0;

        initialize();

        seriesTabPanel.getSimulationPanel().addViewListener(new BidBundleListener());
        seriesTabPanel.getSimulationPanel().addTickListener(new DayListener());
    }

    protected void initialize() {
        setLayout(new GridLayout(1, 1));


        XYSeriesCollection seriescollection = new XYSeriesCollection();

        // Participants will be added to the publisher panel before getting
        // here.
        int count = seriesTabPanel.getAgentCount();

        for (int index = 0; index < count; index++) {
            if (seriesTabPanel.getRole(index) == TACAdxConstants.ADVERTISER) {
                XYSeries series = new XYSeries(seriesTabPanel
                        .getAgentName(index));

                bidSeries.put(seriesTabPanel.getAgentName(index), series);
                seriescollection.addSeries(series);
            }
        }

        chart = createAuctionChart(getQuery(), seriescollection);
        ChartPanel chartpanel = new ChartPanel(chart, false);
        chartpanel.setMouseZoomable(true, false);
        add(chartpanel);
    }    

    public XYLineAndShapeRenderer getRenderer() {
        return (XYLineAndShapeRenderer) ((XYPlot) chart.getPlot()).getRenderer();
    }

    public Query getQuery() {
        return query;
    }

    private class BidBundleListener extends ViewAdaptor {

        public void dataUpdated(final int agent, final int type, final Transportable value) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (type == TACAdxConstants.DU_BIDS
                            && value.getClass().equals(BidBundle.class)) {
                        int index = seriesTabPanel.indexOfAgent(agent);
                        String name = index < 0 ? null : seriesTabPanel
                                .getAgentName(index);

                        if (name != null) {
                            XYSeries timeSeries = bidSeries.get(name);

                            if (timeSeries != null) {

                                BidBundle bundle = (BidBundle) value;

                                double bid = bundle.getBid(query);
                                if (!Double.isNaN(bid)) {
                                    timeSeries.addOrUpdate(currentDay - 1, bid);
                                }
                            }
                        }
                    }
                }
            });

        }
    }

    protected class DayListener implements TickListener {

        public void tick(long serverTime) {
            SeriesPanel.this.tick(serverTime);
        }

        public void simulationTick(long serverTime, int simulationDate) {
            SeriesPanel.this.simulationTick(serverTime, simulationDate);
        }
    }

    protected void tick(long serverTime) {
    }

    protected void simulationTick(long serverTime, int simulationDate) {
        currentDay = simulationDate;
    }
}