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
 * SimulationLayout
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Dec 05 16:49:09 2002
 * Updated : $Date: 2004-10-28 14:24:41 -0500 (Thu, 28 Oct 2004) $
 *           $Revision: 1057 $
 * Purpose :
 *
 */
package se.sics.tasim.viewer;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Point;

import com.botbox.util.ArrayUtils;

public class SimulationLayout implements LayoutManager2 {

	public final static int X_AXIS = 0;
	public final static int Y_AXIS = 1;

	private Component[][] containers = null;

	private Container target;
	private boolean isVerticalLayout;
	private int hgap;
	private int vgap;

	public SimulationLayout(Container target, int axis) {
		this(target, axis, 5, 20);
	}

	public SimulationLayout(Container target, int axis, int hgap, int vgap) {
		if (axis == Y_AXIS) {
			isVerticalLayout = true;
		} else if (axis != X_AXIS) {
			throw new IllegalArgumentException("axis must be X_AXIS or Y_AXIS");
		}
		this.target = target;
		this.hgap = hgap;
		this.vgap = vgap;
	}

	protected int getContainerCount() {
		return containers == null ? 0 : containers.length;
	}

	public int getAxis() {
		return isVerticalLayout ? Y_AXIS : X_AXIS;
	}

	public int getVgap() {
		return vgap;
	}

	public void setVgap(int vgap) {
		this.vgap = vgap;
	}

	public int getHgap() {
		return hgap;
	}

	public void setHgap(int hgap) {
		this.hgap = hgap;
	}

	public void addLayoutComponent(Component comp, Object constraints) {
		if (constraints instanceof String) {
			addLayoutComponent((String) constraints, comp);
		} else if (constraints instanceof Integer) {
			addLayoutComponent(comp, ((Integer) constraints).intValue());
		} else if (constraints != null) {
			throw new IllegalArgumentException(
					"cannot add to layout: constraints must be an Integer");
		} else {
			addLayoutComponent(comp, 0);
		}
	}

