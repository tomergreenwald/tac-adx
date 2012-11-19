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
 * PageHandler
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Jun 17 14:42:17 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.io.IOException;
import java.util.Map;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.PathMap;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.util.Code;

/**
 */
public class PageHandler extends AbstractHttpHandler {

	private PathMap pathMap = new PathMap();

	public PageHandler() {
	}

	public void addPage(String pathSpec, HttpPage page) {
		if (!pathSpec.startsWith("/") && !pathSpec.startsWith("*")) {
			Code.warning("pathSpec should start with '/' or '*' : " + pathSpec);
			pathSpec = "/" + pathSpec;
		}

		pathMap.put(pathSpec, page);
	}

	public Map.Entry getPageEntry(String pathInContext) {
		return pathMap.getMatch(pathInContext);
	}

	public void handle(String pathInContext, String pathParams,
			HttpRequest request, HttpResponse response) throws HttpException,
			IOException {
		if (!isStarted()) {
			return;
		}

		Map.Entry pageEntry = getPageEntry(pathInContext);
		HttpPage page = pageEntry == null ? null : (HttpPage) pageEntry
				.getValue();
		if (page != null) {
			page.handle(pathInContext, pathParams, request, response);
		}
	}

} // PageHandler
