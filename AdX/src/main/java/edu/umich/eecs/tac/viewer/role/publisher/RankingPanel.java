/*
 * RankingPanel.java
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

import edu.umich.eecs.tac.props.*;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import se.sics.isl.transport.Transportable;
import tau.tac.adx.sim.TACAdxConstants;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.*;
import java.util.List;


public class RankingPanel extends JPanel {

    private Query query;
    private Map<Integer, String> names;
    private Map<String, Color> colors;
    private MyTableModel model;

    public RankingPanel(Query query, RankingTabPanel rankingTabPanel) {
        super(new GridLayout(1, 0));

        this.query = query;
        this.names = new HashMap<Integer, String>();
        this.colors = new HashMap<String, Color>();

        initialize();

        rankingTabPanel.getSimulationPanel().addViewListener(new AuctionListener());

    }

    protected void initialize() {
        model = new MyTableModel();
        JTable table = new JTable(model);

        table.setDefaultRenderer(String.class, new RankingRenderer(Color.white, Color.black));
        table.setDefaultRenderer(Double.class, new RankingRenderer(Color.white, Color.black));
        table.setDefaultRenderer(Boolean.class, new RankingRenderer(Color.white, Color.black));
        table.setGridColor(Color.white);

        initColumnSizes(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                "Auction For (" + query.getManufacturer() + " , "
                        + query.getComponent() + ")"));
        add(scrollPane);
    }

    public Query getQuery() {
        return query;
    }

    private void handleQueryReport(int agent, QueryReport queryReport) {
        String name = names.get(agent);

        if (name != null) {
            Ad ad = queryReport.getAd(query);
            double position = queryReport.getPosition(query);
            double promotedRatio = 0;
            
            if(queryReport.getImpressions(query)!= 0){
                promotedRatio = (double)queryReport.getPromotedImpressions(query)/(double)queryReport.getImpressions(query);
            }

            //Color bkgndColor= new Color(255, 255, (int)(255 - 255 * promotedRatio));
            Color bkgndColor;
            if(promotedRatio > .5){
                bkgndColor = Color.lightGray;
            }
            else
                bkgndColor = Color.white;
            
            model.handleQueryReportItem(name, ad, position, bkgndColor);
            
        }
    }

    private void handleBidBundle(int agent, BidBundle bundle) {
        String name = names.get(agent);

        if (name != null) {

            double bid = bundle.getBid(query);

            if (!(BidBundle.PERSISTENT_BID == bid || Double.isNaN(BidBundle.PERSISTENT_BID) && Double.isNaN(bid))) {
                 
                model.handleBidBundleItem(name, bid);
            }
        }
    }

    private class AuctionListener extends ViewAdaptor {

        public void dataUpdated(int agent, int type, Transportable value) {
            if (type == TACAdxConstants.DU_QUERY_REPORT && value.getClass().equals(QueryReport.class)) {

                handleQueryReport(agent, (QueryReport) value);

            } else if (type == TACAdxConstants.DU_BIDS && value.getClass().equals(BidBundle.class)) {

                handleBidBundle(agent, (BidBundle) value);
            }
        }

        public void participant(int agent, int role, String name, int participantID) {
            if (role == TACAdxConstants.ADVERTISER) {
                RankingPanel.this.names.put(agent, name);
                int size = RankingPanel.this.names.size();
                RankingPanel.this.colors.put(name, TACAAViewerConstants.LEGEND_COLORS[size - 1]);
            }
        }
    }


    private void initColumnSizes(JTable table) {
        TableColumn column;
        Component comp;
        int headerWidth;
        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < table.getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);
            comp = headerRenderer.getTableCellRendererComponent(
                    null, column.getHeaderValue(),
                    false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            column.setPreferredWidth(headerWidth);
        }
    }

    public class MyTableModel extends AbstractTableModel {

        String[] columnNames = {"Avg. Position", "    Advertiser    ", "  Bid ($)  ", "Targeted"};
        List<ResultsItem> data;
        Map<String, ResultsItem> map;

        private MyTableModel() {
            data = new ArrayList<ResultsItem>();
            map = new HashMap<String, ResultsItem>();
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.size();
        }

        public Color getRowFgndColor(int row) {
            return data.get(row).getFgndColor();
        }

        public Color getRowBkgndColor(int row){
            return data.get(row).getBkgndColor();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return data.get(row).getPosition();
            } else if (col == 1) {
                return data.get(row).getAdvertiser();
            } else if (col == 2) {
                return data.get(row).getBid();
            } else if (col == 3) {
                return data.get(row).isTargeted();
            } else {
                return null;
            }
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public void handleQueryReportItem(final String name, final Ad ad, final double position, final Color bkgndColor) {

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ResultsItem item = map.get(name);

                    if (item != null) {
                        data.remove(item);
                    } else {
                        item = new ResultsItem(name);
                        map.put(name, item);
                    }


                    item.setAd(ad);
                    item.setPosition(position);
                    item.setFgndColor(colors.get(name));
                    item.setBkgndColor(bkgndColor);

                    if (!Double.isNaN(position)) {
                        data.add(item);
                        Collections.sort(data);
                    }

                    fireTableDataChanged();
                }
            });
        }

        public void handleBidBundleItem(final String name, final double bid) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ResultsItem item = map.get(name);

                    if (item != null) {
                        data.remove(item);
                    } else {
                        item = new ResultsItem(name);
                        map.put(name, item);
                    }

                    item.setBid(bid);

                    if (!Double.isNaN(item.getPosition())) {
                        data.add(item);
                        Collections.sort(data);
                    }

                    fireTableDataChanged();
                }
            });

        }
    }

    private static class ResultsItem implements Comparable<ResultsItem> {
        private String advertiser;
        private Ad ad;
        private double position;
        private double bid;
        private Color fgndColor;
        private Color bkgndColor;

        public ResultsItem(String advertiser) {
            this.advertiser = advertiser;
            this.position = Double.NaN;
            this.bid = Double.NaN;
        }

        public void setAd(Ad ad) {
            this.ad = ad;
        }

        public void setPosition(double position) {
            this.position = position;
        }

        public void setBid(double bid) {
            this.bid = bid;
        }

        public void setBkgndColor(Color color){
            this.bkgndColor = color;
        }

        public void setFgndColor(Color color) {
            this.fgndColor = color;
        }

        public double getBid() {
            return bid;
        }

        public String getAdvertiser() {
            return advertiser;
        }

        public Ad getAd() {
            return ad;
        }

        public double getPosition() {
            return position;
        }

        public Color getFgndColor() {
            return fgndColor;
        }

        public Color getBkgndColor(){
            return bkgndColor;
        }

        public boolean isTargeted() {
            return getAd().getProduct() != null;
        }

        public int compareTo(ResultsItem o) {
            return Double.compare(position, o.position);
        }
    }
}

