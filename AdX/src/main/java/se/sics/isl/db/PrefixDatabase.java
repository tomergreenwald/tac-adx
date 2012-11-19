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
 * PrefixDatabase
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Jan 14 16:13:44 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.db;

import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;

public class PrefixDatabase extends Database {

	private final String prefix;
	private final Database database;

	public PrefixDatabase(String prefix, Database database,
			ConfigManager config, String configPrefix) {
		this.prefix = prefix;
		this.database = database;

		try {
			init(database.getName(), config, configPrefix);
		} catch (IllegalConfigurationException e) {
		}
	}

	protected void init(ConfigManager config, String prefix) {
	}

	public DBTable createTable(String name) {
		return database.createTable(prefix + name);
	}

	public DBTable getTable(String name) {
		return database.getTable(prefix + name);
	}

	public void flush() {
		database.flush();
	}

	public void drop() {
		// \TODO To be implemented!!!
	}

	public boolean isClosed() {
		return database.isClosed();
	}

	public void close() {
		// \TODO To be implemented!!!
	}

} // PrefixDatabase
