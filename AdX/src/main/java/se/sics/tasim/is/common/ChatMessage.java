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
 * ChatMessage
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Mar 20 12:24:32 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 * Purpose :
 *
 */
package se.sics.tasim.is.common;

import se.sics.isl.transport.TransportWriter;

public class ChatMessage {

	private long time;
	private String serverName;
	private String userName;
	private String message;

	public ChatMessage(long time, String serverName, String userName,
			String message) {
		setMessage(time, serverName, userName, message);
	}

	public long getTime() {
		return time;
	}

	public String getServerName() {
		return serverName;
	}

	public String getUserName() {
		return userName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(long time, String serverName, String userName,
			String message) {
		this.time = time;
		this.serverName = serverName;
		this.userName = userName;
		this.message = message;
	}

	public void writeMessage(TransportWriter writer) {
		writer.node("chat").attr("time", time).attr("server", serverName).attr(
				"user", userName).attr("message", message).endNode("chat");
	}

} // ChatMessage
