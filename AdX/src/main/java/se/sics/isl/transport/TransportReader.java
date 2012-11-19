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
 * TransportReader
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Dec 17 16:52:36 2002
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 * Purpose :
 *
 * MODIFIED BY: Lee Callender
 * Date: 10/29/08
 *
 */
package se.sics.isl.transport;

import java.text.ParseException;

public abstract class TransportReader {

	private Context currentContext;

	protected TransportReader() {
	}

	public void setContext(Context context) {
		currentContext = context;
	}

	public abstract void reset();

	protected abstract int getPosition();

	public Transportable readTransportable() throws ParseException {
		String name = getNodeName();
		String className = getTransportableClass(name);
		Transportable object = createTransportable(className);
		readTransportable(object);
		return object;
	}

	protected String getTransportableClass(String nodeName)
			throws ParseException {
		Context context = this.currentContext;
		if (context == null) {
			throw new ParseException("no context for node " + nodeName,
					getPosition());
		}

		String className = context.lookupClass(nodeName);
		if (className != null) {
			return className;
		}
		throw new ParseException("no node named " + nodeName + " in context "
				+ context.getName(), getPosition());
	}

	protected Transportable createTransportable(String className)
			throws ParseException {
		try {
			return (Transportable) Class.forName(className).newInstance();
		} catch (Exception e) {
			throw (ParseException) new ParseException(
					"could not create transportable of type '" + className
							+ '\'', getPosition()).initCause(e);
		}
	}

	protected void readTransportable(Transportable object)
			throws ParseException {
		enterNode();
		object.read(this);
		exitNode();
	}

	public abstract boolean hasMoreNodes() throws ParseException;

	public abstract boolean nextNode(boolean isRequired) throws ParseException;

	public abstract boolean nextNode(String name, boolean isRequired)
			throws ParseException;

	public abstract String getNodeName() throws ParseException;

	public abstract boolean isNode() throws ParseException;

	public abstract boolean isNode(String name) throws ParseException;

	public abstract boolean enterNode() throws ParseException;

	public abstract boolean exitNode() throws ParseException;

	public abstract int getAttributeCount();

	public abstract String getAttributeName(int index) throws ParseException;

	public String getAttribute(int index) throws ParseException {
		return getAttribute(getAttributeName(index), null);
	}

	public String getAttribute(String name) throws ParseException {
		return getAttribute(name, null, true);
	}

	public String getAttribute(String name, String defaultValue)
			throws ParseException {
		return getAttribute(name, defaultValue, false);
	}

	protected abstract String getAttribute(String name, String defaultValue,
			boolean isRequired) throws ParseException;

	public int getAttributeAsInt(String name) throws ParseException {
		return getAttributeAsInt(name, 0, true);
	}

	public int getAttributeAsInt(String name, int defaultValue)
			throws ParseException {
		return getAttributeAsInt(name, defaultValue, false);
	}

	protected int getAttributeAsInt(String name, int defaultValue,
			boolean isRequired) throws ParseException {
		String value = getAttribute(name, null, isRequired);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				// Ignore because default will be returned
			}
		}
		return defaultValue;
	}

	public long getAttributeAsLong(String name) throws ParseException {
		return getAttributeAsLong(name, 0L, true);
	}

	public long getAttributeAsLong(String name, long defaultValue)
			throws ParseException {
		return getAttributeAsLong(name, defaultValue, false);
	}

	protected long getAttributeAsLong(String name, long defaultValue,
			boolean isRequired) throws ParseException {
		String value = getAttribute(name, null, isRequired);
		if (value != null) {
			try {
				return Long.parseLong(value);
			} catch (Exception e) {
				// Ignore because default will be returned
			}
		}
		return defaultValue;
	}

	public float getAttributeAsFloat(String name) throws ParseException {
		return getAttributeAsFloat(name, 0f, true);
	}

	public float getAttributeAsFloat(String name, float defaultValue)
			throws ParseException {
		return getAttributeAsFloat(name, defaultValue, false);
	}

	protected float getAttributeAsFloat(String name, float defaultValue,
			boolean isRequired) throws ParseException {
		String value = getAttribute(name, null, isRequired);
		if (value != null) {
			try {
				return Float.parseFloat(value);
			} catch (Exception e) {
				// Ignore because default will be returned
			}
		}
		return defaultValue;
	}

	// Added double parsing - Lee Callender
	public double getAttributeAsDouble(String name) throws ParseException {
		return getAttributeAsDouble(name, 0.0d, true);
	}

	public double getAttributeAsDouble(String name, double defaultValue)
			throws ParseException {
		return getAttributeAsDouble(name, defaultValue, false);
	}

	public double getAttributeAsDouble(String name, double defaultValue,
			boolean isRequired) throws ParseException {
		String value = getAttribute(name, null, isRequired);
		if (value != null) {
			try {
				return Double.parseDouble(value);
			} catch (Exception e) {
				// Ignore because default will be returned
			}
		}
		return defaultValue;
	}

	public int[] getAttributeAsIntArray(String name) throws ParseException {
		return getAttributeAsIntArray(name, false);
	}

	protected int[] getAttributeAsIntArray(String name, boolean isRequired)
			throws ParseException {
		System.out
				.println("Conversion of String value to int[] not *yet* supported");
		return null;
	}

} // TransportReader
