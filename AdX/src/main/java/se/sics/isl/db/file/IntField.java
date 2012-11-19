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
 * IntField
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Oct 09 17:28:22 2002
 * Updated : $Date: 2008-03-06 11:11:49 -0600 (Thu, 06 Mar 2008) $
 *           $Revision: 3817 $
 */
package se.sics.isl.db.file;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.botbox.util.ArrayUtils;

public class IntField extends FileDBField {

	private int[] values;

	private int lastIndex = -1;
	private int lastValue;

	private int defValue;

	protected IntField(FileDBTable table, String name, int type, int size,
			int flags, Object defaultValue) {
		super(table, name, type, size, flags, defaultValue);
		defValue = (defaultValue != null) ? (getValue(defaultValue)) : (0);
	}

	protected String getString(int index) {
		int value = getInt(index);
		return Integer.toString(value);
	}

	protected Object getObject(int index) {
		return new Integer(getInt(index));
	}

	int getInt(int index) {
		if (index >= table.getObjectCount()) {
			throw new IllegalArgumentException("no such object: " + index
					+ ",size=" + table.getObjectCount());
		}
		return values[index];
	}

	private int getValue(Object value) {
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		}
		try {
			return Integer.parseInt(value.toString());
		} catch (Exception e) {
			throw new IllegalArgumentException("int expected: " + value);
		}
	}

	protected int indexOf(Object value, int start, int end) {
		return indexOf(getValue(value), start, end);
	}

	protected int indexOf(int val, int start, int end) {
		for (int j = start; j < end; j++) {
			if (values[j] == val) {
				return j;
			}
		}
		return -1;
	}

	protected boolean match(int index, Object value) {
		return value == null || getValue(value) == getInt(index);
	}

	protected void remove(int index) {
		System.arraycopy(values, index + 1, values, index, table
				.getObjectCount()
				- index - 1);
	}

	protected void prepareSet(int index, Object value) {
		if (value != null) {
			lastValue = getValue(value);
		} else if (index >= table.getObjectCount() && defaultValue != null) {
			lastValue = defValue;
		} else {
			lastValue = 0;
		}

		if (isUnique()
				&& (indexOf(lastValue, 0, index) >= 0 || indexOf(lastValue,
						index + 1, table.getObjectCount()) >= 0)) {
			throw new IllegalArgumentException("An object with " + name
					+ " = '" + lastValue + "' already exists");
		}
		lastIndex = index;
	}

	protected void set() {
		if (lastIndex >= 0) {
			if (values == null) {
				values = new int[lastIndex + 10];
			} else if (values.length <= lastIndex) {
				values = ArrayUtils.setSize(values, lastIndex + 10);
			}
			values[lastIndex] = lastValue;
			lastIndex = -1;
		}
	}

	protected void ensureCapacity(int index) {
		int startIndex = -1;
		if (values == null) {
			values = new int[index + 10];
			startIndex = 0;
		} else if (values.length <= index) {
			startIndex = values.length;
			values = ArrayUtils.setSize(values, index + 10);
		}

		if (startIndex >= 0) {
			for (int i = startIndex; i < index; i++) {
				values[i] = defValue;
			}
		}
	}

	// -------------------------------------------------------------------
	// IO handling
	// -------------------------------------------------------------------

	protected void loadState(ObjectInputStream oin, int number)
			throws IOException {
		int[] values = new int[number];
		for (int i = 0; i < number; i++) {
			values[i] = oin.readInt();
		}
		this.values = values;
	}

	protected void saveState(ObjectOutputStream oout) throws IOException {
		int len = table.getObjectCount();
		for (int i = 0; i < len; i++) {
			oout.writeInt(values[i]);
		}
	}

} // IntField
