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
 * MessageModel
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Feb 04 16:49:16 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 * Purpose :
 *
 */
package se.sics.isl.gui;

import javax.swing.AbstractListModel;

public class MessageModel extends AbstractListModel {

	public static final int NONE = 0;
	public static final int WARNING = 1;

	private String[] messages;
	private int[] messageFlag;
	private int messagePos = 0;
	private int size = 0;

	public MessageModel(int messageCount) {
		messages = new String[messageCount];
		messageFlag = new int[messageCount];
	}

	public void addMessage(String message) {
		addMessage(message, NONE);
	}

	public void addMessage(String message, int flag) {
		if (size < messages.length) {
			int index = size++;
			messageFlag[index] = flag;
			messages[index] = message;
			fireIntervalAdded(this, index, index);
		} else {
			messages[messagePos] = message;
			messageFlag[messagePos] = flag;
			messagePos = (messagePos + 1) % size;
			// \TODO Should be optimized.
			fireContentsChanged(this, 0, size);
		}
	}

	public void clear() {
		if (size > 0) {
			int oldSize = size;
			fireIntervalRemoved(this, 0, size - 1);
			size = 0;
			for (int i = 0; i < oldSize; i++) {
				messages[i] = null;
			}
		}
	}

	public Object getElementAt(int index) {
		if (index >= size) {
			throw new ArrayIndexOutOfBoundsException(index + " >= " + size);
		}
		index = (messagePos + index) % size;
		return messages[index];
	}

	public int getFlagAt(int index) {
		if (index >= size) {
			throw new ArrayIndexOutOfBoundsException(index + " >= " + size);
		}
		index = (messagePos + index) % size;
		return messageFlag[index];
	}

	public int getSize() {
		return size;
	}

} // MessageModel
