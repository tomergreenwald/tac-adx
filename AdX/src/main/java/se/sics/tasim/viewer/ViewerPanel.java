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
 * ViewerPanel
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Dec 03 17:12:16 2002
 * Updated : $Date: 2008-02-23 17:02:22 -0600 (Sat, 23 Feb 2008) $
 *           $Revision: 3758 $
 */
package se.sics.tasim.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.File;
import java.net.URL;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import com.botbox.util.ArrayUtils;
import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;

public class ViewerPanel extends ViewerConnection {

	private static final Logger log = Logger.getLogger(ViewerPanel.class
			.getName());

	private final static Integer DIALOG_LAYER = new Integer(199);

	private Hashtable iconTable = new Hashtable();

	private ConfigManager config;
	private SimulationViewer viewer;

	private String userName;
	private String serverName;
	private long timeDiff = 0L;

	private JPanel mainPanel;
	private StatusPanel statusPanel;
	private JLabel statusLabel;
	private ChatPanel chatPanel;
	private JComponent viewerPanel;

	private DialogPanel dialogPanel;
	private JComponent currentDialog;

	private TickListener[] tickListeners;

	private ChatListener chatListener;

	private Color backgroundColor = Color.black;
	private Color foregroundColor = Color.white;

	public ViewerPanel(String userName, String serverName) {
		this.serverName = serverName;
		this.userName = userName;

		// TODO: use the tasim_viewer.conf file
		// NOTE: this should actually be the tac09aa_viewer.conf file
		String configFile = "tasim_viewer.conf";
		// String configFile = "tac13adx_viewer.conf";
		URL configURL = ViewerPanel.class.getResource("/config/" + configFile);
		config = new ConfigManager();
		try {
			if (configURL != null) {
				config.loadConfiguration(configURL);
			} else if (!config.loadConfiguration("config" + File.separatorChar
					+ configFile)) {
				// Failed to load the configuration.
				log.severe("could not find config " + configFile);
			}
		} catch (Exception e) {
			log.severe("could not find config " + configFile);
		}

		// Should not be hardcoded but setup depending on simulation type. FIX
		// THIS!!! 0 \TODO

		viewer = null;

		try {
			String simulationViewerClass = config
					.getProperty("simulationViewer");
			viewer = (SimulationViewer) Class.forName(simulationViewerClass)
					.newInstance();
		} catch (InstantiationException e) {
			log.severe("Could not instantiate the simulation viewer");
		} catch (IllegalAccessException e) {
			log.severe("Could not instantiate the simulation viewer");
		} catch (ClassNotFoundException e) {
			log.severe("Could not find the simulation viewer class");
		}

		viewer.init(this);
		viewerPanel = viewer.getComponent();

		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setForeground(foregroundColor);
		mainPanel.setBackground(backgroundColor);
		mainPanel.add(viewerPanel, BorderLayout.CENTER);
		statusLabel = new JLabel("Status:");
		statusLabel.setOpaque(true);
		statusLabel.setForeground(foregroundColor);
		statusLabel.setBackground(backgroundColor);
		chatPanel = new ChatPanel(this);
		// Hack to avoid using another panel
		chatPanel.setStatusLabel(statusLabel);
		mainPanel.add(chatPanel, BorderLayout.SOUTH);
		statusPanel = new StatusPanel(this, foregroundColor, backgroundColor);
		mainPanel.add(statusPanel, BorderLayout.NORTH);
	}

	public JComponent getComponent() {
		return mainPanel;
	}

	public ConfigManager getConfig() {
		return config;
	}

	public String getUserName() {
		return userName;
	}

	public String getServerName() {
		return serverName;
	}

	public long getServerTime() {
		return System.currentTimeMillis() + timeDiff;
	}

	public void setServerTime(long serverTime) {
		this.timeDiff = serverTime - System.currentTimeMillis();
	}

