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
 * TransportWriter
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Dec 17 16:50:16 2002
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 *
 * MODIFIED BY: Lee Callender
 * Date: 10/29/08
 */
package se.sics.isl.transport;

public abstract class TransportWriter {

	/**
	 * Notify the writer that this string value should be regarded as a constant
	 * that probably will be used many times. The implementation might use this
	 * for optimization.
	 * 
	 * @param constant
	 *            the value that should be regarded as a constant.
	 */
	public void addConstant(String constant) {
	}

	public abstract int getNodeLevel();

	public abstract TransportWriter node(String name);

	public abstract TransportWriter endNode(String name);

	public TransportWriter attr(String name, int value) {
		return attr(name, Integer.toString(value));
	}

	public TransportWriter attr(String name, long value) {
		return attr(name, Long.toString(value));
	}

	public TransportWriter attr(String name, float value) {
		return attr(name, Float.toString(value));
	}

	// Added by Lee Callender
	public TransportWriter attr(String name, double value) {
		return attr(name, Double.toString(value));
	}

	public TransportWriter attr(String name, int[] value) {
		// This stuff can not be parsed yet...
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (int j = 0, m = value.length; j < m; j++) {
			if (j > 0) {
				sb.append(',');
			}
			sb.append("" + value[j]);
		}
		sb.append(']');
		return attr(name, sb.toString());
	}

	public abstract TransportWriter attr(String name, String value);

	public TransportWriter write(Transportable object) {
		String nodeName = object.getTransportName();
		node(nodeName);

		int nodeLevel = getNodeLevel();
		object.write(this);
		if (nodeLevel != getNodeLevel()) {
			throw new IllegalStateException("wrong node level "
					+ getNodeLevel() + " (expected " + nodeLevel
					+ ") for transportable " + object.getClass().getName());
		}

		endNode(nodeName);
		return this;
	}

} // TransportWriter
