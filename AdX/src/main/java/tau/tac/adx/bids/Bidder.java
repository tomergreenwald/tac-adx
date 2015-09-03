/**
 * 
 */
package tau.tac.adx.bids;

/**
 * Common interface for all the bidders in the system. A bidder is a participant
 * in the bidding process.
 * 
 * @see BidInfo
 * 
 * @author greenwald
 * 
 */
public interface Bidder {
	// tag interface

	/**
	 * @return Bidder's id.
	 */
	public String getName();
}