	public ImageIcon getIcon(String name) {
		ImageIcon icon = (ImageIcon) iconTable.get(name);
		if (icon != null) {
			return icon;
		}

		try {
			URL url = ViewerPanel.class
					.getResource(name.indexOf('/') >= 0 ? name
							: ("/images/" + name));
			if (url != null) {
				icon = new ImageIcon(url);
				if (icon.getIconHeight() > 0) {
					iconTable.put(name, icon);
					return icon;
				}
			} else {
				log.severe("could not find icon " + name);
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not load icon " + name, e);
		}
		return null;
	}

	public void setStatusMessage(String message) {
		statusLabel.setText("Status: " + message);
	}

	public void addChatMessage(long time, String serverName, String userName,
			String message) {
		chatPanel.addChatMessage(time, serverName, userName, message);
	}

	public void sendChatMessage(String message) {
		if (chatListener != null) {
			chatListener.sendChatMessage(message);
		} else {
			log.warning("no listener for chat message '" + message + '\'');
		}
	}

	public void setChatListener(ChatListener listener) {
		chatListener = listener;
	}

	public void showDialog(JComponent dialog) {
		closeDialog();

		JRootPane rootPane = SwingUtilities.getRootPane(mainPanel);
		if (rootPane == null) {
			log.severe("could not find root pane for viewer to show dialog "
					+ dialog);
		} else {
			JLayeredPane layeredPane = rootPane.getLayeredPane();
			Dimension d = dialog.getPreferredSize();
			if (dialogPanel == null) {
				dialogPanel = new DialogPanel(this, new BorderLayout());
			}
			Insets insets = dialogPanel.getInsets();
			int width = viewerPanel.getWidth() - insets.left - insets.right;
			int height = viewerPanel.getHeight() - insets.top - insets.bottom;
			if (d.width > width) {
				d.width = width;
			}
			if (d.height > height) {
				d.height = height;
			}
			dialogPanel.add(dialog, BorderLayout.CENTER);

			dialogPanel.setBounds(((width - d.width) >> 1) + insets.left,
					((height - d.height) >> 1) + insets.top, d.width
							+ insets.left + insets.right, d.height + insets.top
							+ insets.bottom);
			dialog.setVisible(true);
			layeredPane.add(dialogPanel, DIALOG_LAYER);
			currentDialog = dialog;
			mainPanel.repaint();
		}
	}

	final void closeDialog() {
		if (currentDialog != null) {
			JRootPane rootPane = SwingUtilities.getRootPane(mainPanel);
			if (rootPane != null) {
				JLayeredPane layeredPane = rootPane.getLayeredPane();
				layeredPane.remove(dialogPanel);
				currentDialog.setVisible(false);
				currentDialog = null;
				mainPanel.repaint();
			}
		}
	}

	public synchronized void addTickListener(TickListener listener) {
		tickListeners = (TickListener[]) ArrayUtils.add(TickListener.class,
				tickListeners, listener);
	}

	public synchronized void removeTickListener(TickListener listener) {
		tickListeners = (TickListener[]) ArrayUtils.remove(tickListeners,
				listener);
	}

	/*********************************************************************
	 * ViewerConnection API
	 **********************************************************************/

	public void nextTimeUnit(int timeUnit) {
		statusPanel.nextTimeUnit(timeUnit);
		viewer.nextTimeUnit(timeUnit);
	}

	public void participant(int index, int role, String name, int participantID) {
		viewer.participant(index, role, name, participantID);
	}

	public void dataUpdated(int agent, int type, int value) {
		viewer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, long value) {
		viewer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, float value) {
		viewer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, double value) {
		viewer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, String value) {
		viewer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int agent, int type, Transportable value) {
		viewer.dataUpdated(agent, type, value);
	}

	public void dataUpdated(int type, Transportable value) {
		viewer.dataUpdated(type, value);
	}

	public void interaction(int fromAgent, int toAgent, int type) {
		viewer.interaction(fromAgent, toAgent, type);
	}

	public void interactionWithRole(int fromAgent, int role, int type) {
		viewer.interactionWithRole(fromAgent, role, type);
	}

	public void simulationStarted(int realSimID, String type, long startTime,
			long endTime, String timeUnitName, int timeUnitCount) {
		// Close any old open dialog
		closeDialog();
		statusPanel.simulationStarted(realSimID, type, startTime, endTime,
				timeUnitName, timeUnitCount);
		viewer.simulationStarted(realSimID, type, startTime, endTime,
				timeUnitName, timeUnitCount);
		setStatusMessage("Game " + realSimID + " is running");
	}

	public void simulationStopped(int realSimID) {
		setStatusMessage("Game " + realSimID + " has finished");
		statusPanel.simulationStopped(realSimID);
		viewer.simulationStopped(realSimID);
	}

	// -1, 0 if no more simulations scheduled
	public void nextSimulation(int realSimID, long startTime) {
		if (startTime > 0L) {
			StringBuffer sb = new StringBuffer();
			sb.append("Next game ");
			if (realSimID > 0) {
				sb.append(realSimID).append(' ');
			}
			sb.append("starts at ");
			statusPanel.appendTime(sb, startTime);
			setStatusMessage(sb.toString());
		} else {
			setStatusMessage("No future games scheduled");
		}
	}

	public void intCache(int agent, int type, int[] cache) {
		if (cache == null) {
			System.out.println("**** CACHE IS NULL????");
			return;
		}

		for (int i = 0, n = cache.length; i < n; i++) {
			dataUpdated(agent, type, (long) cache[i]);
		}

	}

	/*********************************************************************
	 * API towards StatusPanel
	 **********************************************************************/

	final void tick(long serverTime) {
		TickListener[] listeners = this.tickListeners;
		if (listeners != null) {
			for (int i = 0, n = listeners.length; i < n; i++) {
				listeners[i].tick(serverTime);
			}
		}
	}

	final void simulationTick(long serverTime, int timeUnit) {
		TickListener[] listeners = this.tickListeners;
		if (listeners != null) {
			for (int i = 0, n = listeners.length; i < n; i++) {
				listeners[i].simulationTick(serverTime, timeUnit);
			}
		}
	}

} // ViewerPanel
