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
 * SimulationPanel
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Dec 05 17:54:59 2002
 * Updated : $Date: 2004-10-28 14:24:41 -0500 (Thu, 28 Oct 2004) $
 *           $Revision: 1057 $
 */
package se.sics.tasim.viewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.botbox.util.ArrayUtils;
import se.sics.isl.util.ConfigManager;

// Should not be a JPanel or implement TickListener. FIX THIS!!!
public class SimulationPanel extends JPanel implements TickListener {

	public static final int LEFT = 1;
	public static final int CENTER = 2;
	public static final int RIGHT = 3;

	public static final int TYPE_BLUE = 0;
	public static final int TYPE_YELLOW = 1;
	public static final int TYPE_GREEN = 2;

	/**
	 * Specifies the maximal value used for types (types are specified between 0
	 * and the maximal value)
	 */
	public static final int MAX_TYPE_VALUE = TYPE_GREEN;

	private static final int DEFAULT_PHASE_NO = 1;
	private static final int DEFAULT_LINE_NO = 72;

	private static final int X1 = 0;
	private static final int Y1 = 1;
	private static final int X2 = 2;
	private static final int Y2 = 3;
	private static final int TYPE = 4;
	private static final int ANIMATION_POS = 5;
	private static final int PARTS = 6;

	private static final int MAX_COLOR = 6;
	private static final Color LIGHTER_BLUE = new Color(0xa0a0ff);
	private static final Color[] FADING_BLUES = { new Color(0x000080),
			new Color(0x0000a0), new Color(0x0000d0), new Color(0x4040f0),
			new Color(0x8080ff), new Color(0xa0a0ff) };

	private static final Color[] FADING_YELLOW = { new Color(0x808000),
			new Color(0xa0a000), new Color(0xd0d000), new Color(0xf0f040),
			new Color(0xffff80), new Color(0xffffa0) };

	private static final Color[] FADING_GREENS = { new Color(0x008000),
			new Color(0x00a000), new Color(0x00d000), new Color(0x40f040),
			new Color(0x80ff80), new Color(0xa0ffa0) };

	private static final int[] arrowAnim = { 0, 1, 2, 3, 4, 3 };

	// The array of potential lines...
	// X,Y - X,Y + type + intense
	// Need to FIX THIS!!! the phases!!!! FIX THIS!!!
	private int[][] connections = new int[DEFAULT_PHASE_NO][DEFAULT_LINE_NO
			* PARTS];
	private int[] connectionCount = new int[DEFAULT_PHASE_NO];
	private int currentPhase = 0;
	private int phaseIndex = 0;
	private int phaseSize = DEFAULT_PHASE_NO;
	private int phaseNumber = phaseSize;
	private boolean isDoublePhase = false;
	private boolean animation = false;

	private long millisPerPhase = 24 * 60 * 60 * 1000;
	private long nextFrame;
	private long nextPhaseShift = Long.MAX_VALUE;
	private boolean isRepaintRequested = false;

	private Icon[] backgroundIcons;
	private int[] backgroundIconInfo;
	private int iconCount;

	private AgentView[] agentViews = new AgentView[15];
	private int participants;
	private int lastTimeUnit = 0;

	// Since only the event dispatch thread paints when using Swing it is
	// safe to reuse this array instead of recreating them each paint.
	private int[] xp = new int[4];
	private int[] yp = new int[4];

	private ViewerPanel viewerPanel;

	private boolean isRunning;

	public SimulationPanel(ViewerPanel viewerPanel) {
		super(null);
		this.viewerPanel = viewerPanel;
		setLayout(new SimulationLayout(this, SimulationLayout.Y_AXIS, 50, 2));
		setBackground(Color.black);
	}

	public boolean isDoublePhase() {
		return isDoublePhase;
	}

	public void setDoublePhase(boolean isDoublePhase) {
		this.isDoublePhase = isDoublePhase;
		if (isDoublePhase) {
			phaseSize = phaseNumber * 2;
			ensureConnectionCapacity(phaseSize);
		} else {
			phaseIndex = 0;
		}
	}

	public int getPhaseNumber() {
		return phaseNumber;
	}

