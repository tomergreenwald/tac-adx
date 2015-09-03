/**
 * TAC Supply Chain Management Simulator
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
 * ViewerApplet
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Mar 12 12:43:11 2003
 * Updated : $Date: 2008-06-12 07:31:52 -0500 (Thu, 12 Jun 2008) $
 *           $Revision: 4728 $
 */
package se.sics.tasim.viewer.applet;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JApplet;

import se.sics.tasim.viewer.ChatListener;
import se.sics.tasim.viewer.ViewerPanel;
import se.sics.isl.transport.ContextFactory;

//MODIFIED BY-Lee Callender
public class ViewerApplet extends JApplet implements ChatListener {

	private static final Logger log = Logger.getLogger(ViewerApplet.class
			.getName());

	public static final String VERSION = "0.8.19";

	private String serverName;
	private int serverPort = 4042;
	private String userName;
	private ContextFactory contextFactory;
	private ViewerPanel mainPanel;

	private AppletConnection connection;

	public ViewerApplet() {
	}

	/**
	 * Applet info.
	 */
	public String getAppletInfo() {
		return "TAC SIM Game Viewer v" + VERSION + ", by SICS";
	}

	/**
	 * Applet parameter info.
	 */
	public String[][] getParameterInfo() {
		String[][] info = { { "user", "name", "the user name" },
				{ "port", "int", "the viewer connection port" },
				{ "serverName", "name", "the server name" },
				{ "contextFactory", "name", "the context factory class" }, };
		return info;
	}

	public void init() {
		this.serverName = getParameter("serverName");
		this.userName = getParameter("user");

		if (serverName == null) {
			throw new IllegalArgumentException("no server name specified");
		}
		if (userName == null) {
			throw new IllegalArgumentException("no user name specified");
		}

		String portDesc = getParameter("port");
		if (portDesc != null) {
			try {
				this.serverPort = Integer.parseInt(portDesc);
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not parse server port '"
						+ portDesc + '\'', e);
			}
		}

		// TODO-contextFactory must be passed in, should be the same as in
		// TACTGateway
		String contextFactoryClassName = getParameter("contextFactory");
		if (contextFactoryClassName == null) {
			throw new IllegalArgumentException("no contextFactory specified");
		}

		try {
			contextFactory = (ContextFactory) Class.forName(
					contextFactoryClassName).newInstance();
		} catch (ClassNotFoundException e) {
			log.severe("unable to load context factory: Class not found");
		} catch (InstantiationException e) {
			log
					.severe("unable to load context factory: Class cannot be instantiated");
		} catch (IllegalAccessException e) {
			log
					.severe("unable to load context factory: Illegal access exception");
		}

		/*
		 * if (contextFactory == null) { throw new
		 * IllegalArgumentException("no context factory specified"); }
		 */

		mainPanel = new ViewerPanel(userName, serverName);
		mainPanel.setChatListener(this);
		getContentPane().add(mainPanel.getComponent());
	}

	public void start() {
		connection = new AppletConnection(this, mainPanel);
		connection.start();
	}

	public void stop() {
		connection.stop();
	}

	public void destroy() {
	}

	// -------------------------------------------------------------------
	// API towards AppletConnection & Chat area
	// -------------------------------------------------------------------

	public String getServerName() {
		return serverName;
	}

	public int getServerPort() {
		return serverPort;
	}

	public String getUserName() {
		return userName;
	}

	public ContextFactory getContextFactory() {
		return contextFactory;
	}

	// -------------------------------------------------------------------
	// Chat handling
	// -------------------------------------------------------------------

	public void addChatMessage(long time, String serverName, String userName,
			String message) {
		mainPanel.addChatMessage(time, serverName, userName, message);
	}

	public void setStatusMessage(String message) {
		mainPanel.setStatusMessage(message);
	}

	public void sendChatMessage(String message) {
		AppletConnection connection = this.connection;
		if (connection != null) {
			connection.sendChatMessage(message);
		}
	}

} // ViewerApplet
