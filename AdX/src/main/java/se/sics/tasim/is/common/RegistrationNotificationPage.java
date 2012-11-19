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
 * RegistrationNotificationPage
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Jun 03 00:43:07 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
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

/**
 */
public class RegistrationNotificationPage extends HttpPage {

	private static final Logger log = Logger
			.getLogger(RegistrationNotificationPage.class.getName());

	private final InfoServer infoServer;

	// -------------------------------------------------------------------
	// User notification handling
	// -------------------------------------------------------------------

	public RegistrationNotificationPage(InfoServer infoServer) {
		this.infoServer = infoServer;
	}

	public void handle(String pathInContext, String pathParams,
			HttpRequest request, HttpResponse response) throws HttpException,
			IOException {
		String name = request.getParameter("id");
		String message = null;
		if (name != null) {
			try {
				int id = Integer.parseInt(name);
				infoServer.updateUser(id);
				log.info("updated user " + id + " using notification");
				message = "<html><body>User " + id
						+ " has been updated</body></html>";
			} catch (Exception e) {
				log
						.log(Level.WARNING, "illegal user update '" + name
								+ '\'', e);
			}
		}

		if (message == null) {
			message = "<html><body>Failed to update user " + name
					+ "</body></html>";
		}

		ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
		writer.write(message);
		response.setContentType(HttpFields.__TextHtml);
		response.setContentLength(writer.size());
		writer.writeTo(response.getOutputStream());
		response.commit();
	}

} // RegistrationNotificationPage
