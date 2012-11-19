/**
 * TAC Supply Chain Management Log Tools
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
 * LogHandler
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson, Anders Sundman
 * Created : Thu Jun 05 13:54:14 2003
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.logtool;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;

/**
 * The abstract class <code>LogHandler</code> should be inherited by all
 * implementations of log analyzers, viewers, etc, that uses the
 * <code>LogManager</code> for the log file processing.
 */
public abstract class LogHandler {

	private LogManager logManager;

	protected LogHandler() {
	}

	final void init(LogManager logManager) {
		if (this.logManager != null) {
			throw new IllegalStateException("already initialized");
		}
		if (logManager == null) {
			throw new NullPointerException();
		}
		this.logManager = logManager;
		init();
	}

	protected LogManager getLogManager() {
		return logManager;
	}

	public ConfigManager getConfig() {
		return logManager.getConfig();
	}

	public File getTempDirectory(String name) throws IOException {
		return logManager.getTempDirectory(name);
	}

	public void warn(String warningMessage) {
		logManager.warn(warningMessage);
	}

	// -------------------------------------------------------------------
	// API to the log implementations
	// -------------------------------------------------------------------

	/**
	 * Initializes the log handler.
	 */
	protected void init() {
	}

	protected void sessionStarted() {
	}

	protected void sessionEnded() {
	}

	/**
	 * Invoked when a new log file should be processed.
	 * 
	 * @param reader
	 *            the log reader for the log file.
	 */
	protected abstract void start(LogReader reader)
			throws IllegalConfigurationException, IOException, ParseException;

} // LogHandler
