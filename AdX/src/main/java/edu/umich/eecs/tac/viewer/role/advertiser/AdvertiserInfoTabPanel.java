/*
 * AdvertiserInfoTabPanel.java
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

import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import edu.umich.eecs.tac.viewer.auction.ResultsPageModel;
import edu.umich.eecs.tac.viewer.role.SimulationTabPanel;
import se.sics.isl.transport.Transportable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Guha Balakrishnan
 */
public class AdvertiserInfoTabPanel extends SimulationTabPanel {

    private int agent;
    private String advertiser;
    private TACAASimulationPanel simulationPanel;
    private JTabbedPane tabbedPane;
    private Map<Query, AdvertiserQueryTabPanel> advertiserQueryTabPanels;
    private Map<Query, ResultsPageModel> models;
    private Color legendColor;

    public AdvertiserInfoTabPanel(int agent, String advertiser, Map<Query, ResultsPageModel> models,
                                  TACAASimulationPanel simulationPanel, Color legendColor) {
        super(simulationPanel);
        this.agent = agent;
        this.advertiser = advertiser;
        this.simulationPanel = simulationPanel;
        this.models = models;
        this.legendColor = legendColor;

        simulationPanel.addViewListener(new CatalogListener());
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        setBackground(TACAAViewerConstants.CHART_BACKGROUND);
        advertiserQueryTabPanels = new HashMap<Query, AdvertiserQueryTabPanel>();
        tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
        tabbedPane.setBackground(TACAAViewerConstants.CHART_BACKGROUND);
        tabbedPane.add("Main", new AdvertiserMainTabPanel(simulationPanel, agent, advertiser, legendColor));
    }

    private void handleRetailCatalog(RetailCatalog retailCatalog) {
        advertiserQueryTabPanels.clear();
        for (Product product : retailCatalog) {
            // Create f0
            Query f0 = new Query();

            // Create f1's
            Query f1Manufacturer = new Query(product.getManufacturer(), null);
            Query f1Component = new Query(null, product.getComponent());

            // Create f2
            Query f2 = new Query(product.getManufacturer(), product
                    .getComponent());

            createAdvertiserQueryTabPanels(f0);
            createAdvertiserQueryTabPanels(f1Manufacturer);
            createAdvertiserQueryTabPanels(f1Component);
            createAdvertiserQueryTabPanels(f2);
        }


        for (Query query : advertiserQueryTabPanels.keySet()) {
            tabbedPane.add(String.format("(%s,%s)", query.getManufacturer(), query.getComponent()),
                    advertiserQueryTabPanels.get(query));
        }
        add(tabbedPane);
    }

    private void createAdvertiserQueryTabPanels(Query query) {
        ResultsPageModel model = models.get(query);

        if (model == null) {
            model = new ResultsPageModel(query, simulationPanel);
            models.put(query, model);
        }

        if (!advertiserQueryTabPanels.containsKey(query)) {
            advertiserQueryTabPanels.put(query,
                                         new AdvertiserQueryTabPanel(agent, advertiser, query,
                                                                     model, simulationPanel, legendColor));
        }
    }

    private class CatalogListener extends ViewAdaptor {

        public void dataUpdated(int type, Transportable value) {
            Class valueType = value.getClass();
            if (valueType == RetailCatalog.class) {
                handleRetailCatalog((RetailCatalog) value);
            }
        }
    }
}
