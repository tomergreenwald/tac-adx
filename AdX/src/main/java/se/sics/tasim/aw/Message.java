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
 * Message
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Oct 07 18:47:09 2002
 * Updated : $Date: 2008-04-04 20:25:04 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3980 $
 */
package se.sics.tasim.aw;

import java.text.ParseException;

import se.sics.isl.transport.Transportable;
import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;

/**
 * <code>Message</code> is used for the communication between all entities in
 * simulations and with the server administration. Each message consists of a
 * sender, a receiver and a content. The content of the messages describe what
 * the purpose of the communication is.
 */
public class Message implements Transportable {

	private String sender;
	private String receiver;
	private Transportable content;

	public Message(String receiver, Transportable content) {
		if (receiver == null) {
			throw new NullPointerException("receiver");
		}
		if (content == null) {
			throw new NullPointerException("content");
		}
		this.receiver = receiver;
		this.content = content;
	}

	public Message(String sender, String receiver, Transportable content) {
		this(receiver, content);
		this.sender = sender;
	}

	public Message() {
	}

	public String getSender() {
		return sender;
	}

	public synchronized void setSender(String sender) {
		if (this.sender != null) {
			throw new IllegalStateException("sender already set");
		}
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public Transportable getContent() {
		return content;
	}

	public Message createReply(Transportable content) {
		return new Message(receiver, sender, content);
	}

	public String toString() {
		return "Message[" + sender + ',' + receiver + ',' + content + ']';
	}

	/*****************************************************************************
	 * Transportable API
	 ****************************************************************************/

	/**
	 * Returns the transport name used for externalization.
	 */
	public String getTransportName() {
		return "message";
	}

	public void read(TransportReader reader) throws ParseException {
		if (receiver != null) {
			throw new IllegalStateException("already initialized");
		}

		// Already in this node when starting - attributes can be accessed
		// before going to next node.
		String receiver = reader.getAttribute("receiver");
		String sender = reader.getAttribute("sender");

		reader.nextNode(true);
		this.content = reader.readTransportable();
		this.receiver = receiver;
		this.sender = sender;
	}

	public void write(TransportWriter writer) {
		if (receiver == null) {
			throw new IllegalStateException("not initalized");
		}
		if (sender == null) {
			throw new IllegalStateException("no sender");
		}
		writer.attr("sender", sender);
		writer.attr("receiver", receiver);
		writer.write(content);
	}

} // Message
