/**
 * 
 */
package tau.tac.adx.auction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import tau.tac.adx.auction.data.AuctionData;
import tau.tac.adx.auction.data.AuctionOrder;
import tau.tac.adx.auction.data.AuctionPriceType;
import tau.tac.adx.auction.data.AuctionState;
import tau.tac.adx.bids.BidInfo;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.report.adn.MarketSegment;

/**
 * @author Tomer
 * 
 */
public class SimpleAuctionManagerTest {

	/**
	 * Test method for
	 * {@link tau.tac.adx.auction.SimpleAuctionManager#runAuction(tau.tac.adx.auction.data.AuctionData, tau.tac.adx.props.AdxQuery)}
	 * .
	 */
	@Test
	public void testRunAuction() {
		SimpleAuctionManager manager = new SimpleAuctionManager();
		for (int i = 0; i < 1000; i++) {
			Double reservePrice = Math.random() * 100;
			// Bids are in millis
			double low = reservePrice / 2 * 1000;
			double high = reservePrice * 2 * 1000;

			AuctionData auctionData = mock(AuctionData.class);
			when(auctionData.getReservePrice()).thenReturn(reservePrice);
			BidInfo lowBid = mock(BidInfo.class);
			BidInfo highBid = new BidInfo(high, null, null,
					MarketSegment.randomMarketSegment(), null);
			when(lowBid.getBid()).thenReturn(low);

			when(auctionData.getAuctionOrder()).thenReturn(
					AuctionOrder.HIGHEST_WINS);
			when(auctionData.getAuctionPriceType()).thenReturn(
					AuctionPriceType.GENERALIZED_SECOND_PRICE);
			AdxQuery query = mock(AdxQuery.class);

			List<BidInfo> bidInfos = new LinkedList<BidInfo>();
			for (int j = 0; j < 7; j++) {
				BidInfo bidInfo = mock(BidInfo.class);
				when(bidInfo.getBid()).thenReturn(high - (j + 1));
				when(bidInfo.clone()).thenReturn(bidInfo);
				bidInfos.add(bidInfo);
			}
			bidInfos.add(highBid);
			when(auctionData.getBidInfos()).thenReturn(bidInfos);
			AdxAuctionResult auctionResult = manager.runAuction(auctionData,
					query);
			assertEquals(AuctionState.AUCTION_COPMLETED,
					auctionResult.getAuctionState());
			assertEquals(high - 1, auctionResult.getWinningPrice().doubleValue(), 0);
			
			bidInfos = new LinkedList<BidInfo>();
			for (int j = 0; j < 7; j++) {
				BidInfo bidInfo = mock(BidInfo.class);
				when(bidInfo.getBid()).thenReturn(low - (j + 1));
				when(bidInfo.clone()).thenReturn(bidInfo);
				bidInfos.add(bidInfo);
			}
			bidInfos.add(highBid);
			when(auctionData.getBidInfos()).thenReturn(bidInfos);
			auctionResult = manager.runAuction(auctionData,
					query);
			assertEquals(AuctionState.AUCTION_COPMLETED,
					auctionResult.getAuctionState());
			assertEquals(reservePrice, auctionResult.getWinningPrice().doubleValue(), 0);
			
			bidInfos = new LinkedList<BidInfo>();
			for (int j = 0; j < 8; j++) {
				BidInfo bidInfo = mock(BidInfo.class);
				when(bidInfo.getBid()).thenReturn(low - (j + 1));
				when(bidInfo.clone()).thenReturn(bidInfo);
				bidInfos.add(bidInfo);
			}
			when(auctionData.getBidInfos()).thenReturn(bidInfos);
			auctionResult = manager.runAuction(auctionData,
					query);
			assertEquals(AuctionState.LOW_BIDS,
					auctionResult.getAuctionState());
			assertNull(auctionResult.getWinningPrice());
		}
	}

