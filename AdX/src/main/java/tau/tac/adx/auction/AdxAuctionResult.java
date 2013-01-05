/**
 * 
 */
package tau.tac.adx.auction;

import tau.tac.adx.Adx;
import tau.tac.adx.auction.data.AuctionResult;
import tau.tac.adx.auction.data.AuctionState;
import tau.tac.adx.bids.BidInfo;

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
	private AuctionState auctionState;

	/**
	 * Winning {@link BidInfo}.
	 */
	private BidInfo winningBidInfo;

	/**
	 * Actual winning price.
	 */
	private Double winningPrice;

	/**
	 * @param auctionState
	 *            {@link AuctionState}.
	 * @param winningBidInfo
	 *            Winning {@link BidInfo}.
	 * @param winningPrice
	 *            Winnin price.
	 */
	public AdxAuctionResult(AuctionState auctionState, BidInfo winningBidInfo,
			Double winningPrice) {
		super();
		this.auctionState = auctionState;
		this.winningBidInfo = winningBidInfo;
		this.winningPrice = winningPrice;
	}

	/**
	 * @return the auctionState
	 */
	public AuctionState getAuctionState() {
		return auctionState;
	}

	/**
	 * @param auctionState
	 *            the auctionState to set
	 */
	public void setAuctionState(AuctionState auctionState) {
		this.auctionState = auctionState;
	}

	/**
	 * @return the winningBidInfo
	 */
	public BidInfo getWinningBidInfo() {
		return winningBidInfo;
	}

	/**
	 * @param winningBidInfo
	 *            the winningBidInfo to set
	 */
	public void setWinningBidInfo(BidInfo winningBidInfo) {
		this.winningBidInfo = winningBidInfo;
	}

	/**
	 * @return the winningPrice
	 */
	public Double getWinningPrice() {
		return winningPrice;
	}

	/**
	 * @param winningPrice
	 *            the winningPrice to set
	 */
	public void setWinningPrice(Double winningPrice) {
		this.winningPrice = winningPrice;
	}

}