	public void addLayoutComponent(String name, Component comp) {
		try {
			addLayoutComponent(comp, Integer.parseInt(name));
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"cannot add to layout: constraints must be an Integer");
		}
	}

	private void addLayoutComponent(Component comp, int index) {
		synchronized (comp.getTreeLock()) {
			if (containers == null) {
				containers = new Component[index + 1][];
			} else if (index >= containers.length) {
				containers = (Component[][]) ArrayUtils.setSize(containers,
						index + 1);
			}
			containers[index] = (Component[]) ArrayUtils.add(Component.class,
					containers[index], comp);
		}
	}

	public void removeLayoutComponent(Component comp) {
		synchronized (comp.getTreeLock()) {
			for (int i = 0, n = getContainerCount(); i < n; i++) {
				Component[] cont = containers[i];
				int index = ArrayUtils.indexOf(cont, comp);
				if (index >= 0) {
					cont = (Component[]) ArrayUtils.remove(cont, index);
					if (cont == null && (i == (n - 1))) {
						// The last component in the top container has been
						// removed and we also need to remove the empty
						// container.
						containers = (Component[][]) ArrayUtils.setSize(
								containers, i);

					} else {
						containers[i] = cont;
					}
					break;
				}
			}
		}
	}

	public Dimension preferredLayoutSize(Container parent) {
		if (parent != target) {
			throw new IllegalArgumentException("this layout can not be shared");
		}
		synchronized (parent.getTreeLock()) {
			int totalWidth = 0;
			int totalHeight = 0;
			int contNumber = getContainerCount();
			if (contNumber > 0) {
				for (int i = 0; i < contNumber; i++) {
					Component[] r = containers[i];
					if (r != null) {
						int maxWidth = 0;
						int maxHeight = 0;
						int compNumber = r.length;
						for (int j = 0; j < compNumber; j++) {
							Dimension cd = r[j].getPreferredSize();
							if (cd.width > maxWidth) {
								maxWidth = cd.width;
							}
							if (cd.height > maxHeight) {
								maxHeight = cd.height;
							}
						}
						if (isVerticalLayout) {
							maxHeight = maxHeight * compNumber + vgap
									* (compNumber - 1);
						} else {
							maxWidth = maxWidth * compNumber + hgap
									* (compNumber - 1);
						}
						if (maxWidth > totalWidth) {
							totalWidth = maxWidth;
						}
						if (maxHeight > totalHeight) {
							totalHeight = maxHeight;
						}
					}
				}
				if (isVerticalLayout) {
					totalWidth = totalWidth * contNumber + hgap
							* (contNumber - 1);
				} else {
					totalHeight = totalHeight * contNumber + vgap
							* (contNumber - 1);
				}
			}

			Insets insets = parent.getInsets();
			return new Dimension(totalWidth + insets.left + insets.right,
					totalHeight + insets.top + insets.bottom);
		}
	}

	public Dimension minimumLayoutSize(Container parent) {
		if (parent != target) {
			throw new IllegalArgumentException("this layout can not be shared");
		}
		synchronized (parent.getTreeLock()) {
			int totalWidth = 0;
			int totalHeight = 0;
			int contNumber = getContainerCount();
			if (contNumber > 0) {
				for (int i = 0; i < contNumber; i++) {
					Component[] r = containers[i];
					if (r != null) {
						int maxWidth = 0;
						int maxHeight = 0;
						int compNumber = r.length;
						for (int j = 0; j < compNumber; j++) {
							Dimension cd = r[j].getMinimumSize();
							if (cd.width > maxWidth) {
								maxWidth = cd.width;
							}
							if (cd.height > maxHeight) {
								maxHeight = cd.height;
							}
						}
						if (isVerticalLayout) {
							maxHeight = maxHeight * compNumber + vgap
									* (compNumber - 1);
						} else {
							maxWidth = maxWidth * compNumber + hgap
									* (compNumber - 1);
						}
						if (maxWidth > totalWidth) {
							totalWidth = maxWidth;
						}
						if (maxHeight > totalHeight) {
							totalHeight = maxHeight;
						}
					}
				}
				if (isVerticalLayout) {
					totalWidth = totalWidth * contNumber + hgap
							* (contNumber - 1);
				} else {
					totalHeight = totalHeight * contNumber + vgap
							* (contNumber - 1);
				}
			}

			Insets insets = parent.getInsets();
			return new Dimension(totalWidth + insets.left + insets.right,
					totalHeight + insets.top + insets.bottom);
		}
	}

	public Dimension maximumLayoutSize(Container parent) {
		if (parent != target) {
			throw new IllegalArgumentException("this layout can not be shared");
		}
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public float getLayoutAlignmentX(Container parent) {
		return 0.5f;
	}

	public float getLayoutAlignmentY(Container target) {
		return 0.5f;
	}

	public void layoutContainer(Container parent) {
		if (parent != target) {
			throw new IllegalArgumentException("this layout can not be shared");
		}
		synchronized (parent.getTreeLock()) {
			int contNumber = getContainerCount();
			;
			if (contNumber > 0) {
				Insets insets = parent.getInsets();
				int x = insets.left;
				int y = insets.top;
				int height = parent.getHeight() - y - insets.bottom;
				int width = parent.getWidth() - x - insets.right;
				if (isVerticalLayout) {
					int compWidth = (width - hgap * (contNumber - 1))
							/ contNumber;
					for (int i = 0, n = contNumber; i < n; i++) {
						Component[] r = containers[i];
						if (r != null) {
							int compNumber = r.length;
							int tempY = y;
							int compHeight = (height - vgap * (compNumber - 1))
									/ compNumber;
							for (int j = 0; j < compNumber; j++) {
								Component c = r[j];
								Dimension d = c.getPreferredSize();
								if (d.width > compWidth) {
									d.width = compWidth;
								}
								if (d.height > compHeight) {
									d.height = compHeight;
								}
								c.setBounds(x + ((compWidth - d.width) >> 1),
										tempY + ((compHeight - d.height) >> 1),
										d.width, d.height);
								tempY += compHeight + vgap;
							}
						}
						x += compWidth + hgap;
					}
				} else {
					int compHeight = (height - vgap * (contNumber - 1))
							/ contNumber;
					for (int i = 0, n = contNumber; i < n; i++) {
						Component[] r = containers[i];
						if (r != null) {
							int compNumber = r.length;
							int tempX = x;
							int compWidth = (width - hgap * (compNumber - 1))
									/ compNumber;
							for (int j = 0; j < compNumber; j++) {
								Component c = r[j];
								Dimension d = c.getPreferredSize();
								if (d.width > compWidth) {
									d.width = compWidth;
								}
								if (d.height > compHeight) {
									d.height = compHeight;
								}
								c.setBounds(tempX
										+ ((compWidth - d.width) >> 1), y
										+ ((compHeight - d.height) >> 1),
										d.width, d.height);
								tempX += compHeight + hgap;
							}
						}
						y += compHeight + vgap;
					}
				}
			}
		}
	}

	public void invalidateLayout(Container target) {
	}

}
