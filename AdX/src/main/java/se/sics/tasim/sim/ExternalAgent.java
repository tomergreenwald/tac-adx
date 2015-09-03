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
 * ExternalAgent
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Dec 16 15:15:41 2002
 * Updated : $Date: 2008-04-04 21:23:36 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3984 $
 */
package se.sics.tasim.sim;

import java.util.logging.Logger;
import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.Message;

final class ExternalAgent extends Agent {

	private final static Logger log = Logger.getLogger(ExternalAgent.class
			.getName());

	public ExternalAgent() {
	}

	protected void simulationSetup() {
		log.fine(getName() + " setup with address " + getAddress());
	}

	protected void simulationFinished() {
	}

	protected void messageReceived(Message message) {
		// This should only happen if the agent has no agent channel
		log.severe("no connection to agent " + getName()
				+ ": ignoring message " + message);
	}

} // ExternalAgent
