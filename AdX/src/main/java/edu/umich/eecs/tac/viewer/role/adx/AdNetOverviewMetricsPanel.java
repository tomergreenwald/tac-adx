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
package edu.umich.eecs.tac.viewer.role.adx;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;

/**
 * @author Patrick R. Jordan
 * @author greenwald
 */
public class AdNetOverviewMetricsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final AdvertiserMetricsModel model;

	public AdNetOverviewMetricsPanel(final TACAASimulationPanel simulationPanel) {
		model = new AdvertiserMetricsModel(simulationPanel);

		initialize();
	}

	private void initialize() {
		setLayout(new GridLayout(1, 1));
		setBorder(BorderFactory.createTitledBorder("Advertiser Information"));
		setBackground(TACAAViewerConstants.CHART_BACKGROUND);

		MetricsNumberRenderer renderer = new MetricsNumberRenderer();
		JTable table = new JTable(model);
		for (int i = 2; i < 4; i++) {
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

		private static final String[] COLUMN_NAMES = new String[] { "Agent",
				"Quality Rating", "Profit", "revenue", "adx cost", "ucs cost",
				"impressions" };

		List<AdvertiserMetricsItem> data;

		Map<Integer, AdvertiserMetricsItem> agents;

		private AdvertiserMetricsModel(
				final TACAASimulationPanel simulationPanel) {
			data = new ArrayList<AdvertiserMetricsItem>();
			agents = new HashMap<Integer, AdvertiserMetricsItem>();

			simulationPanel.addViewListener(new ViewAdaptor() {
				@Override
				public void participant(int agent, int role, String name,
						int participantID) {
					if (role == TACAdxConstants.AD_NETOWRK_ROLE_ID) {
						if (!agents.containsKey(agent)) {
							AdvertiserMetricsItem item = new AdvertiserMetricsItem(
									agent, name, AdvertiserMetricsModel.this,
									simulationPanel);
							agents.put(agent, item);
							data.add(item);
							fireTableDataChanged();
						}
					}
				}
			});
		}

		public void fireUpdatedAgent(int agent) {
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i).getAgent() == agent) {
					fireTableRowsUpdated(i, i);
				}
			}
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {

			if (columnIndex == 0) {
				return data.get(rowIndex).getAdvertiser();
			} else if (columnIndex == 1) {
				return data.get(rowIndex).getQualityRating();
			} else if (columnIndex == 2) {
				return data.get(rowIndex).getProfit();
			} else if (columnIndex == 3) {
				return data.get(rowIndex).getRevenue();
			} else if (columnIndex == 4) {
				return data.get(rowIndex).getADXCost();
			} else if (columnIndex == 5) {
				return data.get(rowIndex).getUCSCost();
			} else if (columnIndex == 6) {
				return data.get(rowIndex).getImpressions();
			}

			return null;
		}

		@Override
		public String getColumnName(int column) {
			return COLUMN_NAMES[column];
		}
	}

	private static class AdvertiserMetricsItem {
		private final int agent;
		private final String advertiser;

		private int impressions;
		private double revenue;
		private double qualityRating;

		/**
		 * @return the impressions
		 */
		public int getImpressions() {
			return impressions;
		}

		/**
		 * @return the agent's quality rating
		 */
		public double getQualityRating() {
			return qualityRating;
		}

		/**
		 * @return the revenue
		 */
		public double getRevenue() {
			return revenue;
		}

		/**
		 * @return the cost
		 */
		public double getADXCost() {
			return adxCost;
		}

		/**
		 * @return the cost
		 */
		public double getUCSCost() {
			return ucsCost;
		}

		private AdvertiserInfo advertiserInfo;

		private final AdvertiserMetricsModel model;
		private double ucsCost;
		private double adxCost;

		private AdvertiserMetricsItem(int agent, String advertiser,
				AdvertiserMetricsModel model,
				TACAASimulationPanel simulationPanel) {
			this.agent = agent;
			this.advertiser = advertiser;
			this.model = model;

			simulationPanel.addViewListener(new DataUpdateListener(this));
		}

		public int getAgent() {
			return agent;
		}

		public String getAdvertiser() {
			return advertiser;
		}

		public double getProfit() {
			return revenue - adxCost - ucsCost;
		}

		public double getCapacity() {
			return advertiserInfo != null ? advertiserInfo
					.getDistributionCapacity() : Double.NaN;
		}

		protected void addRevenue(double revenue) {
			this.revenue += revenue;

			model.fireUpdatedAgent(agent);
		}

		protected void addADXCost(double cost) {
			this.adxCost += cost;

			model.fireUpdatedAgent(agent);
		}

		protected void addUCSCost(double cost) {
			this.ucsCost += cost;

			model.fireUpdatedAgent(agent);
		}

		protected void addImpressions(int impressions) {
			this.impressions += impressions;

			model.fireUpdatedAgent(agent);
		}

		protected void setQualityRating(double qualityRating) {
			this.qualityRating = qualityRating;

			model.fireUpdatedAgent(agent);
		}

		public void setAdvertiserInfo(AdvertiserInfo advertiserInfo) {
			this.advertiserInfo = advertiserInfo;

			model.fireUpdatedAgent(agent);
		}

		public AdvertiserInfo getAdvertiserInfo() {
			return advertiserInfo;
		}
	}

	private static class DataUpdateListener extends ViewAdaptor {
		private final AdvertiserMetricsItem item;

		private DataUpdateListener(AdvertiserMetricsItem item) {
			this.item = item;
		}

		@Override
		public void dataUpdated(final int agent, final int type, final int value) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (agent == item.getAgent()) {
						switch (type) {
						case TACAdxConstants.DU_AD_NETWORK_WIN_COUNT:
							item.addImpressions(value);
							break;
						}
					}
				}
			});

		}

		@Override
		public void dataUpdated(final int agent, final int type,
				final double value) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (agent == item.getAgent()) {
						switch (type) {
						case TACAdxConstants.DU_AD_NETWORK_REVENUE:
							item.addRevenue(value);
							break;
						case TACAdxConstants.DU_AD_NETWORK_ADX_EXPENSE:
							item.addADXCost(value);
							break;
						case TACAdxConstants.DU_AD_NETWORK_UCS_EXPENSE:
							item.addUCSCost(value);
							break;
						case TACAdxConstants.DU_AD_NETWORK_QUALITY_RATING:
							item.setQualityRating(value);
							break;
						}
					}
				}
			});

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
				setBackground(table.getBackground());
				setForeground(table.getForeground());
			}

			setHorizontalAlignment(JLabel.RIGHT);
			setText(String.format("%.2f", object));

			return this;
		}
	}
}
