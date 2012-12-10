/*
 * RankingTabPanel.java
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
import se.sics.tasim.viewer.TickListener;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Guha Balakrishnan
 */
public class RankingTabPanel extends SimulationTabPanel {
    private Map<Query, RankingPanel> rankingPanels;
    private AgentSupport agentSupport;

    public RankingTabPanel(TACAASimulationPanel simulationPanel) {
        super(simulationPanel);

        agentSupport = new AgentSupport();

        simulationPanel.addViewListener(new CatalogListener());
        simulationPanel.addViewListener(agentSupport);
        simulationPanel.addTickListener(new DayListener());

        initialize();
    }

    private void initialize() {
        setBackground(TACAAViewerConstants.CHART_BACKGROUND);
        rankingPanels = new HashMap<Query, RankingPanel>();
    }

    private void handleRetailCatalog(RetailCatalog retailCatalog) {

        this.removeAll();
        rankingPanels.clear();

        for (Product product : retailCatalog) {
            // Create f0
            Query f0 = new Query();

            // Create f1's
            Query f1Manufacturer = new Query(product.getManufacturer(), null);
            Query f1Component = new Query(null, product.getComponent());

            // Create f2
            Query f2 = new Query(product.getManufacturer(), product.getComponent());

            addRankingPanel(f0);
            addRankingPanel(f1Manufacturer);
            addRankingPanel(f1Component);
            addRankingPanel(f2);
        }

        int panelCount = rankingPanels.size();
        int sideCount = (int) Math.ceil(Math.sqrt(panelCount));

        setLayout(new GridLayout(sideCount, sideCount));


        for (Query query : rankingPanels.keySet()) {
            add(rankingPanels.get(query));
        }
    }

    private void addRankingPanel(Query query) {
        if (!rankingPanels.containsKey(query)) {
            rankingPanels.put(query, new RankingPanel(query, this));
        }
    }

    private class CatalogListener extends ViewAdaptor {

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

    protected class DayListener implements TickListener {

        public void tick(long serverTime) {
            RankingTabPanel.this.tick(serverTime);
        }

        public void simulationTick(long serverTime, int simulationDate) {
            RankingTabPanel.this.simulationTick(serverTime, simulationDate);
        }
    }

    protected void tick(long serverTime) {
    }

    protected void simulationTick(long serverTime, int simulationDate) {
        setBorder(BorderFactory.createTitledBorder(String.format("Auction Results for Day %s", simulationDate - 1)));
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

