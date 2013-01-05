package tau.tac.adx.sim;

import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.props.AdxQuery;

/**
 * @author greenwald
 */
public interface AdxAuctioneer {

	/**
	 * Runs an auction for a given {@link AdxQuery}.
	 * 
	 * @param query
	 *            {@link AdxQuery} to run auction for.
	 * @return {@link AdxAuctionResult}.
	 */
	AdxAuctionResult runAuction(AdxQuery query);

	/**
	 * Applies bid updates.
	 */
	public void applyBidUpdates();
}
