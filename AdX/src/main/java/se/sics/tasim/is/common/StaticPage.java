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
 * StaticPage
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Jun 17 16:42:16 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.botbox.html.HtmlWriter;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.util.ByteArrayISO8859Writer;

/**
 */
public class StaticPage extends HttpPage {

	private static final Logger log = Logger.getLogger(StaticPage.class
			.getName());

	private String path;
	private byte[] pageData;
	private String contentType = HttpFields.__TextHtml;

	private StaticPage(String path) {
		if (path == null) {
			throw new NullPointerException();
		}
		this.path = path;
	}

	public StaticPage(String path, String page) {
		this(path);
		setPage(page);
	}

	public StaticPage(String path, HtmlWriter writer) {
		this(path);
		setPage(writer);
	}

	public void setPage(String page) {
		try {
			ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
			writer.write(page);
			this.pageData = writer.getByteArray();
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not set page data for " + path, e);
		}
	}

	public void setPage(HtmlWriter page) {
		try {
			ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
			page.close();
			page.write(writer);
			this.pageData = writer.getByteArray();
		} catch (Exception e) {
			log.log(Level.SEVERE, "could not set page data for " + path, e);
		}
	}

	public void handle(String pathInContext, String pathParams,
			HttpRequest request, HttpResponse response) throws HttpException,
			IOException {
		if (path.equals(pathInContext) && pageData != null) {
			response.setContentType(contentType);
			response.setContentLength(pageData.length);
			response.getOutputStream().write(pageData);
			response.commit();
		}
	}

	public String toString() {
		return "StaticPage[" + path + ','
				+ (pageData == null ? 0 : pageData.length) + ']';
	}

} // StaticPage
