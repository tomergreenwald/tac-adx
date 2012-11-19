/*
 * TACAASimulationPanel.java
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

package edu.umich.eecs.tac.viewer;

import edu.umich.eecs.tac.viewer.role.AdvertiserTabPanel;
import edu.umich.eecs.tac.viewer.role.AgentSupport;
import edu.umich.eecs.tac.viewer.role.MainTabPanel;
import edu.umich.eecs.tac.viewer.role.PublisherTabPanel;
import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.viewer.TickListener;
import se.sics.tasim.viewer.ViewerPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Patrick Jordan
 */
public class TACAASimulationPanel extends JPanel implements TickListener, ViewListener {
    private final Object lock;

    AgentSupport agentSupport;

    private JTabbedPane tabbedPane;

    private ViewerPanel viewerPanel;

    private boolean isRunning;

    private List<ViewListener> viewListeners;
    private List<TickListener> tickListeners;

    public TACAASimulationPanel(ViewerPanel viewerPanel) {
        super(null);
        this.agentSupport = new AgentSupport();
        this.viewerPanel = viewerPanel;
        viewListeners = new CopyOnWriteArrayList<ViewListener>();
        tickListeners = new CopyOnWriteArrayList<TickListener>();
        lock = new Object();
        initialize();
    }

    protected void initialize() {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        setBackground(Color.WHITE);
        add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.setBackground(Color.WHITE);        
    }



    protected void createTabs() {
        tabbedPane.addTab("Dashboard", null, new MainTabPanel(this), "Click to view main dashboard");
        tabbedPane.addTab("Advertisers", null, new AdvertiserTabPanel(this),
                "Click to view Advertisers");
        tabbedPane.addTab("Publisher", null, new PublisherTabPanel(this),
                "Click to view Publisher");
    }

    public TACAAAgentView getAgentView(int agentID) {
        return null;
    }

    public String getAgentName(int agentIndex) {
        TACAAAgentView view = getAgentView(agentIndex);
        return view != null ? view.getName() : Integer.toString(agentIndex);
    }

    public int getHighestAgentIndex() {
        return agentSupport.size();
    }

    public void addAgentView(TACAAAgentView view, int index, String name,
                             int role, String roleName, int container) {
    }

    public void removeAgentView(TACAAAgentView view) {
    }

    /**
     * ****************************************************************** setup
     * and time handling
     * ********************************************************************
     */

    public void simulationStarted(long startTime, long endTime,
                                  int timeUnitCount) {
        // Clear any old items before start a new simulation
        clear();

        createTabs();

        if (!isRunning) {
            viewerPanel.addTickListener(this);
            isRunning = true;
        }
    }

    public void simulationStopped() {
        isRunning = false;
        viewerPanel.removeTickListener(this);

        repaint();
    }

    public void clear() {
        agentSupport = new AgentSupport();

        tabbedPane.removeAll();

        clearViewListeners();
        clearTickListeners();

        // This must be done with event dispatch thread. FIX THIS!!!
        repaint();
    }

    public void nextTimeUnit(int timeUnit) {

    }

    /**
     * ******************************************************************
     * TickListener interface
     * ********************************************************************
     */

    public void tick(long serverTime) {
        fireTick(serverTime);
    }

    public void simulationTick(long serverTime, int timeUnit) {
        fireSimulationTick(serverTime, timeUnit);
    }

    /**
     * ****************************************************************** API
     * towards the agent views
     * ********************************************************************
     */

    ConfigManager getConfig() {
        return viewerPanel.getConfig();
    }

    Icon getIcon(String name) {
        return viewerPanel.getIcon(name);
    }

    void showDialog(JComponent dialog) {
        viewerPanel.showDialog(dialog);
    }

    public void addViewListener(ViewListener listener) {
        synchronized (lock) {
            for(int i = 0; i < agentSupport.size(); i++) {
                listener.participant(agentSupport.agent(i),agentSupport.role(i),
                                     agentSupport.name(i),agentSupport.participant(i));
            }
            viewListeners.add(listener);
        }
    }

    public void removeViewListener(ViewListener listener) {
        synchronized (lock) {
            viewListeners.remove(listener);
        }
    }

    protected void clearViewListeners() {
        synchronized (lock) {
            viewListeners.clear();
        }
    }

    public void addTickListener(TickListener listener) {
        synchronized (lock) {
            tickListeners.add(listener);
        }
    }

    public void removeTickListener(TickListener listener) {
        synchronized (lock) {
            tickListeners.remove(listener);
        }
    }

    protected void clearTickListeners() {
        synchronized (lock) {
            tickListeners.clear();
        }
    }

    public void dataUpdated(int agent, int type, int value) {
        fireDataUpdated(agent, type, value);
    }

    public void dataUpdated(int agent, int type, long value) {
        fireDataUpdated(agent, type, value);
    }

    public void dataUpdated(int agent, int type, float value) {
        fireDataUpdated(agent, type, value);
    }

    public void dataUpdated(int agent, int type, double value) {
        fireDataUpdated(agent, type, value);
    }

    public void dataUpdated(int agent, int type, String value) {
        fireDataUpdated(agent, type, value);
    }

    public void dataUpdated(int agent, int type, Transportable value) {
        fireDataUpdated(agent, type, value);
    }

    public void dataUpdated(int type, Transportable value) {
        fireDataUpdated(type, value);
    }

    public void participant(int agent, int role, String name, int participantID) {
        agentSupport.participant(agent, role, name, participantID);
        
        fireParticipant(agent, role, name, participantID);
    }

    protected void fireParticipant(int agent, int role, String name,
                                   int participantID) {
        synchronized (lock) {
            for (ViewListener listener : viewListeners) {
                listener.participant(agent, role, name, participantID);
            }
        }
    }

    protected void fireDataUpdated(int agent, int type, int value) {
        synchronized (lock) {
            for (ViewListener listener : viewListeners) {
                listener.dataUpdated(agent, type, value);
            }
        }
    }

    protected void fireDataUpdated(int agent, int type, long value) {
        synchronized (lock) {
            for (ViewListener listener : viewListeners) {
                listener.dataUpdated(agent, type, value);
            }
        }
    }

    protected void fireDataUpdated(int agent, int type, float value) {
        synchronized (lock) {
            for (ViewListener listener : viewListeners) {
                listener.dataUpdated(agent, type, value);
            }
        }
    }

    protected void fireDataUpdated(int agent, int type, double value) {
        for (ViewListener listener : viewListeners) {
            listener.dataUpdated(agent, type, value);
        }
    }

    protected void fireDataUpdated(int agent, int type, String value) {
        synchronized (lock) {
            for (ViewListener listener : viewListeners) {
                listener.dataUpdated(agent, type, value);
            }
        }
    }

    protected void fireDataUpdated(int agent, int type, Transportable value) {
        synchronized (lock) {
            for (ViewListener listener : viewListeners) {
                listener.dataUpdated(agent, type, value);
            }
        }
    }

    protected void fireDataUpdated(int type, Transportable value) {
        synchronized (lock) {
            for (ViewListener listener : viewListeners) {
                listener.dataUpdated(type, value);
            }
        }

    }

    protected void fireTick(long serverTime) {
        synchronized (lock) {
            for (TickListener listener : new CopyOnWriteArrayList<TickListener>(tickListeners)) {
                listener.tick(serverTime);
            }
        }
    }

    protected void fireSimulationTick(long serverTime, int timeUnit) {
        synchronized (lock) {
            for (TickListener listener : new CopyOnWriteArrayList<TickListener>(tickListeners)) {
                listener.simulationTick(serverTime, timeUnit);
            }
        }
    }
}
