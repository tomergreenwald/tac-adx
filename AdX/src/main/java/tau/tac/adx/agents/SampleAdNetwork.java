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

import edu.umich.eecs.tac.props.Ad;
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
import tau.tac.adx.sim.TACAdxConstants;

public class SampleAdNetwork extends Agent {

	private final Logger log = Logger.getLogger(SampleAdNetwork.class.getName());

	
    /**
     * Basic simulation information. {@link StartInfo} contains
     * <ul>
     * <li>simulation ID</li>
     * <li>simulation start time</li>
     * <li>simulation length in simulation days</li>
     * <li>actual seconds per simulation day</li>
     * </ul>
     * An agent should receive the {@link StartInfo} at the beginning of the game or during recovery.
     */
    private StartInfo startInfo;
	
    
    /**
     * The list contains all of the {@link CampaignReport campaign reports} delivered to the agent.  Each
     * {@link CampaignReport campaign report} contains...
     */
    protected Queue<CampaignReport> campaignReports;

    protected Queue<AdxPublisherReport> adxPublisherReports;
    
	private PublisherCatalog publisherCatalog;
	private String demandAgentAddress;
	private String adxAgentAddress;
	private AdxBidBundle bidBundle;

	/*
	 * we maintain a list of queries - each characterized by the web site (the
	 * publisher), the device type, the ad type, and the user market segment
	 */
	private AdxQuery[] queries;

	private Random randomGenerator;
	private InitialCampaignMessage initialCampaignMessage;
	private CampaignOpportunityMessage campaignOpportunityMessage;
	private CampaignReport campaignReport;
	private AdNetworkDailyNotification adNetworkDailyNotification;

	private CampaignData pendingCampaign;

	private Map<Integer, CampaignData> myCampaigns;

	private int day;



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

	private void handleAdxPublisherReport(AdxPublisherReport adxPublisherReport) {
		adxPublisherReports.add(adxPublisherReport);
	}

	private void handleAdNetworkDailyNotification(
			AdNetworkDailyNotification adNetNotificationMessage) {
		log.log(Level.INFO, getName() + " Campaign result: "
				+ adNetNotificationMessage);
		adNetworkDailyNotification = adNetNotificationMessage;
		if ((pendingCampaign.id == adNetworkDailyNotification.getCampaignId())
				&& getName().equals(adNetNotificationMessage.getWinner())) {
			/* We are the winners : ) add campaign to list */
			myCampaigns.put(pendingCampaign.id, pendingCampaign);
		}
	}

	private void handleCampaignReport(CampaignReport campaignReport) {
		this.campaignReport = campaignReport;
		campaignReports.add(campaignReport);
		/* ... */
		log.log(Level.INFO, getName() + campaignReport.toMyString());
	}

	private void updateCampaignDataOpportunity(
			CampaignOpportunityMessage campaignOpportunityMessage,
			CampaignData campaignData) {
		campaignData.dayStart = campaignOpportunityMessage.getDayStart();
		campaignData.dayEnd = campaignOpportunityMessage.getDayEnd();
		campaignData.id = campaignOpportunityMessage.getId();
		campaignData.reachImps = campaignOpportunityMessage.getReachImps();
		campaignData.targetSegment = campaignOpportunityMessage
				.getTargetSegment();
		campaignData.mobileCoef = campaignOpportunityMessage.getMobileCoef();
		campaignData.videoCoef = campaignOpportunityMessage.getVideoCoef();
	}

	private void handleICampaignOpportunityMessage(
			CampaignOpportunityMessage com) {

		day = com.getDay();

		campaignOpportunityMessage = com;
		pendingCampaign = new CampaignData();

		updateCampaignDataOpportunity(com, pendingCampaign);

		long cmpBid = Math.abs(randomGenerator.nextLong())
				% campaignOpportunityMessage.getReachImps();

		AdNetBidMessage bids = new AdNetBidMessage(
				randomGenerator.nextInt(100), pendingCampaign.id, cmpBid);
		log.fine("sent campaign bid");
		sendMessage(demandAgentAddress, bids);
	}

	private void updateCampaignData(InitialCampaignMessage campaignMessage,
			CampaignData campaignData) {
		campaignData.reachImps = campaignMessage.getReachImps();
		campaignData.dayStart = campaignMessage.getDayStart();
		campaignData.dayEnd = campaignMessage.getDayEnd();
		campaignData.targetSegment = campaignMessage.getTargetSegment();
		campaignData.videoCoef = campaignMessage.getVideoCoef();
		campaignData.mobileCoef = campaignMessage.getMobileCoef();
		campaignData.id = campaignMessage.getId();
	}

	private void handleInitialCampaignMessage(
			InitialCampaignMessage campaignMessage) {
		log.info(campaignMessage.toString());

		day = 0;

		initialCampaignMessage = campaignMessage;
		demandAgentAddress = campaignMessage.getDemandAgentAddress();
		adxAgentAddress = campaignMessage.getAdxAgentAddress();

		CampaignData campaignData = new CampaignData();
		updateCampaignData(initialCampaignMessage, campaignData);

		myCampaigns.put(initialCampaignMessage.getId(), campaignData);
	}

	private void handleSimulationStatus(SimulationStatus simulationStatus) {
		sendBidAndAds();
	}

	private void handlePublisherCatalog(PublisherCatalog publisherCatalog) {
		this.publisherCatalog = publisherCatalog;
//        demandAgentAddress = publisherCatalog.getDemandAgentId();
//        adxAddress = publisherCatalog.getAdxAgentId();
		generateAdxQuerySpace();
	}

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

		myCampaigns = new HashMap<Integer, CampaignData>();

		randomGenerator = new Random();

		// Add the advertiser name to the logger name for convenient
		// logging. Note: this is usually a bad idea because the logger
		// objects will never be garbaged but since the dummy names always
		// are the same in TAC AA games, only a few logger objects
		// will be created.
		// this.log = Logger.getLogger(DummyAdNetwork.class.getName() + '.'
		// + getName());
		log.fine("AdNet " + getName() + " simulationSetup");
	}

	@Override
	protected void simulationFinished() {
		campaignReports.clear();
		adxPublisherReports.clear();
		bidBundle = null;
	}

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

	private void generateAdxQuerySpace() {
		if (publisherCatalog != null && queries == null) {
			Set<AdxQuery> queryList = new HashSet<AdxQuery>();

			/*
			 * for each web site (publisher) we generate all possible variations
			 * of device type, ad type, and user market segment segment
			 */
			for (PublisherCatalogEntry publisherCatalogEntry : publisherCatalog) {
				for (MarketSegment userSegment : MarketSegment.values()) {
					Set<MarketSegment> marketSegments = new HashSet<MarketSegment>();
					marketSegments.add(userSegment);

					queryList.add(new AdxQuery(publisherCatalogEntry
							.getPublisherName(), marketSegments, Device.mobile,
							AdType.text));

					queryList.add(new AdxQuery(publisherCatalogEntry
							.getPublisherName(), marketSegments, Device.pc,
							AdType.text));

					queryList.add(new AdxQuery(publisherCatalogEntry
							.getPublisherName(), marketSegments, Device.mobile,
							AdType.video));

					queryList.add(new AdxQuery(publisherCatalogEntry
							.getPublisherName(), marketSegments, Device.pc,
							AdType.video));

				}

			}
			queries = new AdxQuery[queryList.size()];
			queryList.toArray(queries);
		}
	}

	private class CampaignData {
		Long reachImps;
		long dayStart;
		long dayEnd;
		MarketSegment targetSegment;
		double videoCoef;
		double mobileCoef;
		int id;
	}

}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	