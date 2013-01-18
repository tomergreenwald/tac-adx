/**
 * 
 */
package tau.tac.adx.auction;

import tau.tac.adx.Adx;
import tau.tac.adx.auction.data.AuctionResult;
import tau.tac.adx.auction.data.AuctionState;
import tau.tac.adx.bids.BidInfo;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.report.adn.MarketSegment;

/**
 * Result of an <b>auction</b>.
 * 
 * @author greenwald
 * 
 */
public class AdxAuctionResult implements AuctionResult<Adx> {

	/**
	 * {@link AuctionState}.
	 */
	private final AuctionState auctionState;

	/**
	 * Winning {@link BidInfo}.
	 */
	private final BidInfo winningBidInfo;

	/**
	 * Actual winning price.
	 */
	private final Double winningPrice;

	/**
	 * @param auctionState
	 *            {@link AuctionState}.
	 * @param winningBidInfo
	 *            Winning {@link BidInfo}.
	 * @param winningPrice
	 *            Wining price.
	 */
	public AdxAuctionResult(AuctionState auctionState, BidInfo winningBidInfo,
			Double winningPrice) {
		super();
		this.auctionState = auctionState;
		this.winningBidInfo = winningBidInfo;
		this.winningPrice = winningPrice;
	}

	/**
	 * @return the marketSegment
	 */
	public MarketSegment getMarketSegment() {
		return winningBidInfo.getMarketSegment();
	}

	/**
	 * @return the auctionState
	 */
	public AuctionState getAuctionState() {
		return auctionState;
	}

	/**
	 * @return the winningBidInfo
	 */
	public BidInfo getWinningBidInfo() {
		return winningBidInfo;
	}

	/**
	 * @return the winningPrice
	 */
	public Double getWinningPrice() {
		return winningPrice;
	}

	/**
	 * @return the campaign
	 */
	public Campaign getCampaign() {
		return winningBidInfo.getCampaign();
	}
}
