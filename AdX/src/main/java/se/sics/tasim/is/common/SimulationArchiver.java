/**
 * TAC Supply Chain Management Simulator
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001-2003 SICS AB. All rights reserved.
 *
 * SICS grants you the right to use, modify, and redistribute this
 * software for noncommercial purposes, on the conditions that you:
 * (1) retain the original headers, including the copyright notice and
 * this text, (2) clearly document the difference between any derived
 * software and the original, and (3) acknowledge your use of this
 * software in pertaining publications and reports.  SICS provides
 * this software "as is", without any warranty of any kind.  IN NO
 * EVENT SHALL SICS BE LIABLE FOR ANY DIRECT, SPECIAL OR INDIRECT,
 * PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSSES OR DAMAGES ARISING OUT
 * OF THE USE OF THE SOFTWARE.
 *
 * -----------------------------------------------------------------
 *
 * SimulationArchiver
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Mar 03 10:40:02 2003
 * Updated : $Date: 2008-04-04 10:38:41 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3951 $
 * Purpose :
 *
 *  The SimulationArchiver is responsible for generating results pages
 *  for finished simulation with its own thread and its own tact.  It
 *  delegates the actual generation to the corresponding SimServer.
 */
package se.sics.tasim.is.common;

import java.util.logging.Logger;
import com.botbox.util.ArrayQueue;

public class SimulationArchiver implements Runnable {

	private static final Logger log = Logger.getLogger(SimulationArchiver.class
			.getName());

	private ArrayQueue simulationQueue = new ArrayQueue();
	private boolean isRunning = false;

	public SimulationArchiver() {
	}

	public synchronized void addSimulation(SimServer simServer, int simulationID) {
		simulationQueue.add(simServer);
		simulationQueue.add(new Integer(simulationID));
		if (!isRunning) {
			isRunning = true;
			new Thread(this, "gameArchiver").start();
		} else {
			notify();
		}
	}

	public void run() {
		try {
			do {
				SimServer simServer;
				int simulationID;
				synchronized (this) {
					while (simulationQueue.size() == 0) {
						try {
							wait();
						} catch (InterruptedException e) {
						}
					}
					simServer = (SimServer) simulationQueue.remove(0);
					if (simServer == null) {
						// Time to stop this thread
						break;
					}
					simulationID = ((Integer) simulationQueue.remove(0))
							.intValue();
				}

				generateResults(simServer, simulationID);
			} while (true);

		} finally {
			isRunning = false;
		}
	}

	private void generateResults(SimServer simServer, int simulationID) {
		simServer.generateResults(simulationID, true);
	}

} // SimulationArchiver
