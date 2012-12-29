package tau.tac.adx.sim;

import java.util.List;

import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.auction.AuctionResult;
import tau.tac.adx.props.TacQuery;

/**
 * @author greenwald
 * @param <T>
 *            Auctioneer type.
 */
public interface TacAuctioneer<T> {

	/**
	 * Runs an auciton for a given {@link TacQuery}.
	 * 
	 * @param query
	 *            {@link TacQuery} to run auction for.
	 * @return {@link AuctionResult}.
	 */
	List<AdxAuctionResult> runAuction(TacQuery<T> query);
}
