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
 * Database
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Oct 09 10:38:23 2002
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.db;

import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;

public abstract class Database {

	private String name;

	protected Database() {
	}

	public final void init(String name, ConfigManager config, String prefix)
			throws IllegalConfigurationException {
		if (this.name != null) {
			throw new IllegalStateException("already initialized");
		}
		validateName(name);
		this.name = name;
		init(config, prefix);
	}

	protected abstract void init(ConfigManager config, String prefix)
			throws IllegalConfigurationException;

	public void validateName(String name) {
		if (name == null || name.length() < 1) {
			throw new IllegalArgumentException("too short name '" + name + '\'');
		}
		char c = name.charAt(0);
		if ((c < 'a') || (c > 'z')) {
			throw new IllegalArgumentException("illegal prefix in name '"
					+ name + '\'');
		}
		for (int i = 1, n = name.length(); i < n; i++) {
			c = name.charAt(i);
			if (((c < 'a') || (c > 'z')) && ((c < 'A') || (c > 'Z'))
					&& ((c < '0') || (c > '9')) && (c != '_')) {
				throw new IllegalArgumentException("illegal character '" + c
						+ "' in '" + name + '\'');
			}
		}
	}

	public final String getName() {
		return name;
	}

	public abstract DBTable createTable(String name);

	public abstract DBTable getTable(String name);

	public abstract void flush();

	public abstract void drop();

	public abstract boolean isClosed();

	public abstract void close();

	// -------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------

	public static int parseInt(Object value, int defaultValue) {
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		}
		if (value == null) {
			return defaultValue;
		}
		try {
			if (value instanceof Long) {
				return (int) ((Long) value).longValue();
			}
			return Integer.parseInt(value.toString());
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static long parseLong(Object value, long defaultValue) {
		if (value instanceof Long) {
			return ((Long) value).longValue();
		}
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		}
		if (value == null) {
			return defaultValue;
		}
		try {
			return Long.parseLong(value.toString());
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public static double parseDouble(Object value, double defaultValue) {
		if (value instanceof Double) {
			return ((Double) value).doubleValue();
		}
		if (value == null) {
			return defaultValue;
		}
		try {
			if (value instanceof Integer) {
				return ((Integer) value).intValue();
			}
			if (value instanceof Long) {
				return ((Long) value).longValue();
			}
			return Double.parseDouble(value.toString());
		} catch (Exception e) {
			return defaultValue;
		}
	}

} // Database
