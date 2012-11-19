/*
 * OverviewTransactionPanel.java
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

import edu.umich.eecs.tac.logviewer.info.Advertiser;
import edu.umich.eecs.tac.logviewer.info.GameInfo;
import edu.umich.eecs.tac.logviewer.gui.UpdatablePanel;
import edu.umich.eecs.tac.logviewer.gui.PositiveBoundedRangeModel;
import edu.umich.eecs.tac.props.QueryReport;
import edu.umich.eecs.tac.props.SalesReport;
import edu.umich.eecs.tac.props.Query;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.Set;

/**
 * @author Lee Callender
 */
public class OverviewTransactionPanel extends UpdatablePanel {
  JLabel costLabel, revenueLabel, profitLabel;
  public static final String COST_STRING = "Total Cost: ";
  public static final String REVENUE_STRING = "Total Revenue: ";
  public static final String PROFIT_STRING = "Total Profit: ";
  public static final DecimalFormat dFormat = new DecimalFormat("$#0.00");
  Query[] querySpace;


  Advertiser advertiser;
  
  public OverviewTransactionPanel(Advertiser advertiser, PositiveBoundedRangeModel dm, GameInfo gameInfo) {
    super(dm);
    this.advertiser = advertiser;
    this.querySpace = gameInfo.getQuerySpace().toArray(new Query[0]);

    mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
    mainPane.setBorder(BorderFactory.createTitledBorder
			   (BorderFactory.createEtchedBorder(),"Transactions"));

    costLabel = new JLabel();
    revenueLabel = new JLabel();
    profitLabel = new JLabel();

    mainPane.add(costLabel);
    mainPane.add(revenueLabel);
    mainPane.add(profitLabel);

    updateMePlz();

  }

  private void setDefaultText(){
    costLabel.setText(COST_STRING+"$0.00");
    revenueLabel.setText(REVENUE_STRING+"$0.00");
    profitLabel.setText(PROFIT_STRING+"$0.00");
  }


  protected void updateMePlz(){
    int current = dayModel.getCurrent();
    QueryReport q_report = advertiser.getQueryReport(current+1);
    SalesReport s_report = advertiser.getSalesReport(current+1);
    if(q_report == null || s_report == null){//TODO-Don't assume both will be null or both will exist.
      setDefaultText();
    }else{
      double cost = 0.0D;
      double revenue = 0.0D;
      for(int i = 0; i < querySpace.length; i++){
        cost += q_report.getCost(querySpace[i]);
        revenue +=  s_report.getRevenue(querySpace[i]);
      }
      double profit = revenue - cost;
      costLabel.setText(COST_STRING+dFormat.format(cost));
      revenueLabel.setText(REVENUE_STRING+dFormat.format(revenue));
      profitLabel.setText(PROFIT_STRING+dFormat.format(profit));
    }
  }

}
