/**
 * 
 */
package tau.tac.adx.auction;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import tau.tac.adx.auction.data.AuctionData;
import tau.tac.adx.auction.data.AuctionOrder;
import tau.tac.adx.bids.BidInfo;

/**
 * @author Tomer
 * 
 */
public class SimpleAuctionManagerTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for
	 * {@link tau.tac.adx.auction.SimpleAuctionManager#runAuction(tau.tac.adx.auction.data.AuctionData, tau.tac.adx.props.AdxQuery)}
	 * .
	 */
	@Test
	public void testRunAuction() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link tau.tac.adx.auction.SimpleAuctionManager#betterBid(tau.tac.adx.bids.BidInfo, tau.tac.adx.bids.BidInfo, tau.tac.adx.auction.data.AuctionOrder)}
	 * .
	 */
	@Test
	public void testBetterBid() {
		fail("Not yet implemented"); // TODO
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
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link tau.tac.adx.auction.SimpleAuctionManager#initializeByAuctionOrder(tau.tac.adx.auction.data.AuctionOrder)}
	 * .
	 */
	@Test
	public void testInitializeByAuctionOrder() {
		fail("Not yet implemented"); // TODO
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
