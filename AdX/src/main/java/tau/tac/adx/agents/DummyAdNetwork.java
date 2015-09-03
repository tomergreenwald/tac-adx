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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import tau.tac.adx.report.publisher.AdxPublisherReport;
import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.props.Ad;

/**
 * @author Mariano Schain
 */

public class DummyAdNetwork extends Agent {

	private final Logger log = Logger.getLogger(DummyAdNetwork.class.getName());

	private PublisherCatalog publisherCatalog;
	private String ServerAddress;
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

	public DummyAdNetwork() {
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
			}

		} catch (NullPointerException e) {
			this.log.log(Level.SEVERE, "Excdeption thrown. ", e);
			return;
		}
	}

	private void handleAdxPublisherReport(AdxPublisherReport content) {
		// TODO Auto-generated method stub
	}

	private void handleAdNetworkDailyNotification(
			AdNetworkDailyNotification adNetNotificationMessage) {
		// log.log(Level.INFO, getName() + " Campaign result: "
		// + adNetNotificationMessage);
		adNetworkDailyNotification = adNetNotificationMessage;
		if ((pendingCampaign.id == adNetworkDailyNotification.getCampaignId())
				&& getName().equals(adNetNotificationMessage.getWinner())) {
			/* We are the winners : ) add campaign to list */
			myCampaigns.put(pendingCampaign.id, pendingCampaign);
		}
	}

	private void handleCampaignReport(CampaignReport campaignReport) {
		this.campaignReport = campaignReport;

		/*
		 * for each campaign, the accumulated statistics from day 1 up to day
		 * n-1 are reported
		 */
		for (CampaignReportKey campaignKey : campaignReport.keys()) {
			int cmpId = campaignKey.getCampaignId();
			CampaignStats cstats = campaignReport.getCampaignReportEntry(
					campaignKey).getCampaignStats();
			// myCampaigns.get(cmpId).setStats(cstats);

			/*
			 * log.fine("Day " + day + ": Updating campaign " + cmpId
			 * +" stats: " + cstats.getTargetedImps() + " tgtImps " +
			 * cstats.getOtherImps() + " nonTgtImps. Cost of imps is " +
			 * cstats.getCost() );
			 */
		}

		// log.log(Level.INFO, getName() + campaignReport.toMyString());
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

		AdNetBidMessage bids = new AdNetBidMessage(randomGenerator.nextInt(10),
				pendingCampaign.id, cmpBid);
		// log.fine("sent campaign bid");
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
		// log.info(campaignMessage.toString());

		day = 0;

		initialCampaignMessage = campaignMessage;
		ServerAddress = campaignMessage.getDemandAgentAddress();

		CampaignData campaignData = new CampaignData();
		updateCampaignData(initialCampaignMessage, campaignData);

		myCampaigns.put(initialCampaignMessage.getId(), campaignData);
	}

	private void handleSimulationStatus(SimulationStatus simulationStatus) {
		sendBidAndAds();
	}

	private void handlePublisherCatalog(PublisherCatalog publisherCatalog) {
		this.publisherCatalog = publisherCatalog;
		generateAdxQuerySpace();
	}

	private void handleAdNetworkReport(AdNetworkReport queryReport) {
		// this.log.log(Level.INFO, queryReport.toString());

	}

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
					// #FIXME
					// if (campaign.targetSegment == segmentsList.iterator()
					// .next())
					bidBundle.addQuery(queries[i],
							(1 + rnd.nextLong() % 1000) / 1000.0, new Ad(null),
							campaign.id, 1);
				}

			}
		}

		if (bidBundle != null) {
			sendToRole(TACAdxConstants.ADX_AGENT_ROLE_ID, bidBundle);
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
		void setStats(CampaignStats s) {
			stats.setValues(s);
		}

		Long reachImps;
		long dayStart;
		long dayEnd;
		Set<MarketSegment> targetSegment;
		double videoCoef;
		double mobileCoef;
		int id;

		/* campaign info as reported */
		CampaignStats stats;
	}

}
