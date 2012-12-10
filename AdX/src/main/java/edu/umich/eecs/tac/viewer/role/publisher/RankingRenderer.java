/*
 * RankingRenderer.java
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

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author Guha Balakrishnan
 */
class RankingRenderer extends DefaultTableCellRenderer {
    private Color bkgndColor;
    private Color fgndColor;
    private Font cellFont;

    public RankingRenderer(Color bkgnd, Color foregnd) {
        super();
        bkgndColor = bkgnd;
        fgndColor = foregnd;
        cellFont = new Font("serif", Font.BOLD, 12);
    }

    public Component getTableCellRendererComponent
            (JTable table, Object value, boolean isSelected,
             boolean hasFocus, int row, int column) {

        fgndColor  = ((RankingPanel.MyTableModel)table.getModel()).getRowFgndColor(row);
        bkgndColor = ((RankingPanel.MyTableModel)table.getModel()).getRowBkgndColor(row);


        if (value.getClass().equals(Double.class)) {
            value = round((Double) value, 3);
        }

        if(value.getClass() == Boolean.class){
            boolean targeted = (Boolean) value;
            JCheckBox checkBox = new JCheckBox();
            if (targeted) {
               checkBox.setSelected(true);
            }
            checkBox.setForeground(fgndColor);
            checkBox.setBackground(bkgndColor);
            checkBox.setHorizontalAlignment((int) JCheckBox.CENTER_ALIGNMENT);
            return checkBox;
        }

  
        JLabel cell = (JLabel) super.getTableCellRendererComponent(
                       table, value, isSelected, hasFocus, row, column);

        cell.setForeground(fgndColor);
        cell.setBackground(bkgndColor);
        cell.setFont(cellFont);
        cell.setHorizontalAlignment((int) JLabel.CENTER_ALIGNMENT);

        return cell;
    }

    public static double round(double Rval, int Rpl) {
        double p = Math.pow(10, Rpl);
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return tmp / p;
    }
}
