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
 * ChatPanel
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Jun 14 17:23:05 2002
 * Updated : $Date: 2004-10-28 14:24:41 -0500 (Thu, 28 Oct 2004) $
 *           $Revision: 1057 $
 * Purpose :
 *
 */
package se.sics.tasim.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import se.sics.tasim.viewer.ViewerPanel;

public class ChatPanel extends JPanel implements ActionListener {

	private JTextArea chatArea;
	private JTextField chatMessage;
	private JButton sendButton;
	private JButton clearButton;

	private String agentName;
	private ViewerPanel mainPanel;

	private SimpleDateFormat dateFormat;
	private Date date;

	public ChatPanel(ViewerPanel mainPanel) {
		super(new BorderLayout());
		this.agentName = mainPanel.getUserName();
		this.mainPanel = mainPanel;
		chatArea = new JTextArea(6, 40);
		chatArea.setBackground(Color.white);
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);
		add(new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

		JPanel panel = new JPanel(new BorderLayout(0, 0));
		panel.setBackground(Color.white);
		chatMessage = new JTextField("");
		panel.add(chatMessage, BorderLayout.CENTER);
		JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panel2.add(sendButton = new JButton("Send"));
		panel2.add(clearButton = new JButton("Clear"));
		panel.add(panel2, BorderLayout.EAST);
		JLabel chatLabel = new JLabel(agentName + ':');
		chatLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		panel.add(chatLabel, BorderLayout.WEST);

		add(panel, BorderLayout.SOUTH);

		sendButton.addActionListener(this);
		chatMessage.addActionListener(this);
		clearButton.addActionListener(this);
	}

	// Hack to avoid using another panel.
	void setStatusLabel(JLabel label) {
		add(label, BorderLayout.NORTH);
	}

	public void addChatMessage(final long time, final String serverName,
			final String userName, final String message) {
		// Use AWT thread to add the message
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				doAddMessage(time, serverName, userName, message);
			}
		});
	}

	private void doAddMessage(long time, String serverName, String userName,
			String message) {
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat("d MMM HH:mm");
			dateFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
			date = new Date(time);
		} else {
			date.setTime(time);
		}
		// Add server name when several servers are used! FIX THIS!!!
		message = dateFormat.format(date) + ' ' + userName + "> " + message;

		String text = chatArea.getText();
		int len = text.length();
		if (text.length() > 0) {
			// Only add line ends (\n) when needed. This saves one row in
			// the chats limited text area.
			text = text + '\n' + message;
			len = text.length();

			// Remove the first line if the chat size is too large (avoids too
			// large chat area if people leaves the viewer running for long
			// times).
			if (len > 5120) {
				int index = text.indexOf('\n');
				if (index > 0 && (++index < len)) {
					text = text.substring(index);
					len = text.length();
				}
			}

		} else {
			text = message;
		}

		chatArea.setText(text);
		// Make sure the last message is visible (JTextField does not
		// automatically do this because we are using setText)
		chatArea.setCaretPosition(len);
	}

	public void actionPerformed(ActionEvent actionEvent) {
		Object source = actionEvent.getSource();
		if ((source == chatMessage) || (source == sendButton)) {
			String message = chatMessage.getText().trim();
			chatMessage.setText("");
			if (message.length() > 0) {
				mainPanel.sendChatMessage(message);
			}

		} else if (source == clearButton) {
			chatArea.setText("");
			chatMessage.setText("");
		}
	}

} // ChatPanel
