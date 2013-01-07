/*
 * LegendPanel.java
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

import edu.umich.eecs.tac.viewer.TACAAViewerConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import tau.tac.adx.sim.TACAdxConstants;

import java.awt.*;

/**
 * @author Guha Balakrishnan
 */
public class LegendPanel extends JPanel {
    private SeriesTabPanel seriesTabPanel;
    private Color[] legendColors;

    public LegendPanel(SeriesTabPanel seriesTabPanel, Color[] legendColors) {
        super(new GridLayout(1, 0));
        this.seriesTabPanel = seriesTabPanel;
        this.legendColors = legendColors;
        initialize();
    }

    private void initialize() {

        int count = seriesTabPanel.getAgentCount();

        JTable table = new JTable(1, 2 * (seriesTabPanel.getAgentCount() - 2));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setBorder(BorderFactory.createEmptyBorder());

        for (int i = 0; i < table.getColumnCount(); i = i + 2) {
            table.getColumnModel().getColumn(i).setCellRenderer(
                    new LegendColorRenderer(legendColors[i / 2]));
            table.getColumnModel().getColumn(i).setPreferredWidth(1);

        }
        int advertiser = 0;
        for (int index = 0; index < count; index++) {
            if (seriesTabPanel.getRole(index) == TACAdxConstants.ADVERTISER) {
                table.getColumnModel().getColumn(advertiser * 2 + 1).setCellRenderer(
                        new LegendTextRenderer(seriesTabPanel.getAgentName(index)));
                advertiser++;
            }
        }

        table.setGridColor(TACAAViewerConstants.CHART_BACKGROUND);

        add(table);

    }

    private class LegendColorRenderer extends DefaultTableCellRenderer {
        Color bkgndColor;

        public LegendColorRenderer(Color bkgndColor) {
            super();
            this.bkgndColor = bkgndColor;
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {

            JLabel cell = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            cell.setBackground(bkgndColor);
            return cell;
        }
    }

    private class LegendTextRenderer extends DefaultTableCellRenderer {
        String agent;

        public LegendTextRenderer(String agent) {
            super();
            this.agent = agent;
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel cell = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            cell.setText(agent);
            return cell;
        }
    }
}
