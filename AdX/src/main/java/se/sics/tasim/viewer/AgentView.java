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
 * AgentView
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Dec 03 17:36:21 2002
 * Updated : $Date: 2004-10-28 14:24:41 -0500 (Thu, 28 Oct 2004) $
 *           $Revision: 1057 $
 */
package se.sics.tasim.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;

public abstract class AgentView extends JComponent {

	private static final Logger log = Logger.getLogger(AgentView.class
			.getName());

	public final static int X_AXIS = 1;
	public final static int Y_AXIS = 2;
	public final static int BOTH_AXIS = 3;

	private SimulationPanel parent;
	private int index;
	private String name;
	private int role;

	private String roleName;

	private Icon agentIcon;

	private int minWidth = 0, minHeight = 0;

	private int axis = BOTH_AXIS;
	private int connectionDistance = 5;

	public AgentView() {
		setOpaque(true);
	}

	final void init(SimulationPanel parent, int index, String name, int role,
			String roleName) {
		if (this.name != null) {
			throw new IllegalStateException("already initialized");
		}
		this.parent = parent;
		this.index = index;
		this.name = name;
		this.role = role;
		this.roleName = roleName;

		String iconName = getConfigProperty("image");
		Icon icon;
		if ((iconName != null) && ((icon = getIcon(iconName)) != null)) {
			// Set the background icon to use for this component. Setting
			// this means that no layout manager will be used.
			setIcon(icon);
		}

		initialized();

		// Do the actual layout
		if (getLayout() == null) {
			if (minWidth > 0 && minHeight > 0) {
				Dimension size = new Dimension(minWidth, minHeight);
				setPreferredSize(size);
				setMinimumSize(size);
			}
		}

		addMouseListener(new MouseAdapter() {
			// Should be fixed not to react on click but on mousePressed and
			// mouseReleased within the same area. FIX THIS!!! FIX THIS!!!
			public void mouseClicked(MouseEvent mouseEvent) {
				Object source = mouseEvent.getSource();
				if ((source == AgentView.this) && !handleMenu(mouseEvent)
						&& SwingUtilities.isLeftMouseButton(mouseEvent)) {
					JComponent dialog = getDialog();
					if (dialog != null) {
						showDialog(dialog);
					}
				}
			}

			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.getSource() == AgentView.this) {
					handleMenu(mouseEvent);
				}
			}

			public void mouseReleased(MouseEvent mouseEvent) {
				if (mouseEvent.getSource() == AgentView.this) {
					handleMenu(mouseEvent);
				}
			}
		});
	}

	protected boolean handleMenu(MouseEvent event) {
		return false;
	}

	protected abstract void initialized();

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public int getRole() {
		return role;
	}

	public String getRoleName() {
		return roleName;
	}

	public Icon getIcon() {
		return agentIcon;
	}

	public void setIcon(Icon agentIcon) {
		this.agentIcon = agentIcon;
		int width = agentIcon.getIconWidth();
		int height = agentIcon.getIconHeight();
		if (width > minWidth) {
			minWidth = width;
		}
		if (height > minHeight) {
			minHeight = height;
		}
	}

	public int getConnectionAxis() {
		return axis;
	}

	public void setConnectionAxis(int axis) {
		this.axis = axis;
	}

	public int getConnectionDistance() {
		return connectionDistance;
	}

	public void setConnectionDistance(int connectionDistance) {
		this.connectionDistance = connectionDistance;
	}

	public Point getConnectionPoint(int type, int x, int y, boolean isTarget) {
		return getConnectionPoint(type, x, y, isTarget, null);
	}

	public Point getConnectionPoint(int type, int toX, int toY,
			boolean isTarget, Point cache) {
		if (cache == null) {
			cache = new Point();
		}
		int x = getX();
		int y = getY();
		int width = getWidth();
		int height = getHeight();

		if (axis == Y_AXIS) {
			cache.x = x + width / 2;
		} else if (toX < x) {
			cache.x = x - connectionDistance;
		} else if (toX > (x + width)) {
			cache.x = x + width + connectionDistance;
		} else {
			cache.x = x + width / 2;
		}
		if (axis == X_AXIS) {
			cache.y = y + height / 2;
		} else if (toY < y) {
			cache.y = y - connectionDistance;
		} else if (toY > (y + height)) {
			cache.y = y + height + connectionDistance;
		} else {
			cache.y = y + height / 2;
		}

		return cache;
	}

	/*********************************************************************
	 * Information retrieval and utilities for sub classes
	 **********************************************************************/

	protected ConfigManager getConfig() {
		return parent.getConfig();
	}

	protected Icon getIcon(String iconName) {
		return parent.getIcon(iconName);
	}

	protected String getConfigProperty(String prop) {
		return getConfigProperty(prop, null);
	}

	protected String getConfigProperty(String prop, String defaultValue) {
		ConfigManager config = parent.getConfig();
		String value = config.getProperty(roleName + '.' + getName() + '.'
				+ prop);
		if (value == null) {
			value = config.getProperty(roleName + '.' + prop);
		}
		return value == null ? defaultValue : value;
	}

	protected Point getConfigPoint(String name) {
		return getConfigPoint(name, null);
	}

	protected Point getConfigPoint(String name, Point point) {
		String value = getConfigProperty(name + ".location");
		// Parse the point
		int index;
		if (value != null && ((index = value.indexOf(',')) > 0)) {
			try {
				int x = Integer.parseInt(value.substring(0, index).trim());
				int y = Integer.parseInt(value.substring(index + 1).trim());
				if (point == null) {
					point = new Point(x, y);
				} else {
					point.setLocation(x, y);
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not parse point " + roleName + '.'
						+ getName() + '.' + name + ": " + value, e);
			}
		}
		return point;
	}

	protected Rectangle getConfigBounds(String name) {
		return getConfigBounds(name, null);
	}

	protected Rectangle getConfigBounds(String name, Rectangle bounds) {
		String value = getConfigProperty(name + ".bounds");
		// Parse the point
		if (value != null) {
			try {
				int index = value.indexOf(',');
				int x = Integer.parseInt(value.substring(0, index).trim());
				int index2 = value.indexOf(',', index + 1);
				int y = Integer.parseInt(value.substring(index + 1, index2)
						.trim());
				index = value.indexOf(',', index2 + 1);
				int width = Integer.parseInt(value.substring(index2 + 1, index)
						.trim());
				int height = Integer
						.parseInt(value.substring(index + 1).trim());

				if (bounds == null) {
					bounds = new Rectangle(x, y, width, height);
				} else {
					bounds.setBounds(x, y, width, height);
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not parse bounds " + roleName
						+ '.' + getName() + '.' + name + ": " + value, e);
			}
		}
		return bounds;
	}

	protected Color getConfigColor(String name, String sub) {
		return getConfigColor(name, sub, null);
	}

	protected Color getConfigColor(String name, String sub, Color defaultColor) {
		String value = getConfigProperty(name + '.' + sub);
		if (value != null) {
			try {
				int radix = 10;
				char c = value.charAt(0);
				if (c == '#' || c == '$') {
					value = value.substring(1);
					radix = 16;
				} else if (c == '0' && value.charAt(1) == 'x') {
					value = value.substring(2);
					radix = 16;
				}
				int colorValue = Integer.parseInt(value, radix);
				return new Color(colorValue);
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not parse color " + roleName + '.'
						+ getName() + '.' + name + '.' + sub + ": " + value, e);
			}
		}
		return defaultColor;
	}

	protected void layoutComponent(String name, JComponent c) {
		Color col = getConfigColor(name, "foreground");
		if (col != null) {
			c.setForeground(col);
		}
		col = getConfigColor(name, "background");
		if (col != null) {
			c.setBackground(col);
		}
		String opaque = getConfigProperty(name + ".opaque");
		if (opaque != null) {
			c.setOpaque("true".equalsIgnoreCase(opaque));
		}

		if (getLayout() == null) {
			Rectangle bounds = getConfigBounds(name);
			if (bounds == null) {
				// No bounds for this component => use layout manager
				log.warning("no bounds for " + roleName + '.' + getName() + '.'
						+ name + ": reverting to layout manager");
				BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
				setLayout(layout);
			} else {
				c.setBounds(bounds);
				if ((bounds.x + bounds.width) > minWidth) {
					minWidth = bounds.x + bounds.width;
				}
				if (bounds.y + bounds.height > minHeight) {
					minHeight = bounds.y + bounds.height;
				}
			}
		}
		add(c);
	}

	protected JScrollPane createScrollPane(JComponent component) {
		return createScrollPane(component, null, false);
	}

	protected JScrollPane createScrollPane(JComponent component, String title) {
		return createScrollPane(component, title, false);
	}

	protected JScrollPane createScrollPane(JComponent component, String title,
			boolean horizontalScrollbar) {
		JScrollPane scrollPane = new JScrollPane(
				component,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				horizontalScrollbar ? JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
						: JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		Color color = component.getBackground();
		if (color == null) {
			color = Color.white;
		}
		scrollPane.setBackground(color);
		scrollPane.getViewport().setBackground(color);
		if (title != null) {
			scrollPane.setBorder(BorderFactory.createTitledBorder(title));
		}

		JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
		scrollBar.setPreferredSize(new Dimension(10, scrollBar.getHeight()));
		if (horizontalScrollbar) {
			scrollBar = scrollPane.getHorizontalScrollBar();
			scrollBar.setPreferredSize(new Dimension(scrollBar.getWidth(), 10));
		}
		return scrollPane;
	}

	protected void showDialog(JComponent dialog) {
		parent.showDialog(dialog);
	}

	protected JComponent getDialog() {
		return null;
	}

	/*********************************************************************
	 * Size handling
	 **********************************************************************/

	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		if (d.width < minWidth) {
			d.width = minWidth;
		}
		if (d.height < minHeight) {
			d.height = minHeight;
		}
		return d;
	}

	public Dimension getMinimumSize() {
		Dimension d = super.getMinimumSize();
		if (d.width < minWidth) {
			d.width = minWidth;
		}
		if (d.height < minHeight) {
			d.height = minHeight;
		}
		return d;
	}

	/*********************************************************************
	 * Paint handling
	 **********************************************************************/

	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			Color originalColor = g.getColor();
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(originalColor);
		}

		if (agentIcon != null) {
			int iconWidth = agentIcon.getIconWidth();
			int iconHeight = agentIcon.getIconHeight();
			agentIcon.paintIcon(this, g, (getWidth() - iconWidth) / 2,
					(getHeight() - iconHeight) / 2);
		}
	}

	// -------------------------------------------------------------------
	// The information methods, called when something has changed/happened
	// -------------------------------------------------------------------

	public abstract void dataUpdated(int type, int value);

	public abstract void dataUpdated(int type, long value);

	public abstract void dataUpdated(int type, float value);

	public abstract void dataUpdated(int type, String value);

	public abstract void dataUpdated(int type, Transportable value);

	/**
	 * Called when the simulation panel enters a new phase
	 * 
	 * @param phase
	 *            the current simulation phase
	 */
	protected void nextPhase(int phase) {
	}

	/**
	 * Called when a new simulation day starts (if the simulation supports the
	 * day notion).
	 * 
	 * @param serverTime
	 *            the current server time
	 * @param timeUnit
	 *            the current simulation date
	 */
	protected void nextTimeUnit(long serverTime, int timeUnit) {
	}

} // AgentView
