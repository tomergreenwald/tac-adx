/*
 * @(#)WindowBorder.java	Created date: 00-9-7
 * $Revision: 3766 $, $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *
 * Copyright (c) 2000 BotBox AB.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * BotBox AB. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * BotBox AB.
 */

package se.sics.isl.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.AbstractBorder;

/**
 * 
 * 
 * @author Joakim Eriksson (joakim.eriksson@botbox.com)
 * @author Niclas Finne (niclas.finne@botbox.com)
 * @author Sverker Janson (sverker.janson@botbox.com)
 * @version $Revision: 3766 $, $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb
 *          2008) $
 */
public class WindowBorder extends AbstractBorder {

	private int borderSize = 3;
	private int titleHeight = 8;

	public WindowBorder() {
	}

	public int getTitleHeight() {
		return titleHeight + borderSize;
	}

	public int getBorderSize() {
		return borderSize;
	}

	public void setBorderSize(int borderSize) {
		this.borderSize = borderSize;
	}

	public Insets getBorderInsets(Component c) {
		return new Insets(titleHeight + borderSize, borderSize, borderSize,
				borderSize);
	}

	public Insets getBorderInsets(Component c, Insets insets) {
		insets.top = titleHeight + borderSize;
		insets.left = borderSize;
		insets.bottom = borderSize;
		insets.right = borderSize;
		return insets;
	}

	public boolean isInTitle(Component c, int x, int y) {
		return y <= 12;
	}

	public boolean isInCloseButton(Component c, int x, int y) {
		int cWidth = c.getWidth();
		int cHeight = c.getHeight();
		return (cWidth - 12) <= x && (cWidth - 2) >= x && 2 <= y && 12 >= y;
	}

	public boolean isBorderOpaque() {
		return true;
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int width,
			int height) {
		// \TODO Ignore active/inactive for now (doesn't work anyway).
		// boolean isActive = window.isActive();

		Color oldColor = g.getColor();
		g.setColor(Color.white);
		g.drawRect(x, y, width - 1, height - 1);

		g.setColor(c.getBackground());
		g.drawRect(x + 2, y + 2, width - 5, height - 5);

		g.setColor(Color.black);
		g.drawRect(x + 1, y + 1, width - 3, height - 3);
		// g.drawRect(x + 2, y + 2, width - 5, height - 5);

		// Modify for border (instead of handling this below)
		x += 2;
		y += 2;
		width -= 4;

		// Draw border
		g.setColor(// isActive ?
				c.getBackground() // Color.white
				// : Color.lightGray
				);
		g.fillRect(x, y, width - 6, 8);

		g.setColor(Color.lightGray);
		g.fillRect(x + width - 9, y, 9, 8);

		g.setColor(Color.black);
		g.drawLine(x, y + 2, x + width - 11, y + 2);
		g.drawLine(x, y + 5, x + width - 11, y + 5);
		g.drawLine(x, y + 8, x + width - 11, y + 8);

		// The close button
		g.drawRect(x + width - 10, y - 1, 10, 9);
		g.drawLine(x + width - 8, y + 1, x + width - 2, y + 7);
		g.drawLine(x + width - 8, y + 7, x + width - 2, y + 1);

		g.setColor(Color.white);
		g.drawLine(x + width - 8, y, x + width - 2, y + 6);
		g.drawLine(x + width - 8, y + 6, x + width - 2, y);

		// if (title != null) {
		// FontMetrics fm = g.getFontMetrics();
		// int fontHeight = fm.getHeight();
		// int descent = fm.getDescent();
		// int ascent = fm.getAscent();
		// int diff;
		// int stringWidth = fm.stringWidth(title);
		// int fx = x + (width - stringWidth) / 2;
		// int fy = y + ascent;
		// g.setColor(Color.blue);
		// g.fillRect(fx + 1, y + 1, stringWidth - 2, fontHeight - 1);
		// g.setColor(Color.black);
		// g.drawRect(fx, y, stringWidth, fontHeight);
		// g.drawString(title,
		// x + (width - stringWidth) / 2,
		// y + ascent);
		// }
		g.setColor(oldColor);
	}

} // WindowBorder