	public void setPhaseNumber(int phaseNumber) {
		if (phaseNumber < 1) {
			throw new IllegalArgumentException("phase number must be positive");
		}
		this.phaseNumber = phaseNumber;
		this.phaseIndex = 0;
		this.phaseSize = isDoublePhase ? phaseNumber * 2 : phaseNumber;
		ensureConnectionCapacity(phaseSize);
	}

	private void ensureConnectionCapacity(int size) {
		if (connectionCount.length < size) {
			connectionCount = ArrayUtils.setSize(connectionCount, size);
			connections = (int[][]) ArrayUtils.setSize(connections, size);
		} else {
			for (int i = size, n = connectionCount.length; i < n; i++) {
				connectionCount[i] = 0;
			}
		}
	}

	public synchronized void addIcon(Icon icon, int dx, int dy) {
		if (backgroundIcons == null) {
			backgroundIcons = new Icon[2];
			backgroundIconInfo = new int[2];
		} else if (iconCount == backgroundIcons.length) {
			backgroundIcons = (Icon[]) ArrayUtils.setSize(backgroundIcons,
					iconCount + 5);
			backgroundIconInfo = ArrayUtils.setSize(backgroundIconInfo,
					iconCount + 5);
		}
		if (dx != LEFT && dx != CENTER && dx != RIGHT) {
			throw new IllegalArgumentException("illegal dx: " + dx);
		}
		if (dy != LEFT && dy != CENTER && dy != RIGHT) {
			throw new IllegalArgumentException("illegal dy: " + dy);
		}
		backgroundIcons[iconCount] = icon;
		backgroundIconInfo[iconCount++] = (dx << 8) | dy;
	}

	public AgentView getAgentView(int agentID) {
		return agentID < participants ? agentViews[agentID] : null;
	}

	public String getAgentName(int agentIndex) {
		AgentView view = getAgentView(agentIndex);
		return view != null ? view.getName() : Integer.toString(agentIndex);
	}

	public int getHighestAgentIndex() {
		return participants;
	}

	public void addAgentView(AgentView view, int index, String name, int role,
			String roleName, int container) {
		if (agentViews.length <= index) {
			agentViews = (AgentView[]) ArrayUtils.setSize(agentViews,
					index + 10);
		}
		if (participants <= index) {
			participants = index + 1;
		}
		view.init(this, index, name, role, roleName);
		agentViews[index] = view;
		add(view, new Integer(container));
	}

	public void removeAgentView(AgentView view) {
		int id = view.getIndex();
		if (id < participants) {
			agentViews[id] = null;
		}
		remove(view);
	}

	/*********************************************************************
	 * setup and time handling
	 **********************************************************************/

	public void simulationStarted(long startTime, long endTime,
			int timeUnitCount) {
		// Clear any old items before start a new simulation
		clear();

		if (timeUnitCount < 1)
			timeUnitCount = 1;
		this.millisPerPhase = (endTime - startTime)
				/ (timeUnitCount * phaseNumber);
		if (this.millisPerPhase < 100) {
			this.millisPerPhase = 100;
		}
		long currentTime = viewerPanel.getServerTime();
		phaseIndex = 0;
		if (currentTime > startTime) {
			// The simulation has already been started and we must modify
			// the nextPhaseShift accordingly
			int currentPhase = (int) ((currentTime - startTime) / this.millisPerPhase);
			setPhase(currentPhase % phaseSize);
			this.nextPhaseShift = startTime + (currentPhase + 1)
					* this.millisPerPhase;
		} else {
			setPhase(0);
			this.nextPhaseShift = startTime + this.millisPerPhase;
		}

		if (!isRunning) {
			viewerPanel.addTickListener(this);
			isRunning = true;
		}
	}

	public void simulationStopped() {
		isRunning = false;
		viewerPanel.removeTickListener(this);
		nextPhaseShift = Long.MAX_VALUE;

		// Clear any connections
		for (int i = 0; i < phaseSize; i++) {
			connectionCount[i] = 0;
		}
		animation = false;
		repaint();
	}

