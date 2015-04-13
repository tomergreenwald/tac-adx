/**
 * TAC Supply Chain Management Simulator
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001-2005 SICS AB. All rights reserved.
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
 * InfoServer
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Jan 08 16:05:21 2003
 * Updated : $Date: 2008-06-12 07:31:52 -0500 (Thu, 12 Jun 2008) $
 *           $Revision: 4728 $
 */
package se.sics.tasim.is.common;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.botbox.html.HtmlWriter;
import com.botbox.util.ArrayUtils;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpServer;
import org.mortbay.http.NCSARequestLog;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.NotFoundHandler;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.http.handler.SecurityHandler;
import org.mortbay.util.InetAddrPort;
import se.sics.isl.db.DBField;
import se.sics.isl.db.DBMatcher;
import se.sics.isl.db.DBObject;
import se.sics.isl.db.DBResult;
import se.sics.isl.db.DBTable;
import se.sics.isl.db.Database;
import se.sics.isl.inet.InetServer;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;
import se.sics.tasim.is.AgentInfo;
import se.sics.tasim.is.AgentLookup;
import se.sics.tasim.is.InfoConnection;

public class InfoServer {

	private static final int ADMIN_USER_ID = 0;

	private static final int MAX_USER_NAME_LENGTH = 20;
	private static final int MAX_USER_PASSWORD_LENGTH = 20;
	private static final int MAX_USER_EMAIL_LENGTH = 80;

	public final static String CONF = "is.";

	public final static boolean ALLOW_SIM_TYPE = false;

	private static final Logger log = Logger.getLogger(InfoServer.class.getName());

	private final ConfigManager config;
	private final String defaultSimulationType;

	private final AgentLookup agentLookup = new AgentLookup();
	private final AgentRealm agentRealm;

	private Database userDatabase;
	private DBTable userTable;

	private Database database;

	private String basePath;

	/** Server information */
	private String infoServerName;
    private String version;
    private String serverType;

	/** The HTTP handling */
	private HttpServer httpServer;
	private HttpContext httpContext;
	private ResourceHandler httpResourceHandler;
	private SocketListener httpSocketListener;
	private PageHandler pageHandler;
	private String httpHost;
	private int httpPort;

	/** HTTP pages */
	private StaticPage menuPage;
	private StaticPage statusPage;
	private RedirectPage redirectPage;

	/** The viewer handling */
	private BlockingViewerServer viewerServer;

	/** Time zone difference */
	private int timeDiff = 0;

	/** Date formatting */
	private static SimpleDateFormat dFormat = null;
	private static Date date = null;

	/** The known simulation servers */
	private SimServer[] servers;
	private boolean storeResults = false;

	private Hashtable managerTable = new Hashtable();
	private SimulationArchiver simulationArchiver;

	/** Timer handling */
	private Timer timer = new Timer();

	// User notification handling
	private String registrationURL = null;

