/*
 */
package tau.tac.adx.agents;

import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
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
import tau.tac.adx.report.demand.UserClassificationServiceLevelNotification;
import tau.tac.adx.sim.Builtin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.eventbus.Subscribe;


public class DemandAgent extends Builtin {

	private int day;
	
	private static final Long ALOC_CMP_REACH = 30000L;
	private static final int  ALOC_CMP_START_DAY = 1;
	private static final int ALOC_CMP_END_DAY = 5;
	
	//FEMALE_YOUNG, FEMALE_OLD, MALE_YOUNG, MALE_OLD, YOUNG_HIGH_INCOME, OLD_HIGH_INCOME, YOUNG_LOW_INCOME, OLD_LOW_INCOME, FEMALE_LOW_INCOME, FEMALE_HIGH_INCOME, MALE_HIGH_INCOME, MALE_LOW_INCOME	
	private static final MarketSegment ALOC_CMP_SGMNT = MarketSegment.FEMALE_HIGH_INCOME;
	private static final double ALOC_CMP_VC = 2.5;
	private static final double ALOC_CMP_MC = 3.5;

	
	public static final String DEMAND_AGENT_NAME = "demand";
	private Logger log;

	private QualityManager qualityManager; 
	private ListMultimap<String, Campaign> adNetCampaigns;
	private Campaign pendingCampaign;
	

	private UserClassificationService ucs;
	
	/**
	 * Default constructor.
	 */
	public DemandAgent() {
		super(DEMAND_AGENT_NAME);
	}

	/**
	 * @see se.sics.tasim.aw.TimeListener#nextTimeUnit(int)
	 */
	@Override
	public void nextTimeUnit(int date) {
		day = date;
		
		/*
		 * Auction campaign and add to repository
		 */
		log.log(Level.INFO, "new day "+ date + " . Auction pending campaign");
		if (pendingCampaign != null) {
			pendingCampaign.auction();
			if (pendingCampaign.isAllocated()) {
			  adNetCampaigns.put(pendingCampaign.getAdvertiser(), pendingCampaign);
			  
			  /* notify regarding newly allocate campaign */
			  getSimulation().getEventBus().post(new CampaignNotification(pendingCampaign));
			  
			}
		}
		
		/*
		 * auction user classification service and announce results to built-in agents
		 */
		log.log(Level.INFO, "Auction user classification service");

		ucs.auction(day);	
		getSimulation().getEventBus().post(new UserClassificationServiceNotification(ucs));

		
		
		for (Campaign campaign : adNetCampaigns.values()) 
			campaign.nextTimeUnit(date);
		/*
		 * report auctions result and campaigns stats to adNet agents
		 */

		log.log(Level.INFO, "Reporting auction results...");

		for (String advertiser : getAdvertiserAddresses()) {

		   CampaignReport report = new CampaignReport();
		   for (Campaign campaign : adNetCampaigns.values()) { 
		      if (campaign.isAllocated() && (advertiser.equals(campaign.getAdvertiser()))) {
		    	  report.addStatsEntry(campaign.getId(), campaign.getStats(1, date));
		      }
		   }
		   
		   getSimulation().sendCampaignReport(advertiser, report);		
		   	   
		   UserClassificationServiceLevelNotification ucsNotification = new UserClassificationServiceLevelNotification(ucs.getAdNetData(advertiser));
   		   getSimulation().sendUserClassificationAuctionResult(advertiser, ucsNotification);
	  	}
				
		/*
		 * Create next campaign opportunity and notify competing adNetwork agents
		 */
		pendingCampaign = new CampaignImpl(qualityManager,
				ALOC_CMP_REACH, ALOC_CMP_START_DAY, 
				ALOC_CMP_END_DAY, ALOC_CMP_SGMNT /* TODO: randomize */, ALOC_CMP_VC, ALOC_CMP_MC);

		log.log(Level.INFO, "Notifying new campaign opportunity..");
		getSimulation().sendCampaignOpportunity(new CampaignOpportunityMessage(pendingCampaign));
		
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Users#setup()
	 */
	@Override
	protected void setup() {
		this.log = Logger.getLogger(DemandAgent.class.getName());
		

		log.info("setting up...");
		
		getSimulation().getEventBus().register(this);

		adNetCampaigns = ArrayListMultimap.create();
		
		qualityManager = new QualityManagerImpl();
		
		ucs = new UserClassificationServiceImpl();
		
		/*
		 * Allocate an initial campaign to each competing adNet agent and notify
		 */
		for (String advertiser : getAdvertiserAddresses()) {
			log.log(Level.INFO, "allocating initial campaigns");
			qualityManager.addAdvertiser(advertiser);
			Campaign campaign = new CampaignImpl(qualityManager,
					ALOC_CMP_REACH, ALOC_CMP_START_DAY, 
					ALOC_CMP_END_DAY, ALOC_CMP_SGMNT /* TODO: randomize */, ALOC_CMP_VC, ALOC_CMP_MC);
			
			campaign.allocateToAdvertiser(advertiser);
			adNetCampaigns.put(advertiser, campaign);
			getSimulation().sendInitialCampaign(advertiser, new InitialCampaignMessage(campaign));
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
			if ((pendingCampaign != null)&&((pendingCampaign.getId() == cbm.getCampaignId()))) 
				pendingCampaign.addAdvertiserBid(sender, cbm.getCampaignBudget());
			/*
			 * update adNet ucs bid
			 */
			ucs.updateAdvertiserBid(sender, cbm.getUcsBid(), day);
		}
		
	}
	
	@Subscribe
	public void impressed(AuctionMessage message) {
		log.log(Level.INFO, "Impressed: " + message.toString());

		/* fetch campaign */
		//Campaign cmpn = message.getCampaign(); 
		//		
	    //cmpn.impress(message.getMarketSegment(), message.isVideo(), message.isMobile, message.getCost());
	}
	

}
