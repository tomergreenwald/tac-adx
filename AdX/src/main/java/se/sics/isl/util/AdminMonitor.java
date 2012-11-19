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
 * AdminMonitor
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Jul 04 16:34:35 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.botbox.util.ThreadPool;

/**
 */
public class AdminMonitor implements ActionListener, AMonitor {

	private static final Logger log = Logger.getLogger(AdminMonitor.class
			.getName());

	private static final String THREAD_NAME = "Threads";
	private static final String STAT_NAME = "System";
	private static final String GC_NAME = "GC";

	private static AdminMonitor defaultAdminMonitor = new AdminMonitor();

	public static AdminMonitor getDefault() {
		return defaultAdminMonitor;
	}

	private long startTime;

	private String title;
	private JFrame window;
	private JTextArea statusText;
	private JPanel buttonPanel;

	private Hashtable monitors = new Hashtable();

	public AdminMonitor() {
		startTime = System.currentTimeMillis();
		monitors.put(THREAD_NAME, this);
		monitors.put(STAT_NAME, this);
		monitors.put(GC_NAME, this);
	}

	public void setTitle(String title) {
		this.title = title;
		if (window != null) {
			window.setTitle(title == null ? "Admin Monitor"
					: ("Admin Monitor: " + title));
		}
	}

	public void setBounds(int x, int y, int width, int height) {
		if (window == null) {
			createWindow();
		}
		window.setBounds(x, y, width, height);
	}

	/**
	 * Set the bounds of the admin monitor window as a string with the form
	 * "x,y" or "x,y,width,height".
	 */
	public void setBounds(String bounds) {
		try {
			StringTokenizer tok = new StringTokenizer(bounds, ", \t");
			int x = Integer.parseInt(tok.nextToken());
			int y = Integer.parseInt(tok.nextToken());
			if (window == null) {
				createWindow();
			}
			if (tok.hasMoreTokens()) {
				window.setBounds(x, y, Integer.parseInt(tok.nextToken()), // width
						Integer.parseInt(tok.nextToken())); // height
			} else {
				window.setLocation(x, y);
			}

		} catch (Exception e) {
			log.log(Level.WARNING, "could not set boundary", e);
		}
	}

	public void start() {
		if (window == null) {
			createWindow();
		}
		window.setVisible(true);
	}

	private void createWindow() {
		String title = this.title;
		window = new JFrame(title == null ? "Admin Monitor"
				: ("Admin Monitor: " + title));
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(statusText = new JTextArea(5, 40)),
				BorderLayout.CENTER);
		statusText.setEditable(false);
		statusText.setFocusable(false);
		statusText.setRequestFocusEnabled(false);
		statusText.setTabSize(12);

		buttonPanel = new JPanel();

		// Create for all buttons
		synchronized (monitors) {
			Enumeration mons = monitors.keys();
			while (mons.hasMoreElements()) {
				String name = (String) mons.nextElement();
				JButton button = new JButton(name);
				button.addActionListener(this);
				buttonPanel.add(button);
			}
		}

		panel.add(buttonPanel, BorderLayout.SOUTH);

		window.getContentPane().add(panel);

