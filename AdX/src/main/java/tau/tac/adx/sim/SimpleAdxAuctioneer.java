/**
 * 
 */
package tau.tac.adx.sim;

import java.util.Collection;
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
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.publishers.reserve.ReservePriceManager;

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
	 */
	@Inject
	public SimpleAdxAuctioneer(AuctionManager auctionManager,
			AdxBidManager bidManager) {
		this.auctionManager = auctionManager;
		this.bidManager = bidManager;
	}

	/**
	 * @see AdxAuctioneer#runAuction(AdxQuery)
	 */
	@Override
	public AdxAuctionResult runAuction(AdxQuery query) {
		Collection<BidInfo> bidInfoCollection = generateBidInfos(query);

		ReservePriceManager reservePriceManager = AdxManager.getPublisher(
				query.getPublisher()).getReservePriceManager();
		Double reservePrice = reservePriceManager.generateReservePrice();
		AuctionData auctionData = new AuctionData(AuctionOrder.HIGHEST_WINS,
				AuctionPriceType.GENERALIZED_SECOND_PRICE, bidInfoCollection,
				reservePrice);
		AdxAuctionResult auctionResult = auctionManager.runAuction(auctionData);
		if (auctionResult.getAuctionState() == AuctionState.AUCTION_COPMLETED) {
			reservePriceManager.addImpressionForPrice(reservePrice);
		}
		return auctionResult;
	}

	private Collection<BidInfo> generateBidInfos(AdxQuery query) {
		Collection<BidInfo> bidInfoCollection = new HashSet<BidInfo>();
		Set<String> advertisers = bidManager.advertisers();
		for (final String advertiser : advertisers) {
			BidInfo bidInfo = bidManager.getBidInfo(advertiser, query);
			if (bidInfo != null) {
				bidInfoCollection.add(bidInfo);
			}
		}
		return bidInfoCollection;
	}

	@Override
	public void nextTimeUnit(int timeUnit) {
		// TODO Auto-generated method stub

	}

	@Override
	public void applyBidUpdates() {
		bidManager.applyBidUpdates();
	}
}
