/**
 * @(#)TableStyle Created date: Wed Apr 02 22:59:33 2003
 * $Revision: 4074 $, $Date: 2008-04-11 11:10:43 -0500 (Fri, 11 Apr 2008) $
 *
 * Copyright (c) 2000, 2001, 2002 BotBox AB.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * BotBox AB. ("Confidential Information").  You shall not disclose
 * such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * BotBox AB.
 */
package com.botbox.html;

/**
 * 
 * @author Joakim Eriksson (joakim.eriksson@botbox.com)
 * @author Niclas Finne (niclas.finne@botbox.com)
 * @author Sverker Janson (sverker.janson@botbox.com)
 * @version $Revision: 4074 $, $Date: 2008-02-24 12:03:02 -0500 (Sun, 24 Feb
 *          2008) $
 */
public class TableStyle {

	private static TableStyle normalTable;
	private static TableStyle lineTable;
	private static TableStyle borderTable;

	static TableStyle getNormalTable() {
		if (normalTable == null) {
			normalTable = new TableStyle();
		}
		return normalTable;
	}

	static TableStyle getLineTable() {
		if (lineTable == null) {
			lineTable = new TableStyle(// outer attributes
					"cellspacing=0 cellpadding=0 " + "border=0 bgcolor=black",
					// Inner attributes
					"cellspacing=1 cellpadding=2 " + "border=0",
					// tr attributes
					null,
					// th attributes
					"bgcolor='#e0e0ff'",
					// td attributes
					"bgcolor='#f0f0f0'");
		}
		return lineTable;
	}

	static TableStyle getBorderTable() {
		if (borderTable == null) {
			borderTable = new TableStyle(// outer attributes
					"cellspacing=0 cellpadding=1 " + "border=0 bgcolor=black",
					// Inner attributes
					"cellspacing=0 border=0",
					// tr attributes
					null,
					// th attributes
					"bgcolor='#e0e0ff'",
					// td attributes
					"bgcolor='#f0f0f0'");
		}
		return borderTable;
	}

	private String outerAttributes;
	// private String[] outerAttributeCache;
	private String attributes;
	// private String[] attributeCache;
	private String trAttributes;
	// private String[] trAttributeCache;
	private String thAttributes;
	// private String[] thAttributeCache;
	private String tdAttributes;
	// private String[] tdAttributeCache;

	private boolean isDoubleTable;

	public TableStyle() {
	}

	public TableStyle(String outerAttributes, String attributes,
			String trAttributes, String thAttributes, String tdAttributes) {
		this.outerAttributes = outerAttributes;
		this.attributes = attributes;
		this.trAttributes = trAttributes;
		this.thAttributes = thAttributes;
		this.tdAttributes = tdAttributes;
		isDoubleTable = true;
	}

	public boolean isDoubleTable() {
		return isDoubleTable;
	}

	public String getOuterAttributes() {
		return outerAttributes;
	}

	// public String[] getOuterAttributeCache()
	// {
	// return outerAttributeCache;
	// }

	// public void setOuterAttributeCache(String[] values, int count)
	// {
	// this.outerAttributeCache = new String[count * 2];
	// for (int i = 0, n = count * 2; i < n; i++)
	// {
	// this.outerAttributeCache[i] = values[i];
	// }
	// }

	public String getAttributes() {
		return attributes;
	}

	// public String[] getAttributeCache()
	// {
	// return attributeCache;
	// }

	// public void setAttributeCache(String[] values, int count)
	// {
	// this.attributeCache = new String[count * 2];
	// for (int i = 0, n = count * 2; i < n; i++)
	// {
	// this.attributeCache[i] = values[i];
	// }
	// }

	public String getTrAttributes() {
		return trAttributes;
	}

	// public String[] getTrAttributeCache()
	// {
	// return trAttributeCache;
	// }

	// public void setTrAttributeCache(String[] values, int count)
	// {
	// this.trAttributeCache = new String[count * 2];
	// for (int i = 0, n = count * 2; i < n; i++)
	// {
	// this.trAttributeCache[i] = values[i];
	// }
	// }

	public String getThAttributes() {
		return thAttributes;
	}

	// public String[] getThAttributeCache()
	// {
	// return thAttributeCache;
	// }

	// public void setThAttributeCache(String[] values, int count)
	// {
	// this.thAttributeCache = new String[count * 2];
	// for (int i = 0, n = count * 2; i < n; i++)
	// {
	// this.thAttributeCache[i] = values[i];
	// }
	// }

	public String getTdAttributes() {
		return tdAttributes;
	}

	// public String[] getTdAttributeCache()
	// {
	// return tdAttributeCache;
	// }

	// public void setTdAttributeCache(String[] values, int count)
	// {
	// this.tdAttributeCache = new String[count * 2];
	// for (int i = 0, n = count * 2; i < n; i++)
	// {
	// this.tdAttributeCache[i] = values[i];
	// }
	// }

} // TableStyle
