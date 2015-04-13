package tau.tac.adx.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.Message;
import se.sics.tasim.props.SimulationStatus;
import se.sics.tasim.props.StartInfo;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.demand.CampaignStats;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.props.PublisherCatalogEntry;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.AdNetBidMessage;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.CampaignReport;
import tau.tac.adx.report.demand.CampaignReportKey;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReport;
import tau.tac.adx.report.publisher.AdxPublisherReport;
import tau.tac.adx.report.publisher.AdxPublisherReportEntry;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.BankStatus;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class GreedyLuckyAdNetwork extends Agent {

	private final Logger log = Logger.getLogger(GreedyLuckyAdNetwork.class.getName());

	/*
	 * Basic simulation information. An agent should receive the {@link
	 * StartInfo} at the beginning of the game or during recovery.
	 */
	@SuppressWarnings("unused")
	private StartInfo startInfo;

	/**
	 * Messages received:
	 * 
	 * We keep all the {@link CampaignReport campaign reports} delivered to the
	 * agent. We also keep the initialization messages {@link PublisherCatalog}
	 * and {@link InitialCampaignMessage} and the most recent messages and
	 * reports {@link CampaignOpportunityMessage}, {@link CampaignReport}, and
	 * {@link AdNetworkDailyNotification}.
	 */
	private final Queue<CampaignReport> campaignReports;
	private PublisherCatalog publisherCatalog;
	private InitialCampaignMessage initialCampaignMessage;
	private AdNetworkDailyNotification adNetworkDailyNotification;

	/*
	 * The addresses of server entities to which the agent should send the daily
	 * bids data
	 */
	private String demandAgentAddress;
	private String adxAgentAddress;

	/**
	 * Information regarding the latest campaign opportunity announced
	 */
	private CampaignData pendingCampaign;

	/**
	 * We maintain a collection (mapped by the campaign id) of the campaigns won
	 * by our agent.
	 */
	private Map<Integer, CampaignData> myCampaigns;

	/*
	 * the bidBundle to be sent daily to the AdX
	 */
	private AdxBidBundle bidBundle;

	/*
	 * current day of simulation
	 */
	private int day;
	private String[] publisherNames;

	private Double cmpBidMillis;

	private double qualityScore;

	private UcsModel ucsModel;

	public GreedyLuckyAdNetwork() {
		demandAgentAddress = null;
		adxAgentAddress = null;

		campaignReports = new LinkedList<CampaignReport>();

		/* in case we join inthe middle of a game .. */
		bidBundle = new AdxBidBundle();
		ucsModel = new UcsModel();
		myCampaigns = new HashMap<Integer, CampaignData>();
		cmpBidMillis = new Double(0.0);
	}

	@Override
	protected void messageReceived(Message message) {
		try {
			Transportable content = message.getContent();

			//log.fine(message.getContent().getClass().toString());
			//System.out.println(message.getContent().getClass().toString());
			//contentSystem.out.println("MESSAGE FROM SERVER");
			

			if (content instanceof InitialCampaignMessage) {
				handleInitialCampaignMessage((InitialCampaignMessage) content);
			} else if (content instanceof CampaignOpportunityMessage) {
				handleICampaignOpportunityMessage((CampaignOpportunityMessage) content);
			} else if (content instanceof CampaignReport) {
				handleCampaignReport((CampaignReport) content);
			} else if (content instanceof AdNetworkDailyNotification) {
				handleAdNetworkDailyNotification((AdNetworkDailyNotification) content);
			} else if (content instanceof AdxPublisherReport) {
				handleAdxPublisherReport((AdxPublisherReport) content);
			} else if (content instanceof SimulationStatus) {
				handleSimulationStatus((SimulationStatus) content);
			} else if (content instanceof PublisherCatalog) {
				handlePublisherCatalog((PublisherCatalog) content);
			} else if (content instanceof AdNetworkReport) {
				handleAdNetworkReport((AdNetworkReport) content);
			} else if (content instanceof StartInfo) {
				handleStartInfo((StartInfo) content);
			} else if (content instanceof BankStatus) {
				handleBankStatus((BankStatus) content);
			} else if (content instanceof CampaignAuctionReport) {
				hadnleCampaignAuctionReport((CampaignAuctionReport) content);
			} else {
				System.out.println("UNKNOWN Message Received: " + content);
			}

		} catch (NullPointerException e) {
			this.log.log(Level.SEVERE,
					"Exception thrown while trying to parse message." + e);
			return;
		}
	}

	private void hadnleCampaignAuctionReport(@SuppressWarnings("unused") CampaignAuctionReport content) {
		// ingoring - this message is obsolete
	}

	private void handleBankStatus(BankStatus content) {
		System.out.println("Day " + day + " :" + content.toString());
	}

	/**
	 * Processes the start information.
	 * 
	 * @param startInfo
	 *            the start information.
	 */
	protected void handleStartInfo(StartInfo startInfo) {
		this.startInfo = startInfo;
	}

	/**
	 * Process the reported set of publishers
	 * 
	 * @param publisherCatalog
	 */
	private void handlePublisherCatalog(PublisherCatalog publisherCatalog) {
		this.publisherCatalog = publisherCatalog;
		getPublishersNames();

	}

	/**
	 * On day 0, a campaign (the "initial campaign") is allocated to each
	 * competing agent. The campaign starts on day 1. The address of the
	 * server's AdxAgent (to which bid bundles are sent) and DemandAgent (to
	 * which bids regarding campaign opportunities may be sent in subsequent
	 * days) are also reported in the initial campaign message
	 */
	private void handleInitialCampaignMessage(
			InitialCampaignMessage campaignMessage) {
		System.out.println(campaignMessage.toString());

		day = 0;

		initialCampaignMessage = campaignMessage;
		demandAgentAddress = campaignMessage.getDemandAgentAddress();
		adxAgentAddress = campaignMessage.getAdxAgentAddress();

		CampaignData campaignData = new CampaignData(initialCampaignMessage);
		campaignData
				.setBudget(initialCampaignMessage.getBudgetMillis() / 1000.0);
		genCampaignQueries(campaignData);

		/*
		 * The initial campaign is already allocated to our agent so we add it
		 * to our allocated-campaigns list.
		 */
		System.out.println("Day " + day + ": Allocated campaign - "
				+ campaignData);
		myCampaigns.put(initialCampaignMessage.getId(), campaignData);
	}

	/**
	 * On day n ( > 0) a campaign opportunity is announced to the competing
	 * agents. The campaign starts on day n + 2 or later and the agents may send
	 * (on day n) related bids (attempting to win the campaign). The allocation
	 * (the winner) is announced to the competing agents during day n + 1.
	 */
	private void handleICampaignOpportunityMessage(
			CampaignOpportunityMessage com) {

		day = com.getDay();

		pendingCampaign = new CampaignData(com);
		System.out.println("Day " + day + ": Campaign opportunity - "
				+ pendingCampaign);

		/*
		 * The campaign requires com.getReachImps() impressions. The competing
		 * Ad Networks bid for the total campaign Budget (that is, the ad
		 * network that offers the lowest budget gets the campaign allocated).
		 * The advertiser is willing to pay the AdNetwork at most 1$ CPM,
		 * therefore the total number of impressions may be treated as a reserve
		 * (upper bound) price for the auction.
		 */

		long cmpimps = com.getReachImps();

		// long cmpBidMillis = random.nextInt((int)cmpimps);

		cmpBidMillis = (new Double(cmpimps)) * qualityScore - 1;

		System.out.println("Day " + day
				+ ": Campaign total budget bid (millis): "
				+ cmpBidMillis.longValue());

	}

	/**
	 * On day n ( > 0), the result of the UserClassificationService and Campaign
	 * auctions (for which the competing agents sent bids during day n -1) are
	 * reported. The reported Campaign starts in day n+1 or later and the user
	 * classification service level is applicable starting from day n+1.
	 */
	private void handleAdNetworkDailyNotification(
			AdNetworkDailyNotification notificationMessage) {

		adNetworkDailyNotification = notificationMessage;

		System.out.println("Day " + day + ": Daily notification for campaign "
				+ adNetworkDailyNotification.getCampaignId());

		String campaignAllocatedTo = " allocated to "
				+ notificationMessage.getWinner();

		if ((pendingCampaign != null)
				&& (pendingCampaign.id == adNetworkDailyNotification
						.getCampaignId())
				&& (notificationMessage.getCostMillis() != 0)) {

			/* add campaign to list of won campaigns */
			pendingCampaign
					.setBudget(notificationMessage.getCostMillis() / 1000.0);

			genCampaignQueries(pendingCampaign);
			myCampaigns.put(pendingCampaign.id, pendingCampaign);

			campaignAllocatedTo = " WON at cost (Millis)"
					+ notificationMessage.getCostMillis();
		}

		qualityScore = notificationMessage.getQualityScore();

		ucsModel.ucsUpdate(notificationMessage.getServiceLevel(),
				notificationMessage.getPrice(), activeCampaigns());

		System.out
				.println("Day " + day + ": " + campaignAllocatedTo
						+ ". UCS Level set to "
						+ notificationMessage.getServiceLevel() + " at price "
						+ notificationMessage.getPrice()
						+ " Quality Score is: "
						+ notificationMessage.getQualityScore());
	}

	/**
	 * The SimulationStatus message received on day n indicates that the
	 * calculation time is up and the agent is requested to send its bid bundle
	 * and cmp/ucs bids to the AdX.
	 */
	private void handleSimulationStatus(SimulationStatus simulationStatus) {
		System.out.println("Day " + day
				+ " : Simulation Status Received. Server reporting end of day "
				+ simulationStatus.getCurrentDate() + " consuming "
				+ simulationStatus.getConsumedMillis() + " milliseconds");
		int cm = simulationStatus.getConsumedMillis();

		if (cm > 4200) {
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXX             XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXX    " + cm
					+ "    XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXX             XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out
					.println(" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

			this.log.log(Level.SEVERE,
					" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			this.log.log(Level.SEVERE, "Day " + day + " overconsumed: " + cm);
			this.log.log(Level.SEVERE,
					" XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		}

		System.out.println("Day " + day + ": Ucs bid is "
				+ (ucsModel != null ? ucsModel.getBid() : "...No Model"));
		/* Note: Campaign bid is in millis */
		AdNetBidMessage bids = new AdNetBidMessage(
				ucsModel != null ? ucsModel.getBid() : 0,
				pendingCampaign != null ? pendingCampaign.id : 0,
				cmpBidMillis != null ? cmpBidMillis.longValue() : 0);
		if (demandAgentAddress != null)
			sendMessage(demandAgentAddress, bids);
		sendBidAndAds();
		System.out.println("Day " + day + " ended. Starting next day");
		++day;
	}

	private boolean activeCampaigns() {
		int dayBiddingFor = day + 1;
		for (CampaignData cmpgn : myCampaigns.values()) {
			if ((dayBiddingFor >= cmpgn.dayStart)
					&& (dayBiddingFor <= cmpgn.dayEnd)
					&& (cmpgn.impsTogo() > 0)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 */
	protected void sendBidAndAds() {

		bidBundle = new AdxBidBundle();

		int dayBiddingFor = day + 1;

		Random random = new Random();

		/* A random bid, fixed for all queries of the campaign */
		/*
		 * Note: bidding per 1000 imps (CPM) - no more than average budget
		 * revenue per imp
		 */

		double rbid = 10.0 * random.nextDouble();

		/*
		 * add bid entries w.r.t. each active campaign with remaining contracted
		 * impressions.
		 * 
		 * for now, a single entry per active campaign is added for queries of
		 * matching target segment.
		 */

		for (CampaignData campaign : myCampaigns.values()) {

			if ((dayBiddingFor >= campaign.dayStart)
					&& (dayBiddingFor <= campaign.dayEnd)
					&& (campaign.impsTogo() > 0)) {

				for (AdxQuery query : campaign.campaignQueries) {
					/*
					 * among matching entries with the same campaign id, the AdX
					 * randomly chooses an entry according to the designated
					 * weight. by setting a constant weight 1, we create a
					 * uniform probability over active campaigns
					 */
					bidBundle.addQuery(query, rbid, new Ad(null), campaign.id,
							1);
				}

				double impressionLimit = campaign.impsTogo();
				double budgetLimit = campaign.budget;
				bidBundle.setCampaignDailyLimit(campaign.id,
						(int) impressionLimit, budgetLimit);

				System.out.println("Day " + day
						+ ": Updated Bid Bundle entries " + "for Campaign id "
						+ campaign.id);
			}
		}

		if (bidBundle != null) {
			System.out.println("Day " + day + ": Sending BidBundle");
			if (adxAgentAddress != null)
				sendMessage(adxAgentAddress, bidBundle);
		}
	}

	/**
	 * Campaigns performance w.r.t. each allocated campaign
	 */
	private void handleCampaignReport(CampaignReport campaignReport) {

		campaignReports.add(campaignReport);

		/*
		 * for each campaign, the accumulated statistics from day 1 up to day
		 * n-1 are reported
		 */
		for (CampaignReportKey campaignKey : campaignReport.keys()) {
			int cmpId = campaignKey.getCampaignId();
			CampaignStats cstats = campaignReport.getCampaignReportEntry(
					campaignKey).getCampaignStats();
			myCampaigns.get(cmpId).setStats(cstats);

			System.out.println("Day " + day + ": Updating campaign " + cmpId
					+ " stats: " + cstats.getTargetedImps() + " tgtImps "
					+ cstats.getOtherImps() + " nonTgtImps. Cost of imps is "
					+ cstats.getCost());
		}
	}

	/**
	 * Users and Publishers statistics: popularity and ad type orientation
	 */
	@SuppressWarnings("static-method")
	private void handleAdxPublisherReport(AdxPublisherReport adxPublisherReport) {
		System.out.println("Publishers Report: ");
		for (PublisherCatalogEntry publisherKey : adxPublisherReport.keys()) {
			AdxPublisherReportEntry entry = adxPublisherReport
					.getEntry(publisherKey);
			System.out.println(entry.toString());
		}
	}

	/**
	 * 
	 * @param AdNetworkReport
	 */
	private void handleAdNetworkReport(@SuppressWarnings("unused") AdNetworkReport adnetReport) {

		System.out.println("Day " + day + " : AdNetworkReport");
		/*
		 * for (AdNetworkKey adnetKey : adnetReport.keys()) {
		 * 
		 * double rnd = Math.random(); if (rnd > 0.95) { AdNetworkReportEntry
		 * entry = adnetReport .getAdNetworkReportEntry(adnetKey);
		 * System.out.println(adnetKey + " " + entry); } }
		 */
	}

	@Override
	protected void simulationSetup() {

		day = 0;
		bidBundle = new AdxBidBundle();

		qualityScore = 1.0;

		ucsModel = new UcsModel();

		myCampaigns = new HashMap<Integer, CampaignData>();
		log.fine("AdNet " + getName() + " simulationSetup");
	}

	@Override
	protected void simulationFinished() {
		campaignReports.clear();
		bidBundle = null;
	}

	/*
	 * genarates an array of the publishers names
	 */
	private void getPublishersNames() {
		if (null == publisherNames && publisherCatalog != null) {
			ArrayList<String> names = new ArrayList<String>();
			for (PublisherCatalogEntry pce : publisherCatalog) {
				names.add(pce.getPublisherName());
			}

			publisherNames = new String[names.size()];
			names.toArray(publisherNames);
		}
	}

	/*
	 * genarates the campaign queries relevant for the specific campaign, and
	 * assign them as the campaigns campaignQueries field
	 */
	private void genCampaignQueries(CampaignData campaignData) {
		Set<AdxQuery> campaignQueriesSet = new HashSet<AdxQuery>();
		for (String PublisherName : publisherNames) {
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.mobile, AdType.text));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.mobile, AdType.video));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.pc, AdType.text));
			campaignQueriesSet.add(new AdxQuery(PublisherName,
					campaignData.targetSegment, Device.pc, AdType.video));
		}

		campaignData.campaignQueries = new AdxQuery[campaignQueriesSet.size()];
		campaignQueriesSet.toArray(campaignData.campaignQueries);
		// System.out.println("!!!!!!!!!!!!!!!!!!!!!!"+Arrays.toString(campaignData.campaignQueries)+"!!!!!!!!!!!!!!!!");
	}

	private class UcsModel {
		/* campaign attributes as set by server */
		/*
		 * The current bid and targetted percentile for the user classification
		 * service
		 */
		private Random random;

		private double ucsBid;
		private double ucsBidPercentile;

		// logistic regression parameters
		private double ucsLearningRate = 0.3;
		private double ucsAlpha;
		private double ucsBeta;

		// latest reported level level and cost, to be applicable during the
		// following simulation day
		@SuppressWarnings("unused")
		private double ucsLevel;
		@SuppressWarnings("unused")
		private double ucsCost;

		// linear regression for cost (given level) === gamma + delta*level
		private double ucsGamma;
		private double ucsDelta;

		public UcsModel() {
			random = new Random();

			ucsLevel = 1.0;
			ucsCost = 0.0;
			ucsBidPercentile = 0.8;

			// logistic regression initial values and constants for
			// prob(toplevel | bid).
			ucsAlpha = -10.0;
			ucsBeta = 10.0;

			// initial linear regression parameters
			ucsGamma = 0.0;
			ucsDelta = 1.0;

			/* initial bid */
			ucsBid = ucsBidByPercentile();

		}

		private double ucsFactor(double percentile) {
			// We are bidding at percentile% of winning top level
			return Math.log((1.0 / percentile) - 1.0);
		}

		private double ucsBidByPercentile() {
			return (ucsFactor(ucsBidPercentile) - ucsAlpha) / ucsBeta;
		}

		public double getBid() {
			return ucsBid;
		}

		@SuppressWarnings("unused")
		public double getCost(double level) {
			return ucsGamma + ucsDelta * level;
		}

		public void ucsUpdate(double level, double cost, boolean bidHigh) {
			double yk = level == 1.0 ? 1.0 : 0;

			// apply logistic regression update for pr(Top|bid) parameters,
			// using current ucsBidPercentile and ucsBid
			ucsAlpha += ucsLearningRate * (yk - ucsBidPercentile);
			ucsBeta += ucsLearningRate * (yk - ucsBidPercentile) * ucsBid;

			// set new bidPercentile and ucsBid,
			// bid at ucsBidPercentile probability of winning top level
			ucsBidPercentile = bidHigh ? 0.9 : 0.9 * random.nextDouble();
			ucsBid = ucsBidByPercentile();

			// apply linear regression update for (Cost|level) parameters
			ucsGamma += ucsLearningRate
					* (cost - (ucsGamma + ucsDelta * level));
			ucsDelta += ucsLearningRate
					* (cost - (ucsGamma + ucsDelta * level)) * level;
		}

	}

	private class CampaignData {
		/* campaign attributes as set by server */
		Long reachImps;
		long dayStart;
		long dayEnd;
		Set<MarketSegment> targetSegment;
		double videoCoef;
		double mobileCoef;
		int id;
		private AdxQuery[] campaignQueries;// array of queries relvent for the
											// campaign.

		/* campaign info as reported */
		CampaignStats stats;
		double budget;

		public CampaignData(InitialCampaignMessage icm) {
			reachImps = icm.getReachImps();
			dayStart = icm.getDayStart();
			dayEnd = icm.getDayEnd();
			targetSegment = icm.getTargetSegment();
			videoCoef = icm.getVideoCoef();
			mobileCoef = icm.getMobileCoef();
			id = icm.getId();

			stats = new CampaignStats(0, 0, 0);
			budget = 0.0;
		}

		public void setBudget(double d) {
			budget = d;
		}

		public CampaignData(CampaignOpportunityMessage com) {
			dayStart = com.getDayStart();
			dayEnd = com.getDayEnd();
			id = com.getId();
			reachImps = com.getReachImps();
			targetSegment = com.getTargetSegment();
			mobileCoef = com.getMobileCoef();
			videoCoef = com.getVideoCoef();
			stats = new CampaignStats(0, 0, 0);
			budget = 0.0;
		}

		@Override
		public String toString() {
			return "Campaign ID " + id + ": " + "day " + dayStart + " to "
					+ dayEnd + " " + MarketSegment.names(targetSegment)
					+ ", reach: " + reachImps + " coefs: (v=" + videoCoef
					+ ", m=" + mobileCoef + ")";
		}

		int impsTogo() {
			return (int) Math.max(0, reachImps - stats.getTargetedImps());
		}

		void setStats(CampaignStats s) {
			stats.setValues(s);
		}

		@SuppressWarnings("unused")
		public AdxQuery[] getCampaignQueries() {
			return campaignQueries;
		}

		@SuppressWarnings("unused")
		public void setCampaignQueries(AdxQuery[] campaignQueries) {
			this.campaignQueries = campaignQueries;
		}

	}

}