	public InfoServer(ConfigManager config) throws IllegalConfigurationException, IOException {
		this.config = config;

		this.infoServerName = config.getProperty(CONF + "server.name", config.getProperty("server.name"));
		if (this.infoServerName == null) {
			this.infoServerName = generateServerName();
		}

        this.serverType = config.getProperty(CONF + "server.type", config.getProperty("server.type", "TAC SIM Server"));
        this.version = config.getProperty(CONF + "server.version", config.getProperty("server.version", "0.0.1"));

		this.defaultSimulationType = config.getProperty(CONF + "simulation.defaultType", config.getProperty(
				"simulation.defaultType", "tac13adx"));

		setTimeZone(config.getPropertyAsInt("timeZone", 0));

		// User notification handling
		this.registrationURL = config.getProperty(CONF + "registration.url");

		// Add any information managers (will exit if no managers are
		// specified in the configuration or if creation failed). FIX THIS!!!
		// TODO
		String[] names = config.getPropertyAsArray(CONF + "manager.names");
		InfoManager[] managers = (InfoManager[]) config.createInstances(CONF
				+ "manager", InfoManager.class, names);
		if (managers == null || managers.length == 0) {
			throw new IllegalConfigurationException("no info managers in "
					+ "configuration");
		}
		for (int i = 0, n = names.length; i < n; i++) {
			managers[i].init(this, names[i]);
		}

		basePath = config.getProperty(CONF + "resultDirectory",
				"./public_html/");
		if (basePath.length() > 0) {
			if (!basePath.endsWith(File.separator)) {
				basePath += File.separator;
			}
			File baseFile = new File(basePath);
			if (!baseFile.exists() && !baseFile.mkdirs()) {
				throw new IllegalConfigurationException(
						"simulation result directory '" + basePath
								+ "' does not exist or is not " + "a directory");
			}
		}

		this.database = DatabaseUtils.createDatabase(config, CONF);
		this.userDatabase = DatabaseUtils.createUserDatabase(config, CONF,
				database);
		this.storeResults = config.getPropertyAsBoolean(CONF
				+ "database.results", false);

		userTable = userDatabase.getTable("users");
		if (userTable == null) {
			userTable = userDatabase.createTable("users");
			userTable.createField("id", DBField.INTEGER, 32, DBField.UNIQUE
					| DBField.PRIMARY | DBField.INDEX);
			userTable.createField("parent", DBField.INTEGER, 32, 0,
					new Integer(-1));
			userTable.createField("name", DBField.STRING,
					MAX_USER_PASSWORD_LENGTH, 0);
			userTable.createField("password", DBField.STRING,
					MAX_USER_PASSWORD_LENGTH, 0);
			userTable.createField("email", DBField.STRING,
					MAX_USER_EMAIL_LENGTH, 0);
			createUser("admin", config.getProperty("admin.password",
					"secret_password"), null);
			userTable.flush();
		} else {
			// START OF BACKWARD COMPABILITY: REMOVE THIS!!!
			if (!userTable.hasField("parent")) {
				userTable.createField("parent", DBField.INTEGER, 32, 0,
						new Integer(-1));
				userTable.flush();
			}
			// END OF BACKWARD COMPABILITY: REMOVE THIS!!!

			// Cache all users for performance reasons
			DBResult res = userTable.select();
			while (res.next()) {
				String name = res.getString("name");
				String password = res.getString("password");
				int userID = res.getInt("id");
				int parentID = res.getInt("parent");
				if (name != null) {
					agentLookup.setUser(name, password, userID, parentID);
				}
			}
			res.close();

			String adminPassword = config.getProperty("admin.password");
			if (adminPassword != null
					&& !agentLookup.validateAgent(ADMIN_USER_ID, adminPassword)) {
				// The password for the admin user has changed!
				agentLookup.setUser("admin", adminPassword, ADMIN_USER_ID);

				// Store the new password for the admin user
				DBMatcher matcher = new DBMatcher();
				DBObject object = new DBObject();
				matcher.setInt("id", ADMIN_USER_ID);
				object.setString("password", adminPassword);
				userTable.update(matcher, object);
				userTable.flush();
			}
		}

		// Start the viewer server
		startViewerServer();

		// Setup the web service
		this.httpHost = config.getProperty(CONF + "http.host", config
				.getProperty("server.host"));
		this.httpPort = config.getPropertyAsInt(CONF + "http.port", 8080);
		this.httpServer = new HttpServer();
		if (this.httpHost != null) {
			InetAddrPort addr = new InetAddrPort(this.httpHost, this.httpPort);
			this.httpSocketListener = new SocketListener(addr);
		} else {
			this.httpSocketListener = new SocketListener();
			this.httpSocketListener.setPort(httpPort);
		}
		this.httpSocketListener.setMaxThreads(30);
		this.httpServer.addListener(httpSocketListener);

		this.httpContext = httpServer.getContext("/");
		this.httpContext.setResourceBase(basePath);

		String accesslog = config.getProperty(CONF + "http.accesslog");
		if (accesslog != null) {
			NCSARequestLog rLog = new NCSARequestLog(accesslog);
			// Keep all logs
			rLog.setRetainDays(0);
			rLog.setAppend(true);
			rLog.setBuffered(false);
			this.httpServer.setRequestLog(rLog);
		}

		this.httpResourceHandler = new ResourceHandler();
		this.httpResourceHandler.setDirAllowed(false);
		this.httpResourceHandler.setAllowedMethods(new String[] {
				HttpRequest.__GET, HttpRequest.__HEAD });
		this.httpResourceHandler.setAcceptRanges(true);

		String adminName = agentLookup.getAgentName(ADMIN_USER_ID);
		String adminPassword = agentLookup.getAgentPassword(ADMIN_USER_ID);
		agentRealm = new AgentRealm(this, serverType);
		if (adminName != null && adminPassword != null) {
			agentRealm.setAdminUser(adminName, adminPassword);
		}
		this.httpContext.addHandler(new SecurityHandler());
		this.httpContext.setRealm(agentRealm);

		this.pageHandler = new PageHandler();
		this.httpContext.addHandler(this.pageHandler);
		this.httpContext.addHandler(this.httpResourceHandler);
		this.httpContext.addHandler(new NotFoundHandler());

		// context.add(new SecurityHandler());

		// Index page
		String page = "<html><head><title>"+serverType + " " + infoServerName
				+ "</title></head>\r\n"
				+ "<FRAMESET BORDER=0 ROWS='105,*'>\r\n"
				+ "<FRAME SRC='/top/'>"
				+ "<FRAMESET BORDER=0 COLS='155,*'>\r\n"
				+ "<FRAME SRC='/menu/'>\r\n"
				+ "<FRAME SRC='/status/' NAME='content'>\r\n"
				+ "</FRAMESET></FRAMESET>\r\n" + "</html>\r\n";
		pageHandler.addPage("/", new StaticPage("/", page));

		// Top page
		page = "<html><body style='margin-bottom: -25'>\r\n"
				+ "<table border=0 width='100%'><tr><td>"
				+ "<img src='http://www.sics.se/tac/images/logo.gif'>"
				+ "</td><td valign=top align=right><font face=arial>"
				+ "<b>Trading Agent Competition</b></font>" + "<br>"
				+ "<font face=arial size='-1' color='#900000'>"
				+ serverType + " " + version
				+ "</font></td></tr></table><hr>" + "</body></html>\r\n";
		pageHandler.addPage("/top/", new StaticPage("/top/", page));

		page = getMenuData();
		this.menuPage = new StaticPage("/menu/", page);
		pageHandler.addPage("/menu/", this.menuPage);

		page = getStatusData();
		this.statusPage = new StaticPage("/status/", page);
		pageHandler.addPage("/status/", this.statusPage);

		// Shortcuts to the first SCM server for this info server. FIX THIS!!!
		// TODO
		this.redirectPage = new RedirectPage();
		pageHandler.addPage("/admin/*", this.redirectPage);
		pageHandler.addPage("/games/*", this.redirectPage);
		pageHandler.addPage("/history/*", this.redirectPage);

		// User notification handling
		if (registrationURL == null
				|| !config.getPropertyAsBoolean(CONF + "registration.disabled",
						false)) {
			String notification = config.getProperty(CONF
					+ "registration.notification");
			String password = config
					.getProperty(CONF + "registration.password");
			boolean isRemoteRegistrationEnabled = config.getPropertyAsBoolean(
					CONF + "registration.remote", false);
			pageHandler.addPage("/register/", new RegistrationPage(this,
					notification, password, isRemoteRegistrationEnabled));
		}
		// Always have the user notify page running to enable user
		// information update during runtime
		pageHandler.addPage("/notify/", new RegistrationNotificationPage(this));

		// Register the menu and status
		try {
			this.httpServer.start();
		} catch (Exception e) {
			throw (IOException) new IOException("could not start HTTP server")
					.initCause(e);
		}
	}

