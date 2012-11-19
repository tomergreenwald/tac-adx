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
 * FileDBResult
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Oct 09 18:12:31 2002
 * Updated : $Date: 2008-03-07 13:10:15 -0600 (Fri, 07 Mar 2008) $
 *           $Revision: 3829 $
 */
package se.sics.isl.db.file;

import java.util.ConcurrentModificationException;

import se.sics.isl.db.DBField;
import se.sics.isl.db.DBMatcher;
import se.sics.isl.db.DBResult;
import se.sics.isl.db.Database;

public class FileDBResult extends DBResult {

	private final FileDBTable table;
	private int changeID;

	private int skip;
	private int limit;

	private int lastIndex = -1;
	private int matchesCounter = 0;

	/** Cache for more efficient matching by the FileDBTable */
	private int[] selectIndex;
	private Object[] selectValues;

	FileDBResult(DBMatcher matcher, FileDBTable table, int[] selectIndex,
			Object[] selectValues) {
		this.table = table;
		this.changeID = table.getChangeCount();
		this.selectIndex = selectIndex;
		this.selectValues = selectValues;

		if (matcher != null) {
			skip = matcher.getSkip();
			limit = matcher.getLimit();
		}
	}

	public int getFieldCount() {
		return table.getFieldCount();
	}

	public DBField getField(int index) {
		return table.getField(index);
	}

	public int getInt(String name) {
		FileDBField field = table.getField(name);
		if (lastIndex < 0) {
			throw new IllegalStateException("no more results");
		}

		if (field instanceof IntField) {
			return ((IntField) field).getInt(lastIndex);
		}
		return Database.parseInt(field.getObject(lastIndex), 0);
	}

	public long getLong(String name) {
		FileDBField field = table.getField(name);
		if (lastIndex < 0) {
			throw new IllegalStateException("no more results");
		}

		if (field instanceof LongField) {
			return ((LongField) field).getLong(lastIndex);
		}
		return Database.parseLong(field.getObject(lastIndex), 0L);
	}

	public long getTimestamp(String name) {
		return getLong(name);
	}

	public double getDouble(String name) {
		FileDBField field = table.getField(name);
		if (lastIndex < 0) {
			throw new IllegalStateException("no more results");
		}

		if (field instanceof DoubleField) {
			return ((DoubleField) field).getDouble(lastIndex);
		}
		return Database.parseDouble(field.getObject(lastIndex), 0.0);
	}

	public String getString(String name) {
		FileDBField field = table.getField(name);
		if (lastIndex < 0) {
			throw new IllegalStateException("no more results");
		}
		return field.getString(lastIndex);
	}

	public Object getObject(String name) {
		FileDBField field = table.getField(name);
		if (lastIndex < 0) {
			throw new IllegalStateException("no more results");
		}
		return field.getObject(lastIndex);
	}

	public boolean next() {
		// Check concurrent changes
		if (changeID != table.getChangeCount()) {
			// The table has been modified since this result set was created
			throw new ConcurrentModificationException();
		}

		// Check for limits
		if (skip > 0) {
			// We should skip some matching objects
			int objectCount = table.getObjectCount();
			do {
				lastIndex = table.next(selectIndex, selectValues, lastIndex);
			} while (lastIndex < objectCount && --skip > 0);

			if (lastIndex >= objectCount) {
				return false;
			}
		}

		if ((limit > 0) && (matchesCounter >= limit)) {
			// The limit has been reached
			return false;
		}

		lastIndex = table.next(selectIndex, selectValues, lastIndex);
		if (lastIndex < table.getObjectCount()) {
			matchesCounter++;
			return true;
		}
		return false;
	}

	public void close() {
	}

	// -------------------------------------------------------------------
	// Interface towards the FileDBTable
	// -------------------------------------------------------------------

	int getLastIndex() {
		return lastIndex;
	}

	void setLastIndex(int lastIndex) {
		this.lastIndex = lastIndex;
	}

	void setChangeID(int changeID) {
		this.changeID = changeID;
	}

} // FileDBResult
