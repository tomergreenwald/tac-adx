/*
 * @(#)HtmlWriter.java	Created date: 01-11-21
 * $Revision: 4074 $, $Date: 2008-04-11 11:10:43 -0500 (Fri, 11 Apr 2008) $
 *
 * Copyright (c) 2001 BotBox AB.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * BotBox AB. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * BotBox AB.
 *
 * \TODO:
 *  - int id = defineTable(TableStyle style);
 *    (add new table styles)
 *  - better form API (form styles)
 */

package com.botbox.html;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import com.botbox.util.ArrayUtils;

/**
 * Provides simpler generation of HTML.
 * 
 * Depends on com.botbox.util.ArrayUtils.
 * 
 * This class is NOT thread safe!!!
 * 
 * @author Joakim Eriksson (joakim.eriksson@botbox.com)
 * @author Niclas Finne (niclas.finne@botbox.com)
 * @author Sverker Janson (sverker.janson@botbox.com)
 * @version $Revision: 4074 $, $Date: 2008-02-24 12:03:02 -0500 (Sun, 24 Feb
 *          2008) $
 */
public class HtmlWriter {

	private final static boolean DEBUG = false;

	// Always use CRLF as line breaks in HTML
	private final static String EOL = "\r\n";

	public final static int NORMAL = 0;
	public final static int BORDERED = 1;
	public final static int LINED = 2;

	// Modes: 8 lower bits => mode type
	// 8 highest bits => style type
	private final static int BODY = 1;
	private final static int TABLE = 2;
	private final static int COLGROUP = 3;
	private final static int FORM = 4;

	// Attribute mode TAG_OPEN: during the time a tag is 'open'
	// attributes can be added. No attributes can be added to a tag
	// once it has closed.
	private final static int TAG_OPEN = 1 << 10;

	// Attribute mode CACHE_ATTR: used in nested tags where attributes
	// needs to be specified in several levels simultaneously (the
	// attributes for the inner tags are cached until the complete tag
	// set can be closed). Can only be used in combination with
	// TAG_OPEN.
	private final static int CACHE_ATTR = 1 << 12;

	// The tag or tag set is still opened and must be closed before any
	// other tag or text are added (only attributes can be added while a
	// tag or tag set is opened).
	private final static int PENDING = TAG_OPEN | CACHE_ATTR;

	// Add a new line after this mode ended
	private final static int NEWLINE = 1 << 13;

	// Table extra types
	private final static int TABLE_TR = 1 << 14;
	private final static int TABLE_TD = 1 << 15;
	private final static int TABLE_TH = 1 << 16;

	private Writer out;
	private boolean autoflush;
	private boolean inError;

	private char[] buffer = new char[1024];
	private int size = 0;

	private String[] outerAttrCache = null;
	private int outerAttrSize = 0;
	private String outerAttributes;

	private String[] innerAttrCache = null;
	private int innerAttrSize = 0;
	private String innerAttributes;

	private int[] modes = new int[6];
	private int modeCounter = 0;

	private TableStyle currentTableStyle;

	/** Headings */

	// Heading 1
	private String heading1Start = "<p><font size='+3' face=arial><b>";
	private String heading1End = "</b></font><br><br>";
	// Heading 2
	private String heading2Start = "<p><font size='+2' face=arial><b>";
	private String heading2End = "</b></font><br><br>";
	// Heading 3
	private String heading3Start = "<p><font size='+1' face=arial><b>";
	private String heading3End = "</b></font><br><br>";
	// Heading 4
	private String heading4Start = "<p><font face=arial><b>";
	private String heading4End = "</b></font><br><br>";

	/*****************************************************************************
   * 
   ****************************************************************************/

	public HtmlWriter() {
	}

	public HtmlWriter(OutputStream out) {
		this(out, true);
	}

	public HtmlWriter(OutputStream out, boolean autoflush) {
		this.out = new BufferedWriter(new OutputStreamWriter(out));
		this.autoflush = autoflush;
	}

	public HtmlWriter(Writer out) {
		this(out, true);
	}

	public HtmlWriter(Writer out, boolean autoflush) {
		this.out = out;
		this.autoflush = autoflush;
	}

