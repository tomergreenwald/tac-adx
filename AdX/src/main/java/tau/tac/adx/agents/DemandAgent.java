/*
 */
package tau.tac.adx.agents;

import static tau.tac.adx.sim.TACAdxConstants.ADVERTISER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Message;
import se.sics.tasim.props.StartInfo;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.demand.CampaignImpl;
import tau.tac.adx.demand.QualityManager;
import tau.tac.adx.demand.QualityManagerImpl;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.report.demand.CampaignBidMessage;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.CampaignReport;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.sim.Builtin;


public class DemandAgent extends Builtin {

	private static final Long ALOC_CMP_REACH = 30000L;
	private static final int  ALOC_CMP_START_DAY = 1;
	private static final int ALOC_CMP_END_DAY = 5;
	private static final int ALOC_CMP_SGMNT = 3;
	private static final double ALOC_CMP_VC = 2.5;
	private static final double ALOC_CMP_MC = 3.5;

	
	public static final String DEMAND_AGENT_NAME = "demand";
	private Logger log;

	private QualityManager qualityManager; 
	private ListMultimap<String, Campaign> adNetCampaigns;
	//private Map<String, Campaign> pendingCampaigns;
	private Campaign pendingCampaign;
	
	
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
		/*
		 * Auction campaign and add to repository
		 */
		if (pendingCampaign != null) {
			pendingCampaign.auction();
			if (pendingCampaign.isAllocated())
			  adNetCampaigns.put(pendingCampaign.getAdvertiser(), pendingCampaign);
		}
		
		for (Campaign campaign : adNetCampaigns.values()) 
			campaign.nextTimeUnit(date);
		
		
		/*
		 * report result and campaigns stats to adNet agents
		 */

		for (String advertiser : getAdvertiserAddresses()) {
		   CampaignReport report = new CampaignReport();
		   for (Campaign campaign : adNetCampaigns.values()) { 
		      if (campaign.isAllocated() && (advertiser.equals(campaign.getAdvertiser()))) {
		    	  report.addStatsEntry(campaign.getId(), campaign.getStats(1, date));
		      }
		   }
		   
		   getSimulation().sendCampaignReport(advertiser, report);		
		   
	  	}
				
		/*
		 * Create next campaign opportunity and notify 
		 */
		pendingCampaign = new CampaignImpl(qualityManager,
				ALOC_CMP_REACH, ALOC_CMP_START_DAY, 
				ALOC_CMP_END_DAY, ALOC_CMP_SGMNT /* TODO: randomize */, ALOC_CMP_VC, ALOC_CMP_MC);
		
		getSimulation().sendCampaignOpportunity(new CampaignOpportunityMessage(pendingCampaign));
		
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Users#setup()
	 */
	@Override
	protected void setup() {
		this.log = Logger.getLogger(DemandAgent.class.getName());
		
		adNetCampaigns = ArrayListMultimap.create();
		
		qualityManager = new QualityManagerImpl();
		
		/*
		 * Allocate an initial campaign to each competing adNet agent and notify
		 */
		for (String advertiser : getAdvertiserAddresses()) {
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

		if (content instanceof CampaignBidMessage) {			
			CampaignBidMessage cbm = (CampaignBidMessage) content;
			/*
			 * collect campaign bids for campaign opportunities 
			 */			
			if ((pendingCampaign != null)&&((pendingCampaign.getId() == cbm.getId()))) 
				pendingCampaign.addAdvertiserBid(sender, cbm.getBudget());
		}
		
	}

}
