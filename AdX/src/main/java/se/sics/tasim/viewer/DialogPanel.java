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
 * DialogPanel
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Sun Feb 02 22:16:33 2003
 * Updated : $Date: 2004-10-28 14:24:41 -0500 (Thu, 28 Oct 2004) $
 *           $Revision: 1057 $
 * Purpose :
 *
 */
package se.sics.tasim.viewer;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import se.sics.isl.gui.WindowBorder;

public class DialogPanel extends JPanel implements MouseListener,
		MouseMotionListener {

	private ViewerPanel viewerPanel;
	private WindowBorder windowBorder;

	private Point mousePoint = new Point();
	private int deltaX, deltaY;
	private int minX, maxX, maxY;
	private boolean isPressed;

	public DialogPanel(ViewerPanel viewerPanel, LayoutManager layout) {
		super(layout);
		this.viewerPanel = viewerPanel;
		this.windowBorder = new WindowBorder();
		setBorder(windowBorder);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/*********************************************************************
	 * MouseListener
	 **********************************************************************/

	public void mouseClicked(MouseEvent mouseEvent) {
		if (mouseEvent.getSource() == this
				&& windowBorder.isInCloseButton(this, mouseEvent.getX(),
						mouseEvent.getY())) {
			viewerPanel.closeDialog();
		}
	}

	public void mousePressed(MouseEvent mouseEvent) {
		if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
			int x = mouseEvent.getX();
			int y = mouseEvent.getY();
			if (windowBorder.isInTitle(this, x, y)) {
				mousePoint.setLocation(x, y);
				SwingUtilities.convertPointToScreen(mousePoint,
						(Component) mouseEvent.getSource());
				this.deltaX = getX() - mousePoint.x;
				this.deltaY = getY() - mousePoint.y;

				Component parent = getParent();
				if (parent != null) {
					// this.maxX = parent.getWidth() - getWidth();
					// this.maxY = parent.getHeight() - getHeight();
					this.minX = 20 - getWidth();
					this.maxX = parent.getWidth() - 20;
					this.maxY = parent.getHeight()
							- windowBorder.getTitleHeight();
				} else {
					this.minX = 0;
					this.maxX = this.maxY = Integer.MAX_VALUE;
				}

				isPressed = true;
			}
		}
	}

	public void mouseReleased(MouseEvent mouseEvent) {
		isPressed = false;
	}

	public void mouseEntered(MouseEvent mouseEvent) {
	}

	public void mouseExited(MouseEvent mouseEvent) {
	}

	/*********************************************************************
	 * MouseMotionListener
	 **********************************************************************/

	// Should also support resize of the dialog!!! FIX THIS!!!
	public void mouseDragged(MouseEvent e) {
		if (isPressed && SwingUtilities.isLeftMouseButton(e)) {
			mousePoint.setLocation(e.getX(), e.getY());
			SwingUtilities.convertPointToScreen(mousePoint, (Component) e
					.getSource());
			int newX = deltaX + mousePoint.x;
			int newY = deltaY + mousePoint.y;
			if (newX < minX) {
				newX = minX;
			} else if (newX > maxX) {
				newX = maxX;
			}
			if (newY < 0) {
				newY = 0;
			} else if (newY > maxY) {
				newY = maxY;
			}
			setLocation(newX, newY);
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

} // DialogPanel
