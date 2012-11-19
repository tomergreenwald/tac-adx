/*
 * ParamsPanel.java
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

import javax.swing.*;
import java.text.DecimalFormat;

/**
 * Displays the main game parameters for a given simulation
 *
 * @author - Lee Callender
 */

public class ParamsPanel {
    private JPanel mainPane;
    JLabel simulationID, secondsPerDay, numberOfDays, squash, server;
    JLabel storageCostLabel;
    JLabel suppNomCap, suppMaxRFQs, suppDiscountFactor;

    public ParamsPanel(GameInfo gameInfo) {
	    mainPane = new JPanel();
	    mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
	    mainPane.setBorder(BorderFactory.createTitledBorder
		                    (BorderFactory.createEtchedBorder()," Simulation Parameters "));

	    simulationID = new JLabel("Simulation: " + gameInfo.getSimulationID()
				                        + " (" + gameInfo.getSimulationType() + ')');
	    server = new JLabel("Server: " + gameInfo.getServer());
	    secondsPerDay = new JLabel("Seconds per day: " + gameInfo.getSecondsPerDay());
      numberOfDays = new JLabel("Number of days: " + gameInfo.getNumberOfDays());
      DecimalFormat squashFormat = new DecimalFormat("#.###");
      squash = new JLabel("Squashing Parameter: " + squashFormat.format(gameInfo.getSquashingParameter()));


      mainPane.add(server);
	    mainPane.add(simulationID);
	    mainPane.add(secondsPerDay);
      mainPane.add(numberOfDays);
      mainPane.add(squash);  //Format the number of digits shown.
    }

    public JPanel getMainPane() {
	    return mainPane;
    }
} // ParamsPanel
