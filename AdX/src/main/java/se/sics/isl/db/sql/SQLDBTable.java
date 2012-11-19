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
 * SQLDBTable
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Apr 15 14:33:12 2003
 * Updated : $Date: 2008-04-11 13:26:06 -0500 (Fri, 11 Apr 2008) $
 *           $Revision: 4077 $
 */
package se.sics.isl.db.sql;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.botbox.util.ArrayUtils;
import se.sics.isl.db.DBField;
import se.sics.isl.db.DBMatcher;
import se.sics.isl.db.DBObject;
import se.sics.isl.db.DBResult;
import se.sics.isl.db.DBTable;
import se.sics.isl.db.EmptyDBResult;

public class SQLDBTable extends DBTable {

	private static final Logger log = Logger.getLogger(SQLDBTable.class
			.getName());

	protected final SQLDatabase database;
	private boolean isDropped = false;

	private SQLDBField[] fields;
	private int fieldNumber = 0;

	private int dirtyStartField = -1;
	private boolean dirtyFields = false;

	public SQLDBTable(SQLDatabase database, String name,
			DatabaseMetaData metaData) {
		super(name);
		this.database = database;
		// Read fields from meta data
		try {
			ResultSet rs = metaData.getColumns(database.getDatabaseName(),
					null, name, null);
			while (rs.next()) {
				String columnName = rs.getString(4);
				int sqlType = rs.getInt(5);
				int size = rs.getInt(7);
				int type = getDBType(sqlType);
				String defaultValue = rs.getString(13);
				if (type >= 0) {
					// \TODO ADD SUPPORT FOR FLAGS!!!
					addField(columnName, type, size, 0, defaultValue);

				} else {
					// Incompatible type => ignore column for now
					log.warning("ignore column " + columnName
							+ " of unsupported type " + rs.getString(6));
				}
			}

		} catch (Exception e) {
			log.log(Level.SEVERE, "could not read meta info for column "
					+ this.name, e);
			log.log(Level.SEVERE, e.getMessage());
			database.handleError(e);
		}
	}

	public SQLDBTable(SQLDatabase database, String name) {
		super(name);
		this.database = database;
	}

	public boolean hasField(String name) {
		return DBField.indexOf(fields, 0, fieldNumber, name) >= 0;
	}

	public DBField createField(String name, int type, int size, int flags,
			Object defaultValue) {
		if (isDropped) {
			throw new IllegalStateException("table " + this.name
					+ " has been dropped");
		}
		if (database.isClosed()) {
			throw new IllegalStateException("database with table " + this.name
					+ " has been closed");
		}
		if (DBField.indexOf(fields, 0, fieldNumber, name) >= 0) {
			throw new IllegalArgumentException("field already exists");
		}

		database.validateName(name);

		if (dirtyStartField < 0) {
			dirtyStartField = fieldNumber;
			dirtyFields = true;
		}
		return addField(name, type, size, flags, defaultValue);
	}

	private SQLDBField addField(String name, int type, int size, int flags,
			Object defaultValue) {
		SQLDBField field = new SQLDBField(name, type, size, flags,
				defaultValue, database.isSQLite());
		if (fields == null) {
			fields = new SQLDBField[5];
		} else if (fields.length == fieldNumber) {
			fields = (SQLDBField[]) ArrayUtils.setSize(fields, fieldNumber + 5);
		}

		fields[fieldNumber++] = field;
		return field;
	}

	public void drop() {
		if (dropTable()) {
			log.finest(name + ": table dropped");
			database.tableDropped(this);

			executeStatement("DROP TABLE `" + this.name + '`');
		}
	}

	protected boolean dropTable() {
		if (isDropped) {
			return false;
		}
		isDropped = true;

		fieldNumber = 0;
		fields = null;
		dirtyFields = false;
		return true;
	}

	public int getFieldCount() {
		return fieldNumber;
	}

