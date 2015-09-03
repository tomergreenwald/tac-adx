/**
 * 
 */
package tau.tac.adx.auction.data;

/**
 * Auction winner's price type. Possible values are:
 * <ul>
 * <li> {@link AuctionPriceType#GENERALIZED_FIRST_PRICE}</li>
 * <li> {@link AuctionPriceType#GENERALIZED_SECOND_PRICE}</li>
 * </ul>
 * 
 * @author greenwald
 * 
 */
public enum AuctionPriceType {

	/**
	 * Price of winning bid will be the price paid by the auction winner.
	 */
	GENERALIZED_FIRST_PRICE, /**
	 * Price of second winning bid will be the price
	 * paid by the auction winner.
	 */
	GENERALIZED_SECOND_PRICE,

}
