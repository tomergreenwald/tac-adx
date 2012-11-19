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
 * BlockingViewerChannel
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Mar 12 22:33:58 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.tact.TACTConnection;
import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.Context;

public class BlockingViewerChannel extends TACTConnection {

	private static final Logger log = Logger
			.getLogger(BlockingViewerChannel.class.getName());

	private static int channelCounter = 0;

	private final BlockingViewerServer viewerServer;
	private final BinaryTransportReader reader = new BinaryTransportReader();

	private SimServer simServer;

	public BlockingViewerChannel(BlockingViewerServer server, Socket socket,
			Context context) throws IOException {
		super(server.getName() + '-' + (++channelCounter), socket);
		this.viewerServer = server;
		this.reader.setContext(context);
		this.viewerServer.addViewerConnection(this);
	}

	// -------------------------------------------------------------------
	// TACTonnection API
	// -------------------------------------------------------------------

	protected void connectionOpened() {
	}

	protected void connectionClosed() {
		viewerServer.removeViewerConnection(this);
		SimServer server = this.simServer;
		if (server != null) {
			this.simServer = null;
			server.removeViewerConnection(this);
		}
	}

	protected void dataRead(byte[] buffer, int offset, int length) {
		reader.setMessage(buffer, offset, length);

		SimServer server = this.simServer;
		if (server == null) {
			// Not yet logged in
			try {
				if (reader.nextNode("auth", false)) {
					String serverName = reader.getAttribute("serverName");
					String userName = reader.getAttribute("userName");

					server = viewerServer.getSimServer(this, serverName);
					if (server != null) {
						setUserName(userName);
						this.simServer = server;
						log.finer("logged in " + userName + " as " + getName()
								+ " from " + getRemoteHost());
						server.addViewerConnection(this);

						// if (reader.hasMoreNodes()) {
						// server.viewerDataReceived(this, reader);
						// }

					} else {
						log.severe(getName() + " could not login " + userName
								+ " (unknown server " + serverName + ')');
						close();
					}
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, getName()
						+ " could not handle authentication", e);
				close();
			}
		} else {
			server.viewerDataReceived(this, reader);
		}
	}

} // BlockingViewerChannel
