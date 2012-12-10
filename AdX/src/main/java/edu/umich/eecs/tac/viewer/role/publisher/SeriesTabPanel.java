/*
 * SeriesTabPanel.java
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

import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import edu.umich.eecs.tac.viewer.role.AgentSupport;
import edu.umich.eecs.tac.viewer.role.SimulationTabPanel;
import se.sics.isl.transport.Transportable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Guha Balakrishnan
 */
public class SeriesTabPanel extends SimulationTabPanel {
    private Map<Query, SeriesPanel> seriesPanels;
    private AgentSupport agentSupport;


    public SeriesTabPanel(TACAASimulationPanel simulationPanel) {
        super(simulationPanel);

        agentSupport = new AgentSupport();

        simulationPanel.addViewListener(new BidBundleListener());
        simulationPanel.addViewListener(agentSupport);

        initialize();
    }

    private void initialize() {
        setBackground(TACAAViewerConstants.CHART_BACKGROUND);
        seriesPanels = new HashMap<Query, SeriesPanel>();
    }

    private void handleRetailCatalog(RetailCatalog retailCatalog) {

        this.removeAll();
        seriesPanels.clear();

        for (Product product : retailCatalog) {
            // Create f0
            Query f0 = new Query();

            // Create f1's
            Query f1Manufacturer = new Query(product.getManufacturer(), null);
            Query f1Component = new Query(null, product.getComponent());

            // Create f2
            Query f2 = new Query(product.getManufacturer(), product.getComponent());

            addSeriesPanel(f0);
            addSeriesPanel(f1Manufacturer);
            addSeriesPanel(f1Component);
            addSeriesPanel(f2);
        }

        int panelCount = seriesPanels.size();
        Math.ceil(Math.sqrt(panelCount));

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        c.ipady = 200;


        int index = 0;
        for (Query query : seriesPanels.keySet()) {
            SeriesPanel temp = seriesPanels.get(query);
            c.gridx = index/4;
            c.gridy = index%4;
            add(temp, c);
            index++;
        }
        
        LegendPanel legendPanel = new LegendPanel(this, TACAAViewerConstants.LEGEND_COLORS);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 4;
        c.ipadx = 500;
        c.ipady = 0;
        c.gridwidth = 4;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(5, 0, 0, 0);

        legendPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        c.anchor = GridBagConstraints.PAGE_END;
        add(legendPanel, c);
    }

    private void addSeriesPanel(Query query) {
        if (!seriesPanels.containsKey(query)) {
            seriesPanels.put(query, new SeriesPanel(query, this));
        }
    }

    private class BidBundleListener extends ViewAdaptor {
        public void dataUpdated(final int type, final Transportable value) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Class valueType = value.getClass();
                    if (valueType == RetailCatalog.class) {
                        handleRetailCatalog((RetailCatalog) value);
                    }
                }
            });
        }
    }

    public int getAgentCount() {
        return agentSupport.size();
    }

    public int getAgent(int index) {
        return agentSupport.agent(index);
    }

    public int getRole(int index) {
        return agentSupport.role(index);
    }

    public int getParticipant(int index) {
        return agentSupport.participant(index);
    }

    public int indexOfAgent(int agent) {
        return agentSupport.indexOfAgent(agent);
    }

    public String getAgentName(int index) {
        return agentSupport.name(index);
    }


}

