/*
 * QueryUserInteractionPanel.java
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
import edu.umich.eecs.tac.props.Ad;
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
public class QueryUserInteractionPanel {
  JPanel mainPane;
  JLabel impLabel, clicksLabel, convLabel, ctrLabel, convRateLabel;
  public static final String IMPRESSIONS_STRING = "Impressions: ";
  public static final String CLICKS_STRING = "Clicks: ";
  public static final String CONVERSIONS_STRING = "Conversions: ";
  public static final String CTR_STRING = "CTR: ";
  public static final String CONV_RATE_STRING = "Conv. Rate: ";
  public static final DecimalFormat dFormat = new DecimalFormat("##.##%");
  //public static final DecimalFormat pFormat = new DecimalFormat("#0.###");


  //GameInfo gameInfo;
  Query query;
  Advertiser advertiser;
  PositiveBoundedRangeModel dayModel;
  public QueryUserInteractionPanel(Query query, Advertiser advertiser, PositiveBoundedRangeModel dm){
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
    
    impLabel = new JLabel();
    clicksLabel = new JLabel();
    convLabel = new JLabel();
    ctrLabel = new JLabel();
    convRateLabel = new JLabel();

    mainPane.add(impLabel);
    mainPane.add(clicksLabel);
    mainPane.add(convLabel);
    mainPane.add(ctrLabel);
    mainPane.add(convRateLabel);
    
    updateMePlz();

  }

  private void updateMePlz(){
    int current = dayModel.getCurrent();
    QueryReport q_report = advertiser.getQueryReport(current+1);
    SalesReport s_report = advertiser.getSalesReport(current+1);
    if(q_report == null || s_report == null){//TODO-Don't assume both will be null or both will exist.
      setDefaultText();
    }else{
      int impressions = q_report.getImpressions(query);
      int clicks = q_report.getClicks(query);
      int conversions = s_report.getConversions(query);
      impLabel.setText(IMPRESSIONS_STRING+q_report.getImpressions(query));
      clicksLabel.setText(CLICKS_STRING+q_report.getClicks(query));
      convLabel.setText(CONVERSIONS_STRING+s_report.getConversions(query));
      ctrLabel.setText(CTR_STRING+dFormat.format(calcCTR(impressions, clicks)));
      convRateLabel.setText(CONV_RATE_STRING+dFormat.format(calcConvRate(conversions, clicks)));
    }

    
    
  }

  private void setDefaultText(){

    impLabel.setText(IMPRESSIONS_STRING+0);
    clicksLabel.setText(CLICKS_STRING+0);
    convLabel.setText(CONVERSIONS_STRING+0);
    ctrLabel.setText(CTR_STRING+0.0+"%");
    convRateLabel.setText(CONV_RATE_STRING+0.0+"%");
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


  public Component getMainPane() {
    return mainPane;
  }

}
