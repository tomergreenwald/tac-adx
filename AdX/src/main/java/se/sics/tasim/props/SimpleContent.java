/**
 * TAC Supply Chain Management Simulator
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
 * SimpleContent
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Oct 11 14:14:52 2002
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.props;

import java.text.ParseException;
import java.util.logging.Logger;

import com.botbox.util.ArrayUtils;
import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.isl.transport.Transportable;

/**
 * <code>SimpleContent</code> is an abstract base class for parameterized
 * messages. It provides functionality for setting and getting named attributes.
 * <p>
 * 
 * <p>
 * <b>Warning:</b> serialized objects of this class might not be compatible with
 * future versions. Only use serialization of this class for temporary storage
 * or RMI using the same version of the class.
 */
public abstract class SimpleContent implements Transportable,
		java.io.Serializable {

	private static final long serialVersionUID = 3025394464668122965L;

	private static final Logger log = Logger.getLogger(SimpleContent.class
			.getName());

	private static final int NAME = 0;
	private static final int VALUE = 1;
	private static final int PARTS = 2;

	private Object[] attributePairs;
	private int attributeCount;
	private boolean isLocked = false;

	protected SimpleContent() {
	}

	public String getAttribute(String name) {
		return getAttribute(name, null);
	}

	public String getAttribute(String name, String defaultValue) {
		Object value = get(name);
		return value == null ? defaultValue : value.toString();
	}

	public int getAttributeAsInt(String name, int defaultValue) {
		Object value = get(name);
		if (value != null) {
			if (value instanceof Integer) {
				return ((Integer) value).intValue();
			}
			try {
				if (value instanceof Long) {
					return (int) ((Long) value).longValue();
				}
				return Integer.parseInt(value.toString());
			} catch (Exception e) {
				log.warning("attribute '" + name
						+ "' has a non-integer value '" + value + '\'');
			}
		}
		return defaultValue;
	}

	public long getAttributeAsLong(String name, long defaultValue) {
		Object value = get(name);
		if (value != null) {
			if (value instanceof Long) {
				return ((Long) value).longValue();
			}
			if (value instanceof Integer) {
				return ((Integer) value).intValue();
			}
			try {
				return Long.parseLong(value.toString());
			} catch (Exception e) {
				log.warning("attribute '" + name + "' has a non-long value '"
						+ value + '\'');
			}
		}
		return defaultValue;
	}

	public float getAttributeAsFloat(String name, float defaultValue) {
		Object value = get(name);
		if (value != null) {
			if (value instanceof Float) {
				return ((Float) value).floatValue();
			}
			try {
				if (value instanceof Integer) {
					return ((Integer) value).intValue();
				}
				return Float.parseFloat(value.toString());
			} catch (Exception e) {
				log.warning("attribute '" + name + "' has a non-float value '"
						+ value + '\'');
			}
		}
		return defaultValue;
	}

	private Object get(String name) {
		int index = ArrayUtils.keyValuesIndexOf(attributePairs, PARTS, 0,
				attributeCount * PARTS, name);
		return (index >= 0) ? attributePairs[index + VALUE] : null;
	}

	public void setAttribute(String name, String value) {
		if (value == null) {
			removeAttribute(name);
		} else {
			set(name, value);
		}
	}

	public void setAttribute(String name, int value) {
		set(name, new Integer(value));
	}

	public void setAttribute(String name, long value) {
		set(name, new Long(value));
	}

	public void setAttribute(String name, float value) {
		set(name, new Float(value));
	}

	private void set(String name, Object value) {
		if (isLocked) {
			throw new IllegalStateException("locked");
		}
		int index = ArrayUtils.keyValuesIndexOf(attributePairs, PARTS, 0,
				attributeCount * PARTS, name);
		if (index >= 0) {
			attributePairs[index + VALUE] = value;
		} else {
			index = attributeCount * PARTS;
			if (attributePairs == null) {
				attributePairs = new Object[4 * PARTS];
			} else if (index == attributePairs.length) {
				attributePairs = ArrayUtils.setSize(attributePairs, index + 10
						* PARTS);
			}
			attributePairs[index + NAME] = name;
			attributePairs[index + VALUE] = value;
			attributeCount++;
		}
	}

	public void removeAttribute(String name) {
		if (isLocked) {
			throw new IllegalStateException("locked");
		}
		int index = ArrayUtils.keyValuesIndexOf(attributePairs, PARTS, 0,
				attributeCount * PARTS, name);
		if (index >= 0) {
			attributeCount--;

			int lastIndex = attributeCount * PARTS;
			attributePairs[index + NAME] = attributePairs[lastIndex + NAME];
			attributePairs[index + VALUE] = attributePairs[lastIndex + VALUE];
			attributePairs[lastIndex + NAME] = null;
			attributePairs[lastIndex + VALUE] = null;
		}
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void lock() {
		isLocked = true;
	}

	protected StringBuffer params(StringBuffer buf) {
		buf.append('[');
		if (attributeCount > 0) {
			buf.append(attributePairs[NAME]).append('=').append(
					attributePairs[VALUE]);
			for (int i = PARTS, n = attributeCount * PARTS; i < n; i += PARTS) {
				buf.append(',').append(attributePairs[i + NAME]).append('=')
						.append(attributePairs[i + VALUE]);
			}
		}
		return buf.append(']');
	}

	// -------------------------------------------------------------------
	// Transportable (externalization support)
	// -------------------------------------------------------------------

	public void read(TransportReader reader) throws ParseException {
		if (isLocked) {
			throw new IllegalStateException("locked");
		}
		boolean lock = reader.getAttributeAsInt("lock", 0) > 0;
		if (reader.nextNode("params", false)) {
			for (int i = 0, n = reader.getAttributeCount(); i < n; i++) {
				String name = reader.getAttributeName(i);
				setAttribute(name, reader.getAttribute(i));
			}
		}
		isLocked = lock;
	}

	public void write(TransportWriter writer) {
		if (isLocked) {
			writer.attr("lock", 1);
		}

		if (attributeCount > 0) {
			writer.node("params");
			for (int i = 0, n = attributeCount * PARTS; i < n; i += PARTS) {
				String name = attributePairs[i + NAME].toString();
				Object value = attributePairs[i + VALUE];
				if (value instanceof Integer) {
					writer.attr(name, ((Integer) value).intValue());
				} else if (value instanceof Long) {
					writer.attr(name, ((Long) value).longValue());
				} else if (value instanceof Float) {
					writer.attr(name, ((Float) value).floatValue());
				} else {
					writer.attr(name, value.toString());
				}
			}
			writer.endNode("params");
		}
	}

	// -------------------------------------------------------------------
	// Serialization
	// -------------------------------------------------------------------

	// private void readObject(java.io.ObjectInputStream oin)
	// throws java.io.IOException, ClassNotFoundException
	// {
	// // Read all fields
	// oin.defaultReadObject();

	// attributeCount = oin.readInt();
	// if (attributeCount > 0) {
	// attributePairs = new Object[attributeCount * PARTS];
	// for (int i = 0, n = attributeCount * PARTS; i < n; i++) {
	// attributePairs[i] = oin.readObject();
	// }
	// }
	// }

	// private void writeObject(java.io.ObjectOutputStream oout)
	// throws java.io.IOException
	// {
	// oout.defaultWriteObject();
	// // Write a serialization version
	// oout.writeInt(attributeCount);
	// if (attributeCount > 0) {
	// for (int i = 0, n = attributeCount; i < n; i++) {
	// oout.writeObject(attributePairs[i]);
	// }
	// }
	// }

} // SimpleContent
