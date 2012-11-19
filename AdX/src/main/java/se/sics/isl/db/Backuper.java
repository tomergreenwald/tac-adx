/**
 * SICS ISL Java Utilities
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
 * Backuper
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Jun 02 22:55:54 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.db;

import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.util.ArgumentManager;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;

/**
 */
public class Backuper {

	private static final Logger log = Logger
			.getLogger(Backuper.class.getName());

	private Backuper() {
	}

	public static void main(String[] args) throws IllegalConfigurationException {
		Logger.getLogger("").setLevel(Level.FINEST);
		ArgumentManager config = new ArgumentManager("Backuper", args);
		config.addOption("config", "configfile", "set the config file to use");
		config.addOption("source.name", "name", "set the source database name");
		config.addOption("target.name", "name", "set the target database name");
		config.addOption("source.driver", "driver", "set the source driver");
		config.addOption("target.driver", "driver", "set the target driver");
		config.addOption("source.table", "driver", "set the source table");
		config.addOption("target.table", "driver", "set the target table");
		config.addOption("dump", "dump the source table to standard out");
		config.addHelp("h", "show this help message");
		config.addHelp("help");
		config.validateArguments();

		String configFile = config.getArgument("config");
		if (configFile != null) {
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
		}
		// No more need for argument handling. Lets free the memory
		config.finishArguments();

		String sourceName = getConfig(config, "source.name", true);
		String sourceDriver = getConfig(config, "source.driver", true);
		String sourceTable = getConfig(config, "source.table", true);
		boolean dump = config.getPropertyAsBoolean("dump", false);
		String targetName = getConfig(config, "target.name", !dump);
		String targetDriver = getConfig(config, "target.driver", !dump);
		String targetTable = getConfig(config, "target.table", !dump);
		try {
			Database source = (Database) Class.forName(sourceDriver)
					.newInstance();
			source.init(sourceName, config, "");

			DBTable s = source.getTable(sourceTable);
			if (s == null) {
				throw new IllegalConfigurationException("source table '"
						+ sourceTable + "' does not exist");
			}

			if (dump) {
				// Dump the database to standard out
				DBResult result = s.select();
				int count = 1;
				while (result.next()) {
					System.out.print("" + (count++) + ":");
					for (int i = 0, n = result.getFieldCount(); i < n; i++) {
						DBField field = result.getField(i);
						String fieldName = field.getName();
						Object value = result.getObject(fieldName);
						System.out.print("|" + value);
					}
					System.out.println();
				}
				result.close();

			} else {
				Database target = (Database) Class.forName(targetDriver)
						.newInstance();
				target.init(targetName, config, "");

				DBTable t = target.createTable(targetTable);
				DBResult result = s.select();
				DBObject object = new DBObject();
				if (result.next()) {
					for (int i = 0, n = result.getFieldCount(); i < n; i++) {
						DBField field = result.getField(i);
						t.createField(field.getName(), field.getType(), field
								.getSize(), field.getFlags(), field
								.getDefaultValue());
					}
					do {
						for (int i = 0, n = result.getFieldCount(); i < n; i++) {
							DBField field = result.getField(i);
							String fieldName = field.getName();
							Object value = result.getObject(fieldName);
							if (value != null) {
								object.setObject(fieldName, value);
							}
						}
						t.insert(object);
						object.clear();
					} while (result.next());
				}
				target.flush();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getConfig(ConfigManager config, String name,
			boolean required) throws IllegalConfigurationException {
		String value = config.getProperty(name);
		if (required && value == null) {
			throw new IllegalConfigurationException("missing " + name);
		}
		return value;
	}

} // Backuper
