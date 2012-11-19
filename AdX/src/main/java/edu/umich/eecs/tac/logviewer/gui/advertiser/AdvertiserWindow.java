/*
 * AdvertiserWindow.java
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

import javax.swing.*;
import java.awt.*;

import edu.umich.eecs.tac.logviewer.info.GameInfo;
import edu.umich.eecs.tac.logviewer.info.Advertiser;
import edu.umich.eecs.tac.logviewer.monitor.ParserMonitor;
import edu.umich.eecs.tac.logviewer.gui.*;
import edu.umich.eecs.tac.props.Query;

/**
 * @author Lee Callender
 */
public class AdvertiserWindow extends JFrame{
  private static final int ROOT_NUM_QUERIES = 4;
  GameInfo gameInfo;
  DayChanger dayChanger;
  Advertiser advertiser;
  int advertiserIndex;
  PositiveBoundedRangeModel dayModel;
  Query[] querySpace;

  public AdvertiserWindow(GameInfo gameInfo, Advertiser advertiser,
		                      PositiveBoundedRangeModel dayModel, ParserMonitor[] monitors) {
      super(advertiser.getName());

      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      this.gameInfo = gameInfo;
      this.advertiser = advertiser;
      advertiserIndex = gameInfo.getAdvertiserIndex(advertiser);
      this.querySpace = gameInfo.getQuerySpace().toArray(new Query[0]);
      this.dayModel = dayModel;
      dayChanger = new DayChanger(dayModel);

      JTabbedPane tabPane = new JTabbedPane();

      tabPane.addTab("Overview", null, createOverviewPane(), "Advertiser overview information");
      tabPane.addTab("Bidding Metrics", null, createBiddingPane(), "Information about bidding");
      tabPane.addTab("User Metrics", null, createUserInteractionPane(), "Information about user interactions");
      tabPane.addTab("Transactions", null, createSalesPane(), "Information about sales");
      
      //tabPane.addTab("Component Order Details", null, createComponentCommPane(), "Detailed information about supplier communication");

      if(monitors != null) {
	      for (int i = 0, n = monitors.length; i < n; i++) {
	        if(monitors[i].hasAgentView(advertiser)) {
		        tabPane.addTab("Monitor " + monitors[i].getName(), null,
				    monitors[i].getAgentView(advertiser),
				    "Information from the monitor "
				    + monitors[i].getName());
	        }
	      }
      }

      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(tabPane, BorderLayout.CENTER);
      getContentPane().add(dayChanger.getMainPane(), BorderLayout.SOUTH);

      pack();
  }


  protected JPanel createBankPane() {
	  PositiveRangeDiagram accountDiagram, diffDiagram,
	    interestDiagram, penaltyDiagram;


	  GridBagLayout gbl = new GridBagLayout();
	  GridBagConstraints gblConstraints = new GridBagConstraints();
	  gblConstraints.fill = GridBagConstraints.BOTH;

	  JPanel pane = new JPanel();
	  pane.setLayout(gbl);

    accountDiagram = new PositiveRangeDiagram(1, dayModel);
    accountDiagram.setPreferredSize(new Dimension(200,150));
    accountDiagram.setData(0, advertiser.getAccountBalance(), 1);
    accountDiagram.addConstant(Color.black, 0);
    accountDiagram.setBorder(BorderFactory.createTitledBorder(""));
    accountDiagram.setTitle(0, "Account Balance: $", "");
    gblConstraints.weightx = 1;
    gblConstraints.weighty = 1;
    gblConstraints.gridwidth = 3;
    gblConstraints.gridx = 0;
    gblConstraints.gridy = 0;
    gbl.setConstraints(accountDiagram, gblConstraints);
    pane.add(accountDiagram);

    int[] diff = new int[dayModel.getLast()-1];
    diff[0] = 0;
    for (int i = 1, n = dayModel.getLast()-1; i < n; i++) {
	    diff[i] = (advertiser.getAccountBalance(i+1) - advertiser.getAccountBalance(i-1)) / 2;
    }

    diffDiagram = new PositiveRangeDiagram(1, dayModel);
    diffDiagram.setPreferredSize(new Dimension(200,150));
    diffDiagram.setData(0, diff, 1);
    diffDiagram.addConstant(Color.black, 0);
    diffDiagram.setBorder(BorderFactory.createTitledBorder(""));
    diffDiagram.setTitle(0, "Account diff: $", "");

    gblConstraints.weightx = 1;
    gblConstraints.weighty = 1;
    gblConstraints.gridwidth = 1;
    gblConstraints.gridx = 0;
    gblConstraints.gridy = 1;
    gbl.setConstraints(diffDiagram, gblConstraints);
    pane.add(diffDiagram);

      return pane;
    }

