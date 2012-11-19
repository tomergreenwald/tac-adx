/*
 * DefaultPublisherBehavior.java
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
package edu.umich.eecs.tac.auction;

import edu.umich.eecs.tac.props.*;
import edu.umich.eecs.tac.util.config.ConfigProxy;
import edu.umich.eecs.tac.sim.Users;
import edu.umich.eecs.tac.sim.AgentRepository;
import edu.umich.eecs.tac.sim.QueryReportSender;
import edu.umich.eecs.tac.user.UserEventListener;

import java.util.Set;
import java.util.Random;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;

import se.sics.tasim.aw.Message;
import se.sics.tasim.sim.SimulationAgent;
import se.sics.isl.transport.Transportable;

/**
 * @author Patrick Jordan
 */
public class DefaultPublisherBehavior implements PublisherBehavior {
	private Logger log;

	private AuctionFactory auctionFactory;

	private RetailCatalog retailCatalog;

	private UserClickModel userClickModel;

	private QueryReportManager queryReportManager;

	/**
	 * Query space defines the set of allowable queries
	 */
	private Set<Query> querySpace;

	/**
	 * The bid tracker tracks the current bids for each agent
	 */
	private BidTracker bidTracker;

	/**
	 * The spend tracker tracks the current spend for each agent
	 */
	private SpendTracker spendTracker;

	/**
	 * The bid manager tracks the bid related information for the publisher
	 */
	private BidManager bidManager;

	/**
	 * Configuration proxy for this publisher
	 */
	private ConfigProxy publisherConfigProxy;

	/**
	 * The basic slot information
	 */
	private SlotInfo slotInfo;

	/**
	 * The basic reserve information
	 */
	private ReserveInfo reserveInfo;

	/**
	 * The basic publisher info
	 */
	private PublisherInfo publisherInfo;

	private Random random;

	private ConfigProxy config;

	private AgentRepository agentRepository;

	private QueryReportSender queryReportSender;

	private ClickCharger clickCharger;

	private BidBundleWriter bidBundleWriter;

	public DefaultPublisherBehavior(ConfigProxy config,
			AgentRepository agentRepository,
			QueryReportSender queryReportSender, ClickCharger clickCharger,
			BidBundleWriter bidBundleWriter) {

		if (config == null) {
			throw new NullPointerException("config cannot be null");
		}

		this.config = config;

		if (agentRepository == null) {
			throw new NullPointerException("agent repository cannot be null");
		}

		this.agentRepository = agentRepository;

		if (queryReportSender == null) {
			throw new NullPointerException("query report sender cannot be null");
		}

		this.queryReportSender = queryReportSender;

		if (clickCharger == null) {
			throw new NullPointerException("click charger cannot be null");
		}

		this.clickCharger = clickCharger;

		if (bidBundleWriter == null) {
			throw new NullPointerException("bid bundle writer cannot be null");
		}

		this.bidBundleWriter = bidBundleWriter;
	}

	public void nextTimeUnit(int date) {
		spendTracker.reset();

		// Auctions should be updated here.
		if (bidManager != null) {
			bidManager.nextTimeUnit(date);
		}
	}

	public void setup() {
		this.log = Logger.getLogger(DefaultPublisherBehavior.class.getName());

		random = new Random();

		spendTracker = createSpendTracker();

		bidTracker = createBidTracker();

		setPublisherInfo(createPublisherInfo());

		auctionFactory = createAuctionFactory();
		auctionFactory.setPublisherInfo(getPublisherInfo());

		queryReportManager = createQueryReportManager();

		for (SimulationAgent agent : agentRepository.getUsers()) {
			Users users = (Users) agent.getAgent();
			users.addUserEventListener(new ClickMonitor());
		}
	}

	private PublisherInfo createPublisherInfo() {
		double squashingMin = config.getPropertyAsDouble("squashing.min", 0.0);
		double squashingMax = config.getPropertyAsDouble("squashing.max", 1.0);
        double squashingPower = config.getPropertyAsDouble("squashing.power", 1.0);
		double squashing = Math.pow(squashingMin + random.nextDouble()* (squashingMax - squashingMin),
                                    1.0/squashingPower);

		PublisherInfo publisherInfo = new PublisherInfo();
		publisherInfo.setSquashingParameter(squashing);
		publisherInfo.lock();

		return publisherInfo;
	}

	private BidTracker createBidTracker() {
		BidTracker bidTracker = new BidTrackerImpl(0);

		return bidTracker;
	}

	private SpendTracker createSpendTracker() {
		SpendTracker spendTracker = new SpendTrackerImpl(0);

		return spendTracker;
	}

	private QueryReportManager createQueryReportManager() {
		QueryReportManager queryReportManager = new QueryReportManagerImpl(
				queryReportSender, 0);

		for (String advertiser : agentRepository.getAdvertiserAddresses()) {
			queryReportManager.addAdvertiser(advertiser);
		}

		for (SimulationAgent agent : agentRepository.getUsers()) {
			Users users = (Users) agent.getAgent();
			users.addUserEventListener(queryReportManager);
		}

		return queryReportManager;
	}

