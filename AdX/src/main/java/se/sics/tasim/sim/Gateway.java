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
 * Gateway
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Oct 10 15:34:04 2002
 * Updated : $Date: 2008-04-04 21:23:36 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3984 $
 */
package se.sics.tasim.sim;

import java.io.IOException;

import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;

public abstract class Gateway {

	private Admin admin;
	private String name;

	protected Gateway() {
	}

	final void init(Admin admin, String name)
			throws IllegalConfigurationException {
		if (admin == null) {
			throw new NullPointerException();
		}
		this.admin = admin;
		this.name = name;
		initGateway();
	}

	public String getName() {
		return name;
	}

	// The version handling should be handled better. FIX THIS!!! \TODO
	public String getServerVersion() {
		return Admin.SERVER_VERSION;
	}

	final void start() throws IOException {
		startGateway();
	}

	final void stop() {
		stopGateway();
	}

	protected abstract void initGateway() throws IllegalConfigurationException;

	protected abstract void startGateway() throws IOException;

	protected abstract void stopGateway();

	protected ConfigManager getConfig() {
		return admin.getConfig();
	}

	protected void loginAgentChannel(AgentChannel channel, String name,
			String password) {
		channel.init(admin, name, password);
	}

} // Gateway
