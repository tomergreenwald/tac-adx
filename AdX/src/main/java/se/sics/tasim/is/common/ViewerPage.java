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
 * ViewerPage
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Mar 12 12:49:29 2003
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3981 $
 */
package se.sics.tasim.is.common;

import java.io.IOException;
import java.util.logging.Logger;

import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.util.ByteArrayISO8859Writer;

public class ViewerPage extends HttpPage {

	private static final Logger log = Logger.getLogger(ViewerPage.class
			.getName());

	private String viewerClass = "se.sics.tasim.viewer.applet.ViewerApplet";
	private String viewerCodeBase = "/code/";
	private String viewerArchive = "simviewer.jar"; // TODO-could be changed
													// @asleep
	private String width = "100%";
	private String height = "100%";

	private int infoPort = 4042;
	private String contextFactory = "";

	private String topPage;
	private String middlePage;
	private String bottomPage;

	public ViewerPage(InfoServer infoServer, SimServer simServer) {
		String serverName = simServer.getServerName();
		viewerArchive = infoServer.getConfig().getProperty("is.viewer.jars",
				viewerArchive);
		contextFactory = infoServer.getConfig().getProperty(
				"is.viewer.contextFactory", contextFactory);

        infoPort = infoServer.getConfig().getPropertyAsInt("is.viewer.port", 4042);

		// Generate the page
		topPage = "<html>\r\n<head>\r\n<title>Simulation viewer for "
				+ serverName
				+ "</title>\r\n</head>\r\n<body bgcolor=black>\r\n"
				+ "<object\r\n"
				+ "   classid='clsid:8AD9C840-044E-11D1-B3E9-00805F499D93'\r\n"
				+ "   codebase='http://java.sun.com/products/plugin/autodl/jinstall-1_4_1-windows-i586.cab#Version=1,4,1,0'\r\n"
				+ "   width='"
				+ width
				+ "' height='"
				+ height
				+ "'>\r\n"
				+ "  <param name=code value='"
				+ viewerClass
				+ "'>\r\n"
				+ "  <param name=codebase value='"
				+ viewerCodeBase
				+ "'>\r\n"
				+ "  <param name=archive value='"
				+ viewerArchive
				+ "'>\r\n"
				+ "  <param name=type value='application/x-java-applet;version=1.4.1'>\r\n"
				+ "  <param name='scriptable' value='false'>\r\n"
				+ "  <param name='serverName' value='" + serverName + "'>\r\n"
				+ "<param name=contextFactory value='" + contextFactory
				+ "'>\r\n";
		middlePage = "\r\n" + "  <COMMENT>\r\n" + "    <embed\r\n"
				+ "       type='application/x-java-applet;version=1.4.1'\r\n"
				+ "       code='" + viewerClass + "'\r\n" + "       codebase='"
				+ viewerCodeBase + "'\r\n" + "       archive='" + viewerArchive
				+ "'\r\n" + "       width='" + width + "'\r\n"
				+ "       height='" + height + "'\r\n"
				+ "       scriptable=false\r\n" + "       serverName='"
				+ serverName + "'\r\n" + "       contextFactory='"
				+ contextFactory + "'\r\n";
		bottomPage = "	pluginspage='http://java.sun.com/products/plugin/index.html#download'\r\n"
				+ "       alt='Your browser understands the &lt;EMBED&gt; tag but isn't running the Java Applet, for some reason.'>\r\n"
				+ "	<noembed>\r\n"
				+ "      Your browser is completely ignoring the Java Applet!\r\n"
				+ "       </noembed>\r\n"
				+ "    </embed>\r\n"
				+ "  </COMMENT>\r\n"
				+ "</object>\r\n"
				+ "<br>\r\n"
				+ "<font size=-1 color=white><em>Note that you can see more information by clicking on the entities during a game.</em></font>"
				+ "</body>\r\n" + "</html>\r\n";
	}

	public void handle(String pathInContext, String pathParams,
			HttpRequest request, HttpResponse response) throws HttpException,
			IOException {
		String userName = request.getAuthUser();
		String page = topPage + "<param name=user value='" + userName
				+ "'>\r\n" + "<param name=port value='" + infoPort + "'>\r\n"
				+ "<param name=contextFactory value='" + contextFactory
				+ "'>\r\n" + middlePage + "user='" + userName + "'\r\n"
				+ "port='" + infoPort + "'\r\n" + "contextFactory='"
				+ contextFactory + "'\r\n" + bottomPage;

		ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
		writer.write(page);
		response.setContentType(HttpFields.__TextHtml);
		response.setContentLength(writer.size());
		writer.writeTo(response.getOutputStream());
		response.commit();
	}

} // ViewerPage
