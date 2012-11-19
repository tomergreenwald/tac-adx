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
 * FileDBTable
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Oct 09 12:56:26 2002
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.db.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.botbox.util.ArrayUtils;
import se.sics.isl.db.DBMatcher;
import se.sics.isl.db.DBObject;
import se.sics.isl.db.DBResult;
import se.sics.isl.db.DBTable;
import se.sics.isl.db.DBField;

public class FileDBTable extends DBTable {

	private final static String BAK_EXT = ".~1~";

	private final static Logger log = Logger.getLogger(FileDBTable.class
			.getName());

	protected final FileDatabase database;
	private final String fileFields;
	private final String fileObjects;

	private FileDBField[] fields;
	private int fieldNumber = 0;
	private int objectNumber = 0;
	private boolean dirtyFields = false;
	private boolean dirtyObjects = false;

	private boolean isDropped = false;
	private boolean objectsLoaded = false;
	private boolean exists = false;

	private int changeCount = 0;

	protected FileDBTable(FileDatabase database, String name, boolean create) {
		super(name);
		this.database = database;
		File root = database.getDatabaseRoot();
		fileFields = new File(root, name + ".db").getAbsolutePath();
		fileObjects = new File(root, name + ".dat").getAbsolutePath();

		if (!create) {
			// Load table description
			loadFields();
		} else {
			objectsLoaded = true;
		}
	}

	boolean exists() {
		return exists;
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
		if (DBField.indexOf(fields, 0, fieldNumber, name) >= 0) {
			throw new IllegalArgumentException("field already exists");
		}

		database.validateName(name);

		if (!objectsLoaded) {
			loadObjects();
		}

		FileDBField field = newField(name, type, size, flags, defaultValue);

		if (objectNumber > 0) {
			// Reserve space for all existing rows for easier information
			// handling
			field.ensureCapacity(objectNumber);
		}

		if (fields == null) {
			fields = new FileDBField[5];
		} else if (fields.length == fieldNumber) {
			fields = (FileDBField[]) ArrayUtils
					.setSize(fields, fieldNumber + 5);
		}
		fields[fieldNumber++] = field;
		dirtyFields = true;
		changeCount++;
		return field;
	}

	private FileDBField newField(String name, int type, int size, int flags,
			Object defaultValue) {
		switch (type) {
		case DBField.INTEGER:
			return new IntField(this, name, type, size, flags, defaultValue);
		case DBField.LONG:
		case DBField.TIMESTAMP:
			return new LongField(this, name, type, size, flags, defaultValue);
		case DBField.DOUBLE:
			return new DoubleField(this, name, type, size, flags, defaultValue);
		case DBField.STRING:
		case DBField.BYTE:
			return new ObjectField(this, name, type, size, flags, defaultValue);
		default:
			throw new IllegalArgumentException("unknown type " + type);
		}
	}

	public void drop() {
		if (dropTable()) {
			log.finest(name + ": table dropped");
			database.tableDropped(this);

			File[] files = database.getDatabaseRoot().listFiles(
					new NameFilter(this.name));
			for (int i = 0, n = files.length; i < n; i++) {
				files[i].delete();
			}
		}
	}

	protected boolean dropTable() {
		if (isDropped) {
			return false;
		}
		isDropped = true;

		objectNumber = 0;
		fieldNumber = 0;
		exists = dirtyFields = dirtyObjects = false;
		fields = null;
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
		if (!objectsLoaded) {
			loadObjects();
		}
		return objectNumber;
	}

	public void insert(DBObject object) throws NumberFormatException {
		if (isDropped) {
			throw new IllegalStateException("table " + this.name
					+ " has been dropped");
		}
		validate(object);

		if (!objectsLoaded) {
			loadObjects();
		}

		for (int i = 0, n = fieldNumber; i < n; i++) {
			FileDBField field = fields[i];
			field.prepareSet(objectNumber, object.getObject(field.getName()));
		}
		// Now we know that all values have been accepted and we can set them
		for (int i = 0, n = fieldNumber; i < n; i++) {
			fields[i].set();
		}
		objectNumber++;
		dirtyObjects = true;
		changeCount++;
	}

	public int update(DBMatcher matcher, DBObject object) {
		if (isDropped) {
			throw new IllegalStateException("table " + this.name
					+ " has been dropped");
		}
		validate(object);

		if (!objectsLoaded) {
			loadObjects();
		}

		FileDBResult result = (FileDBResult) select(matcher);
		int objectsChanged = 0;
		while (result.next()) {
			int lastIndex = result.getLastIndex();
			for (int i = 0, n = fieldNumber; i < n; i++) {
				FileDBField field = fields[i];
				Object v = object.getObject(field.getName());
				if (v != null) {
					field.prepareSet(lastIndex, v);
				}
			}
			// Now we know that all values have been accepted and we can set
			// them
			for (int i = 0, n = fieldNumber; i < n; i++) {
				fields[i].set();
			}
			dirtyObjects = true;
			changeCount++;
			objectsChanged++;
			// This was a permitted change
			result.setChangeID(changeCount);
		}

		return objectsChanged;
	}

