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
 * ResultManager
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Jan 10 16:27:54 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.io.File;
import java.io.IOException;

import se.sics.tasim.logtool.LogReader;
import se.sics.tasim.logtool.ParticipantInfo;

/**
 * The ResultManager is responsible for storing simulation results for the
 * simulation types it handles. It also generates the result and score web
 * pages.
 */
public abstract class ResultManager {

	private SimServer simServer;
	private boolean addToTable;
	private String destinationPath;
	private LogReader logReader;
	private String gameLogName;

	final void init(SimServer simServer, boolean addToTable, String gameLogName) {
		if (simServer == null) {
			throw new NullPointerException();
		}
		this.simServer = simServer;
		this.gameLogName = gameLogName;
		this.addToTable = addToTable;
	}

	public final void generateResult(LogReader logReader, String destinationPath)
			throws IOException {
		this.destinationPath = destinationPath;
		this.logReader = logReader;

		generateResult();
	}

	/**
	 * Returns the directory where the result pages should be stored.
	 * <p>
	 * The path is either an empty string or ends with a file separator and the
	 * result manager should at least create the file
	 * <code>getDestinationPath() + "index.html"</code>.
	 * 
	 * @return the destination path where to store the result pages
	 */
	protected String getDestinationPath() {
		return destinationPath;
	}

	protected LogReader getLogReader() {
		return logReader;
	}

	protected String getGameLogName() {
		return gameLogName;
	}

	/**
	 * Adds this simulation to the history table. The specified participants
	 * should be sorted with best scored participant first.
	 * 
	 * @param participants
	 *            the sorted vector with the participants in this simulation
	 */
	protected void addSimulationToHistory(ParticipantInfo[] participants) {
		addSimulationToHistory(participants, null);
	}

	/**
	 * Adds this simulation to the history table. The specified participants
	 * should be sorted with best scored participant first.
	 * 
	 * @param participants
	 *            the sorted vector with the participants in this simulation
	 * @param participantColors
	 *            the HTML colors for the specified participants or
	 *            <CODE>null</CODE> if no colors should be used
	 */
	protected void addSimulationToHistory(ParticipantInfo[] participants,
			String[] participantColors) {
		if (addToTable && simServer != null) {
			simServer.addSimulationToHistory(logReader, participants,
					participantColors);
		}
	}

	// This method will be REMOVED due to better API. FIX THIS!!! TODO
	protected void addSimulationResult(ParticipantInfo[] participants,
			long[] scores) {
		if (simServer != null) {
			simServer.addSimulationResult(logReader, participants, scores,
					!addToTable);
		}
	}

	// This method will be REMOVED due to better API. FIX THIS!!! TODO
	protected void addSimulationResult(ParticipantInfo[] participants,
			double[] scores) {
		if (simServer != null) {
			simServer.addSimulationResult(logReader, participants, scores,
					!addToTable);
		}
	}

	/**
	 * <code>generateResult</code> is responsible for:
	 * <ul>
	 * <li>Storing the result in the database
	 * <li>Generating the result web pages
	 * <li>Updating the total score table for this type of simulation and server
	 * <li>Adding this simulation to the history page
	 * </ul>
	 */
	protected abstract void generateResult() throws IOException;

} // ResultManager
