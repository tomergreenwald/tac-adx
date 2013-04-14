/**
 * 
 */
package tau.tac.adx.sim;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
import tau.tac.adx.publishers.reserve.ReservePriceManager;
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
		eventBus.register(this);
	}

	/**
	 * @see AdxAuctioneer#runAuction(AdxQuery)
	 */
	@Override
	public AdxAuctionResult runAuction(AdxQuery query) {
		Collection<BidInfo> bidInfoCollection = generateBidInfos(query);

		ReservePriceManager reservePriceManager = AdxManager.getInstance()
				.getPublisher(query.getPublisher()).getReservePriceManager();
		Double reservePrice = reservePriceManager.generateReservePrice();
		AuctionData auctionData = new AuctionData(AuctionOrder.HIGHEST_WINS,
				AuctionPriceType.GENERALIZED_SECOND_PRICE, bidInfoCollection,
				reservePrice);
		AdxAuctionResult auctionResult = auctionManager.runAuction(auctionData, query);
		if (auctionResult.getAuctionState() == AuctionState.AUCTION_COPMLETED) {
			reservePriceManager.addImpressionForPrice(reservePrice);
		}
		return auctionResult;
	}

	private Collection<BidInfo> generateBidInfos(AdxQuery query) {
		Collection<BidInfo> bidInfoCollection = new HashSet<BidInfo>();
		String[] advertisers = AdxManager.getInstance().getSimulation()
				.getAdxAdvertiserAddresses();
		for (final String advertiser : advertisers) {
			AdxQuery classifiedQuery = getClassifiedQuery(advertiser, query);
			BidInfo bidInfo = bidManager
					.getBidInfo(advertiser, classifiedQuery);
			if (bidInfo != null) {
				bidInfoCollection.add(bidInfo);
			}
		}
		return bidInfoCollection;
	}

	private AdxQuery getClassifiedQuery(String advertiser, AdxQuery query) {
		UserClassificationService userClassificationService = AdxManager
				.getInstance().getUserClassificationService();
		UserClassificationServiceAdNetData adNetData = userClassificationService
				.getAdNetData(advertiser);
		if (adNetData.getServiceLevel() > 0) {
			return query;
		}
		AdxQuery clone = query.clone();
		clone.setMarketSegments(new HashSet<MarketSegment>());
		// return clone;
		return query;
	}

	@Override
	public void nextTimeUnit(int timeUnit) {
	}

	@Override
	public void applyBidUpdates() {
		bidManager.applyBidUpdates();
	}

}
