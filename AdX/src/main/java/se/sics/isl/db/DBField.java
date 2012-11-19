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
 * DBField
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Oct 09 14:36:22 2002
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 * Purpose :
 *
 */
package se.sics.isl.db;

public abstract class DBField {

	public final static int INTEGER = 0;
	public final static int LONG = 1;
	public final static int TIMESTAMP = 2;
	public final static int DOUBLE = 3;
	public final static int STRING = 4;
	public final static int BYTE = 5;

	public final static int UNIQUE = 1;
	public final static int AUTOINCREMENT = 2;
	public final static int INDEX = 4;
	public final static int MAY_BE_NULL = 8;
	public final static int PRIMARY = 16;

	protected final String name;
	protected final int type;
	protected final int size;
	protected final int flags;
	protected final Object defaultValue;

	protected DBField(String name, int type, int size, int flags,
			Object defaultValue) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.name = name;
		this.type = type;
		this.size = size;
		this.flags = flags;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}

	public int getSize() {
		return size;
	}

	public int getFlags() {
		return flags;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public boolean isUnique() {
		return (flags & UNIQUE) != 0;
	}

	// -------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------

	public static int indexOf(DBField[] fields, int start, int end, String name) {
		for (int i = start; i < end; i++) {
			if (name.equals(fields[i].name)) {
				return i;
			}
		}
		return -1;
	}

} // DBField
