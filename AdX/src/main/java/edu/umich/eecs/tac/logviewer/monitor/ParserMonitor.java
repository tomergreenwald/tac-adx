/*
 * ParserMonitor.java
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

package edu.umich.eecs.tac.logviewer.monitor;

import javax.swing.JComponent;

import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.logtool.LogHandler;
import se.sics.tasim.props.ServerConfig;
import edu.umich.eecs.tac.logviewer.gui.PositiveBoundedRangeModel;
import edu.umich.eecs.tac.logviewer.info.Actor;
import edu.umich.eecs.tac.logviewer.util.SimulationParser;

/**
 * The abstract base class for all Parser Monitors. A parser monitor
 * is an extention of the log-file handler and can be used to analyse
 * and visualize the content of simulation log-files.<p />
 *
 * The process of creating and using a monitor can be described with
 * the following steps. Create a new, custom monitor class, by
 * extending the abstract ParserMonitor class and overrideing methods
 * that are needed. Edit the file log.conf and add your monitor to the
 * file together with configuration values for it. This will cause the
 * monitor to be instatiated, propperly initiated and run. If your
 * monitor has declared that it has a graphical representation of its
 * results, theese should be accessible from the main visualization
 * tool window.
 *
 * This has primarily been adapted from TAC SCM
 * @author SICS, Lee Callender 
 */
abstract public class ParserMonitor {

    private SimulationParser simParser;
    private String name;
    private LogHandler logHandler;
    private PositiveBoundedRangeModel dayModel;

    /**
     * The <code>init</code> method is called directly after an
     * instance has been created. It supplies the monitor with it's
     * initial values.
     *
     * @param name Name of the monitor, a <code>String</code> value
     * @param logHandler The <code>LogHandler</code> that created the monitor
     * @param simParser The <code>SimulationParser</code> that the
     * monitor is monitoring value
     * @param dayModel A <code>PositiveBoundedRangeModel</code>. This
     * is the data model used to keep track on the current day in the
     * visualizer main window.
     */
    public final void init(String name, LogHandler logHandler,
		     SimulationParser simParser, PositiveBoundedRangeModel dayModel) {
	    this.name = name;
	    this.logHandler = logHandler;
	    this.simParser = simParser;
	    this.dayModel = dayModel;
    }

  /**
   * Invoked when the parse process starts.
   */
  public void parseStarted() {
  }

  /**
   * Invoked when the parse process ends.
   */
  public void parseStopped() {
  }

    /**
     * @return The name of the monitor, a <code>String</code> value
     */
    public String getName() {
	return name;
    }

    /**
     * @return The name of the monitor, a <code>String</code> value
     */
    public String toString() {
	return name;
    }

    /**
     * @return The <code>LogHandler</code> used by the monitor
     */
    protected LogHandler getLogHandler() {
	return logHandler;
    }

    /**
     * @return The configuration for the visualizer including this monitor
     */
    protected ConfigManager getConfig() {
      return logHandler.getConfig();
    }

    /**
     * @return The <code>SimulationParser</code> that the monitor is
     * monitoring
     */
    protected SimulationParser getSimulationParser() {
	return simParser;
    }

    /**
     * @return The day model for the visualization of this simulation
     */
    protected PositiveBoundedRangeModel getDayModel() {
      return dayModel;
    }

    /**
     * The method <code>hasSimulationView</code> specifies if the
     * monitor has an agent <i>independent<i>, graphical
     * representation that can be displayed in the visualizers main
     * window. <p />
     *
     * If the method returns true, <code>getSimulationView</code>
     * should return the graphical representation.
     *
     * @return <code>true</code> if it has an agent independent,
     * graphical representation. <code>false</code> otherwise.
     */
    public boolean hasSimulationView() {
	return false;
    }

    /**
     * @return The agent independent, graphical representation of the
     * monitor, a <code>JComponent</code> value.
     */
    public JComponent getSimulationView() {
	return null;
    }

    /**
     * The method <code>hasAgentView</code> specifies if the
     * monitor has an agent <i>dependent</i>, graphical representation that
     * can be displayed in the visualizers agent windows. <p>
     *
     * If the method returns true, <code>getAgentView</code>
     * should return the graphical representation.
     *
     * @param actor The agent for which an agent graphical
     * representation is asked
     * @return <code>true</code> if it has an agent dependant
     * graphical representation for the specified
     * agent. <code>false</code> otherwise.
     */
    public boolean hasAgentView(Actor actor) {
	return false;
    }

    /**
     * @param actor The agent for which an agent graphical
     * representation is asked
     * @return The agent dependent, graphical representation of the
     * monitor for the specified agent, a <code>JComponent</code>
     * value.
     */
    public JComponent getAgentView(Actor actor) {
	return null;
    }

