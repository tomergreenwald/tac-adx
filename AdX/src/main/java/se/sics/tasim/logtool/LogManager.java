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
 * LogManager
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Jun 05 13:41:07 2003
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.logtool;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;
import se.sics.isl.util.LogFormatter;

/**
 */
public class LogManager {

	private static final String CONF = "manager.";

	public static final String VERSION = "0.4.1 beta";

	private final static String USER_AGENT;
	static {
		String os;
		try {
			os = System.getProperty("os.name");
		} catch (Exception e) {
			os = null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("SIMLogManager/" + VERSION);
		if (os != null) {
			sb.append(" (");
			sb.append(os);
			sb.append(')');
		}
		USER_AGENT = sb.toString();
	}

	private static final Logger log = Logger.getLogger(LogManager.class
			.getName());

	private ConfigManager config;
	private File gameDirectory;
	private Hashtable handlerTable = new Hashtable();
	private boolean isSession = false;

	public LogManager(ConfigManager config) throws IOException,
			IllegalConfigurationException {
		this.config = config;

		gameDirectory = new File(config.getProperty("game.directory", "games"));
		if ((!gameDirectory.exists() && !gameDirectory.mkdirs())
				|| !gameDirectory.isDirectory()) {
			throw new IllegalConfigurationException(
					"could not create directory '"
							+ gameDirectory.getAbsolutePath() + '\'');
		}

		// setup logging
		setLogging(config);
		log.info("TAC SIM Log Tool " + VERSION);

		// Find out if anything should be done immediately
		String dataFileName = config.getProperty("file");
		if (dataFileName != null) {
			processSingleFile(dataFileName);
		} else {
			ValueSet games = new ValueSet(config.getProperty("games", ""));
			ValueSet excludes = new ValueSet(config.getProperty("excludes", ""));
			if (games.hasValues()) {
				// Handle all the games
				(new LogSession(this, config.getProperty("server"), games,
						excludes)).start();

			} else {
				System.err.println("nothing to do");
				System.exit(0);
			}
		}
	}

	private void processSingleFile(String dataFileName) {
		boolean showXML = config.getPropertyAsBoolean("xml", false);
		if (showXML) {
			// Show the game data as XML to standard out
			generateXML(dataFileName);
		} else {
			File path = new File(dataFileName);
			if (path.isFile()) {
				parseFile(dataFileName);
			} else {
				for(File file: path.listFiles()) {
					parseFile(file.getAbsolutePath());
				}
			}
		}
	}

	private void parseFile(String dataFileName) {
		try {
			sessionStarted();
			processDataFile(dataFileName);
		} finally {
			sessionEnded();
		}
	}

	// -------------------------------------------------------------------
	// XML handling
	// -------------------------------------------------------------------

	private void generateXML(String filename) {
		try {
			LogReader.generateXML(getDataStream(filename));
		} catch (FileNotFoundException e) {
			log.severe("could not find the game log file '" + filename + '\'');
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not process the game log file '"
					+ filename + '\'', e);
		}
	}

	// -------------------------------------------------------------------
	// Log Handler handling
	// -------------------------------------------------------------------

	private LogHandler setupLogHandler(String handlerName)
			throws IllegalConfigurationException {
		try {
			LogHandler handler = (LogHandler) Class.forName(handlerName)
					.newInstance();
			handler.init(this);
			if (isSession) {
				handler.sessionStarted();
			}
			return handler;
		} catch (Exception e) {
			throw (IllegalConfigurationException) new IllegalConfigurationException(
					"could not create log handler " + handlerName).initCause(e);
		}
	}

	private LogHandler getLogHandler(String type)
			throws IllegalConfigurationException {
		LogHandler handler = (LogHandler) handlerTable.get(type);
		if (handler != null) {
			return handler;
		}

    //TODO-This probably shouldn't be hardcoded...
    String handlerName = config.getProperty("handler." + type, config
				.getProperty("handler", "edu.umich.eecs.tac.logviewer."
						+ "Visualizer"));
		handler = setupLogHandler(handlerName);
		handlerTable.put(type, handler);
		return handler;
	}

