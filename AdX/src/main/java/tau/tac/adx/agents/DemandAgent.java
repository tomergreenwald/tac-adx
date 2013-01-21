/*
 */
package tau.tac.adx.agents;

import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.aw.Message;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.demand.CampaignImpl;
import tau.tac.adx.demand.QualityManager;
import tau.tac.adx.demand.QualityManagerImpl;
import tau.tac.adx.demand.UserClassificationService;
import tau.tac.adx.demand.UserClassificationServiceImpl;
import tau.tac.adx.messages.AuctionMessage;
import tau.tac.adx.messages.CampaignNotification;
import tau.tac.adx.messages.UserClassificationServiceNotification;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.AdNetBidMessage;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.CampaignReport;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.sim.Builtin;
import tau.tac.adx.sim.TACAdxConstants;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.eventbus.Subscribe;

public class DemandAgent extends Builtin {

	private int day;

	private static final int ALOC_CMP_REACH = 10000;
	private static int aloc_cmp_reach;
	
	private static final int ALOC_CMP_START_DAY = 1;
	private static final int ALOC_CMP_END_DAY = 5;

	// FEMALE_YOUNG, FEMALE_OLD, MALE_YOUNG, MALE_OLD, YOUNG_HIGH_INCOME,
	// OLD_HIGH_INCOME, YOUNG_LOW_INCOME, OLD_LOW_INCOME, FEMALE_LOW_INCOME,
	// FEMALE_HIGH_INCOME, MALE_HIGH_INCOME, MALE_LOW_INCOME
	private static final MarketSegment ALOC_CMP_SGMNT = MarketSegment.FEMALE_HIGH_INCOME;
	private static final double ALOC_CMP_VC = 2.5;
	private static final double ALOC_CMP_MC = 3.5;

	private Logger log;

	private QualityManager qualityManager;
	private ListMultimap<String, Campaign> adNetCampaigns;
	private Campaign pendingCampaign;
	private Campaign tommorrowsPendingCampaign;

	private UserClassificationService ucs;

	/**
	 * Default constructor.
	 */
	public DemandAgent() {
		super(TACAdxConstants.DEMAND_AGENT_NAME);
	}

	public void preNextTimeUnit(int date) {
		day = date;
		if (date == 0) {
			zeroDayInitialization();
		} else {
			pendingCampaign = tommorrowsPendingCampaign;
			auctionTomorrowsCampaign(date);
			ucs.auction(day);
			getSimulation().getEventBus().post(
					new UserClassificationServiceNotification(ucs));

			consolidateCmpaignStatistics(date);
			reportAuctionResutls(date);

		}
		createAndPublishTomorrowsPendingCampaign();
	}

	private void createAndPublishTomorrowsPendingCampaign() {
		/*
		 * Create next campaign opportunity and notify competing adNetwork
		 * agents
		 */
		tommorrowsPendingCampaign = new CampaignImpl(qualityManager,
				aloc_cmp_reach, ALOC_CMP_START_DAY, ALOC_CMP_END_DAY,
				ALOC_CMP_SGMNT /*
				 * TODO: randomize
				 */, ALOC_CMP_VC, ALOC_CMP_MC);
		
		log.log(Level.INFO, "Notifying new campaign opportunity..");
		getSimulation().sendCampaignOpportunity(
				new CampaignOpportunityMessage(tommorrowsPendingCampaign, day));
	}

	private void reportAuctionResutls(int date) {
		/*
		 * report auctions result and campaigns stats to adNet agents
		 */

		log.log(Level.INFO, "Reporting auction results...");

		for (String advertiser : getAdxAdvertiserAddresses()) {

			CampaignReport report = new CampaignReport();
			for (Campaign campaign : adNetCampaigns.values()) {
				if (campaign.isAllocated()
						&& (advertiser.equals(campaign.getAdvertiser()))) {
					report.addStatsEntry(campaign.getId(),
							campaign.getStats(1, date));
				}
			}

			getSimulation().sendCampaignReport(advertiser, report);

			AdNetworkDailyNotification adNetworkNotification = new AdNetworkDailyNotification(
					ucs.getAdNetData(advertiser), pendingCampaign);
			
			/* TODO: erase campaign winner and cost for non-winning advertisers */
			getSimulation().sendUserClassificationAuctionResult(advertiser,
					adNetworkNotification);
		}
	}

	private void consolidateCmpaignStatistics(int date) {
		for (Campaign campaign : adNetCampaigns.values())
			campaign.nextTimeUnit(date);
	}

	private void auctionTomorrowsCampaign(int date) {
		/*
		 * Auction campaign and add to repository
		 */
		log.log(Level.INFO, "new day " + date
				+ " . Auction pending campaign");
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
		 * auction user classification service and announce results to
		 * built-in agents
		 */
		log.log(Level.INFO, "Auction user classification service");
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Users#setup()
	 */
	@Override
	protected void setup() {
		//ConfigManager config = getSimulation().getConfig();
		aloc_cmp_reach = getSimulation().getConfig().getPropertyAsInt("adxusers.population_size", ALOC_CMP_REACH);
		
		this.log = Logger.getLogger(DemandAgent.class.getName());

		log.info("setting up...");

		getSimulation().getEventBus().register(this);

		adNetCampaigns = ArrayListMultimap.create();

		qualityManager = new QualityManagerImpl();

		ucs = new UserClassificationServiceImpl();

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
					aloc_cmp_reach, ALOC_CMP_START_DAY, ALOC_CMP_END_DAY,
					ALOC_CMP_SGMNT /* TODO: randomize */, ALOC_CMP_VC,
					ALOC_CMP_MC);

			campaign.allocateToAdvertiser(advertiser);
			adNetCampaigns.put(advertiser, campaign);
			
			getSimulation().sendInitialCampaign(advertiser,
					new InitialCampaignMessage(campaign, this.getAddress()));
			
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
		log.log(Level.FINEST, "Impressed: " + message.toString());

		/* fetch campaign */
		Campaign cmpn = message.getAuctionResult().getCampaign();
		if (cmpn != null) {
			cmpn.impress(
					message.getAuctionResult().getMarketSegment(),
					message.getQuery().getAdType(),
					message.getQuery().getDevice(),
					(long) (message.getAuctionResult().getWinningPrice() * 1000));
		}
	}

	@Override
	public void nextTimeUnit(int timeUnit) {
		// TODO Auto-generated method stub

	}

}
