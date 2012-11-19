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
 * HistoryPage
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : 15 April, 2002
 * Updated : $Date: 2008-04-04 20:42:56 -0500 (Fri, 04 Apr 2008) $
 *	     $Revision: 3981 $
 */

package se.sics.tasim.is.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.botbox.html.HtmlWriter;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.util.ByteArrayISO8859Writer;

public class HistoryPage extends HttpPage {

	private static final Logger log = Logger.getLogger(HistoryPage.class
			.getName());

	private final String pathInfo;
	private final SimServer simServer;
	private final String gamePath;
	private int simulationsPerPage;
	private String simTablePrefix;

	public HistoryPage(String pathInfo, SimServer simServer, String gamePath,
			String simTablePrefix, int simulationsPerPage) {
		this.pathInfo = pathInfo;
		this.simServer = simServer;
		if (gamePath.length() > 0 && !gamePath.endsWith(File.separator)) {
			gamePath += File.separator;
		}
		this.gamePath = gamePath;
		this.simTablePrefix = simTablePrefix;
		this.simulationsPerPage = simulationsPerPage;
	}

	public void handle(String pathInContext, String pathParams,
			HttpRequest request, HttpResponse response) throws HttpException,
			IOException {
		if (!pathInfo.equals(pathInContext)) {
			return;
		}

		String gameStr = request.getParameter("id");
		if ("last".equals(gameStr)) {
			// Show last game
			showLastGame(response);
			return;
		}

		int lastGameID = simServer.getLastPlayedSimulationID();
		StringBuffer game = new StringBuffer();
		game
				.append(
						"<html><body bgcolor=white link='#204020' vlink='#204020'>"
								+ "<font face='Arial,Helvetica,sans-serif' size='+2'><b>"
								+ "Game History for ").append(
						simServer.getServerName()).append("</b></font><p>\r\n");

		if (lastGameID == -1) {
			game.append("<font face='Arial,Helvetica,sans-serif' size='+1'>"
					+ "No games played</font><p>\r\n");
		} else {
			int gameID = lastGameID;
			if (gameStr != null) {
				try {
					gameID = Integer.parseInt(gameStr);
				} catch (Exception e) {
				}
			}

			gameID--;

			int id = 1 + (gameID - (gameID % simulationsPerPage));
			StringBuffer sb = new StringBuffer();
			sb.append("<font face='Arial,Helvetica,sans-serif' size=2>");
			link(sb, "First", 1, id > 1);
			link(sb, "Previous", id - simulationsPerPage, id > 1);
			link(sb, "Next", id + simulationsPerPage,
					(id + simulationsPerPage) <= lastGameID);
			link(sb, "Last", lastGameID,
					(id + simulationsPerPage) <= lastGameID);
			sb.append("</font>");
			String links = sb.toString();

			game.append("<form><table border=0 width='100%'><tr><td>").append(
					links).append(
					"</td><td align=right>"
							+ "<font face='Arial,Helvetica,sans-serif' size=2>"
							+ "Go to page with game "
							+ "<input type=text name=id size=5 border=0>"
							+ "<input type=submit value=Go></font></td></tr>"
							+ "</table></form>\r\n"
							+ "<p>\r\n<table border=1 width='100%'>"
							+ "<colgroup span=1 align=right></colgroup>"
							+ "<colgroup span=2></colgroup>"
							+ "<tr><th>Game</th><th>Start Time (Duration)</th>"
							+ "<th>Participants</th></tr>");
			readPage(game, ((id - 1) / simulationsPerPage) + 1);
			game.append("</table>\r\n<p>\r\n").append(links);
		}
		game.append("</body></html>\r\n");

		ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
		writer.write(game.toString());
		response.setContentType(HttpFields.__TextHtml);
		response.setContentLength(writer.size());
		writer.writeTo(response.getOutputStream());
		response.commit();
	}

	private void readPage(StringBuffer data, int id) {
		FileReader file = null;
		try {
			char[] readChars = new char[2048];
			int len;
			file = new FileReader(gamePath + simTablePrefix + id + ".html");
			while ((len = file.read(readChars, 0, 2048)) > 0) {
				data.append(readChars, 0, len);
			}
		} catch (FileNotFoundException e) {
			System.err.println("could not find "
					+ (gamePath + simTablePrefix + id + ".html"));
			data.append("<tr><td colspan='4' align='center'>" + "&nbsp;"
					+ "</td></tr>");
		} catch (IOException e) {
			log.log(Level.WARNING, "could not read game page " + id, e);
			data.append("<tr><td colspan='4' align='center'>"
					+ "[<font color=red>could not read game data</font>]"
					+ "</td></tr>");
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e2) {
				}
			}
		}
	}

	private void showLastGame(HttpResponse response) throws HttpException,
			IOException {
		int lastGameID = simServer.getLastPlayedSimulationID();
		StringBuffer page = new StringBuffer();
		int delay = simServer.getSecondsToNextSimulationEnd();
		page.append("<html><head><title>").append("Last game played at ")
				.append(simServer.getServerName()).append(
						"</title>\r\n"
								+ "<META http-equiv=\"refresh\" content=\"")
				.append(delay).append(pathInfo).append(
						"?id=last\">\r\n</head>\r\n");
		if (lastGameID < 1) {
			page.append("<body>Waiting for first game" + "</body></html>\r\n");

		} else {
			readResultPage(page, lastGameID);
		}

		ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer();
		writer.write(page.toString());
		response.setContentType(HttpFields.__TextHtml);
		response.setContentLength(writer.size());
		writer.writeTo(response.getOutputStream());
		response.commit();
	}

	private void readResultPage(StringBuffer data, int id) {
		FileReader file = null;
		try {
			char[] readChars = new char[2048];
			int len;
			file = new FileReader(gamePath + id + "/index.html");
			while ((len = file.read(readChars, 0, 2048)) > 0) {
				data.append(readChars, 0, len);
			}
		} catch (FileNotFoundException e) {
			data.append("Could not find results for game ").append(id);
		} catch (IOException e) {
			log.log(Level.WARNING, "could not read result page for " + id, e);
			data.append("Could not read result page for " + id + ": " + e);
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e2) {
				}
			}
		}
	}

	private void link(StringBuffer sb, String title, int pos, boolean link) {
		if (link) {
			sb.append("<a href='?id=").append(pos).append("'>").append(title)
					.append("</a> &nbsp; ");
		} else {
			sb.append(title).append(" &nbsp; ");
		}
	}

} // HistoryPage