	/**
	 * Test method for
	 * {@link tau.tac.adx.auction.SimpleAuctionManager#betterBid(tau.tac.adx.bids.BidInfo, tau.tac.adx.bids.BidInfo, tau.tac.adx.auction.data.AuctionOrder)}
	 * .
	 */
	@Test
	public void testBetterBid() {
		for (int i = 0; i < 1000; i++) {
			Double reservePrice = Math.random() * 100;
			// Bids are in millis
			Double low = reservePrice / 2 * 1000;
			Double high = reservePrice * 2 * 1000;

			BidInfo lowBid = mock(BidInfo.class);
			BidInfo highBid = mock(BidInfo.class);
			when(highBid.getBid()).thenReturn(high);
			when(lowBid.getBid()).thenReturn(low);

			assertTrue(SimpleAuctionManager.betterBid(highBid, lowBid,
					AuctionOrder.HIGHEST_WINS));
			assertFalse(SimpleAuctionManager.betterBid(lowBid, highBid,
					AuctionOrder.HIGHEST_WINS));
			assertFalse(SimpleAuctionManager.betterBid(highBid, lowBid,
					AuctionOrder.LOWEST_WINS));
			assertTrue(SimpleAuctionManager.betterBid(lowBid, highBid,
					AuctionOrder.LOWEST_WINS));
		}
	}

	/**
	 * Test method for
	 * {@link tau.tac.adx.auction.SimpleAuctionManager#betterThan(double, double, tau.tac.adx.auction.data.AuctionOrder)}
	 * .
	 */
	@Test
	public void testBetterThan() {
		for (int i = 0; i < 1000; i++) {
			Double reservePrice = Math.random() * 100;
			Double low = reservePrice / 2;
			Double high = reservePrice * 2;

			assertTrue(SimpleAuctionManager.betterThan(high, reservePrice,
					AuctionOrder.HIGHEST_WINS));
			assertFalse(SimpleAuctionManager.betterThan(low, reservePrice,
					AuctionOrder.HIGHEST_WINS));
			assertFalse(SimpleAuctionManager.betterThan(high, reservePrice,
					AuctionOrder.LOWEST_WINS));
			assertTrue(SimpleAuctionManager.betterThan(low, reservePrice,
					AuctionOrder.LOWEST_WINS));
		}
	}

	/**
	 * Test method for
	 * {@link tau.tac.adx.auction.SimpleAuctionManager#calculateAuctionResult(tau.tac.adx.bids.BidInfo, tau.tac.adx.bids.BidInfo, tau.tac.adx.auction.data.AuctionData)}
	 * .
	 */
	@Test
	public void testCalculateAuctionResult() {
		for (int i = 0; i < 1000; i++) {
			Double reservePrice = Math.random() * 100;
			// Bids are in millis
			Double low = reservePrice / 2 * 1000;
			Double low2 = reservePrice / 3 * 1000;
			Double high = reservePrice * 2 * 1000;
			Double high2 = reservePrice * 3 * 1000;

			AuctionData auctionData = mock(AuctionData.class);
			when(auctionData.getReservePrice()).thenReturn(reservePrice);
			BidInfo lowBid = mock(BidInfo.class);
			BidInfo highBid = mock(BidInfo.class);
			when(highBid.getBid()).thenReturn(high);
			when(lowBid.getBid()).thenReturn(low);
			BidInfo lowBid2 = mock(BidInfo.class);
			BidInfo highBid2 = mock(BidInfo.class);
			when(highBid2.getBid()).thenReturn(high2);
			when(lowBid2.getBid()).thenReturn(low2);

			when(auctionData.getAuctionOrder()).thenReturn(
					AuctionOrder.HIGHEST_WINS);
			when(auctionData.getAuctionPriceType()).thenReturn(
					AuctionPriceType.GENERALIZED_SECOND_PRICE);
			AdxAuctionResult auctionResult = SimpleAuctionManager
					.calculateAuctionResult(highBid, lowBid, auctionData);
			assertEquals(AuctionState.AUCTION_COPMLETED,
					auctionResult.getAuctionState());
			assertEquals(reservePrice, auctionResult.getWinningPrice());

			auctionResult = SimpleAuctionManager.calculateAuctionResult(
					highBid2, highBid, auctionData);
			assertEquals(AuctionState.AUCTION_COPMLETED,
					auctionResult.getAuctionState());
			assertEquals(high, auctionResult.getWinningPrice());

			auctionResult = SimpleAuctionManager.calculateAuctionResult(lowBid,
					lowBid, auctionData);
			assertEquals(AuctionState.LOW_BIDS, auctionResult.getAuctionState());

			when(auctionData.getAuctionPriceType()).thenReturn(
					AuctionPriceType.GENERALIZED_FIRST_PRICE);
			auctionResult = SimpleAuctionManager.calculateAuctionResult(
					highBid, lowBid, auctionData);
			assertEquals(AuctionState.AUCTION_COPMLETED,
					auctionResult.getAuctionState());
			assertEquals(high, auctionResult.getWinningPrice());

			auctionResult = SimpleAuctionManager.calculateAuctionResult(
					highBid2, highBid, auctionData);
			assertEquals(AuctionState.AUCTION_COPMLETED,
					auctionResult.getAuctionState());
			assertEquals(high2, auctionResult.getWinningPrice());

			auctionResult = SimpleAuctionManager.calculateAuctionResult(lowBid,
					lowBid, auctionData);
			assertEquals(AuctionState.LOW_BIDS, auctionResult.getAuctionState());

			when(auctionData.getAuctionOrder()).thenReturn(
					AuctionOrder.LOWEST_WINS);
			when(auctionData.getAuctionPriceType()).thenReturn(
					AuctionPriceType.GENERALIZED_SECOND_PRICE);
			auctionResult = SimpleAuctionManager.calculateAuctionResult(lowBid,
					highBid, auctionData);
			assertEquals(AuctionState.AUCTION_COPMLETED,
					auctionResult.getAuctionState());
			assertEquals(reservePrice, auctionResult.getWinningPrice());

			auctionResult = SimpleAuctionManager.calculateAuctionResult(
					lowBid2, lowBid, auctionData);
			assertEquals(AuctionState.AUCTION_COPMLETED,
					auctionResult.getAuctionState());
			assertEquals(low, auctionResult.getWinningPrice());

			auctionResult = SimpleAuctionManager.calculateAuctionResult(
					highBid, highBid2, auctionData);
			assertEquals(AuctionState.LOW_BIDS, auctionResult.getAuctionState());

			when(auctionData.getAuctionPriceType()).thenReturn(
					AuctionPriceType.GENERALIZED_FIRST_PRICE);
			auctionResult = SimpleAuctionManager.calculateAuctionResult(lowBid,
					highBid, auctionData);
			assertEquals(AuctionState.AUCTION_COPMLETED,
					auctionResult.getAuctionState());
			assertEquals(low, auctionResult.getWinningPrice());

			auctionResult = SimpleAuctionManager.calculateAuctionResult(
					lowBid2, lowBid, auctionData);
			assertEquals(AuctionState.AUCTION_COPMLETED,
					auctionResult.getAuctionState());
			assertEquals(low2, auctionResult.getWinningPrice());

			auctionResult = SimpleAuctionManager.calculateAuctionResult(
					highBid, highBid2, auctionData);
			assertEquals(AuctionState.LOW_BIDS, auctionResult.getAuctionState());
		}
	}

