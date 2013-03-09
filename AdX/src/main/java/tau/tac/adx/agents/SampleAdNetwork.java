package tau.tac.adx.agents;

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
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.report.publisher.AdxPublisherReport;
import edu.umich.eecs.tac.props.Ad;

public class SampleAdNetwork extends Agent {

	private final Logger log = Logger.getLogger(SampleAdNetwork.class.getName());

    /*
     * Basic simulation information.
     * An agent should receive the {@link StartInfo} at the beginning of the game or during recovery.
     */
    private StartInfo startInfo;
	  
    /** 
     * Messages received: 
     * 
     * We keep all the {@link CampaignReport campaign reports} and 
     * {@link AdxPublisherReport publisher reports} delivered to the agent.
     * We also keep the initialization messages 
     * {@link PublisherCatalog} and {@link InitialCampaignMessage}  
     * and the most recent messages and reports 
     * {@link CampaignOpportunityMessage}, {@link CampaignReport}, 
     * and {@link AdNetworkDailyNotification}. 
    */
    private Queue<CampaignReport> campaignReports;
    private Queue<AdxPublisherReport> adxPublisherReports;
	private PublisherCatalog publisherCatalog;
	private InitialCampaignMessage initialCampaignMessage;
	private CampaignOpportunityMessage campaignOpportunityMessage;
	private CampaignReport campaignReport;
	private AdNetworkDailyNotification adNetworkDailyNotification;
	
	/*
	 * The addresses of server entities to which the agent should send the daily bids data  
	 */
	private String demandAgentAddress;
	private String adxAgentAddress;
	
	/*
	 * we maintain a list of queries - each characterized by the web site (the
	 * publisher), the device type, the ad type, and the user market segment
	 */
	private AdxQuery[] queries;

	/**
	 * Information regarding the latest campaign opportunity announced
	 */
	private CampaignData pendingCampaign;

	/**
	 * We maintain a collection (mapped by the campaign id)
	 * of the campaigns won by our agent. 
	 */
	private Map<Integer, CampaignData> myCampaigns;

	/*
	 * the bidBundle to be sent daily to the AdX
	 */
	private AdxBidBundle bidBundle;
	
	/*
	 * The current bid level for the user classification service
	 */
	double ucsBid;
	
	/*
	 * The targeted service level for the user classification service
	 */
	double ucsTargetLevel;

	
	/*
	 * current day of simulation 
	 */
	private int day;

	private Random randomGenerator;


	public SampleAdNetwork() {
		campaignReports = new LinkedList<CampaignReport>();
	}

