/**
 * 
 */
package tau.tac.adx.auction;

import java.util.Collection;
import java.util.Set;

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
	 * Collection of {@link BidInfo}s.
	 */
	private final Collection<BidInfo> bidInfos;

	/**
	 * @param auctionState
	 *            {@link AuctionState}.
	 * @param winningBidInfo
	 *            Winning {@link BidInfo}.
	 * @param winningPrice
	 *            Wining price.
	 * @param participants
	 *            List of participants in the auction.
	 */
	public AdxAuctionResult(AuctionState auctionState, BidInfo winningBidInfo,
			Double winningPrice, Collection<BidInfo> bidInfos) {
		super();
		this.auctionState = auctionState;
		this.winningBidInfo = winningBidInfo;
		this.winningPrice = winningPrice;
		this.bidInfos = bidInfos;
	}

	/**
	 * @return the marketSegment
	 */
	public Set<MarketSegment> getMarketSegments() {
		return winningBidInfo.getMarketSegments();
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
		if (winningBidInfo == null) {
			return null;
		}
		return winningBidInfo.getCampaign();
	}

	/**
	 * @return the bidInfos
	 */
	public Collection<BidInfo> getBidInfos() {
		return bidInfos;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AdxAuctionResult [auctionState=" + auctionState
				+ ", winningBidInfo=" + winningBidInfo + ", winningPrice="
				+ winningPrice + ", bidInfos=" + bidInfos + "]";
	}

}
