/**
 * 
 */
package tau.tac.adx.auction;

import tau.tac.adx.auction.data.AuctionData;
import tau.tac.adx.auction.data.AuctionResult;
import tau.tac.adx.props.AdxQuery;

/**
 * An <b>auction manager</b> runs auction according to given {@link AuctionData}
 * .
 * 
 * @author greenwald
 * 
 */
public interface AuctionManager {

	/**
	 * Runs an <b>auction</b> according to given {@link AuctionData}.
	 * 
	 * @param auctionData
	 *            {@link AuctionData} to run auction according to.
	 * @param query 
	 * @return An {@link AuctionResult}.
	 */
	public AdxAuctionResult runAuction(AuctionData auctionData, AdxQuery query);

}
