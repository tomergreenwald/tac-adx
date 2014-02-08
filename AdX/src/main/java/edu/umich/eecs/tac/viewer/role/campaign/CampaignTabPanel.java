/*
 * AdvertiserTabPanel.java
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

package edu.umich.eecs.tac.viewer.role.campaign;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTabbedPane;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.viewer.TickListener;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReport;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import edu.umich.eecs.tac.viewer.auction.ResultsPageModel;
import edu.umich.eecs.tac.viewer.role.SimulationTabPanel;

/**
 * @author Tomer Greenwald
 */
public class CampaignTabPanel extends SimulationTabPanel {
	private JTabbedPane tabbedPane;

	private int currentDay;
	private final Map<Integer, CampaignInfoTabPanel> campaignInfoPanels;
	private final Map<Query, ResultsPageModel> resultsPageModels;
	private int participantNum;
	private final TACAASimulationPanel simulationPanel;

	public CampaignTabPanel(TACAASimulationPanel simulationPanel) {
		super(simulationPanel);
		participantNum = 0;
		campaignInfoPanels = new HashMap<Integer, CampaignInfoTabPanel>();
		resultsPageModels = new HashMap<Query, ResultsPageModel>();

		this.simulationPanel = simulationPanel;
		simulationPanel.addViewListener(new ParticipantListener());
		simulationPanel.addTickListener(new DayListener());
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		setBackground(TACAAViewerConstants.CHART_BACKGROUND);
		tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
		tabbedPane.setBackground(TACAAViewerConstants.CHART_BACKGROUND);
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	protected class DayListener implements TickListener {
		@Override
		public void simulationTick(long serverTime, int simulationDate) {
			currentDay = simulationDate;
		}

		@Override
		public void tick(long serverTime) {
		}
	}

	private class ParticipantListener extends ViewAdaptor {
		
		@Override
		public void dataUpdated(int type, Transportable value) {
			if(value instanceof CampaignAuctionReport) {
				CampaignAuctionReport campaignAuctionReport = (CampaignAuctionReport) value;
				if(!campaignInfoPanels.containsKey(campaignAuctionReport.getCampaignID())) {
					CampaignInfoTabPanel infoPanel = new CampaignInfoTabPanel(campaignAuctionReport,
							simulationPanel);

					campaignInfoPanels.put(campaignAuctionReport.getCampaignID(), infoPanel);
					tabbedPane.addTab("Day #"+ currentDay + " - " + campaignAuctionReport.getCampaignID(), infoPanel);
					participantNum++;
				}
			}
		}
	}

}