	void sessionStarted() {
		if (!isSession) {
			isSession = true;

			Enumeration e = handlerTable.elements();
			while (e.hasMoreElements()) {
				LogHandler handler = (LogHandler) e.nextElement();
				handler.sessionStarted();
			}
		}
	}

	void sessionEnded() {
		if (isSession) {
			isSession = false;

			Enumeration e = handlerTable.elements();
			while (e.hasMoreElements()) {
				LogHandler handler = (LogHandler) e.nextElement();
				handler.sessionEnded();
			}
		}
	}

	void processDataFile(String filename) {
		LogReader reader = null;
		try {
			reader = new LogReader(getDataStream(filename));
			System.out.println("Processing game " + reader.getSimulationID()
					+ " (" + filename + ')');
			String simType = reader.getSimulationType();
			LogHandler handler = getLogHandler(simType);
			try {
				handler.start(reader);
			} finally {
				reader.close();
			}
		} catch (FileNotFoundException e) {
			log.severe("could not find the game log file '" + filename + '\'');
		} catch (EOFException e) {
			if (reader == null || !reader.isCancelled()) {
				// Reader was not cancelled which means the file was truncated
				log.log(Level.SEVERE, "could not process the game log file '"
						+ filename + '\'', e);
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not process the game log file '"
					+ filename + '\'', e);
		}
	}

	private InputStream getDataStream(String filename) throws IOException {
		File fp = new File(filename);
		if (!fp.exists()) {
			filename = filename.endsWith(".gz") ? filename.substring(0,
					filename.length() - 3) : (filename + ".gz");
		}
		if (filename.endsWith(".gz")) {
			return new GZIPInputStream(new FileInputStream(filename));
		} else {
			return new FileInputStream(filename);
		}
	}

	// -------------------------------------------------------------------
	// Public API
	// -------------------------------------------------------------------

	public ConfigManager getConfig() {
		return config;
	}

	public File getGameDirectory() {
		return gameDirectory;
	}

	// -------------------------------------------------------------------
	// API towards the log handler
	// -------------------------------------------------------------------

	File getTempDirectory(String name) throws IOException {
		File fp = new File(gameDirectory, name);
		if ((!fp.exists() && !fp.mkdirs()) || !fp.isDirectory()) {
			throw new IOException("could not create directory '"
					+ fp.getAbsolutePath() + '\'');
		}
		return fp;
	}

	void warn(String message) {
		System.out.println(message);
	}

	// -------------------------------------------------------------------
	// Logging handling
	// -------------------------------------------------------------------

	private void setLogging(ConfigManager config) throws IOException {
		int consoleLevel = config.getPropertyAsInt("log.consoleLevel", 0);
		int fileLevel = config.getPropertyAsInt("log.fileLevel", 6);
		Level consoleLogLevel = LogFormatter.getLogLevel(consoleLevel);
		Level fileLogLevel = LogFormatter.getLogLevel(fileLevel);
		Level logLevel = consoleLogLevel.intValue() < fileLogLevel.intValue() ? consoleLogLevel
				: fileLogLevel;
		boolean showThreads = config.getPropertyAsBoolean("log.threads", false);

		Logger root = Logger.getLogger("");
		Logger.getLogger("se.sics").setLevel(logLevel);

		LogFormatter formatter = new LogFormatter();
		formatter.setAliasLevel(2);
		LogFormatter.setFormatterForAllHandlers(formatter);

		formatter.setShowingThreads(showThreads);
		LogFormatter.setConsoleLevel(consoleLogLevel);

		if (fileLogLevel != Level.OFF) {
			FileHandler handler = new FileHandler("logtool.log", true);
			handler.setFormatter(formatter);
			root.addHandler(handler);
			handler.setLevel(fileLogLevel);
		}
	}

} // LogManager
