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
 * Main
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Oct 07 17:58:33 2002
 * Updated : $Date: 2008-04-04 21:23:36 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3984 $
 */
package se.sics.tasim.sim;

import java.io.IOException;
import java.util.StringTokenizer;

import se.sics.isl.util.AdminMonitor;
import se.sics.isl.util.ArgumentManager;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;
import se.sics.tasim.is.InfoConnection;

public class Main {

	private final static String DEFAULT_CONFIG = "config/server.conf";

	public final static String CONF = "sim.";

	private Main() {
	}

	// -------------------------------------------------------------------
	// Simulator startup
	// -------------------------------------------------------------------

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, IllegalConfigurationException {
		ArgumentManager config = new ArgumentManager("Simulator", args);
		config.addOption("config", "configfile", "set the config file to use");
		config.addOption("serverName", "serverName", "set the server name");
		config.addOption("log.consoleLevel", "level",
				"set the console log level");
		config.addOption("log.fileLevel", "level", "set the file log level");
		config.addHelp("h", "show this help message");
		config.addHelp("help");
		config.validateArguments();

		String configFile = config.getArgument("config", DEFAULT_CONFIG);
		try {
			config.loadConfiguration(configFile);
			config.removeArgument("config");
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			config.usage(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		// No more need for argument handling. Lets free the memory
		config.finishArguments();

		Admin admin = new Admin(config);

		// Start all simulation managers
		String[] simNames = split(config.getProperty(CONF + "manager.names"));
		SimulationManager[] simManagers = (SimulationManager[]) createInstances(
				config, CONF + "manager", SimulationManager.class, simNames,
				true);
		for (int i = 0, n = simNames.length; i < n; i++) {
			simManagers[i].init(admin, simNames[i]);
		}

		// Gateways are the last things to start since they open the
		// access points to the outside world. Allow setup without any
		// gateways because sometimes all agents are builtin.
		String[] gateways = split(config.getProperty(CONF + "gateway.names"));
		Gateway[] ways = (Gateway[]) createInstances(config, CONF + "gateway",
				Gateway.class, gateways, false);
		if (ways != null) {
			for (int i = 0, n = ways.length; i < n; i++) {
				ways[i].init(admin, gateways[i]);
				ways[i].start();
			}
		}

		if (config.getPropertyAsBoolean("admin.gui", false)) {
			AdminMonitor adminMonitor = AdminMonitor.getDefault();
			if (adminMonitor != null) {
				String bounds = config.getProperty("admin.bounds");
				if (bounds != null) {
					adminMonitor.setBounds(bounds);
				}
				adminMonitor.setTitle(admin.getServerName());
				adminMonitor.start();
			}
		}

		if (config.getPropertyAsBoolean(CONF + "createSimulation", false)) {
			// Immediately create an empty simulation for testing purposes
			if (!admin.createSimulation(null, null, false)) {
				System.exit(1);
			}
		}
	}

	private static String[] split(String nList) {
		if (nList != null) {
			StringTokenizer tok = new StringTokenizer(nList, ", \t");
			int len = tok.countTokens();
			if (len > 0) {
				String[] names = new String[len];
				for (int i = 0; i < len; i++) {
					names[i] = tok.nextToken();
				}
				return names;
			}
		}
		return null;
	}

	private static Object[] createInstances(ConfigManager config, String name,
			Class type, String[] simNames, boolean exitIfEmpty) {
		if (simNames != null && simNames.length > 0) {
			String className = null;
			String iName = null;
			try {
				Object[] vector = (Object[]) java.lang.reflect.Array
						.newInstance(type, simNames.length);
				for (int i = 0, n = simNames.length; i < n; i++) {
					iName = simNames[i];
					className = config.getProperty(name + '.' + iName
							+ ".class");
					if (className == null) {
						throw new IllegalArgumentException(
								"no class for manager " + iName + " specified");
					}
					vector[i] = Class.forName(className).newInstance();
				}
				return vector;
			} catch (Exception e) {
				System.err.println("could not create " + name + ' ' + iName
						+ " '" + className + '\'');
				e.printStackTrace();
				System.exit(1);
			}
		}
		if (exitIfEmpty) {
			System.err.println("no " + name + " specified in configuration");
			System.exit(1);
		}
		return null;
	}

} // Main
