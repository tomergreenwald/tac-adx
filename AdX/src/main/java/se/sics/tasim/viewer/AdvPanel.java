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
 * AdvPanel
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Feb 03 17:14:40 2003
 * Updated : $Date: 2004-10-28 14:24:41 -0500 (Thu, 28 Oct 2004) $
 *           $Revision: 1057 $
 */
package se.sics.tasim.viewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JLabel;

public class AdvPanel extends JLabel {

	private ViewerPanel viewerPanel;

	public AdvPanel(ViewerPanel viewerPanel) {
		super("Viewing games on server " + viewerPanel.getServerName());
		this.viewerPanel = viewerPanel;
		setVerticalAlignment(CENTER);
		setHorizontalAlignment(CENTER);
		setVerticalTextPosition(BOTTOM);
		setHorizontalTextPosition(CENTER);
		// setIconTextGap(1);
		// setIcon(viewerPanel.getIcon("banner2.jpg"));
	}

	public Insets getInsets() {
		return getInsets(null);
	}

	public Insets getInsets(Insets insets) {
		if (insets == null) {
			return new Insets(0, 2, 0, 2);
		} else {
			insets.left = insets.right = 2;
			insets.top = insets.bottom = 0;
			return insets;
		}
	}

	protected void paintBorder(Graphics g) {
		Color originalColor = g.getColor();
		int width = getWidth() - 1;
		int height = getHeight() - 1;
		g.setColor(getForeground());

		g.drawLine(0, 0, 0, height);
		g.drawLine(width, 0, width, height);

		// if (icon != null) {
		// int iconWidth = icon.getIconWidth();
		// int iconHeight = icon.getIconHeight();
		// icon.paintIcon(this, g, (width - iconWidth) / 2,
		// (height - iconHeight) / 2);
		// }

		g.setColor(originalColor);
	}

} // AdvPanel
