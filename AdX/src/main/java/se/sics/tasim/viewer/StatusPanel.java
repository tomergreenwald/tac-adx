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
 * StatusPanel
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Jan 30 15:19:28 2003
 * Updated : $Date: 2004-10-28 14:24:41 -0500 (Thu, 28 Oct 2004) $
 *           $Revision: 1057 $
 */
package se.sics.tasim.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import se.sics.isl.gui.Clock;

public class StatusPanel extends JPanel implements ActionListener {

	private final static int MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

	private ViewerPanel viewerPanel;

	private JLabel serverTimeLabel;
	private long lastServerTime = 0L;

	private AdvPanel advPanel;

	private JLabel simLabel;

	private Clock simClock;
	private JLabel simTimeUnitLabel;
	private int simTimeUnit = 0;
	private long millisPerUnit = 1000;
	private long nextTimeUnit = Long.MAX_VALUE;
	private int simRatio = 1;
	private int timeUnitCount = 0;

	private long simStartTime;
	private long lastTimeUnitUpdate;
	private String timeUnitName = null;
	private boolean useSimulationTime = false;

	private JProgressBar timeProgress;

	private Timer timer = new Timer(1000, this);
	private Timer simTimer = null;

	public StatusPanel(ViewerPanel viewerPanel, Color foregroundColor,
			Color backgroundColor) {
		super(new BorderLayout());
		this.viewerPanel = viewerPanel;

		setForeground(foregroundColor);
		setBackground(backgroundColor);
		setBorder(BorderFactory.createLineBorder(foregroundColor));

		// Size of the side status panels. Must be at least as large to
		// contain the clock.
		Dimension dim = new Dimension(180, 50);
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(backgroundColor);
		panel.setForeground(foregroundColor);
		serverTimeLabel = new JLabel();
		serverTimeLabel.setForeground(foregroundColor);
		panel.add(serverTimeLabel, BorderLayout.NORTH);
		simLabel = new JLabel();
		simLabel.setForeground(foregroundColor);
		panel.add(simLabel, BorderLayout.SOUTH);
		panel.setPreferredSize(dim);
		add(panel, BorderLayout.WEST);

		advPanel = new AdvPanel(viewerPanel);
		advPanel.setForeground(foregroundColor);
		advPanel.setBackground(backgroundColor);
		add(advPanel, BorderLayout.CENTER);

		panel = new JPanel(new BorderLayout());
		panel.setForeground(foregroundColor);
		panel.setBackground(backgroundColor);
		simClock = new Clock();
		simClock.setPreferredSize(new Dimension(50, 50));
		panel.add(simClock, BorderLayout.EAST);

		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.setBackground(backgroundColor);
		simTimeUnitLabel = new JLabel("", JLabel.LEFT);
		simTimeUnitLabel.setVerticalAlignment(JLabel.TOP);
		simTimeUnitLabel.setForeground(foregroundColor);
		panel2.add(simTimeUnitLabel, BorderLayout.CENTER);
		timeProgress = new JProgressBar(JProgressBar.HORIZONTAL, 0, 100);
		timeProgress.setOpaque(false);
		timeProgress.setBorderPainted(false);
		timeProgress.setPreferredSize(new Dimension(120, 5));
		timeProgress.setBorder(BorderFactory.createEmptyBorder(0, 5, 3, 5));
		panel2.add(timeProgress, BorderLayout.SOUTH);

		panel.add(panel2, BorderLayout.WEST);
		panel.setPreferredSize(dim);
		add(panel, BorderLayout.EAST);

		long currentTime = viewerPanel.getServerTime();
		updateTime(currentTime);

		// Synchronize the timer at the second and start it
		timer
				.setInitialDelay((int) ((currentTime / 1000) * 1000 + 1000 - currentTime));
		timer.setRepeats(true);
		timer.start();
	}

