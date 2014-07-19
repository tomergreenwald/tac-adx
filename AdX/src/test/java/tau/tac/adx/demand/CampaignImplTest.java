/**
 * 
 */
package tau.tac.adx.demand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReport;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReportEntry;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReportKey;

/**
 * @author greenwald
 * 
 */
public class CampaignImplTest {

	/**
	 * Test method for {@link tau.tac.adx.demand.CampaignImpl#auction()}.
	 */
	@Test
	public void testAuction() {
		int loops = 10000;
		int randomAllocations = 0;
		int noAuctions = 0;
		for (int loopCounter = 0; loopCounter < loops; loopCounter++) {
			// set up
			QualityManager qualityManager = mock(QualityManager.class);
			int expectedReach = (int) (10000 * Math.random());
			String advertiserPrefix = "adv";
			double[] qualityRatings = new double[8];
			long[] bids = new long[8];
			double[] effectiveBids = new double[8];

			double bestEffectiveBid = Double.NEGATIVE_INFINITY;

			CampaignImpl campaignImpl = new CampaignImpl(qualityManager,
					expectedReach, 0, 0, null, 0, 0);
			for (int i = 0; i < 8; i++) {
				qualityRatings[i] = Math.random();
				when(qualityManager.getQualityScore(advertiserPrefix + i))
						.thenReturn(qualityRatings[i]);
				bids[i] = (long) (expectedReach * Math.random());
				effectiveBids[i] = qualityRatings[i] / bids[i];
				boolean addedSuccesfully = campaignImpl.addAdvertiserBid(
						advertiserPrefix + i, bids[i]);
				assertEquals(bids[i] >= expectedReach * 0.1 / qualityRatings[i]
						&& bids[i] <= expectedReach * qualityRatings[i],
						addedSuccesfully);
				if (addedSuccesfully) {
					if (effectiveBids[i] > bestEffectiveBid) {
						bestEffectiveBid = effectiveBids[i];
					}
				}
			}
			// run method
			CampaignAuctionReport auctionReport = campaignImpl.auction();

			// validate
			if (bestEffectiveBid == Double.NEGATIVE_INFINITY) {
				assertNull(auctionReport);
				noAuctions++;
			} else {
				if (!auctionReport.isRandomAllocation()) {
					CampaignAuctionReportEntry entry = auctionReport
							.getEntry(new CampaignAuctionReportKey(
									auctionReport.getWinner()));
					// verify auction winner had the best offer (if two or more had the
					// same offer it doesn't matter who wins)
					assertEquals(bestEffectiveBid, entry.getEffctiveBid(), 0.0);
				} else {
					randomAllocations++;
				}
				// Verify report
				for (CampaignAuctionReportKey key : auctionReport) {
					CampaignAuctionReportEntry reportEntry = auctionReport
							.getEntry(key);
					int index = Character.getNumericValue(key.getAdnetName()
							.charAt(key.getAdnetName().length() - 1));
					assertEquals(bids[index], reportEntry.getActualBid(), 0.0);
					assertEquals(effectiveBids[index],
							reportEntry.getEffctiveBid(), 0.0);
				}
			}
		}
		// validate that 0.3 of the allocations were randomly chosen.
		assertEquals(0.3, 1.0 * randomAllocations / (loops - noAuctions), 0.01);
	}
}