  protected JPanel createOverviewPane(){
    PositiveRangeDiagram accountDiagram;
    OverviewTransactionPanel otp;
    OverviewBidPanel obp;
    OverviewUserMetricPanel oump;

    GridBagLayout gbl = new GridBagLayout();
	  GridBagConstraints gblConstraints = new GridBagConstraints();
	  gblConstraints.fill = GridBagConstraints.BOTH;

	  JPanel pane = new JPanel();
	  pane.setLayout(gbl);

    accountDiagram = new PositiveRangeDiagram(1, dayModel);
    accountDiagram.setPreferredSize(new Dimension(200,150));
    accountDiagram.setData(0, advertiser.getAccountBalance(), 1);
    accountDiagram.addConstant(Color.black, 0);
    accountDiagram.setBorder(BorderFactory.createTitledBorder(""));
    accountDiagram.setTitle(0, "Account Balance: $", "");
    gblConstraints.weightx = 1;
    gblConstraints.weighty = 1;
    gblConstraints.gridwidth = 3;
    gblConstraints.gridx = 0;
    gblConstraints.gridy = 0;
    gbl.setConstraints(accountDiagram, gblConstraints);
    pane.add(accountDiagram);

    obp = new OverviewBidPanel(advertiser, dayModel, gameInfo, gameInfo.getNumberOfDays());
    gblConstraints.weightx = 1;
    gblConstraints.weighty = 1;
    gblConstraints.gridwidth = 1;
    gblConstraints.gridx = 1;
    gblConstraints.gridy = 1;
    gbl.setConstraints(obp.getMainPane(), gblConstraints);
    pane.add(obp.getMainPane());

    oump = new OverviewUserMetricPanel(advertiser, dayModel, gameInfo);
    gblConstraints.weightx = 1;
    gblConstraints.weighty = 1;
    gblConstraints.gridwidth = 1;
    gblConstraints.gridx = 2;
    gblConstraints.gridy = 1;
    gbl.setConstraints(oump.getMainPane(), gblConstraints);
    pane.add(oump.getMainPane());

    otp = new OverviewTransactionPanel(advertiser, dayModel, gameInfo);
    gblConstraints.weightx = 1;
    gblConstraints.weighty = 1;
    gblConstraints.gridwidth = 1;
    gblConstraints.gridx = 0;
    gblConstraints.gridy = 1;
    gbl.setConstraints(otp.getMainPane(), gblConstraints);
    pane.add(otp.getMainPane());

    return pane;
  }

  protected JPanel createBiddingPane() {

    GridBagLayout gbl = new GridBagLayout();
	  GridBagConstraints gblConstraints = new GridBagConstraints();
	  gblConstraints.fill = GridBagConstraints.BOTH;

	  JPanel pane = new JPanel();
    pane.setLayout(gbl);

    //Add query panels
    QueryBidPanel current;
    gblConstraints.weightx = 1;
    gblConstraints.weighty = 1;
    gblConstraints.gridwidth = 1;
    //TODO-Number of queries should not be hardcoded
    for(int i = 0; i < 4; i++){
      for(int j = 0; j < 4; j++){
        gblConstraints.gridx = i;
        gblConstraints.gridy = j;
        current = new QueryBidPanel(querySpace[4*i + j], advertiser,
              this.dayModel, gameInfo.getNumberOfDays(), querySpace);
        //Add queryPanel information
        gbl.setConstraints(current.getMainPane(), gblConstraints);
        pane.add(current.getMainPane());
      }
    }

    return pane;
  }

