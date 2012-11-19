/*
 * Visualizer.java
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
package edu.umich.eecs.tac.logviewer;

import se.sics.tasim.logtool.LogHandler;
import se.sics.tasim.logtool.LogReader;
import se.sics.isl.util.IllegalConfigurationException;
import edu.umich.eecs.tac.TACAASimulationInfo;
import edu.umich.eecs.tac.logviewer.util.SimulationParser;
import edu.umich.eecs.tac.logviewer.gui.PositiveBoundedRangeModel;
import edu.umich.eecs.tac.logviewer.gui.MainWindow;
import edu.umich.eecs.tac.logviewer.monitor.ParserMonitor;
import edu.umich.eecs.tac.logviewer.info.GameInfo;

import java.io.IOException;
import java.text.ParseException;



/**
 * NOTE: This is a modification of Visualizer.java from TAC SCM
 * @author SICS, Lee Callender
 */
public class Visualizer extends LogHandler {

   MainWindow mainWindow = null;

  protected void start(LogReader reader)
			throws IllegalConfigurationException, IOException, ParseException{

    SimulationParser sp = new SimulationParser(this, reader);
    PositiveBoundedRangeModel dayModel = new PositiveBoundedRangeModel();
	  createMonitors(sp, dayModel);

	  // Start parsing
	  sp.start();
	  if(sp.errorParsing()) {
	    System.err.println("Error while parsing file");
	    return;
	  }

	  // Create simulation info object and let monitors do post processing
	  GameInfo gameInfo = new GameInfo(sp);

	  // Show the visualizer window
	  if(getConfig().getPropertyAsBoolean("showGUI", true) && gameInfo != null) {
	    dayModel.setLast(gameInfo.getNumberOfDays()-1);
	    mainWindow = new MainWindow(gameInfo, dayModel, sp.getMonitors());
	    mainWindow.setVisible(true);
	  }

    sp = null;
  }


  private void createMonitors(SimulationParser sp, PositiveBoundedRangeModel dayModel)
	      throws IllegalConfigurationException, IOException {
	    String[] names = getConfig().getPropertyAsArray("monitor.names");

      ParserMonitor[] monitors = (ParserMonitor[]) getConfig().createInstances("monitor", ParserMonitor.class, names);

      if(monitors == null){
        //System.out.println("No ParserMonitors added.");
        return;
      }

      for (int i = 0, n = monitors.length; i < n; i++) {
	      monitors[i].init(names[i], this, sp, dayModel);
	      sp.addMonitor(monitors[i]);
	    }
  }//createMonitors
} // Visualizer


