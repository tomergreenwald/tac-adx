/*
 * MainWindow.java
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
import edu.umich.eecs.tac.logviewer.monitor.ParserMonitor;

import javax.swing.*;
import java.awt.*;

/**
 * @author Lee Callender
 **/
public class MainWindow extends JFrame {

    ParamsPanel paramsPane;
    GlobalAccountPanel accountPane;
    DayChanger dayChanger;
    AdvertiserDisplay advertiserDisplay;
    PopulationPanel populationPanel;
    AuctionResultsDisplay auctionResultsDisplay;

  public MainWindow(final GameInfo gameInfo,
                    final PositiveBoundedRangeModel dayModel,
                    final ParserMonitor[] monitors) {


    super("TAC AA Visualizer - main window");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gblConstraints = new GridBagConstraints();
    Container pane = getContentPane();
    pane.setLayout(gbl);

    dayChanger = new DayChanger(dayModel);
    accountPane = new GlobalAccountPanel(gameInfo, dayModel);
    paramsPane = new ParamsPanel(gameInfo);
    advertiserDisplay = new AdvertiserDisplay(gameInfo, dayModel, monitors);
    populationPanel = new PopulationPanel(gameInfo);
    auctionResultsDisplay = new AuctionResultsDisplay(gameInfo, dayModel);
    
    gblConstraints.fill = GridBagConstraints.HORIZONTAL;
    gblConstraints.anchor = GridBagConstraints.NORTHWEST;
    gblConstraints.weighty = 0;

    gblConstraints.gridx = 0;
    gblConstraints.gridy = 0;
    gbl.setConstraints(paramsPane.getMainPane(), gblConstraints);
    pane.add(paramsPane.getMainPane());

    gblConstraints.gridx = 0;
    gblConstraints.gridy = 1;
    gbl.setConstraints(accountPane.getMainPane(), gblConstraints);
    pane.add(accountPane.getMainPane());

    gblConstraints.gridx = 0;
    gblConstraints.gridy = 2;
    gbl.setConstraints(dayChanger.getMainPane(), gblConstraints);
    pane.add(dayChanger.getMainPane());

    gblConstraints.gridx = 0;
    gblConstraints.gridy = 3;
    gbl.setConstraints(populationPanel.getMainPane(), gblConstraints);
    pane.add(populationPanel.getMainPane());
    /*

//     gblConstraints.gridx = 0;
//     gblConstraints.gridy = 3;
//     gbl.setConstraints(colorPane.getMainPane(), gblConstraints);
//     pane.add(colorPane.getMainPane());

    gblConstraints.fill = GridBagConstraints.BOTH;
    gblConstraints.gridx = 0;
    gblConstraints.gridy = 4;
    gbl.setConstraints(monitorPane.getMainPane(), gblConstraints);
    pane.add(monitorPane.getMainPane());
    */
    gblConstraints.fill = GridBagConstraints.HORIZONTAL;
    //gblConstraints.weighty = 1;
    //gblConstraints.weightx = 1;
    gblConstraints.gridx = 1;
    gblConstraints.gridy = 0;
    gblConstraints.gridheight = 5;
    gbl.setConstraints(advertiserDisplay.getMainPane(), gblConstraints);
    pane.add(advertiserDisplay.getMainPane());

    gblConstraints.gridx = 2;
    gblConstraints.gridy = 0;
    gblConstraints.weightx = 1.0;
    gblConstraints.weighty = 1.0;
    //gbl.setConstraints(populationPanel.getMainPane(), gblConstraints);
    //pane.add(populationPanel.getMainPane());
    gbl.setConstraints(auctionResultsDisplay.getMainPane(), gblConstraints);
    pane.add(auctionResultsDisplay.getMainPane());

    pack();
    setLocationRelativeTo(null);
  }

} // MainWindow

