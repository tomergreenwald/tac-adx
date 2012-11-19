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
 * RedirectPage
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Oct 30 13:00:01 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.io.IOException;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;

/**
 */
public class RedirectPage extends HttpPage {

	private String redirectPath;
	private boolean appendPath;

	public RedirectPage() {
	}

	public void setRedirectPath(String redirectPath, boolean appendPath) {
		if (redirectPath != null && !redirectPath.startsWith("/")) {
			redirectPath = '/' + redirectPath;
		}
		this.redirectPath = redirectPath;
		this.appendPath = appendPath;
	}

	public void handle(String pathInContext, String pathParams,
			HttpRequest request, HttpResponse response) throws HttpException,
			IOException {
		String redirectPath = this.redirectPath;
		if (redirectPath != null) {
			StringBuffer buf = request.getRootURL();
			buf.append(redirectPath);
			if (appendPath) {
				buf.append(request.getPath());
			}

			String location = buf.toString();
			response.setField(HttpFields.__Location, location);
			response.setStatus(302);
			request.setHandled(true);
		}
	}

} // RedirectPage