	private String generateServerName() {
		return InetServer.getLocalHostName();
	}

	public ConfigManager getConfig() {
		return config;
	}

	public String getDefaultSimulationType() {
		return defaultSimulationType;
	}

	public PageHandler getPageHandler() {
		return pageHandler;
	}

	public HttpContext getHttpContext() {
		return httpContext;
	}

	public int getHttpPort() {
		return httpPort;
	}

	// -------------------------------------------------------------------
	// Server time handling
	// -------------------------------------------------------------------

	public long getServerTimeSeconds() {
		return (System.currentTimeMillis() + timeDiff) / 1000;
	}

	public long getServerTimeMillis() {
		return System.currentTimeMillis() + timeDiff;
	}

	public static synchronized String getServerTimeAsString(long serverTime) {
		if (dFormat == null) {
			dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dFormat.setTimeZone(new java.util.SimpleTimeZone(0, "UTC"));
			date = new Date(0L);
		}
		date.setTime(serverTime);
		return dFormat.format(date);
	}

	public int getTimeZone() {
		return timeDiff / 3600000;
	}

	public void setTimeZone(int hoursFromUTC) {
		this.timeDiff = hoursFromUTC * 3600000;
	}

	void schedule(TimerTask task, long delay, long period) {
		timer.schedule(task, delay, period);
	}

