package tau.tac.adx.auction.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Test;

import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.auction.tracker.AdxSpendTrackerImpl;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.report.adn.MarketSegment;

public class AdxSpendTrackerImplTest {

	@Test
	public void testConstructor() {
		AdxSpendTrackerImpl spendTracker = new AdxSpendTrackerImpl();

		assertNotNull(spendTracker);
	}

	@Test
	public void testAddAdvertiser() {
		AdxSpendTrackerImpl spendTracker = new AdxSpendTrackerImpl();

		String advertiser = "Alice";

		assertEquals(spendTracker.size(), 0, 0);
		spendTracker.addAdvertiser(advertiser);
		assertEquals(spendTracker.size(), 1, 0);
		spendTracker.addAdvertiser(advertiser);
		assertEquals(spendTracker.size(), 1, 0);

		for (int i = 0; i < 8; i++) {
			spendTracker.addAdvertiser("" + i);
			assertEquals(spendTracker.size(), i + 2, 0);
		}
	}

	@Test
	public void testAddCost() {
		AdxSpendTrackerImpl spendTracker = new AdxSpendTrackerImpl();

		String advertiser = "Alice";
		AdxQuery query = new AdxQuery("pub",
				MarketSegment.FEMALE,
				Device.mobile, AdType.text);
		double cost = 1.0;

		spendTracker.addCost(advertiser, query, cost);

		assertEquals(spendTracker.getDailyCost(advertiser), cost, 0.0);
		assertEquals(spendTracker.getDailyCost(advertiser, query), cost, 0.0);
		assertEquals(spendTracker.size(), 1, 0);

		spendTracker.addCost(advertiser, query, cost);

		assertEquals(spendTracker.getDailyCost(advertiser), 2 * cost, 0.0);
		assertEquals(spendTracker.getDailyCost(advertiser, query), 2 * cost,
				0.0);
		assertEquals(spendTracker.size(), 1, 0);

		assertEquals(spendTracker.getDailyCost("notAlice"), 0.0, 0.0);
		assertEquals(spendTracker.getDailyCost(advertiser, new AdxQuery("a",
				MarketSegment.FEMALE,
				null, null)), 0.0, 0.0);
		assertEquals(spendTracker.getDailyCost("notAlice", query), 0.0, 0.0);

		spendTracker.addAdvertiser("bob");
		assertEquals(spendTracker.getDailyCost("bob", query), 0.0, 0.0);

		spendTracker.addAdvertiser("bobbob");
		assertEquals(spendTracker.getDailyCost("bobbob"), 0.0, 0.0);

		spendTracker.reset();
		assertEquals(spendTracker.getDailyCost(advertiser), 0.0, 0.0);

		for (int i = 0; i < 8; i++) {
			spendTracker.addCost(advertiser, new AdxQuery(), cost);
		}
	}
}
