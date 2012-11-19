/*
 * QueryBidPanel.java
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

import edu.umich.eecs.tac.logviewer.info.GameInfo;
import edu.umich.eecs.tac.logviewer.info.Advertiser;
import edu.umich.eecs.tac.props.*;
import static edu.umich.eecs.tac.logviewer.util.VisualizerUtils.*;
import edu.umich.eecs.tac.logviewer.gui.PositiveBoundedRangeModel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * @author Lee Callender
 */
public class QueryBidPanel {
  JPanel mainPane;
  JLabel bidLabel, reserveLabel, adLabel, cpcLabel, vpcLabel, posLabel;
  public static final String BID_STRING = "Bid: ";
  public static final String RESERVE_STRING = "Spend Limit: ";
  public static final String AD_STRING = "Ad: ";
  public static final String CPC_STRING = "Avg. CPC: ";
  public static final String VPC_STRING = "Avg. VPC: ";
  public static final String POS_STRING = "Avg. Position: ";
  public static final String AD_NULL = "NULL";
  public static final DecimalFormat dFormat = new DecimalFormat("$#0.000");
  public static final DecimalFormat pFormat = new DecimalFormat("#0.###");

  double[] bid;
  double[] reserve;
  Ad[]     ad;
  double[] cpc;
  double[] vpc;
  double[]    pos;
  Query[] querySpace; 

  //GameInfo gameInfo;
  Query query;
  Advertiser advertiser;
  PositiveBoundedRangeModel dayModel;

  public QueryBidPanel(Query query, Advertiser advertiser,
                       PositiveBoundedRangeModel dm, int numDays, Query[] querySpace){
    this.query = query;
    this.advertiser = advertiser;
    this.dayModel = dm;
    this.bid = new double[numDays];
    this.reserve = new double[numDays];
    this.ad = new Ad[numDays];
    this.cpc = new double[numDays];
    this.vpc = new double[numDays];
    this.pos = new double[numDays];
    this.querySpace = querySpace;

    if(dayModel != null) {
	    dayModel.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent ce) {
			    updateMePlz();
		    }
		  });
    }

    applyData();

    mainPane = new JPanel();
    mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
    mainPane.setBorder(BorderFactory.createTitledBorder
			   (BorderFactory.createEtchedBorder(),query.toString()));

    bidLabel = new JLabel();
    reserveLabel = new JLabel();
    adLabel = new JLabel();
    cpcLabel = new JLabel();
    vpcLabel = new JLabel();
    posLabel = new JLabel();

    mainPane.add(bidLabel);
    mainPane.add(reserveLabel);
    mainPane.add(adLabel);
    mainPane.add(cpcLabel);
    mainPane.add(vpcLabel);
    mainPane.add(posLabel);

    updateMePlz();
  }

  private void applyData(){
    bid[0] = Double.NaN;
    reserve[0] = Double.NaN;
    ad[0] = null;
    cpc[0] = Double.NaN;
    vpc[0] = Double.NaN;
    pos[0] = Double.NaN;

    for(int i = 0; i < bid.length - 1; i++){
      BidBundle current = advertiser.getBidBundle(i);
      QueryReport report = advertiser.getQueryReport(i+2);
      SalesReport s_report = advertiser.getSalesReport(i+2);
      //What if advertiser doesn't send BidBundle today?
      if(current != null){
        bid[i+1] = current.getBid(query);
        reserve[i+1] = current.getDailyLimit(query);
        ad[i+1] = current.getAd(query);
      }else{
        bid[i+1] = BidBundle.PERSISTENT_BID;
        reserve[i+1] = BidBundle.PERSISTENT_BID;
        ad[i+1] = BidBundle.PERSISTENT_AD;
      }

      if(report != null){
        cpc[i+1] = report.getCPC(query);
        pos[i+1] = report.getPosition(query);
      }else{
        cpc[i+1] = Double.NaN;
        pos[i+1] = Double.NaN;
      }

      if(s_report != null && report != null){
        vpc[i+1] = (s_report.getRevenue(query) - report.getCost(query))/report.getClicks(query);
      }else{
        vpc[i+1] = Double.NaN;
      }

      if(i != 0){//Does this still apply?
        if((Double.isNaN(bid[i+1]) && Double.isNaN(BidBundle.PERSISTENT_BID)) ||
            bid[i+1] == BidBundle.PERSISTENT_BID ||
            bid[i+1] < 0)

          bid[i+1] = bid[i];

        if((Double.isNaN(reserve[i+1]) && Double.isNaN(BidBundle.PERSISTENT_SPEND_LIMIT)) ||
            reserve[i+1] == BidBundle.PERSISTENT_SPEND_LIMIT)

          reserve[i+1] = reserve[i];

        if(ad[i+1] == BidBundle.PERSISTENT_AD)
          ad[i+1] = ad[i];
      }
    }
  }

  private void updateMePlz(){
    //System.out.println("updating!");
    String s = (""+dayModel.getCurrent());
    int day = dayModel.getCurrent();
    if(Double.isNaN(bid[day]))
      bidLabel.setText(BID_STRING+bid[day]);
    else
      bidLabel.setText(BID_STRING+dFormat.format(bid[day]));
    if(Double.isNaN(reserve[day]))
      reserveLabel.setText(RESERVE_STRING+reserve[day]);
    else
      reserveLabel.setText(RESERVE_STRING+dFormat.format(reserve[day]));
    if(ad[day] != null){

      adLabel.setText(AD_STRING+formatToString(ad[day]));

    }else{

      adLabel.setText(AD_STRING+AD_NULL);


    }if(Double.isNaN(cpc[day]))
      cpcLabel.setText(CPC_STRING+cpc[day]);
    else
      cpcLabel.setText(CPC_STRING+dFormat.format(cpc[day]));
    if(Double.isNaN(vpc[day]))
      vpcLabel.setText(VPC_STRING+vpc[day]);
    else
      vpcLabel.setText(VPC_STRING+dFormat.format(vpc[day]));
    if(Double.isNaN(pos[day]))
      posLabel.setText(POS_STRING+pos[day]);
    else
      posLabel.setText(POS_STRING+pFormat.format(pos[day]));

  }

  public Component getMainPane() {
    return mainPane;
  }

  private boolean validAd(Ad ad){
    if(ad.isGeneric()){
      return true;
    }

    String m1 = ad.getProduct().getManufacturer();
    String c1 = ad.getProduct().getComponent();
    
    for(int i = 0; i < querySpace.length; i++){
      Query q = querySpace[i];
      String m2 = q.getManufacturer();
      String c2 = q.getComponent();

      if(m1.equals(m2) && c1.equals(c2)){
        return true;
      }
    }

    return false;
  }
}
