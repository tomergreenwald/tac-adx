/**
 * 
 */
package tau.tac.adx.sim;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import se.sics.tasim.aw.TimeListener;
import tau.tac.adx.Adx;
import tau.tac.adx.AdxManager;
import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.auction.AuctionManager;
import tau.tac.adx.auction.data.AuctionData;
import tau.tac.adx.auction.data.AuctionOrder;
import tau.tac.adx.auction.data.AuctionPriceType;
import tau.tac.adx.auction.data.AuctionState;
import tau.tac.adx.auction.manager.AdxBidManager;
import tau.tac.adx.bids.BidInfo;
import tau.tac.adx.demand.UserClassificationService;
import tau.tac.adx.demand.UserClassificationServiceAdNetData;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.publishers.reserve.MultiReservePriceManager;
import tau.tac.adx.publishers.reserve.UserAdTypeReservePriceManager;
import tau.tac.adx.report.adn.MarketSegment;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import edu.umich.eecs.tac.auction.BidManager;

/**
 * Simple {@link TacAuctioneer} for {@link Adx}.
 * 
 * @author greenwald
 * 
 */
public class SimpleAdxAuctioneer implements AdxAuctioneer, TimeListener {
	
	/**
	 * {@link AuctionManager}.
	 */
	private final AuctionManager auctionManager;
	/**
	 * {@link BidManager}.
	 */
	private final AdxBidManager bidManager;
	
	private final Random random;

	/**
	 * @param auctionManager
	 *            {@link AuctionManager}.
	 * @param bidManager
	 *            {@link BidManager}.
	 * @param eventBus
	 *            {@link EventBus}.
	 */
	@Inject
	public SimpleAdxAuctioneer(AuctionManager auctionManager,
			AdxBidManager bidManager, EventBus eventBus) {
		this.auctionManager = auctionManager;
		this.bidManager = bidManager;
		this.random = new Random();
		eventBus.register(this);
	}

	/**
	 * @see AdxAuctioneer#runAuction(AdxQuery)
	 */
	@Override
	public AdxAuctionResult runAuction(AdxQuery query) {
		List<BidInfo> bidInfos = generateBidInfos(query);

		MultiReservePriceManager<AdxQuery> reservePriceManager = AdxManager.getInstance()
				.getPublisher(query.getPublisher()).getReservePriceManager();
		double reservePrice = reservePriceManager.generateReservePrice(query);
		AuctionData auctionData = new AuctionData(AuctionOrder.HIGHEST_WINS,
				AuctionPriceType.GENERALIZED_SECOND_PRICE, bidInfos,
				reservePrice);
		AdxAuctionResult auctionResult = auctionManager.runAuction(auctionData, query);
		if (auctionResult.getAuctionState() == AuctionState.AUCTION_COPMLETED) {
			reservePriceManager.addImpressionForPrice(reservePrice, query);
		}
		return auctionResult;
	}

	private List<BidInfo> generateBidInfos(AdxQuery query) {
		List<BidInfo> bidInfos = new LinkedList<BidInfo>();
		String[] advertisers = AdxManager.getInstance().getSimulation()
				.getAdxAdvertiserAddresses();
		for (final String advertiser : advertisers) {
			AdxQuery classifiedQuery = getClassifiedQuery(advertiser, query);
			BidInfo bidInfo = bidManager
					.getBidInfo(advertiser, classifiedQuery);
			if (bidInfo != null) {
				bidInfos.add(bidInfo);
			}
		}
		return bidInfos;
	}

	protected AdxQuery getClassifiedQuery(String advertiser, AdxQuery query) {
		UserClassificationService userClassificationService = AdxManager
				.getInstance().getUserClassificationService();
		UserClassificationServiceAdNetData adNetData = userClassificationService
				.getAdNetData(advertiser);
		if (adNetData.getServiceLevel() >= random.nextDouble()) {
			return query;
		}
		AdxQuery clone = query.clone();
		clone.setMarketSegments(new HashSet<MarketSegment>());
		return clone;
	}

	@Override
	public void nextTimeUnit(int timeUnit) {
	}

	@Override
	public void applyBidUpdates() {
		bidManager.applyBidUpdates();
	}

}