	public DBField getField(int index) {
		if (index >= fieldNumber) {
			throw new IndexOutOfBoundsException("index=" + index + ",size="
					+ fieldNumber);
		}
		return fields[index];
	}

	public int getObjectCount() {
		int count = 0;

		if (dirtyFields) {
			flush();
		}
		try {
			Statement stm = database.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT count(*) FROM `" + name
					+ '`');
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			stm.close();
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not get table size from " + this.name,
					e);
			database.handleError(e);
		}
		return count;
	}

	public void insert(DBObject object) throws NumberFormatException {
		if (isDropped) {
			throw new IllegalStateException("table " + this.name
					+ " has been dropped");
		}
		if (object.getFieldCount() == 0) {
			return;
		}

		if (dirtyFields) {
			flush();
		}
		validate(object);

		// INSERT INTO `pekka` (`id`, `name`, `test`, `parent`)
		// VALUES ('1', 'testar', NOW(NULL), '-1');

		StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO `").append(name).append("` (");
		for (int i = 0, n = object.getFieldCount(); i < n; i++) {
			if (i > 0)
				sb.append(',');
			sb.append('`').append(object.getFieldName(i)).append('`');
		}
		sb.append(") VALUES (");
		for (int i = 0, n = object.getFieldCount(); i < n; i++) {
			if (i > 0)
				sb.append(',');
			sb.append('?');
		}
		sb.append(')');

		String sqlQuery = sb.toString();
		try {
			PreparedStatement pstmt = database.getConnection()
					.prepareStatement(sqlQuery);
			for (int i = 0, n = object.getFieldCount(); i < n; i++) {
				Object value = object.getObject(object.getFieldName(i));
				pstmt.setObject(i + 1, value);
			}

			if (pstmt.executeUpdate() == 0) {
				log.warning("could not insert data " + object);
			}
			pstmt.close();
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not insert data " + object + ": "
					+ sqlQuery, e);
			database.handleError(e);
			throw (IllegalArgumentException) new IllegalArgumentException(
					"could not insert data").initCause(e);
		}
	}

	public int update(DBMatcher matcher, DBObject object) {
		if (isDropped) {
			throw new IllegalStateException("table " + this.name
					+ " has been dropped");
		}
		if (object.getFieldCount() == 0) {
			return 0;
		}

		if (dirtyFields) {
			flush();
		}
		validate(object);

		// UPDATE `pekka` SET `name` = 'test', `test` = '20030416160350'
		// WHERE `id` = '1' LIMIT 1;

		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE `").append(name).append("` SET ");
		for (int i = 0, n = object.getFieldCount(); i < n; i++) {
			String fieldName = object.getFieldName(i);
			if (i > 0)
				sb.append(',');
			sb.append('`').append(fieldName).append("`=?");
		}
		addWhereClausePrefix(sb, matcher);

		String sqlQuery = sb.toString();
		try {
			PreparedStatement pstmt = database.getConnection()
					.prepareStatement(sqlQuery);

			for (int i = 0, n = object.getFieldCount(); i < n; i++) {
				String fieldName = object.getFieldName(i);
				Object value = object.getObject(fieldName);
				// \TODO Can this be done this way or should it use SQL types??
				pstmt.setObject(i + 1, value);
			}
			addWhereClausePostfix(pstmt, object.getFieldCount(), matcher);
			int result = pstmt.executeUpdate();
			pstmt.close();
			return result;
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not execute update: " + sqlQuery, e);
			database.handleError(e);
			throw (IllegalArgumentException) new IllegalArgumentException(
					"could not update data").initCause(e);
		}
	}

	public int remove(DBMatcher matcher) {
		if (isDropped) {
			throw new IllegalStateException("table " + this.name
					+ " has been dropped");
		}
		if (dirtyFields) {
			flush();
		}

		StringBuffer sb = new StringBuffer();
		sb.append("DELETE FROM `").append(name).append('`');
		addWhereClausePrefix(sb, matcher);

		String sqlQuery = sb.toString();
		try {
			PreparedStatement pstmt = database.getConnection()
					.prepareStatement(sqlQuery);
			addWhereClausePostfix(pstmt, 0, matcher);
			int result = pstmt.executeUpdate();
			pstmt.close();
			return result;
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not remove " + matcher + ": "
					+ sqlQuery, e);
			database.handleError(e);
			throw (IllegalArgumentException) new IllegalArgumentException(
					"could not remove data").initCause(e);
		}
	}

	public DBResult select() {
		if (dirtyFields) {
			flush();
		}

		// SELECT * FROM `pekka` LIMIT 0, 30
		try {
			Statement stm = database.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT * FROM `" + name + '`');
			return new SQLDBResult(this, stm, rs);

		} catch (Exception e) {
			log.log(Level.SEVERE, "could not select " + this.name, e);
			database.handleError(e);
		}

		return new EmptyDBResult();
	}

	public DBResult select(DBMatcher matcher) {
		if (matcher == null) {
			return select();
		}

		if (dirtyFields) {
			flush();
		}

		StringBuffer sb = new StringBuffer().append("SELECT * FROM `").append(
				name).append('`');
		addWhereClausePrefix(sb, matcher);
		try {
			PreparedStatement pstmt = database.getConnection()
					.prepareStatement(sb.toString());
			addWhereClausePostfix(pstmt, 0, matcher);
			ResultSet rs = pstmt.executeQuery();
			return new SQLDBResult(this, pstmt, rs);

		} catch (Exception e) {
			log.log(Level.SEVERE, "could not select " + this.name, e);
			database.handleError(e);
		}

		return new EmptyDBResult();
	}

	public void flush() {

		// CREATE TABLE `pekka` (
		// `id` INT NOT NULL,
		// `name` VARCHAR(80) NOT NULL,
		// `test` TIMESTAMP NOT NULL,
		// `parent` INT DEFAULT '-1' NOT NULL,
		// PRIMARY KEY (`id`),
		// INDEX (`id`),
		// UNIQUE (`id`)
		// );

		if (dirtyFields) {
			String prefix = "ALTER TABLE `" + this.name + "` ";
			StringBuffer sb = new StringBuffer();
			if (dirtyStartField == 0) {
				// No previous fields => create table
				sb.append("CREATE TABLE `").append(this.name).append("` (");
				for (int i = dirtyStartField; i < fieldNumber; i++) {
					if (i > dirtyStartField) {
						sb.append(", ");
					}
					fields[i].addBasicType(sb);
				}
				for (int i = dirtyStartField; i < fieldNumber; i++) {
					fields[i].addExtraTypeInfo(sb);
				}
				sb.append(')');
			} else {
				sb.append(prefix).append(" ADD ");
				for (int i = dirtyStartField; i < fieldNumber; i++) {
					if (i > dirtyStartField) {
						sb.append(", ADD ");
					}
					fields[i].addBasicType(sb);
				}
				for (int i = dirtyStartField; i < fieldNumber; i++) {
					fields[i].addExtraTypeInfoChange(sb);
				}
			}

			String sql = sb.toString();
			try {
				Statement stm = database.getConnection().createStatement();
				stm.execute(sql);
				stm.close();
				dirtyStartField = -1;
				dirtyFields = false;
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not alter table fields in "
						+ this.name + ": \"" + sql + '"', e);
				database.handleError(e);
			}
		}
	}

	// -------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------

	private void validate(DBObject object) {
		for (int i = 0, n = object.getFieldCount(); i < n; i++) {
			if (DBField.indexOf(fields, 0, fieldNumber, object.getFieldName(i)) < 0) {
				throw new IllegalArgumentException("unknown field '"
						+ object.getFieldName(i) + '\'');
			}
		}
	}

	private void addWhereClausePrefix(StringBuffer sb, DBMatcher matcher) {
		if (matcher.getFieldCount() > 0) {
			sb.append(" WHERE ");
			for (int i = 0, n = matcher.getFieldCount(); i < n; i++) {
				String fieldName = matcher.getFieldName(i);
				if (i > 0)
					sb.append(" AND ");
				sb.append('`').append(fieldName).append("`=?");
			}
		}

		// MLB - 20080411 - This code doesn't work on SQLite, as it apparently
		// doesn't handle LIMIT or OFFSET in INSERT commands. I commented it out
		// and it still worked with MySql, so apparently this isn't needed for
		// very important stuff. It may be used in the competition mode, which
		// is
		// why I made it still happen if we aren't using SQLite.
		if (!database.isSQLite()) {
			int skip = matcher.getSkip();

			int limit = matcher.getLimit();
			if (limit > 0) {
				sb.append(" LIMIT ");
				if (skip > 0) {
					sb.append(skip).append(',');
				}
				sb.append(limit);
			} else if (skip > 0) {
				sb.append(" OFFSET ").append(skip);
			}
		}
	}

	private void addWhereClausePostfix(PreparedStatement pstmt, int index,
			DBMatcher matcher) throws SQLException {
		if (matcher.getFieldCount() > 0) {
			for (int i = 0, n = matcher.getFieldCount(); i < n; i++) {
				String fieldName = matcher.getFieldName(i);
				Object value = matcher.getObject(fieldName);
				pstmt.setObject(index + i + 1, value);
			}
		}
	}

	// private void appendWhereClause(StringBuffer sb, DBMatcher matcher) {
	// if (matcher.getFieldCount() > 0) {
	// sb.append(" WHERE ");
	// for (int i = 0, n = matcher.getFieldCount(); i < n; i++) {
	// String fieldName = matcher.getFieldName(i);
	// String value = matcher.getString(fieldName);
	// if (i > 0) sb.append(',');
	// sb.append('`').append(fieldName).append("`='");
	// if (value != null) {
	// sb.append(value);
	// }
	// sb.append('\'');
	// }
	// }
	// int skip = matcher.getSkip();
	// int limit = matcher.getLimit();
	// if (limit > 0) {
	// sb.append(" LIMIT ");
	// if (skip > 0) {
	// sb.append(skip).append(',');
	// }
	// sb.append(limit);
	// } else if (skip > 0) {
	// sb.append(" OFFSET ").append(skip);
	// }
	// }

	private void executeStatement(String sql) {
		try {
			Statement stm = database.getConnection().createStatement();
			stm.execute(sql);
			stm.close();
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not execute \"" + sql + '"', e);
			database.handleError(e);
		}
	}

	// converts from standard Java.sql.Types to the custom type in DBField
	private int getDBType(int sqlType) {
		switch (sqlType) {
		case Types.BIT:
		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
		case Types.BIGINT:
			return DBField.INTEGER;
		case Types.FLOAT:
		case Types.REAL:
		case Types.DOUBLE:
			return DBField.DOUBLE;
		case Types.NUMERIC:
			return -1;
		case Types.DECIMAL:
			return -1;
		case Types.CHAR:
			return DBField.BYTE;
		case Types.VARCHAR:
			return DBField.STRING;
		case Types.LONGVARCHAR:
			return -1;
		case Types.DATE:
		case Types.TIME:
		case Types.TIMESTAMP:
			return DBField.TIMESTAMP;
		case Types.BINARY:
			return -1;
		case Types.VARBINARY:
			return -1;
		case Types.LONGVARBINARY:
			return -1;
		case Types.NULL:
			return -1;
		case Types.OTHER:
			return -1;
		case Types.JAVA_OBJECT:
			return -1;
		case Types.DISTINCT:
			return -1;
		case Types.STRUCT:
			return -1;
		case Types.ARRAY:
			return -1;
		case Types.BLOB:
			return -1;
		case Types.CLOB:
			return -1;
		case Types.REF:
			return -1;
		case Types.DATALINK:
			return -1;
		case Types.BOOLEAN:
			return DBField.BYTE;
		default:
			return -1;
		}
	}

} // SQLDBTable
