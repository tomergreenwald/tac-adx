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
 * DatabaseUtils
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Jun 24 14:07:40 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.db.Database;
import se.sics.isl.db.PrefixDatabase;
import se.sics.isl.util.ConfigManager;

/**
 */
public class DatabaseUtils {

	private static final Logger log = Logger.getLogger(DatabaseUtils.class
			.getName());

	// Prevent instances of this class
	private DatabaseUtils() {
	}

	public static Database createDatabase(ConfigManager config,
			String configPrefix) {
		// Database names must match [a-z][a-z0-9_]*
		String databaseName = config.getProperty(configPrefix + "database",
				"infodb");
		return createDatabase(config, configPrefix, databaseName.toLowerCase());
	}

	public static Database createUserDatabase(ConfigManager config,
			String configPrefix, Database parentDatabase) {
		String userDatabaseName = config.getProperty(configPrefix
				+ "user.database");
		return (userDatabaseName != null) ? createDatabase(config, configPrefix
				+ "user.", userDatabaseName.toLowerCase()) : parentDatabase;
	}

	public static Database createChildDatabase(ConfigManager config,
			String configPrefix, String databasePrefix, Database parentDatabase) {
		return new PrefixDatabase(createDatabasePrefix(databasePrefix),
				parentDatabase, config, configPrefix);
	}

	private static String createDatabasePrefix(String databasePrefix) {
		// A database name must match [a-z][a-z0-9_]*. All illegal
		// characters are replaced with '_' or their lower case
		// correspondence.
		databasePrefix = databasePrefix.toLowerCase();

		// Server names are always at least one character
		StringBuffer sb = null;
		char c = databasePrefix.charAt(0);
		if ((c < 'a') || (c > 'z')) {
			// Database names must start with a lower case character
			sb = new StringBuffer().append('s');
		}

		for (int i = 0, n = databasePrefix.length(); i < n; i++) {
			c = databasePrefix.charAt(i);
			if (((c < 'a') || (c > 'z')) && ((c < '0') || (c > '9'))
					&& (c != '_')) {
				if (sb == null) {
					sb = new StringBuffer();
					if (i > 0)
						sb.append(databasePrefix.substring(0, i));
				}
				sb.append('_');
			} else if (sb != null) {
				sb.append(c);
			}
		}
		if (sb != null) {
			return sb.append('_').toString();
		} else {
			return databasePrefix + '_';
		}
	}

	private static Database createDatabase(ConfigManager config,
			String configPrefix, String databaseName) {
		String databaseDriver = config.getProperty(configPrefix
				+ "database.driver", "se.sics.isl.db.file.FileDatabase");
		Database database = null;
		do {
			try {
				Database base = (Database) Class.forName(databaseDriver)
						.newInstance();
				base.init(databaseName, config, configPrefix + "database.");
				database = base;
			} catch (Exception e) {
				log.log(Level.SEVERE,
						"could not create database driver of type '"
								+ databaseDriver + '\'', e);
				log.severe("will retry database " + databaseName
						+ " in 60 seconds...");
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e2) {
				}
			}
		} while (database == null);
		return database;
	}

} // DatabaseUtils
