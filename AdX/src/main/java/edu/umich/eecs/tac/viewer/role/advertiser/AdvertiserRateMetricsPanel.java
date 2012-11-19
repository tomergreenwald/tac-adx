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


import edu.umich.eecs.tac.TACAAConstants;
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
public class AdvertiserRateMetricsPanel extends JPanel {
    private int agent;
    private String advertiser;

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

    private boolean advertiserBorder;

    public AdvertiserRateMetricsPanel(int agent, String advertiser,
                                      TACAASimulationPanel simulationPanel, boolean advertiserBorder) {
        this.agent = agent;
        this.advertiser = advertiser;
        this.advertiserBorder = advertiserBorder;
        initialize();

        simulationPanel.addViewListener(new DataUpdateListener());
    }

    private void initialize() {
        setLayout(new GridLayout(6, 2));
        setBackground(TACAAViewerConstants.CHART_BACKGROUND);

        ctrLabel = new JLabel("---");
        ctrLabel.setToolTipText("Click-through Rate");
        add(new JLabel("CTR:"));
        add(ctrLabel);

        convRateLabel = new JLabel("---");
        convRateLabel.setToolTipText("Conversion Rate");
        add(new JLabel("Conv. Rate:"));
        add(convRateLabel);

        cpmLabel = new JLabel("---");
        add(new JLabel("CPM:"));
        add(cpmLabel);

        cpcLabel = new JLabel("---");
        cpcLabel.setToolTipText("Cost Per Click");
        add(new JLabel("CPC:"));
        add(cpcLabel);

        vpcLabel = new JLabel("---");
        vpcLabel.setToolTipText("Value Per Click");
        add(new JLabel("VPC:"));
        add(vpcLabel);

        roiLabel = new JLabel("---");
        roiLabel.setToolTipText("Return on Investment");
        add(new JLabel("ROI:"));
        add(roiLabel);

        if (advertiserBorder)
            setBorder(BorderFactory.createTitledBorder(advertiser));
        else
            setBorder(BorderFactory.createTitledBorder("Rate Metrics"));
    }

    public int getAgent() {
        return agent;
    }

    public String getAdvertiser() {
        return advertiser;
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

    private class DataUpdateListener extends ViewAdaptor {

        public void dataUpdated(final int agent, final int type, final int value) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (agent == AdvertiserRateMetricsPanel.this.agent) {
                        switch (type) {
                            case TACAAConstants.DU_IMPRESSIONS:
                                addImpressions(value);
                                break;
                            case TACAAConstants.DU_CLICKS:
                                addClicks(value);
                                break;
                            case TACAAConstants.DU_CONVERSIONS:
                                addConversions(value);
                                break;
                        }
                    }
                }
            });

        }

        public void dataUpdated(final int agent, final int type, final Transportable value) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (agent == AdvertiserRateMetricsPanel.this.agent) {
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
            double cost = 0.0;

            for (int i = 0; i < queryReport.size(); i++) {
                cost += queryReport.getCost(i);
            }

            addCost(cost);

        }

        private void handleSalesReport(SalesReport salesReport) {
            double revenue = 0.0;

            for (int i = 0; i < salesReport.size(); i++) {
                revenue += salesReport.getRevenue(i);
            }
            addRevenue(revenue);
        }
    }
}
