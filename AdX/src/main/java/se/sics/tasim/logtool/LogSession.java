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
 * LogSession
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson, Anders Sundman
 * Created : Tue Jul 15 11:02:44 2003
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.logtool;

import java.io.File;
import java.io.FileFilter;
import java.util.logging.Logger;

import se.sics.isl.util.ConfigManager;

/**
 */
public class LogSession implements FileFilter {

	private static final Logger log = Logger.getLogger(LogSession.class
			.getName());

	private static final String CONF = "game.directory.";

	private LogManager logManager;
	private String server;
	private ValueSet games;
	private ValueSet excludes;
	private boolean isServerTree = true;
	private boolean isGameTree = false;
	private String gameDirectory;

	public LogSession(LogManager logManager, String server, ValueSet games,
			ValueSet excludes) {
		this.logManager = logManager;
		this.games = games;
		this.excludes = excludes;
		this.server = server;

		ConfigManager config = logManager.getConfig();
		this.isGameTree = config.getPropertyAsBoolean(CONF + "gameTree", false);
		this.isServerTree = config.getPropertyAsBoolean(CONF + "serverTree",
				true);
		this.gameDirectory = logManager.getGameDirectory().getAbsolutePath();
	}

	public void start() {
		ConfigManager config = logManager.getConfig();
		boolean oldShowGUI = config.getPropertyAsBoolean("showGUI", true);
		int startGame = games.getMin();
		int endGame = games.getMax();
		boolean showGUI = endGame == startGame;
		if (!showGUI) {
			config.setProperty("showGUI", "false");
		}
		try {
			logManager.sessionStarted();
			for (int i = startGame; i <= endGame; i++) {
				if (games.isIncluded(i) && !excludes.isIncluded(i)) {
					String name = getFileName(i);
					logManager.processDataFile(name);
				}
			}
		} finally {
			if (!showGUI && oldShowGUI) {
				config.setProperty("showGUI", "true");
			}
			logManager.sessionEnded();
		}
	}

	private String getFileName(int gameID) {
		StringBuffer path = new StringBuffer().append(gameDirectory).append(
				File.separatorChar);
		if (isServerTree && server != null) {
			path.append(server).append(File.separatorChar);
		}
		if (isGameTree) {
			path.append(gameID).append(File.separatorChar)
					.append("game.slg.gz");
		} else {
			path.append("game");
			if (!isServerTree && server != null) {
				path.append('-').append(server).append('-');
			}
			path.append(gameID).append(".slg.gz");
		}
		return path.toString();
	}

	// -------------------------------------------------------------------
	// FileFilter API
	// -------------------------------------------------------------------

	public boolean accept(File file) {
		String name = file.getName();
		int nameLength;
		if (isGameTree) {
			int gameID = getNumber(name, 0, name.length());
			return gameID > 0 && checkGame(gameID) && file.isDirectory();

		} else if ((nameLength = getLogEnd(name)) > 0) {
			// Only accept patterns of "game<id>.slg[.gz]"
			int gameID = getNumber(name, 6, nameLength);
			return gameID > 0 && checkGame(gameID);
		} else {
			return false;
		}
	}

	private boolean checkGame(int gameID) {
		return games.isIncluded(gameID) && !excludes.isIncluded(gameID);
	}

	// -------------------------------------------------------------------
	// Comparator
	// -------------------------------------------------------------------

	public int compare(Object o1, Object o2) {
		String n1 = ((File) o1).getName();
		String n2 = ((File) o2).getName();
		int n1len, n2len, v1, v2;

		// Special sorting of directories consisting of only digits
		if (isGameTree) {
			v1 = getNumber(n1, 0, n1.length());
			v2 = getNumber(n2, 0, n2.length());
			if (v1 > 0 && v2 > 0) {
				return v1 - v2;
			}
		} else if (((n1len = getLogEnd(n1)) > 0)
				&& ((n2len = getLogEnd(n2)) > 0)) {
			// Special sorting of file with the pattern "game<id>.slg[.gz]"
			v1 = getNumber(n1, 6, n1len);
			v2 = getNumber(n2, 6, n2len);
			if (v1 > 0 && v2 > 0) {
				return v1 - v2;
			}
		}

		return n1.compareTo(n2);
	}

	private int getLogEnd(String text) {
		if (!text.startsWith("game")) {
			return -1;
		} else if (text.endsWith(".slg")) {
			return text.length() - 4;
		} else if (text.endsWith(".slg.gz")) {
			return text.length() - 7;
		} else {
			return -1;
		}
	}

	private int getNumber(String text, int start, int end) {
		char c;
		int value = 0;
		for (int i = start; i < end; i++) {
			c = text.charAt(i);
			if (c >= '0' && c <= '9') {
				value = value * 10 + c - '0';
			} else {
				return -1;
			}
		}
		return value;
	}

} // LogSession
