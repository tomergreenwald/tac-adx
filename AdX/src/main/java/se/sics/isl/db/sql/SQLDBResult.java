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
 * SQLDBResult
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Apr 16 16:42:18 2003
 * Updated : $Date: 2008-04-11 12:25:00 -0500 (Fri, 11 Apr 2008) $
 *           $Revision: 4076 $
 *
 */
package se.sics.isl.db.sql;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.db.DBField;
import se.sics.isl.db.DBResult;

public class SQLDBResult extends DBResult {

	private static final Logger log = Logger.getLogger(SQLDBResult.class
			.getName());

	private SQLDBTable table;
	private Statement stm;
	private ResultSet rs;

	public SQLDBResult(SQLDBTable table, Statement stm, ResultSet rs) {
		this.table = table;
		this.stm = stm;
		this.rs = rs;
	}

	public int getFieldCount() {
		return table.getFieldCount();
	}

	public DBField getField(int index) {
		return table.getField(index);
	}

	public int getInt(String name) {
		try {
			return rs.getInt(name);
		} catch (Exception e) {
			log.log(Level.WARNING, "could not getInt " + name, e);
			return 0;
		}
	}

	public long getLong(String name) {
		try {
			return rs.getLong(name);
		} catch (Exception e) {
			log.log(Level.WARNING, "could not getLong " + name, e);
			return 0L;
		}
	}

	public double getDouble(String name) {
		try {
			return rs.getDouble(name);
		} catch (Exception e) {
			log.log(Level.WARNING, "could not getDouble " + name, e);
			return 0D;
		}
	}

	public String getString(String name) {
		try {
			return rs.getString(name);
		} catch (Exception e) {
			log.log(Level.WARNING, "could not getString " + name, e);
			return null;
		}
	}

	public Object getObject(String name) {
		try {
			return rs.getObject(name);
		} catch (Exception e) {
			log.log(Level.WARNING, "could not getObject " + name, e);
			return null;
		}
	}

	public long getTimestamp(String name) {
		try {
			Timestamp ts = rs.getTimestamp(name);
			return ts != null ? ts.getTime() : 0L;
		} catch (Exception e) {
			log.log(Level.WARNING, "could not getTimestamp " + name, e);
			return 0L;
		}
	}

	public boolean next() {
		boolean hasNext = false;
		try {
			hasNext = rs.next();
		} catch (Exception e) {
			log.log(Level.WARNING, "could not next", e);
		}
		return hasNext;
	}

	public void close() {
		try {
			rs.close();
			stm.close();
		} catch (Exception e) {
		}
	}

} // SQLDBResult
