/*
 */
package tau.tac.adx.agents;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Message;
import tau.tac.adx.AdxManager;
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
import tau.tac.adx.sim.Builtin;
import tau.tac.adx.sim.TACAdxConstants;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.eventbus.Subscribe;

public class DemandAgent extends Builtin {

	private int day;

	private static final int TOTAL_POPULATION_DEFAULT = 10000;
	private static int total_population;
	private static int aloc_cmp_reach;

	private static final int ALOC_CMP_LENGTH = 5;

	private static final double ALOC_CMP_VC = 2.0;
	private static final double ALOC_CMP_MC = 1.5;

	private static Random random;

	private static int[] CMP_LENGTHS = { 3, 5, 10 };
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

			consolidateCmpaignStatistics(day);
			reportAuctionResutls(day);
		}
		getSimulation().getEventBus().post(
				new UserClassificationServiceNotification(ucs));
		createAndPublishTomorrowsPendingCampaign();
	}

	private void createAndPublishTomorrowsPendingCampaign() {
		/*
		 * Create next campaign opportunity (to start on day + 2)  
		 * and notify competing adNetwork agents
		 */

		pendingCampaign = new CampaignImpl(qualityManager,
				CMP_REACHS[random.nextInt(3)], day + 2, 
						day + 1 + CMP_LENGTHS[random.nextInt(3)],
				MarketSegment.randomMarketSegment(), ALOC_CMP_VC, ALOC_CMP_MC);

		log.log(Level.INFO, "Notifying new campaign opportunity..");
		getSimulation().sendCampaignOpportunity(
				new CampaignOpportunityMessage(pendingCampaign, day));
	}

	private void reportAuctionResutls(int date) {
		/*
		 * report auctions result and campaigns stats to adNet agents
		 */
		log.log(Level.INFO, "Reporting auction results...");
		log.log(Level.INFO, "Pending campaign: " + pendingCampaign);

		for (String advertiser : getAdxAdvertiserAddresses()) {
			
			/* we only report adx simulation results starting from day 2 (w.r.t. day 1) */
			if (date >=2 ) {
			   CampaignReport report = new CampaignReport();
		   	   for (Campaign campaign : adNetCampaigns.values()) {
				   if (campaign.isAllocated()
						&& (campaign.getDayStart() < date)    
						&& (advertiser.equals(campaign.getAdvertiser()))) {
					report.addStatsEntry(campaign.getId(),
							campaign.getStats(campaign.getDayStart(), date-1));
				   }
			   }
			   getSimulation().sendCampaignReport(advertiser, report);
			}
			
			
			AdNetworkDailyNotification adNetworkNotification = new AdNetworkDailyNotification(
					ucs.getAdNetData(advertiser), pendingCampaign);

			/* remove campaign cost for non-winning advertisers */
			if (!advertiser.equals(pendingCampaign.getAdvertiser())) {
				adNetworkNotification.zeroCost();
			} 
			
			getSimulation().sendUserClassificationAuctionResult(advertiser,
					adNetworkNotification);
		}
	}

	private void consolidateCmpaignStatistics(int date) {
		/* nothing to consolidate before day 2 (w.r.t. simulated day 1) */
		if (date >= 2) {
		   for (Campaign campaign : adNetCampaigns.values())
			   campaign.nextTimeUnit(date);
		}
	}

	private void auctionTomorrowsCampaign(int date) {
		/*
		 * Auction campaign and add to repository
		 * 
		 * on day 1 we auction the campaign starting on day 2 for which bids where received on day 0 
		 * on day n we auction the campaign starting on day n+1 for which bids where received on day n-1
		 * 
		 * Note: the campaign (pendingCampaign) was created on day n-1 
		 */
		log.log(Level.INFO, "new day " + date + " . Auction pending campaign");
		if (pendingCampaign != null) {
			pendingCampaign.auction();
			if (pendingCampaign.isAllocated()) {
				adNetCampaigns.put(pendingCampaign.getAdvertiser(),
						pendingCampaign);

				/* notify regarding newly allocate campaign */
				getSimulation().getEventBus().post(
						new CampaignNotification(pendingCampaign));

			}
		}

		/*
		 * auction user classification service and announce results to built-in
		 * agents
		 */
		log.log(Level.INFO, "Auction user classification service");
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Users#setup()
	 */
	@Override
	protected void setup() {
		random = new Random();

		total_population = getSimulation().getConfig().getPropertyAsInt(
				"adxusers.population_size", TOTAL_POPULATION_DEFAULT);

		CMP_REACHS = new int[3];
		CMP_REACHS[0] = total_population / 2;
		CMP_REACHS[1] = total_population;
		CMP_REACHS[2] = total_population * 3 / 2;

		aloc_cmp_reach = total_population / 8;

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
			log.log(Level.INFO, "allocating initial campaigns");
			qualityManager.addAdvertiser(advertiser);
			Campaign campaign = new CampaignImpl(qualityManager,
					aloc_cmp_reach, 1, ALOC_CMP_LENGTH,
					MarketSegment.randomMarketSegment(), ALOC_CMP_VC,
					ALOC_CMP_MC);

			campaign.allocateToAdvertiser(advertiser);
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

		log.log(Level.INFO, "Got message..");

		if (content instanceof AdNetBidMessage) {

			AdNetBidMessage cbm = (AdNetBidMessage) content;

			log.log(Level.INFO, "..AdNetBidMessage: " + cbm.toString());

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
		if (cmpn != null) {
//			log.log(Level.INFO,"IMPRESSED ("+ cmpn.getId() + "," + cmpn.getAdvertiser() + ") segments:" + message.getAuctionResult().getMarketSegments());

			boolean wasOverLimit = cmpn.isOverTodaysLimit();
						
			cmpn.impress(message.getUser(), 
					message.getQuery().getAdType(),
					message.getQuery().getDevice(),
					message.getAuctionResult().getWinningPrice()
					);

			if (wasOverLimit) {
				/* rare - should warn: not supposed to bid on over-limit campaigns */
				log.log(Level.WARNING, " Impressed while over limit: " + cmpn.getId());			
			} else if (cmpn.isOverTodaysLimit()) {
				/* notify on transition campaign limit expiration */
				getSimulation().getEventBus().post(
						new CampaignLimitReached(cmpn.getId(), cmpn.getAdvertiser()));
				log.log(Level.INFO, " Campaign limit expired Impressed while over limit: " + cmpn.getId());			
			}

		} else {
			log.log(Level.SEVERE, "IMPRESSED: Campaign Missing!!! " + message.getAuctionResult());			
		}
	}

	@Override
	public void nextTimeUnit(int timeUnit) {
		// TODO Auto-generated method stub

	}

}
