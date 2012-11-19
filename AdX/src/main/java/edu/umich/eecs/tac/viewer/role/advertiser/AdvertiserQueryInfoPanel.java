/*
 * AdvertiserQueryInfoPanel.java
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
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryReport;
import edu.umich.eecs.tac.props.SalesReport;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import se.sics.isl.transport.Transportable;

import javax.swing.*;
import java.awt.*;

/**
 * @author Patrick R. Jordan
 */
public class AdvertiserQueryInfoPanel extends JPanel {
    private int agent;
    private String advertiser;
    private Query query;

    private int impressions;
    private int clicks;
    private int conversions;
    private double revenue;
    private double cost;

    private JLabel ctrLabel;
    private JLabel convRateLabel;
    private JLabel cpcLabel;
    private JLabel cpmLabel;
    private JLabel vpcLabel;
    private JLabel roiLabel;

    public AdvertiserQueryInfoPanel(int agent, String advertiser, Query query, TACAASimulationPanel simulationPanel) {
        this.agent = agent;
        this.advertiser = advertiser;
        this.query = query;

        initialize();

        simulationPanel.addViewListener(new DataUpdateListener());
    }

    private void initialize() {
        setLayout(new GridLayout(3, 4));
        setBackground(TACAAViewerConstants.CHART_BACKGROUND);

        ctrLabel = new JLabel("---");
        add(new JLabel("CTR:"));
        add(ctrLabel);

        cpcLabel = new JLabel("---");
        add(new JLabel("CPC:"));
        add(cpcLabel);

        convRateLabel = new JLabel("---");
        add(new JLabel("Conv. Rate:"));
        add(convRateLabel);

        vpcLabel = new JLabel("---");
        add(new JLabel("VPC:"));
        add(vpcLabel);

        cpmLabel = new JLabel("---");
        add(new JLabel("CPM:"));
        add(cpmLabel);

        roiLabel = new JLabel("---");
        add(new JLabel("ROI:"));
        add(roiLabel);

        setBorder(BorderFactory.createTitledBorder("Rate Metrics"));
    }

    public int getAgent() {
        return agent;
    }

    public String getAdvertiser() {
        return advertiser;
    }

    private class DataUpdateListener extends ViewAdaptor {

        public void dataUpdated(final int agent, final int type, final Transportable value) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (agent == AdvertiserQueryInfoPanel.this.agent) {
                        switch (type) {
                            case TACAAConstants.DU_SALES_REPORT:
                                handleSalesReport((SalesReport) value);
                                break;
                            case TACAAConstants.DU_QUERY_REPORT:
                                handleQueryReport((QueryReport) value);
                                break;
                        }
                    }
                }
            });

        }

        private void handleQueryReport(QueryReport queryReport) {
            addImpressions(queryReport.getImpressions(query));
            addClicks(queryReport.getClicks(query));
            addCost(queryReport.getCost(query));

        }

        private void handleSalesReport(SalesReport salesReport) {
            addConversions(salesReport.getConversions(query));
            addRevenue(salesReport.getRevenue(query));
        }
    }

    protected void addRevenue(double revenue) {
        this.revenue += revenue;

        updateCTR();
        updateVPC();
        updateROI();
    }

    protected void addCost(double cost) {
        this.cost += cost;

        updateCPC();
        updateVPC();
        updateCPM();
        updateROI();
    }

    protected void addImpressions(int impressions) {
        this.impressions += impressions;

        updateCTR();
        updateCPM();
    }

    protected void addClicks(int clicks) {
        this.clicks += clicks;

        updateCTR();
        updateConvRate();
        updateCPC();
        updateVPC();
    }

    protected void addConversions(int conversions) {
        this.conversions += conversions;

        updateConvRate();
    }

    protected void updateCTR() {
        if (impressions > 0) {
            ctrLabel.setText(String.format("%.2f%%", (100.0 * ((double) clicks) / ((double) impressions))));
        } else {
            ctrLabel.setText("---");
        }
    }

    protected void updateConvRate() {
        if (clicks > 0) {
            convRateLabel.setText(String.format("%.2f%%", (100.0 * ((double) conversions) / ((double) clicks))));
        } else {
            convRateLabel.setText("---");
        }
    }

    protected void updateCPC() {
        if (clicks > 0) {
            cpcLabel.setText(String.format("%.2f", cost / ((double) clicks)));
        } else {
            cpcLabel.setText("---");
        }
    }

    protected void updateCPM() {
        if (impressions > 0) {
            cpmLabel.setText(String.format("%.2f", cost / (impressions / 1000.0)));
        } else {
            cpmLabel.setText("---");
        }
    }

    protected void updateROI() {
        if (cost > 0.0) {
            roiLabel.setText(String.format("%.2f%%", (100.0 * (revenue - cost) / cost)));
        } else {
            roiLabel.setText("---");
        }
    }

    protected void updateVPC() {
        if (clicks > 0) {
            vpcLabel.setText(String.format("%.2f", (revenue - cost) / ((double) clicks)));
        } else {
            vpcLabel.setText("---");
        }
    }
}