	// protected boolean update(Object key, DBObject object) {
	// validate(object);

	// if (!objectsLoaded) {
	// loadObjects();
	// }
	// return false;
	// }

	public int remove(DBMatcher matcher) {
		if (isDropped) {
			throw new IllegalStateException("table " + this.name
					+ " has been dropped");
		}
		if (!objectsLoaded) {
			loadObjects();
		}

		FileDBResult result = (FileDBResult) select(matcher);
		int objectsRemoved = 0;
		// Should be optimized to remove continuous intervals in one go
		while (result.next()) {
			int lastIndex = result.getLastIndex();

			// This MUST NOT fail because then the database is out of order!!!
			if (lastIndex + 1 < objectNumber) {
				for (int i = 0, n = fieldNumber; i < n; i++) {
					fields[i].remove(lastIndex);
				}
				result.setLastIndex(lastIndex - 1);
			}
			dirtyObjects = true;
			changeCount++;
			objectNumber--;
			objectsRemoved++;
			// This was a permitted change
			result.setChangeID(changeCount);
		}

		return objectsRemoved;
	}

	// protected boolean remove(Object key, DBObject object) {
	// if (!objectsLoaded) {
	// loadObjects();
	// }
	// return false;
	// }

	public DBResult select() {
		if (!objectsLoaded) {
			loadObjects();
		}
		return new FileDBResult(null, this, null, null);
	}

	public DBResult select(DBMatcher matcher) {
		if (matcher == null) {
			return select();
		}

		if (!objectsLoaded) {
			loadObjects();
		}

		// "Compile" the select fields
		int matchNumber = matcher.getFieldCount();
		int[] fieldIndex;
		Object[] matchValues;
		if (matchNumber == 0) {
			fieldIndex = null;
			matchValues = null;
		} else {
			fieldIndex = new int[matchNumber];
			matchValues = new Object[matchNumber];
			for (int i = 0; i < matchNumber; i++) {
				String fname = matcher.getFieldName(i);
				if ((fieldIndex[i] = DBField.indexOf(fields, 0, fieldNumber,
						fname)) < 0) {
					throw new IllegalArgumentException("unknown field '"
							+ fname + '\'');
				}
				matchValues[i] = matcher.getObject(fname);
			}
		}
		return new FileDBResult(matcher, this, fieldIndex, matchValues);
	}

