/*
 * @(#)ArrayUtils.java	Created date: 1999-11-30
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
 * 
 * 
 * @author Joakim Eriksson (joakim.eriksson@botbox.com)
 * @author Niclas Finne (niclas.finne@botbox.com)
 * @author Sverker Janson (sverker.janson@botbox.com)
 * @version $Revision: 4074 $, $Date: 2008-04-11 11:10:43 -0500 (Fri, 11 Apr
 *          2008) $
 */
public class ArrayUtils {

	// public static int indexOf(Object[] array, Object value)
	// {
	// if (array != null)
	// for (int i = 0; i < array.length; i++)
	// if (array[i] == value)
	// return i;
	// return -1;
	// }

	/**
	 * Returns the index of the specified value using equals() as comparison.
	 * This method handles if the array is <CODE>null</CODE>.
	 * 
	 * @param array
	 *            the array to search in
	 * @param element
	 *            the element to search for
	 * @return the index of the specified element or <CODE>-1</CODE> if the
	 *         element was not found in the array.
	 */
	public static int indexOf(Object[] array, Object element) {
		if (array != null) {
			if (element == null) {
				for (int i = 0, n = array.length; i < n; i++) {
					if (array[i] == null) {
						return i;
					}
				}
			} else {
				for (int i = 0, n = array.length; i < n; i++) {
					if (element.equals(array[i])) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	public static int indexOf(Object[] array, int start, int end, Object element) {
		if (element == null) {
			for (int i = start; i < end; i++) {
				if (array[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = start; i < end; i++) {
				if (element.equals(array[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	// Compares each nth object in the array with the specified key and
	// returns the index of the key if found
	public static int keyValuesIndexOf(Object[] array, int nth, Object key) {
		if (array != null) {
			if (key == null) {
				for (int i = 0, n = array.length; i < n; i += nth) {
					if (array[i] == null) {
						return i;
					}
				}
			} else {
				for (int i = 0, n = array.length; i < n; i += nth) {
					if (key.equals(array[i])) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	public static int keyValuesIndexOf(Object[] array, int nth, int start,
			int end, Object key) {
		if (key == null) {
			for (int i = start; i < end; i += nth) {
				if (array[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = start; i < end; i += nth) {
				if (key.equals(array[i])) {
					return i;
				}
			}
		}
		return -1;
	}

	public static int indexOf(int[] array, int element) {
		if (array != null) {
			for (int i = 0, n = array.length; i < n; i++) {
				if (element == array[i]) {
					return i;
				}
			}
		}
		return -1;
	}

	public static int indexOf(int[] array, int start, int end, int element) {
		for (int i = start; i < end; i++) {
			if (element == array[i]) {
				return i;
			}
		}
		return -1;
	}

	public static int keyValuesIndexOf(int[] array, int nth, int key) {
		if (array != null) {
			for (int i = 0, n = array.length; i < n; i += nth) {
				if (key == array[i]) {
					return i;
				}
			}
		}
		return -1;
	}

	public static int keyValuesIndexOf(int[] array, int nth, int start,
			int end, int key) {
		for (int i = start; i < end; i += nth) {
			if (key == array[i]) {
				return i;
			}
		}
		return -1;
	}

	public static int[] add(int[] array, int value) {
		if (array == null) {
			return new int[] { value };
		} else {
			int[] tmp = new int[array.length + 1];
			System.arraycopy(array, 0, tmp, 0, array.length);
			tmp[array.length] = value;
			return tmp;
		}
	}

	public static Object[] add(Object[] array, Object value) {
		Object[] tmp = (Object[]) java.lang.reflect.Array.newInstance(array
				.getClass().getComponentType(), array.length + 1);
		System.arraycopy(array, 0, tmp, 0, array.length);
		tmp[array.length] = value;
		return tmp;
	}

	public static Object[] add(Class componentType, Object[] array, Object value) {
		Object[] tmp;
		if (array == null) {
			tmp = (Object[]) java.lang.reflect.Array.newInstance(componentType,
					1);
		} else {
			tmp = (Object[]) java.lang.reflect.Array.newInstance(componentType,
					array.length + 1);
			System.arraycopy(array, 0, tmp, 0, array.length);
		}
		tmp[tmp.length - 1] = value;
		return tmp;
	}

	public static Object[] insert(Object[] array, int index, int number) {
		if ((index < 0) || (index > array.length)) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		Object[] tmp = (Object[]) java.lang.reflect.Array.newInstance(array
				.getClass().getComponentType(), array.length + number);
		if (index > 0) {
			System.arraycopy(array, 0, tmp, 0, index);
		}
		if (index < array.length) {
			System.arraycopy(array, index, tmp, index + number, array.length
					- index);
		}
		return tmp;
	}

	public static Object[] insert(Class componentType, Object[] array,
			int index, int number) {
		int len = array == null ? 0 : array.length;
		if ((index < 0) || (index > len)) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		Object[] tmp = (Object[]) java.lang.reflect.Array.newInstance(
				componentType, len + number);
		if (index > 0) {
			System.arraycopy(array, 0, tmp, 0, index);
		}
		if (index < len) {
			System.arraycopy(array, index, tmp, index + number, array.length
					- index);
		}
		return tmp;
	}

	/**
	 * Concatenates the two arrays. The second array will be returned if the
	 * first array is null and vice versa. An actual concatenation is only
	 * performed if both arrays are non-null. This method requires that the two
	 * arrays are of the same component type.
	 * 
	 * @param array1
	 *            the first array to concatenate (may be NULL)
	 * @param array2
	 *            the second array to concatenate (may be NULL)
	 * @return the concatenation or NULL if both arrays were NULL
	 */
	public static Object[] concat(Object[] array1, Object[] array2) {
		if (array1 == null) {
			return array2;
		} else if (array2 == null) {
			return array1;
		}

		Object[] tmp = (Object[]) java.lang.reflect.Array.newInstance(array1
				.getClass().getComponentType(), array1.length + array2.length);
		System.arraycopy(array1, 0, tmp, 0, array1.length);
		System.arraycopy(array2, 0, tmp, array1.length, array2.length);
		return tmp;
	}

	/**
	 * Removes the element at the specified index. If the array only contains
	 * one element (the removed one), <CODE>null</CODE> is returned.
	 * 
	 * @param array
	 *            the array to remove the element from
	 * @param index
	 *            the index of the element to remove
	 * @return the array with the element removed or <CODE>null</CODE> if the
	 *         array no longer contains any elements.
	 */
	public static Object[] remove(Object[] array, int index) {
		// Check that the index is a valid index
		if ((index < 0) || (index >= array.length))
			throw new ArrayIndexOutOfBoundsException(index);

		if (array.length == 1)
			return null;

		Object[] tmp = (Object[]) java.lang.reflect.Array.newInstance(array
				.getClass().getComponentType(), array.length - 1);
		if (index > 0) {
			System.arraycopy(array, 0, tmp, 0, index);
		}
		if (index < tmp.length) {
			System.arraycopy(array, index + 1, tmp, index, tmp.length - index);
		}
		return tmp;
	}

	public static Object[] remove(Object[] array, Object element) {
		int index = indexOf(array, element);
		return (index >= 0) ? remove(array, index) : array;
	}

	public static Object[] remove(Object[] array, int index, int number) {
		// Check that the index is a valid index
		if ((index < 0) || (index >= array.length)) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		if ((array.length - index) < number) {
			number = array.length - index;
		}

		if ((index == 0) && (array.length == number)) {
			return null;
		}

		Object[] tmp = (Object[]) java.lang.reflect.Array.newInstance(array
				.getClass().getComponentType(), array.length - number);
		if (index > 0) {
			System.arraycopy(array, 0, tmp, 0, index);
		}
		if (index + number < array.length) {
			System.arraycopy(array, index + number, tmp, index, array.length
					- index - number);
		}
		return tmp;
	}

	public static Object[] setSize(Object[] array, int newSize) {
		if (array.length == newSize) {
			return array;
		}

		Object[] tmp = (Object[]) java.lang.reflect.Array.newInstance(array
				.getClass().getComponentType(), newSize);
		if (newSize > array.length) {
			System.arraycopy(array, 0, tmp, 0, array.length);
		} else {
			System.arraycopy(array, 0, tmp, 0, newSize);
		}
		return tmp;
	}

	// public static Object[] setSize(Class type, Object[] array, int newSize)
	// {
	// if (array == null)
	// {
	// return (Object[]) java.lang.reflect.Array.newInstance(type, newSize);
	// }

	// if (array.length == newSize)
	// {
	// return array;
	// }

	// Object[] tmp = (Object[])
	// java.lang.reflect.Array.newInstance(type, newSize);
	// if (newSize > array.length)
	// {
	// System.arraycopy(array, 0, tmp, 0, array.length);
	// }
	// else
	// {
	// System.arraycopy(array, 0, tmp, 0, newSize);
	// }
	// return tmp;
	// }

	public static char[] setSize(char[] array, int newSize) {
		if (array.length == newSize) {
			return array;
		}

		char[] tmp = new char[newSize];
		if (newSize > array.length) {
			System.arraycopy(array, 0, tmp, 0, array.length);
		} else {
			System.arraycopy(array, 0, tmp, 0, newSize);
		}
		return tmp;
	}

	public static byte[] setSize(byte[] array, int newSize) {
		if (array.length == newSize) {
			return array;
		}

		byte[] tmp = new byte[newSize];
		if (newSize > array.length) {
			System.arraycopy(array, 0, tmp, 0, array.length);
		} else {
			System.arraycopy(array, 0, tmp, 0, newSize);
		}
		return tmp;
	}

	public static float[] setSize(float[] array, int newSize) {
		if (array.length == newSize) {
			return array;
		}

		float[] tmp = new float[newSize];
		if (newSize > array.length) {
			System.arraycopy(array, 0, tmp, 0, array.length);
		} else {
			System.arraycopy(array, 0, tmp, 0, newSize);
		}
		return tmp;
	}

	public static double[] setSize(double[] array, int newSize) {
		if (array.length == newSize) {
			return array;
		}

		double[] tmp = new double[newSize];
		if (newSize > array.length) {
			System.arraycopy(array, 0, tmp, 0, array.length);
		} else {
			System.arraycopy(array, 0, tmp, 0, newSize);
		}
		return tmp;
	}

	public static boolean[] setSize(boolean[] array, int newSize) {
		if (array.length == newSize) {
			return array;
		}

		boolean[] tmp = new boolean[newSize];
		if (newSize > array.length) {
			System.arraycopy(array, 0, tmp, 0, array.length);
		} else {
			System.arraycopy(array, 0, tmp, 0, newSize);
		}
		return tmp;
	}

	public static int[] setSize(int[] array, int newSize) {
		if (array.length == newSize) {
			return array;
		}

		int[] tmp = new int[newSize];
		if (newSize > array.length) {
			System.arraycopy(array, 0, tmp, 0, array.length);
		} else {
			System.arraycopy(array, 0, tmp, 0, newSize);
		}
		return tmp;
	}

	public static long[] setSize(long[] array, int newSize) {
		if (array.length == newSize) {
			return array;
		}

		long[] tmp = new long[newSize];
		if (newSize > array.length) {
			System.arraycopy(array, 0, tmp, 0, array.length);
		} else {
			System.arraycopy(array, 0, tmp, 0, newSize);
		}
		return tmp;
	}

	public static String toString(Object[] array) {
		if (array == null) {
			return "null";
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append(array.getClass().getComponentType().getName())
					.append('[');
			if (array.length > 0) {
				sb.append(array[0]);

				for (int i = 1; i < array.length; i++) {
					sb.append(',').append(array[i]);
				}
			}
			return sb.append(']').toString();
		}
	}

	/*****************************************************************************
	 * Test Main
	 ****************************************************************************/

	// public static void main(String[] args)
	// {
	// String[] test = new String[0];
	// System.out.println(toString(test));
	// test = (String[]) add(test, "t1");
	// test = (String[]) add(test, "t2");
	// test = (String[]) add(test, "t3");
	// test = (String[]) add(test, "t4");
	// test = (String[]) add(test, "t5");
	// System.out.println(toString(test));
	// test = (String[]) remove(test, 2);
	// test = (String[]) remove(test, 0);
	// System.out.println(toString(test));
	// test = (String[]) remove(test, 0);
	// test = (String[]) remove(test, 0);
	// test = (String[]) remove(test, 0);
	// System.out.println(toString(test));
	// if (test == null)
	// {
	// test = new String[0];
	// }
	// test = (String[]) add(test, "c1");
	// test = (String[]) add(test, "c2");
	// test = (String[]) add(test, "c3");
	// test = (String[]) add(test, "c4");
	// test = (String[]) add(test, "c5");
	// System.out.println(toString(test));
	// test = (String[]) insert(test, 2, 5);
	// System.out.println(toString(test));
	// test = (String[]) remove(test, 0, 5);
	// System.out.println(toString(test));
	// }
}
