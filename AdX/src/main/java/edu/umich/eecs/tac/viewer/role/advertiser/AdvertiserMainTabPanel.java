/*
 * AdvertiserMainTabPanel.java
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
import edu.umich.eecs.tac.viewer.role.AgentRevCostPanel;
import edu.umich.eecs.tac.viewer.role.SimulationTabPanel;

import java.awt.*;

/**
 * @author Guha Balakrishnan
 */
public class AdvertiserMainTabPanel extends SimulationTabPanel {
    private TACAASimulationPanel simulationPanel;
    private int agent;
    private String name;
    private Color legendColor;

    public AdvertiserMainTabPanel(TACAASimulationPanel simulationPanel, int agent,
                                  String advertiser, Color legendColor){
         super(simulationPanel);
         this.simulationPanel = simulationPanel;
         this.agent = agent;
         this.name = advertiser;
         this.legendColor = legendColor;

         initialize();
    }

    private void initialize(){
        setLayout(new GridBagLayout());


        ProfitPanel profitPanel = new ProfitPanel(simulationPanel, agent, name, legendColor);
        AdvertiserRateMetricsPanel ratesMetricsPanel = new AdvertiserRateMetricsPanel(
						                      agent, name, simulationPanel, false);
        AdvertiserCountPanel countPanel = new AdvertiserCountPanel(
						                      agent, name, simulationPanel, false, legendColor);
        AgentRevCostPanel agentRevCostPanel = new AgentRevCostPanel(agent, name, simulationPanel, true);

        AdvertiserPropertiesPanel advertiserPropertiesPanel =
                            new AdvertiserPropertiesPanel(agent, name, simulationPanel);

        AdvertiserCapacityPanel advertiserCapacityPanel =
                            new AdvertiserCapacityPanel(agent, name, simulationPanel, legendColor);


        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 2;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(agentRevCostPanel, c);

        c.gridx = 2;
        c.weightx = 1;
        add(profitPanel, c);

        c.gridx = 3;
        c.weightx = 2;
        add(advertiserCapacityPanel, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 2;
        add(ratesMetricsPanel, c);

        c.gridx = 2;
        c.weightx = 1;
        add(advertiserPropertiesPanel,c);

        c.gridx = 3;
        c.weightx = 2;
        add(countPanel,c);
    }
}