	void schedule(TimerTask task, long delay) {
		timer.schedule(task, delay);
	}

	// -------------------------------------------------------------------
	// SimServer handling
	// -------------------------------------------------------------------

	public SimServer getSimServer(String serverName) {
		SimServer[] servers = this.servers;
		int index = SimServer.indexOf(servers, serverName);
		return index >= 0 ? servers[index] : null;
	}

	public synchronized void addInfoConnection(InfoConnectionImpl connection) {
		// Should check server name, password and SimConnection. FIX THIS!!!
		// TODO
		String serverName = connection.getServerName();
		int index = SimServer.indexOf(servers, serverName);
		if (index >= 0) {
			// Server already exists
			servers[index].setInfoConnection(connection);
		} else {
			String path = basePath + serverName + File.separatorChar;
			Database serverDatabase = DatabaseUtils.createChildDatabase(config,
					CONF, serverName, database);
			SimServer server = new SimServer(this, serverDatabase, connection,
					path, storeResults);
			boolean firstServer = servers == null;
			servers = (SimServer[]) ArrayUtils.add(SimServer.class, servers,
					server);
			menuPage.setPage(getMenuData());
			statusPage.setPage(getStatusData());
			if (firstServer) {
				// Redirect all shortcuts to the first registered server. FIX
				// THIS!!!
				// TODO
				redirectPage.setRedirectPath("" + '/' + serverName, true);
			}
		}
	}

	// -------------------------------------------------------------------
	// API towards SimServer
	// -------------------------------------------------------------------

	public SimulationArchiver getSimulationArchiver() {
		if (simulationArchiver == null) {
			synchronized (this) {
				if (simulationArchiver == null) {
					simulationArchiver = new SimulationArchiver();
				}
			}
		}
		return simulationArchiver;
	}

	public InfoManager getInfoManager(String type) {
		return (InfoManager) managerTable.get(type);
	}

	// Called whenever the server message changes
	public void serverMessageChanged(SimServer simServer) {
		statusPage.setPage(getStatusData());
	}

	// -------------------------------------------------------------------
	// API towards the InfoManagers
	// -------------------------------------------------------------------

	public synchronized void addInfoManager(String type, InfoManager manager) {
		managerTable.put(type, manager);
	}

	// -------------------------------------------------------------------
	// API towards the Viewer Server
	// -------------------------------------------------------------------

	private void startViewerServer() throws IOException {
		viewerServer = new BlockingViewerServer(this);
		viewerServer.start();
	}

	void serverClosed(BlockingViewerServer viewerServer) {
		if (this.viewerServer == viewerServer) {
			log.severe("VIEWER SERVER CLOSED!!!");
			// Try to restart the viewer server
			try {
				startViewerServer();
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not restart viewer", e);
			}
		}
	}

	// -------------------------------------------------------------------
	// User notification handling
	// -------------------------------------------------------------------

	public synchronized boolean updateUser(int id) {
		DBMatcher matcher = new DBMatcher();
		matcher.setInt("id", id);
		matcher.setLimit(1);
		return updateUser(matcher);
	}

	public synchronized boolean updateUser(String userName) {
		DBMatcher matcher = new DBMatcher();
		matcher.setString("name", userName);
		matcher.setLimit(1);
		return updateUser(matcher);
	}

	private synchronized boolean updateUser(DBMatcher matcher) {
		DBResult res = userTable.select(matcher);
		if (res.next()) {
			String name = res.getString("name");
			String password = res.getString("password");
			int userID = res.getInt("id");
			int parentID = res.getInt("parent");
			if (name != null) {
				agentLookup.setUser(name, password, userID, parentID);

				// Notify all simulation servers
				SimServer[] servers = this.servers;
				if (servers != null) {
					for (int i = 0, n = servers.length; i < n; i++) {
						servers[i].setUser(name, password, userID);
					}
				}
				agentRealm.updateUser(name);
				res.close();
				return true;
			}
		}
		res.close();
		return false;
	}

	// -------------------------------------------------------------------
	// User handling
	// -------------------------------------------------------------------

