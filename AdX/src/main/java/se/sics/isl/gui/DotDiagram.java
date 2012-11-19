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
 * DotDiagram
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Sun Feb 02 13:15:02 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */

package se.sics.isl.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * A simple dot diagram with support for several dot lines and any number of
 * constant lines.
 */
public class DotDiagram extends JComponent {

	public static final int NORMAL = 0;
	public static final int ADDITIVE = 1;
	public static final int FILLED_ADDITIVE = 2;

	// The data "vector" - any number of vectors
	private int[][] data;
	private int[] dataLen;
	private int[] start;
	private int[] maxData;
	private int[] minData;
	private int totMax;
	private int totMin;
	private boolean lockMinMax = false;
	private int maxDataLen;
	private double factor;

	private int[] constantY;
	private Color[] constantColor;

	private int sizeX;
	private int sizeY;
	private int lowerY;
	private double xspace;
	private int ySpacing = 0;
	private int xSpacing = 0;
	private boolean rescale = false;
	private boolean gridVisible = false;

	private String yLabel = null;
	private String xLabel = null;

	private boolean isAdditive = false;
	private boolean isFilled = false;

	// Cache to avoid creating new insets objects for each repaint. Is
	// created when first needed.
	private Insets insets;

	private Color[] lineColor;

	public DotDiagram(int diagrams) {
		this(diagrams, NORMAL);
	}

	public DotDiagram(int diagrams, int mode) {
		data = new int[diagrams][];
		lineColor = new Color[diagrams];
		dataLen = new int[diagrams];
		start = new int[diagrams];
		maxData = new int[diagrams];
		minData = new int[diagrams];
		for (int i = 0; i < diagrams; i++) {
			lineColor[i] = Color.black;
		}
		setOpaque(true);
		isFilled = mode == FILLED_ADDITIVE;
		isAdditive = isFilled || mode == ADDITIVE;
	}

	public void setShowGrid(boolean on) {
		gridVisible = on;
	}

	public void setGridYSpacing(int spacing) {
		// values / y grid
		ySpacing = spacing;
	}

	public void setYLabel(String yLabel) {
		this.yLabel = yLabel;
	}

	public void setXLabel(String xLabel) {
		this.xLabel = xLabel;
	}

	public void setName(int index, String name) {
		// if (names == null) {
		// names = new String[diagrams];
		// }
		// names[index] = name;
		// StringBuffer sb = new StringBuffer();
		// for (int i = 0, n = names.length; i < n; i++) {
		// // Setup JLables...
		// }
	}

	public void setToolTipVisible(boolean showToolTip) {
		// setToolTipText(showToolTip ? toolTipText : null);
	}

	public void addConstant(Color color, int y) {
		int index;
		if (constantY == null) {
			index = 0;
			constantY = new int[1];
			constantColor = new Color[1];
		} else {
			index = constantY.length;

			int[] tmpY = new int[index + 1];
			Color[] tmpC = new Color[index + 1];
			for (int i = 0; i < index; i++) {
				tmpY[i] = constantY[i];
				tmpC[i] = constantColor[i];
			}
			constantY = tmpY;
			constantColor = tmpC;
		}
		constantY[index] = y;
		constantColor[index] = color == null ? Color.black : color;
		rescale = true;
		repaint();
	}

	public void setMinMax(int min, int max) {
		totMax = max;
		totMin = min;
		lockMinMax = true;
		rescale = true;
	}

	public void setData(int diag, int[] data, int start, int len) {
		int maxData = Integer.MIN_VALUE;
		int minData = Integer.MAX_VALUE;

		if (len > 0) {
			int totLen = data.length;
			for (int i = start, n = start + len; i < n; i++) {
				int val = data[i % totLen];
				if (maxData < val)
					maxData = val;
				if (minData > val)
					minData = val;
			}
		}
		if (minData > 0) {
			minData = 0;
		}
		if (maxData < minData) {
			maxData = minData;
		}

		this.dataLen[diag] = len;
		this.start[diag] = start;
		this.data[diag] = data;
		this.maxData[diag] = maxData;
		this.minData[diag] = minData;
		this.rescale = true;

		repaint();
	}

	public void setDotColor(int diag, Color color) {
		if (color == null) {
			throw new NullPointerException();
		}
		lineColor[diag] = color;
		if (dataLen[diag] > 0) {
			repaint();
		}
	}

