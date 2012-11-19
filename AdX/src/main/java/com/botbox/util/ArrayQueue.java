/*
 * @(#)ArrayQueue.java	Created date: 2000-04-10
 * $Revision: 4074 $, $Date: 2008-04-11 11:10:43 -0500 (Fri, 11 Apr 2008) $
 *
 * Copyright (c) 2000-2005 BotBox AB.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * BotBox AB. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * BotBox AB.
 */

package com.botbox.util;

/**
 * The ArrayQueue class implements a simple queue using a rotating, resizable
 * array. Permits all elements, including <tt>null</tt>.
 * 
 * The usage is basically the same as for ArrayList but this implementation is
 * optimized for adding last and removing first element while the ArrayList
 * shifts a lot of data when removing first element.
 * 
 * <strong> Note that this implementation is not synchronized and if an
 * ArrayQueue instance is accessed by several threads concurrently, and at least
 * one thread modifies the queue, it must be synchronized externally. </strong>
 * 
 * @author Joakim Eriksson (joakim.eriksson@botbox.com)
 * @author Niclas Finne (niclas.finne@botbox.com)
 * @author Sverker Janson (sverker.janson@botbox.com)
 * @version $Revision: 4074 $, $Date: 2008-04-11 11:10:43 -0500 (Fri, 11 Apr
 *          2008) $
 */
public class ArrayQueue implements Cloneable, java.io.Serializable {

	private static final long serialVersionUID = 5791745982858131414L;

	private transient Object[] queueData;
	private transient int first = 0;
	private transient int last = 0;
	private int size = 0;

	public ArrayQueue() {
		this(10);
	}

	public ArrayQueue(int initialCapacity) {
		if (initialCapacity < 0) {
			throw new IllegalArgumentException("illegal capacity: "
					+ initialCapacity);
		}
		queueData = new Object[initialCapacity];
	}

	public void ensureCapacity(int minCapacity) {
		int capacity = queueData.length;
		if (capacity < minCapacity) {
			int newCapacity = (capacity * 3) / 2 + 1;
			set(newCapacity < minCapacity ? minCapacity : newCapacity);
		}
	}

	/**
	 * Copies all data in the queue to a new data array of the specified size
	 * (MUST be large enough) and replaces the old data array.
	 */
	private void set(int newCapacity) {
		Object[] newData = new Object[newCapacity];
		copy(newData);
		// The data is always in the beginning after allocating new data array
		first = 0;
		last = size;
		queueData = newData;
	}

	/**
	 * Copies all data in the queue to the new data array (MUST be large
	 * enough!)
	 */
	private void copy(Object[] newData) {
		if (first < last) {
			// Case 1: straight forward
			// [...1,2,3,4...]
			System.arraycopy(queueData, first, newData, 0, size);
		} else if (size > 0) {
			// Case 2: wrapped queue detected
			// [4,5,6,......,1,2,3]
			int capacity = queueData.length;
			// Since last and first differ at least one element must exist
			System.arraycopy(queueData, first, newData, 0, capacity - first);
			if (last > 0) {
				System.arraycopy(queueData, 0, newData, capacity - first, last);
			}
		} else {
			// first == last => the queue is empty i.e. do nothing
		}
	}

