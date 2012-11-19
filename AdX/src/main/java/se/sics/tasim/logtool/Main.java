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
 * Main
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson, Anders Sundman
 * Created : Tue Jul 15 13:16:15 2003
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.logtool;

import java.io.IOException;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import se.sics.isl.util.ArgumentManager;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;

/**
 */
public class Main extends java.awt.event.WindowAdapter {

	private Main() {
	}

	public void windowClosing(java.awt.event.WindowEvent e) {
		System.exit(1);
	}

	// -------------------------------------------------------------------
	// Setup main
	// -------------------------------------------------------------------

	public static void main(String[] args)
			throws IllegalConfigurationException, IOException {
		ArgumentManager config = new ArgumentManager("LogManager", args);
		config.addOption("config", "configfile", "set the config file to use");
		config.addOption("file", "datafile", "set the game data file to use");
		config.addOption("games", "games", "set the games as 1-2,5,7");
		config.addOption("excludes", "games",
				"set the games to exclude as 1-2,5,7");
		config.addOption("server", "host", "set the server for the games");
		config.addOption("handler", "loghandler",
				"set the game data handler to use");
		config.addOption("showGUI", "true",
				"set if GUI should be used when supported");
		config.addOption("xml", "show the game data as XML");
		config.addOption("game.directory", "directory",
				"the local game directory");
		config.addOption("game.directory.gameTree", "false",
				"set if each game has its own directory");
		config.addOption("game.directory.serverTree", "true",
				"set if each server has its own directory with games");
		config.addOption("verbose", "set for verbose output");
		config.addOption("log.consoleLevel", "level",
				"set the console log level");
		config.addOption("version", "show the version");
		config.addHelp("h", "show this help message");
		config.addHelp("help");
		config.validateArguments();

		if (config.hasArgument("version")) {
			System.out.println("LogManager version " + LogManager.VERSION);
			System.exit(0);
		}

		String configFile = config.getArgument("config", "log.conf");
		try {
			config.loadConfiguration(configFile);
			config.removeArgument("config");
		} catch (IllegalArgumentException e) {
			showWarning(config, "could not load config",
					"could not load config from '" + configFile + "': " + e);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			showWarning(config, "could not load config",
					"could not load config from '" + configFile + "': " + e);
			return;
		}

		// No more need for argument handling. Lets free the memory
		config.finishArguments();

		// Check Java version
		String version = System.getProperty("java.version");
		if (ConfigManager.compareVersion("1.4", version) > 0) {
			showWarning(config, "Wrong Java version",
					"Java 2 SE 1.4 or newer required! Version " + version
							+ " detected.");
			return;
		}

		if (config.getPropertyAsBoolean("showGUI", true)
				&& config.getPropertyAsBoolean("useSystemUI", true)) {
			try {
				// Set the system look and feel
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (UnsupportedLookAndFeelException exc) {
				System.err.println("unsupported look-and-feel: " + exc);
			} catch (Exception exc) {
				System.err.println("could not change look-and-feel: " + exc);
			}
		}

		new LogManager(config);
	}

	private static void showWarning(ConfigManager config, String title,
			String message) {
		System.err.println(message);
		if (config.getPropertyAsBoolean("showGUI", true)) {
			java.awt.Frame w = new java.awt.Frame(title);
			java.awt.Dimension d = java.awt.Toolkit.getDefaultToolkit()
					.getScreenSize();
			w.add(new java.awt.Label(message));
			w.pack();
			w.setLocation((d.width - w.getWidth()) / 2, (d.height - w
					.getHeight()) / 2);
			w.addWindowListener(new Main());
			w.setVisible(true);
		} else {
			System.exit(1);
		}
	}

} // Main
