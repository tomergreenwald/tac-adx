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
 * BinaryTransportWriter
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Jan 13 17:13:34 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 * Purpose :
 * To write XML-like messages in a compact binary format
 *
 * N count NameIndex T1 T2 T3... V1 V2 V3...
 * 1 1     2         ?  ?  ?
 * T V1 V2 V3 - same types as the previous "node" - only for empty nodes...
 *
 * n [and the rest as N - but without the end-tag since n is a complete node]
 * \n - ends nodes and tables
 *
 * MODIFIED BY: Lee Callender
 * Date: 10/29/2008
 *
 */
package se.sics.isl.transport;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Hashtable;

import com.botbox.util.ArrayUtils;

public class BinaryTransportWriter extends TransportWriter implements
		BinaryTransport {

	public static final String SUPPORT_CONSTANTS = "constants";
	public static final String SUPPORT_TABLES = "tables";

	private static final int DEF_SIZE = 10;
	private static final int ALIAS_SIZE = 256;
	private static final int DATA_SIZE = 1024;

	private static final int TYPE_POS = 0;
	private static final int NAME_POS = 1;

	private Hashtable constantLookup = new Hashtable();

	private boolean inNode = false; // Defining a node
	private int nodeLevel = 0;

	private boolean isTablesSupported = false;
	private boolean isConstantsSupported = false;

	// Storage for old attributes "rows" when defining tables...
	private int[][] currentRow = new int[2][DEF_SIZE * 2]; // Type-Name pairs
	private long[][] currentIValues = new long[2][DEF_SIZE];
	private float[][] currentFValues = new float[2][DEF_SIZE];
	private double[][] currentDValues = new double[2][DEF_SIZE]; // Modified by
																	// Lee
																	// Callender
	private Object[][] currentOValues = new Object[2][DEF_SIZE];
	private int attrCount[] = new int[2];
	private int nodeName[] = new int[2];
	private boolean nodeWritten[] = new boolean[2];
	private int currentPos = 0;

	private int nextID = 0;

	private byte[] aliasData = new byte[ALIAS_SIZE];
	private int aliasSize = 0;

	private byte[] byteData = new byte[DATA_SIZE]; // Current 'message' buffer
	private int nrOfBytes;

	public BinaryTransportWriter() {
		clear();
	}

	public boolean isSupported(String name) {
		if (SUPPORT_CONSTANTS.equals(name)) {
			return isConstantsSupported;
		} else if (SUPPORT_TABLES.equals(name)) {
			return isTablesSupported;
		} else {
			return false;
		}
	}

	public void setSupported(String name, boolean isSupported) {
		if (SUPPORT_CONSTANTS.equals(name)) {
			isConstantsSupported = isSupported;
		} else if (SUPPORT_TABLES.equals(name)) {
			isTablesSupported = isSupported;
		}
	}

	// -------------------------------------------------------------------
	// apis for writing a block of data
	// -------------------------------------------------------------------

	// \TODO HACK!!!! BECAUSE ALIASES ARE PER CONNECTION BUT WE WANT TO USE
	// BROADCAST.

	private void writeInit() {
		Enumeration enumb = constantLookup.keys();
		while (enumb.hasMoreElements()) {
			String name = (String) enumb.nextElement();
			int id = ((Integer) constantLookup.get(name)).intValue();
			writeByte(ALIAS);
			writeShort(id);
			writeString(name);
		}
	}

	public int getInitSize() {
		int oldPos = nrOfBytes;
		writeInit();
		int len = nrOfBytes - oldPos;
		nrOfBytes = oldPos;
		return len;
	}

	public byte[] getInitBytes() {
		int oldPos = nrOfBytes;
		writeInit();
		if (oldPos != nrOfBytes) {
			int size = nrOfBytes - oldPos;
			byte[] buffer = new byte[size];
			System.arraycopy(byteData, oldPos, buffer, 0, size);
			nrOfBytes = oldPos;
			return buffer;
		}
		return null;
	}

	public void writeInit(ByteBuffer buffer) {
		int oldPos = nrOfBytes;
		writeInit();
		if (oldPos != nrOfBytes) {
			buffer.put(byteData, oldPos, nrOfBytes - oldPos);
			nrOfBytes = oldPos;
		}
	}

	public void writeInit(OutputStream stream) throws IOException {
		int oldPos = nrOfBytes;
		writeInit();
		if (oldPos != nrOfBytes) {
			stream.write(byteData, oldPos, nrOfBytes - oldPos);
			nrOfBytes = oldPos;
		}
	}

	// \TODO HACK!!!! BECAUSE ALIASES ARE PER CONNECTION BUT WE WANT TO USE
	// BROADCAST.

	public int size() {
		return aliasSize + nrOfBytes;
	}

	public void write(ByteBuffer buffer) {
		if (aliasSize > 0) {
			buffer.put(aliasData, 0, aliasSize);
		}
		if (nrOfBytes > 0) {
			buffer.put(byteData, 0, nrOfBytes);
		}
	}

	public void write(OutputStream stream) throws IOException {
		if (aliasSize > 0) {
			stream.write(aliasData, 0, aliasSize);
		}
		if (nrOfBytes > 0) {
			stream.write(byteData, 0, nrOfBytes);
		}
	}

	public void write(byte[] buffer) {
		if (buffer.length < aliasSize + nrOfBytes) {
			throw new IndexOutOfBoundsException("Too many bytes to fit array, "
					+ "requires " + (aliasSize + nrOfBytes) + ", got "
					+ buffer.length);
		}
		if (aliasSize > 0) {
			System.arraycopy(aliasData, 0, buffer, 0, aliasSize);
		}
		if (nrOfBytes > 0) {
			System.arraycopy(byteData, 0, buffer, aliasSize, nrOfBytes);
		}
	}

	public byte[] getBytes() {
		byte[] buffer = new byte[aliasSize + nrOfBytes];
		if (aliasSize > 0) {
			System.arraycopy(aliasData, 0, buffer, 0, aliasSize);
		}
		if (nrOfBytes > 0) {
			System.arraycopy(byteData, 0, buffer, aliasSize, nrOfBytes);
		}
		return buffer;
	}

	// -------------------------------------------------------------------
	// TransportWriter API
	// -------------------------------------------------------------------

	public void addConstant(String constant) {
		if (isConstantsSupported) {
			createConstantID(constant);
		}
	}

	public TransportWriter attr(String name, int value) {
		if (!inNode)
			throw new IllegalArgumentException(
					"Can not output attributes outside of nodes");
		int nid = createConstantID(name);
		int index = setType(INT, nid);
		currentIValues[currentPos][index] = value;
		return this;
	}

	public TransportWriter attr(String name, long value) {
		if (!inNode)
			throw new IllegalArgumentException(
					"Can not output attributes outside of nodes");
		// System.out.println("LONG Attribute: " + name + " = " + value);
		int nid = createConstantID(name);
		int index = setType(LONG, nid);
		currentIValues[currentPos][index] = value;
		return this;
	}

	public TransportWriter attr(String name, float value) {
		if (!inNode)
			throw new IllegalArgumentException(
					"Can not output attributes outside of nodes");
		// System.out.println("FLOAT Attribute: " + name);
		int nid = createConstantID(name);
		int index = setType(FLOAT, nid);
		currentFValues[currentPos][index] = value;
		return this;
	}

	// Modified by Lee Callender
	public TransportWriter attr(String name, double value) {
		if (!inNode)
			throw new IllegalArgumentException(
					"Can not output attributes outside of nodes");
		int nid = createConstantID(name);
		int index = setType(DOUBLE, nid);
		currentDValues[currentPos][index] = value;
		return this;
	}

	public TransportWriter attr(String name, String value) {
		if (!inNode)
			throw new IllegalArgumentException(
					"Can not output attributes outside of nodes");
		int nid = createConstantID(name);
		int cid;
		if (isConstantsSupported && ((cid = getConstantID(value)) >= 0)) {
			int index = setType(CONSTANT_STRING, nid);
			currentIValues[currentPos][index] = cid;
		} else {
			int index = setType(STRING, nid);
			currentOValues[currentPos][index] = value;
		}
		return this;
	}

	public TransportWriter attr(String name, int[] value) {
		if (!inNode)
			throw new IllegalArgumentException(
					"Can not output attributes outside of nodes");
		int nid = createConstantID(name);
		int index = setType(INT_ARR, nid);
		currentOValues[currentPos][index] = value;
		return this;
	}

	public int getNodeLevel() {
		return nodeLevel;
	}

	public TransportWriter node(String name) {
		if (inNode) {
			writeCurrentNode(START_NODE, currentPos);
		}

		// Update this node information
		currentPos = 1 - currentPos;
		nodeName[currentPos] = createConstantID(name);
		nodeWritten[currentPos] = false;
		attrCount[currentPos] = 0;
		nodeLevel++;
		inNode = true;
		return this;
	}

	public TransportWriter endNode(String name) {
		// \TODO Should verify the node name.
		return endNode();
	}

	private TransportWriter endNode() {
		if (nodeLevel > 0) {
			writeCurrentNode(END_NODE, currentPos);
			nodeLevel--;
		}
		inNode = false;
		return this;
	}

	private void writeCurrentNode(byte nodeType, int pos) {
		// Syncronize if several writing to same stream...
		int name = nodeName[pos];

		// Write only if name is defined
		if (name == -1 || nodeWritten[pos]) {
			if (nodeType == END_NODE) {
				writeByte(END_NODE);
			}

		} else {
			boolean writeTypes = true;
			if (nodeType == END_NODE) {
				int other = 1 - pos;
				if (isTablesSupported && nodeName[other] == name
						&& nodeWritten[other]
						&& attrCount[pos] == attrCount[other]) {
					// Possible table. Compare all attributes
					writeTypes = false;
					for (int i = 0, n = attrCount[pos] * 2; i < n; i++) {
						if (currentRow[pos][i] != currentRow[other][i]) {
							// Table not possible
							writeTypes = true;
							break;
						}
					}
				}
				writeByte(writeTypes ? NODE : TABLE);
			} else {
				writeByte(START_NODE);
			}
			if (writeTypes) {
				writeByte(attrCount[pos]);
				writeShort(name);
				// Write types
				for (int i = 0, n = attrCount[pos] * 2; i < n; i += 2) {
					int type = currentRow[pos][i + TYPE_POS];
					writeByte(type);
					writeShort(currentRow[pos][i + NAME_POS]);
				}
			}
			// Write values
			for (int i = 0, n = attrCount[pos]; i < n; i++) {
				int type = currentRow[pos][i * 2 + TYPE_POS];
				switch (type) {
				case INT:
					writeInt((int) currentIValues[pos][i]);
					break;
				case LONG:
					writeLong(currentIValues[pos][i]);
					break;
				case FLOAT:
					writeFloat(currentFValues[pos][i]);
					break;
				case DOUBLE: // Modified by Lee Callender
					writeDouble(currentDValues[pos][i]);
					break;
				case STRING:
					writeString((String) currentOValues[pos][i]);
					break;
				case CONSTANT_STRING:
					writeShort((int) currentIValues[pos][i]);
					break;
				case INT_ARR:
					writeIntArr((int[]) currentOValues[pos][i]);
					break;
				}
			}
			// Written - do not write again
			nodeWritten[pos] = true;
		}
	}

	private void writeByte(int data) {
		if (nrOfBytes >= byteData.length) {
			byteData = (byte[]) ArrayUtils.setSize(byteData, nrOfBytes
					+ DATA_SIZE);
		}
		// System.out.println("Writing byte: " + data + " (0x"
		// + Integer.toHexString(data) + ") to " + nrOfBytes);
		byteData[nrOfBytes++] = (byte) (data & 0xff);
	}

	private void writeShort(int data) {
		writeByte((data >> 8) & 0xff);
		writeByte(data & 0xff);
	}

	private void writeInt(int data) {
		writeByte((int) (data >>> 24) & 0xff);
		writeByte((int) (data >>> 16) & 0xff);
		writeByte((int) (data >>> 8) & 0xff);
		writeByte((int) data & 0xff);
	}

	private void writeLong(long data) {
		writeByte((int) (data >>> 56) & 0xff);
		writeByte((int) (data >>> 48) & 0xff);
		writeByte((int) (data >>> 40) & 0xff);
		writeByte((int) (data >>> 32) & 0xff);
		writeByte((int) (data >>> 24) & 0xff);
		writeByte((int) (data >>> 16) & 0xff);
		writeByte((int) (data >>> 8) & 0xff);
		writeByte((int) (data & 0xff));
	}

	private void writeFloat(float data) {
		writeInt(Float.floatToIntBits(data));
	}

	private void writeDouble(double data) // Modified by Lee Callender
	{
		writeLong(Double.doubleToLongBits(data));
	}

	/**
	 * Adds the specified string in UTF-8 machine independent format.
	 */
	private void writeString(String value) {
		// Make sure there is room for the maximum encoded string size
		// where each character takes three bytes.
		int maxSize = getMaxUTF8Size(value);
		if (nrOfBytes + maxSize > byteData.length) {
			byteData = (byte[]) ArrayUtils.setSize(byteData, nrOfBytes
					+ maxSize + DATA_SIZE);
		}
		int len = writeUTF8(byteData, nrOfBytes, value);
		nrOfBytes += len;
	}

	private void writeAlias(int id, String name) {
		int maxSize = 1 + 2 + getMaxUTF8Size(name);
		if (aliasSize + maxSize > aliasData.length) {
			aliasData = (byte[]) ArrayUtils.setSize(aliasData, aliasSize
					+ maxSize + ALIAS_SIZE);
		}
		aliasData[aliasSize++] = (byte) (ALIAS & 0xff);
		aliasData[aliasSize++] = (byte) ((id >> 8) & 0xff);
		aliasData[aliasSize++] = (byte) (id & 0xff);
		aliasSize += writeUTF8(aliasData, aliasSize, name);
	}

	private int getMaxUTF8Size(String value) {
		// Make sure there is room for the maximum encoded string size
		// where each character takes three bytes + the string size (short).
		int len = value.length();
		return len * 3 + 2;
	}

	private int writeUTF8(byte[] buffer, int offset, String value) {
		// This code has been "inspired" from Core JavaTM Technologies Tech
		// Tips, January 10, 2003 and java.io.Data{Input,Output}Stream.
		int index = offset + 2;
		for (int i = 0, len = value.length(); i < len; i++) {
			char c = value.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				buffer[index++] = (byte) c;
			} else if (c > 0x07FF) {
				buffer[index++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
				buffer[index++] = (byte) (0x80 | ((c >> 6) & 0x3F));
				buffer[index++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			} else {
				buffer[index++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
				buffer[index++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			}
		}
		int size = index - offset - 2;
		if (size > 65535) {
			throw new IllegalArgumentException("too large string: "
					+ value.length());
		}
		buffer[offset] = (byte) ((size >> 8) & 0xff);
		buffer[offset + 1] = (byte) (size & 0xff);
		return size + 2;
	}

	private void writeIntArr(int[] value) {
		// Make sure there is room for the maximum int arr
		// where each int takes 4 bytes.
		int len = value.length;
		if (nrOfBytes + len * 4 >= byteData.length) {
			byteData = (byte[]) ArrayUtils.setSize(byteData, nrOfBytes + len
					* 4 + DATA_SIZE);
		}
		writeShort(len);
		for (int i = 0, n = len; i < n; i++) {
			writeInt(value[i]);
		}
	}

	// private void writeBytes(byte[] bytes) {
	// if (nrOfBytes + bytes.length >= byteData.length) {
	// byteData = (byte[])
	// ArrayUtils.setSize(byteData, bytes.length + nrOfBytes + DATA_SIZE);
	// }
	// System.arraycopy(bytes, 0, byteData, nrOfBytes, bytes.length);
	// nrOfBytes += bytes.length;
	// }

	private int getConstantID(String name) {
		Integer alias = (Integer) constantLookup.get(name);
		return (alias != null) ? alias.intValue() : -1;
	}

	private int createConstantID(String name) {
		Integer alias = (Integer) constantLookup.get(name);
		if (alias != null) {
			return alias.intValue();
		} else {
			int id = nextID++;
			constantLookup.put(name, new Integer(id));
			writeAlias(id, name);
			return id;
		}
	}

	// ensure that all nodes and atts are in the "output"
	public void finish() {
		if (nodeLevel > 0) {
			for (int i = 0, n = nodeLevel; i < n; i++) {
				endNode();
			}
		}
	}

	public void clear() {
		aliasSize = 0;
		nrOfBytes = 0;
		nodeLevel = 0;
		inNode = false;
		nodeName[0] = -1;
		nodeWritten[0] = false;
		nodeName[1] = -1;
		nodeWritten[1] = false;
	}

	private int setType(int type, int name) {
		int ac = attrCount[currentPos];
		if (ac >= currentIValues[currentPos].length) {
			int newSize = ac + DEF_SIZE;
			currentIValues[currentPos] = ArrayUtils.setSize(
					currentIValues[currentPos], newSize);
			currentFValues[currentPos] = ArrayUtils.setSize(
					currentFValues[currentPos], newSize);
			currentDValues[currentPos] = ArrayUtils.setSize(
					currentDValues[currentPos], newSize); // Modified by Lee
															// Callender
			currentOValues[currentPos] = ArrayUtils.setSize(
					currentOValues[currentPos], newSize);
			currentRow[currentPos] = ArrayUtils.setSize(currentRow[currentPos],
					newSize * 2);
		}

		currentRow[currentPos][ac * 2] = type;
		currentRow[currentPos][ac * 2 + 1] = name;

		return attrCount[currentPos]++;
	}

	// -------------------------------------------------------------------
	// Test main
	// -------------------------------------------------------------------

	// public static void main(String[] a) throws Exception {
	// BinaryTransportWriter bmw = new BinaryTransportWriter();
	// bmw.setSupported(SUPPORT_CONSTANTS, true);
	// bmw.setSupported(SUPPORT_TABLES, true);
	// bmw.addConstant("fffff");
	// bmw.node("test").attr("id", 66).attr("str", "rad1").attr("con", "fffff");
	// bmw.endNode("test");
	// bmw.node("test").attr("id", 67).attr("str", "rad2").attr("con", "fffff");
	// bmw.endNode("test");
	// bmw.node("test").attr("id", 68).attr("str", "rad3").attr("con", "fffff");
	// bmw.endNode("test");
	// bmw.node("test");
	// bmw.attr("id", System.currentTimeMillis());
	// bmw.attr("str", "rad2");
	// bmw.endNode("test");
	// bmw.node("test2");
	// bmw.attr("id", 68.0f);
	// bmw.attr("str", "rad3");
	// bmw.node("subnode");
	// bmw.attr("id", 69);
	// bmw.attr("str", "sub-rad1");
	// bmw.endNode("subnode");
	// bmw.node("subnode");
	// bmw.attr("id", 70);
	// bmw.attr("str", "sub-rad2");
	// bmw.endNode("subnode");
	// bmw.node("subnode");
	// bmw.attr("id", 71);
	// bmw.attr("arr1", new int[]{65,66,67,68});
	// bmw.attr("str", "sub-rad3");
	// bmw.endNode("subnode");
	// bmw.endNode("test2");
	// bmw.finish();

	// // Write to bytes!
	// byte[] bmsg = new byte[bmw.size()];
	// bmw.write(bmsg);

	// System.out.println("Message:" + new String(bmsg));

	// BinaryTransportReader bmr = new BinaryTransportReader();
	// System.out.println("Parsing:");

	// bmr.setMessage(bmsg);

	// System.out.println("Printing message:");
	// bmr.printMessage();

	// while (bmr.nextNode(false)) {
	// System.out.println("Node Name: " + bmr.getNodeName());
	// System.out.println("Attribute id = " + bmr.getAttribute("id", null));
	// System.out.println("Attribute str = " + bmr.getAttribute("str", null));
	// }
	// }

} // BinaryTransportWriter