  protected JPanel createSalesPane(){
	  GridBagLayout gbl = new GridBagLayout();
	  GridBagConstraints gblConstraints = new GridBagConstraints();
	  gblConstraints.fill = GridBagConstraints.BOTH;

	  JPanel pane = new JPanel();
    pane.setLayout(gbl);

    //Add query panels
    QuerySalesPanel current;
    gblConstraints.weightx = 1;
    gblConstraints.weighty = 1;
    gblConstraints.gridwidth = 1;
    //TODO-Number of queries should not be hardcoded
    for(int i = 0; i < ROOT_NUM_QUERIES; i++){
      for(int j = 0; j < ROOT_NUM_QUERIES; j++){
        gblConstraints.gridx = i;
        gblConstraints.gridy = j;
        current = new QuerySalesPanel(querySpace[ROOT_NUM_QUERIES*i + j], advertiser, this.dayModel);
        //Add queryPanel information
        gbl.setConstraints(current.getMainPane(), gblConstraints);
        pane.add(current.getMainPane());
      }
    }

    return pane;
  }

  protected JPanel createRatioPane(){

    GridBagLayout gbl = new GridBagLayout();
	  GridBagConstraints gblConstraints = new GridBagConstraints();
	  gblConstraints.fill = GridBagConstraints.BOTH;

	  JPanel pane = new JPanel();
    pane.setLayout(gbl);

    //Add query panels
    QueryRatioPanel current;
    gblConstraints.weightx = 1;
    gblConstraints.weighty = 1;
    gblConstraints.gridwidth = 1;
    //TODO-Number of queries should not be hardcoded
    for(int i = 0; i < ROOT_NUM_QUERIES; i++){
      for(int j = 0; j < ROOT_NUM_QUERIES; j++){
        gblConstraints.gridx = i;
        gblConstraints.gridy = j;
        current = new QueryRatioPanel(querySpace[ROOT_NUM_QUERIES*i + j], advertiser, this.dayModel);
        //Add queryPanel information
        gbl.setConstraints(current.getMainPane(), gblConstraints);
        pane.add(current.getMainPane());
      }
    }

    return pane;
  }

  protected JPanel createUserInteractionPane(){
    GridBagLayout gbl = new GridBagLayout();
	  GridBagConstraints gblConstraints = new GridBagConstraints();
	  gblConstraints.fill = GridBagConstraints.BOTH;
    JPanel pane = new JPanel();
    pane.setLayout(gbl);

    QueryUserInteractionPanel current;
    gblConstraints.weightx = 1;
    gblConstraints.weighty = 1;
    gblConstraints.gridwidth = 1;
    //TODO-Number of queries should not be hardcoded
    for(int i = 0; i < ROOT_NUM_QUERIES; i++){
      for(int j = 0; j < ROOT_NUM_QUERIES; j++){
        gblConstraints.gridx = i;
        gblConstraints.gridy = j;
        current = new QueryUserInteractionPanel(querySpace[ROOT_NUM_QUERIES*i + j], advertiser, this.dayModel);
        //Add queryPanel information
        gbl.setConstraints(current.getMainPane(), gblConstraints);
        pane.add(current.getMainPane());
      }
    }

    return pane;
  }

  private PositiveRangeDiagram createDiagram(Dimension preferredSize,
					     int[] data,
					     String titlePrefix,
					     String titlePostfix) {
      PositiveRangeDiagram diagram = new PositiveRangeDiagram(1, dayModel);
      diagram.setPreferredSize(preferredSize);
      diagram.setData(0, data, 1);
      diagram.addConstant(Color.black, 0);
      diagram.setBorder(BorderFactory.createTitledBorder(""));
      diagram.setTitle(0, titlePrefix, titlePostfix);
      return diagram;
  }
}//AdvertiserWindow