	public void validateUserInfo(String name, String password, String email) {
		if (name == null || (name = name.trim()).length() < 2) {
			throw new IllegalArgumentException(
					"name must be at least 2 characters");
		}
		if (name.length() > MAX_USER_NAME_LENGTH) {
			throw new IllegalArgumentException("too long name (max "
					+ MAX_USER_NAME_LENGTH + " characters)");
		}
		// Check for illegal characters
		for (int i = 0, n = name.length(); i < n; i++) {
			char c = name.charAt(i);
			if (c <= 32) {
				throw new IllegalArgumentException(
						"name may only contain characters " + "or digits");
			}
		}
		if (name.equalsIgnoreCase("dummy") || name.equalsIgnoreCase("dummy-")) {
			throw new IllegalArgumentException("user '" + name
					+ "' already exists");
		}
		if (password == null || (password = password.trim()).length() < 4) {
			throw new IllegalArgumentException("password must be at least 4 "
					+ "characters");
		}
		if (password.length() > MAX_USER_PASSWORD_LENGTH) {
			throw new IllegalArgumentException("too long password (max "
					+ MAX_USER_PASSWORD_LENGTH + " characters)");
		}
		if (email != null && email.length() > MAX_USER_EMAIL_LENGTH) {
			throw new IllegalArgumentException("too long email (max "
					+ MAX_USER_EMAIL_LENGTH + " characters)");
		}

		// Check if the user 'name' already exists
		if (agentLookup.getAgentID(name) >= 0) {
			throw new IllegalArgumentException("user '" + name
					+ "' already exists");
		}

		// Check that 'name' can not be used to construct the name
		// of another user (by adding a digit).
		AgentInfo[] agents = agentLookup.getAgentInfos();
		if (agents != null) {
			int length = name.length();
			for (int i = 0, n = agents.length; i < n; i++) {
				AgentInfo agent = agents[i];
				String u = agent.getName();
				char c;
				if (u.length() == (length + 1) && u.startsWith(name)
						&& ((c = u.charAt(length)) >= '0') && (c <= '9')) {
					throw new IllegalArgumentException("user '" + name
							+ "' already exists");
				}
			}
		}
	}

	public synchronized int createUser(String name, String password,
			String email) {
		// Validate that all information is correct and no conflicting agent
		// exists
		validateUserInfo(name, password, email);

		DBObject object = new DBObject();
		int id = userTable.getObjectCount();
		int userID = id * 11;
		object.setInt("id", userID);
		object.setString("name", name);
		object.setString("password", password);

		// MLB 20080404 - SQLite doesn't like null fields, so we set
		// email to "" if we don't have one.
		object.setString("email", (email == null) ? ("") : (email));

		userTable.insert(object);
		userTable.flush();

		agentLookup.setUser(name, password, userID);

		// Notify all simulation servers
		SimServer[] servers = this.servers;
		if (servers != null) {
			for (int i = 0, n = servers.length; i < n; i++) {
				servers[i].setUser(name, password, userID);
			}
		}
		return userID;
	}

	public synchronized int claimUser(String name, String password, String email) {
		int userID = agentLookup.getAgentID(name);
		if (userID < 0 && updateUser(name)) {
			userID = agentLookup.getAgentID(name);
		}

		if ((userID < 0) || ((userID % 11) != 0)) {
			// Agent account does not exist. Create it.
			// The reason might be that the agent name conflicts with
			// another name but the create method will handle this
			return createUser(name, password, email);
		}

		String pwd = agentLookup.getAgentPassword(userID);
		if (pwd == null) {
			// No password although agent account exists.
			throw new IllegalStateException(
					"could not find password for agent " + name);
		}

		boolean updatePassword = !pwd.equals(password);
		if (updatePassword || email != null) {
			// The agent exists. Update agent information.
			DBMatcher matcher = new DBMatcher();
			DBObject object = new DBObject();
			matcher.setInt("id", userID);
			if (updatePassword) {
				object.setString("password", password);
			}
			if (email != null) {
				object.setString("email", email);
			}
			userTable.update(matcher, object);
			userTable.flush();

			if (updatePassword) {
				agentLookup.setUser(name, password, userID);

				// Notify all simulation servers
				SimServer[] servers = this.servers;
				if (servers != null) {
					for (int i = 0, n = servers.length; i < n; i++) {
						servers[i].setUser(name, password, userID);
					}
				}
				agentRealm.updateUser(name);
			}
		}
		return userID;
	}

	public String getUserName(int userID) {
		return agentLookup.getAgentName(userID);
	}

