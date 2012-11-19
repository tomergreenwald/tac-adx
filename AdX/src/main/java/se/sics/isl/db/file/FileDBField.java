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
 * FileDBField
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Oct 09 17:26:41 2002
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 * Purpose :
 *
 */
package se.sics.isl.db.file;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import se.sics.isl.db.DBField;

public abstract class FileDBField extends DBField {

	protected final FileDBTable table;

	protected FileDBField(FileDBTable table, String name, int type, int size,
			int flags, Object defaultValue) {
		super(name, type, size, flags, defaultValue);
		this.table = table;
	}

	protected abstract String getString(int index);

	protected abstract Object getObject(int index);

	protected abstract int indexOf(Object value, int start, int end);

	protected abstract boolean match(int index, Object value);

	protected abstract void remove(int index);

	protected abstract void prepareSet(int index, Object value);

	protected abstract void set();

	protected abstract void ensureCapacity(int index);

	protected abstract void loadState(ObjectInputStream oin, int len)
			throws IOException, ClassNotFoundException;

	protected abstract void saveState(ObjectOutputStream oout)
			throws IOException;

} // FileDBField
