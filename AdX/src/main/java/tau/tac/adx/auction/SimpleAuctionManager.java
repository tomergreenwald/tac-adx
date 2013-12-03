/**
 * 
 */
package tau.tac.adx.auction;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import tau.tac.adx.auction.data.AuctionData;
import tau.tac.adx.auction.data.AuctionOrder;
import tau.tac.adx.auction.data.AuctionResult;
import tau.tac.adx.auction.data.AuctionState;
import tau.tac.adx.bids.BidInfo;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.report.adn.MarketSegment;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * A simple implementation for the {@link AuctionManager} interface.
 * 
 * @author greenwald
 * 
 */
public class SimpleAuctionManager implements AuctionManager {

	/**
	 * Empty constructor.
	 */
	@Inject
	public SimpleAuctionManager() {
	}

	/**
	 * @see AuctionManager#runAuction(AuctionData)
	 */
	@Override
	public AdxAuctionResult runAuction(AuctionData auctionData, AdxQuery query) {
		BidInfo winningBid = initializeByAuctionOrder(auctionData
				.getAuctionOrder());
		BidInfo secondBid = initializeByAuctionOrder(auctionData
				.getAuctionOrder());
		for (BidInfo bidInfo : auctionData.getBidInfoCollection()) {
			if (betterBid(bidInfo, winningBid, auctionData.getAuctionOrder())) {
				secondBid = winningBid;
				winningBid = bidInfo;
			} else if (betterBid(bidInfo, secondBid,
					auctionData.getAuctionOrder())) {
				secondBid = bidInfo;
			}
		}
		BidInfo adjustedWinningBid = (BidInfo) winningBid.clone();
		Set<MarketSegment> marketSegments = Sets.intersection(
				query.getMarketSegments(), winningBid.getMarketSegments());
		adjustedWinningBid.setMarketSegments(marketSegments);
		return calculateAuctionResult(adjustedWinningBid, secondBid,
				auctionData);
	}

	/**
	 * Determines whether a {@link BidInfo new bid} is better than an old one,
	 * according to given {@link AuctionOrder}.
	 * 
	 * @param oldBid
	 *            Old {@link BidInfo bid}.
	 * @param newBid
	 *            New {@link BidInfo bid}.
	 * @param auctionOrder
	 *            {@link AuctionOrder} in which bids are to be ordered.
	 * @return <code>true</code> if <b>new bid</b> is better ranked than <b>old
	 *         bid</b>, according to given {@link AuctionOrder},
	 *         <code>false</code> otherwise.
	 */
	protected boolean betterBid(BidInfo newBid, BidInfo oldBid,
			AuctionOrder auctionOrder) {
		return betterThan(newBid.getBid(), oldBid.getBid(), auctionOrder);
	}

	/**
	 * Determines whether a value is better than another according to a given
	 * {@link AuctionOrder}.
	 * 
	 * @param first
	 *            First value to compare.
	 * @param second
	 *            Seconds value to compare.
	 * @param auctionOrder
	 *            {@link AuctionOrder} to compare by.
	 * @return <code>true</code> if first value is better than second value
	 *         according to the given {@link AuctionOrder} , <code>false</code>
	 *         otherwise.
	 */
	protected boolean betterThan(double first, double second,
			AuctionOrder auctionOrder) {
		switch (auctionOrder) {
		case HIGHEST_WINS:
			return first > second;
		case LOWEST_WINS:
			return first < second;
		default:
			throw switchCaseException(auctionOrder);
		}
	}

	/**
	 * Calculates the result of an auction according to given wining bids and
	 * {@link AuctionData} parameters, such as: {@link AuctionData#reservePrice}
	 * and {@link AuctionData#auctionOrder}.
	 * 
	 * @param winningBid
	 *            The winning bid for the auction.
	 * @param secondBid
	 *            The second winning bid for the auction.
	 * @param auctionData
	 *            {@link AuctionData} for the auction.
	 * @return The {@link AuctionResult}.
	 */
	protected AdxAuctionResult calculateAuctionResult(BidInfo winningBid,
			BidInfo secondBid, AuctionData auctionData) {
		List<String> participants = new LinkedList<String>();
		for (BidInfo bidInfo : auctionData.getBidInfoCollection()) {
			participants.add(bidInfo.getBidder().getName());
		}
		if (winningBid.equals(initializeByAuctionOrder(auctionData
				.getAuctionOrder()))) {
			return new AdxAuctionResult(AuctionState.NO_BIDS, null, Double.NaN,
					participants);
		}
		if (!passedReservePrice(winningBid, auctionData)) {
			return new AdxAuctionResult(AuctionState.LOW_BIDS, null, null,
					participants);
		}
		switch (auctionData.getAuctionPriceType()) {
		case GENERALIZED_FIRST_PRICE:
			return new AdxAuctionResult(AuctionState.AUCTION_COPMLETED,
					winningBid, winningBid.getBid(), participants);
		case GENERALIZED_SECOND_PRICE:
			double winningPrice;
			if (!passedReservePrice(secondBid, auctionData)) {
				winningPrice = auctionData.getReservePrice();
			} else {
				winningPrice = secondBid.getBid();
			}
			return new AdxAuctionResult(AuctionState.AUCTION_COPMLETED,
					winningBid, winningPrice, participants);
		default:
			throw switchCaseException(auctionData);
		}

	}

	/**
	 * Initializes a default {@link BidInfo} according to given
	 * {@link AuctionOrder}. Used as initial value at
	 * {@link #runAuction(AuctionData)}.
	 * 
	 * @param auctionOrder
	 *            {@link AuctionOrder} to sort bids by.
	 * @return An initial {@link BidInfo} to be used when performing an auction.
	 */
	protected BidInfo initializeByAuctionOrder(AuctionOrder auctionOrder) {
		switch (auctionOrder) {
		case HIGHEST_WINS:
			return new BidInfo(Double.MIN_VALUE, null, null,
					new HashSet<MarketSegment>(), null);
		case LOWEST_WINS:
			return new BidInfo(Double.MAX_VALUE, null, null,
					new HashSet<MarketSegment>(), null);
		default:
			throw switchCaseException(auctionOrder);
		}
	}

	/**
	 * Determines whether a bid passes a reserve price.
	 * 
	 * @param bid
	 *            {@link BidInfo} to check.
	 * @param auctionData
	 *            {@link AuctionData} to retrieve information from, such as:
	 *            {@link AuctionData#reservePrice} and
	 *            {@link AuctionData#auctionOrder}.
	 * @return <code>true</code> if {@link BidInfo bid} passes the reserve
	 *         price, <code>false</code> otherwise.
	 */
	protected boolean passedReservePrice(BidInfo bid, AuctionData auctionData) {
		if (auctionData.getReservePrice().equals(Double.NaN)) {
			return true;
		}
		return betterThan(bid.getBid(), auctionData.getReservePrice(),
				auctionData.getAuctionOrder());
	}

	/**
	 * Generates a default {@link UnsupportedOperationException} with a given
	 * {@link Object}.
	 * 
	 * @param object
	 *            {@link Object} to throw exception for.
	 * @return A new {@link UnsupportedOperationException} with details about
	 *         the causing object.
	 */
	protected UnsupportedOperationException switchCaseException(Object object) {
		return new UnsupportedOperationException(object.getClass().getName()
				+ " given type is not supporterd: " + object);
	}
}
