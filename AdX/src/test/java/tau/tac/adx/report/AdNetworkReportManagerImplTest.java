/**
 * 
 */
package tau.tac.adx.report;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;

import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.auction.data.AuctionState;
import tau.tac.adx.bids.BidInfo;
import tau.tac.adx.bids.Bidder;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.messages.AuctionMessage;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.generators.AdxQueryGenerator;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.AdNetworkReportManagerImpl;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.generators.SimpleUserGenerator;
import tau.tac.adx.util.Utils;

/**
 * @author Tomer
 * 
 */
public class AdNetworkReportManagerImplTest {

	/**
	 * Test method for
	 * {@link tau.tac.adx.report.adn.AdNetworkReportManagerImpl#auctionPerformed(tau.tac.adx.messages.AuctionMessage)}
	 * .
	 */
	@Test
	public void testAuctionPerformed() {
		AdNetworkReportManagerImpl managerImpl = new AdNetworkReportManagerImpl(
				null, new EventBus());
		Random random = new Random();
		AuctionState auctionState = AuctionState.values()[random
				.nextInt(AuctionState.values().length - 1)];
		double bidPrice = random.nextDouble();
		int campaignId = (int) (Math.random() * 100);
		int campaignId2 = (int) (Math.random() * 100);
		Campaign campaign = Mockito.mock(Campaign.class);
		Mockito.when(campaign.getId()).thenReturn(campaignId);
		Mockito.when(campaign.getAdvertiser()).thenReturn("1");
		Campaign campaign2 = Mockito.mock(Campaign.class);
		Mockito.when(campaign2.getId()).thenReturn(campaignId2);
		Mockito.when(campaign2.getAdvertiser()).thenReturn("2");
		MarketSegment segment = MarketSegment.values()[random
				.nextInt(MarketSegment.values().length - 1)];
		Double winningPrice = random.nextDouble();
		List<BidInfo> bidInfos = new LinkedList<BidInfo>();
		Bidder bidder = Mockito.mock(Bidder.class, Mockito.RETURNS_DEEP_STUBS);
		Mockito.when(bidder.getName()).thenReturn("1");
		Bidder bidder2 = Mockito.mock(Bidder.class, Mockito.RETURNS_DEEP_STUBS);
		Mockito.when(bidder.getName()).thenReturn("2");
		BidInfo bidInfo = new BidInfo(0, bidder , null, Collections.singleton(segment), campaign);
		BidInfo bidInfo2 = new BidInfo(0, bidder2 , null, Collections.singleton(segment), campaign2);
		bidInfos.add(bidInfo);
		bidInfos.add(bidInfo2);
		BidInfo winningBidInfo = new BidInfo(bidPrice, bidder, null, segment,
				campaign);
		AdxAuctionResult auctionResult = new AdxAuctionResult(auctionState,
				winningBidInfo, winningPrice, bidInfos );
		SimpleUserGenerator userGenerator = new SimpleUserGenerator(random.nextDouble());
		AdxUser user = userGenerator.generate(1).get(0);
		AdxQueryGenerator generator = Utils.getInjector().getInstance(
				AdxQueryGenerator.class);
		AdxQuery query = generator.generate(1).iterator().next();
		AuctionMessage auctionMessage = new AuctionMessage(auctionResult,
				query, user);
		managerImpl.auctionPerformed(auctionMessage);
		Map<String, AdNetworkReport> reports = managerImpl.getAdNetworkReports();
		Assert.assertEquals(campaignId, reports.get(bidder.getName()).getEntry(0).getKey().getCampaignId());
		Assert.assertEquals(campaignId2, reports.get(bidder2.getName()).getEntry(0).getKey().getCampaignId());
	}

}
