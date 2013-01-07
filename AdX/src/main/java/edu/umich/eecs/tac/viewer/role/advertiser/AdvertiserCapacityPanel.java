/*
 * AdvertiserCapacityPanel.java
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

import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.SalesReport;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import static edu.umich.eecs.tac.viewer.ViewerChartFactory.createCapacityChart;
import static edu.umich.eecs.tac.viewer.ViewerUtils.buildQuerySpace;
import edu.umich.eecs.tac.viewer.role.SimulationTabPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.viewer.TickListener;
import tau.tac.adx.sim.TACAdxConstants;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Guha Balakrishnan
 */
public class AdvertiserCapacityPanel extends SimulationTabPanel {
    private int agent;
    private int currentDay;
    private XYSeries relativeCapacity;
    private int capacity;
    private int window;
    private Map<Integer, Integer> amountsSold;
    private Set<Query> queries;
    private Color legendColor;

    public AdvertiserCapacityPanel(int agent, String advertiser, TACAASimulationPanel simulationPanel,
                                   Color legendColor) {
        super(simulationPanel);
        this.agent = agent;
        this.legendColor = legendColor;
        currentDay = 0;

        simulationPanel.addViewListener(new DataUpdateListener());
        simulationPanel.addTickListener(new DayListener());
        initialize();
    }

    protected void initialize() {
        setLayout(new GridLayout(1, 1));
        setBorder(BorderFactory.createTitledBorder(" Capacity Used"));
        setBackground(TACAAViewerConstants.CHART_BACKGROUND);

        amountsSold = new HashMap<Integer, Integer>();
        queries = new HashSet<Query>();
        relativeCapacity = new XYSeries("Relative Capacity");
        XYSeriesCollection seriescollection = new XYSeriesCollection();


        seriescollection.addSeries(relativeCapacity);
        JFreeChart chart = createCapacityChart(seriescollection, legendColor);
        ChartPanel chartpanel = new ChartPanel(chart, false);
        chartpanel.setMouseZoomable(true, false);

        add(chartpanel);
    }

    protected class DayListener implements TickListener {

        public void tick(long serverTime) {
            AdvertiserCapacityPanel.this.tick(serverTime);
        }

        public void simulationTick(long serverTime, int simulationDate) {
            AdvertiserCapacityPanel.this.simulationTick(serverTime, simulationDate);
        }
    }

    protected void tick(long serverTime) {
    }

    protected void simulationTick(long serverTime, int simulationDate) {
        currentDay = simulationDate;
    }


    private int getAmountSold(SalesReport report) {

        int result = 0;

        for (Query query : queries) {
            result = result + report.getConversions(query);
        }

        return result;
    }

    private void updateChart() {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                double soldInWindow = 0;

                for (int i = Math.max(0, currentDay - window); i < currentDay; i++) {

                    if (!(amountsSold.get(i) == null || Double.isNaN(amountsSold.get(i)))) {

                        soldInWindow = soldInWindow + amountsSold.get(i);

                    }

                }

                relativeCapacity.addOrUpdate(currentDay, (soldInWindow / capacity) * 100);
            }
        });

    }

    private void handleRetailCatalog(RetailCatalog retailCatalog) {
        buildQuerySpace(queries, retailCatalog);
    }

    private void handleAdvertiserInfo(AdvertiserInfo advertiserInfo) {
        capacity = advertiserInfo.getDistributionCapacity();
        window = advertiserInfo.getDistributionWindow();
    }

    private void handleSalesReport(SalesReport salesReport) {
        int sold = getAmountSold(salesReport);

        amountsSold.put(currentDay - 1, sold);

        updateChart();
    }

    private class DataUpdateListener extends ViewAdaptor {

        public void dataUpdated(int agent, int type, Transportable value) {
            if (AdvertiserCapacityPanel.this.agent == agent
                    && type == TACAdxConstants.DU_ADVERTISER_INFO &&
                    value.getClass() == AdvertiserInfo.class) {
                handleAdvertiserInfo((AdvertiserInfo) value);
            }

            if (AdvertiserCapacityPanel.this.agent == agent &&
                    type == TACAdxConstants.DU_SALES_REPORT &&
                    value.getClass() == SalesReport.class) {
                handleSalesReport((SalesReport) value);
            }
        }

        public void dataUpdated(int type, Transportable value) {
            Class valueType = value.getClass();
            if (valueType == RetailCatalog.class) {
                handleRetailCatalog((RetailCatalog) value);
            }
        }

    }


}
