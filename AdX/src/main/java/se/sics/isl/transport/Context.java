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
 * Context
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Feb 12 15:50:50 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 * Purpose :
 *
 */
package se.sics.isl.transport;

import java.util.Hashtable;

public class Context {

	private Context parent;
	private String name;
	private Hashtable registry = new Hashtable();

	public Context(String name) {
		this(name, null);
	}

	public Context(String name, Context parent) {
		if (name == null)
			throw new NullPointerException();
		this.name = name;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public Context getParent() {
		return parent;
	}

	public String lookupClass(String transportName) {
		String className = (String) registry.get(transportName);
		return className == null && parent != null ? parent
				.lookupClass(transportName) : className;
	}

	public void addTransportable(String transportName, String className) {
		registry.put(transportName, className);
	}

	public void addTransportable(Transportable instance) {
		registry
				.put(instance.getTransportName(), instance.getClass().getName());
	}

} // Context
