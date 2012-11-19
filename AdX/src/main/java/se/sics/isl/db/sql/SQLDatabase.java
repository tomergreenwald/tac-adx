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
 * SQLDatabase
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Apr 15 14:11:27 2003
 * Updated : $Date: 2008-04-11 13:26:06 -0500 (Fri, 11 Apr 2008) $
 *           $Revision: 4077 $
 */
package se.sics.isl.db.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.botbox.util.ArrayUtils;
import se.sics.isl.db.*;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;

/**
 */
public class SQLDatabase extends Database {

	private static final Logger log = Logger.getLogger(SQLDatabase.class
			.getName());

	// The default Driver and database URL
	private String databaseURL = "jdbc:mysql://localhost:3306/mysql";
	private String driverName = "org.gjt.mm.mysql.Driver";
	private String databaseUser = null;
	private String databasePassword = null;

	private String database;
	private Connection databaseConnection;

	private SQLDBTable[] tables;
	private int tableNumber = 0;

	private boolean isDropped = false;
	private boolean isClosed = false;

	private boolean isSQLite = false;

	public SQLDatabase() {
	}

	protected void init(ConfigManager config, String prefix)
			throws IllegalConfigurationException {
		String name = getName();

		database = config.getProperty(prefix + "sql.database", name);
		validateName(database);

		driverName = config.getProperty(prefix + "sql.driver", driverName);
		databaseURL = config.getProperty(prefix + "sql.url", databaseURL);
		databaseUser = config.getProperty(prefix + "sql.user");
		databasePassword = config.getProperty(prefix + "sql.password");
		try {
			Driver driver = (Driver) Class.forName(driverName).newInstance();

			Connection cdb = getConnection(false);

			isSQLite = config.getProperty(prefix + "sql.driver").equals(
					"org.sqlite.JDBC");

			// we do not need to create a database when using SQLite
			if (!isSQLite) {
				Statement stm = cdb.createStatement();
				stm.execute("CREATE DATABASE IF NOT EXISTS " + database);
				stm.close();
			}

			cdb.setCatalog(database);

			// \TODO Require meta information for now.
			DatabaseMetaData meta = cdb.getMetaData();
			log.info(getName() + ": using database " + database + " ("
					+ meta.getDatabaseProductName() + ' '
					+ meta.getDatabaseProductVersion() + ')');
			// rs = meta.getCatalogs();
			// while (rs.next()) {
			// System.out.println("CATALOG: " + rs.getString(1));
			// }
			// rs.close();;

			ResultSet rs = meta.getTables(database, null, null, null);
			ArrayList tableList = null;
			while (rs.next()) {
				// All tables in the database
				if (tableList == null) {
					tableList = new ArrayList();
				}

				// rs.getString(3) is the table name
				// we want to skip the SQLite internal tables
				if (rs.getString(3).indexOf("SQLITE") == -1) {
					tableList.add(new SQLDBTable(this, rs.getString(3), meta));
				}
			}
			rs.close();

			if (tableList != null) {
				this.tableNumber = tableList.size();
				this.tables = (SQLDBTable[]) tableList
						.toArray(new SQLDBTable[this.tableNumber]);
			}

		} catch (Exception e) {
			log.log(Level.SEVERE, getName() + ": could not open database "
					+ database, e);
			throw new IllegalConfigurationException(
					"could not open SQL database " + database);
		}
	}

	public DBTable createTable(String name) {
		if (isDropped) {
			throw new IllegalStateException("database " + getName()
					+ " has been dropped");
		}
		if (isClosed) {
			throw new IllegalStateException("database " + getName()
					+ " has been closed");
		}

		if (getTable(name) != null) {
			throw new IllegalArgumentException("table '" + name
					+ "' already exists");
		}

		validateName(name);

		SQLDBTable table = new SQLDBTable(this, name);
		log.fine(getName() + ": added table " + name);
		return addTable(table);
	}

	public DBTable getTable(String name) {
		int index = DBTable.indexOf(tables, 0, tableNumber, name);
		return index >= 0 ? tables[index] : null;
	}

	public void flush() {
		for (int i = 0; i < tableNumber; i++) {
			tables[i].flush();
		}
	}

