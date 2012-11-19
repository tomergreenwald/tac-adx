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
 * SQLDBField
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Apr 15 15:19:32 2003
 * Updated : $Date: 2008-04-25 13:26:22 -0500 (Fri, 25 Apr 2008) $
 *           $Revision: 4351 $
 *
 */
package se.sics.isl.db.sql;

import se.sics.isl.db.DBField;

/**
 */
public class SQLDBField extends DBField {
	private boolean isSQLite = false;

	public SQLDBField(String name, int type, int size, int flags,
			Object defaultValue, boolean isSQLite) {
		super(name, type, size, flags, defaultValue);
		this.isSQLite = isSQLite;
	}

	protected void addBasicType(StringBuffer sb) {
		sb.append('`').append(name).append('`').append(' ').append(
				getTypeAsString(type));
		if (defaultValue != null) {
			sb.append(" DEFAULT '").append(defaultValue).append('\'');
		}
		// MLB - 20080425 - So, it seems like the competition stuff wants to add
		// some incomplete rows to some tables. Seems like someone was relying
		// on
		// lazy sql implementations to not enforce the NOT NULL requirement for
		// column types...TODO figure out what is going on
		/*
		 * if ((flags & MAY_BE_NULL) == 0) { sb.append(" NOT NULL"); }
		 */
		if ((flags & AUTOINCREMENT) != 0) {
			sb.append(" AUTO_INCREMENT");
		}
	}

	protected void addExtraTypeInfo(StringBuffer sb) {
		if ((flags & PRIMARY) != 0) {
			sb.append(", PRIMARY KEY(`").append(name).append("`)");
		}

		// MLB - 20080411
		if (!isSQLite) {
			if ((flags & INDEX) != 0) {
				sb.append(", INDEX(`").append(name).append("`)");
			}
		}

		if ((flags & UNIQUE) != 0) {
			sb.append(", UNIQUE(`").append(name).append("`)");
		}
	}

	protected void addExtraTypeInfoChange(StringBuffer sb) {
		if ((flags & PRIMARY) != 0) {
			sb.append(", DROP PRIMARY KEY, ADD PRIMARY KEY(`").append(name)
					.append("`)");
		}

		// MLB - 20080411
		if (!isSQLite) {
			if ((flags & INDEX) != 0) {
				sb.append(", ADD INDEX(`").append(name).append("`)");
			}
		}

		if ((flags & UNIQUE) != 0) {
			sb.append(", ADD UNIQUE(`").append(name).append("`)");
		}
	}

	private String getTypeAsString(int type) {
		if (isSQLite) {
			switch (type) {
			case INTEGER:
			case LONG:
			case BYTE:
				return "INTEGER";
			case TIMESTAMP:
				return "TEXT";
			case DOUBLE:
				return "REAL";
			case STRING:
				if (size > 255) {
					return "BLOB";
				} else {
					return "TEXT";
				}
			default:
				// What should be done for unknown types?
				System.err.println("SQLDBField: unknown type: " + type);
				return "INTEGER";
			}
		} else {
			switch (type) {
			case INTEGER:
				// \TODO Should use different INT based on size.
				return "INT";
			case LONG:
				return "BIGINT";
			case TIMESTAMP:
				return "TIMESTAMP";
			case DOUBLE:
				return "DOUBLE";
			case STRING:
				if (size > 255) {
					return "BLOB";
				} else {
					return "VARCHAR(" + (size > 0 ? size : 80) + ')';
				}
			case BYTE:
				return "TINYINT";
			default:
				// What should be done for unknown types?
				System.err.println("SQLDBField: unknown type: " + type);
				return "INT";
			}
		}
	}

} // SQLDBField
