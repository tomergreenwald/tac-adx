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
 * ISClient
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Dec 03 17:08:01 2002
 * Updated : $Date: 2008-04-04 21:23:36 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3984 $
 */
package se.sics.tasim.sim;

import java.awt.Dimension;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import se.sics.isl.util.ConfigManager;
import se.sics.tasim.is.EventWriter;
import se.sics.tasim.viewer.ChatListener;
import se.sics.tasim.viewer.ViewerConnection;
import se.sics.tasim.viewer.ViewerPanel;

final class ISClient implements ChatListener {

	private final Admin admin;
	private JFrame window;
	private ViewerPanel viewer;
	private ViewerConnection writer;

	public ISClient(Admin admin, EventWriter realWriter) {
		this.admin = admin;

		ConfigManager config = admin.getConfig();
		if (config.getPropertyAsBoolean(Admin.CONF + "gui.systemLookAndFeel",
				false)) {
			try {
				// Set the system look and feel
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (UnsupportedLookAndFeelException exc) {
				Logger.global.warning("ISClient: unsupported look-and-feel: "
						+ exc);
			} catch (Exception exc) {
				Logger.global
						.warning("ISClient: could not change look-and-feel: "
								+ exc);
			}
		}

		// Open the application GUI!!!
		String serverName = admin.getServerName();
		window = new JFrame("Simulation Viewer for " + serverName);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		viewer = new ViewerPanel("admin", serverName);
		viewer.setChatListener(this);
		writer = new BuiltinGUIWriter(viewer, realWriter);
		window.getContentPane().add(viewer.getComponent());
		window.setSize(config.getPropertyAsInt(Admin.CONF + "gui.width", 800),
				config.getPropertyAsInt(Admin.CONF + "gui.height", 750));

		// Center on screen
		Dimension screenSize = window.getToolkit().getScreenSize();
		window.setLocation(config.getPropertyAsInt(Admin.CONF + "gui.x",
				(screenSize.width - window.getWidth()) / 2), config
				.getPropertyAsInt(Admin.CONF + "gui.y",
						(screenSize.height - window.getHeight()) / 2));
	}

	public void start() {
		window.setVisible(true);
	}

	public EventWriter getEventWriter() {
		return writer;
	}

	public ViewerConnection getViewerConnection() {
		return writer;
	}

	public void addChatMessage(long time, String serverName, String userName,
			String message) {
		viewer.addChatMessage(time, serverName, userName, message);
	}

	/*****************************************************************************
	 * ChatListener API
	 ****************************************************************************/

	public void sendChatMessage(String message) {
		admin.sendChatMessage(message);
	}

} // ISClient
