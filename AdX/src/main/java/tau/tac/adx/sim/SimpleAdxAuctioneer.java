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
import tau.tac.adx.auction.AuctionData;
import tau.tac.adx.auction.AuctionManager;
import tau.tac.adx.auction.AuctionOrder;
import tau.tac.adx.auction.AuctionPriceType;
import tau.tac.adx.auction.AuctionState;
import tau.tac.adx.auction.manager.AdxBidManager;
import tau.tac.adx.bids.BidInfo;
import tau.tac.adx.bids.BidProduct;
import tau.tac.adx.bids.Bidder;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.publishers.reserve.ReservePriceManager;

/**
 * Simple {@link TacAuctioneer} for {@link Adx}.
 * 
 * @author greenwald
 * 
 */
public class SimpleAdxAuctioneer implements AdxAuctioneer, TimeListener {

	private final AuctionManager auctionManager;
	private AdxBidManager bidManager;

	/**
	 * @param auctionManager
	 */
	public SimpleAdxAuctioneer(AuctionManager auctionManager) {
		this.auctionManager = auctionManager;
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
			double bid = bidManager.getBid(advertiser, query);
			Bidder bidder = new Bidder() {

				@Override
				public int getId() {
					return AdxManager.getSimulation().agentIndex(advertiser);
				}
			};
			BidProduct bidProduct = bidManager.getAdLink(advertiser, query);
			BidInfo bidInfo = new BidInfo(bid, bidder, bidProduct);
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
