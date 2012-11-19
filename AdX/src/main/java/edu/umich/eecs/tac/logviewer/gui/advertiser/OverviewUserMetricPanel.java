/*
 * OverviewUserMetricPanel.java
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
import edu.umich.eecs.tac.logviewer.info.GameInfo;
import edu.umich.eecs.tac.logviewer.gui.UpdatablePanel;
import edu.umich.eecs.tac.logviewer.gui.PositiveBoundedRangeModel;

import javax.swing.*;
import java.text.DecimalFormat;

/**
 * @author Lee Callender
 */
public class OverviewUserMetricPanel extends UpdatablePanel {
  JLabel impLabel, clicksLabel, convLabel, ctrLabel, convRateLabel, capAvLabel;
  public static final String IMPRESSIONS_STRING = "Total Impressions: ";
  public static final String CLICKS_STRING = "Total Clicks: ";
  public static final String CONVERSIONS_STRING = "Total Conversions: ";
  public static final String CAPACITY_AVAIL_STRING = "Capacity Available:";
  //public static final String 
  public static final String CTR_STRING = "CTR: ";
  public static final String CONV_RATE_STRING = "Conv. Rate: ";
  public static final DecimalFormat dFormat = new DecimalFormat("###.##%");
  Query[] querySpace;


  Advertiser advertiser;
  int window;

  public OverviewUserMetricPanel(Advertiser advertiser, PositiveBoundedRangeModel dm, GameInfo gameInfo) {
    super(dm);
    this.advertiser = advertiser;
    this.querySpace = gameInfo.getQuerySpace().toArray(new Query[0]);
    this.window = advertiser.getDistributionWindow();

    mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
    mainPane.setBorder(BorderFactory.createTitledBorder
          			   (BorderFactory.createEtchedBorder(),"User Metrics"));

    impLabel = new JLabel();
    clicksLabel = new JLabel();
    convLabel = new JLabel();
    ctrLabel = new JLabel();
    convRateLabel = new JLabel();
    capAvLabel = new JLabel();

    mainPane.add(impLabel);
    mainPane.add(clicksLabel);
    mainPane.add(convLabel);
    mainPane.add(ctrLabel);
    mainPane.add(convRateLabel);
    //mainPane.add(capAvLabel);

    updateMePlz();

  }

  protected void updateMePlz(){
    int current = dayModel.getCurrent();
    QueryReport q_report = advertiser.getQueryReport(current+1);
    SalesReport s_report = advertiser.getSalesReport(current+1);
    if(q_report == null || s_report == null){
      setDefaultText();
    }else{
      int impressions = 0;
      int clicks = 0;
      int conversions = 0;
      for(int i=0; i < querySpace.length; i++){
        impressions += q_report.getImpressions(querySpace[i]);
        clicks += q_report.getClicks(querySpace[i]);
        conversions += s_report.getConversions(querySpace[i]);
      }
      impLabel.setText(IMPRESSIONS_STRING+impressions);
      clicksLabel.setText(CLICKS_STRING+clicks);
      convLabel.setText(CONVERSIONS_STRING+conversions);
      ctrLabel.setText(CTR_STRING+dFormat.format(calcCTR(impressions, clicks)));
      convRateLabel.setText(CONV_RATE_STRING+dFormat.format(calcConvRate(conversions, clicks)));


      int c = 0;
      for(int i = 0; i < window; i++){
        if(current+1-i < 1)
          continue;

        s_report = advertiser.getSalesReport(current+1-i);
        for(int j=0; j < querySpace.length; j++){
          c = s_report.getConversions(querySpace[j]);
        }
      }

      capAvLabel.setText(CAPACITY_AVAIL_STRING+
            dFormat.format(calcCapacityAvail(c, advertiser.getDistributionCapacity())));


    }



  }

  private void setDefaultText(){

    impLabel.setText(IMPRESSIONS_STRING+0);
    clicksLabel.setText(CLICKS_STRING+0);
    convLabel.setText(CONVERSIONS_STRING+0);
    ctrLabel.setText(CTR_STRING+0.0+"%");
    convRateLabel.setText(CONV_RATE_STRING+0.0+"%");
    capAvLabel.setText(CAPACITY_AVAIL_STRING+100.0+"%");
  }

  protected double calcCTR(int impressions, int clicks) {
		if (impressions > 0) {
			return (((double) clicks)
					/ ((double) impressions));
		} else {
			return 0.0D;
		}
	}

	protected double calcConvRate(int conversions, int clicks) {
		if (clicks > 0) {
			return (((double) conversions)
					/ ((double) clicks));
		} else {
			return 0.0D;
		}
	}

  protected double calcCapacityAvail(int conversions, int capacity){
			return (((double) capacity - (double) conversions) / ((double) capacity));
  }
  
}