	public void simulationStarted(int simID, String simType, long startTime,
			long endTime, String timeUnitName, int timeUnitCount) {
		long currentTime = viewerPanel.getServerTime();
		this.simStartTime = startTime;
		this.simTimeUnit = 0;
		if (timeUnitCount > 0) {
			this.millisPerUnit = (endTime - startTime) / timeUnitCount;
			if (this.millisPerUnit < 1000)
				this.millisPerUnit = 1000;
			this.timeUnitName = timeUnitName;
			this.simRatio = (int) (MILLIS_PER_DAY / millisPerUnit);
			this.timeUnitCount = timeUnitCount;

			if (currentTime > startTime) {
				// The simulation has already been started and we must modify
				// the nextTimeUnit accordingly
				this.simTimeUnit = (int) ((currentTime - startTime) / this.millisPerUnit);
				this.nextTimeUnit = startTime + simTimeUnit
						* this.millisPerUnit;
			} else {
				this.nextTimeUnit = startTime;
			}

		} else {
			this.millisPerUnit = Long.MAX_VALUE;
			this.timeUnitName = null;
			this.simRatio = 1;
			this.timeUnitCount = 1;
			this.nextTimeUnit = Long.MAX_VALUE;
		}

		this.lastTimeUnitUpdate = currentTime;
		simLabel.setText("Game " + simID);
		// simLabel.setText(simType == null
		// ? ("Game " + simID)
		// : ("Game " + simID + " (" + simType + ')'));
		if (simRatio <= 1 || timeUnitName == null) {
			this.useSimulationTime = false;
			simClock.setShowingSeconds(true);
			simTimeUnitLabel.setText("");
		} else {
			this.useSimulationTime = true;
			simClock.setShowingSeconds(false);
			if (simTimer == null) {
				simTimer = new Timer(100, this);
				simTimer.setRepeats(true);
			}
			simTimer.start();
		}
		updateTime(currentTime);
	}

	public void simulationStopped(int simID) {
		this.useSimulationTime = false;
		if (simTimer != null) {
			simTimer.stop();
		}
		simRatio = 1;
		nextTimeUnit = Long.MAX_VALUE;
		simClock.setShowingSeconds(true);
		timeProgress.setValue(0);
	}

	public void nextTimeUnit(int timeUnit) {
		if (timeUnit < timeUnitCount) {
			// Since we now know that next time unit notification is
			// supported, there is no need to manually change this anymore
			this.nextTimeUnit = Long.MAX_VALUE;
			this.lastTimeUnitUpdate = viewerPanel.getServerTime();
			this.simTimeUnit = timeUnit;
			simTimeUnitLabel.setText(timeUnitName + ": " + simTimeUnit + " / "
					+ (timeUnitCount - 1));
		}
	}

	public StringBuffer appendTime(StringBuffer sb, long time) {
		time /= 1000;
		long sek = time % 60;
		long minutes = (time / 60) % 60;
		long hours = (time / 3600) % 24;
		if (hours < 10)
			sb.append('0');
		sb.append(hours).append(':');
		if (minutes < 10)
			sb.append('0');
		sb.append(minutes).append(':');
		if (sek < 10)
			sb.append('0');
		sb.append(sek);
		return sb;
	}

	private void updateTime(long serverTime) {
		StringBuffer sb = new StringBuffer();
		sb.append("Server time: ");
		appendTime(sb, serverTime);
		serverTimeLabel.setText(sb.toString());

		if (serverTime >= nextTimeUnit) {
			// Time unit update
			nextTimeUnit += millisPerUnit;
			lastTimeUnitUpdate = serverTime;
			if (simTimeUnit < (timeUnitCount - 1)) {
				simTimeUnit++;
				simTimeUnitLabel.setText(timeUnitName + ": " + simTimeUnit
						+ " / " + (timeUnitCount - 1));
			}
		}

		if (useSimulationTime) {
			long progress = ((serverTime - lastTimeUnitUpdate) * 100 / millisPerUnit);
			timeProgress.setValue(progress < 100 ? (int) progress : 100);
		}

		updateClock(serverTime);
	}

	private void updateClock(long serverTime) {
		if (useSimulationTime) {
			serverTime = (serverTime - simStartTime) * simRatio;
		}
		simClock.setTime(serverTime);
	}

	/*********************************************************************
	 * ActionListener
	 *********************************************************************/

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == timer) {
			long serverTime = viewerPanel.getServerTime();
			updateTime(serverTime);
			viewerPanel.tick(serverTime);

		} else if (source == simTimer) {
			long serverTime = viewerPanel.getServerTime();
			updateClock(serverTime);
			viewerPanel.simulationTick(serverTime, simTimeUnit);
		}
	}

} // StatusPanel
