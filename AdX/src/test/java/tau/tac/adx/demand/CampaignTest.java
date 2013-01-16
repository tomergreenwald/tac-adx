package tau.tac.adx.demand;



import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import tau.tac.adx.report.adn.MarketSegment;



/**
 * @author mariano
 */
public class CampaignTest {
  Long reachImps = 100L;
  int dayStart = 5;
  int dayEnd = 12;
  MarketSegment targetSegment = MarketSegment.FEMALE_HIGH_INCOME;
  MarketSegment nonTargetSegment = MarketSegment.OLD_HIGH_INCOME;

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
		
		
		campaign = new CampaignImpl(qualityMgr,  
				reachImps,  dayStart,  dayEnd,
				 targetSegment,  vcoef, mcoef);

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
		assertEquals(campaign.getAdvertiser(),"a1");
		assertEquals(campaign.getBudget(), 100L, 0);
	}	

	@Test
	public void testAuctionSingleAdvertiser2() {
		campaign.addAdvertiserBid("a2", 150L);
		assertNull(campaign.getAdvertiser());
		campaign.auction();
		assertNull(campaign.getAdvertiser());
		assertNull(campaign.getBudget());
	}	

	
	@Test
	public void testAuctionMultipleAdvertisers1() {
		campaign.addAdvertiserBid("a1", 80L);
		campaign.addAdvertiserBid("a2", 50L);
		campaign.addAdvertiserBid("a3", 120L);
		campaign.auction();
		assertEquals(campaign.getAdvertiser(),"a2");
		assertEquals(campaign.getBudget(), 80L, 0);
	}

	@Test
	public void testAuctionMultipleAdvertisers2() {
		campaign.addAdvertiserBid("a1", 120L);
		campaign.addAdvertiserBid("a2", 110L);
		campaign.addAdvertiserBid("a3", 80L);
		campaign.auction();
		assertEquals(campaign.getAdvertiser(),"a3");
		assertEquals(campaign.getBudget(), 100L, 0);
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


	void multiImpress(Campaign cpgn, boolean targeted, long costMillis, int multi) {
		for (int i=0; i < multi; i++)
			if (targeted)
				cpgn.impress(targetSegment, false, false, costMillis);
			else
				cpgn.impress(nonTargetSegment, false, false, costMillis);
	}
	
	
	@Test
	public void testQualityScore() {
		CampaignStats dstats;
		
		campaign.addAdvertiserBid("a1", 80L);
		campaign.addAdvertiserBid("a2", 50L);
		campaign.addAdvertiserBid("a3", 120L);
		campaign.auction();
		assertEquals(campaign.getAdvertiser(),"a2");
		assertEquals(campaign.getBudget(), 80L, 0);
		assertFalse(campaign.isActive());
				
		for (int day = dayStart; day <= dayEnd; day ++) {
			campaign.nextTimeUnit(day);
			assertTrue(campaign.isActive());
			multiImpress(campaign,true,500,10);
			multiImpress(campaign,false,300,3);
		}
		
		campaign.nextTimeUnit(dayEnd+1);
		assertFalse(campaign.isActive());
		
		dstats = campaign.getStats(dayStart, dayEnd);
		assertEquals(dstats.tartgetedImps, 80, 0);
		assertEquals(dstats.otherImps, 24, 0);
		assertEquals(dstats.cost, 47.2, 0.01);

		assertEquals(qualityMgr.getQualityScore("a2"), 0.82 , 0.01);

	}

}
