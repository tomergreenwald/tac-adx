/**
 * SICS ISL Java Utilities
 * http://www.sics.se/tac/                         tac-dev@sics.se
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
 * VUMeter
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Dec 03 20:53:07 2002
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 * Purpose :
 *
 */

package se.sics.isl.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;

public class VUMeter extends JComponent {

	private Color ltGreen = new Color(0xb0ffb0);
	private Color ltRed = new Color(0xffb0b0);
	private Color ltYellow = new Color(0xf0f0b0);
	private Color ltGray = new Color(0xa0a0a0);

	private boolean useAntiAliasing = true;
	private double value;

	private int width = 0;
	private int height = 0;

	public VUMeter() {
		setOpaque(true);
		setDoubleBuffered(true);
	}

	public void setValue(double value) {
		this.value = value;
		repaint();
	}

	public double getValue() {
		return value;
	}

	public boolean isAntiAliasing() {
		return useAntiAliasing;
	}

	public void setAntiAliasing(boolean useAntiAliasing) {
		this.useAntiAliasing = useAntiAliasing;
	}

	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, width, height);
		}

		if (useAntiAliasing && g instanceof Graphics2D) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}

		width = (int) getWidth() - 1;
		height = (int) getHeight() - 1;

		int midx = width / 2;

		g.setColor(Color.white);
		g.fillArc(0, 0, width, height * 2 - 1, 0, 180);

		g.setColor(Color.black);
		g.drawArc(0, 0, width, height * 2 - 1, 0, 180);

		double factor = 0.8f;
		g.setColor(ltGreen);
		fillArc(g, factor, 120, 50);

		g.setColor(ltYellow);
		fillArc(g, factor, 60, 60);

		g.setColor(ltRed);
		fillArc(g, factor, 10, 50);

		factor = 0.6f;
		g.setColor(Color.white);
		fillArc(g, factor, 0, 180);

		// The meter!
		g.setColor(Color.black);
		double angle = 3.141592 - 3.141592 * value;
		g.drawLine(midx, height - 2, midx
				+ (int) (Math.cos(angle) * midx * 0.8), height
				- (int) (0.8 * height * Math.sin(angle)) - 2);

		factor = 0.3f;
		g.setColor(Color.gray);
		fillArc(g, factor, 0, 180);

		g.setColor(Color.black);
		g.drawLine(0, height - 1, width, height - 1);
	}

	private void fillArc(Graphics g, double factor, int start, int length) {
		g.fillArc((int) (0.5 + width * (1 - factor) / 2),
				(int) (height * (1 - factor)), (int) (0.5 + width * factor),
				(int) (height * 2 * factor), start, length);
	}

	/*****************************************************************************
	 * Test Main
	 ****************************************************************************/

	public static void main(String[] args) {
		javax.swing.JFrame window = new javax.swing.JFrame("Test");
		VUMeter meter = new VUMeter();
		meter.setPreferredSize(new java.awt.Dimension(60, 40));
		window.getContentPane().add(meter);
		window.pack();
		window.setVisible(true);
		double v = 0;
		double dv = 0.01;
		while (true) {
			meter.setValue(v);
			v += dv;
			if (v <= 0 || v >= 1)
				dv = -dv;
			try {
				Thread.sleep(25);
			} catch (Exception e) {
			}
		}
	}
}