		window.pack();
	}

	public void addMonitor(String name, AMonitor monitor) {
		synchronized (monitors) {
			if (monitors.get(name) != null) {
				log.log(Level.WARNING, "monitor '" + name
						+ "' already registered",
				// Just to get a dump of where the problem occurred for now
						new IllegalArgumentException(
								"monitor already registered"));
				return;
			}

			monitors.put(name, monitor);

			if (buttonPanel != null) {
				final JButton button = new JButton(name);
				button.addActionListener(this);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						buttonPanel.add(button);
						buttonPanel.revalidate();
					}
				});
			}
		}
	}

	// -------------------------------------------------------------------
	// AMonitor API
	// -------------------------------------------------------------------

	public String getStatus(String name) {
		if (name == THREAD_NAME) {
			Enumeration pools = ThreadPool.getThreadPools();
			StringBuffer sb = new StringBuffer().append("--- ThreadPools ---");
			while (pools.hasMoreElements()) {
				ThreadPool pool = (ThreadPool) pools.nextElement();
				sb.append('\n');
				pool.getThreadStatus(sb);
			}
			return sb.toString();
		} else if (name == STAT_NAME) {
			long memory = Runtime.getRuntime().totalMemory();
			long free = Runtime.getRuntime().freeMemory();
			return "--- System ---" + "\nSystem Running:\t" + getSystemTime()
					+ "\nTotal Memory:\t" + formatMemory(memory)
					+ "\nAvailable Memory:\t" + formatMemory(free)
					+ "\nUsed Memory:\t" + formatMemory(memory - free)
					+ "\nActive Threads:\t" + getThreadCount()
					+ "\nJava Version:\t"
					+ System.getProperty("java.version", "");
		} else if (name == GC_NAME) {
			long memory = Runtime.getRuntime().totalMemory();
			long free = Runtime.getRuntime().freeMemory();

			System.gc();

			long memory2 = Runtime.getRuntime().totalMemory();
			long free2 = Runtime.getRuntime().freeMemory();
			return "--- Memory ---" + "\nBefore GC:" + "\n  Total Memory:\t"
					+ formatMemory(memory) + "\n  Available Memory:\t"
					+ formatMemory(free) + "\n  Used Memory:\t"
					+ formatMemory(memory - free) + "\nAfter GC:"
					+ "\n  Total Memory:\t" + formatMemory(memory2)
					+ "\n  Available Memory:\t" + formatMemory(free2)
					+ "\n  Used Memory:\t" + formatMemory(memory2 - free2);
		}
		return null;
	}

	private String formatMemory(long value) {
		boolean isNegative = value < 0;
		if (isNegative) {
			value = -value;
		}

		// -9 223 372 036 854 775 808
		char[] buffer = new char[1 + 19 + 6];

		int index = buffer.length - 1;
		if (value == 0) {
			buffer[index--] = '0';
		} else {
			for (int count = 0; value > 0 && index >= 0; count++) {
				if (((count % 3) == 0) && count > 0 && index > 0) {
					buffer[index--] = ' ';
				}
				buffer[index--] = (char) ('0' + (value % 10));
				value /= 10;
			}
		}

		if (isNegative && index >= 0) {
			buffer[index--] = '-';
		}
		return new String(buffer, index + 1, buffer.length - index - 1);
	}

	private String getSystemTime() {
		long time = (System.currentTimeMillis() - startTime) / 1000;
		StringBuffer sb = new StringBuffer();
		int days = (int) (time / (24 * 60 * 60));
		if (days > 0) {
			sb.append(days).append(" day");
			if (days > 1) {
				sb.append('s');
			}
			sb.append(' ');
			time = time % (24 * 60 * 60);
		}
		int h = (int) (time / (60 * 60));
		if (h > 0) {
			sb.append(h).append(" hour");
			if (h > 1)
				sb.append('s');
			sb.append(' ');
			time = time % (60 * 60);
		}
		int min = (int) (time / 60);
		int seconds = (int) (time % 60);
		sb.append(min).append(" min ").append(seconds).append(" sec");
		return sb.toString();
	}

	private String getThreadCount() {
		try {
			ThreadGroup group = Thread.currentThread().getThreadGroup();
			ThreadGroup parent;
			int activeCount;

			// Find the top thread group
			while ((parent = group.getParent()) != null) {
				group = parent;
			}

			return Integer.toString(group.activeCount());
		} catch (SecurityException e) {
			// Could not access thread group
			return "no access";
		}
	}

	// -------------------------------------------------------------------
	// ActionListener
	// -------------------------------------------------------------------

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source instanceof JButton) {
			String name = event.getActionCommand();
			if (name != null) {
				AMonitor monitor = (AMonitor) monitors.get(name);
				String text;
				if ((monitor != null)
						&& ((text = monitor.getStatus(name)) != null)) {
					statusText.setText(text);
				} else {
					statusText.setText("No status for monitor\n" + name);
				}
			}
		}
	}

} // AdminMonitor