    /**
     * <code>warn</code> ivokes the <code>warn</code> method on the
     * <code>LogHandler</code> instance of this monitor. This results
     * in a message, "monitorname: warning" beeing displayed in the
     * <code>LogManager</code> interface.
     *
     * @param warning The <code>String</code> warning message to display
     */
    protected void warn(String warning) {
	logHandler.warn(name + ": " + warning);
    }


  /**
   * Invoked when a message to all participants with a specific role is
   * encountered in the log file. Example of this is the RFQs sent by
   * the customers to all manufacturers each day.
   *
   * @param sender the sender of the message
   * @param role the role of all receivers
   * @param content the message content
   */
  public void messageToRole(int sender, int role,
			    Transportable content) {
  }

  /**
   * Invoked when a message to a specific receiver is encountered in
   * the log file. Example of this is the offers sent by the
   * manufacturers to the customers.
   *
   * @param sender the sender of the message
   * @param receiver the receiver of the message
   * @param content the message content
   */
  public void message(int sender, int receiver,
		      Transportable content) {
  }

  /**
   * Invoked when some general data is encountered in the log file. An
   * example of this is the server configuration for the simulation.
   *
   * @param object the data container
   * @see se.sics.tasim.props.ServerConfig
   */
  public void data(Transportable object) {
  }

  /**
   * Invoked when a data update is encountered in the log file.
   *
   * @param agent the agent for which the data was updated
   * @param type the type of the data
   * @param value the data value
   * @see tau.tac.adx.sim.TACAdxConstants
   */
  public void dataUpdated(int agent, int type, int value) {
  }

  /**
   * Invoked when a data update is encountered in the log file.
   *
   * @param agent the agent for which the data was updated
   * @param type the type of the data
   * @param value the data value
   * @see tau.tac.adx.sim.TACAdxConstants
   */
  public void dataUpdated(int agent, int type, long value) {
  }

  /**
   * Invoked when a data update is encountered in the log file.
   *
   * @param agent the agent for which the data was updated
   * @param type the type of the data
   * @param value the data value
   * @see tau.tac.adx.sim.TACAdxConstants
   */
  public void dataUpdated(int agent, int type, float value) {
  }

  /**
   * Invoked when a data update is encountered in the log file.
   *
   * @param agent the agent for which the data was updated
   * @param type the type of the data
   * @param value the data value
   * @see tau.tac.adx.sim.TACAdxConstants
   */
  public void dataUpdated(int agent, int type, String value) {
  }

  /**
   * Invoked when a data update is encountered in the log file.
   *
   * @param agent the agent for which the data was updated
   * @param type the type of the data
   * @param content the content sent
   * @see tau.tac.adx.sim.TACAdxConstants
   */
  public void dataUpdated(int agent, int type, Transportable content) {
  }

  /**
   * Invoked when a data update is encountered in the log
   * file. Examples of this is the RetailCatalog in
   * the beginning of a simulation.
   *
   * @param type the type of the data
   * @param content the content sent
   * @see tau.tac.adx.sim.TACAdxConstants
   * @see edu.umich.eecs.tac.props.RetailCatalog
   * (See other props classes sent to everyone)
   */
  public void dataUpdated(int type, Transportable content) {
  }

  /**
   * Invoked when an interest update for a specific agent is
   * encountered in the log file.
   *
   * @param agent the agent for which its bank account has been
   * changed by applying an interest
   * @param amount the interest amount
   */
  public void interest(int agent, long amount) {
  }

  /**
   * Invoked when a transaction is encountered in the log file.
   *
   * @param supplier the supplier which receives the payment
   * @param customer the paying customer
   * @param orderID the order causing the payment
   * @param amount the transacted amount
   */
  public void transaction(int supplier, int customer,
			     int orderID, long amount) {
  }

  /**
   * Invoked when a penalty claim is encountered in the log file.
   *
   * @param supplier the supplier which pays the penalty
   * @param customer the customer which receives the penalty
   * @param orderID the order causing the penalty
   * @param amount the penalty fee
   */
  public void penalty(int supplier, int customer,
			 int orderID, int amount,
			 boolean orderCancelled) {
  }

  /**
   * Invoked when a new day notification is encountered in the log file.
   *
   * @param date the new day in the simulation
   * @param serverTime the server time at that point in the simulation
   */
  public void nextDay(int date, long serverTime) {
  }

  /**
   * Invoked when an unknown (unhandled) node is encountered in the
   * log file.
   *
   * @param nodeName the name of the unhandled node.
   */
  public void unhandledNode(String nodeName) {
  }

} // ParserMonitor