	protected void paintComponent(Graphics g0) {
		Graphics2D g = (Graphics2D) g0;
		Color oldColor = g.getColor();
		int width = getWidth();
		int height = getHeight();
		int yLabelSize = 0;
		if (yLabel != null || xLabel != null) {
			FontMetrics fm = g.getFontMetrics();
			yLabelSize = fm.stringWidth(yLabel);
		}

		if (isOpaque()) {
			g.setColor(Color.white);
			g.fillRect(0, 0, width, height);
		}

		insets = getInsets(insets);
		int reserveX = yLabel != null ? (12 * width) / 200 : 0;
		int x = insets.left + reserveX;
		int y = insets.top;
		width = width - insets.left - insets.right - reserveX;
		height = height - insets.top - insets.bottom;

		if (rescale || width != sizeX || height != sizeY) {
			if (rescale) {
				rescale = false;
				maxDataLen = 0;
				if (lockMinMax) {
					for (int i = 0, n = this.data.length; i < n; i++) {
						if (this.dataLen[i] > maxDataLen)
							maxDataLen = this.dataLen[i];
					}
				} else {
					totMin = Integer.MAX_VALUE;
					totMax = Integer.MIN_VALUE;

					if (constantY != null) {
						for (int i = 0, n = constantY.length; i < n; i++) {
							int cy = constantY[i];
							if (cy < totMin)
								totMin = cy;
							if (cy > totMax)
								totMax = cy;
						}
					}

					int sum = 0;
					for (int i = 0, n = this.data.length; i < n; i++) {
						if (this.dataLen[i] > 0) {
							int min = this.minData[i];
							int max = this.maxData[i];
							if (min < totMin)
								totMin = min;
							if (max > totMax)
								totMax = max;
							if (this.dataLen[i] > maxDataLen)
								maxDataLen = this.dataLen[i];
							sum += max;
						}
					}
					if (isAdditive && sum > totMax) {
						totMax = sum;
					}
					// Make sure the zero line is visible
					if (totMin > 0) {
						totMin = 0;
					}
					if (totMax < totMin) {
						totMax = totMin;
					}
				}
			}

			sizeY = height;
			sizeX = width - 2;

			if (totMax < 0) {
				factor = (sizeY - 15) / (double) (0 - totMin);
			} else if (totMax == totMin) {
				factor = 1;
			} else {
				factor = (sizeY - 15) / (double) (totMax - totMin);
			}

			lowerY = sizeY - 5;
			if (maxDataLen == 0) {
				xspace = 1;
			} else {
				xspace = (double) sizeX / maxDataLen;
			}
		}

		x = x + 2;

		// Draw grid...
		int zero = y + lowerY - (int) (factor * (0 - totMin));

		if (gridVisible) {
			g.setColor(Color.lightGray);
			for (int i = 0, n = 10; i < n; i++) {
				g.drawLine(x + (i * sizeX) / 10, y, x + (i * sizeX) / 10, y
						+ sizeY - 1);
			}
			int z0 = zero - y;
			int z1 = zero - (y + sizeY);
			double tot = (ySpacing * factor) == 0 ? (sizeY / 10.0)
					: (factor * ySpacing);
			for (double i = 0; i < z0; i += tot) {
				g
						.drawLine(x + 1, (int) (zero - i), x + sizeX,
								(int) (zero - i));
			}

			for (double i = 0; i > z1; i -= tot) {
				g
						.drawLine(x + 1, (int) (zero - i), x + sizeX,
								(int) (zero - i));
			}
		}

		if (isAdditive) {
			if (isFilled) {
				int delta = (int) (xspace + 1);
				for (int i = 0, n = maxDataLen; i < n; i++) {
					int lastY = zero;
					for (int j = 0, m = data.length; j < m; j++) {
						if (dataLen[j] > i) {
							int[] drawData = data[j];
							int pos = start[j] + i;
							int y0 = (int) (factor * drawData[pos
									% drawData.length]);
							if (y0 > 0) {
								g.setColor(lineColor[j]);
								g.fillRect(x + (int) (i * xspace), lastY - y0,
										delta, y0);
								lastY -= y0;
							}
						}
					}
				}
			} else {
				for (int i = 0, n = maxDataLen; i < n; i++) {
					int lastY = zero;
					for (int j = 0, m = data.length; j < m; j++) {
						if (dataLen[j] > i) {
							int[] drawData = data[j];
							int pos = start[j] + i;
							int y0 = (int) (factor * drawData[pos
									% drawData.length]);
							if (y0 > 0) {
								g.setColor(lineColor[j]);
								g.drawLine(x + (int) ((i - 1) * xspace), lastY
										- y0, x + (int) (i * xspace), lastY
										- y0);
								lastY -= y0;
							}
						}
					}
				}
			}

		} else {
			for (int j = 0, m = data.length; j < m; j++) {
				if (dataLen[j] > 0) {
					int[] drawData = data[j];
					int maxLen = drawData.length;
					int startData = start[j];
					int lastY = (int) (factor * (drawData[startData % maxLen] - totMin));
					g.setColor(lineColor[j]);
					for (int i = 1, n = dataLen[j]; i < n; i++) {
						int pos = startData + i;
						int y0 = (int) (factor * (drawData[pos % maxLen] - totMin));
						g.drawLine(x + (int) ((i - 1) * xspace), y + lowerY
								- lastY, x + (int) (i * xspace), y + lowerY
								- y0);
						lastY = y0;
					}
				}
			}
		}

		if (constantY != null) {
			for (int i = 0, n = constantY.length; i < n; i++) {
				int cy = y + lowerY - (int) (factor * (constantY[i] - totMin));
				g.setColor(constantColor[i]);
				g.drawLine(x, cy, x + sizeX, cy);
			}
		}

		// Draw the coordinate system last to avoid having it overwritten
		// by diagrams (for example with value of 0)
		x = x - 2;
		g.setColor(Color.black);
		g.drawLine(x, zero, x + sizeX, zero);
		g.drawLine(x + 1, y, x + 1, y + sizeY - 1);

		if (yLabel != null) {
			g.rotate(-3.1415 / 2);
			g.scale(height / 200.0, width / 200.0);
			g.drawString(yLabel, -100 - yLabelSize / 2, 10);
		}
	}

