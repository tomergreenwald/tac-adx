/*
 * AverageRankingPanel.java
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
package edu.umich.eecs.tac.viewer.auction;

import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;

import javax.swing.*;
import java.awt.*;

/**
 * @author Patrick Jordan
 */
public class AverageRankingPanel extends JPanel {
    private final ResultsPageModel model;

    public AverageRankingPanel(ResultsPageModel model) {
        this.model = model;

        initialize();
    }

    private Query getQuery() {
        return model.getQuery();
    }
    
    private void initialize() {
        setBorder(BorderFactory.createTitledBorder(String.format("(%s,%s) average results", getQuery().getManufacturer(), getQuery().getComponent())));
        setBackground(TACAAViewerConstants.CHART_BACKGROUND);

        JList resultsList = new JList(model);
        resultsList.setCellRenderer(new AdRenderer(getQuery()));
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setLayout(new GridLayout(1, 1));
        add(new JScrollPane(resultsList));
    }    
}