	@Override
	protected void messageReceived(Message message) {
		try {
			Transportable content = message.getContent();
			log.fine(message.getContent().getClass().toString());
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
	        }

		} catch (NullPointerException e) {
			this.log.log(Level.SEVERE,
					"Null Message received. " + e.getStackTrace());
			return;
		}
	}

    /**
     * Processes the start information.
     * @param startInfo the start information.
     */
    protected void handleStartInfo(StartInfo startInfo) {
        this.startInfo = startInfo;
    }


    /**
     * Process the reported set of publishers
     * @param publisherCatalog
     */
	private void handlePublisherCatalog(PublisherCatalog publisherCatalog) {
		this.publisherCatalog = publisherCatalog;
		generateAdxQuerySpace();
	}
    

	/**
	 * On day 0, a campaign (the "initial campaign") is allocated to each competing agent. 
	 * The campaign starts on day 1. 
	 * The address of the server's AdxAgent (to which bid bundles are sent) 
	 * and DemandAgent (to which bids regarding campaign opportunities may be sent in subsequent days)
	 * are also reported in the initial campaign message 
	 */
	private void handleInitialCampaignMessage(
			InitialCampaignMessage campaignMessage) {
		log.info(campaignMessage.toString());

		day = 0;

		initialCampaignMessage = campaignMessage;
		demandAgentAddress = campaignMessage.getDemandAgentAddress();
		adxAgentAddress = campaignMessage.getAdxAgentAddress();

		CampaignData campaignData = new CampaignData(initialCampaignMessage);

		/*
		 * The initial campaign is already allocated to our agent so we add it to our 
		 * allocated-campaigns list.
		 */
		log.fine("Allocated campaign: " + campaignData);
		myCampaigns.put(initialCampaignMessage.getId(), campaignData);
	}


	/**
	 * On day n (!=0) a campaign opportunity is announced to the competing agents.
	 * The campaign starts on day n + 2 or later and the agents may send (on day n) 
	 * related bids (attempting to win the campaign). The allocation (the winner) is
	 *  announced to the competing agents during day n + 1. 
	 */
	private void handleICampaignOpportunityMessage(
			CampaignOpportunityMessage com) {

		day = com.getDay();

		pendingCampaign = new CampaignData(com);
		log.fine("campaign opportunity: " + pendingCampaign);

		/*
		 * The campaign requires com.getReachImps() impressions.
		 * The competing Ad Networks bid for the total campaign Budget 
		 * (that is, the ad network that offers the lowest budget gets
		 *  the campaign allocated).
		 * The advertiser is willing to pay the AdNetwork at most 1$ CPM,
		 * therefore the total number of impressions may be treated as 
		 * a reserve (upper bound) price for the auction.  
		 */
		long cmpBid = Math.abs(randomGenerator.nextLong())
				% (com.getReachImps()/1000);
		
		/*
		 * Adjust ucs bid s.t. target level is achieved. 
		 */
		ucsBid = ucsBid*(1 + ucsTargetLevel - adNetworkDailyNotification.getServiceLevel());
		/*
		 * the bid for the user classification service is piggybacked  
		 */
		AdNetBidMessage bids = new AdNetBidMessage(ucsBid, pendingCampaign.id, cmpBid);
		
		log.fine("sending ucs bid: "+ ucsBid + " campaign total budget bid: " + cmpBid);
		sendMessage(demandAgentAddress, bids);
	}

	/**
	 * On day n (!=0), the result of the UserClassificationService and Campaign auctions
	 * (for which the competing agents sent bids during day n -1) are reported.
	 * The reported Campaign starts in day n+1 or later and the user classification service 
	 * level is applicable starting from day n+1. 
	 */
	private void handleAdNetworkDailyNotification(
			AdNetworkDailyNotification notificationMessage) {
				
		log.log(Level.INFO, getName() + " UCS Level set to " 
			    + notificationMessage.getServiceLevel()+ " at price "
				+ notificationMessage.getPrice());				
				
		adNetworkDailyNotification = notificationMessage;

		String campaignAllocatedTo = " allocated to " + notificationMessage.getWinner();

		if ((pendingCampaign.id == adNetworkDailyNotification.getCampaignId())
				&& getName().equals(notificationMessage.getWinner())) {
			
			/* add campaign to list of won campaigns*/
			myCampaigns.put(pendingCampaign.id, pendingCampaign);
			campaignAllocatedTo = " WON !!! ";
		}
		
		log.log(Level.INFO, getName() + " Campaign " 
			    + notificationMessage.getCampaignId() + campaignAllocatedTo
				+ " at budget " 
				+ notificationMessage.getCost());

	}
    
	/**
	 * The SimulationStatus message received on day n indicates that the calculation time
	 *  is up and the agent is requested to send its bid bundle to the AdX.  
	 */
	private void handleSimulationStatus(SimulationStatus simulationStatus) {
		sendBidAndAds();
	}

	/**
	 * 
	 */
	protected void sendBidAndAds() {

		bidBundle = new AdxBidBundle();

		/*
		 * create a uniform probability over active campaigns
		 */
		for (CampaignData campaign : myCampaigns.values()) {
			int dayBiddingFor = day + 1;
			if ((dayBiddingFor >= campaign.dayStart)
					&& (dayBiddingFor <= campaign.dayEnd)) {
				/* add entry w.r.t. this campaign */

				/*
				 * for each matching publisher and opportunity context (segment,
				 * device, ad type) combination: (TODO: add a probability vector
				 * over the active campaigns)
				 */

				Random rnd = new Random();

				for (int i = 0; i < queries.length; i++) {
					Set<MarketSegment> segmentsList = queries[i]
							.getMarketSegments();
					if (campaign.targetSegment == segmentsList.iterator()
							.next())
						bidBundle.addQuery(queries[i], rnd.nextLong() % 1000,
								new Ad(null), campaign.id, 1);
				}

			}
		}

		if (bidBundle != null) {
			sendMessage(adxAgentAddress, bidBundle);
		}
	}
		

	/**
	 * Campaigns performance regarding each allocated campaign 
	 */
	private void handleCampaignReport(CampaignReport campaignReport) {
		this.campaignReport = campaignReport;
		campaignReports.add(campaignReport);
		log.log(Level.INFO, getName() + "Campaign Report: "+ campaignReport.toMyString());
	}

	/**
	 * Users and Publishers statistics 
	 */
	private void handleAdxPublisherReport(AdxPublisherReport adxPublisherReport) {
		adxPublisherReports.add(adxPublisherReport);
	}
	
	/**
	 * 
	 * @param queryReport
	 */
	private void handleAdNetworkReport(AdNetworkReport queryReport) {
		this.log.log(Level.INFO, queryReport.toString());

		/*
		 * AdNetworkReportEntry entry =
		 * queryReport.getAdNetworkReportEntry(getName());
		 * 
		 * for (int i = 0; i < queries.length; i++) { AdxQuery query =
		 * queries[i]; queryReport. int index =
		 * queryReport.indexForEntry(query); if (index >= 0) { impressions[i] +=
		 * queryReport.getImpressions(index); clicks[i] +=
		 * queryReport.getClicks(index); } }
		 */

	}

	// private void handleSalesReport(SalesReport salesReport) {
	// for (int i = 0; i < queries.length; i++) {
	// AdxQuery query = queries[i];
	//
	// int index = salesReport.indexForEntry(query);
	// if (index >= 0) {
	// conversions[i] += salesReport.getConversions(index);
	// values[i] += salesReport.getRevenue(index);
	// }
	// }
	// }

	@Override
	protected void simulationSetup() {
		day = 0;
		bidBundle = new AdxBidBundle();
		ucsTargetLevel = (randomGenerator.nextInt(10)+1)/10.0;
		ucsBid= randomGenerator.nextInt(100);
		myCampaigns = new HashMap<Integer, CampaignData>();
		randomGenerator = new Random();
		log.fine("AdNet " + getName() + " simulationSetup");
	}

	@Override
	protected void simulationFinished() {
		campaignReports.clear();
		adxPublisherReports.clear();
		bidBundle = null;
	}


	/**
	 * A user visit to a publisher's web-site results in an impression opportunity (a query) 
	 * that is characterized by the the publisher, the market segment the user may belongs to, 
	 * the device used (mobile or desktop) and the ad type (text or video).
	 *     
	 * An array of all possible queries is generated here, based on the publisher names
	 * reported at game initialization in the publishers catalog message   
	 */
	private void generateAdxQuerySpace() {
		if (publisherCatalog != null && queries == null) {
			Set<AdxQuery> querySet = new HashSet<AdxQuery>();

			/*
			 * for each web site (publisher) we generate all possible variations
			 * of device type, ad type, and user market segment
			 */
			for (PublisherCatalogEntry publisherCatalogEntry : publisherCatalog) {
				for (MarketSegment userSegment : MarketSegment.values()) {
					Set<MarketSegment> singleMarketSegment = new HashSet<MarketSegment>();
					singleMarketSegment.add(userSegment);
					String publishersName = publisherCatalogEntry.getPublisherName();
					
					querySet.add(new AdxQuery(publishersName, singleMarketSegment, Device.mobile,
							AdType.text));

					querySet.add(new AdxQuery(publishersName, singleMarketSegment, Device.pc,
							AdType.text));

					querySet.add(new AdxQuery(publishersName, singleMarketSegment, Device.mobile,
							AdType.video));

					querySet.add(new AdxQuery(publishersName, singleMarketSegment, Device.pc,
							AdType.video));

				}

			}
			queries = new AdxQuery[querySet.size()];
			querySet.toArray(queries);
		}
	}

	private class CampaignData {
		public CampaignData(InitialCampaignMessage icm) {
			reachImps = icm.getReachImps();
			dayStart = icm.getDayStart();
			dayEnd = icm.getDayEnd();
			targetSegment = icm.getTargetSegment();
			videoCoef = icm.getVideoCoef();
			mobileCoef = icm.getMobileCoef();
			id = icm.getId();
		}
		public CampaignData(CampaignOpportunityMessage com) {
			dayStart = com.getDayStart();
			dayEnd = com.getDayEnd();
			id = com.getId();
			reachImps = com.getReachImps();
			targetSegment = com.getTargetSegment();
			mobileCoef = com.getMobileCoef();
			videoCoef = com.getVideoCoef();
		}
		
		@Override
		public String toString() {
		   return "Campaign ID "+ id + ": "+ "day " + dayStart + " to "+ dayEnd + " " + targetSegment.name() + ", reach: " +  reachImps + 
				   " coefs: (v=" + videoCoef + ", m=" +  mobileCoef + ")";	
		}
		
		Long reachImps;
		long dayStart;
		long dayEnd;
		MarketSegment targetSegment;
		double videoCoef;
		double mobileCoef;
		int id;
	}

}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	