	public void clear() {
		nextPhaseShift = Long.MAX_VALUE;

		// Clear any connections
		for (int i = 0; i < phaseSize; i++) {
			connectionCount[i] = 0;
		}

		int participants = this.participants;
		this.participants = 0;
		for (int i = 0, n = participants; i < n; i++) {
			agentViews[i] = null;
		}
		// This must be done with event dispatch thread. FIX THIS!!!
		removeAll();
		repaint();
	}

	public void nextTimeUnit(int timeUnit) {
		if (isDoublePhase) {
			phaseIndex = (timeUnit % 2) * phaseNumber;
		}
	}

	/*********************************************************************
	 * TickListener interface
	 **********************************************************************/

	public void tick(long serverTime) {
		if (serverTime >= nextPhaseShift) {
			nextPhaseShift += millisPerPhase;
			setPhase((currentPhase + 1) % phaseSize);

			for (int i = 0; i < participants; i++) {
				AgentView view = agentViews[i];
				if (view != null) {
					view.nextPhase(currentPhase);
				}
			}

			repaint();
		}
	}

	public void simulationTick(long serverTime, int timeUnit) {
		if (timeUnit != lastTimeUnit) {
			lastTimeUnit = timeUnit;
			for (int i = 0; i < participants; i++) {
				AgentView view = agentViews[i];
				if (view != null) {
					view.nextTimeUnit(serverTime, timeUnit);
				}
			}
		}

		if (isRepaintRequested) {
			repaint();
			isRepaintRequested = false;
		}
	}

	/*********************************************************************
	 * Phase and connection handling
	 **********************************************************************/

	public void setPhase(int phase) {
		if (phase >= phaseSize) {
			throw new IllegalArgumentException("phase: " + phase
					+ ", phaseNumber: " + phaseNumber + ", phaseSize: "
					+ phaseSize);
		}

		// Clear the old phase...
		connectionCount[this.currentPhase] = 0;
		this.currentPhase = phase;
		animation = true;
		nextFrame = 0;
	}

	public void addConnection(AgentView fromView, AgentView toView, int phase,
			int type) {
		int height = getHeight();
		int x = toView.getX() + toView.getWidth() / 2;
		int y = toView.getY() + toView.getHeight() / 2;
		Point from = fromView.getConnectionPoint(type, x, y, false);
		int x1 = from.x;
		int y1 = from.y;
		from = toView.getConnectionPoint(type, x1, y1, true, from);
		if (height > 0) {
			from.y += ((y1 - y) * toView.getHeight()) / (2 * height);
		}
		addConnection(x1, y1, from.x, from.y, phase, type);
	}

	private void addConnection(int x1, int y1, int x2, int y2, int phase,
			int type) {
		phase += phaseIndex;

		int index = connectionCount[phase] * PARTS;
		int[] conns = connections[phase];
		if (conns == null) {
			conns = connections[phase] = new int[index + 5 * PARTS];
		} else if (index >= conns.length) {
			conns = connections[phase] = ArrayUtils.setSize(conns, index + 5
					* PARTS);
		}

		conns[index + X1] = x1;
		conns[index + Y1] = y1;
		conns[index + X2] = x2;
		conns[index + Y2] = y2;
		// System.out.println("ADDING CONNECTION " + connectionCount[phase] +
		// " TO x="
		// + from.x + ',' + from.y + " of type " + type + " for phase " +
		// phase);
		conns[index + TYPE] = type;
		conns[index + ANIMATION_POS] = 0;

		connectionCount[phase]++;
		animation = true;
		requestRepaint();
	}

	/*********************************************************************
	 * API towards the agent views
	 **********************************************************************/

	ConfigManager getConfig() {
		return viewerPanel.getConfig();
	}

	Icon getIcon(String name) {
		return viewerPanel.getIcon(name);
	}

	void showDialog(JComponent dialog) {
		viewerPanel.showDialog(dialog);
	}

	/*********************************************************************
	 * Paint handling
	 **********************************************************************/