	public void flush() {
		if (dirtyFields) {
			saveFields();
			// Must resave all objects if some fields have been changed
			if (objectNumber > 0) {
				saveObjects();
			}
		}
		if (dirtyObjects) {
			saveObjects();
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

	// -------------------------------------------------------------------
	// Interface towards FileDBResult
	// -------------------------------------------------------------------

	int getChangeCount() {
		return changeCount;
	}

	FileDBField getField(String name) {
		int index = DBField.indexOf(fields, 0, fieldNumber, name);
		if (index >= 0) {
			return fields[index];
		}
		throw new IllegalArgumentException("unknown field '" + name + '\'');
	}

	int next(int[] fieldIndex, Object[] matchValues, int lastIndex) {
		if (lastIndex < 0) {
			lastIndex = 0;
		} else {
			lastIndex++;
		}
		if (lastIndex >= objectNumber) {
			return objectNumber;
		}

		if (fieldIndex == null) {
			return lastIndex;
		} else if (fieldNumber == 0) {
			return objectNumber;
		} else {
			int startIndex, endIndex = lastIndex;
			int matchNumber = fieldIndex.length;
			do {
				endIndex = startIndex = fields[fieldIndex[0]].indexOf(
						matchValues[0], endIndex, objectNumber);
				if (endIndex < 0) {
					return objectNumber;
				}
				for (int i = 1; i < matchNumber; i++) {
					endIndex = fields[fieldIndex[i]].indexOf(matchValues[i],
							endIndex, objectNumber);
					if (endIndex < 0) {
						return objectNumber;
					}
				}
			} while (endIndex > startIndex);
			return endIndex;
		}
	}

	// -------------------------------------------------------------------
	// IO handling
	// -------------------------------------------------------------------

	protected void loadFields() {
		loadState(fileFields, true);
	}

	protected void saveFields() {
		saveState(fileFields);
	}

	protected void loadObjects() {
		objectsLoaded = true;
		loadState(fileObjects, true);
	}

	protected void saveObjects() {
		saveState(fileObjects);
	}

	private void loadState(String name, boolean revert) {
		try {
			InputStream in = getInputStream(name);
			loadState(name, in);
			exists = true;
		} catch (FileNotFoundException e) {
			// No saved state
		} catch (Exception e) {
			log.log(Level.SEVERE, this.name + ": could not load data from "
					+ name, e);
			exists = true;
			if (revert && revertFile(name)) {
				loadState(name, false);
			}
		}
	}

	private void loadState(String name, InputStream in)
			throws ClassNotFoundException, IOException {
		ObjectInputStream oin = null;
		try {
			oin = new ObjectInputStream(in);
			if (name == fileFields) {
				int number = oin.readInt();
				FileDBField[] fields = new FileDBField[number];
				for (int i = 0; i < number; i++) {
					int type = oin.readInt();
					int size = oin.readInt();
					int flags = oin.readInt();
					String fieldName = (String) oin.readObject();
					Object defaultValue = oin.readObject();
					fields[i] = newField(fieldName, type, size, flags,
							defaultValue);
				}
				this.fields = fields;
				this.fieldNumber = number;

			} else { // name == fileObjects
				int number = oin.readInt();
				for (int i = 0; i < fieldNumber; i++) {
					fields[i].loadState(oin, number);
				}
				objectNumber = number;
			}
		} finally {
			if (oin != null) {
				oin.close();
			} else {
				in.close();
			}
		}
	}

	private void saveState(String name) {
		OutputStream out = null;
		ObjectOutputStream oout = null;
		try {
			out = getOutputStream(name);
			oout = new ObjectOutputStream(out);
			if (name == fileObjects) {
				dirtyObjects = false;
				oout.writeInt(objectNumber);
				for (int i = 0; i < fieldNumber; i++) {
					fields[i].saveState(oout);
				}
			} else { // fileFields
				dirtyFields = false;
				oout.writeInt(fieldNumber);
				for (int i = 0; i < fieldNumber; i++) {
					FileDBField field = fields[i];
					Object defaultValue = field.getDefaultValue();
					oout.writeInt(field.getType());
					oout.writeInt(field.getSize());
					oout.writeInt(field.getFlags());
					oout.writeObject(field.getName());
					oout.writeObject(defaultValue == null ? null : defaultValue
							.toString());
				}
				exists = true;
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, this.name + ": could not save data to "
					+ name, e);
			if (name == fileObjects) {
				dirtyObjects = true;
			} else {
				dirtyFields = true;
			}
		} finally {
			try {
				if (oout != null) {
					oout.close();
				} else if (out != null) {
					out.close();
				}
			} catch (IOException e) {
			}
		}
	}

	private InputStream getInputStream(String filename) throws IOException {
		try {
			return new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			String bakName = filename + BAK_EXT;
			if (new File(bakName).renameTo(new File(filename))) {
				return new FileInputStream(filename);
			}
			throw e;
		}
	}

	private OutputStream getOutputStream(String filename) throws IOException {
		String bakName = filename + BAK_EXT;
		File fp = new File(filename);
		if (fp.exists()) {
			File bakFp = new File(bakName);
			bakFp.delete();
			fp.renameTo(bakFp);
		}
		return new FileOutputStream(filename);
	}

	private boolean revertFile(String name) {
		File bakFp = new File(name + BAK_EXT);
		File fp = new File(name);
		if (fp.exists() && !fp.delete()) {
			log.severe(this.name + ": could not remove old file " + fp
					+ " when reverting (will retry)" + bakFp);
			// Remove the file we began saving because the save failed
			// but wait a short while because otherwise it might not
			// be possible to remove the newly created file.
			Runtime.getRuntime().gc();
			// Wait a small time because sometime just edited files
			// can not be removed
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
			}
			Runtime.getRuntime().gc();
			if (!fp.delete()) {
				log.severe(this.name + ": could not remove old file " + fp
						+ " when reverting " + bakFp);

				int nr = 1;
				File rFp = new File(name + ".~" + (++nr) + '~');
				// Loop until we find an empty remove file that we could
				// use to move the old broken file to (but NOT forever!)
				// We could of course use Java's unique temporary file creation
				// and then use a garbage collect scheme to erase the files.
				// \TODO
				while (rFp.exists() && !rFp.delete() && (++nr < 10)) {
					log.severe(this.name
							+ ": could not remove old removed file " + rFp
							+ " when reverting attribute");
					rFp = new File(name + ".~" + nr + '~');
				}
				if (!fp.renameTo(rFp)) {
					log.severe(this.name + ": could not rename old file " + fp
							+ " to " + rFp + " when reverting");
				}
			}
		}
		return bakFp.renameTo(fp);
	}

} // FileDBTable
