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
 * AgentRealm
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Jun 17 20:06:39 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import org.mortbay.http.HashUserRealm;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.UserPrincipal;
import org.mortbay.http.UserRealm;

/**
 */
public class AgentRealm extends HashUserRealm {

	public static final String ADMIN_ROLE = "admin";

	private InfoServer infoServer;

	public AgentRealm(InfoServer infoServer, String realmName) {
		super(realmName);
		this.infoServer = infoServer;
	}

	void setAdminUser(String name, String password) {
		put(name, password);
		addUserToRole(name, ADMIN_ROLE);
	}

	public UserPrincipal authenticate(String username, Object credentials,
			HttpRequest request) {
		if (get(username) == null) {
			updateUser(username);
		}
		return super.authenticate(username, credentials, request);
	}

	public void updateUser(String name) {
		String password = infoServer.getUserPassword(name);
		if (password != null) {
			put(name, password);
		}
	}

} // AgentRealm
