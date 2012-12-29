/**
 * 
 */
package tau.tac.adx.sim;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tau.tac.adx.Adx;
import tau.tac.adx.AdxManager;
import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.auction.AuctionState;
import tau.tac.adx.bids.BidInfo;
import tau.tac.adx.bids.BidProduct;
import tau.tac.adx.props.AdLink;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.TacQuery;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.publishers.reserve.ReservePriceManager;
import tau.tac.adx.util.EnumGenerator;
import edu.umich.eecs.tac.props.Product;

/**
 * Simple {@link TacAuctioneer} for {@link Adx}.
 * 
 * @author greenwald
 * 
 */
public class SimpleAdxAuctioneer implements TacAuctioneer<Adx> {

	/**
	 * @see tau.tac.adx.sim.TacAuctioneer#runAuction(tau.tac.adx.props.TacQuery)
	 */
	@Override
	public List<AdxAuctionResult> runAuction(TacQuery<Adx> query) {
		AdxPublisher publisher = AdxManager.getPublisher(((AdxQuery) query)
				.getPublisher());
		ReservePriceManager reservePriceManager = publisher
				.getReservePriceManager();
		double generateReservePrice = reservePriceManager
				.generateReservePrice();
		Product ad = new Product("a", "b");
		BidProduct bidProduct = new AdLink(ad, "eddy");
		BidInfo bidInfo = new BidInfo(Math.random() * 100, null, bidProduct);
		Map<AuctionState, Integer> weights = new HashMap<AuctionState, Integer>();
		weights.put(AuctionState.AUCTION_COPMLETED, 3);
		weights.put(AuctionState.LOW_BIDS, 2);
		weights.put(AuctionState.NO_BIDS, 1);
		EnumGenerator<AuctionState> generator = new EnumGenerator<AuctionState>(
				weights);
		// TODO run auction
		AdxAuctionResult auctionResult = new AdxAuctionResult(
				generator.randomType(), bidInfo, bidInfo.getBid());
		if (auctionResult.getAuctionState() == AuctionState.AUCTION_COPMLETED) {
			reservePriceManager.addImpressionForPrice(generateReservePrice);
		}
		return Collections.singletonList(auctionResult);
	}

}