	protected void paintComponent(Graphics g) {
		Color oldColor = g.getColor();
		int width = getWidth();
		int height = getHeight();
		if (isOpaque()) {
			// Clear the panel
			g.setColor(getBackground());
			g.fillRect(0, 0, width, height);
		}

		for (int i = 0; i < iconCount; i++) {
			int info = backgroundIconInfo[i];
			int dx = (info >> 8);
			int dy = info & 0xff;
			Icon icon = backgroundIcons[i];
			if (dx == LEFT) {
				dx = 0;
			} else if (dx == RIGHT) {
				dx = width - icon.getIconWidth() - 1;
			} else {
				dx = (width - icon.getIconWidth()) / 2;
			}
			if (dy == LEFT) {
				dy = 0;
			} else if (dy == RIGHT) {
				dy = height - icon.getIconHeight() - 1;
			} else {
				dy = (height - icon.getIconHeight()) / 2;
			}
			icon.paintIcon(this, g, dx, dy);
		}
		updateLines(g);
		g.setColor(oldColor);
	}

	private void updateLines(Graphics g) {
		Color[] colors = null;
		int[] conns = connections[currentPhase];
		int maxAnim = arrowAnim.length;
		boolean newFrame = false;
		if (animation) {
			long currentTime = System.currentTimeMillis();
			if (nextFrame < currentTime) {
				newFrame = true;
				nextFrame = currentTime + 100;
				animation = false;
			}
		}

		for (int i = 0, n = connectionCount[currentPhase] * PARTS; i < n; i += PARTS) {
			int type = conns[i + TYPE];

			int x1 = conns[i + X1];
			int x2 = conns[i + X2];
			int y1 = conns[i + Y1];
			int y2 = conns[i + Y2];

			int xl = x1 - x2;
			int yl = y1 - y2;
			double len = Math.sqrt(xl * xl + yl * yl);
			double dx = (x1 - x2) / len;
			double dy = (y1 - y2) / len;

			int animPos = conns[i + ANIMATION_POS];
			int intensity = arrowAnim[animPos];

			if (newFrame && animPos < maxAnim - 1) {
				conns[i + ANIMATION_POS]++;
				animation = true;
			}

			// This is under the assumption that we are not going to
			// show both MESSAGE and TRANSPORTS at the same time.
			switch (type) {
			case TYPE_YELLOW:
				colors = FADING_YELLOW;
				break;
			case TYPE_GREEN:
				colors = FADING_GREENS;
				break;
			default:
				colors = FADING_BLUES;
			}

			g.setColor(colors[intensity]);

			makeArrow(x1, y1, x2, y2, 4, 2, dx, dy, xp, yp);
			g.fillPolygon(xp, yp, 4);
			makeArrow(x2, y2, x2 - (int) (dx * 10), y2 - (int) (dy * 10), 6, 1,
					dx, dy, xp, yp);
			g.fillPolygon(xp, yp, 4);

			g.setColor(colors[intensity + 1]);

			x1 = x1 - (int) (dx * 4);
			x2 = x2 - (int) (dx * 2);
			y1 = y1 - (int) (dy * 4);
			y2 = y2 - (int) (dy * 2);

			makeArrow(x1, y1, x2, y2, 2, 1, dx, dy, xp, yp);
			g.fillPolygon(xp, yp, 4);
			makeArrow(x2, y2, x2 - (int) (dx * 6), y2 - (int) (dy * 6), 5, 1,
					dx, dy, xp, yp);
			g.fillPolygon(xp, yp, 4);
		}
		if (animation) {
			requestRepaint();
		}
	}

	private void makeArrow(int x1, int y1, int x2, int y2, int width1,
			int width2, double dx, double dy, int[] xp, int[] yp) {

		xp[0] = x1 + (int) (dy * width1);
		yp[0] = y1 - (int) (dx * width1);

		xp[1] = x1 - (int) (dy * width1);
		yp[1] = y1 + (int) (dx * width1);

		xp[3] = x2 + (int) (dy * width2);
		yp[3] = y2 - (int) (dx * width2);
		xp[2] = x2 - (int) (dy * width2);
		yp[2] = y2 + (int) (dx * width2);
	}

	private void requestRepaint() {
		isRepaintRequested = true;
	}

} // SimulationPanel
