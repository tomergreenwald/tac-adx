/**
 * SICS ISL Java Utilities
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
 * BarDiagram
 * A simple bar diagram
 *
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Sun Feb 02 13:15:02 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 * Purpose :
 *
 */
package se.sics.isl.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class BarDiagram extends JComponent {

	private static final double PI2 = 3.141592 / 2d;

	private int[] data;
	private int maxData;
	private int minData;
	private double factor;
	private int xspace;
	private int xfill;
	private boolean rescale = false;

	private int sizeX;
	private int sizeY;
	private int lowerY;

	private Color barColor;
	private Color leftColor;
	private Color rightColor;
	private Color[] allColors;

	private boolean isShowingValue = false;
	private Color inValueColor = Color.white;
	private Color outValueColor = Color.black;

	// Cache to avoid creating new insets objects for each repaint. Is
	// created when first needed.
	private Insets insets;

	private String[] names;

	public BarDiagram() {
		setOpaque(true);
		setBackground(Color.white);
	}

	public void setToolTipVisible(boolean showToolTip) {
		setToolTipText(showToolTip ? "" : null);
	}

	/**
	 * Sets the names for the bars. The name and values will be shown as
	 * tooltips if tooltips are turned on.
	 * 
	 * @param names
	 *            the names of the bars or <CODE>null</CODE> to clear the names
	 */
	public void setNames(String[] names) {
		this.names = names;
	}

	public void setData(int[] data) {
		maxData = Integer.MIN_VALUE;
		minData = Integer.MAX_VALUE;

		if (data != null) {
			for (int i = 0, n = data.length; i < n; i++) {
				if (maxData < data[i])
					maxData = data[i];
				if (minData > data[i])
					minData = data[i];
			}
		}

		if (minData > 0) {
			minData = 0;
		}
		if (maxData < minData) {
			maxData = minData;
		}

		this.data = data;
		rescale = true;

		setupColors();
		repaint();
	}

	public String getToolTipText(MouseEvent event) {
		// Only show tool tips if some data exists
		if (xspace == 0) {
			return null;
		}

		String[] names = this.names;
		int[] data = this.data;

		int index = event.getX() / xspace;
		int nameLen = names == null ? 0 : names.length;
		int dataLen = data == null ? 0 : data.length;
		if (dataLen >= nameLen) {
			if (index >= dataLen) {
				index = dataLen - 1;
			}

		} else if (index >= nameLen) {
			index = nameLen - 1;
		}

		if (index < 0) {
			// Neither name or value exists to be displayed
			return null;
		}

		String name = index >= nameLen ? "Value =" : names[index];
		int value = index >= dataLen ? 0 : data[index];

		return name + ' ' + value;
	}

	public void setBarColor(Color color) {
		barColor = color;
		leftColor = null;
		rightColor = null;
		allColors = null;
	}

	public void setBarColors(Color leftColor, Color rightColor) {
		this.leftColor = leftColor;
		this.rightColor = rightColor;
		allColors = null;
		setupColors();
	}

	private void setupColors() {
		if (leftColor != null && data != null) {
			if (allColors != null && allColors.length == data.length) {
				return;
			}
			float len = data.length;
			float r0 = leftColor.getRed() / len;
			float r1 = rightColor.getRed() / len;
			float g0 = leftColor.getGreen() / len;
			float g1 = rightColor.getGreen() / len;
			float b0 = leftColor.getBlue() / len;
			float b1 = rightColor.getBlue() / len;
			allColors = new Color[data.length];
			for (int i = 0, n = data.length; i < n; i++) {
				allColors[i] = new Color((int) (r0 * (n - i) + r1 * i),
						(int) (g0 * (n - i) + g1 * i), (int) (b0 * (n - i) + b1
								* i));
			}
		}
	}

	public boolean isShowingValue() {
		return isShowingValue;
	}

	public void setShowingValue(boolean isShowingValue) {
		if (this.isShowingValue != isShowingValue) {
			this.isShowingValue = isShowingValue;
			repaint();
		}
	}

	public void setValueColor(Color color) {
		this.outValueColor = this.inValueColor = color;
	}

	public void setValueColors(Color inBarColor, Color outBarColor) {
		this.inValueColor = inBarColor;
		this.outValueColor = outBarColor;
	}

	protected void paintComponent(Graphics g) {
		Color oldColor = g.getColor();
		int width = getWidth();
		int height = getHeight();

		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, width, height);
		}

		insets = getInsets(insets);
		int x = insets.left;
		int y = insets.top;
		width = width - insets.left - insets.right;
		height = height - insets.top - insets.bottom;

		if (width != sizeX || height != sizeY || rescale) {
			// Rescale!!!
			rescale = false;

			sizeY = height;
			sizeX = width;

			lowerY = sizeY - 3;
			if (minData == maxData) {
				factor = 1;
			} else {
				factor = (sizeY - 15) / (double) (maxData - minData);
			}

			xspace = data == null ? 2 : sizeX / data.length;
			xfill = xspace - 2;
		}

		g.setColor(Color.black);
		g.drawLine(x, y + lowerY, x + sizeX, y + lowerY);

		if (data == null) {
			g.setColor(oldColor);
			return;
		}

		int[] drawData = data;
		if (allColors == null) {
			if (barColor != null)
				g.setColor(barColor);
			else
				g.setColor(getForeground());
		}
		for (int i = 0, n = drawData.length; i < n; i++) {
			int hei = (int) (factor * drawData[i]);
			if (allColors != null) {
				g.setColor(allColors[i]);
			}
			g.fillRect(x + i * xspace, y + lowerY - hei, xfill, hei);
		}

		g.setColor(Color.black);
		for (int i = 0, n = drawData.length; i < n; i++) {
			int hei = (int) (factor * drawData[i]);
			g.drawRect(x + i * xspace, y + lowerY - hei, xfill, hei);
		}

		if (isShowingValue && g instanceof Graphics2D) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.rotate(-PI2);
			for (int i = 0, n = drawData.length; i < n; i++) {
				if (drawData[i] > 0) {
					int py = (int) (factor * drawData[i]) - y - lowerY;
					if (py > -50) {
						py = -80;
						g.setColor(inValueColor);
					} else {
						g.setColor(outValueColor);
					}
					g2d.drawString(Integer.toString(drawData[i]), py, x + i
							* xspace + 4 + xspace / 2);
				}
			}
			// restore the original non-rotation
			g2d.rotate(PI2);
		}

		g.setColor(oldColor);
	}

	/*****************************************************************************
	 * Test Main
	 ****************************************************************************/

	public static void main(String[] args) throws Exception {
		JFrame jf = new JFrame("test");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BarDiagram bd = new BarDiagram();
		bd.setBarColors(Color.red, Color.green);
		bd.setShowingValue(true);
		bd.setData(new int[] { 12, 42, 12, 21, 55, 3, 15, 12, 42, 12, 21, 55,
				3, 15 });

		jf.setSize(800, 200);
		jf.getContentPane().setLayout(new BorderLayout());
		jf.getContentPane().add(bd, BorderLayout.CENTER);
		jf.setVisible(true);

		for (int i = 0; i < 10; i++) {
			bd.setData(new int[] { 12, 42, 12, 21, 55, 3, 15, 12, 42, 12, 21,
					55, 13, 15 });
			Thread.sleep(500);
			bd.setData(new int[] { 12, 24, 21, 12, 12, 21, 55, 3, 15, 32, 55,
					23, 33, 15 });
			Thread.sleep(500);
		}
	}

}