	public void drop() {
		if (!isDropped) {
			isDropped = true;
			isClosed = true;

			log.fine(getName() + ": database dropped");
			int len = tableNumber;
			tableNumber = 0;
			for (int i = 0; i < tableNumber; i++) {
				tables[i].dropTable();
			}
			tables = null;

			// DROP TABLE!!!
			try {
				Connection cdb = getConnection(true);
				Statement stm = cdb.createStatement();
				stm.execute("DROP DATABASE " + database);
				stm.close();
				cdb.close();
			} catch (Exception e) {
				log.log(Level.SEVERE, getName() + ": could not drop database "
						+ database, e);
			}
		}
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void close() {
		if (!isClosed) {
			isClosed = true;
			for (int i = 0; i < tableNumber; i++) {
				tables[i].flush();
			}
			try {
				if (databaseConnection != null
						&& !databaseConnection.isClosed()) {
					databaseConnection.close();
				}
			} catch (Exception e) {
			}
		}
	}

	private SQLDBTable addTable(SQLDBTable table) {
		if (tables == null) {
			tables = new SQLDBTable[5];
		} else if (tables.length <= tableNumber) {
			tables = (SQLDBTable[]) ArrayUtils.setSize(tables, tableNumber + 5);
		}
		tables[tableNumber++] = table;
		return table;
	}

	// -------------------------------------------------------------------
	// API towards SQLDBTable
	// -------------------------------------------------------------------

	Connection getConnection() throws SQLException {
		return getConnection(true);
	}

	private Connection getConnection(boolean useDatabase) throws SQLException {
		if (isDropped) {
			throw new SQLException("database " + getName()
					+ " has been dropped");
		}
		if (isClosed) {
			throw new SQLException("database " + getName() + " has been closed");
		}

		Connection connection = this.databaseConnection;
		if (connection == null || connection.isClosed()) {
			log.finest(getName() + ": connecting to database " + databaseURL
					+ " (" + database + ')');
			if (databaseUser != null) {
				connection = this.databaseConnection = DriverManager
						.getConnection(databaseURL, databaseUser,
								databasePassword == null ? ""
										: databasePassword);
			} else {
				connection = this.databaseConnection = DriverManager
						.getConnection(databaseURL, null);
			}
		}
		if (useDatabase) {
			connection.setCatalog(database);
		}
		return connection;
	}

	void handleError(Exception e) {
		// \TODO Perhaps some errors should cause a reconnect to the database?
	}

	String getDatabaseName() {
		return database;
	}

	boolean isSQLite() {
		return isSQLite;
	}

	void tableDropped(SQLDBTable table) {
		int index = ArrayUtils.indexOf(tables, 0, tableNumber, table);
		if (index >= 0) {
			tableNumber--;
			tables[index] = tables[tableNumber];
			tables[tableNumber] = null;
			log.fine(getName() + ": dropped table " + table.getName());
		}
	}

	// -------------------------------------------------------------------
	// Test Main
	// -------------------------------------------------------------------

	public static void main(String[] args) throws IllegalConfigurationException {
		Logger.getLogger("").setLevel(Level.FINEST);

		ConfigManager config = new ConfigManager();
		config.setProperty("sql.database", "mytest");
		SQLDatabase db = new SQLDatabase();
		db.init("test", config, "");
		DBTable table = db.getTable("test");
		// if (table != null) {
		// db.drop();
		// db = new SQLDatabase("test", config, "");
		// table = db.getTable("test");
		// }

		if (table == null) {
			System.out.println("Creating table test");
			table = db.createTable("test");
			DBField field = table.createField("id", DBField.INTEGER, 32,
					DBField.UNIQUE | DBField.PRIMARY, null);
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
			if (!table.hasField("hacke")) {
				System.out.println("creating field hacke");
				table.createField("hacke", DBField.INTEGER, 32, DBField.UNIQUE
						| DBField.INDEX | DBField.AUTOINCREMENT, null);
				table.flush();
			} else {
				System.out.println("field 'hacke' aready exists");
			}
		}

		DBMatcher matcher = new DBMatcher();
		matcher.setLimit(5, 5);
		DBResult result = table.select(matcher);
		while (result.next()) {
			System.out.println("RESULT: id=" + result.getInt("id") + " score="
					+ result.getInt("score"));
		}

		matcher = new DBMatcher();
		matcher.setInt("id", 17);
		result = table.select(matcher);
		while (result.next()) {
			System.out.println("MATCHED RESULT: id=" + result.getInt("id")
					+ " score=" + result.getInt("score"));
		}

		System.out.println("Number of rows: " + table.getObjectCount());

		matcher.clear();
		// matcher.setLimit(1);
		matcher.setInt("id", 2);
		System.out.println("REMOVED " + table.remove(matcher)
				+ " objects with id=2");

		System.out.println("Number of rows: " + table.getObjectCount());

		System.out.println("Adding item with id 2 and 2343");
		DBObject o = new DBObject();
		o.setInt("id", 2);
		o.setInt("score", 2343);
		table.insert(o);
		table.flush();

		System.out.println("Number of rows: " + table.getObjectCount());

		matcher.clear();
		matcher.setInt("id", 2);
		result = table.select(matcher);
		while (result.next()) {
			System.out.println("MATCHED RESULT: id=" + result.getInt("id")
					+ " score=" + result.getInt("score"));
		}

		System.out.println("Updating item 2 to score 64");
		o.clear();
		o.setInt("score", 64);
		table.update(matcher, o);

		System.out.println("Number of rows: " + table.getObjectCount());

		matcher.clear();
		matcher.setInt("id", 2);
		result = table.select(matcher);
		while (result.next()) {
			System.out.println("MATCHED RESULT: id=" + result.getInt("id")
					+ " score=" + result.getInt("score"));
		}
	}

} // SQLDatabase