	public boolean checkError() {
		return inError;
	}

	public void ensureCapacity(int newSize) {
		if (newSize > buffer.length) {
			expandBuffer(newSize);
		}
	}

	public void write(Writer out) throws IOException {
		out.write(buffer, 0, size);
	}

	public int size() {
		return size;
	}

	public void flush() {
		if (out != null) {
			try {
				out.write(buffer, 0, size);
				size = 0;
				out.flush();
			} catch (IOException e) {
				inError = true;
			}
		}
	}

	public void close() {
		// Close all open tags
		int mode = popMode();
		if (mode >= 0) {
			do {
				handleMode(mode);
			} while ((mode = popMode()) >= 0);
		}
		flush();
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				inError = true;
			} finally {
				out = null;
			}
		}
	}

	public String toString() {
		return new String(buffer, 0, size);
	}

	/*****************************************************************************
	 * Page header/footer
	 ****************************************************************************/

	public HtmlWriter pageStart(String title) {
		return pageStart(title, null);
	}

	public HtmlWriter pageStart(String title, String headData) {
		checkMode();
		a("<html>").a(EOL).a("<head>");
		if (title != null) {
			a("<title>").a(title).a("</title>").a(EOL);
		}
		if (headData != null) {
			a(headData).a(EOL);
		}
		a("</head>").a(EOL).a("<body");
		pushMode(BODY | TAG_OPEN | NEWLINE);
		return this;
	}

	public HtmlWriter pageEnd() {
		// Close all open tags
		int mode = popMode(BODY);
		if (mode >= 0) {
			handleMode(mode);
		}
		return this;
	}

	private void pageEnd(int mode) {
		// Only one page mode supported at this time (BODY)
		a(EOL).a("</body>").a(EOL).a("</html>");
	}

	/*****************************************************************************
	 * Table structure handling
	 ****************************************************************************/

	public HtmlWriter table() {
		return table(NORMAL, null);
	}

	public HtmlWriter table(String attributes) {
		return table(NORMAL, attributes);
	}

	public HtmlWriter table(int type) {
		return table(type, null);
	}

	public HtmlWriter table(int type, String attributes) {
		checkMode();

		currentTableStyle = getTableStyle(type);
		innerAttributes = currentTableStyle.getAttributes();
		outerAttributes = currentTableStyle.getOuterAttributes();
		a("<table");
		if (currentTableStyle.isDoubleTable()) {
			pushMode(TABLE | TAG_OPEN | CACHE_ATTR | NEWLINE | (type << 24));
		} else {
			pushMode(TABLE | NEWLINE | (type << 24));
			pushMode(innerAttributes != null ? (TAG_OPEN | NEWLINE | CACHE_ATTR)
					: (TAG_OPEN | NEWLINE));
		}
		if (attributes != null) {
			attr(attributes);
		}
		return this;
	}

	private TableStyle getTableStyle(int type) {
		switch (type) {
		case NORMAL:
			return TableStyle.getNormalTable();
		case BORDERED:
			return TableStyle.getBorderTable();
		case LINED:
			return TableStyle.getLineTable();
		default:
			System.err
					.println("HtmlWriter: could not find table style " + type);
			return TableStyle.getNormalTable();
		}
	}

	private void updateTable() {
		int mode = peekMode(TABLE);
		if (mode >= 0) {
			// Only update if we are inside a table
			currentTableStyle = getTableStyle((mode >> 24) & 0xff);
		}
	}

	private int endData(int mode) {
		if ((mode & TABLE_TH) != 0) {
			a("</th>");
			mode -= TABLE_TH;
		}
		if ((mode & TABLE_TD) != 0) {
			a("</td>");
			mode -= TABLE_TD;
		}
		return mode;
	}

	private int endRow(int mode) {
		mode = endData(mode);
		if ((mode & TABLE_TR) != 0) {
			a("</tr>").a(EOL);
			mode -= TABLE_TR;
		}
		return mode;
	}

	public HtmlWriter tableEnd() {
		int mode = popMode(TABLE);
		if (mode >= 0) {
			handleMode(mode);
		}
		return this;
	}

	private void tableEnd(int mode) {
		// Time to end the table
		endRow(mode);
		if (currentTableStyle.isDoubleTable()) {
			a("</table></td></tr></table>");
		} else {
			a("</table>");
		}
		updateTable();
	}

	private int tableHeaderEnd(int mode) {
		// Time to close the table start
		if (currentTableStyle.isDoubleTable()) {
			a("><tr><td>").a("<table");
		}
		return mode | NEWLINE;
	}

	/*****************************************************************************
	 * Table Data Handling
	 ****************************************************************************/

	public HtmlWriter tr() {
		int mode = popMode(TABLE);
		if (mode >= 0) {
			mode = endRow(mode);
			a("<tr");
			pushMode(mode | TABLE_TR);
			// Need to use a special mode to preserve the NEWLINE in the above
			// mode
			innerAttributes = currentTableStyle.getTrAttributes();
			pushMode(innerAttributes != null ? TAG_OPEN | CACHE_ATTR : TAG_OPEN);
		}
		return this;
	}

	public HtmlWriter th() {
		return th(null, null);
	}

	public HtmlWriter th(String text) {
		return th(text, null);
	}

	public HtmlWriter th(String text, String attributes) {
		td("<th", text, attributes, TABLE_TH);
		return this;
	}

	public HtmlWriter td() {
		return td(null, null);
	}

	public HtmlWriter td(String text) {
		return td(text, null);
	}

	public HtmlWriter td(String text, String attributes) {
		td("<td", text, attributes, TABLE_TD);
		return this;
	}

	private void td(String tag, String text, String attributes, int tagType) {
		int mode = popMode(TABLE);
		if (mode >= 0) {
			mode = endData(mode);
			if ((mode & TABLE_TR) == 0) {
				String trAtts = currentTableStyle.getTrAttributes();
				a("<tr");
				if (trAtts != null) {
					a(' ').a(trAtts);
				}
				a('>');
				mode |= TABLE_TR;
			}
			a(tag);
			pushMode(mode | tagType);

			String tdAtts = tagType == TABLE_TH ? currentTableStyle
					.getThAttributes() : currentTableStyle.getTdAttributes();
			if (text != null && text.length() > 0) {
				if (attributes != null && attributes.length() > 0) {
					pushMode(tdAtts != null ? TAG_OPEN | CACHE_ATTR : TAG_OPEN);
					innerAttributes = tdAtts;
					attr(attributes);
				} else {
					if (tdAtts != null) {
						a(' ').a(tdAtts);
					}
					a('>');
				}
				text(text);
			} else {
				pushMode(tdAtts != null ? TAG_OPEN | CACHE_ATTR : TAG_OPEN);
				innerAttributes = tdAtts;
				if (attributes != null && attributes.length() > 0) {
					attr(attributes);
				}
			}
		}
	}

	public HtmlWriter colgroup(int span) {
		return colgroup(span, null);
	}

	public HtmlWriter colgroup(int span, String attributes) {
		int mode = popMode(TABLE);
		if (mode >= 0) {
			mode = endRow(mode);
			a("<colgroup span=").a(span);
			if (attributes != null && attributes.length() > 0) {
				a(' ').a(attributes);
			}
			pushMode(mode);
			pushMode(COLGROUP | TAG_OPEN | NEWLINE);
		}
		return this;
	}

	private int colgroupHeaderEnd(int mode) {
		a("></colgroup");
		// The ending of a colgroup always absorbs the type because
		// nothing else can be added to it
		return 0;
	}

	/*****************************************************************************
	 * Form handling
	 ****************************************************************************/

	public HtmlWriter form() {
		return form(null, null, null);
	}

	public HtmlWriter form(String action) {
		return form(action, null, null);
	}

	public HtmlWriter form(String action, String method) {
		return form(action, method, null);
	}

	public HtmlWriter form(String action, String method, String attributes) {
		checkMode();
		a("<form");
		if (action != null && action.length() > 0) {
			a(" action='").a(action).a('\'');
		}
		if (method != null && method.length() > 0) {
			a(" method='").a(method).a('\'');
		}
		if (attributes != null && attributes.length() > 0) {
			a(' ').a(attributes);
		}
		pushMode(FORM | NEWLINE);
		pushMode(TAG_OPEN | NEWLINE);
		return this;
	}

	public HtmlWriter formEnd() {
		int mode = popMode(FORM);
		if (mode >= 0) {
			handleMode(mode);
		}
		return this;
	}

	private void formEnd(int mode) {
		a("</form>").a(EOL);
	}

	/*****************************************************************************
	 * Headings
	 ****************************************************************************/

	public HtmlWriter h1(String text) {
		checkMode();
		return a(EOL).a(heading1Start).a(text).a(heading1End).a(EOL)
				.addNewLine();
	}

	public HtmlWriter h2(String text) {
		checkMode();
		return a(EOL).a(heading2Start).a(text).a(heading2End).a(EOL)
				.addNewLine();
	}

	public HtmlWriter h3(String text) {
		checkMode();
		return a(EOL).a(heading3Start).a(text).a(heading3End).a(EOL)
				.addNewLine();
	}

	public HtmlWriter h4(String text) {
		checkMode();
		return a(EOL).a(heading4Start).a(text).a(heading4End).a(EOL)
				.addNewLine();
	}

	/*****************************************************************************
	 * Text
	 ****************************************************************************/

	public HtmlWriter tag(String name) {
		return tag(name, null);
	}

	public HtmlWriter tag(String name, String attributes) {
		checkMode();
		a('<').a(name);
		pushMode(TAG_OPEN);
		if (attributes != null && attributes.length() > 0) {
			attr(attributes);
		}
		return this;
	}

	public HtmlWriter tag(char name) {
		return tag(name, null);
	}

	public HtmlWriter tag(char name, String attributes) {
		checkMode();
		a('<').a(name);
		pushMode(TAG_OPEN);
		if (attributes != null && attributes.length() > 0) {
			attr(attributes);
		}
		return this;
	}

	public HtmlWriter tagEnd(String name) {
		checkMode();
		return a('<').a('/').a(name).a('>').a(EOL);
	}

	public HtmlWriter tagEnd(char name) {
		checkMode();
		return a('<').a('/').a(name).a('>').a(EOL);
	}

	/*****************************************************************************
	 * Convenient way to specify some common stuff
	 ****************************************************************************/

	public HtmlWriter comment(String comment) {
		checkMode();
		return a(EOL).a("<!-- ").a(comment).a(" -->").a(EOL);
	}

	public HtmlWriter p() {
		checkMode();
		a("<p");
		pushMode(TAG_OPEN | NEWLINE);
		return this;
	}

	/*****************************************************************************
	 * Convenient way to specify some common attributes
	 ****************************************************************************/

	// public HtmlWriter width(int width)
	// {
	// return attr("width", width);
	// }
	// public HtmlWriter width(String width)
	// {
	// return attr("width", width);
	// }
	// public HtmlWriter border(int border)
	// {
	// return attr("border", border);
	// }
	// public HtmlWriter bgcolor(String color)
	// {
	// return attr("bgcolor", color);
	// }
	/*****************************************************************************
	 * Attribute handling
	 ****************************************************************************/

	public HtmlWriter attr(String name, int value) {
		return attr(name, Integer.toString(value));
	}

	public HtmlWriter attr(String name, long value) {
		return attr(name, Long.toString(value));
	}

	public HtmlWriter attr(String name, String value) {
		int mode = peekMode();
		if (mode <= 0 || (mode & PENDING) == 0) {
			// We are not in an open tag and attributes can not be added
			System.err
					.println("HtmlWriter: could not add attribute outside tag: "
							+ name + "='" + value + '\'');
			return this;
		}

		// Check if we should cache attributes to a later time. This
		// indicates that a tag set is added where some attributes can not
		// be added until the complete set is closed. An example is
		// bordered tables that consists of two HTML tables where some
		// attributes should be in the external table and some in the
		// internal.
		if ((mode & CACHE_ATTR) != 0) {
			if (outerAttributes != null) {
				// \TODO Should cache this result.
				parseAttributes(outerAttributes, 0);
				outerAttributes = null;
			}

			if (innerAttributes != null) {
				// \TODO Should cache this result.
				parseAttributes(innerAttributes, 1);
				innerAttributes = null;
			}

			// Might need to special handle attributes
			if ((mode & TABLE) != 0) {
				// Only nested table sets have the CACHE_ATTR flag set
				if ("width".equals(name)) {
					addInnerAttr("width", value);

					// The inner table must always have width = 100% if it
					// should
					// completely fill the external table
					addOuterAttr("width", "100%");
					return this;
				}
			}
			// All other attributes are stored in the attribute cache
			addInnerAttr(name, value);
		} else {
			addAttr(name, value);
		}
		return this;
	}

	private void addOuterAttr(String name, String value) {
		// Add to attribute cache
		int size = outerAttrSize * 2;
		int index = ArrayUtils.keyValuesIndexOf(outerAttrCache, 2, 0, size,
				name);
		if (index >= 0) {
			outerAttrCache[index + 1] = value;
		} else {
			if (outerAttrCache == null) {
				outerAttrCache = new String[10];
			} else if (size == outerAttrCache.length) {
				outerAttrCache = (String[]) ArrayUtils.setSize(outerAttrCache,
						size + 10);
			}
			outerAttrCache[size] = name;
			outerAttrCache[size + 1] = value;
			outerAttrSize++;
		}
	}

	private void flushOuterAttributes() {
		if (outerAttrSize > 0) {
			// Make sure space is available for one more character because
			// the tag must always be closed
			for (int index = 0, n = outerAttrSize * 2; index < n; index += 2) {
				addAttr(outerAttrCache[index], outerAttrCache[index + 1]);
				outerAttrCache[index] = outerAttrCache[index + 1] = null;
			}
			outerAttrSize = 0;
		}
	}

	private void addInnerAttr(String name, String value) {
		// Add to attribute cache
		int size = innerAttrSize * 2;
		int index = ArrayUtils.keyValuesIndexOf(innerAttrCache, 2, 0, size,
				name);
		if (index >= 0) {
			innerAttrCache[index + 1] = value;
		} else {
			if (innerAttrCache == null) {
				innerAttrCache = new String[10];
			} else if (size == innerAttrCache.length) {
				innerAttrCache = (String[]) ArrayUtils.setSize(innerAttrCache,
						size + 10);
			}
			innerAttrCache[size] = name;
			innerAttrCache[size + 1] = value;
			innerAttrSize++;
		}
	}

	private void flushInnerAttributes() {
		if (innerAttrSize > 0) {
			// Make sure space is available for one more character because
			// the tag must always be closed
			for (int index = 0, n = innerAttrSize * 2; index < n; index += 2) {
				addAttr(innerAttrCache[index], innerAttrCache[index + 1]);
				innerAttrCache[index] = innerAttrCache[index + 1] = null;
			}
			innerAttrSize = 0;
		}
	}

	private void addAttr(String name, String value, int type) {
		if (type == 2) {
			attr(name, value);
		} else if (type == 0) {
			addOuterAttr(name, value);
		} else {
			addInnerAttr(name, value);
		}
	}

	private void addAttr(String name, String value) {
		int nameLen = name.length();
		int valLen;
		int len;
		if (value == null || value.length() == 0) {
			valLen = 0;
			len = 1 + name.length();
		} else {
			// Make sure there is room for the largest possible attribute
			// in which case every character is stuffed
			// i.e. ' ' + name + '=' + '"' + stuff(value) + '"'
			valLen = value.length();
			len = 1 + name.length() + 1 + 1 + valLen * 2 + 1;
		}

		// Add to buffer
		len += size;
		if (len > buffer.length) {
			expandBuffer(len);
		}

		buffer[size++] = ' ';
		name.getChars(0, nameLen, buffer, size);
		size += nameLen;

		if (valLen > 0) {
			buffer[size++] = '=';
			if (value.indexOf('\'') < 0) {
				// value ok to add directly
				buffer[size++] = '\'';
				value.getChars(0, valLen, buffer, size);
				size += valLen;
				buffer[size++] = '\'';
			} else if (value.indexOf('"') < 0) {
				buffer[size++] = '"';
				value.getChars(0, valLen, buffer, size);
				size += valLen;
				buffer[size++] = '"';
			} else {
				// Both ' and " in the value. We are in big trouble! For now try
				// to stuff " with backspace and hope for the best.
				buffer[size++] = '"';
				for (int i = 0; i < valLen; i++) {
					char c = value.charAt(i);
					if (c == '"') {
						buffer[size++] = '\\';
					}
					buffer[size++] = c;
				}
				buffer[size++] = '"';
			}
		}
	}

	// Add the possibility to specify attributes directly as a string.
	public HtmlWriter attr(String attributes) {
		int mode = peekMode();
		if (mode <= 0 || (mode & PENDING) == 0) {
			// We are not in an open tag and attributes can not be added
			System.err
					.println("HtmlWriter: could not add attribute outside tag: "
							+ attributes);
			return this;
		}

		// Check if we should cache the attributes until a later time
		if ((mode & CACHE_ATTR) != 0) {
			parseAttributes(attributes, 2);
		} else {
			a(' ').a(attributes);
		}
		return this;
	}

	private void parseAttributes(String attributes, int type) {
		int len = attributes.length();
		int mode = 0;
		boolean inValue = false;
		boolean isStuffed = false;
		char quoteDelimiter = ' ';
		int start = 0;
		char c;
		String name = null;
		if (DEBUG)
			System.out.println("Parsing '" + attributes + '\'');
		for (int i = 0, n = attributes.length(); i < n; i++) {
			c = attributes.charAt(i);
			if (c == '\\') {
				// Take the next character whatever it is
				if (mode == 1 || mode == 2) {
					i++;
					isStuffed = true;
				}
			} else {
				switch (mode) {
				case 0:
					if (c <= ' ') {
						// ignore white space
					} else if (c == '"' || c == '\'') {
						quoteDelimiter = c;
						mode = 2;
						start = i + 1;
					} else {
						mode = 1;
						start = i;
					}
					break;
				case 1: // Name
					if (c <= ' ') {
						String value = isStuffed ? destuff(attributes, start, i)
								: attributes.substring(start, i);
						isStuffed = false;
						if (inValue) {
							if (DEBUG)
								System.out.println("Attribute '" + name + "'='"
										+ value + '\'');
							addAttr(name, value, type);
							mode = 0;
							inValue = false;
						} else {
							mode = 3;
							name = value;
						}
					} else if (c == '=') {
						String value = isStuffed ? destuff(attributes, start, i)
								: attributes.substring(start, i);
						isStuffed = false;
						if (inValue) {
							// Assume that the current value really is the name
							// of
							// next attribute and that the previous attribute
							// had no
							// \TODO value. Is this correct behaviour???

							if (DEBUG) {
								System.out.println("Attribute '" + name
										+ "'=null");
							}
							addAttr(name, null, type);
						} else {
							inValue = true;
						}
						name = value;
						mode = 0;
					}
					break;
				case 2:
					if (c == quoteDelimiter) {
						String value = isStuffed ? destuff(attributes, start, i)
								: attributes.substring(start, i);
						isStuffed = false;
						if (inValue) {
							if (DEBUG)
								System.out.println("Attribute '" + name + "'='"
										+ value + '\'');
							addAttr(name, value, type);
							mode = 0;
							inValue = false;
						} else {
							mode = 3;
							name = value;
						}
					}
					break;
				case 3:
					if (c <= ' ') {
						// ignore white space
					} else if (c == '=') {
						inValue = true;
						mode = 0;
					} else {
						// Something that is not a value
						if (DEBUG) {
							System.out.println("Attribute '" + name + "'=null");
						}
						addAttr(name, null, type);
						mode = 0;
						// Retry this character
						i--;
					}
				}
			}
		}
		if (mode > 0) {
			String value = isStuffed ? destuff(attributes, start, attributes
					.length()) : attributes.substring(start);
			if (inValue) {
				// Name and value

				if (DEBUG) {
					System.out.println("Attribute '" + name + "'='" + value
							+ '\'');
				}
				addAttr(name, value, type);
			} else {
				// in name
				if (DEBUG) {
					System.out.println("Attribute '" + value + "'=null");
				}
				addAttr(value, null, type);
			}
		} else if (inValue) {
			// Waiting for value i.e. empty attribute
			if (DEBUG) {
				System.out.println("Attribute '" + name + "'=null");
			}
			addAttr(name, null, type);
		}
	}

	private String destuff(String text, int start, int end) {
		// Must remove all stuffing
		char[] buf = new char[end - start];
		int bufLen = 0;
		for (int i = start; i < end; i++) {
			char c = text.charAt(i);
			if (c != '\\') {
				buf[bufLen++] = c;
			}
		}
		return new String(buf, 0, bufLen);
	}

	private int wss(String text, int start, int len) {
		while (start < len && text.charAt(start) <= 32) {
			start++;
		}
		return start;
	}

	public HtmlWriter text(char c) {
		checkMode();
		return a(c);
	}

	public HtmlWriter text(int value) {
		return text(Integer.toString(value));
	}

	public HtmlWriter text(long value) {
		return text(Long.toString(value));
	}

	public HtmlWriter text(String text) {
		checkMode();
		return a(text);
	}

	/*****************************************************************************
	 * Caching and writing
	 ****************************************************************************/

	private void expandBuffer(int minSize) {
		int newSize = buffer.length * 2;
		if (minSize > newSize) {
			newSize = minSize + 5;
		}
		buffer = ArrayUtils.setSize(buffer, newSize);
	}

	private HtmlWriter a(char c) {
		int len = size + 1;
		if (len > buffer.length) {
			expandBuffer(len);
		}
		buffer[size++] = c;
		return this;
	}

	private HtmlWriter a(int value) {
		return a(Integer.toString(value));
	}

	private HtmlWriter a(String text) {
		if (text == null) {
			text = String.valueOf(text);
		}
		int len = text.length();
		int newSize = size + len;
		if (newSize > buffer.length) {
			expandBuffer(newSize);
		}
		text.getChars(0, len, buffer, size);
		size = newSize;
		return this;
	}

	public HtmlWriter newLine() {
		if ((modeCounter > 0) && (modes[modeCounter - 1] & PENDING) != 0) {
			// We are in an open tag (set) and the new line should be added
			// after the tag (set) has been closed.
			modes[modeCounter - 1] |= NEWLINE;
			return this;
		} else {
			return addNewLine();
		}
	}

	private HtmlWriter addNewLine() {
		a(EOL);
		if (out != null) {
			try {
				out.write(buffer, 0, size);
				size = 0;
				if (autoflush) {
					out.flush();
				}
			} catch (IOException e) {
				inError = true;
			}
		}
		return this;
	}

	/*****************************************************************************
	 * Utilities
	 ****************************************************************************/

	private void checkMode() {
		while ((modeCounter > 0) && ((modes[modeCounter - 1] & PENDING) != 0)) {
			handleMode(modes[--modeCounter]);
		}

		// Special handling inside tables (\TODO should this be done???)
		int mode = peekMode(TABLE);
		if (mode >= 0 && ((mode & (TABLE_TD | TABLE_TH)) == 0)) {
			// Inside table but not inside td or th => start a new td
			td();
			while ((modeCounter > 0)
					&& ((modes[modeCounter - 1] & PENDING) != 0)) {
				handleMode(modes[--modeCounter]);
			}
		}
	}

	private void pushMode(int mode) {
		if (modeCounter == modes.length) {
			modes = ArrayUtils.setSize(modes, modeCounter + 10);
		}
		modes[modeCounter++] = mode;
	}

	private int peekMode() {
		return (modeCounter > 0) ? modes[modeCounter - 1] : -1;
	}

	private int peekMode(int type) {
		int mode;
		int len = modeCounter;
		while (len > 0) {
			mode = modes[--len];
			if ((mode & 0xff) == type) {
				return mode;
			}
		}
		return -1;
	}

	private int popMode() {
		while ((modeCounter > 0) && ((modes[modeCounter - 1] & PENDING) != 0)) {
			handleMode(modes[--modeCounter]);
		}
		return (modeCounter > 0) ? modes[--modeCounter] : -1;
	}

	private int popMode(int type) {
		while ((modeCounter > 0) && ((modes[modeCounter - 1] & PENDING) != 0)) {
			handleMode(modes[--modeCounter]);
		}

		int mode;
		while (modeCounter > 0) {
			mode = modes[--modeCounter];
			if ((mode & 0xff) == type) {
				return mode;
			} else {
				handleMode(mode);
			}
		}
		System.err.println("HtmlWriter: could not find tag " + type);
		return -1;
	}

	private int handleMode(int mode) {
		int type = mode & 0xff;

		if ((mode & PENDING) != 0) {
			boolean newline;
			if ((mode & NEWLINE) != 0) {
				newline = true;
				mode -= NEWLINE;
			} else {
				newline = false;
			}

			if (outerAttrSize > 0) {
				flushOuterAttributes();
			} else if (outerAttributes != null) {
				a(' ').a(outerAttributes);
				outerAttributes = null;
			}
			// Tag sets requires special handling
			if (type == TABLE) {
				mode = tableHeaderEnd(mode);
				// Must update the type in case it has changed
				// (if one of the tagset handlers sets it to 0 it will be
				// absorbed)
				type = mode & 0xff;
			} else if (type == COLGROUP) {
				mode = colgroupHeaderEnd(mode);
				// Must update the type in case it has changed
				// (if one of the tagset handlers sets it to 0 it will be
				// absorbed)
				type = mode & 0xff;
			}

			// Copy any cached attribute information
			if (innerAttrSize > 0) {
				flushInnerAttributes();
			} else if (innerAttributes != null) {
				a(' ').a(innerAttributes);
				innerAttributes = null;
			}
			a('>');
			if (newline) {
				addNewLine();
			}
			if (type > 0) {
				pushMode(mode & ~PENDING);
			}
			return mode;
		}

		switch (type) {
		case BODY:
			pageEnd(mode);
			break;
		case TABLE:
			tableEnd(mode);
			break;
		case FORM:
			formEnd(mode);
			break;
		default:
			System.err.println("HtmlWriter: unhandled type=" + type + " mode="
					+ mode);
			break;
		}
		if ((mode & NEWLINE) != 0) {
			addNewLine();
		}
		return type;
	}

	/*****************************************************************************
	 * Test
	 ****************************************************************************/

	// public static void main(String[] args)
	// {
	// HtmlWriter o = new HtmlWriter();
	// String width = "60%";
	// o.pageStart("Test").attr("bgcolor", "#f0f0f0")
	// .h1("Test Html");
	// if (args.length > 0)
	// {
	// o.tag('b');
	// o.parseAttributes(args[0], 2);
	// o.tagEnd('b');
	// }
	// o.table().attr("border", 1).attr("width", width)
	// .attr("class", "test").attr("test='hej'")
	// .colgroup(1, "t=h")
	// .colgroup(1, "t=b")
	// .td().text("Hello").td("Column2")
	// .tr().text("testing").td().attr("align","center").text("hello")
	// .tableEnd()
	// .tag('p')
	// .newLine()
	// .table(HtmlWriter.LINED).attr("width", width).attr("class", "test")
	// .attr("test='hej'")
	// .colgroup(1, "t=h")
	// .colgroup(1, "t=b")
	// .td().text("Hello").td("Column2")
	// .tr().text("testing").td().attr("align","center").text("hello")
	// .tableEnd()
	// .tag('p')
	// .newLine()
	// .table(HtmlWriter.BORDERED).attr("width", width).attr("class", "test")
	// .attr("test='hej'")
	// .colgroup(1, "t=h")
	// .colgroup(1, "t=b")
	// .td().text("Hello").td("Column2", "class=column2")
	// .tr().text("testing").td().attr("align","center").text("hello")
	// .tr().text("column1").td().attr("bgcolor=red").text("hello column 2")
	// .tableEnd()
	// .tag('p')
	// .newLine()
	// .tag('a').attr("href","http://www.sics.se/'andersl")
	// .attr("class", "\"pekka\"")
	// .tagEnd('a');
	// o.close();
	// System.out.println(o);
	// }
} // HtmlWriter
