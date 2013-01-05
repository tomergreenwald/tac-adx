/**
 * 
 */
package tau.tac.adx.auction.data;

/**
 * Auction state after an auction was performed.
 * 
 * @author greenwald
 * 
 */
public enum AuctionState {

	/**
	 * Auction was completed successfully.
	 */
	AUCTION_COPMLETED,
	/**
	 * All of the bids that took part in the <b>auction</b> were lower than the
	 * <b>reserve price</b> required.
	 */
	LOW_BIDS, /**
	 * No bids were available to perform <b>auction</b> with.
	 */
	NO_BIDS

}
