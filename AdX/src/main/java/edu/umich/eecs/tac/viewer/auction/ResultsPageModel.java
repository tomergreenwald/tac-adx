/*
 * ResultsPageModel.java
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

import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryReport;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.ViewListener;
import se.sics.isl.transport.Transportable;
import tau.tac.adx.sim.TACAdxConstants;

import javax.swing.*;
import java.util.*;

/**
 * @author Patrick R. Jordan
 */
public class ResultsPageModel extends AbstractListModel implements ViewListener {
    private Query query;
    private List<ResultsItem> results;
    private Map<Integer,ResultsItem> items;
    private Map<Integer,String> names;

    public ResultsPageModel(Query query, TACAASimulationPanel simulationPanel) {
        this.query = query;
        this.results = new ArrayList<ResultsItem>();
        this.names = new HashMap<Integer, String>();
        this.items = new HashMap<Integer, ResultsItem>();

        simulationPanel.addViewListener(this);
    }

    public int getSize() {
        return results.size();
    }

    public Object getElementAt(int index) {
        return results.get(index);
    }

    public Query getQuery() {
        return query;
    }

    public void dataUpdated(int agent, int type, int value) {
    }

    public void dataUpdated(int agent, int type, long value) {
    }

    public void dataUpdated(int agent, int type, float value) {
    }

    public void dataUpdated(int agent, int type, double value) {
    }

    public void dataUpdated(int agent, int type, String value) {
    }

    public void dataUpdated(final int agent, int type, Transportable value) {
        if (type == TACAdxConstants.DU_QUERY_REPORT &&
                value.getClass().equals(QueryReport.class)) {

            final QueryReport queryReport = (QueryReport) value;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {

                    ResultsItem item = items.get(agent);

                    if (item != null) {
                        results.remove(item);
                    }

                    Ad ad = queryReport.getAd(query);

                    double position = queryReport.getPosition(query);

                    if (ad != null && !Double.isNaN(position)) {

                        String advertiser = names.get(agent);

                        if (advertiser != null) {

                            item = new ResultsItem(advertiser, ad, position);

                            results.add(item);

                            items.put(agent, item);

                        }

                    }


                    Collections.sort(results);
                    fireContentsChanged(this, 0, getSize());
                }
            });

        }
    }

    public void dataUpdated(int type, Transportable value) {
    }

    public void participant(int agent, int role, String name, int participantID) {
        if (role == TACAdxConstants.ADVERTISER) {
            names.put(agent, name);
        }
    }
}

