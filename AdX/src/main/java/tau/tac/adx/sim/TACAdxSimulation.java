/*
 * TACAASimulation.java
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
package tau.tac.adx.sim;

import static tau.tac.adx.sim.TACAdxConstants.ADX_AGENT_ROLE_ID;
import static tau.tac.adx.sim.TACAdxConstants.AD_NETOWRK_ROLE_ID;
import static tau.tac.adx.sim.TACAdxConstants.DEMAND_AGENT_ROLE_ID;
import static tau.tac.adx.sim.TACAdxConstants.DU_NETWORK_AVG_RESPONSE;
import static tau.tac.adx.sim.TACAdxConstants.DU_NETWORK_LAST_RESPONSE;
import static tau.tac.adx.sim.TACAdxConstants.ROLE_NAME;
import static tau.tac.adx.sim.TACAdxConstants.TYPE_NONE;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.isl.util.IllegalConfigurationException;
import se.sics.tasim.aw.Message;
import se.sics.tasim.is.EventWriter;
import se.sics.tasim.is.SimulationInfo;
import se.sics.tasim.is.common.Competition;
import se.sics.tasim.props.ServerConfig;
import se.sics.tasim.props.SimulationStatus;
import se.sics.tasim.props.StartInfo;
import se.sics.tasim.sim.LogWriter;
import se.sics.tasim.sim.Simulation;
import se.sics.tasim.sim.SimulationAgent;
import tau.tac.adx.AdxManager;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.agents.DefaultAdxUsers;
import tau.tac.adx.agents.DemandAgent;
import tau.tac.adx.auction.manager.AdxBidManager;
import tau.tac.adx.auction.tracker.AdxBidTracker;
import tau.tac.adx.auction.tracker.AdxBidTrackerImpl;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.props.ReservePriceInfo;
import tau.tac.adx.report.adn.AdNetworkKey;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.AdNetworkReportSender;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.CampaignReport;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReport;
import tau.tac.adx.report.publisher.AdxPublisherReport;
import tau.tac.adx.report.publisher.AdxPublisherReportSender;
import tau.tac.adx.sim.config.AdxConfigurationParser;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.util.AdxModule;

import com.botbox.util.ArrayUtils;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.BankStatus;
import edu.umich.eecs.tac.sim.Bank;
import edu.umich.eecs.tac.sim.BankStatusSender;

/**
 * @author Lee Callender, Patrick Jordan, Ben Cassell
 * @author greenwald, Mariano Schain
 */
