/*
 * AdvertiserDisplay.java
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
 */
public class AdvertiserDisplay {
  JPanel mainPane;
  AdvertiserPanel[] ap;

  public AdvertiserDisplay(GameInfo gameInfo,
		      PositiveBoundedRangeModel dayModel,
		      ParserMonitor[] monitors) {

    mainPane = new JPanel();
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gblConstraints = new GridBagConstraints();
    mainPane.setLayout(gbl);

    mainPane.setBorder(BorderFactory.createTitledBorder
		       (" Game situation "));

    ap = new AdvertiserPanel[gameInfo.getAdvertiserCount()];
    gblConstraints.gridx = 0;
    gblConstraints.weighty = 0;
    gblConstraints.anchor = GridBagConstraints.CENTER;
    for (int i = 0, n = gameInfo.getAdvertiserCount(); i < n; i++) {
      ap[i] = new AdvertiserPanel(gameInfo,gameInfo.getAdvertiser(i),
				                          dayModel,monitors);

      gblConstraints.gridy = i+1;
      gbl.setConstraints(ap[i].getMainPane(), gblConstraints);
      mainPane.add(ap[i].getMainPane(), gblConstraints);
    }



  }

  public JPanel getMainPane() {
    return mainPane;
  }
} // ActorDisplay