	// public AgentInfo getAgentInfo(int userID) {
	// return agentLookup.getAgentInfo(userID);
	// }

	public AgentInfo[] getAgentInfos() {
		return agentLookup.getAgentInfos();
	}

	// Only allows the base agent login (not the sub agents)
	public int getUserID(String name) {
		int userID = agentLookup.getAgentID(name);
		if (userID < 0 && updateUser(name)) {
			userID = agentLookup.getAgentID(name);
		}
		return userID;
	}

	public String getUserPassword(String name) {
		int userID = agentLookup.getAgentID(name);
		if (userID < 0 && updateUser(name)) {
			userID = agentLookup.getAgentID(name);
		}

		return (userID >= 0) && ((userID % 11) == 0) ? agentLookup
				.getAgentPassword(userID) : null;
	}

	public boolean isAdministrator(int userID) {
		return userID == ADMIN_USER_ID;
	}

	// public void requestUser(InfoConnection connection, String userName) {
	// DBMatcher dbm = new DBMatcher();
	// dbm.setString("name", userName);
	// dbm.setLimit(1);
	// DBResult res = userTable.select(dbm);
	// if (!res.next()) {
	// int len = userName.length();
	// res.close();
	// res = null;
	// // Could not find the name but if the name ends with a digit it
	// // might be a sub-agent and we need to check if the agent
	// // without the ending digit exists and use it in that case.
	// if (len > 1 && Character.isDigit(userName.charAt(len - 1))) {
	// dbm.setString("name", userName.substring(0, len - 1));
	// res = userTable.select(dbm);
	// if (!res.next()) {
	// res.close();
	// // No such user found
	// res = null;
	// }
	// }
	// }
	// if (res != null) {
	// String password = res.getString("password");
	// int userID = res.getInt("id");
	// res.close();
	// } else {

	// }
	// }

	// -------------------------------------------------------------------
	// Dynamic web page handling
	// -------------------------------------------------------------------

	private String getStatusData() {
		HtmlWriter page = new HtmlWriter();
		SimServer[] servers = this.servers;
		page.pageStart("Server Status");
		if (servers == null) {
			page.text("<em>No servers are running at this time.</em>");
		} else {
			for (int i = 0, n = servers.length; i < n; i++) {
				String message = servers[i].getServerMessage();
				page.h2("Server " + servers[i].getServerName() + " is "
						+ (servers[i].isConnected() ? "running." : "offline."));
				if (message != null) {
					page.text(message).p();
				}
			}
		}
		page.close();
		return page.toString();
	}

	private String getMenuData() {
		HtmlWriter page = new HtmlWriter();
		page.pageStart("Menu").attr("style", "margin-right: -25").table().attr(
				"border", 0).attr("width", "100%");
		title(page, "Menu", false);
		link(page, "/status/", "Status");

		// User notification handling
		link(page, registrationURL != null ? registrationURL : "/register/",
				"Register new user");
		// link(page, "/user/", "User Settings");

		SimServer[] servers = this.servers;
		if (servers != null) {
			for (int i = 0, n = servers.length; i < n; i++) {
				String name = servers[i].getServerName();
				title(page, "Server " + name, true);
				link(page, '/' + name + "/games/",
						"Coming games (watch, create)");
				link(page, '/' + name + "/history/", "Game History");
				// link(page, '/' + name + "/scores/", "Score Table"); FIX
				// THIS!!! TODO
			}
		}
		page.close();
		return page.toString();
	}

	private void title(HtmlWriter page, String title, boolean whitespace) {
		if (whitespace) {
			page.tr().td("&nbsp;");
		}
		page.tr().td().attr("bgcolor='#202080'").tag("font",
				"face='Arial,Helvetica,sans-serif' color=white").tag('b').text(
				title).tagEnd('b').tagEnd("font");
	}

	private void link(HtmlWriter page, String url, String text) {
		page.tr().td().tag('a').attr("href", url).attr("target=content").tag(
				"font", "face='Arial,Helvetica,sans-serif'").text(text).tagEnd(
				"font").tagEnd('a');
	}


    public String getVersion() {
        return version;
    }

    void setVersion(String version) {
        this.version = version;
    }

    public String getServerType() {
        return serverType;
    }

    void setServerType(String serverType) {
        this.serverType = serverType;
    }
} // InfoServer
