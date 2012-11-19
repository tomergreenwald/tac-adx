/*
 * AuctionResultsPanel.java
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

import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryReport;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.logviewer.info.GameInfo;
import edu.umich.eecs.tac.logviewer.info.Advertiser;
import static edu.umich.eecs.tac.logviewer.util.VisualizerUtils.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;

/**
 * @author Lee Callender
 * This panel displays the average positions of all advertisers
 * for a given auction over the course of a day.
 */

public class AuctionResultsPanel {
  public final static String NA = "";

  JPanel mainPane;
  JLabel[] positionLabels;
  int[] indexes;
  Advertiser[] advertisers;

  Query query;
  PositiveBoundedRangeModel dayModel;
  GameInfo gameInfo;

  public AuctionResultsPanel(Query query, GameInfo gameInfo, PositiveBoundedRangeModel dm){
    this.query = query;
    this.gameInfo = gameInfo;
    this.dayModel = dm;
    this.advertisers = gameInfo.getAdvertisers();

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
			   (BorderFactory.createEtchedBorder(),formatToString(query)));
    mainPane.setMinimumSize(new Dimension(105, 155));
    mainPane.setPreferredSize(new Dimension(105, 155));
    mainPane.setBackground(Color.WHITE);

    indexes = new int[advertisers.length];
    positionLabels = new JLabel[advertisers.length];
    for(int i = 0; i < indexes.length; i++){
      indexes[i] = i;
      positionLabels[indexes.length-i-1] = new JLabel(NA);
      positionLabels[indexes.length-i-1].setForeground(advertisers[i].getColor());
      mainPane.add(positionLabels[indexes.length-i-1]);
    }
    
    updateMePlz();
  }

  private void updateMePlz(){
    int day = dayModel.getCurrent();
    double[] averagePosition = new double[advertisers.length];
    QueryReport report = advertisers[0].getQueryReport(day+1); 
    if(report == null){
      noQueryReportDay();
    }else{
      for(int i = 0; i < indexes.length; i++){
        averagePosition[indexes[i]] = report.getPosition(query, advertisers[indexes[i]].getAddress());
        //System.out.println(averagePosition[indexes[i]]+","+advertisers[indexes[i]].getName());
      }
      hardSort(averagePosition, indexes);

      for(int i = 0; i < indexes.length; i++){
        Ad ad = report.getAd(query, advertisers[indexes[i]].getAddress());
        String adString;
        if(ad == null)
          adString = NA;
        else
          adString = formatToString(ad);

        positionLabels[i].setText(adString);
        positionLabels[i].setForeground(advertisers[indexes[i]].getColor());
      }
    }
  }

  /**
   * Display 'N/A' for everyone
   */
  private void noQueryReportDay(){
    for(int i = 0; i < indexes.length; i++){
      indexes[i] = i;
      positionLabels[i].setText(NA);
      positionLabels[i].setForeground(advertisers[i].getColor());
    }
  }

  public Component getMainPane() {
    return mainPane;
  }

  
}