public class TACAdxSimulation extends Simulation implements AdxAgentRepository,
		BankStatusSender, AdxPublisherReportSender, AdNetworkReportSender {
	private Bank bank;

	private final String timeUnitName = "Day";
	private int currentTimeUnit = 0;
	private int secondsPerDay = 10;
	private int numberOfDays = 60;

	private int numberOfAdvertisers = TACAdxManager.NUMBER_OF_ADVERTISERS;

	private int pingInterval = 0;
	private int nextPingRequest = 0;
	private int nextPingReport = 0;

	// private RetailCatalog retailCatalog;
	/**
	 * Simulation's {@link PublisherCatalog}.
	 */
	private PublisherCatalog publisherCatalog;
	// private UserClickModel userClickModel;
	// private String[] manufacturers;
	// private String[] components;
	private final String[] advertiserAddresses = new String[numberOfAdvertisers];
	private final String[] adxAdvertiserAddresses = new String[numberOfAdvertisers];
	private Map<String, AdvertiserInfo> advertiserInfoMap;
	private List<AdxUser> userPopulation;
	private Map<Device, Integer> deviceDistributionMap;
	private Map<AdType, Integer> adTypeDistributionMap;

	private final Random random;

	public String[] getAdxAdvertiserAddresses() {
		return adxAdvertiserAddresses;
	}

	private Competition competition;
	private final Injector injector = Guice.createInjector(new AdxModule());

	private final Runnable afterTickTarget = new Runnable() {
		@Override
		public void run() {
			handleAfterTick();
		}
	};

	/**
	 * The system's main event bus.
	 */
	private EventBus eventBus;
	

	private boolean recoverAgents = false;
	private AdxAuctioneer auctioneer;

	private static final Logger log = Logger.getLogger(TACAdxSimulation.class
			.getName());
	private DefaultAdxUsers adxAgent;
	private DemandAgent demandAgent;

	public TACAdxSimulation(ConfigManager config, Competition competition) {
		super(config);
		this.setCompetition(competition);
		random = new Random();
		AdxManager.getInstance().setSimulation(this);
	}

	public TACAdxSimulation(ConfigManager config) {
		super(config);

		random = new Random();
		AdxManager.getInstance().setSimulation(this);
	}

	/**
	 * @return the eventBus
	 */
	@Override
	public EventBus getEventBus() {
		return eventBus;
	}

	@Override
	public Map<String, AdvertiserInfo> getAdvertiserInfo() {
		return getAdvertiserInfoMap();
	}

	@Override
	protected void setupSimulation() throws IllegalConfigurationException {
		eventBus = new EventBus(
				TACAdxConstants.ADX_EVENT_BUS_NAME);
		auctioneer = injector.getInstance(AdxAuctioneer.class);
		ConfigManager config = getConfig();
		SimulationInfo info = getSimulationInfo();
		AdxConfigurationParser adxConfigurationParser = new AdxConfigurationParser(
				config);

		int seconds = info.getParameter("secondsPerDay", 0);
		this.secondsPerDay = seconds <= 1 ? config.getPropertyAsInt(
				"game.secondsPerDay", secondsPerDay) : seconds; // SimulationInfo
		// gets priority
		// over
		// ConfigManager
		if (this.secondsPerDay < 1)
			this.secondsPerDay = 1;

		this.numberOfDays = // Make sure this is correct.
		info.getSimulationLength() / (this.secondsPerDay * 1000);

		int pingIntervalSeconds = config.getPropertyAsInt("ping.interval", 0);
		if (pingIntervalSeconds > 0) {
			this.pingInterval = pingIntervalSeconds / this.secondsPerDay;
			if (this.pingInterval <= 1) {
				this.pingInterval = 1;
			}
			this.nextPingRequest = this.pingInterval;
			this.nextPingReport = this.pingInterval + 1;
		} else {
			this.pingInterval = 0;
		}

		this.numberOfAdvertisers = config
				.getPropertyAsInt("game.numberOfAdvertisers",
						TACAdxManager.NUMBER_OF_ADVERTISERS);

		log.info("TACAA Simulation " + info.getSimulationID()
				+ " is setting up...");

		// Initialize in-game agents, bank etc.
		setBank(new Bank(this, this.getSimulationInfo(), numberOfAdvertisers));

		// createBuiltinAgents("users", USERS, Users.class);
		// createBuiltinAgents("publisher", PUBLISHER, Publisher.class);
		createBuiltinAgents(TACAdxConstants.ADX_AGENT_NAME, ADX_AGENT_ROLE_ID,
				DefaultAdxUsers.class);
		log.info("Created Adx Agent");

		createBuiltinAgents(TACAdxConstants.DEMAND_AGENT_NAME,
				DEMAND_AGENT_ROLE_ID, DemandAgent.class);
		log.info("Created Demand Agent");

		this.publisherCatalog = adxConfigurationParser.createPublisherCatalog();
		this.userPopulation = adxConfigurationParser.createUserPopulation();
		this.deviceDistributionMap = adxConfigurationParser
				.createDeviceDistributionMap();
		this.adTypeDistributionMap = adxConfigurationParser
				.createAdTypeDistributionMap();

		validateConfiguration();

		// Create proxy agents for all participants
		for (int i = 0, n = info.getParticipantCount(); i < n; i++) {
			// Must associate a user id with the agent to connect it with an
			// agent identity (might be external). Only ADVERTISER participants
			// are allowed to join the simulation for now which means the
			// participant role does not need to be checked
			createExternalAgent("adv" + (i + 1), AD_NETOWRK_ROLE_ID,
					info.getParticipantID(i));
		}
		if (info.getParticipantCount() < numberOfAdvertisers) {
			createDummies("dummy.adnetwork", AD_NETOWRK_ROLE_ID,
					numberOfAdvertisers - info.getParticipantCount());
		}

		adxConfigurationParser.initializeAdvertisers(this);
		// Simulation setup needs to be called after advertisers have been
		// initialized

		AdxManager.getInstance().setup();

		SimulationAgent adxSimulationAgent = getAgents(ADX_AGENT_ROLE_ID)[0];
		adxAgent = (DefaultAdxUsers) adxSimulationAgent.getAgent();
		adxAgent.simulationSetup(this, adxSimulationAgent.getIndex());
		addTimeListener(adxAgent);

		SimulationAgent demandSimulationAgent = getAgents(DEMAND_AGENT_ROLE_ID)[0];
		demandAgent = (DemandAgent) demandSimulationAgent.getAgent();
		demandAgent.simulationSetup(this, demandSimulationAgent.getIndex());
		addTimeListener(demandAgent);

	}

	@Override
	protected String getTimeUnitName() {
		return timeUnitName;
	}

	@Override
	protected int getTimeUnitCount() {
		return numberOfDays;
	}

	@Override
	protected void startSimulation() {
		LogWriter logWriter = getLogWriter();

		// Save the server configuration to the log.
		ConfigManager config = getConfig();
		ServerConfig serverConfig = new ServerConfig(config);
		logWriter.write(serverConfig);

		SimulationInfo simInfo = getSimulationInfo();
		StartInfo startInfo = createStartInfo(simInfo);
		startInfo.lock();

		logWriter.dataUpdated(TYPE_NONE, startInfo);

		sendToRole(AD_NETOWRK_ROLE_ID, startInfo);

		// If a new agent arrives now it will be recovered
		recoverAgents = true;

		// Send the publisher catalog to the ad networks

		sendToRole(AD_NETOWRK_ROLE_ID, this.publisherCatalog);
		sendToRole(AD_NETOWRK_ROLE_ID, new ReservePriceInfo(this.publisherCatalog.reservePriceType));

		for (SimulationAgent publisher : getPublishers()) {
			Publisher publisherAgent = (Publisher) publisher.getAgent();
			publisherAgent.sendPublisherInfoToAll();
		}

		for (Map.Entry<String, AdvertiserInfo> entry : getAdvertiserInfoMap()
				.entrySet()) {
			sendMessage(entry.getKey(), entry.getValue());
			getEventWriter().dataUpdated(agentIndex(entry.getKey()),
					TACAdxConstants.DU_ADVERTISER_INFO, entry.getValue());
		}

		startTickTimer(simInfo.getStartTime(), secondsPerDay * 1000);

		logWriter.commit();

	}

	private StartInfo createStartInfo(SimulationInfo info) {
		return new StartInfo(info.getSimulationID(), info.getStartTime(),
				info.getSimulationLength(), secondsPerDay);
	}

	/**
	 * Notification when this simulation is preparing to stop. Called after the
	 * agents have been stopped but still can receive messages.
	 */
	@Override
	protected void prepareStopSimulation() {
		// No longer any need to recover agents
		recoverAgents = false;

		// The bank needs to send its final account statuses
		getBank().sendBankStatusToAll();

		for (SimulationAgent agent : getAgents(ADX_AGENT_ROLE_ID)) {
			if (agent.getAgent() instanceof AdxUsers) {
				AdxUsers adxUsers = (AdxUsers) agent.getAgent();
				adxUsers.sendReportsToAll();
			}
		}

		// Send the final simulation status
		int millisConsumed = (int) (getServerTime() - getSimulationInfo()
				.getEndTime());
		SimulationStatus status = new SimulationStatus(numberOfDays,
				millisConsumed, true);
		sendToRole(AD_NETOWRK_ROLE_ID, status);

	}

	/**
	 * Notification when this simulation has been stopped. Called after the
	 * agents shutdown.
	 */
	@Override
	protected void completeStopSimulation() {
		LogWriter writer = getLogWriter();
		writer.commit();
	}

	@Override
	protected void preNextTimeUnit(int timeUnit) {
		auctioneer.applyBidUpdates();
		adxAgent.preNextTimeUnit(timeUnit);
		demandAgent.preNextTimeUnit(timeUnit);
		if (timeUnit < numberOfDays) {
			// Let the bank send their first messages
			getBank().sendBankStatusToAll();
		}
	}

	/**
	 * Called when entering a new time unit similar to time listeners but this
	 * method is guaranteed to be called before the time listeners.
	 * 
	 * @param timeUnit
	 *            the current time unit
	 */
	@Override
	protected void nextTimeUnitStarted(int timeUnit) {
		this.currentTimeUnit = timeUnit;

		LogWriter writer = getLogWriter();
		writer.nextTimeUnit(timeUnit, getServerTime());

		if (timeUnit >= numberOfDays) {
			// Time to stop the simulation
			requestStopSimulation();
		}
	}

	/**
	 * Called when a new time unit has begun similar to time listeners but this
	 * method is guaranteed to be called after the time listeners.
	 * 
	 * @param timeUnit
	 *            the current time unit
	 */
	@Override
	protected void nextTimeUnitFinished(int timeUnit) {
		if (timeUnit < numberOfDays) {
			int millisConsumed = (int) (getServerTime()
					- getSimulationInfo().getStartTime() - timeUnit
					* secondsPerDay * 1000);

			SimulationStatus status = new SimulationStatus(timeUnit,
					millisConsumed);
			sendToRole(AD_NETOWRK_ROLE_ID, status); // Advertisers notified of
													// new day
		}
		invokeLater(afterTickTarget); // ?
	}

	/**
	 * Called each day after all morning messages has been sent.
	 */
	private void handleAfterTick() {
		if (pingInterval > 0 && currentTimeUnit < numberOfDays) {
			if (currentTimeUnit >= nextPingRequest) {
				nextPingRequest += pingInterval;

				SimulationAgent[] advertisers = getAgents(AD_NETOWRK_ROLE_ID);
				if (advertisers != null) {
					for (int i = 0, n = advertisers.length; i < n; i++) {
						advertisers[i].requestPing();
					}
				}
			}

			if (currentTimeUnit >= nextPingReport) {
				nextPingReport += pingInterval;

				SimulationAgent[] advertisers = getAgents(AD_NETOWRK_ROLE_ID);
				if (advertisers != null) {
					EventWriter writer = getEventWriter();
					synchronized (writer) {
						for (int i = 0, n = advertisers.length; i < n; i++) {
							SimulationAgent sa = advertisers[i];
							if (sa.getPingCount() > 0) {
								int index = sa.getIndex();
								writer.dataUpdated(index,
										DU_NETWORK_AVG_RESPONSE,
										sa.getAverageResponseTime());
								writer.dataUpdated(index,
										DU_NETWORK_LAST_RESPONSE,
										sa.getLastResponseTime());
							}
						}
					}
				}
			}
		}

		// Since all day start handling now is finished for this day and
		// the manufacturer agents will have some time to respond when
		// requesting pings, it is a good time to do some memory
		// management.
		System.gc();
		System.gc();
	}

	private void validateConfiguration() throws IllegalConfigurationException {
		// do validation
	}

	/**
	 * Called whenever an external agent has logged in and needs to recover its
	 * state. The simulation should respond with the current recover mode (none,
	 * immediately, or after next time unit). This method should return
	 * <code>RECOVERY_NONE</code> if the simulation not yet have been started.
	 * <p/>
	 * <p/>
	 * The simulation might recover the agent using this method if recovering
	 * the agent can be done using the agent communication thread. In that case
	 * <code>RECOVERY_NONE</code> should be returned. If any other recover mode
	 * is returned, the simulation will later be asked to recover the agent
	 * using the simulation thread by a call to <code>recoverAgent</code>.
	 * <p/>
	 * A common case might be when an agent reestablishing a lost connection to
	 * the server.
	 * 
	 * @param agent
	 *            the <code>SimulationAgent</code> to be recovered.
	 * @return the recovery mode for the agent
	 * @see #RECOVERY_NONE
	 * @see #RECOVERY_IMMEDIATELY
	 * @see #RECOVERY_AFTER_NEXT_TICK
	 * @see #recoverAgent(se.sics.tasim.sim.SimulationAgent)
	 */
	@Override
	protected int getAgentRecoverMode(SimulationAgent agent) {
		if (!recoverAgents) {
			return RECOVERY_NONE;
		}
		if (agent.hasAgentBeenActive()) {
			// The agent has been active and we must use the simulation
			// thread to retrieve all active orders (the information may
			// only be accessed using the simulation thread)
			return RECOVERY_AFTER_NEXT_TICK;
		}
		// The agent has not been active i.e. not sent any messages in
		// this simulation. This means the agent can not have any active
		// orders and only the startup messages needs to be sent. This can
		// be done using any thread.
		recoverAgent(agent);
		return RECOVERY_NONE;
	}

	/**
	 * Called whenever an external agent has logged in and needs to recover its
	 * state. The simulation should respond with the setup messages together
	 * with any other state information the agent needs to continue playing in
	 * the simulation (orders, inventory, etc). This method should not do
	 * anything if the simulation not yet have been started.
	 * <p/>
	 * <p/>
	 * A common case might be when an agent reestablishing a lost connection to
	 * the server.
	 * 
	 * @param agent
	 *            the <code>SimulationAgent</code> to be recovered.
	 */
	@Override
	protected void recoverAgent(SimulationAgent agent) {

		if (recoverAgents) {

			log.warning("recovering agent " + agent.getName());

			String agentAddress = agent.getAddress();

			StartInfo info = createStartInfo(getSimulationInfo());
			info.lock();

			sendMessage(new Message(agentAddress, info));

			sendMessage(new Message(agentAddress, this.publisherCatalog));

			sendMessage(new Message(agentAddress, getAdvertiserInfoMap().get(
					agentAddress)));

			for (SimulationAgent publisher : getPublishers()) {
				Publisher publisherAgent = (Publisher) publisher.getAgent();
				publisherAgent.sendPublisherInfo(agentAddress);
			}
		}
	}

	/**
	 * Delivers a message to the coordinator (the simulation). The coordinator
	 * must self validate the message.
	 * 
	 * @param message
	 *            the message
	 */
	@Override
	protected void messageReceived(Message message) {
		log.warning("received (ignoring) " + message);
	}

	public static String getSimulationRoleName(int simRole) {
		return simRole >= 0 && simRole < ROLE_NAME.length ? ROLE_NAME[simRole]
				: null;
	}

	public static int getSimulationRole(String role) {
		return ArrayUtils.indexOf(ROLE_NAME, role);
	}

	// -------------------------------------------------------------------
	// Logging handling
	// -------------------------------------------------------------------

	/**
	 * Validates this message to ensure that it may be delivered to the agent.
	 * Messages to the coordinator and the administration are never validated.
	 * 
	 * @param receiverAgent
	 *            the agent to deliver the message to
	 * @param message
	 *            the message to validate
	 * @return true if the message should be delivered and false otherwise
	 */
	@Override
	protected boolean validateMessage(SimulationAgent receiverAgent,
			Message message) {
		String sender = message.getSender();
		SimulationAgent senderAgent = getAgent(sender);
		int senderIndex;
		if (senderAgent == null) {
			// Messages from or the coordinator or administration are always
			// allowed.
			senderIndex = COORDINATOR_INDEX;

		} else if (senderAgent.getRole() == receiverAgent.getRole()) {
			// No two agents with the same role in the simulation may
			// communicate with each other. A simple security measure to
			// avoid manufacturer agents to communicate or deceive each
			// other.
			return false;

		} else {
			senderIndex = senderAgent.getIndex();
		}

		int receiverIndex = receiverAgent.getIndex();
		Transportable content = message.getContent();
		Class contentType = content.getClass();
		if (logContentType(contentType)) {
			LogWriter writer = getLogWriter();
			writer.message(senderIndex, receiverIndex, content, getServerTime());
			writer.commit();
		}

		int type = getContentType(contentType);
		if (type != TYPE_NONE) {
			getEventWriter().interaction(senderIndex, receiverIndex, type);
		}
		return true;

	}

	/**
	 * Validates this message to ensure that it may be broadcasted to all agents
	 * with the specified role.
	 * <p/>
	 * This method can also be used to log messages
	 * 
	 * @param sender
	 *            the agent sender the message
	 * @param role
	 *            the role of all receiving agents
	 * @param content
	 *            the message content
	 * @return true if the message should be delivered and false otherwise
	 */
	@Override
	protected boolean validateMessageToRole(SimulationAgent sender, int role,
			Transportable content) {
		// Only customer broadcast of RFQBundle to manufacturers are
		// allowed for now.
		// if (role == MANUFACTURER && senderAgent.getRole() == CUSTOMER
		// && content.getClass() == RFQBundle.class) {
		// logToRole(senderAgent.getIndex(), role, content);
		// return true;
		// }
		if (content instanceof AdxBidBundle) {
			return true;
		}
		return true;
	}

	/**
	 * Validates this message from the coordinator to ensure that it may be
	 * broadcasted to all agents with the specified role.
	 * <p/>
	 * This method can also be used to log messages
	 * 
	 * @param role
	 *            the role of all receiving agents
	 * @param content
	 *            the message content
	 * @return true if the message should be delivered and false otherwise
	 */
	@Override
	protected boolean validateMessageToRole(int role, Transportable content) {
		// Broadcasts from the coordinator are always allowed
		logToRole(COORDINATOR_INDEX, role, content);
		return true;
	}

	private void logToRole(int senderIndex, int role, Transportable content) {
		// Log this broadcast
		Class contentType = content.getClass();
		if (logContentType(contentType)) {
			LogWriter writer = getLogWriter();
			writer.messageToRole(senderIndex, role, content, getServerTime());
			writer.commit();
		}

		int type = getContentType(contentType);
		if (type != TYPE_NONE) {
			getEventWriter().interactionWithRole(senderIndex, role, type);
		}
	}

	private boolean logContentType(Class type) {
		return type != StartInfo.class;
	}

	private int getContentType(Class type) {
		return TYPE_NONE;
	}

	// -------------------------------------------------------------------
	// API to TACSCM builtin agents (trusted components)
	// -------------------------------------------------------------------

	@Override
	public final int getNumberOfAdvertisers() {
		return numberOfAdvertisers;
	}

	@Override
	public final SimulationAgent[] getPublishers() {
		return new SimulationAgent[] {};
	}

	@Override
	public final String[] getAdvertiserAddresses() {
		return advertiserAddresses;
	}

	public final String getAgentName(String agentAddress) {
		SimulationAgent agent = getAgent(agentAddress);
		return agent != null ? agent.getName() : agentAddress;
	}

	public final void transaction(String source, String recipient, double amount) {
		// log.finer("Transacted " + amount + " from " + source + " to "
		// + recipient);
		//
		// SimulationAgent sourceAgent = getAgent(source);
		// SimulationAgent receipientAgent = getAgent(recipient);
		//
		// if (receipientAgent != null && receipientAgent.getRole() ==
		// ADVERTISER) {
		// getBank().deposit(recipient, amount);
		// }
		// if (sourceAgent != null && sourceAgent.getRole() == ADVERTISER) {
		// getBank().withdraw(source, amount);
		// }
		//
		// int sourceIndex = sourceAgent != null ? sourceAgent.getIndex()
		// : COORDINATOR_INDEX;
		// int receipientIndex = receipientAgent != null ? receipientAgent
		// .getIndex() : COORDINATOR_INDEX;
		//
		// LogWriter writer = getLogWriter();
		// synchronized (writer) {
		// writer.node("transaction").attr("source", sourceIndex)
		// .attr("recipient", receipientIndex).attr("amount", amount)
		// .endNode("transaction");
		// }
	}

	// -------------------------------------------------------------------
	// API to Bank to allow it to send bank statuses
	// -------------------------------------------------------------------

	@Override
	public void sendBankStatus(String agentName, BankStatus status) {
		sendMessage(agentName, status);

		EventWriter eventWriter = getEventWriter();
		eventWriter.dataUpdated(getAgent(agentName).getIndex(),
				TACAdxConstants.DU_AD_NETWORK_BANK_ACCOUNT,
				status.getAccountBalance());
	}

	public final void sendInitialCampaign(String agentName,
			InitialCampaignMessage initialCampaignMessage) {
		sendMessage(agentName, initialCampaignMessage);
		getEventWriter().dataUpdated(agentIndex(agentName),
				TACAdxConstants.DU_INITIAL_CAMPAIGN, initialCampaignMessage);
	}

	public final void sendCampaignOpportunity(
			CampaignOpportunityMessage campaignOpportunityMessage) {
		sendToRole(AD_NETOWRK_ROLE_ID, campaignOpportunityMessage);
		getEventWriter().dataUpdated(TACAdxConstants.DU_CAMPAIGN_OPPORTUNITY,
				campaignOpportunityMessage);
	}

	public final void sendCampaignReport(String agentName,
			CampaignReport campaignReport) {
		sendMessage(agentName, campaignReport);
		getEventWriter().dataUpdated(agentIndex(agentName),
				TACAdxConstants.DU_CAMPAIGN_REPORT, campaignReport);
	}

	public final void sendDemandDailyNotification(String agentName,
			AdNetworkDailyNotification dailyNotification) {
		sendMessage(agentName, dailyNotification);
		getEventWriter().dataUpdated(agentIndex(agentName),
				TACAdxConstants.DU_DEMAND_DAILY_REPORT, dailyNotification);
	}

	public void broadcastImpressions(String advertiser, int impressions) {
		getEventWriter().dataUpdated(agentIndex(advertiser),
				TACAdxConstants.DU_IMPRESSIONS, impressions);
	}

	@Override
	public void broadcastPublisherReport(AdxPublisherReport report) {
		sendToRole(AD_NETOWRK_ROLE_ID, report);
		getEventWriter().dataUpdated(TACAdxConstants.DU_PUBLISHER_QUERY_REPORT,
				report);

	}
	
	public void sendCampaignAuctionReport(
			CampaignAuctionReport campaignAuctionReport) {
		getEventWriter().dataUpdated(TACAdxConstants.DU_CAMPAIGN_AUCTION_REPORT,
				campaignAuctionReport);
	}

	
	/* competing agents are not notified regarding private auction details!
	public void sendToAgentCampaignAuctionReport(String agentName,
			CampaignAuctionReport campaignAuctionReport) {
		sendMessage(agentName, campaignAuctionReport);
	}
	*/

	public void broadcastClicks(String advertiser, int clicks) {
		getEventWriter().dataUpdated(agentIndex(advertiser),
				TACAdxConstants.DU_CLICKS, clicks);
	}

	@Override
	public PublisherCatalog getPublisherCatalog() {
		return publisherCatalog;
	}

	@Override
	public List<AdxUser> getUserPopulation() {
		return userPopulation;
	}

	@Override
	public Map<Device, Integer> getDeviceDistributionMap() {
		return deviceDistributionMap;
	}

	@Override
	public Map<AdType, Integer> getAdTypeDistributionMap() {
		return adTypeDistributionMap;
	}

	@Override
	public void broadcastAdNetowrkReport(String bidder, AdNetworkReport report) {
		sendMessage(bidder, report);
		getEventWriter().dataUpdated(agentIndex(bidder),
				TACAdxConstants.DU_AD_NETWORK_REPORT, report);
		int value = 0;
		for (AdNetworkKey adNetworkKey : report) {
			value += report.getAdNetworkReportEntry(adNetworkKey).getWinCount();
		}
		getEventWriter().dataUpdated(agentIndex(bidder),
				TACAdxConstants.DU_AD_NETWORK_WIN_COUNT, value);
		broadcastADXExpense(bidder, report.getDailyCost());
	}

	public void broadcastAdNetworkQualityRating(String adNet, double rating) {
		getEventWriter().dataUpdated(agentIndex(adNet),
				TACAdxConstants.DU_AD_NETWORK_QUALITY_RATING, rating);
	}

	public void broadcastAdNetworkRevenue(String adNet, double revenue) {
		getBank().deposit(adNet, revenue);
		getEventWriter().dataUpdated(agentIndex(adNet),
				TACAdxConstants.DU_AD_NETWORK_REVENUE, revenue);
	}

	public void broadcastUCSWin(String adNet,
			double expense) {
		getEventWriter().dataUpdated(agentIndex(adNet),
				TACAdxConstants.DU_AD_NETWORK_UCS_EXPENSE, expense);
		broadcastAdNetworkExpense(adNet, expense);
	}

	public void broadcastADXExpense(String adNet, double expense) {
		getEventWriter().dataUpdated(agentIndex(adNet),
				TACAdxConstants.DU_AD_NETWORK_ADX_EXPENSE, expense);
		broadcastAdNetworkExpense(adNet, expense);
	}

	public void broadcastAdNetworkExpense(String adNet, double expense) {
		getBank().withdraw(adNet, expense);
		getEventWriter().dataUpdated(agentIndex(adNet),
				TACAdxConstants.DU_AD_NETWORK_EXPENSE, expense);
	}

	@Override
	public AdxAuctioneer getAuctioneer() {
		return auctioneer;
	}

	public Map<String, AdvertiserInfo> getAdvertiserInfoMap() {
		return advertiserInfoMap;
	}

	public void setAdvertiserInfoMap(
			Map<String, AdvertiserInfo> advertiserInfoMap) {
		this.advertiserInfoMap = advertiserInfoMap;
	}

	/**
	 * @return the competition
	 */
	public Competition getCompetition() {
		return competition;
	}

	/**
	 * @param competition
	 *            the competition to set
	 */
	public void setCompetition(Competition competition) {
		this.competition = competition;
	}

	/**
	 * @return the bank
	 */
	public Bank getBank() {
		return bank;
	}

	/**
	 * @param bank
	 *            the bank to set
	 */
	public void setBank(Bank bank) {
		this.bank = bank;
	}

	@Override
	public AdxBidManager getAdxBidManager() {
		return injector.getInstance(AdxBidManager.class);
	}

	@Override
	public AdxBidTracker getAdxBidTracker() {
		return injector.getInstance(AdxBidTracker.class);
	}

}
