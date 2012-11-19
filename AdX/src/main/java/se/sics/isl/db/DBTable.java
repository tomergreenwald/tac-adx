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
 * DBTable
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Oct 09 10:39:07 2002
 * Updated : $Date: 2008-04-11 12:13:50 -0500 (Fri, 11 Apr 2008) $
 *           $Revision: 4075 $
 */
package se.sics.isl.db;

public abstract class DBTable {

	protected final String name;

	public DBTable(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract boolean hasField(String name);

	public DBField createField(String name, int type, int size, int flags) {
		return createField(name, type, size, flags, null);
	}

	public abstract DBField createField(String name, int type, int size,
			int flags, Object defaultValue);

	public abstract void drop();

	public abstract int getFieldCount();

	public abstract DBField getField(int index);

	public abstract int getObjectCount();

	public abstract void insert(DBObject object);

	public abstract int update(DBMatcher matcher, DBObject value);

	// protected abstract boolean update(Object key, DBObject object);

	public abstract int remove(DBMatcher matcher);

	// protected abstract boolean remove(Object key, DBObject object);

	public abstract DBResult select();

	public abstract DBResult select(DBMatcher matcher);

	public void flush() {
	}

	// -------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------

	public static int indexOf(DBTable[] tables, int start, int end, String name) {
		for (int i = start; i < end; i++) {
			// MLB - 20080411 - SQLite has some issues
			// with converting the table names to
			// uppercase. If we change this line to
			// ignore case, then it fixes the problem,
			// but we now need to worry about having
			// multiple tables with the same name, but
			// different case. I don't think that would
			// ever happen.
			if (name.equalsIgnoreCase(tables[i].name)) {
				return i;
			}
		}
		return -1;
	}

} // DBTable
