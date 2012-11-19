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
 * Clock
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Dec 03 09:55:10 2002
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 * Purpose :
 *
 */
package se.sics.isl.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Clock extends JPanel {

	private final static double PI = 3.141592;

	// A time in milliseconds after 1970:00/00 GMT
	private long time = 0;
	private boolean showSeconds = true;

	private boolean useAntiAliasing = true;

	// Cache for the polygon data. Since only the dispatch thread is
	// painting there is no synchronization problems.
	private int[] xpoints = new int[3];
	private int[] ypoints = new int[3];

	// Cache to avoid creating new insets objects for each repaint. Is
	// created when first needed.
	private Insets insets;

	public Clock() {
		// Only if not inherited from JPanel
		// setOpaque(true);
		// setDoubleBuffered(true);
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		if (this.time != time) {
			this.time = time;
			repaint();
		}
	}

	public boolean isShowingSeconds() {
		return showSeconds;
	}

	public void setShowingSeconds(boolean secs) {
		this.showSeconds = secs;
	}

	public boolean isAntiAliasing() {
		return useAntiAliasing;
	}

	public void setAntiAliasing(boolean useAntiAliasing) {
		this.useAntiAliasing = useAntiAliasing;
	}

	protected void paintComponent(Graphics g) {
		Color originalColor = g.getColor();
		insets = getInsets(insets);
		int width = getWidth();
		int height = getHeight();

		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, width, height);
		}

		width = width - insets.left - insets.right - 1;
		height = height - insets.top - insets.bottom - 1;
		if (width <= 0 || height <= 0) {
			g.setColor(originalColor);
			return;
		}

		if (useAntiAliasing && g instanceof Graphics2D) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}

		int x = insets.left;
		int y = insets.top;
		int midx = width / 2;
		int midy = height / 2;

		g.setColor(Color.white);
		g.fillOval(x, y, width, height);
		g.setColor(Color.black);
		g.drawOval(x, y, width - 1, height - 1);

		double p = PI / 6.0;
		for (int i = 0; i < 12; i++) {
			double s = (i % 3 == 0 ? 0.9 : 0.95);
			double ip = i * p;
			int dx = (int) (midx * Math.cos(ip));
			int dy = (int) (midy * Math.sin(ip));
			int xs = (int) (dx * s);
			int ys = (int) (dy * s);
			g.drawLine(x + midx + xs, y + midy + ys, x + midx + dx, y + midy
					+ dy);
		}
		double sec = time / 1000.0;
		double min = sec / 60.0;
		double hr = (min / 60) % 12;
		double angle;

		if (showSeconds) {
			angle = 2 * PI * (min % 60) - PI / 2;
			g.drawLine(x + midx, y + midy, x + midx
					+ (int) (midx * Math.cos(angle)), y + midy
					+ (int) (midy * Math.sin(angle)));
		}

		angle = 2 * PI * ((min / 60.0) % 60) - PI / 2;
		int dx = (int) (midx / 1.1 * Math.cos(angle));
		int dy = (int) (midy / 1.1 * Math.sin(angle));

		xpoints[0] = x + midx + dy / 30;
		ypoints[0] = y + midy - dx / 30;
		xpoints[1] = x + midx + dx;
		ypoints[1] = y + midy + dy;
		xpoints[2] = x + midx - dy / 30;
		ypoints[2] = y + midy + dx / 30;

		g.setColor(Color.gray);
		g.fillPolygon(xpoints, ypoints, 3);
		g.setColor(Color.black);
		g.drawPolygon(xpoints, ypoints, 3);
		// g.drawLine(midx + dy/30, midy - dx/30, midx + dx, midy + dy);
		// g.drawLine(midx - dy/30, midy + dx/30, midx + dx, midy + dy);

		angle = 2 * PI * ((min / 720.0) % 12) - PI / 2;
		dx = (int) (midx / 1.5 * Math.cos(angle));
		dy = (int) (midy / 1.5 * Math.sin(angle));

		xpoints[0] = x + midx + dy / 20;
		ypoints[0] = y + midy - dx / 20;
		xpoints[1] = x + midx + dx;
		ypoints[1] = y + midy + dy;
		xpoints[2] = x + midx - dy / 20;
		ypoints[2] = y + midy + dx / 20;

		g.setColor(Color.gray);
		g.fillPolygon(xpoints, ypoints, 3);
		g.setColor(Color.black);
		g.drawPolygon(xpoints, ypoints, 3);

		// g.drawLine(midx + dy/20, midy - dx/20, midx + dx, midy + dy);
		// g.drawLine(midx - dy/20, midy + dx/20, midx + dx, midy + dy);
		// Restore original color
		g.setColor(originalColor);
	}

	/*****************************************************************************
	 * Test Main
	 ****************************************************************************/

	public static void main(String[] args) {
		JFrame window = new JFrame("GMT Clock");
		JPanel panel = new JPanel();
		Clock clk = new Clock();
		Clock clk2 = new Clock();
		clk.setPreferredSize(new Dimension(50, 50));
		clk2.setPreferredSize(new Dimension(100, 100));
		panel.add(clk);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.getContentPane().add(panel, BorderLayout.NORTH);
		window.getContentPane().add(clk2, BorderLayout.CENTER);
		window.pack();
		window.setVisible(true);

		clk.setShowingSeconds(false);
		clk2.setShowingSeconds(false);
		while (true) {
			// clk.setTime(System.currentTimeMillis());
			long time = System.currentTimeMillis() * 6171;
			clk.setTime(time);
			clk2.setTime(time);
			try {
				Thread.sleep(20);
			} catch (Exception e) {
			}
		}
	}

} // Clock
