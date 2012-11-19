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
 * FormatUtils
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Jun 25 10:20:05 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.util;

/**
 */
public class FormatUtils {

	// Prevent instances of this class
	private FormatUtils() {
	}

	/**
	 * Formats a currency amount as: <em>ddd ddd ddd[.ddd] [M]</em>
	 * 
	 * @param amount
	 *            the amount to format
	 * @return the formatted amount as a string
	 */
	public static String formatAmount(long amount) {
		long aAmount = amount >= 0 ? amount : -amount;
		String str = Long.toString(aAmount);
		boolean meg = str.length() > 6;
		StringBuffer sb = new StringBuffer();
		if (amount < 0)
			sb.append('-');
		int slen = str.length() - (meg ? 6 : 0);
		int pos = 0;
		for (int i = slen; i > 0; i--) {
			sb.append(str.charAt(slen - i));
			if ((i > 1) && ((2 + slen - pos) % 3 == 0)) {
				sb.append(' ');
			}
			pos++;
		}

		// If not four chars & Meg!
		if (pos < 4 && meg) {
			sb.append('.');
			while (pos < 4) {
				sb.append(str.charAt(slen++));
				pos++;
			}
		}

		if (meg)
			sb.append(" M");
		return sb.toString();
	}

	/**
	 * Formats a value using spaces to separate thousands as:
	 * <em>ddd ddd ddd</em>.
	 * 
	 * @param value
	 *            the value to format
	 * @return the formatted value as a string
	 */
	public static String formatLong(long value) {
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

	/**
	 * Formats a value using the specified separator to separating thousands as:
	 * <em>ddd&amp;nbsp;ddd&amp;nbsp;ddd</em> (with "&amp;nbsp;" as separator).
	 * 
	 * @param value
	 *            the value to format
	 * @param separator
	 *            the separator to use
	 * @return the formatted value as a string
	 */
	public static String formatLong(long value, String separator) {
		boolean isNegative = value < 0;
		if (isNegative) {
			value = -value;
		}

		// -9 223 372 036 854 775 808
		int sepLen = separator.length();
		int maxLen = 1 + 19 + 6 * sepLen;
		char[] buffer = new char[maxLen];

		int index = maxLen - 1;
		if (value == 0) {
			buffer[index--] = '0';
		} else {
			for (int count = 0; value > 0 && index >= 0; count++) {
				if (((count % 3) == 0) && count > 0 && index > sepLen) {
					index -= sepLen;
					separator.getChars(0, sepLen, buffer, index + 1);
				}
				buffer[index--] = (char) ('0' + (value % 10));
				value /= 10;
			}
		}

		if (isNegative && index >= 0) {
			buffer[index--] = '-';
		}
		return new String(buffer, index + 1, maxLen - index - 1);
	}

	/**
	 * Formats a value with two decimals, using spaces to separate thousands as:
	 * <em>ddd ddd ddd.dd</em>.
	 * 
	 * @param value
	 *            the value to format
	 * @return the formatted value as a string
	 */
	public static String formatDouble(double value) {
		boolean isNegative = value < 0.0 || (value == 0.0 && 1 / value < 0.0);
		if (isNegative) {
			value = -value;
		}

		// -9 223 372 036 854 775 808.00
		char[] buffer = new char[1 + 19 + 6 + 3];

		long intValue = (long) value;
		int decValue = (int) ((value - intValue) * 100 + 0.5);
		int index = buffer.length - 1;

		buffer[index--] = (char) ('0' + (decValue % 10));
		buffer[index--] = (char) ('0' + ((decValue / 10) % 10));
		buffer[index--] = '.';

		if (intValue == 0) {
			buffer[index--] = '0';
		} else {
			for (int count = 0; intValue > 0 && index >= 0; count++) {
				if (((count % 3) == 0) && count > 0 && index > 0) {
					buffer[index--] = ' ';
				}
				buffer[index--] = (char) ('0' + (intValue % 10));
				intValue /= 10;
			}
		}

		if (isNegative && index >= 0) {
			buffer[index--] = '-';
		}
		return new String(buffer, index + 1, buffer.length - index - 1);

		// long i = (long) value;
		// if (value < 0) {
		// value = -value;
		// }
		// long dec = ((long) (0.5 + value * 100)) % 100;
		// return "" + i + '.' + (dec < 10 ? "0" : "") + dec;
	}

	/**
	 * Formats a value with two decimals, using the specified separator to
	 * separating thousands as: <em>ddd&amp;nbsp;ddd&amp;nbsp;ddd.dd</em> (with
	 * "&amp;nbsp;" as separator).
	 * 
	 * @param value
	 *            the value to format
	 * @param separator
	 *            the separator to use
	 * @return the formatted value as a string
	 */
	public static String formatDouble(double value, String separator) {
		boolean isNegative = value < 0.0 || (value == 0.0 && 1 / value < 0.0);
		if (isNegative) {
			value = -value;
		}

		// -9 223 372 036 854 775 808.00
		int sepLen = separator.length();
		int maxLen = 1 + 19 + 6 * sepLen + 3;
		char[] buffer = new char[maxLen];

		long intValue = (long) value;
		int decValue = (int) ((value - intValue) * 100 + 0.5);
		int index = maxLen - 1;

		buffer[index--] = (char) ('0' + (decValue % 10));
		buffer[index--] = (char) ('0' + ((decValue / 10) % 10));
		buffer[index--] = '.';

		if (intValue == 0) {
			buffer[index--] = '0';
		} else {
			for (int count = 0; intValue > 0 && index >= 0; count++) {
				if (((count % 3) == 0) && count > 0 && index > sepLen) {
					index -= sepLen;
					separator.getChars(0, sepLen, buffer, index + 1);
				}
				buffer[index--] = (char) ('0' + (intValue % 10));
				intValue /= 10;
			}
		}

		if (isNegative && index >= 0) {
			buffer[index--] = '-';
		}
		return new String(buffer, index + 1, maxLen - index - 1);
	}

	// -------------------------------------------------------------------
	// Test main
	// -------------------------------------------------------------------

	// public static void main(String[] args) {
	// if (args[0].indexOf('.') >= 0) {
	// System.out.println("'" + args[0] + "' => '"
	// + formatDouble(Double.parseDouble(args[0])) + '\'');
	// System.out.println("'" + args[0] + "' => '"
	// + formatDouble(Double.parseDouble(args[0]),
	// "&nbsp;") + '\'');
	// } else {
	// System.out.println("'" + args[0] + "' => '"
	// + formatLong(Long.parseLong(args[0])) + '\'');
	// System.out.println("'" + args[0] + "' => '"
	// + formatLong(Long.parseLong(args[0]), "&nbsp;")
	// + '\'');
	// }

	// // Interesting case for which the formatters will not work because
	// // both v and -v are negative!
	// // System.out.println();
	// // long T = -9223372036854775808L;
	// // long TT = -T;
	// // System.out.println(T);
	// // System.out.println(TT);
	// // System.out.println(-T);
	// // System.out.println(T == TT);
	// }

} // FormatUtils
