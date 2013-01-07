/**
 * 
 */
package tau.tac.adx.messages;

import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.users.AdxUser;

/**
 * Default interface for every auction message in the <b>Ad Exchange</b> system.
 * 
 * @author greenwald
 * 
 */
public class AuctionMessage implements AdxMessage {

	/**
	 * {@link AdxAuctionResult}.
	 */
	AdxAuctionResult auctionResult;
	/**
	 * Issuing {@link AdxQuery}.
	 */
	AdxQuery query;
	/**
	 * Participating {@link AdxUser}.
	 */
	AdxUser user;

	/**
	 * @param auctionResult
	 * @param query
	 * @param user
	 */
	public AuctionMessage(AdxAuctionResult auctionResult, AdxQuery query,
			AdxUser user) {
		super();
		this.auctionResult = auctionResult;
		this.query = query;
		this.user = user;
	}

	/**
	 * @return the auctionResult
	 */
	public AdxAuctionResult getAuctionResult() {
		return auctionResult;
	}

	/**
	 * @return the query
	 */
	public AdxQuery getQuery() {
		return query;
	}

	/**
	 * @return the user
	 */
	public AdxUser getUser() {
		return user;
	}
}
