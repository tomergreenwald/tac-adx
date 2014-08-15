/**
 * 
 */
package tau.tac.adx.sim;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;

import tau.tac.adx.AdxManager;
import tau.tac.adx.demand.UserClassificationService;
import tau.tac.adx.demand.UserClassificationServiceAdNetData;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.report.adn.MarketSegment;

import com.google.common.eventbus.EventBus;

/**
 * @author Tomer
 * 
 */
public class SimpleAdxAuctioneerTest {

	/**
	 * Test method for
	 * {@link tau.tac.adx.sim.SimpleAdxAuctioneer#getClassifiedQuery(java.lang.String, tau.tac.adx.props.AdxQuery)}
	 * .
	 */
	@Test
	public void testGetClassifiedQuery() {
		EventBus eventBus = new EventBus();
		SimpleAdxAuctioneer auctioneer = new SimpleAdxAuctioneer(null, null,
				eventBus);
		AdxQuery adxQuery = new AdxQuery();
		adxQuery.setMarketSegments(MarketSegment.randomMarketSegment());
		UserClassificationService userClassificationService = mock(UserClassificationService.class);
		UserClassificationServiceAdNetData ucsAdNetData = mock(UserClassificationServiceAdNetData.class);
		String advertiser = "adv1";
		when(userClassificationService.getAdNetData(advertiser)).thenReturn(
				ucsAdNetData);
		AdxManager.getInstance().setUserClassificationService(
				userClassificationService);

		when(ucsAdNetData.getServiceLevel()).thenReturn(1.0);
		AdxQuery classifiedQuery = auctioneer.getClassifiedQuery(advertiser,
				adxQuery);
		assertEquals(adxQuery, classifiedQuery);

		when(ucsAdNetData.getServiceLevel()).thenReturn(0.0);
		classifiedQuery = auctioneer.getClassifiedQuery(advertiser, adxQuery);
		assertEquals(new AdxQuery(), classifiedQuery);

		try {
			classifiedQuery = auctioneer.getClassifiedQuery("adv2", adxQuery);
			Assert.fail("A NullPointerException shoudl have been thrown.");
		} catch (NullPointerException e) {
			// Left blank intentionally
		}

		for (int j = 0; j < 100; j++) {
			double ucsLevel = Math.random();
			when(ucsAdNetData.getServiceLevel()).thenReturn(ucsLevel);
			int fullyClassified = 0;
			int notClassified = 0;
			int rounds = 1000;
			for (int i = 0; i < rounds; i++) {
				classifiedQuery = auctioneer.getClassifiedQuery(advertiser,
						adxQuery);
				if (classifiedQuery.getMarketSegments().size() == 0) {
					notClassified++;
				} else if (classifiedQuery.getMarketSegments().equals(
						adxQuery.getMarketSegments())) {
					fullyClassified++;
				} else {
					Assert.fail("reuslt query has to be fully classified or not at all.");
				}
			}
			assertEquals(ucsLevel, 1.0 * fullyClassified / rounds, 0.06);
			assertEquals(1 - ucsLevel, 1.0 * notClassified / rounds, 0.05);
		}
	}

}
