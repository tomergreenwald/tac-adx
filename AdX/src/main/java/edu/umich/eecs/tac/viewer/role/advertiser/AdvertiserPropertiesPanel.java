/*
 * AdvertiserPropertiesPanel.java
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

package edu.umich.eecs.tac.viewer.role.advertiser;

import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.viewer.GraphicUtils;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import se.sics.isl.transport.Transportable;
import tau.tac.adx.sim.TACAdxConstants;

import javax.swing.*;
import java.awt.*;

/**
 * @author Guha Balakrishnan and Patrick Jordan
 */
public class AdvertiserPropertiesPanel extends JPanel {
    private int agent;
    private JLabel manufacturerLabel;
    private JLabel componentLabel;

    public AdvertiserPropertiesPanel(int agent, String name, TACAASimulationPanel simulationPanel) {
        this.agent = agent;
        simulationPanel.addViewListener(new AdvertiserInfoListener());
        initialize();
    }

    private void initialize() {
        setLayout(new GridLayout(2, 1));
        setBackground(TACAAViewerConstants.CHART_BACKGROUND);
        setBorder(BorderFactory.createTitledBorder("Specialty Information"));

        manufacturerLabel = new JLabel(new ImageIcon());
        manufacturerLabel.setBorder(BorderFactory.createTitledBorder("Manufacturer"));
        add(manufacturerLabel);

        componentLabel = new JLabel(new ImageIcon());
        componentLabel.setBorder(BorderFactory.createTitledBorder("Component"));
        add(componentLabel);
    }

    private class AdvertiserInfoListener extends ViewAdaptor {

        public void dataUpdated(int agent, int type, Transportable value) {
            if (agent == AdvertiserPropertiesPanel.this.agent && type == TACAdxConstants.DU_ADVERTISER_INFO && value.getClass() == AdvertiserInfo.class) {
                AdvertiserInfo info = (AdvertiserInfo) value;
                String component = info.getComponentSpecialty();
                String manufacturer = info.getManufacturerSpecialty();

                ImageIcon icon = GraphicUtils.iconForComponent(component);

                if (icon != null) {
                    componentLabel.setIcon(icon);
                }

                icon = GraphicUtils.iconForManufacturer(manufacturer);
                if (icon != null) {
                    manufacturerLabel.setIcon(icon);
                }
            }
        }
    }


}