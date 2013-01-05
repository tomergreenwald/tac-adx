/**
 * 
 */
package tau.tac.adx.auction.data;

/**
 * Auction winner selection order. Possible values are:
 * <ul>
 * <li> {@link AuctionOrder#HIGHEST_WINS}</li>
 * <li> {@link AuctionOrder#LOWEST_WINS}</li>
 * </ul>
 * 
 * @author greenwald
 * 
 */
public enum AuctionOrder {

	/**
	 * Highest bid will win the auction.
	 */
	HIGHEST_WINS, /**
	 * Lowest bid will win the auction.
	 */
	LOWEST_WINS

}
