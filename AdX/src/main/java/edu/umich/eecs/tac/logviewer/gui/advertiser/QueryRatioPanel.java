/*
 * QueryRatioPanel.java
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

import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryReport;
import edu.umich.eecs.tac.props.SalesReport;
import edu.umich.eecs.tac.logviewer.info.Advertiser;
import edu.umich.eecs.tac.logviewer.gui.PositiveBoundedRangeModel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.data.general.ValueDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.Range;

/**
 * @author Lee Callender
 */
public class QueryRatioPanel {
   JPanel mainPane;

  private DefaultValueDataset ctrValue;
	private DefaultValueDataset convValue;

  //GameInfo gameInfo;
  Query query;
  Advertiser advertiser;
  PositiveBoundedRangeModel dayModel;

  public QueryRatioPanel(Query query, Advertiser advertiser, PositiveBoundedRangeModel dayModel){
    this.query = query;
    this.advertiser = advertiser;
    this.dayModel = dayModel;

    if(dayModel != null) {
	    dayModel.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent ce) {
			    updateMePlz();
		    }
		  });
    }

    mainPane = new JPanel();
    mainPane.setLayout(new GridLayout(2, 1));
    mainPane.setBorder(BorderFactory.createTitledBorder(query.toString()));
    //smainPane.setMaximumSize(new Dimension(10,10));

    ChartPanel CTRChart = new ChartPanel(createCTRChart());
    ChartPanel CONVChart = new ChartPanel(createConvChart());
    //CTRChart.setMaximumDrawHeight(100);
    //CTRChart.setMaximumDrawWidth(50);
    //CONVChart.setMaximumDrawHeight(100);
    //CONVChart.setMaximumDrawWidth(50);
    mainPane.add(CTRChart);
		mainPane.add(CONVChart);

}

  private JFreeChart createCTRChart() {
		return createChart("CTR", ctrValue = new DefaultValueDataset(0.0));
	}

	private JFreeChart createConvChart() {
		return createChart("Conv Rate",
				convValue = new DefaultValueDataset(0.0));
	}

  private JFreeChart createChart(String s, ValueDataset dataset) {
		MeterPlot meterplot = new MeterPlot(dataset);
		meterplot.setDialShape(DialShape.CHORD);
		meterplot.setRange(new Range(0.0D, 100D));
		meterplot.addInterval(new MeterInterval("", new Range(0, 100.0D),
				Color.lightGray, new BasicStroke(2.0F),
				new Color(0, 255, 0, 64)));
		meterplot.setNeedlePaint(Color.darkGray);
		meterplot.setDialBackgroundPaint(Color.white);
		meterplot.setDialOutlinePaint(Color.gray);
		meterplot.setMeterAngle(260);
		meterplot.setTickLabelsVisible(true);
		meterplot.setTickLabelFont(new Font("Dialog", 1, 10));
		meterplot.setTickLabelPaint(Color.darkGray);
		meterplot.setTickSize(5D);
		meterplot.setTickPaint(Color.lightGray);
		meterplot.setValuePaint(Color.black);
		meterplot.setValueFont(new Font("Dialog", 1, 14));
		meterplot.setUnits("%");
		return new JFreeChart(s, JFreeChart.DEFAULT_TITLE_FONT, meterplot,
				false);
	}

  protected void updateCTR(int impressions, int clicks) {
		if (impressions > 0) {
			ctrValue.setValue(100.0 * ((double) clicks)
					/ ((double) impressions));
		} else {
			ctrValue.setValue(0.0D);
		}
	}

	protected void updateConvRate(int conversions, int clicks) {
		if (clicks > 0) {
			convValue.setValue(100.0 * ((double) conversions)
					/ ((double) clicks));
		} else {
			convValue.setValue(0.0D);
		}
	}

  private void updateMePlz(){
    /*int impressions, clicks, conversions;
    int currentDay = dayModel.getCurrent();
    QueryReport q_report = advertiser.getQueryReport(dayModel.getCurrent()+1);
    SalesReport s_report = advertiser.getSalesReport(dayModel.getCurrent()+1);
    if(s_report != null && q_report != null){
      updateCTR(q_report.getImpressions(query), q_report.getClicks(query));
      updateConvRate(s_report.getConversions(query), q_report.getClicks(query));
    }else{
      updateCTR(0,0);
      updateConvRate(0,0);
    } */

  }

  public Component getMainPane() {
    return mainPane;
  }
}
