/*
 * AdvertiserOverviewMetricsPanel.java
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

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReport;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReportEntry;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReportKey;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;

/**
 * @author Tomer Greenwald
 */
public class CampaignAuctionReportPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final AdvertiserMetricsModel model;
	private int winnerRow;

	public CampaignAuctionReportPanel(CampaignAuctionReport campaignAuctionReport, final TACAASimulationPanel simulationPanel) {
		model = new AdvertiserMetricsModel(campaignAuctionReport, simulationPanel);
		winnerRow = 0;
		for (int i = 0; i < campaignAuctionReport.size(); i++) {
			CampaignAuctionReportEntry entry = campaignAuctionReport.getEntry(i);
			if(entry.getKey().getAdnetName().equals(campaignAuctionReport.getWinner())) {
				break;
			} else {
				winnerRow++;
			}
		}
		initialize();
	}

	private void initialize() {
		setLayout(new GridLayout(1, 1));
		setBorder(BorderFactory.createTitledBorder("Advertiser Information"));
		setBackground(TACAAViewerConstants.CHART_BACKGROUND);

		MetricsNumberRenderer renderer = new MetricsNumberRenderer();
		JTable table = new JTable(model);
		for (int i = 1; i < 4; i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane);
	}

	private static class AdvertiserMetricsModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private static final String[] COLUMN_NAMES = new String[] { "Ad Network", "Effective bid",
			"Actual Bid", "Quality Rating"};

		private CampaignAuctionReport campaignAuctionReport;

		private AdvertiserMetricsModel(
				CampaignAuctionReport campaignAuctionReport, final TACAASimulationPanel simulationPanel) {
			this.campaignAuctionReport = campaignAuctionReport;

		}
		
		@Override
		public int getRowCount() {
			return campaignAuctionReport.size();
		}

		@Override
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			CampaignAuctionReportEntry entry = campaignAuctionReport.getEntry(rowIndex);
			
			if (columnIndex == 0) {
				return entry.getKey().getAdnetName();
			} else if (columnIndex == 1) {
				return entry.getEffctiveBid();
			} else if (columnIndex == 2) {
				return entry.getActualBid();
			} else if (columnIndex == 3) {
				return entry.getEffctiveBid() / entry.getActualBid();
			}
			return null;
		}

		@Override
		public String getColumnName(int column) {
			return COLUMN_NAMES[column];
		}
	}

	public class MetricsNumberRenderer extends JLabel implements
			TableCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MetricsNumberRenderer() {
			setOpaque(true);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object object, boolean isSelected, boolean hasFocus, int row,
				int column) {
			

			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				if(row == winnerRow) {
					if(row == 0) {
						setBackground(Color.orange);
					} else {
						setBackground(Color.magenta);
					}
				} else {
				setBackground(table.getBackground());
				setForeground(table.getForeground());
				}
			}

			setHorizontalAlignment(JLabel.RIGHT);
			setText(String.format("%.2f", object));

			return this;
		}
	}
}
