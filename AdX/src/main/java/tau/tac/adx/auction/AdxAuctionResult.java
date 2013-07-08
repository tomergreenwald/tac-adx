/**
 * 
 */
package tau.tac.adx.auction;

import java.util.List;
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
	 * List of participants in the auction.
	 */
	private final List<String> participants;

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
			Double winningPrice, List<String> participants) {
		super();
		this.auctionState = auctionState;
		this.winningBidInfo = winningBidInfo;
		this.winningPrice = winningPrice;
		this.participants = participants;
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
	 * @return the participants
	 */
	public List<String> getParticipants() {
		return participants;
	}

	@Override
	public String toString() {
		return "State: "
				+ auctionState.toString()
				+ " Winning: "
				+ winningPrice.toString()
				+ " Segments: "
				+ ((winningBidInfo != null) ? winningBidInfo
						.getMarketSegments() : "");
	}
}
