package tau.tac.adx.users;

import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.props.AdxQuery;

/**
 * Adx user event listener interface.
 * 
 * @author greenwald
 */
public interface AdxUserEventListener {
	/**
	 * @param query
	 *            Issued {@link AdxQuery}.
	 */
	void queryIssued(AdxQuery query);

	/**
	 * Auction was performed by the <b>ADX</b> and results are given as
	 * parameters.
	 * 
	 * @param auctionResult
	 *            {@link AdxAuctionResult}.
	 * @param query
	 *            Issuing {@link AdxQuery}.
	 * @param user
	 *            Participating {@link AdxUser}.
	 */
	void auctionPerformed(AdxAuctionResult auctionResult, AdxQuery query,
			AdxUser user);
}
