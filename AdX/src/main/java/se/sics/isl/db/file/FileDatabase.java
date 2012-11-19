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
 * FileDatabase
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Oct 09 13:01:28 2002
 * Updated : $Date: 2008-03-06 11:33:35 -0600 (Thu, 06 Mar 2008) $
 *           $Revision: 3819 $
 */
package se.sics.isl.db.file;

import java.io.File;
import java.util.logging.Logger;

import com.botbox.util.ArrayUtils;
import se.sics.isl.db.*;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;

public class FileDatabase extends Database {

	private final static Logger log = Logger.getLogger(FileDatabase.class
			.getName());

	private File databaseRoot;
	private FileDBTable[] tables;
	private int tableNumber = 0;
	private boolean isDropped = false;
	private boolean isClosed = false;

	public FileDatabase() {
	}

	protected void init(ConfigManager config, String prefix) {
		// String directory, boolean create) {
		String directory = config.getProperty(prefix + "file.path", getName());
		File file = new File(directory);
		boolean create = config.getPropertyAsBoolean(prefix + "file.create",
				false);

		if (file.exists()) {
			if (file.isDirectory()) {
				databaseRoot = file.getAbsoluteFile();
			} else {
				throw new IllegalArgumentException("File '" + directory
						+ "' is not a directory");
			}
		} else if (create && file.mkdirs()) {
			databaseRoot = file.getAbsoluteFile();
		} else {
			throw new IllegalArgumentException((create) ? ("Database '"
					+ directory + "' could not be created") : ("Database '"
					+ directory + "' does not exist"));
		}
		log.info(getName() + ": database opened as " + databaseRoot.getPath());
	}

	public DBTable createTable(String name) {
		if (isDropped) {
			throw new IllegalStateException("database " + getName()
					+ " has been dropped");
		}
		if (getTable(name) != null) {
			throw new IllegalArgumentException("table '" + name
					+ "' already exists");
		}

		validateName(name);

		FileDBTable table = new FileDBTable(this, name, true);
		log.info(getName() + ": added table " + name);
		return addTable(table);
	}

	public DBTable getTable(String name) {
		int index = DBTable.indexOf(tables, 0, tableNumber, name);
		if (index < 0 && !isDropped) {
			FileDBTable table = new FileDBTable(this, name, false);
			if (table.exists()) {
				log.finest(getName() + ": loaded table " + name);
				return addTable(table);
			} else {
				return null;
			}
		} else {
			return tables[index];
		}
	}

	public void flush() {
		for (int i = 0; i < tableNumber; i++) {
			tables[i].flush();
		}
	}

	public void drop() {
		if (!isDropped) {
			isDropped = true;

			log.info(getName() + ": database dropped");
			int len = tableNumber;
			tableNumber = 0;
			for (int i = 0; i < tableNumber; i++) {
				tables[i].dropTable();
			}
			tables = null;

			File[] files = databaseRoot.listFiles();
			for (int i = 0, n = files.length; i < n; i++) {
				files[i].delete();
			}
			databaseRoot.delete();
		}
	}

	public boolean isClosed() {
		return isClosed;
	}

	// \TODO Implement complete close
	public void close() {
		if (!isClosed) {
			isClosed = true;
			for (int i = 0; i < tableNumber; i++) {
				tables[i].flush();
			}
		}
	}

	private FileDBTable addTable(FileDBTable table) {
		if (tables == null) {
			tables = new FileDBTable[5];
		} else if (tables.length <= tableNumber) {
			tables = (FileDBTable[]) ArrayUtils
					.setSize(tables, tableNumber + 5);
		}
		tables[tableNumber++] = table;
		return table;
	}

	// -------------------------------------------------------------------
	// API towards FileDBTable
	// -------------------------------------------------------------------

	void tableDropped(FileDBTable table) {
		int index = ArrayUtils.indexOf(tables, 0, tableNumber, table);
		if (index >= 0) {
			tableNumber--;
			tables[index] = tables[tableNumber];
			tables[tableNumber] = null;
			log.info(getName() + ": dropped table " + table.getName());
		}
	}

	File getDatabaseRoot() {
		return databaseRoot;
	}

	// -------------------------------------------------------------------
	// Test Main
	// -------------------------------------------------------------------

	public static void main(String[] args) throws IllegalConfigurationException {
		FileDatabase db = new FileDatabase();
		ConfigManager config = new ConfigManager();
		config.setProperty("file.path", "testdb");
		config.setProperty("file.create", "true");
		db.init("test", config, "");

		DBTable table = db.getTable("test");
		if (table != null) {
			db.drop();
			db = new FileDatabase();
			db.init("test", config, "");
			table = db.getTable("test");
		}
		if (table == null) {
			System.out.println("Creating table test");
			table = db.createTable("test");
			DBField field = table.createField("id", DBField.INTEGER, 32,
					DBField.UNIQUE, null);
			field = table.createField("score", DBField.INTEGER, 32, 0, null);
			DBObject o = new DBObject();
			for (int i = 0, n = 20; i < n; i++) {
				o.setInt("id", i);
				o.setInt("score", i * 32);
				table.insert(o);
			}
			table.flush();
		} else {
			System.out.println("Database test already existed");
		}

		DBResult result = table.select();
		while (result.next()) {
			System.out.println("RESULT: id=" + result.getInt("id") + " score="
					+ result.getInt("score"));
		}

		DBMatcher matcher = new DBMatcher();
		matcher.setInt("id", 17);
		result = table.select(matcher);
		while (result.next()) {
			System.out.println("MATCHED RESULT: id=" + result.getInt("id")
					+ " score=" + result.getInt("score"));
		}

		System.out.println("No Elements:"
				+ ((FileDBTable) table).getObjectCount());

		matcher.clear();
		matcher.setLimit(1);
		matcher.setInt("id", 2);
		System.out.println("REMOVED " + table.remove(matcher) + " objects");

		matcher.clear();
		matcher.setLimit(5);
		System.out.println("No Elements:"
				+ ((FileDBTable) table).getObjectCount());
		result = table.select(matcher);
		while (result.next()) {
			System.out.println("LIMIT RESULT: id=" + result.getInt("id")
					+ " score=" + result.getInt("score") + " lastIndex="
					+ ((FileDBResult) result).getLastIndex() + " objectCount="
					+ ((FileDBTable) table).getObjectCount());
		}

		DBObject o = new DBObject();
		o.setInt("id", 12);
		o.setInt("score", 2343);
		table.insert(o);
		table.flush();
	}

} // FileDatabase
