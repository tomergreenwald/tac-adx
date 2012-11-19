/*
 * AdvertiserPanel.java
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

import edu.umich.eecs.tac.logviewer.info.Advertiser;
import edu.umich.eecs.tac.logviewer.info.GameInfo;
import edu.umich.eecs.tac.logviewer.monitor.ParserMonitor;
import edu.umich.eecs.tac.logviewer.gui.advertiser.AdvertiserWindow;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.border.BevelBorder;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 * @author Lee Callender
 **/
public class AdvertiserPanel {

    JPanel mainPane;
    JPanel diagramPane;
    JLabel name, manufacturer, component, capacity;
    PositiveRangeDiagram accountDiagram;
    Advertiser advertiser;

    PositiveBoundedRangeModel dayModel;
    AdvertiserWindow advertiserWindow;
    GameInfo gameInfo;
    ParserMonitor[] monitors;

    public AdvertiserPanel(final GameInfo gameInfo,
			     final Advertiser advertiser,
			     final PositiveBoundedRangeModel dayModel,
			     final ParserMonitor[] monitors) {
      this.dayModel = dayModel;
      this.advertiser = advertiser;
      this.gameInfo = gameInfo;
      this.monitors = monitors;
      mainPane = new JPanel();
      mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
      mainPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
      //mainPane.setMinimumSize(new Dimension(150, 100));
      //mainPane.setMaximumSize(new Dimension(150, 100));

      mainPane.addMouseListener(new MouseInputAdapter() {
	      public void mouseClicked(MouseEvent me) {
		      openAgentWindow();
	      }
	    });

      name = new JLabel(advertiser.getName());
      name.setForeground(advertiser.getColor());

      manufacturer = new JLabel("Manufacturer: "+advertiser.getManufacturerSpecialty());
      component = new JLabel("Component: "+advertiser.getComponentSpecialty());
      capacity = new JLabel("Capacity: "+advertiser.getDistributionCapacity());

      mainPane.add(name);  
      mainPane.add(manufacturer);
      mainPane.add(component);
      mainPane.add(capacity);
    }

    protected void openAgentWindow() {
      if(advertiserWindow == null) {
	      advertiserWindow = new AdvertiserWindow(gameInfo, advertiser, dayModel, monitors);
	      advertiserWindow.setLocationRelativeTo(mainPane);
	      advertiserWindow.setVisible(true);
	    } else if (advertiserWindow.isVisible())
	      advertiserWindow.toFront();
	    else
	      advertiserWindow.setVisible(true);

      int state = advertiserWindow.getExtendedState();
	    if ((state & AdvertiserWindow.ICONIFIED) != 0) {
	      advertiserWindow.setExtendedState(state & ~AdvertiserWindow.ICONIFIED);
	    }
    }

    public Component getMainPane() {
      return mainPane;
    }

} // AdvertiserPanel

