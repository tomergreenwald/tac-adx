/*
 * UserPanel.java
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

package edu.umich.eecs.tac.viewer.role;

import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import static edu.umich.eecs.tac.viewer.ViewerChartFactory.createDaySeriesChartWithColors;
import static tau.tac.adx.sim.TACAdxConstants.*;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import se.sics.tasim.viewer.TickListener;

import javax.swing.*;
import java.awt.*;

/**
 * @author Patrick Jordan
 */
public class UserPanel extends SimulationTabPanel {
    private XYSeries nsTimeSeries;
    private XYSeries isTimeSeries;
    private XYSeries f0TimeSeries;
    private XYSeries f1TimeSeries;
    private XYSeries f2TimeSeries;
    private XYSeries tTimeSeries;

    private XYSeriesCollection seriescollection;
    private int currentDay;

    public UserPanel(TACAASimulationPanel simulationPanel) {
        super(simulationPanel);
        setBorder(BorderFactory.createTitledBorder("User State Distribution"));

        currentDay = 0;

        initializeView();

        simulationPanel.addTickListener(new DayListener());
        simulationPanel.addViewListener(new UserSearchStateListener());
    }

    protected void initializeView() {
        createDataset();
        setLayout(new BorderLayout());
        setBackground(TACAAViewerConstants.CHART_BACKGROUND);
        JFreeChart jfreechart = createDaySeriesChartWithColors(null, "Users per state", seriescollection, true);
        ChartPanel chartpanel = new ChartPanel(jfreechart, false);
        chartpanel.setMouseZoomable(true, false);

        add(chartpanel, BorderLayout.CENTER);
    }

    protected void nextTimeUnit(long serverTime, int timeUnit) {
        currentDay = timeUnit;
    }

    private class UserSearchStateListener extends ViewAdaptor {

        public void dataUpdated(final int agent, final int type, final int value) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    switch (type) {
                        case DU_NON_SEARCHING:
                            nsTimeSeries.addOrUpdate(currentDay, value);
                            break;
                        case DU_INFORMATIONAL_SEARCH:
                            isTimeSeries.addOrUpdate(currentDay, value);
                            break;
                        case DU_FOCUS_LEVEL_ZERO:
                            f0TimeSeries.addOrUpdate(currentDay, value);
                            break;
                        case DU_FOCUS_LEVEL_ONE:
                            f1TimeSeries.addOrUpdate(currentDay, value);
                            break;
                        case DU_FOCUS_LEVEL_TWO:
                            f2TimeSeries.addOrUpdate(currentDay, value);
                            break;
                        case DU_TRANSACTED:
                            tTimeSeries.addOrUpdate(currentDay, value);
                            break;
                        default:
                            break;
                    }
                }
            });

        }
    }

    private void createDataset() {
        nsTimeSeries = new XYSeries("NS");
        isTimeSeries = new XYSeries("IS");
        f0TimeSeries = new XYSeries("F0");
        f1TimeSeries = new XYSeries("F1");
        f2TimeSeries = new XYSeries("F2");
        tTimeSeries = new XYSeries("T");

        seriescollection = new XYSeriesCollection();
        seriescollection.addSeries(isTimeSeries);
        seriescollection.addSeries(f0TimeSeries);
        seriescollection.addSeries(f1TimeSeries);
        seriescollection.addSeries(f2TimeSeries);
        seriescollection.addSeries(tTimeSeries);
    }

    protected class DayListener implements TickListener {
        public void tick(long serverTime) {
        }

        public void simulationTick(long serverTime, int simulationDate) {
            UserPanel.this.nextTimeUnit(serverTime, simulationDate);
        }
    }
}
