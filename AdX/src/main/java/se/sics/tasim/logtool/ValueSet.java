/**
 * TAC Supply Chain Management Log Tools
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001-2004 SICS AB. All rights reserved.
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
 * ValueSet
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Mar 29 16:14:03 2004
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.logtool;

/**
 */
public class ValueSet {

	private int min = -1;
	private int max = -1;

	private int[] intervals;
	private int intervalsCount;

	private int[] values;
	private int valuesCount;

	public ValueSet(String info) {
		parseInfo(info);
	}

	private void parseInfo(String info) {
		int len = info.length();
		int index = 0;

		int currentValue = -1;
		int startValue = -1;
		while (index < len) {
			char c = info.charAt(index++);
			if (c <= 32) {
				while (index < len && (c = info.charAt(index++)) <= 32)
					;

				if (c >= '0' && c <= '9' && currentValue >= 0) {
					// special case: start of new value => break previous value
					index--;
					c = ',';
				} else if (c <= 32) {
					// String ending with space: break previous value
					c = ',';
				}
			}

			if (c >= '0' && c <= '9') {
				if (currentValue >= 0) {
					currentValue = currentValue * 10 + (c - '0');
				} else {
					currentValue = c - '0';
				}

			} else if (c == ',') {
				if (currentValue >= 0) {
					if (startValue >= 0) {
						addInterval(startValue, currentValue);
					} else {
						addValue(currentValue);
					}
					startValue = -1;
					currentValue = -1;
				} else if (startValue >= 0) {
					throw new IllegalArgumentException("no interval end: "
							+ startValue + "-...");
				}

			} else if (c == '-') {
				if (startValue >= 0) {
					// Already in interval
					throw new IllegalArgumentException("continuous interval: "
							+ startValue + "-...-");
				}

				if (currentValue < 0) {
					startValue = 1;
				} else {
					startValue = currentValue;
					currentValue = -1;
				}

			} else {
				throw new IllegalArgumentException("illegal character: " + c);
			}
		}
		if (currentValue >= 0) {
			if (startValue >= 0) {
				addInterval(startValue, currentValue);
			} else {
				addValue(currentValue);
			}
		} else if (startValue >= 0) {
			throw new IllegalArgumentException("no interval end: " + startValue
					+ "-...");
		}
	}

	private void addInterval(int start, int end) {
		if (end < start) {
			throw new IllegalArgumentException("illegal interval: " + start
					+ '-' + end);
		}
		if (start == end) {
			addValue(start);
			return;
		}

		if (intervals == null) {
			intervals = new int[10];
		} else if (intervals.length == intervalsCount) {
			intervals = setSize(intervals, intervalsCount + 10);
		}
		intervals[intervalsCount] = start;
		intervals[intervalsCount + 1] = end;
		intervalsCount += 2;
		setMaxMin(start, end);
	}

	private void addValue(int value) {
		if (values == null) {
			values = new int[10];
		} else if (values.length == valuesCount) {
			values = setSize(values, valuesCount + 10);
		}
		values[valuesCount++] = value;
		setMaxMin(value, value);
	}

	private void setMaxMin(int start, int end) {
		if (start < min || min < 0) {
			min = start;
		}
		if (end > max || max < 0) {
			max = end;
		}
	}

	private int[] setSize(int[] array, int size) {
		int[] tmp = new int[size];
		System.arraycopy(array, 0, tmp, 0, array.length);
		return tmp;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public boolean hasValues() {
		return min >= 0 && max >= min;
	}

	public boolean isIncluded(int value) {
		if (value > max || value < min) {
			return false;
		}

		for (int i = 0, n = intervalsCount; i < n; i += 2) {
			if (value >= intervals[i] && value <= intervals[i + 1]) {
				return true;
			}
		}
		for (int i = 0, n = valuesCount; i < n; i++) {
			if (values[i] == value) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		boolean comma = false;
		for (int i = 0, n = intervalsCount; i < n; i += 2) {
			if (comma)
				sb.append(',');
			else
				comma = true;
			sb.append(intervals[i]).append('-').append(intervals[i + 1]);
		}
		for (int i = 0, n = valuesCount; i < n; i++) {
			if (comma)
				sb.append(',');
			else
				comma = true;
			sb.append(values[i]);
		}
		return sb.toString();
	}

	// public static void main(String[] args) {
	// ValueSet m = new ValueSet("  -34, 33, 78- 78, 45 46 47 - 51 ");
	// System.out.println("ValueSet: " + m);
	// System.out.println("ValueSet: " + m.isIncluded(12)
	// + " 78: " + m.isIncluded(78)
	// + " 35: " + m.isIncluded(35));
	// }

} // ValueSet
