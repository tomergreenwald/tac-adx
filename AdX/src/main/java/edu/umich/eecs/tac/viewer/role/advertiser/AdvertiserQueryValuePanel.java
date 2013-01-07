/*
 * AdvertiserQueryValuePanel.java
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

import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryReport;
import edu.umich.eecs.tac.props.SalesReport;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import static edu.umich.eecs.tac.viewer.ViewerChartFactory.createDifferenceChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.viewer.TickListener;
import tau.tac.adx.sim.TACAdxConstants;

import javax.swing.*;
import java.awt.*;

/**
 * @author Patrick Jordan
 */
public class AdvertiserQueryValuePanel extends JPanel {
    private int agent;
    private Query query;

    private XYSeriesCollection seriescollection;
    private XYSeries revSeries;
    private XYSeries costSeries;
    private int currentDay;

    public AdvertiserQueryValuePanel(int agent, String advertiser, Query query,
                                     TACAASimulationPanel simulationPanel) {

        setBackground(TACAAViewerConstants.CHART_BACKGROUND);
        revSeries = new XYSeries("Revenue");
        costSeries = new XYSeries("Cost");
        seriescollection = new XYSeriesCollection();

        this.agent = agent;
        this.query = query;

        simulationPanel.addTickListener(new DayListener());
        simulationPanel.addViewListener(new DataUpdateListener());
        initialize();
    }

    private void initialize() {
        setLayout(new GridLayout(1, 1));
        seriescollection.addSeries(revSeries);
        seriescollection.addSeries(costSeries);

        JFreeChart chart = createDifferenceChart(seriescollection);
        ChartPanel chartpanel = new ChartPanel(chart, false);
        chartpanel.setMouseZoomable(true, false);
        add(chartpanel);

        setBorder(BorderFactory.createTitledBorder("Revenue and Cost"));
    }

    protected class DayListener implements TickListener {

        public void tick(long serverTime) {
            AdvertiserQueryValuePanel.this.tick(serverTime);
        }

        public void simulationTick(long serverTime, int simulationDate) {
            AdvertiserQueryValuePanel.this.simulationTick(serverTime, simulationDate);
        }
    }

    protected void tick(long serverTime) {
    }

    protected void simulationTick(long serverTime, int simulationDate) {
        currentDay = simulationDate;
    }

    private double getDayCost(QueryReport report) {
        return report.getCost(query);
    }

    private double getDayRevenue(SalesReport report) {
        return report.getRevenue(query);
    }

    private class DataUpdateListener extends ViewAdaptor {

        public void dataUpdated(final int agent, final int type, final Transportable value) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (type == TACAdxConstants.DU_QUERY_REPORT &&
                            value.getClass().equals(QueryReport.class) &&
                            agent == AdvertiserQueryValuePanel.this.agent) {

                        QueryReport queryReport = (QueryReport) value;

                        costSeries.addOrUpdate(currentDay, getDayCost(queryReport));
                    }

                    if (type == TACAdxConstants.DU_SALES_REPORT &&
                            value.getClass().equals(SalesReport.class) &&
                            agent == AdvertiserQueryValuePanel.this.agent) {

                        SalesReport salesReport = (SalesReport) value;

                        revSeries.addOrUpdate(currentDay, getDayRevenue(salesReport));
                    }
                }
            });


        }
    }

}