	private BidManager createBidManager(BidTracker bidTracker,
			SpendTracker spendTracker) {

		BidManager bidManager = new BidManagerImpl(userClickModel, bidTracker,
				spendTracker);

		// All advertisers should be known to the bidManager
		String[] advertisers = agentRepository.getAdvertiserAddresses();
		for (int i = 0, n = advertisers.length; i < n; i++) {
			bidManager.addAdvertiser(advertisers[i]);
		}

		return bidManager;
	}

	private AuctionFactory createAuctionFactory() {
		String auctionFactoryClass = config.getProperty("auctionfactory.class",
				"edu.umich.eecs.tac.auction.LahaiePennockAuctionFactory");

		AuctionFactory factory = null;

		try {
			factory = (AuctionFactory) Class.forName(auctionFactoryClass)
					.newInstance();
		} catch (InstantiationException e) {
			log.log(Level.SEVERE, "error creating auction factory", e);
		} catch (IllegalAccessException e) {
			log.log(Level.SEVERE, "error creating auction factory", e);
		} catch (ClassNotFoundException e) {
			log.log(Level.SEVERE, "error creating auction factory", e);
		}

		return factory;
	}

	public void stopped() {
	}

	public void shutdown() {
	}

	public void messageReceived(Message message) {
		String sender = message.getSender();
		Transportable content = message.getContent();

		if (content instanceof BidBundle) {
			handleBidBundle(sender, (BidBundle) content);
		} else if (content instanceof UserClickModel) {
			handleUserClickModel((UserClickModel) content);
		} else if (content instanceof RetailCatalog) {
			handleRetailCatalog((RetailCatalog) content);
		} else if (content instanceof SlotInfo) {
			handleSlotInfo((SlotInfo) content);
		} else if (content instanceof ReserveInfo) {
			handleReserveInfo((ReserveInfo) content);
		}
	}

	private void handleSlotInfo(SlotInfo slotInfo) {
		this.slotInfo = slotInfo;

		if (auctionFactory != null) {
			auctionFactory.setSlotInfo(slotInfo);
		}
	}

	private void handleReserveInfo(ReserveInfo reserveInfo) {
		this.reserveInfo = reserveInfo;

		if (auctionFactory != null) {
			auctionFactory.setReserveInfo(reserveInfo);
		}
	}

	private void handleUserClickModel(UserClickModel userClickModel) {
		this.userClickModel = userClickModel;

		bidManager = createBidManager(bidTracker, spendTracker);

		if (auctionFactory != null) {
			auctionFactory.setBidManager(bidManager);
		}
	}

	private void handleBidBundle(String advertiser, BidBundle bidBundle) {
		if (bidManager == null) {
			// Not yet initialized => ignore the RFQ
			log.warning("Received BidBundle from " + advertiser
					+ " before initialization");
		} else {
			bidManager.updateBids(advertiser, bidBundle);

			bidBundleWriter.writeBundle(advertiser, bidBundle);
		}
	}

	private void handleRetailCatalog(RetailCatalog retailCatalog) {
		this.retailCatalog = retailCatalog;

		generatePossibleQueries();

		bidTracker.initializeQuerySpace(querySpace);
	}

	private void generatePossibleQueries() {
		if (retailCatalog != null && querySpace == null) {
			querySpace = new HashSet<Query>();

			for (Product product : retailCatalog) {
				Query f0 = new Query();
				Query f1_manufacturer = new Query(product.getManufacturer(),
						null);
				Query f1_component = new Query(null, product.getComponent());
				Query f2 = new Query(product.getManufacturer(), product
						.getComponent());

				querySpace.add(f0);
				querySpace.add(f1_manufacturer);
				querySpace.add(f1_component);
				querySpace.add(f2);
			}

		}
	}

	public void sendQueryReportsToAll() {
		if (queryReportManager != null)
			queryReportManager.sendQueryReportToAll();
	}

	public Auction runAuction(Query query) {

		if (auctionFactory != null) {
			return auctionFactory.runAuction(query);
		}

		return null;
	}

	public PublisherInfo getPublisherInfo() {
		return publisherInfo;
	}

	public void setPublisherInfo(PublisherInfo publisherInfo) {
		this.publisherInfo = publisherInfo;
	}

    public void applyBidUpdates() {
        bidManager.applyBidUpdates();
    }

    protected class ClickMonitor implements UserEventListener {

		public void queryIssued(Query query) {
		}

		public void viewed(Query query, Ad ad, int slot, String advertiser,
				boolean isPromoted) {
		}

		public void clicked(Query query, Ad ad, int slot, double cpc,
				String advertiser) {
			clickCharger.charge(advertiser, cpc);
			spendTracker.addCost(advertiser, query, cpc);
		}

		public void converted(Query query, Ad ad, int slot, double salesProfit,
				String advertiser) {
		}
	}
}