	/*****************************************************************************
	 * Test main
	 ****************************************************************************/

	public static void main(String[] args) throws Exception {
		JFrame jf = new JFrame("DotDiagram - Test");
		// bd.setMinMax(0, 200);
		int[] data = new int[100];
		int[] data2 = new int[100];
		int[] data3 = new int[100];

		boolean neg = true;
		int pos = 100;
		int pos2 = 50;
		int pos3 = 0;
		int s = neg ? -10 : 0;
		for (int i = 0; i < 60; i++) {
			pos = pos + (int) (s + 20 * Math.random());
			pos2 = pos2 + (int) (s + 20 * Math.random());
			pos3 = pos3 + (int) (s + 20 * Math.random());
			data[i] = pos;
			data2[i] = pos2;
			data3[i] = pos3;
		}
		DotDiagram bd = setupDiagram(10, data, data2, data3, NORMAL);
		DotDiagram bd2 = setupDiagram(10, data, data2, data3, ADDITIVE);
		DotDiagram bd3 = setupDiagram(10, data, data2, data3, FILLED_ADDITIVE);

		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setSize(600, 200);
		jf.getContentPane().setLayout(new GridLayout(0, 3));
		jf.getContentPane().add(bd);
		jf.getContentPane().add(bd2);
		jf.getContentPane().add(bd3);
		jf.setVisible(true);

		for (int i = 0; i < 20000; i++) {
			Thread.sleep(100);
			pos = pos + (int) (5 - 10 * Math.random());
			pos2 = pos2 + (int) (5 - 10 * Math.random());
			pos3 = pos3 + (int) (5 - 10 * Math.random());

			if (pos < 0)
				pos = 0;
			if (pos3 < 0)
				pos3 = 0;

			data[(60 + i) % data.length] = pos;
			data2[(60 + i) % data2.length] = pos2;
			data3[(60 + i) % data3.length] = pos3;
			bd.setData(0, data, i % data.length, 60);
			bd.setData(1, data2, i % data2.length, 60);
			bd.setData(2, data3, i % data3.length, 60);
			bd2.setData(0, data, i % data.length, 60);
			bd2.setData(1, data2, i % data2.length, 60);
			bd2.setData(2, data3, i % data3.length, 60);
			bd3.setData(0, data, i % data.length, 60);
			bd3.setData(1, data2, i % data2.length, 60);
			bd3.setData(2, data3, i % data3.length, 60);
		}
	}

	private static DotDiagram setupDiagram(int v, int[] d1, int[] d2, int[] d3,
			int mode) {
		DotDiagram bd = new DotDiagram(v, mode);
		bd.setDotColor(0, Color.red);
		bd.setDotColor(1, Color.green);
		bd.setDotColor(2, Color.blue);
		bd.addConstant(Color.yellow, 100);
		bd.setShowGrid(true);
		bd.setYLabel("The Y-Axis (" + (mode == NORMAL ? "normal" : "additive")
				+ ')');
		bd.setData(0, d1, 0, 60);
		bd.setData(1, d2, 0, 60);
		bd.setData(2, d3, 0, 60);
		return bd;
	}

}
