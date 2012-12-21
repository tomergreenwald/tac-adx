/*
 * PopulationWindow.java
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
import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.RetailCatalog;

import javax.swing.*;


import java.awt.*;

/**
 * @author Lee Callender
 */
public class PopulationWindow extends JFrame {
  GameInfo gameInfo;

  public PopulationWindow(GameInfo gameInfo){
    super("Users per product population");

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    this.gameInfo = gameInfo;

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(createPopulationPane(), BorderLayout.CENTER);
    pack();
  }

 public JPanel createPopulationPane(){
   GridBagLayout gbl = new GridBagLayout();
	 GridBagConstraints gblConstraints = new GridBagConstraints();
	 gblConstraints.fill = GridBagConstraints.BOTH;

	 JPanel pane = new JPanel();
   pane.setLayout(gbl);

   //Add query panels
   ProductPopulationPanel current;
   Product[] products = gameInfo.getRetailCatalog().keys().toArray(new Product[0]);
   gblConstraints.weightx = 1;
   gblConstraints.weighty = 1;
   gblConstraints.gridwidth = 1;

   //TODO-Number of queries should not be hardcoded
   for(int i = 0; i < 3; i++){
     for(int j = 0; j < 3; j++){
       gblConstraints.gridx = i;
       gblConstraints.gridy = j;
       current = new ProductPopulationPanel(gameInfo, products[i*3 + j]);

        //Add queryPanel information
        gbl.setConstraints(current.getMainPane(), gblConstraints);
        pane.add(current.getMainPane());
      }
    }

    return pane;
 }

}
