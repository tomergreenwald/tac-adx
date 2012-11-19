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
 * BuiltinInfoConnection
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Jan 08 14:02:17 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.io.IOException;

import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;
import se.sics.tasim.is.SimConnection;

/**
 * Setup the information system to be builtin in a simulation server
 */
public class BuiltinInfoConnection extends InfoConnectionImpl {

	private InfoServer infoServer;

	public BuiltinInfoConnection() {
	}

	public void init(ConfigManager config)
			throws IllegalConfigurationException, IOException {
		super.init(config);
		if (infoServer == null) {
			infoServer = new InfoServer(config);
		}

		checkInitialized();
	}

	public void setSimConnection(SimConnection connection) {
		super.setSimConnection(connection);
		checkInitialized();
	}

	public void auth(String serverName, String serverPassword,
			String serverVersion) {
		super.auth(serverName, serverPassword, serverVersion);
		checkInitialized();
	}

	private void checkInitialized() {
		if (getServerName() != null && getSimConnection() != null
				&& infoServer != null) {
			infoServer.addInfoConnection(this);
		}
	}

} // BuiltinInfoConnection
