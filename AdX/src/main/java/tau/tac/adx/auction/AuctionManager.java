/**
 * 
 */
package tau.tac.adx.auction;

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
	 * @return An {@link AuctionResult}.
	 */
	public AdxAuctionResult runAuction(AuctionData auctionData);

}
