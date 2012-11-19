/**
 * SICS ISL Java Utilities
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001-2005 SICS AB. All rights reserved.
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
 * BinaryTransportReader
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Jan 14 13:45:21 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.transport;

import java.io.PrintStream;
import java.text.ParseException;
import java.util.Hashtable;

import com.botbox.util.ArrayUtils;

public class BinaryTransportReader extends TransportReader implements
		BinaryTransport {

	private static final int DEF_SIZE = 10;

	private int currentPosition = -1;
	private int currentNode = -1;
	private int currentValue = -1;

	private byte[] messageData;
	private int dataOffset;
	private int dataLen;

	private int[] nodeStack = new int[DEF_SIZE];
	private int nodeLevel = 0;
	private boolean nodeEntered = false;

	private String[] aliases = new String[48];
	private Hashtable nameLookup = new Hashtable();

	public BinaryTransportReader() {
	}

	public void setMessage(byte[] messageData) {
		setMessage(messageData, 0, messageData.length);
	}

	public void setMessage(byte[] messageData, int offset, int length) {
		if ((offset | length | (messageData.length - offset - length)) < 0) {
			throw new IndexOutOfBoundsException();
		}
		this.messageData = messageData;
		this.dataOffset = offset;
		this.dataLen = offset + length;
		nodeLevel = 0;
		reset();
	}

	public void clear() {
		nodeLevel = 0;
		dataOffset = dataLen = 0;
		reset();
		messageData = null;
	}

	public void reset() {
		if (nodeLevel == 0) {
			currentPosition = dataOffset - 1;
			nodeEntered = false;
			currentNode = currentValue = -1;
		} else {
			currentPosition = nodeStack[nodeLevel - 1];
			currentNode = currentPosition;
			currentValue = getValuePosForNode(currentNode);
			nodeEntered = true; // Still in node...
		}
	}

	protected int getPosition() {
		return currentPosition;
	}

	private int getAlias(String alias) {
		Integer i = (Integer) nameLookup.get(alias);
		return i == null ? -1 : i.intValue();
	}

	private int addAliases(int pos) throws ParseException {
		while ((pos < dataLen) && ((messageData[pos] & 0xff) == ALIAS)) {
			pos = addAlias(pos);
		}
		return pos;
	}

	private int addAlias(int pos) throws ParseException {
		if (pos + 5 >= dataLen) {
			throw new ParseException("unexpected EOF", pos);
		}
		pos++;
		int id = ((messageData[pos] & 0xff) << 8)
				+ (messageData[pos + 1] & 0xff);
		pos += 2;
		int len = ((messageData[pos] & 0xff) << 8)
				+ (messageData[pos + 1] & 0xff);
		pos += 2;

		String alias = getSValue(pos, len);
		if (nameLookup.get(alias) == null) {
			nameLookup.put(alias, new Integer(id));
		}

		// System.out.println("Adding alias " + alias + " = " + id);

		if (aliases.length <= id) {
			aliases = (String[]) ArrayUtils.setSize(aliases, id + 32);
		}
		aliases[id] = alias;

		return pos + len;
	}

	public boolean hasMoreNodes() throws ParseException {
		int mark = currentPosition;
		int node = currentNode;
		int value = currentValue;
		boolean entered = nodeEntered;
		if (nextNode(false)) {
			currentPosition = mark;
			currentNode = node;
			currentValue = value;
			nodeEntered = entered;
			return true;
		}
		return false;
	}

	public boolean nextNode(boolean isRequired) throws ParseException {
		// go to next node
		if (skipToNextNode()) {
			// int attNo = messageData[currentPosition + 1] & 0xff;
			// int nameID = ((messageData[currentPosition + 2] & 0xff) << 8) +
			// (messageData[currentPosition + 3] & 0xff);
			// String name = getName(nameID);
			// System.out.println(">> Node: " + name + " attCount = " + attNo);
			return true;
		}

		if (isRequired) {
			throw new ParseException("no more nodes", currentPosition);
		}
		return false;
	}

	private boolean skipToNextNode() throws ParseException {
		if (currentPosition < dataOffset) {
			if (dataLen == dataOffset) {
				return false;
			}

			// Step into the "data" and add the aliases. Any beginning aliases
			// should never be read again and we change the data start pointer.
			dataOffset = addAliases(dataOffset);
			if (dataOffset >= dataLen) {
				// Already at the end of the message
				return false;
			}
			currentPosition = dataOffset;
			if ((messageData[currentPosition] & 0xff) == TABLE) {
				throw new ParseException("table without type", currentPosition);
			}
			currentNode = currentPosition;
			currentValue = getValuePosForNode(currentNode);
			return true;
		}

		// Ok we are at a node, that we should skip...
		int pos = currentPosition;
		int op = messageData[pos] & 0xff;

		// If we have entered an automatically-ended node there are no
		// subnodes...
		if (nodeEntered && (op == NODE || op == TABLE)) {
			return false;
		}

		// So skip it... and
		pos = skipNode(currentNode, currentValue);

		if (pos >= dataLen) {
			// No more nodes
			return false;

		} else if (op == NODE || op == TABLE) {
			// This node is ended automatically! next node will do!
			// - can only be at end or alias between...
			pos = addAliases(pos);
			if (pos < dataLen) {
				switch (messageData[pos] & 0xff) {
				case NODE:
				case START_NODE:
					currentPosition = pos;
					currentNode = currentPosition;
					currentValue = getValuePosForNode(currentNode);
					return true;
				case TABLE:
					currentPosition = pos;
					currentValue = pos + 1;
					return true;
				default:
					return false;
				}
			}
			// No more node found...
			return false;

		} else {
			// Here we need to end this node first (since it was a START_NODE)
			// and then find another node...

			// \TODO UNLESS we are in "enter node" mode, then we just need to
			// find
			// the next node at all...
			int level = 1;
			int levelTarget = nodeEntered ? 1 : 0;
			int lastNode = currentNode;
			while (pos < dataLen && level >= 0) {
				op = messageData[pos] & 0xff;
				switch (op) {
				case END_NODE:
					level--;
					if (nodeEntered && level == 0)
						return false;
					pos++;
					lastNode = -1;
					break;
				case ALIAS:
					pos = addAlias(pos);
					break;
				case NODE:
					if (level == levelTarget) {
						currentPosition = pos;
						currentNode = currentPosition;
						currentValue = getValuePosForNode(currentNode);
						nodeEntered = false;
						return true;
					} else {
						lastNode = pos;
						pos = skipNode(pos, getValuePosForNode(pos));
					}
					break;
				case TABLE:
					if (level == levelTarget) {
						currentPosition = pos;
						currentValue = pos + 1;
						nodeEntered = false;
						return true;
					} else {
						pos = skipNode(lastNode, pos + 1);
					}
					break;
				case START_NODE:
					if (level == levelTarget) {
						currentPosition = pos;
						currentNode = currentPosition;
						currentValue = getValuePosForNode(currentNode);
						nodeEntered = false;
						return true;
					} else {
						lastNode = pos;
						pos = skipNode(pos, getValuePosForNode(pos));
						level++;
					}
					break;
				default:
					throw new ParseException("unknown op '" + op + '\'', pos);
				}
			}
			return false;
		}
	}

	private int getValuePosForNode(int node) {
		return node + 4 + 3 * (messageData[node + 1] & 0xFF);
	}

	private int skipNode(int nodePos, int valPos) {
		if ((nodePos + 4) >= dataLen) {
			// \TODO No more complete nodes. Perhaps throw ParseException if not
			// complete node?
			return dataLen;
		}

		int attNo = messageData[nodePos + 1] & 0xff;
		// System.out.println("-- skipping node: at " + pos +
		// " atts = " + attNo);
		nodePos += 4;
		for (int i = 0; i < attNo; i++) {
			switch (messageData[nodePos] & 0xff) {
			case INT:
			case FLOAT:
				valPos += 4;
				break;
			case DOUBLE: // Modified by Lee Callender
			case LONG:
				valPos += 8;
				break;
			case STRING: {
				int slen = ((messageData[valPos] & 0xff) << 8)
						+ (messageData[valPos + 1] & 0xff);
				valPos += slen + 2;
				break;
			}
			case CONSTANT_STRING:
				valPos += 2;
				break;
			case INT_ARR: {
				int slen = ((messageData[valPos] & 0xff) << 8)
						+ (messageData[valPos + 1] & 0xff);
				valPos += slen * 4 + 2;
				break;
			}
			}
			nodePos += 3;
		}
		return valPos;
	}

	private int getIValue(int pos) {
		return ((messageData[pos] & 0xff) << 24)
				+ ((messageData[pos + 1] & 0xff) << 16)
				+ ((messageData[pos + 2] & 0xff) << 8)
				+ (messageData[pos + 3] & 0xff);
	}

	private long getLValue(int pos) {
		long v = ((messageData[pos] & 0xffL) << 56)
				+ ((messageData[pos + 1] & 0xffL) << 48)
				+ ((messageData[pos + 2] & 0xffL) << 40)
				+ ((messageData[pos + 3] & 0xffL) << 32)
				+ ((messageData[pos + 4] & 0xffL) << 24)
				+ ((messageData[pos + 5] & 0xffL) << 16)
				+ ((messageData[pos + 6] & 0xffL) << 8)
				+ (messageData[pos + 7] & 0xffL);
		return v;
	}

	private String getSValue(int pos, int length) throws ParseException {
		int end = pos + length;
		if (end > dataLen) {
			throw new ParseException("unexpected EOF", pos);
		}
		// Allocate char for max length
		char[] buf = new char[length];

		// This code has been "inspired" from Core JavaTM Technologies Tech
		// Tips, January 10, 2003 and java.io.Data{Input,Output}Stream.
		int index = 0;
		int c, char2, char3;
		while (pos < end) {
			c = messageData[pos] & 0xff;

			switch (c >> 4) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				/* 0xxxxxxx */
				pos++;
				buf[index++] = (char) c;
				break;
			case 12:
			case 13:
				/* 110x xxxx 10xx xxxx */
				pos += 2;
				if (pos > end) {
					throw new ParseException("malformed UTF-8", pos);
				}
				char2 = messageData[pos - 1] & 0xff;
				if ((char2 & 0xC0) != 0x80) {
					throw new ParseException("malformed UTF-8", pos - 2);
				}
				buf[index++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
				break;
			case 14:
				/* 1110 xxxx 10xx xxxx 10xx xxxx */
				pos += 3;
				if (pos > end)
					throw new ParseException("malformed UTF-8", pos);
				char2 = messageData[pos - 2] & 0xff;
				char3 = messageData[pos - 1] & 0xff;
				if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
					throw new ParseException("malformed UTF-8", pos - 3);
				}
				buf[index++] = (char) (((c & 0x0F) << 12)
						| ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
				break;
			default:
				/* 10xx xxxx, 1111 xxxx */
				throw new ParseException("malformed UTF-8", pos);
			}
		}

		return new String(buf, 0, index);
	}

	private String getCValue(int pos) throws ParseException {
		int id = ((messageData[pos] & 0xff) << 8)
				+ (messageData[pos + 1] & 0xff);
		return getName(id);
	}

	private String getName(int id) throws ParseException {
		if (id >= aliases.length || aliases[id] == null) {
			throw new ParseException("no alias for id " + id, currentPosition);
		}
		return aliases[id];
	}

	public boolean nextNode(String name, boolean isRequired)
			throws ParseException {
		int oldPos = currentPosition;
		int oldNode = currentNode;
		int oldValue = currentValue;
		boolean oldEntered = nodeEntered;
		while (nextNode(false)) {
			if (isNode(name)) {
				return true;
			}
		}
		// \TODO Ensure that everything is as it was.
		currentPosition = oldPos;
		currentNode = oldNode;
		currentValue = oldValue;
		nodeEntered = oldEntered;

		if (isRequired) {
			throw new ParseException("node '" + name + "' not found",
					currentPosition);
		}
		return false;
	}

	public String getNodeName() throws ParseException {
		if (currentNode < 0) {
			throw new ParseException("before first node", 0);
		}
		int nameID = ((messageData[currentNode + 2] & 0xff) << 8)
				+ (messageData[currentNode + 3] & 0xff);
		return getName(nameID);
	}

	public boolean isNode() throws ParseException {
		return currentNode >= 0;
	}

	public boolean isNode(String name) throws ParseException {
		if (currentNode < 0) {
			return false;
		}
		int nameID = ((messageData[currentNode + 2] & 0xff) << 8)
				+ (messageData[currentNode + 3] & 0xff);
		return nameID == getAlias(name);
	}

	// Check that we are at an un-entered node
	// and goes down into the specific node (still standing at the same pos)
	public boolean enterNode() throws ParseException {
		if (nodeEntered || currentNode < 0)
			return false;

		if (nodeLevel >= nodeStack.length) {
			int newSize = nodeStack.length + DEF_SIZE;
			nodeStack = ArrayUtils.setSize(nodeStack, newSize);
		}
		nodeStack[nodeLevel++] = currentPosition;
		nodeEntered = true;
		// System.out.println("Entered node: level = " + nodeLevel +
		// " pos = " + currentPosition);
		return true;
	}

	public boolean exitNode() throws ParseException {
		if (nodeLevel > 0) {
			nodeLevel--;
			currentPosition = nodeStack[nodeLevel];
			if ((messageData[currentPosition] & 0xFF) == TABLE) {
				currentValue = currentPosition + 1;
			} else {
				currentNode = currentPosition;
				currentValue = getValuePosForNode(currentNode);
			}
			nodeEntered = false;
			// System.out.println("Exited node: level = " + nodeLevel +
			// " pos = " + currentPosition);
			return true;
		} else {
			return false;
		}
	}

	public int getAttributeCount() {
		if (currentNode >= 0) {
			return messageData[currentNode + 1] & 0xff;
		}
		return 0;
	}

	public String getAttributeName(int index) throws ParseException {
		int count = getAttributeCount();
		if (index >= count || index < 0) {
			throw new IndexOutOfBoundsException("Index: " + index + " Size: "
					+ count);
		}

		int pos = currentNode + 5 + index * 3;
		int nid = ((messageData[pos] & 0xff) << 8)
				+ (messageData[pos + 1] & 0xff);
		return getName(nid);
	}

	private Object getAttributeAsObject(int id) throws ParseException {
		if (currentNode < 0) {
			return null;
		}

		int attNo = messageData[currentNode + 1] & 0xff;
		int pos = currentNode + 4;
		int valPos = currentValue;
		for (int i = 0; i < attNo; i++) {
			int nid = ((messageData[pos + 1] & 0xff) << 8)
					+ (messageData[pos + 2] & 0xff);
			switch (messageData[pos] & 0xff) {
			case INT:
				if (nid == id) {
					return Integer.toString(getIValue(valPos));
				}
				valPos += 4;
				break;
			case LONG:
				if (nid == id) {
					return Long.toString(getLValue(valPos));
				}
				valPos += 8;
				break;
			case FLOAT:
				if (nid == id) {
					return Float.toString(Float
							.intBitsToFloat(getIValue(valPos)));
				}
				valPos += 4;
				break;
			case DOUBLE: // Modified by Lee Callender
				if (nid == id) {
					return Double.toString(Double
							.longBitsToDouble(getLValue(valPos)));
				}
				valPos += 8;
				break;
			case STRING: {
				int slen = ((messageData[valPos] & 0xff) << 8)
						+ (messageData[valPos + 1] & 0xff);
				if (nid == id) {
					return getSValue(valPos + 2, slen);
				}
				valPos += slen + 2;
				break;
			}
			case CONSTANT_STRING:
				if (nid == id) {
					return getCValue(valPos);
				}
				valPos += 2;
				break;
			case INT_ARR: {
				int slen = ((messageData[valPos] & 0xff) << 8)
						+ (messageData[valPos + 1] & 0xff);
				// Only return if correct ID otherwise step on...
				if (nid == id) {
					return getAttributeAsIntArray(valPos + 2, slen);
				}
				valPos += slen * 4 + 2;
				break;
			}
			}
			pos += 3;
		}
		return null;
	}

	private String getAttributeAsString(int id) throws ParseException {
		Object aVal = getAttributeAsObject(id);
		if (aVal == null)
			return null;
		if (aVal instanceof int[]) {
			int[] tmp = (int[]) aVal;
			StringBuffer sb = new StringBuffer();
			sb.append('[');
			for (int i = 0, n = tmp.length; i < n; i++) {
				if (i > 0) {
					sb.append(',');
				}
				sb.append("" + tmp[i]);
			}
			sb.append(']');
			return sb.toString();
		} else if (aVal instanceof String) {
			return (String) aVal;
		} else {
			throw new ParseException("Illegal object type: " + aVal,
					currentPosition);
		}
	}

	protected String getAttribute(String name, String defaultValue,
			boolean isRequired) throws ParseException {
		Integer id = (Integer) nameLookup.get(name);
		String value = id != null ? getAttributeAsString(id.intValue()) : null;
		if (value == null) {
			if (isRequired) {
				throw new ParseException("attribute " + name + " not found",
						currentPosition);
			}
			return defaultValue;
		} else {
			return value;
		}
	}

	private int[] getAttributeAsIntArray(int valPos, int slen) {
		int[] tmp = new int[slen];
		for (int i = 0, n = slen; i < n; i++) {
			tmp[i] = getIValue(valPos + i * 4);
		}
		return tmp;
	}

	protected int[] getAttributeAsIntArray(String name, boolean isRequired)
			throws ParseException {
		Integer id = (Integer) nameLookup.get(name);
		Object val = id != null ? getAttributeAsObject(id.intValue()) : null;
		if (val != null) {
			if (val instanceof int[]) {
				return (int[]) val;
			} else {
				throw new ParseException(
						"Illegal value type, expected int[] got " + val, 0);
			}
		}
		return null;
	}

	// -------------------------------------------------------------------
	// Output
	// -------------------------------------------------------------------

	// Note that these methods will mess up the current position of the
	// reading
	public void printMessage() throws ParseException {
		printMessage(System.out);
	}

	public void printMessage(PrintStream out) throws ParseException {
		while (exitNode())
			;
		reset();
		printNodes(out, "");
	}

	private boolean printNodes(PrintStream out, String tab)
			throws ParseException {
		boolean nodes = false;
		String subTab = tab + "  ";
		while (nextNode(false)) {
			if (!nodes) {
				nodes = true;
				if (tab != "")
					out.println('>');
			}
			out.print(tab);
			out.print('<');
			out.print(getNodeName());
			for (int i = 0, atts = getAttributeCount(); i < atts; i++) {
				out.print(' ' + getAttributeName(i) + "=\"" + getAttribute(i)
						+ '"');
			}
			enterNode();
			boolean subNodes = printNodes(out, subTab);
			exitNode();
			if (subNodes) {
				out.print(tab);
				out.print("</");
				out.print(getNodeName());
				out.println('>');
			} else {
				out.println(" />");
			}
		}
		return nodes;
	}

} // BinaryTransportReader
