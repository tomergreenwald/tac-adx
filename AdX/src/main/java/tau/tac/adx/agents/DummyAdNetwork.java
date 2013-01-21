/*
 * DummyAdvertiser.java
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
package tau.tac.adx.agents;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.Message;
import se.sics.tasim.props.SimulationStatus;
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
import edu.umich.eecs.tac.props.AdvertiserInfo;

/**
 * @author Lee Callender, Patrick Jordan
 */

public class DummyAdNetwork extends Agent {

	private final Logger log = Logger.getLogger(DummyAdNetwork.class.getName());

	private PublisherCatalog publisherCatalog;
	private String ServerAddress;
	private AdxBidBundle bidBundle;

	private AdxQuery[] queries;
	private double[] impressions;
	private double[] clicks;
	private double[] conversions;
	private double[] values;

	private Random randomGenerator;
	private InitialCampaignMessage initialCampaignMessage;
	private CampaignOpportunityMessage campaignOpportunityMessage;
	private CampaignReport campaignReport;
	private AdNetworkDailyNotification adNetworkDailyNotification;

	private CampaignData pendingCampaign;

	private Map<Integer, CampaignData> myCampaigns;
	
	private int day;

	public DummyAdNetwork() {
	}

	@Override
	protected void messageReceived(Message message) {
		try {
			Transportable content = message.getContent();
			if (content instanceof InitialCampaignMessage) {
				handleInitialCampaignMessage((InitialCampaignMessage) content);
			} else if (content instanceof CampaignOpportunityMessage) {
				handleICampaignOpportunityMessage((CampaignOpportunityMessage) content);
			} else 	if (content instanceof CampaignReport) {
				handleCampaignReport((CampaignReport) content);
			} else	if (content instanceof AdNetworkDailyNotification) {
				handleAdNetworkDailyNotification((AdNetworkDailyNotification) content);
			} else if (content instanceof AdxPublisherReport) {	
				handleAdxPublisherReport((AdxPublisherReport) content);
			} else if (content instanceof SimulationStatus) {
				handleSimulationStatus((SimulationStatus) content);				
			}	
			// handleAdNetworkReport((AdNetworkReport) content);
			// } else if (content instanceof SimulationStatus) {
			
		} catch (NullPointerException e) {
			this.log.log(Level.SEVERE, "Null Message received.");
			return;
		}
	}

	private void handleAdxPublisherReport(AdxPublisherReport content) {
		// TODO Auto-generated method stub
	}

	private void handleAdNetworkDailyNotification(
			AdNetworkDailyNotification adNetNotificationMessage) {

		adNetworkDailyNotification = adNetNotificationMessage;
		if ((pendingCampaign.id == adNetworkDailyNotification.getCampaignId()) &&
				getName().equals(adNetNotificationMessage.getWinner())) {
			/* We are the winners : ) add campaign to list */
			myCampaigns.put(pendingCampaign.id, pendingCampaign);
		}
	}

	private void handleCampaignReport(CampaignReport CampaignReport) {
		this.campaignReport = CampaignReport;
		/* ... */
	}

	
	private void updateCampaignDataOpportunity(CampaignOpportunityMessage campaignOpportunityMessage,
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

		long cmpBid = randomGenerator.nextLong()
				% campaignOpportunityMessage.getReachImps();

		AdNetBidMessage bids = new AdNetBidMessage(
				randomGenerator.nextInt(100), pendingCampaign.id, cmpBid);

		sendMessage(ServerAddress, bids);		
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
		ServerAddress = campaignMessage.getServerId();

		CampaignData campaignData = new CampaignData();
		updateCampaignData(initialCampaignMessage, campaignData);

		myCampaigns.put(initialCampaignMessage.getId(), campaignData);
	}

	private void handleSimulationStatus(SimulationStatus simulationStatus) {
		sendBidAndAds();
	}

	private void handleRetailCatalog(PublisherCatalog publisherCatalog) {
		this.publisherCatalog = publisherCatalog;
		generateAdxQuerySpace();
	}

	private void handleAdvertiserInfo(AdvertiserInfo advertiserInfo) {
		ServerAddress = advertiserInfo.getPublisherId();

	}

	// private void handleAdNetworkReport(AdNetworkReport queryReport) {
	// for (int i = 0; i < queries.length; i++) {
	// AdxQuery query = queries[i];
	// queryReport.g
	// int index = queryReport.indexForEntry(query);
	// if (index >= 0) {
	// impressions[i] += queryReport.getImpressions(index);
	// clicks[i] += queryReport.getClicks(index);
	// }
	// }
	// }

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
		log.fine("dummy " + getName() + " simulationSetup");
	}

	@Override
	protected void simulationFinished() {
		bidBundle = null;
	}

	protected void sendBidAndAds() {

		bidBundle = new AdxBidBundle();
		
		for (CampaignData campaign : myCampaigns.values()) {
			if (((day+1) >= campaign.dayStart) && (day < campaign.dayEnd)) {
			   /* add entry w.r.t. this campaign */	
			}
		}

		
		
		
		
		/* TODO: Remove legacy code below*/
		
		Ad ad = new Ad(null);

		for (int i = 0; i < queries.length; i++) {
			bidBundle.addQuery(queries[i], values[i] / clicks[i], ad);
			// bidBundle.addAdxQuery(queries[i], 100, ad);
		}

		if (bidBundle != null && ServerAddress != null) {
			sendMessage(ServerAddress, bidBundle);
		}
	}

	private void generateAdxQuerySpace() {
		if (publisherCatalog != null && queries == null) {
			Set<AdxQuery> queryList = new HashSet<AdxQuery>();
			Random random = new Random();
			for (PublisherCatalogEntry publisherCatalogEntry : publisherCatalog) {
				// Create f0
				AdxQuery f0 = new AdxQuery();

				// Create f1's
				AdxQuery f1_manufacturer = new AdxQuery(
						publisherCatalogEntry.getPublisherName(),
						Collections.singletonList(MarketSegment.values()[random
								.nextInt(MarketSegment.values().length)]),
						null, null);
				AdxQuery f1_component = new AdxQuery(
						publisherCatalogEntry.getPublisherName(),
						Collections.singletonList(MarketSegment.values()[random
								.nextInt(MarketSegment.values().length)]),
						Device.values()[random.nextInt(Device.values().length)],
						AdType.values()[random.nextInt(AdType.values().length)]);

				// Create f2
				AdxQuery f2 = new AdxQuery(
						publisherCatalogEntry.getPublisherName(),
						Collections.singletonList(MarketSegment.values()[random
								.nextInt(MarketSegment.values().length)]),
						Device.values()[random.nextInt(Device.values().length)],
						AdType.values()[random.nextInt(AdType.values().length)]);

				queryList.add(f0);
				queryList.add(f1_manufacturer);
				queryList.add(f1_component);
				queryList.add(f2);
			}

			queries = queryList.toArray(new AdxQuery[0]);
			impressions = new double[queries.length];
			clicks = new double[queries.length];
			conversions = new double[queries.length];
			values = new double[queries.length];

			for (int i = 0; i < queries.length; i++) {
				impressions[i] = 100;
				clicks[i] = 9;
				conversions[i] = 1;
			}
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
