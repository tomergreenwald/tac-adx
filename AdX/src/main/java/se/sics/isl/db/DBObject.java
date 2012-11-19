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
 * DBObject
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Oct 09 10:40:35 2002
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.db;

import com.botbox.util.ArrayUtils;

public class DBObject {

	private String[] names;
	private Object[] values;
	private int fieldNumber = 0;

	public DBObject() {
	}

	public void clear() {
		int count = fieldNumber;
		fieldNumber = 0;
		while (--count >= 0) {
			names[count] = null;
			values[count] = null;
		}
	}

	public int getFieldCount() {
		return fieldNumber;
	}

	public String getFieldName(int index) {
		if (index >= fieldNumber) {
			throw new IndexOutOfBoundsException("index=" + index + ",size="
					+ fieldNumber);
		}
		return names[index];
	}

	public int getInt(String name) {
		return Database.parseInt(getObject(name), 0);
	}

	public void setInt(String name, int value) {
		setObject(name, new Integer(value));
	}

	public long getLong(String name) {
		return Database.parseLong(getObject(name), 0L);
	}

	public void setLong(String name, long value) {
		setObject(name, new Long(value));
	}

	public double getDouble(String name) {
		return Database.parseDouble(getObject(name), 0.0);
	}

	public void setDouble(String name, double value) {
		setObject(name, new Double(value));
	}

	public String getString(String name) {
		Object value = getObject(name);
		return value == null ? null : value.toString();
	}

	public void setString(String name, String value) {
		setObject(name, value);
	}

	public long getTimestamp(String name) {
		return getLong(name);
	}

	public void setTimestamp(String name, long value) {
		setObject(name, new Long(value));
	}

	public Object getObject(String name) {
		int index = ArrayUtils.indexOf(names, 0, fieldNumber, name);
		return index < 0 ? null : values[index];
	}

	public void setObject(String name, Object value) {
		int index = ArrayUtils.indexOf(names, 0, fieldNumber, name);
		if (index < 0) {
			if (names == null) {
				names = new String[10];
				values = new Object[10];
			} else if (names.length <= fieldNumber) {
				names = (String[]) ArrayUtils.setSize(names, fieldNumber + 10);
				values = ArrayUtils.setSize(values, fieldNumber + 10);
			}
			index = fieldNumber++;
			names[index] = name;
		}
		values[index] = value;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer().append(DBObject.class.getName())
				.append('[');
		for (int i = 0, n = fieldNumber; i < n; i++) {
			if (i > 0)
				sb.append(',');
			sb.append(names[i]).append('=').append(values[i]);
		}
		return sb.append(']').toString();
	}

} // DBObject
