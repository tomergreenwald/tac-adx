/*
 * QuerySalesPanel.java
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

package edu.umich.eecs.tac.logviewer.gui.advertiser;

import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryReport;
import edu.umich.eecs.tac.props.SalesReport;
import edu.umich.eecs.tac.logviewer.info.Advertiser;
import edu.umich.eecs.tac.logviewer.gui.PositiveBoundedRangeModel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.text.DecimalFormat;
import java.awt.*;

/**
 * @author Lee Callender
 */
public class QuerySalesPanel {
  JPanel mainPane;
  JLabel costLabel, revenueLabel, profitLabel;
  public static final String COST_STRING = "Cost: ";
  public static final String REVENUE_STRING = "Revenue: ";
  public static final String PROFIT_STRING = "Profit: ";
  public static final DecimalFormat dFormat = new DecimalFormat("$#0.00");
  //public static final DecimalFormat pFormat = new DecimalFormat("#0.###");


  //GameInfo gameInfo;
  Query query;
  Advertiser advertiser;
  PositiveBoundedRangeModel dayModel;
  public QuerySalesPanel(Query query, Advertiser advertiser, PositiveBoundedRangeModel dm){
    this.query = query;
    this.advertiser = advertiser;
    this.dayModel = dm;

    if(dayModel != null) {
	    dayModel.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent ce) {
			    updateMePlz();
		    }
		  });
    }

    mainPane = new JPanel();
    mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
    mainPane.setBorder(BorderFactory.createTitledBorder
			   (BorderFactory.createEtchedBorder(),query.toString()));

    costLabel = new JLabel();
    revenueLabel = new JLabel();
    profitLabel = new JLabel();

    mainPane.add(costLabel);
    mainPane.add(revenueLabel);
    mainPane.add(profitLabel);

    updateMePlz();

  }

  private void updateMePlz(){
    int current = dayModel.getCurrent();
    QueryReport q_report = advertiser.getQueryReport(current+1);
    SalesReport s_report = advertiser.getSalesReport(current+1);
    if(q_report == null || s_report == null){
      setDefaultText();
    }else{
      double cost = q_report.getCost(query);
      double revenue =  s_report.getRevenue(query);
      double profit = revenue - cost;
      costLabel.setText(COST_STRING+dFormat.format(cost));
      revenueLabel.setText(REVENUE_STRING+dFormat.format(revenue));
      profitLabel.setText(PROFIT_STRING+dFormat.format(profit));
    }
  }

  private void setDefaultText(){
    costLabel.setText(COST_STRING+"$0.00");
    revenueLabel.setText(REVENUE_STRING+"$0.00");
    profitLabel.setText(PROFIT_STRING+"$0.00");
  }



  public Component getMainPane() {
    return mainPane;
  }

}

