/*
 * ProductPopulationPanel.java
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

package edu.umich.eecs.tac.logviewer.gui;

import edu.umich.eecs.tac.logviewer.info.GameInfo;
import edu.umich.eecs.tac.props.UserPopulationState;
import edu.umich.eecs.tac.props.Product;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author Lee Callender
 **/
public class ProductPopulationPanel {
  JPanel mainPane;
  
  GameInfo gameInfo;
  Product product;

  private static final int numQueryStates = 6;
  private XYSeries nsTimeSeries;
	private XYSeries isTimeSeries;
	private XYSeries f0TimeSeries;
	private XYSeries f1TimeSeries;
	private XYSeries f2TimeSeries;
	private XYSeries tTimeSeries;

	private XYSeriesCollection seriescollection;

  public ProductPopulationPanel(GameInfo gameInfo, Product product){
    this.gameInfo = gameInfo;
    this.product = product;
    
    mainPane = new JPanel();
    mainPane.setLayout(new BorderLayout());
    mainPane.setBorder(BorderFactory.createTitledBorder
			   (BorderFactory.createEtchedBorder(),product.toString()));
    //mainPane.setMinimumSize(new Dimension(280,200));
	  //mainPane.setPreferredSize(new Dimension(280,200));


    createDataset();
    applyData();
    JFreeChart jfreechart = createChart(seriescollection);
    ChartPanel chartpanel = new ChartPanel(jfreechart, false);
		chartpanel.setPreferredSize(new Dimension(300, 200));
		chartpanel.setMouseZoomable(true, false);
    mainPane.add(chartpanel, BorderLayout.CENTER);
  }

  private void applyData(){
    UserPopulationState[] ups = gameInfo.getUserPopulationState();
    for(int currentDay = 0, n = ups.length; currentDay < n; currentDay++){//For every day
      int[] population = ups[currentDay].getDistribution(product);

      for(int k = 0; k < population.length; k++){
        switch(k){
          case 0: //NS
            nsTimeSeries.addOrUpdate(currentDay, population[k]);
            break;
          case 1: //IS
            isTimeSeries.addOrUpdate(currentDay, population[k]);
            break;
          case 2: //F0
            f0TimeSeries.addOrUpdate(currentDay, population[k]);
            break;
          case 3: //F1
            f1TimeSeries.addOrUpdate(currentDay, population[k]);
            break;
          case 4: //F2
            f2TimeSeries.addOrUpdate(currentDay, population[k]);
            break;
          case 5: //F3
            tTimeSeries.addOrUpdate(currentDay, population[k]);
            break;
        }
      }
    }
  }

  private void createDataset(){
    nsTimeSeries = new XYSeries("NS");
		isTimeSeries = new XYSeries("IS");
		f0TimeSeries = new XYSeries("F0");
		f1TimeSeries = new XYSeries("F1");
		f2TimeSeries = new XYSeries("F2");
		tTimeSeries = new XYSeries("T");

		seriescollection = new XYSeriesCollection();
		//seriescollection.addSeries(nsTimeSeries);
		seriescollection.addSeries(isTimeSeries);
		seriescollection.addSeries(f0TimeSeries);
		seriescollection.addSeries(f1TimeSeries);
		seriescollection.addSeries(f2TimeSeries);
		seriescollection.addSeries(tTimeSeries);
  }

  private JFreeChart createChart(XYDataset xydataset) {
		JFreeChart jfreechart = ChartFactory.createXYLineChart(
				"User state distribution", "Day", "Users per state", xydataset,
				PlotOrientation.VERTICAL, true, true, false);
		jfreechart.setBackgroundPaint(Color.white);

		XYPlot xyplot = (XYPlot) jfreechart.getPlot();

		xyplot.setBackgroundPaint(Color.lightGray);

		xyplot.setDomainGridlinePaint(Color.white);

		xyplot.setRangeGridlinePaint(Color.white);

		xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));

		xyplot.setDomainCrosshairVisible(true);

		xyplot.setRangeCrosshairVisible(true);

		org.jfree.chart.renderer.xy.XYItemRenderer xyitemrenderer = xyplot
				.getRenderer();

		xyitemrenderer.setBaseStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL));

		if (xyitemrenderer instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer xylineandshaperenderer = (XYLineAndShapeRenderer) xyitemrenderer;
			xylineandshaperenderer.setBaseShapesVisible(false);
		}

		return jfreechart;
	}

  public JPanel getMainPane() {
    return mainPane;
  }
}

