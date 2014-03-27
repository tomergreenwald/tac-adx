package tau.tac.adx.demand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import tau.tac.adx.AdxManager;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.sim.TACAdxSimulation;

/**
 * @author mariano
 */
public class CampaignTest {
	int reachImps = 100;
	int dayStart = 5;
	int dayEnd = 12;
	// #FIXME fix this test
	Set<MarketSegment> targetSegment = MarketSegment.randomMarketSegment();
	MarketSegment nonTargetSegment = MarketSegment.OLD;

	double vcoef = 2.5;
	double mcoef = 0.8;

	Campaign campaign;

	QualityManager qualityMgr;

	@Before
	public void setUp() {

		qualityMgr = new QualityManagerImpl();
		qualityMgr.addAdvertiser("a1");
		qualityMgr.addAdvertiser("a2");
		qualityMgr.addAdvertiser("a3");
		// #FIXME fix this test
		//
		campaign = new CampaignImpl(qualityMgr, reachImps, dayStart, dayEnd,
				targetSegment, vcoef, mcoef);
		campaign.setRandomAllocPr(new Double(0.0));

		assertNotNull(campaign);
	}

	@Test
	public void testAddAdvertisers() {
		campaign.addAdvertiserBid("a1", 50L);
		assertEquals(campaign.getBiddingAdvertisers().get("a1"), 50L, 0);

		campaign.addAdvertiserBid("a2", 30L);
		assertEquals(campaign.getBiddingAdvertisers().get("a1"), 50L, 0);
		assertEquals(campaign.getBiddingAdvertisers().get("a2"), 30L, 0);

		campaign.addAdvertiserBid("a3", 150L);
		assertEquals(campaign.getBiddingAdvertisers().get("a1"), 50L, 0);
		assertEquals(campaign.getBiddingAdvertisers().get("a2"), 30L, 0);
		/* bids beyond reserve are not considered */
		assertNull(campaign.getBiddingAdvertisers().get("a3"));

	}

	@Test
	public void testAuctionSingleAdvertiser1() {
		campaign.addAdvertiserBid("a1", 50L);
		assertNull(campaign.getAdvertiser());
		campaign.auction();
		assertEquals(campaign.getAdvertiser(), "a1");
		assertEquals(campaign.getBudgetMillis(), 100, 0);
	}

	@Test
	public void testAuctionSingleAdvertiser2() {
		campaign.addAdvertiserBid("a2", 150L);
		assertNull(campaign.getAdvertiser());
		campaign.auction();
		assertEquals("", campaign.getAdvertiser());
		assertEquals(campaign.getBudgetMillis(), 0);
	}

	@Test
	public void testAuctionMultipleAdvertisers1() {
		campaign.addAdvertiserBid("a1", 80L);
		campaign.addAdvertiserBid("a2", 50L);
		campaign.addAdvertiserBid("a3", 120L);
		campaign.auction();
		assertEquals(campaign.getAdvertiser(), "a2");
		assertEquals(campaign.getBudgetMillis(), 80, 0.001);
	}

	@Test
	public void testAuctionMultipleAdvertisers2() {
		campaign.addAdvertiserBid("a1", 120L);
		campaign.addAdvertiserBid("a2", 110L);
		campaign.addAdvertiserBid("a3", 80L);
		campaign.auction();
		assertEquals(campaign.getAdvertiser(), "a3");
		assertEquals(campaign.getBudgetMillis(), 100, 0);
	}

	@Test
	public void testAllocated1() {
		campaign.addAdvertiserBid("a1", 120L);
		assertFalse(campaign.isAllocated());
		campaign.auction();
		assertFalse(campaign.isAllocated());
	}

	@Test
	public void testAllocated2() {
		campaign.addAdvertiserBid("a1", 80L);
		assertFalse(campaign.isAllocated());
		campaign.auction();
		assertTrue(campaign.isAllocated());
	}

	// void multiImpress(Campaign cpgn, boolean targeted, long costMillis,
	// int multi) {
	// for (int i = 0; i < multi; i++)
	// if (targeted)
	// cpgn.impress(targetSegment, AdType.text, Device.pc, costMillis);
	// else
	// cpgn.impress(nonTargetSegment, AdType.text, Device.pc,
	// costMillis);
	// }

	@Test
	@Ignore
	public void testQualityScore() {
		CampaignStats dstats;

		campaign.addAdvertiserBid("a1", 80L);
		campaign.addAdvertiserBid("a2", 50L);
		campaign.addAdvertiserBid("a3", 120L);
		campaign.auction();
		assertEquals(campaign.getAdvertiser(), "a2");
		assertEquals(campaign.getBudgetMillis(), 80, 0);
		// assertFalse(campaign.isActive());

		for (int day = dayStart; day <= dayEnd; day++) {
			campaign.preNextTimeUnit(day);
			assertTrue(campaign.isActive());
			// multiImpress(campaign, true, 500, 10);
		}
		AdxManager.getInstance().setSimulation(mock(TACAdxSimulation.class));
		campaign.preNextTimeUnit(dayEnd + 2);
		assertFalse(campaign.isActive());

		dstats = campaign.getStats(dayStart, dayEnd);
		assertEquals(dstats.tartgetedImps, 80, 0);
		assertEquals(dstats.otherImps, 24, 0);
		assertEquals(dstats.cost, 47.2, 0.01);

		assertEquals(qualityMgr.getQualityScore("a2"), 0.82, 0.01);

	}

}