	/**
	 * Test method for
	 * {@link tau.tac.adx.auction.SimpleAuctionManager#initializeByAuctionOrder(tau.tac.adx.auction.data.AuctionOrder)}
	 * .
	 */
	@Test
	public void testInitializeByAuctionOrder() {
		assertEquals(Double.MIN_VALUE, SimpleAuctionManager
				.initializeByAuctionOrder(AuctionOrder.HIGHEST_WINS).getBid(),
				0);
		assertEquals(Double.MAX_VALUE, SimpleAuctionManager
				.initializeByAuctionOrder(AuctionOrder.LOWEST_WINS).getBid(), 0);
	}

	/**
	 * Test method for
	 * {@link tau.tac.adx.auction.SimpleAuctionManager#passedReservePrice(tau.tac.adx.bids.BidInfo, tau.tac.adx.auction.data.AuctionData)}
	 * .
	 */
	@Test
	public void testPassedReservePrice() {
		for (int i = 0; i < 1000; i++) {
			Double reservePrice = Math.random() * 100;
			// Bids are in millis
			Double low = reservePrice / 2 * 1000;
			Double high = reservePrice * 2 * 1000;

			AuctionData auctionData = mock(AuctionData.class);
			when(auctionData.getReservePrice()).thenReturn(reservePrice);
			BidInfo bid = mock(BidInfo.class);

			when(auctionData.getAuctionOrder()).thenReturn(
					AuctionOrder.HIGHEST_WINS);
			when(bid.getBid()).thenReturn(high);
			assertTrue(SimpleAuctionManager
					.passedReservePrice(bid, auctionData));
			when(bid.getBid()).thenReturn(low);
			assertFalse(SimpleAuctionManager.passedReservePrice(bid,
					auctionData));
			when(auctionData.getAuctionOrder()).thenReturn(
					AuctionOrder.LOWEST_WINS);
			when(bid.getBid()).thenReturn(high);
			assertFalse(SimpleAuctionManager.passedReservePrice(bid,
					auctionData));
			when(bid.getBid()).thenReturn(low);
			assertTrue(SimpleAuctionManager
					.passedReservePrice(bid, auctionData));
		}
	}

}
