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

package edu.umich.eecs.tac.viewer.role.advertiser;

import edu.umich.eecs.tac.TACAAConstants;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import static edu.umich.eecs.tac.viewer.ViewerChartFactory.createDaySeriesChartWithColors;
import edu.umich.eecs.tac.viewer.role.SimulationTabPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import se.sics.tasim.viewer.TickListener;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick Jordan
 */
public class AdvertiserCountTabPanel extends SimulationTabPanel {
    Map<Integer, String> agents;

    private int currentDay;
    private XYSeriesCollection impressions;
    private XYSeriesCollection clicks;
    private XYSeriesCollection conversions;
    private Map<String, XYSeries> impressionsMap;
    private Map<String, XYSeries> clicksMap;
    private Map<String, XYSeries> conversionsMap;

    public AdvertiserCountTabPanel(TACAASimulationPanel simulationPanel) {
        super(simulationPanel);

        agents = new HashMap<Integer, String>();
        impressionsMap = new HashMap<String, XYSeries>();
        clicksMap = new HashMap<String, XYSeries>();
        conversionsMap = new HashMap<String, XYSeries>();

        simulationPanel.addViewListener(new DataUpdateListener());
        simulationPanel.addTickListener(new DayListener());
        initialize();
    }

    private void initialize() {
        setLayout(new GridLayout(3, 1));
        setBackground(TACAAViewerConstants.CHART_BACKGROUND);

        add(new ChartPanel(createImpressionsChart()));
        add(new ChartPanel(createClicksChart()));
        add(new ChartPanel(createConversionsChart()));

        setBorder(BorderFactory.createTitledBorder("Counts"));

    }

    private JFreeChart createConversionsChart() {
        conversions = new XYSeriesCollection();
        return createDaySeriesChartWithColors("Convs", conversions, true);
    }

    private JFreeChart createClicksChart() {
        clicks = new XYSeriesCollection();
        return createDaySeriesChartWithColors("Clicks", clicks, false);
    }

    private JFreeChart createImpressionsChart() {
        impressions = new XYSeriesCollection();
        return createDaySeriesChartWithColors("Imprs", impressions, false);
    }

    protected void addImpressions(String advertiser, int impressions) {

        this.impressionsMap.get(advertiser).addOrUpdate(currentDay, impressions);
    }

    protected void addClicks(String advertiser, int clicks) {
        this.clicksMap.get(advertiser).addOrUpdate(currentDay, clicks);
    }

    protected void addConversions(String advertiser, int conversions) {
        this.conversionsMap.get(advertiser).addOrUpdate(currentDay, conversions);
    }

    private class DataUpdateListener extends ViewAdaptor {

        public void dataUpdated(final int agent, final int type, final int value) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    String agentAddress = agents.get(agent);

                    if (agentAddress != null) {
                        switch (type) {
                            case TACAAConstants.DU_IMPRESSIONS:
                                addImpressions(agentAddress, value);
                                break;
                            case TACAAConstants.DU_CLICKS:
                                addClicks(agentAddress, value);
                                break;
                            case TACAAConstants.DU_CONVERSIONS:
                                addConversions(agentAddress, value);
                                break;
                        }
                    }
                }
            });
        }

        public void participant(final int agent, final int role, final String name, final int participantID) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    handleParticipant(agent, role, name, participantID);
                }
            });
        }
    }

    protected class DayListener implements TickListener {

        public void tick(long serverTime) {
            AdvertiserCountTabPanel.this.tick(serverTime);
        }

        public void simulationTick(long serverTime, int simulationDate) {
            AdvertiserCountTabPanel.this.simulationTick(serverTime, simulationDate);
        }
    }

    protected void tick(long serverTime) {
    }

    protected void simulationTick(long serverTime, int simulationDate) {
        currentDay = simulationDate;
    }

    private void handleParticipant(int agent, int role, String name, int participantID) {
        if (!agents.containsKey(agent) && role == TACAAConstants.ADVERTISER) {
            agents.put(agent, name);
            XYSeries impressionsSeries = new XYSeries(name);
            XYSeries clicksSeries = new XYSeries(name);
            XYSeries conversionsSeries = new XYSeries(name);
            impressionsMap.put(name, impressionsSeries);
            impressions.addSeries(impressionsSeries);
            clicksMap.put(name, clicksSeries);
            clicks.addSeries(clicksSeries);
            conversionsMap.put(name, conversionsSeries);
            conversions.addSeries(conversionsSeries);
        }
    }
}