	public void trimToSize() {
		if (size < queueData.length) {
			// Make sure only the needed space is occupied
			set(size);
		}
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public boolean contains(Object element) {
		return indexOf(element) >= 0;
	}

	public int indexOf(Object element) {
		return indexOf(element, 0);
	}

	public int indexOf(Object element, int index) {
		if ((index < 0) || (index >= size)) {
			return -1;
		}

		int capacity = queueData.length;
		int n = (first + index) % capacity;
		if (element == null) {
			// Find first NULL
			while (index < size) {
				if (queueData[n] == null) {
					return index;
				}

				n = (n + 1) % capacity;
				index++;
			}

		} else {
			while (index < size) {
				if (element.equals(queueData[n])) {
					return index;
				}

				n = (n + 1) % capacity;
				index++;
			}
		}
		return -1;
	}

	private int lastIndexOf(Object element) {
		if (size == 0) {
			return -1;
		}

		int n = last;
		int index = size;
		if (element == null) {
			// Find first NULL
			do {
				if (n > 0) {
					n--;
				} else {
					n = queueData.length - 1;
				}
				index--;

				if (queueData[n] == null) {
					return index;
				}
			} while (index > 0);

		} else {
			do {
				if (n > 0) {
					n--;
				} else {
					n = queueData.length - 1;
				}
				index--;
				if (element.equals(queueData[n])) {
					return index;
				}
			} while (index > 0);
		}
		return -1;
	}

	public Object get(int index) {
		// getIndex will ensure that the index is valid
		return queueData[getIndex(index)];
	}

	public Object set(int index, Object element) {
		index = getIndex(index);
		Object oldValue = queueData[index];
		queueData[index] = element;
		return oldValue;
	}

	public boolean add(Object element) {
		ensureCapacity(size + 1);
		queueData[last] = element;
		last = (last + 1) % queueData.length;
		size++;
		return true;
	}

	public void add(int index, Object element) {
		if (index == size) {
			// Add at the end of the queue
			add(element);
		} else {
			// Make sure there is room for the new element
			ensureCapacity(size + 1);

			// Make sure it is a valid index (must be 0 <= index < size
			index = getIndex(index);

			if (index == first) {
				// Special case when adding first to avoid having to shift data
				if (first > 0) {
					first--;
					index = first;
				} else {
					// first == 0 => wrapp first to the end of the array
					// (this is possible because we KNOW that at least on
					// position
					// is free)
					index = first = queueData.length - 1;
				}
			} else if (index < last) {
				// Case 1 (non wrapped) or Case 2 (index in first part of array)
				System.arraycopy(queueData, index, queueData, index + 1, last
						- index);
				last = (last + 1) % queueData.length;
			} else {
				// index > last => index > first (since at least one position is
				// free)
				// Case 2 (wrapped) (index in the end of the data array)
				// first > 0 because first != last (at least one element in the
				// list
				// and at least one free position)
				System.arraycopy(queueData, first, queueData, first - 1, index
						- first);
				index--;
				first--;
			}
			queueData[index] = element;
			size++;
		}
	}

	public Object remove(int index) {
		Object value;
		// This method also checks that the index is valid (the queue is not
		// empty)
		index = getIndex(index);

		value = queueData[index];
		if (index == first) {
			// This is the most common case for a queue
			queueData[first] = null;
			first = (first + 1) % queueData.length;

		} else if (index < last) {
			// Case 1 (non wrapped) or
			// Case 2 when index is less than last (in the first part of the
			// array)
			last--;
			if (index < last) {
				// An element in the middle is removed
				System.arraycopy(queueData, index + 1, queueData, index, last
						- index);
			}
			queueData[last] = null;

		} else if ((last == 0) && (index == queueData.length - 1)) {
			// Special case when no elements exists at the beginning of the data
			// array and the removed index is last in the data array
			// (wrap the last pointer to the end of the array)
			queueData[index] = null;
			last = queueData.length - 1;

		} else {
			// Case 2 (wrapped) and index must be larger than first
			// (in the middle of end of the array)
			// (we have already checked index==first)

			System.arraycopy(queueData, first, queueData, first + 1, index
					- first);
			queueData[first++] = null;
		}
		size--;
		return value;
	}

	public void clear() {
		int queueLen = queueData.length;
		for (int i = 0, index = first; i < size; i++) {
			queueData[index] = null;
			index = (index + 1) % queueLen;
		}
		first = last = size = 0;
	}

	/**
	 * Returns a shallow copy of this Queue (the elements themselves are not
	 * copied).
	 */
	public Object clone() {
		try {
			ArrayQueue q = (ArrayQueue) super.clone();
			// The set method will always create a new array which guarantees
			// that the new queue will have it very own private array to play
			// with.
			q.set(q.size);
			return q;
		} catch (CloneNotSupportedException e) {
			// Should never happen
			throw new InternalError();
		}
	}

	public Object[] toArray() {
		Object[] array = new Object[size];
		copy(array);
		return array;
	}

	public Object[] toArray(Object[] array) {
		if (array.length < size) {
			array = (Object[]) java.lang.reflect.Array.newInstance(array
					.getClass().getComponentType(), size);
		}
		copy(array);
		if (array.length > size) {
			array[size] = null;
		}
		return array;
	}

	private int getIndex(int index) {
		if ((index >= 0) && (index < size)) {
			return (first + index) % queueData.length;
		}
		throw new IndexOutOfBoundsException("index=" + index + " size=" + size);
	}

	// -------------------------------------------------------------------
	// Serialization - only save minimum amount of data
	// -------------------------------------------------------------------

	private void writeObject(java.io.ObjectOutputStream out)
			throws java.io.IOException {
		int queueLen = queueData.length;
		out.defaultWriteObject();
		out.writeInt(queueLen);
		for (int i = 0, index = first; i < size; i++) {
			out.writeObject(queueData[index]);
			index = (index + 1) % queueLen;
		}
	}

	private void readObject(java.io.ObjectInputStream in)
			throws java.io.IOException, ClassNotFoundException {
		in.defaultReadObject();
		queueData = new Object[in.readInt()];
		for (int i = 0; i < size; i++) {
			queueData[i] = in.readObject();
		}
		first = 0;
		last = size;
	}

	// -------------------------------------------------------------------
	// Test Main
	// -------------------------------------------------------------------

	// public static void main(String[] args)
	// {
	// ArrayQueue q = new ArrayQueue(5);
	// q.add("1"); q.add("2"); q.add("3"); q.add("4");
	// System.out.print("ADD 1-4: ");q.dump();

	// q.add(0, "X"); System.out.print("ADD X@0: "); q.dump();
	// System.out.print("Remove 0: " + q.remove(0) + " "); q.dump();
	// System.out.print("Remove 0: " + q.remove(0) + " "); q.dump();
	// q.add("5");System.out.print("ADD 5: ");q.dump();
	// q.add("6");System.out.print("ADD 6: ");q.dump();

	// System.out.print("FORI: ");
	// for (int i = 0, n = q.size(); i < n; i++)
	// System.out.print(q.get(i) + " ");
	// q.dump();

	// System.out.print("INDX 4: " + q.indexOf("4") + " "); q.dump();
	// System.out.print("INDX 6: " + q.indexOf("6") + " "); q.dump();
	// System.out.print("INDX 0: " + q.indexOf("0") + " "); q.dump();

	// q.add("7");System.out.print("ADD 7: ");q.dump();

	// while (q.size() > 0)
	// {
	// System.out.print("Remove 0: " + q.remove(0) + " "); q.dump();
	// }
	// q.add("1"); q.add("2"); q.add("3"); q.add("4"); q.add("5");
	// System.out.print("ADD 1-5: ");q.dump();

	// System.out.print("Remove 2: " + q.remove(2) + " "); q.dump();
	// System.out.print("Remove 2: " + q.remove(2) + " "); q.dump();
	// System.out.print("Remove " + (q.size() - 1) + ": "
	// + q.remove(q.size() - 1) + " "); q.dump();
	// System.out.print("Remove " + (q.size() - 1) + ": "
	// + q.remove(q.size() - 1) + " "); q.dump();
	// System.out.print("INDX 4: " + q.indexOf("4") + "   "); q.dump();
	// System.out.print("INDX 6: " + q.indexOf("6") + "   "); q.dump();
	// System.out.print("INDX 0: " + q.indexOf("0") + "  "); q.dump();
	// }

	// private void dump()
	// {
	// System.out.print("[FST=" + first + ",LST=" + last
	// + ",SZ=" + size + ",LN=" + queueData.length + "]|");
	// for (int i = 0, n = queueData.length; i < n; i++)
	// {
	// System.out.print(" " + (queueData[i] == null ? "_" : queueData[i]));
	// }
	// System.out.println();
	// }

} // ArrayQueue
