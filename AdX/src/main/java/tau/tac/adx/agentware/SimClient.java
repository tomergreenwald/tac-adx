/*
 * SimClient.java
 *
 * COPYRIGHT  2008
 * THE REGENTS OF THE UNIVERSITY OF MICHIGAN
 * ALL RIGHTS RESERVED
 *
 * PERMISSION IS GRANTED TO USE, COPY, CREATE DERIVATIVE WORKS AND REDISTRIBUTE THIS
 * SOFTWARE AND SUCH DERIVATIVE WORKS FOR NONCOMMERCIAL EDUCATION AND RESEARCH
 * PURPOSES, SO LONG AS NO FEE IS CHARGED, AND SO LONG AS THE COPYRIGHT NOTICE
 * ABOVE, THIS GRANT OF PERMISSION, AND THE DISCLAIMER BELOW APPEAR IN ALL COPIES
 * MADE; AND SO LONG AS THE NAME OF THE UNIVERSITY OF MICHIGAN IS NOT USED IN ANY
 * ADVERTISING OR PUBLICITY PERTAINING TO THE USE OR DISTRIBUTION OF THIS SOFTWARE
 * WITHOUT SPECIFIC, WRITTEN PRIOR AUTHORIZATION.
 *
 * THIS SOFTWARE IS PROVIDED AS IS, WITHOUT REPRESENTATION FROM THE UNIVERSITY OF
 * MICHIGAN AS TO ITS FITNESS FOR ANY PURPOSE, AND WITHOUT WARRANTY BY THE
 * UNIVERSITY OF MICHIGAN OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT
 * LIMITATION THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE REGENTS OF THE UNIVERSITY OF MICHIGAN SHALL NOT BE LIABLE FOR ANY
 * DAMAGES, INCLUDING SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WITH
 * RESPECT TO ANY CLAIM ARISING OUT OF OR IN CONNECTION WITH THE USE OF THE SOFTWARE,
 * EVEN IF IT HAS BEEN OR IS HEREAFTER ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package tau.tac.adx.agentware;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.BinaryTransportWriter;
import se.sics.isl.transport.Context;
import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.LogFormatter;
import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.Message;
import se.sics.tasim.props.AdminContent;
import se.sics.tasim.props.Alert;
import se.sics.tasim.props.SimulationStatus;
import se.sics.tasim.props.StartInfo;
import tau.tac.adx.props.AdxInfoContextFactory;

import com.botbox.util.ArrayQueue;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class SimClient implements Runnable {

	private static final Logger log = Logger.getLogger(SimClient.class
			.getName());

	/**
	 * Version information for the server and agent scopes
	 */
	private final static String CLIENT_VERSION = "0.9.6";

	/**
	 * Disconnect if requested to wait for more than 50 seconds
	 */
	private static final int MIN_MILLIS_BEFORE_DISCONNECT = 50000;

	/**
	 * Configuration
	 */
	private final ConfigManager config;

	/**
	 * Server information
	 */
	private final String userName;
	private final String userPassword;
	private final String serverHost;
	private final int serverPort;
	private final Context currentContext;

	private long serverTimeDiff = 0;
	private int autoJoinCount;

	/**
	 * Server connection
	 */
	private ServerConnection connection;
	private boolean isQuitPending = false;
	private boolean isAutoJoinPending = false;

	/**
	 * Message handling
	 */
	private final ArrayQueue messageQueue = new ArrayQueue();

	/**
	 * Agent implementation handling
	 */
	private final String agentImpl;
	private Agent agent;
	private AgentServiceImpl agentService;

	/**
	 * Logging
	 */
	private final String logFilePrefix;
	private final String logSimPrefix;
	private final LogFormatter formatter;
	private FileHandler rootFileHandler;
	private FileHandler simLogHandler;
	private String simLogName;

	public SimClient(ConfigManager config, String serverHost, int serverPort,
			String name, String password, String agentImpl) throws IOException {
		this.config = config;
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.userName = name;
		this.userPassword = password;
		this.agentImpl = agentImpl;
		this.autoJoinCount = config.getPropertyAsInt("autojoin", 1);

		String logPrefix = config.getProperty("log.prefix", "aw");
		this.logFilePrefix = getLogDirectory("log.directory", logPrefix);
		this.logSimPrefix = getLogDirectory("log.sim.directory", logPrefix);

		// Set shorter names for the log
		formatter = new LogFormatter();
		formatter.setAliasLevel(2);
		LogFormatter.setFormatterForAllHandlers(formatter);

		setLogging();

		currentContext = new AdxInfoContextFactory().createContext();

		if (!createAgentInstance()) {
			showWarning("Agent Setup Failed", "could not setup the agent");
			System.exit(1);
		}

		this.connection = new ServerConnection(this, 0L);
		this.connection.open();
		// Start the message thread
		new Thread(this, "SimClient").start();
	}

	public String getUserName() {
		return userName;
	}

	public String getServerHost() {
		return serverHost;
	}

	public int getServerPort() {
		return serverPort;
	}

	public Context getContext() {
		return currentContext;
	}

	public long getServerTime() {
		return System.currentTimeMillis() - serverTimeDiff;
	}

	public long getTimeDiff() {
		return serverTimeDiff;
	}

	// -------------------------------------------------------------------
	// Message request handling
	// -------------------------------------------------------------------

	private void requestServerTime() {
		AdminContent time = new AdminContent(AdminContent.SERVER_TIME);
		Message msg = new Message(userName, Agent.ADMIN, time);
		deliverToServer(msg);
	}

	public void requestQuit() {
		isQuitPending = true;
		clearMessages();
		ServerConnection connection = this.connection;
		if (connection != null) {
			connection.close();
		} else {
			// Could not send quit to server so we simply quit anyway
			System.exit(1);
		}
	}

	public void autoJoinSimulation(boolean force) {
		if (autoJoinCount > 0 || force) {
			if (isAutoJoinPending) {
				isAutoJoinPending = false;
			} else {
				autoJoinCount--;
			}
			requestJoinSimulation();
		}
	}

	// public void requestNextSimulation() {
	// AdminContent content = new AdminContent(AdminContent.NEXT_SIMULATION);
	// Message msg = new Message(userName, Agent.ADMIN, content);
	// deliverToServer(msg);
	// }

	public void requestJoinSimulation() {
		AdminContent content = new AdminContent(AdminContent.JOIN_SIMULATION);
		Message msg = new Message(userName, Agent.ADMIN, content);
		deliverToServer(msg);
	}

	// -------------------------------------------------------------------
	// Agent Handling
	// -------------------------------------------------------------------

	private boolean createAgentInstance() {
		if (agent != null) {
			return true;
		}

		// Check which class to create and create it!!!
		try {
			log.finer("creating agent instance of " + agentImpl);
			agent = (Agent) Class.forName(agentImpl).newInstance();
			return true;
		} catch (ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			log.log(Level.SEVERE, "could not create an agent instance of "
					+ agentImpl, e);
			return false;
		}
	}

	// Sets up an agent to start playing a simulation!
	private boolean setupAgent(Message setupMessage) {
		shutdownAgent();

		if (!createAgentInstance()) {
			showWarning("Agent Setup Failed", "could not setup the agent");
			requestQuit();
			return false;
		}

		StartInfo info = (StartInfo) setupMessage.getContent();
		enterSimulationLog(info.getSimulationID());
		try {
			Agent agent = this.agent;
			this.agent = null;
			log.finer("creating agent service based on " + info);
			// Must use the name (address) given to the agent in this simulation
			this.agentService = new AgentServiceImpl(this, userName, agent,
					setupMessage);
			this.agentService.deliverToAgent(setupMessage);
			return true;
		} finally {
			if (this.agentService == null) {
				exitSimulationLog();
			}
		}
	}

	private void shutdownAgent() {
		AgentServiceImpl oldAgentService = this.agentService;
		if (oldAgentService != null) {
			// Let the old agent be garbaged
			this.agentService = null;
			try {
				log.finer("stopping agent service");
				oldAgentService.stopAgent();
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not stop old agent", e);
			} finally {
				exitSimulationLog();
			}
		}
	}

	// -------------------------------------------------------------------
	// API towards the AgentServiceImpl
	// -------------------------------------------------------------------

	protected void stopSimulation(AgentServiceImpl agentService) {
		if (this.agentService == agentService) {
			shutdownAgent();
			autoJoinSimulation(false);
		}
	}

	protected boolean deliverToServer(Message msg) {
		ServerConnection connection = this.connection;
		if (connection == null) {
			// Not connected to server
			return false;
		}

		if (connection.sendMessage(msg)) {
			return true;
		} else {
			return false;
		}
	}

	// -------------------------------------------------------------------
	// API towards ServerConnection
	// -------------------------------------------------------------------

	boolean connectionOpened(ServerConnection connection) {
		if (this.connection != connection) {
			connection.close();
			return false;
		}

		// Login to the server
		AdminContent auth = new AdminContent(AdminContent.AUTH);
		auth.setAttribute("name", userName);
		auth.setAttribute("password", userPassword);
		auth.setAttribute("client.version", CLIENT_VERSION);
		Message msg = new Message(userName, Agent.ADMIN, auth);

		return connection.sendMessage(msg);
	}

	void connectionClosed(ServerConnection connection) {
		if (this.connection == connection) {
			this.connection = null;

			clearMessages();
			if (isQuitPending) {
				System.exit(0);
			} else {
				isAutoJoinPending = true;
				this.connection = new ServerConnection(this, 30000L);
				this.connection.open();

				showWarning("Connection Lost", "Lost connection to "
						+ serverHost + " (will reconnect in 30 seconds)");
				shutdownAgent();
			}
		}
	}

	@SuppressWarnings("static-method")
	void showWarning(String title, String message) {
		log.severe("**************************************************"
				+ "**********");
		log.severe("* " + title);
		log.severe("* " + message);
		log.severe("**************************************************"
				+ "**********");
	}

	void messageFromServer(ServerConnection connection, Message message) {
		if (this.connection != connection) {
			connection.close();
		} else {
			addMessage(message, connection.getID());
		}
	}

	void adminFromServer(ServerConnection connection, AdminContent admin) {
		if (this.connection != connection) {
			connection.close();
		} else {
			if (log.isLoggable(Level.FINEST)) {
				log.finest("(" + connection.getID() + ") received " + admin);
			}

			handleAdminContent(admin);
		}
	}

	void alertFromServer(ServerConnection connection, Alert alert) {
		if (this.connection != connection) {
			connection.close();
		} else {
			if (log.isLoggable(Level.FINEST)) {
				log.finest("(" + connection.getID() + ") received " + alert);
			}

			handleAlert(alert);
		}
	}

	private synchronized void addMessage(Message message, int connectionID) {
		if (log.isLoggable(Level.FINEST)) {
			log.finest("(" + connectionID + ") received " + message);
		}
		messageQueue.add(message);
		notify();
	}

	private synchronized Message nextMessage() throws InterruptedException {
		while (messageQueue.size() == 0) {
			wait();
		}
		return (Message) messageQueue.remove(0);
	}

	private synchronized void clearMessages() {
		messageQueue.clear();
	}

	// -------------------------------------------------------------------
	// Message handling
	// -------------------------------------------------------------------

	@Override
	public void run() {
		do {
			Message msg = null;
			try {
				msg = nextMessage();

				Transportable content = msg.getContent();

				if (content instanceof AdminContent) {
					handleAdminContent((AdminContent) content);

				} else if (content instanceof StartInfo) {
					// Setup a new agent instance and give it the simulation
					// start message. The new agent instance must be given
					// the address it has been assigned in this simulation.
					setupAgent(msg);

				} else if (content instanceof Alert) {
					handleAlert((Alert) content);

				} else if (agentService != null) {
					// If an agentService already is set up - send all messages
					// to it!!
					agentService.deliverToAgent(msg);

					if (content instanceof SimulationStatus) {
						SimulationStatus status = (SimulationStatus) content;
						if (status.isSimulationEnded()) {
							stopSimulation(agentService);
						}
					}

				} else {
					log.severe("No agent registered to receive " + msg);
				}
			} catch (ThreadDeath e) {
				log.log(Level.SEVERE, "message thread died", e);
				throw e;

			} catch (Throwable e) {
				log.log(Level.SEVERE, "could not handle message " + msg, e);
			}
		} while (true);
	}

	// -------------------------------------------------------------------
	// Message handling
	// -------------------------------------------------------------------

	private void handleAdminContent(AdminContent admin) {
		ServerConnection connection = this.connection;
		int type = admin.getType();

		if (admin.isError()) {
			// Failed to do whatever we tried to do
			if (type == AdminContent.AUTH) {
				// Failed to login
				showWarning("Authentication Failed", "could not login as "
						+ userName + ": " + admin.getErrorReason());
				requestQuit();

			} else {
				showWarning("Request Failed",
						"Failed to " + AdminContent.getTypeAsString(type) + ": "
								+ AdminContent.getErrorAsString(admin.getError())
								+ " (" + admin.getErrorReason() + ')');
				// What should be done here? FIX THIS!!!
			}
		} else if (connection == null) {
			// Connection has closed => ignore message

		} else {
			switch (type) {
			case AdminContent.AUTH: {
				String serverVersion = admin.getAttribute("server.version");
				connection.setAuthenticated(true);
				if (ConfigManager.compareVersion(serverVersion, "0.8.13") >= 0) {
					connection
							.setTransportSupported(BinaryTransportWriter.SUPPORT_TABLES);
					// connection
					// .setTransportSupported(BinaryTransportWriter.SUPPORT_CONSTANTS);
				}
				if (ConfigManager.compareVersion(serverVersion, "0.9") < 0) {
					// This is an older version of the server that does not
					// immediately send out this information by itself
					requestServerTime();
					autoJoinSimulation(false);
				}
				break;
			}

			case AdminContent.SERVER_TIME: {
				long serverTime = admin.getAttributeAsLong("time", 0L);
				if (serverTime > 0) {
					serverTimeDiff = System.currentTimeMillis() - serverTime;
					formatter.setLogTime(serverTime);
				}
				break;
			}

			case AdminContent.NEXT_SIMULATION:
			case AdminContent.JOIN_SIMULATION: {
				long currentTime = getServerTime();
				long startTime = admin.getAttributeAsLong("startTime", 0L);
				long nextTime = 0L;
				if (startTime > 0) {
					int simulationID = admin
							.getAttributeAsInt("simulation", -1);
					String simText = simulationID >= 0 ? " " + simulationID
							: "";
					// Simulation has been joined or already existed (displayed
					// to user by StatusInfo). Create the agent immediately to
					// ensure it has enough time to setup.
					if (this.agentService == null && !createAgentInstance()) {
						showWarning("Agent Setup Failed",
								"could not setup the agent");
						requestQuit();
					}
					if (startTime > currentTime) {
						log.info("next simulation" + simText + " starts in "
								+ ((startTime - currentTime) / 1000)
								+ " seconds");
					} else {
						log.info("next simulation" + simText
								+ " has already started");
					}
					nextTime = startTime - 15000L;

				} else if (autoJoinCount > 0
						&& (type == AdminContent.NEXT_SIMULATION)) {
					autoJoinSimulation(false);

				} else {
					nextTime = admin.getAttributeAsLong("nextTime", 0L);
					if (nextTime < currentTime) {
						nextTime = currentTime + 60
								* (long) (20000 + Math.random() * 5000);
					}
				}

				if (nextTime > currentTime) {
					// Next time to check for next simulation
					long delay = nextTime - currentTime;
					if (delay > MIN_MILLIS_BEFORE_DISCONNECT) {
						// We should wait a while => disconnect for now
						long maxSleep = 60 * (long) (56000 + Math.random() * 1000);
						// Do not wait too long before checking in at the server
						// again in case games have been rescheduled.
						if (delay > maxSleep) {
							delay = maxSleep;
						}

						log.info("[will reconnect in " + (delay / 60000)
								+ " minutes, " + ((delay / 1000) % 60)
								+ " seconds]");

						isAutoJoinPending = true;
						this.connection = new ServerConnection(this, delay);
						this.connection.open();
						connection.close();
					}
				}
				break;
			}

			default:
				log.warning("unhandled admin content: " + admin);
				break;
			}
		}
	}

	private void handleAlert(final Alert alert) {
		showWarning("ALERT: " + alert.getTitle(), alert.getMessage());
	}

	// -------------------------------------------------------------------
	// Logging handling
	// -------------------------------------------------------------------

	private synchronized void setLogging() throws IOException {
		int consoleLevel = config.getPropertyAsInt("log.consoleLevel", 0);
		int fileLevel = config.getPropertyAsInt("log.fileLevel", 0);
		Level consoleLogLevel = LogFormatter.getLogLevel(consoleLevel);
		Level fileLogLevel = LogFormatter.getLogLevel(fileLevel);
		Level logLevel = consoleLogLevel.intValue() < fileLogLevel.intValue() ? consoleLogLevel
				: fileLogLevel;
		boolean showThreads = config.getPropertyAsBoolean("log.threads", false);
		String[] packages = config
				.getPropertyAsArray("log.packages", "se.sics");
		if (packages != null && packages.length > 0) {
			for (int i = 0, n = packages.length; i < n; i++) {
				Logger.getLogger(packages[i]).setLevel(logLevel);
			}
		} else {
			Logger awRoot = Logger.getLogger("se.sics");
			awRoot.setLevel(logLevel);
		}

		formatter.setShowingThreads(showThreads);
		LogFormatter.setConsoleLevel(consoleLogLevel);
		// LogFormatter.setLevelForAllHandlers(logLevel);

		Logger root = Logger.getLogger("");
		if (fileLogLevel != Level.OFF) {
			if (rootFileHandler == null) {
				rootFileHandler = new FileHandler(logFilePrefix + "%g.log",
						1000000, 10);
				rootFileHandler.setFormatter(formatter);
				root.addHandler(rootFileHandler);
			}
			rootFileHandler.setLevel(fileLogLevel);
			if (simLogHandler != null) {
				simLogHandler.setLevel(fileLogLevel);
			}
		} else if (rootFileHandler != null) {
			exitSimulationLog();
			root.removeHandler(rootFileHandler);
			rootFileHandler.close();
			rootFileHandler = null;
		}
	}

	private String getLogDirectory(String property, String name)
			throws IOException {
		String logDirectory = config.getProperty(property);
		if (logDirectory != null) {
			// Create directories for logs
			File fp = new File(logDirectory);
			if ((!fp.exists() && !fp.mkdirs()) || !fp.isDirectory()) {
				throw new IOException("could not create directory '"
						+ logDirectory + '\'');
			}
			return fp.getAbsolutePath() + File.separatorChar + name;
		} else {
			return name;
		}
	}

	private synchronized void enterSimulationLog(int simulationID) {
		exitSimulationLog();

		if (rootFileHandler != null) {
			LogFormatter.separator(log, Level.FINE,
					"Entering log for simulation " + simulationID);
			try {
				Logger root = Logger.getLogger("");
				String name = logSimPrefix + "_SIM_" + simulationID + ".log";
				simLogHandler = new FileHandler(name, true);
				simLogHandler.setFormatter(formatter);
				simLogHandler.setLevel(rootFileHandler.getLevel());
				simLogName = name;
				root.addHandler(simLogHandler);
				root.removeHandler(rootFileHandler);
				LogFormatter.separator(log, Level.FINE, "Log for simulation "
						+ simulationID + " at " + serverHost + ':' + serverPort
						+ " started");
			} catch (Exception e) {
				log.log(Level.SEVERE, "could not open log file for simulation "
						+ simulationID, e);
			}
		}
	}

	private synchronized void exitSimulationLog() {
		if (simLogHandler != null) {
			Logger root = Logger.getLogger("");
			LogFormatter.separator(log, Level.FINE, "Simulation log complete");

			root.addHandler(rootFileHandler);
			root.removeHandler(simLogHandler);
			simLogHandler.close();
			simLogHandler = null;
			// Try to remove the lock file since it is no longer needed
			if (simLogName != null) {
				new File(simLogName + ".lck").delete();
				simLogName = null;
			}
		}
	}

} // SimClient

