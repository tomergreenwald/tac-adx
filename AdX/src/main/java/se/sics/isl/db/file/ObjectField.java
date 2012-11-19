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
 * ObjectField
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Oct 09 17:28:22 2002
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.db.file;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.botbox.util.ArrayUtils;

public class ObjectField extends FileDBField {

	private Object[] values;

	private int lastIndex = -1;
	private Object lastValue;

	protected ObjectField(FileDBTable table, String name, int type, int size,
			int flags, Object defaultValue) {
		super(table, name, type, size, flags, defaultValue);
		if (type == BYTE) {
			throw new IllegalArgumentException(
					"byte format not yet supported!!!");
		}
	}

	protected String getString(int index) {
		Object value = getObject(index);
		return value == null ? null : value.toString();
	}

	protected Object getObject(int index) {
		if (index >= table.getObjectCount()) {
			throw new IllegalArgumentException("no such Object: " + index
					+ ",size=" + table.getObjectCount());
		}
		return values[index];
	}

	protected int indexOf(Object val, int start, int end) {
		if (val == null) {
			for (int j = start; j < end; j++) {
				if (values[j] == null) {
					return j;
				}
			}
		} else {
			for (int j = start; j < end; j++) {
				if (val.equals(values[j])) {
					return j;
				}
			}
		}
		return -1;
	}

	protected boolean match(int index, Object value) {
		return value == null || value.equals(getObject(index));
	}

	protected void remove(int index) {
		System.arraycopy(values, index + 1, values, index, table
				.getObjectCount()
				- index - 1);
	}

	protected void prepareSet(int index, Object value) {
		if (value != null) {
			// if (type == BYTE && value.getClass() != byte[].class) {
			// throw new IllegalArgumentException("value must be byte array");
			// }
			// \TODO Only store strings for now!
			lastValue = value.toString();
		} else if (index >= table.getObjectCount() && defaultValue != null) {
			lastValue = defaultValue;
		} else {
			lastValue = null;
		}

		if (isUnique()
				&& (indexOf(lastValue, 0, index) >= 0 || indexOf(lastValue,
						index + 1, table.getObjectCount()) >= 0)) {
			throw new IllegalArgumentException("An Object with " + name
					+ " = '" + lastValue + "' already exists");
		}
		lastIndex = index;
	}

	protected void set() {
		if (lastIndex >= 0) {
			if (values == null) {
				values = new Object[lastIndex + 10];
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
			values = new Object[index + 10];
			startIndex = 0;
		} else if (values.length <= index) {
			startIndex = values.length;
			values = ArrayUtils.setSize(values, index + 10);
		}

		if (startIndex >= 0 && defaultValue != null) {
			for (int i = startIndex; i < index; i++) {
				values[i] = defaultValue;
			}
		}
	}

	/*****************************************************************************
	 * IO handling
	 ****************************************************************************/

	protected void loadState(ObjectInputStream oin, int number)
			throws IOException, ClassNotFoundException {
		Object[] values = new Object[number];
		for (int i = 0; i < number; i++) {
			values[i] = oin.readObject();
		}
		this.values = values;
	}

	protected void saveState(ObjectOutputStream oout) throws IOException {
		int len = table.getObjectCount();
		// Assume strings for now
		for (int i = 0; i < len; i++) {
			Object v = values[i];
			oout.writeObject(v == null ? null : v.toString());
		}
	}

} // ObjectField
