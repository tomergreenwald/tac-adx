/*
 * ProfitPanel.java
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

import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import static edu.umich.eecs.tac.viewer.ViewerChartFactory.createDaySeriesChartWithColor;
import edu.umich.eecs.tac.viewer.role.SimulationTabPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import se.sics.tasim.viewer.TickListener;
import tau.tac.adx.sim.TACAdxConstants;

import javax.swing.*;
import java.awt.*;

/**
 * @author Guha Balakrishnan
 */
public class ProfitPanel extends SimulationTabPanel {

    private int currentDay;
    private String advertiser;
    private int agent;
    private XYSeries series;
    private Color legendColor;

    public ProfitPanel(TACAASimulationPanel simulationPanel, int agent,
                       String advertiser, Color legendColor) {
        super(simulationPanel);

        this.agent = agent;
        this.advertiser = advertiser;
        currentDay = 0;
        this.legendColor = legendColor;


        simulationPanel.addTickListener(new DayListener());
        simulationPanel.addViewListener(new BankStatusListener());
        initialize();
    }

    protected void initialize() {
        setLayout(new GridLayout(1, 1));
        setBorder(BorderFactory.createTitledBorder("Advertiser Profit"));
        setBackground(TACAAViewerConstants.CHART_BACKGROUND);

        XYSeriesCollection seriescollection = new XYSeriesCollection();
        series = new XYSeries(advertiser);
        seriescollection.addSeries(series);

        JFreeChart chart = createDaySeriesChartWithColor(null, seriescollection, legendColor);
        ChartPanel chartpanel = new ChartPanel(chart, false);
        chartpanel.setMouseZoomable(true, false);

        add(chartpanel);
    }

    protected void tick(long serverTime) {
    }

    protected void simulationTick(long serverTime, int simulationDate) {
        currentDay = simulationDate;
    }

    protected class DayListener implements TickListener {

        public void tick(long serverTime) {
            ProfitPanel.this.tick(serverTime);
        }

        public void simulationTick(long serverTime, int simulationDate) {
            ProfitPanel.this.simulationTick(serverTime, simulationDate);
        }
    }

    protected class BankStatusListener extends ViewAdaptor {

        public void dataUpdated(final int agent, final int type, final double value) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (type == TACAdxConstants.DU_BANK_ACCOUNT && agent == ProfitPanel.this.agent) {
                        series.addOrUpdate(currentDay, value);
                    }
                }
            });

        }
    }
}