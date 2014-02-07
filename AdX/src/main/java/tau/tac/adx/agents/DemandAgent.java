/*
 */
package tau.tac.adx.agents;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Message;
import tau.tac.adx.AdxManager;
import tau.tac.adx.auction.data.AuctionState;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.demand.CampaignImpl;
import tau.tac.adx.demand.QualityManager;
import tau.tac.adx.demand.QualityManagerImpl;
import tau.tac.adx.demand.UserClassificationService;
import tau.tac.adx.demand.UserClassificationServiceImpl;
import tau.tac.adx.messages.AuctionMessage;
import tau.tac.adx.messages.CampaignLimitReached;
import tau.tac.adx.messages.CampaignNotification;
import tau.tac.adx.messages.UserClassificationServiceNotification;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.AdNetBidMessage;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.CampaignReport;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReport;
import tau.tac.adx.sim.Builtin;
import tau.tac.adx.sim.TACAdxConstants;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.eventbus.Subscribe;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class DemandAgent extends Builtin {

	private int day;

	private static final int TOTAL_POPULATION_DEFAULT = 10000;
	private static int total_population;
	private static int aloc_cmp_reach;

	private static final int ALOC_CMP_LENGTH_DEFAULT = 5;
	private static int   alloc_cmp_length;

	private static final double CMP_VC_DEFAULT = 2.0;
	private static double   cmp_vc;

	private static final double CMP_MC_DEFAULT = 1.5;
	private static double   cmp_mc;

	private static Random random;

	private static int[] CMP_LENGTHS_DEFAULT = { 3, 5, 10 };
	private static int   CMP_LENGTHS_COUNT_DEFAULT = 3;
	
	private static int[] cmp_lengths;
	private static int   cmp_lengths_count;

	private static int[] CMP_REACHS;

	private Logger log;

	private QualityManager qualityManager;
	private ListMultimap<String, Campaign> adNetCampaigns;
	private Campaign pendingCampaign;

	private UserClassificationService ucs;

	/**
	 * Default constructor.
	 */
	public DemandAgent() {
		super(TACAdxConstants.DEMAND_AGENT_NAME);
	}

	public void preNextTimeUnit(int date) {
		day = date;
		if (day == 0) {
			zeroDayInitialization();
		} else {
			auctionTomorrowsCampaign(day);
			ucs.auction(day);

			for (Campaign campaign : adNetCampaigns.values()) {
				campaign.preNextTimeUnit(date);
			}
			reportAuctionResutls(day);
		}
		getSimulation().getEventBus().post(
				new UserClassificationServiceNotification(ucs));
		createAndPublishTomorrowsPendingCampaign();
	}

	private void createAndPublishTomorrowsPendingCampaign() {
		/*
		 * Create next campaign opportunity (to start on day + 2) and notify
		 * competing adNetwork agents
		 */

		int lastCmpDay = day + 1 + cmp_lengths[random.nextInt(cmp_lengths_count)];

		if (lastCmpDay < 60) {

			pendingCampaign = new CampaignImpl(qualityManager,
					CMP_REACHS[random.nextInt(3)], day + 2, lastCmpDay,
					MarketSegment.randomMarketSegment(), cmp_vc,
					cmp_mc);

			pendingCampaign.registerToEventBus();

			log.log(Level.INFO,
					"Day " + day + " :"
							+ "Notifying new campaign opportunity: "
							+ pendingCampaign.logToString());
			getSimulation().sendCampaignOpportunity(
					new CampaignOpportunityMessage(pendingCampaign, day));
		} else {
			/**
			 * All campaigns must end during the game
			 */
			pendingCampaign = null;
		}

	}

	private void reportAuctionResutls(int date) {
		/*
		 * report auctions result and campaigns stats to adNet agents
		 */
		log.log(Level.FINE, "Day "
				+ day
				+ " :"
				+ "Reporting auction results..."
				+ ((pendingCampaign != null) ? pendingCampaign.logToString()
						: "No pending campaign"));

		for (String advertiser : getAdxAdvertiserAddresses()) {

			/*
			 * we only report adx simulation results starting from day 2 (w.r.t.
			 * day 1)
			 */
			if (date >= 2) {
				CampaignReport report = new CampaignReport();
				for (Campaign campaign : adNetCampaigns.values()) {
					if (campaign.isAllocated()
							&& (campaign.getDayStart() < date)
							&& (advertiser.equals(campaign.getAdvertiser()))
							&& (campaign.shouldReport())) {
						report.addStatsEntry(campaign.getId(),
								campaign.getTotals());
					}
				}
				getSimulation().sendCampaignReport(advertiser, report);
			}

			AdNetworkDailyNotification adNetworkNotification = new AdNetworkDailyNotification(
					ucs.getAdNetData(advertiser), pendingCampaign,
					qualityManager.getQualityScore(advertiser));

			/* remove campaign cost for non-winning advertisers */
			if ((pendingCampaign != null)
					&& (!advertiser.equals(pendingCampaign.getAdvertiser()))) {
				adNetworkNotification.zeroCost();
			}

			getSimulation().sendDemandDailyNotification(advertiser,
					adNetworkNotification);
		}
	}

	private void auctionTomorrowsCampaign(int date) {
		/*
		 * Auction campaign and add to repository
		 * 
		 * on day 1 we auction the campaign starting on day 2 for which bids
		 * where received on day 0 on day n we auction the campaign starting on
		 * day n+1 for which bids where received on day n-1
		 * 
		 * Note: the campaign (pendingCampaign) was created on day n-1
		 */
		if (pendingCampaign != null) {
			log.log(Level.INFO, "Day " + day + " : Auction pending campaign: "
					+ pendingCampaign.logToString());
			CampaignAuctionReport campaignAuctionReport = pendingCampaign.auction();
			if (campaignAuctionReport != null) {
				getSimulation().sendCampaignAuctionReport(campaignAuctionReport);
			}
			if (pendingCampaign.isAllocated()) {
				adNetCampaigns.put(pendingCampaign.getAdvertiser(),
						pendingCampaign);

				/* notify regarding newly allocate campaign */
				getSimulation().getEventBus().post(
						new CampaignNotification(pendingCampaign));

			} else { /* not allocated, auction failed (reserve not met) */
				log.log(Level.INFO, "Day " + day
						+ " : Campaign auction: Not allocated");				
			}
			
		} else {
			log.log(Level.INFO, "Day " + day
					+ " : No pending campaign to auction");
		}
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Users#setup()
	 */
	@Override
	protected void setup() {
		int numOfCompetitors = getSimulation().getNumberOfAdvertisers();
		
		random = new Random();
		
		total_population = getSimulation().getConfig().getPropertyAsInt(
				"adxusers.population_size", TOTAL_POPULATION_DEFAULT);

		alloc_cmp_length = getSimulation().getConfig().getPropertyAsInt(
				"campaigns.preallocated.length", ALOC_CMP_LENGTH_DEFAULT);
		
		cmp_vc =  getSimulation().getConfig().getPropertyAsDouble(
				"campaigns.video_coef", CMP_VC_DEFAULT);
		
		cmp_mc =  getSimulation().getConfig().getPropertyAsDouble(
				"campaigns.mobile_coef", CMP_MC_DEFAULT);
		
		String[] cmp_lengths_str = getSimulation().getConfig().getPropertyAsArray("campaigns.lengths");
		if (cmp_lengths_str == null) {
			cmp_lengths_count = CMP_LENGTHS_COUNT_DEFAULT;
			cmp_lengths = CMP_LENGTHS_DEFAULT;			
		} else {
			cmp_lengths_count = cmp_lengths_str.length;
			cmp_lengths = new int[cmp_lengths_count];
			for (int i = 0; i < cmp_lengths_count; i++) {
				cmp_lengths[i] = Integer.parseInt(cmp_lengths_str[i]);
			}
		}
		
		CMP_REACHS = new int[cmp_lengths_count];
		for (int i = 0; i < cmp_lengths_count; i++) { /* each campaign should target 1/competitiors of the population daily */
			CMP_REACHS[i] = (total_population / numOfCompetitors) * cmp_lengths[i];
		}

		aloc_cmp_reach = total_population / numOfCompetitors;

		this.log = Logger.getLogger(DemandAgent.class.getName());

		log.info("setting up...");

		getSimulation().getEventBus().register(this);

		adNetCampaigns = ArrayListMultimap.create();

		qualityManager = new QualityManagerImpl();

		ucs = new UserClassificationServiceImpl();
		AdxManager.getInstance().setUserClassificationService(ucs);

		log.fine("Finished setup");
	}

	private void zeroDayInitialization() {
		/*
		 * Allocate an initial campaign to each competing adNet agent and notify
		 */
		for (String advertiser : getAdxAdvertiserAddresses()) {

			qualityManager.addAdvertiser(advertiser);
			Campaign campaign = new CampaignImpl(qualityManager,
					aloc_cmp_reach, 1, alloc_cmp_length,
					MarketSegment.randomMarketSegment(), cmp_vc,
					cmp_mc);

			campaign.allocateToAdvertiser(advertiser);
			log.log(Level.FINE,
					"Allocating initial campaign : " + campaign.logToString());
			campaign.registerToEventBus();

			adNetCampaigns.put(advertiser, campaign);

			getSimulation().sendInitialCampaign(
					advertiser,
					new InitialCampaignMessage(campaign, this.getAddress(),
							AdxManager.getInstance().getAdxAgentAddress()));

			getSimulation().getEventBus().post(
					new CampaignNotification(campaign));

			ucs.updateAdvertiserBid(advertiser, 0, 0);
		}
	}

	/**
	 * @see Builtin#stopped()
	 */
	@Override
	protected void stopped() {
	}

	/**
	 * @see Builtin#shutdown()
	 */
	@Override
	protected void shutdown() {
	}

	/**
	 * @see se.sics.tasim.aw.Agent#messageReceived(se.sics.tasim.aw.Message)
	 */
	@Override
	protected void messageReceived(Message message) {
		String sender = message.getSender();
		Transportable content = message.getContent();

		if (content instanceof AdNetBidMessage) {

			AdNetBidMessage cbm = (AdNetBidMessage) content;

			log.log(Level.FINE,
					"Day " + day + " :" + "Got AdNetBidMessage from " + sender
							+ " :" + " UCS Bid: " + cbm.getUcsBid()
							+ " Cmp ID: " + cbm.getCampaignId() + " Cmp Bid: "
							+ cbm.getCampaignBudget());

			/*
			 * collect campaign bids for campaign opportunities
			 */
			if ((pendingCampaign != null)
					&& ((pendingCampaign.getId() == cbm.getCampaignId())))
				pendingCampaign.addAdvertiserBid(sender,
						cbm.getCampaignBudget());
			/*
			 * update adNet ucs bid
			 */
			ucs.updateAdvertiserBid(sender, cbm.getUcsBid(), day);
		}

	}

	@Subscribe
	public void impressed(AuctionMessage message) {
		/* fetch campaign */
		Campaign cmpn = message.getAuctionResult().getCampaign();
		AuctionState auctionState = message.getAuctionResult()
				.getAuctionState();
		if (auctionState == AuctionState.AUCTION_COPMLETED) {

			cmpn.impress(message.getUser(), message.getQuery().getAdType(),
					message.getQuery().getDevice(), message.getAuctionResult()
							.getWinningPrice());

			if (cmpn.isOverTodaysLimit() || cmpn.isOverTotalLimits()) {
				/* notify on transition campaign limit expiration */
				getSimulation().getEventBus().post(
						new CampaignLimitReached(cmpn.getId(), cmpn
								.getAdvertiser()));
				log.log(Level.FINER,
						"Day "
								+ day
								+ " :Campaign limit expired Impressed while over limit: "
								+ cmpn.getId() + ", daily limit was: "
								+ cmpn.getImpressionLimit() + ", "
								+ cmpn.getBudgetlimit() +  " values are: "
								+ cmpn.getTodayStats().getTargetedImps() + ", "
								+ cmpn.getTodayStats().getCost() + ", total limit was: "
								+ cmpn.getTotalImpressionLimit() + ", "
								+ cmpn.getTotalBudgetlimit() + " total values are: "
								+ cmpn.getTotals().getTargetedImps() + cmpn.getTodayStats().getTargetedImps() + ", "
								+ cmpn.getTotals().getCost() + cmpn.getTodayStats().getCost());
			}
		}
	}

	@Override
	public void nextTimeUnit(int timeUnit) {
		// TODO Auto-generated method stub
	}

}
