/**
 * SICS TAC Server - InfoServer
 * http://www.sics.se/tac/	  tac-dev@sics.se
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
 * ScorePage
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : 15 April, 2002
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *	     $Revision: 3981 $
 * Purpose :
 *
 */

package se.sics.tasim.is.common;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.util.ByteArrayISO8859Writer;
import org.mortbay.util.URI;

public class ScorePage extends HttpPage {

	private static final Logger log = Logger.getLogger(ScorePage.class
			.getName());

	private final SimServer simServer;
	private final String gamePath;

	public ScorePage(SimServer simServer, String gamePath) {
		this.simServer = simServer;
		if (gamePath != null && !gamePath.endsWith("/")) {
			gamePath += '/';
		}
		this.gamePath = gamePath;
	}

	public void handle(String pathInContext, String pathParams,
			HttpRequest request, HttpResponse response) throws HttpException,
			IOException {
		Competition competition = simServer.getCurrentCompetition();
		String scorePath = (competition != null) ? ("competition/"
				+ competition.getID() + "/") : "default/";

		String location;
		if (gamePath != null) {
			location = gamePath + scorePath;
		} else {
			// This is only possible when used stand alone
			StringBuffer buf = request.getRequestURL();
			location = URI.addPaths(buf.toString(), "../history/" + scorePath);
		}
		response.setField(HttpFields.__Location, location);
		response.setStatus(302);
		request.setHandled(true);
	}

} // ScorePage
