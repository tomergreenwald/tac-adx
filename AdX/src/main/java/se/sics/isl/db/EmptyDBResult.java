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
 * EmptyDBResult
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Apr 16 16:46:00 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.db;

/**
 */
public class EmptyDBResult extends DBResult {

	public EmptyDBResult() {
	}

	public int getFieldCount() {
		return 0;
	}

	public DBField getField(int index) {
		throw new IndexOutOfBoundsException("index=" + index + ",size=" + 0);
	}

	public int getInt(String name) {
		throw new IllegalStateException("no more results");
	}

	public long getLong(String name) {
		throw new IllegalStateException("no more results");
	}

	public double getDouble(String name) {
		throw new IllegalStateException("no more results");
	}

	public String getString(String name) {
		throw new IllegalStateException("no more results");
	}

	public Object getObject(String name) {
		throw new IllegalStateException("no more results");
	}

	public long getTimestamp(String name) {
		throw new IllegalStateException("no more results");
	}

	public boolean next() {
		return false;
	}

	public void close() {
	}

} // EmptyDBResult
