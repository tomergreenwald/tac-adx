/*
 * AdvertiserQueryTabPanel.java
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

import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.auction.AverageRankingPanel;
import edu.umich.eecs.tac.viewer.auction.ResultsPageModel;
import edu.umich.eecs.tac.viewer.role.AgentSupport;

import javax.swing.*;
import java.awt.*;

/**
 * @author Guha Balakrishnan and Patrick Jordan
 */
public class AdvertiserQueryTabPanel extends JPanel {
    private int agent;
    private String advertiser;
    private Query query;
    private ResultsPageModel resultsPageModel;
    private TACAASimulationPanel simulationPanel;
    private Color legendColor;
    private AgentSupport agentSupport;

    public AdvertiserQueryTabPanel(int agent, String advertiser, Query query, ResultsPageModel resultsPageModel,TACAASimulationPanel simulationPanel,
                                   Color legendColor) {
        this.agent = agent;
        this.advertiser = advertiser;
        this.query = query;
        this.simulationPanel = simulationPanel;
        this.legendColor = legendColor;
        this.resultsPageModel = resultsPageModel;
        agentSupport = new AgentSupport();
        simulationPanel.addViewListener(agentSupport);

        initialize();
    }

    private void initialize() {
        setLayout(new GridLayout(1, 2));
        setBackground(TACAAViewerConstants.CHART_BACKGROUND);

        GridBagConstraints c;

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 4;
        c.fill = GridBagConstraints.BOTH;
        AverageRankingPanel averageRankingPanel = new AverageRankingPanel(resultsPageModel);
        leftPanel.add(averageRankingPanel, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        AdvertiserQueryInfoPanel queryInfoPanel = new AdvertiserQueryInfoPanel(agent, advertiser, query, simulationPanel);
        leftPanel.add(queryInfoPanel, c);

        add(leftPanel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());


        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;


        AdvertiserQueryPositionPanel positionPanel = new AdvertiserQueryPositionPanel(agent, advertiser, query, simulationPanel,
                legendColor);
        rightPanel.add(positionPanel, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 3;
        c.fill = GridBagConstraints.BOTH;
        AdvertiserQueryCountPanel queryCountPanel = new AdvertiserQueryCountPanel(agent, advertiser, query, simulationPanel, legendColor);
        rightPanel.add(queryCountPanel, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        c.weighty = 1;
        AdvertiserQueryValuePanel queryValuePanel = new AdvertiserQueryValuePanel(agent, advertiser, query, simulationPanel);
        rightPanel.add(queryValuePanel, c);
        add(rightPanel);
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