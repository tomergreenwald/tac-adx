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
 * ServerConfig
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu May 22 17:54:19 2003
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.props;

import java.util.Enumeration;

import se.sics.isl.util.ConfigManager;

/**
 * <code>ServerConfig</code> contains the server configuration for a
 * game/simulation instance. Among other things it is used to store the server
 * configuration in the game/simulation logs.
 * 
 * <p>
 * <b>Warning:</b> serialized objects of this class might not be compatible with
 * future versions. Only use serialization of this class for temporary storage
 * or RMI using the same version of the class.
 */
public class ServerConfig extends SimpleContent {

	private static final long serialVersionUID = 5568859194058033441L;

	private static final int INTEGER = 0;
	private static final int FLOAT = 1;
	private static final int STRING = 2;

	public ServerConfig() {
	}

	public ServerConfig(ConfigManager config) {
		Enumeration enumeration = config.names();
		while (enumeration.hasMoreElements()) {
			String name = (String) enumeration.nextElement();
			String value = config.getProperty(name);
			int type = checkType(value);
			if (type == INTEGER) {
				try {
					long v = Long.parseLong(value);
					if (v <= Integer.MAX_VALUE && v >= Integer.MIN_VALUE) {
						setAttribute(name, (int) v);
					} else {
						setAttribute(name, v);
					}
				} catch (Exception e) {
					setAttribute(name, value);
				}
			} else if (type == FLOAT) {
				try {
					setAttribute(name, Float.parseFloat(value));
				} catch (Exception e) {
					setAttribute(name, value);
				}
			} else {
				// String
				setAttribute(name, value);
			}
		}
	}

	private int checkType(String value) {
		int type = INTEGER;
		for (int i = 0, n = value.length(); i < n; i++) {
			char c = value.charAt(i);
			if (c == '.') {
				if (type != INTEGER) {
					// Double dots can not be a float
					return STRING;
				}
				type = FLOAT;
			} else if (c < '0' || c > '9') {
				return STRING;
			}
		}
		return type;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer().append(getTransportName());
		return params(buf).toString();
	}

	// -------------------------------------------------------------------
	// Transportable (externalization support)
	// -------------------------------------------------------------------

	/**
	 * Returns the transport name used for externalization.
	 */
	public String getTransportName() {
		return "serverConfig";
	}

} // ServerConfig
