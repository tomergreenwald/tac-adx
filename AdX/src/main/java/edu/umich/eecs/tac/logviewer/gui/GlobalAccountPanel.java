/*
 * GlobalAccountPanel.java
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

/**
 * @author Lee Callender
 * Modified from SICS GlobalAccountPanel.java
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.ImageIcon;
import javax.swing.Box;
import java.awt.Component;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import edu.umich.eecs.tac.logviewer.info.GameInfo;

public class GlobalAccountPanel {
    JPanel mainPane;
    PositiveRangeDiagram accountDiagram;

    public GlobalAccountPanel(GameInfo simInfo, PositiveBoundedRangeModel dayModel) {
	    mainPane = new JPanel();
	    mainPane.setLayout(new BorderLayout());
	    mainPane.setBorder(BorderFactory.createTitledBorder
			   (BorderFactory.createEtchedBorder()," Account Balance "));
	    mainPane.setMinimumSize(new Dimension(280,200));
	    mainPane.setPreferredSize(new Dimension(280,200));

	    accountDiagram = new PositiveRangeDiagram(simInfo.getAdvertiserCount(), dayModel);

	    accountDiagram.addConstant(Color.black, 0);

      for (int i = 0, n = simInfo.getAdvertiserCount(); i < n; i++) {
	      accountDiagram.setData(i, simInfo.getAdvertiser(i).getAccountBalance(), 1);
	      accountDiagram.setDotColor(i, simInfo.getAdvertiser(i).getColor());
	    }

	    accountDiagram.setToolTipText("Account balance for all agents");
	    mainPane.add(accountDiagram, BorderLayout.CENTER);
    }

    public JPanel getMainPane() {
	    return mainPane;
    }
} // GlobalAccountPanel

