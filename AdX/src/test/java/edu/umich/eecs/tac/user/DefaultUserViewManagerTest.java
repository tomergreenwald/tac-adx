/*
 * DefaultUserViewManagerTest.java
 *
 * COPYRIGHT  2008
 * THE REGENTS OF THE UNIVERSITY OF MICHIGAN
 * ALL RIGHTS RESERVED
 *
 * PERMISSION IS GRANTED TO USE, COPY, CREATE DERIVATIVE WORKS AND REDISTRIBUTE THIS
 * SOFTWARE AND SUCH DERIVATIVE WORKS FOR NONCOMMERCIAL EDUCATION AND RESEARCH
 * PURPOSES, SO LONG AS NO FEE IS CHARGED, AND SO LONG AS THE COPYRIGHT NOTICE
 * ABOVE, THIS GRANT OF PERMISSION, AND THE DISCLAIMER BELOW APPEAR IN ALL COPIES
 * MADE; AND SO LONG AS THE NAME OF THE UNIVERSITY OF MICHIGAN IS NOT USED IN ANY
 * ADVERTISING OR PUBLICITY PERTAINING TO THE USE OR DISTRIBUTION OF THIS SOFTWARE
 * WITHOUT SPECIFIC, WRITTEN PRIOR AUTHORIZATION.
 *
 * THIS SOFTWARE IS PROVIDED AS IS, WITHOUT REPRESENTATION FROM THE UNIVERSITY OF
 * MICHIGAN AS TO ITS FITNESS FOR ANY PURPOSE, AND WITHOUT WARRANTY BY THE
 * UNIVERSITY OF MICHIGAN OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT
 * LIMITATION THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE REGENTS OF THE UNIVERSITY OF MICHIGAN SHALL NOT BE LIABLE FOR ANY
 * DAMAGES, INCLUDING SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WITH
 * RESPECT TO ANY CLAIM ARISING OUT OF OR IN CONNECTION WITH THE USE OF THE SOFTWARE,
 * EVEN IF IT HAS BEEN OR IS HEREAFTER ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package edu.umich.eecs.tac.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import tau.tac.adx.props.AdLink;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.Auction;
import edu.umich.eecs.tac.props.Pricing;
import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.Ranking;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.SlotInfo;
import edu.umich.eecs.tac.props.UserClickModel;
import edu.umich.eecs.tac.sim.RecentConversionsTracker;

/**
 * @author Patrick Jordan
 */
@RunWith(JMock.class)
public class DefaultUserViewManagerTest {
	private Mockery context;

	private Random random;

	private RetailCatalog retailCatalog;

	private UserClickModel userClickModel;

	private RecentConversionsTracker recentConversionsTracker;

	private DefaultUserViewManager userViewManager;

	private Map<String, AdvertiserInfo> advertiserInfo;

	private SlotInfo slotInfo;

	private Query query;

	private String alice;

	private String bob;

	private String eve;

	private Product product;

	private String manufacturer;

	private String component;

	@Before
	public void setup() {
		context = new JUnit4Mockery();

		random = new Random(103);

		alice = "alice";
		bob = "bob";
		eve = "eve";

		manufacturer = "man";

		component = "com";

		product = new Product(manufacturer, component);

		query = new Query(manufacturer, component);

		retailCatalog = new RetailCatalog();

		retailCatalog.addProduct(product);

		retailCatalog.setSalesProfit(product, 1.0);

		slotInfo = new SlotInfo();

		recentConversionsTracker = context.mock(RecentConversionsTracker.class);

		AdvertiserInfo aliceInfo = new AdvertiserInfo();

		aliceInfo.setDistributionCapacityDiscounter(0.0);

		AdvertiserInfo bobInfo = new AdvertiserInfo();

		bobInfo.setDistributionCapacityDiscounter(0.0);

		AdvertiserInfo eveInfo = new AdvertiserInfo();

		eveInfo.setDistributionCapacityDiscounter(1.0);
		eveInfo.setFocusEffects(query.getType(), 1.0);
		eveInfo.setManufacturerSpecialty(manufacturer);

		advertiserInfo = new HashMap<String, AdvertiserInfo>();
		advertiserInfo.put(alice, aliceInfo);
		advertiserInfo.put(bob, bobInfo);
		advertiserInfo.put(eve, eveInfo);

		userClickModel = new UserClickModel(new Query[] { query },
				new String[] { alice, bob, eve });
		userClickModel.setContinuationProbability(0, 0.75);
		userClickModel.setAdvertiserEffect(0, 0, 0.0);
		userClickModel.setAdvertiserEffect(0, 1, 1.0);
		userClickModel.setAdvertiserEffect(0, 2, 1.0);

		userViewManager = new DefaultUserViewManager(retailCatalog,
				recentConversionsTracker, advertiserInfo, slotInfo, random);
		userViewManager.setUserClickModel(userClickModel);
	}

	@Test
	public void testConstructors() {
		assertNotNull(userViewManager);
		assertNotNull(new DefaultUserViewManager(retailCatalog,
				recentConversionsTracker, advertiserInfo, slotInfo));
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorRetailCatalogNull() {
		new DefaultUserViewManager(null, recentConversionsTracker,
				advertiserInfo, slotInfo);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorTrackerNull() {
		new DefaultUserViewManager(retailCatalog, null, advertiserInfo,
				slotInfo);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorInfoNull() {
		new DefaultUserViewManager(retailCatalog, recentConversionsTracker,
				null, slotInfo);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorRandomNull() {
		new DefaultUserViewManager(retailCatalog, recentConversionsTracker,
				advertiserInfo, slotInfo, null);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorAuctionInfoNull() {
		new DefaultUserViewManager(retailCatalog, recentConversionsTracker,
				advertiserInfo, null);
	}

	@Test
	public void testUserClickModel() {
		assertSame(userViewManager.getUserClickModel(), userClickModel);
	}

	@Test
	public void testUserEventSupport() {
		UserEventListener listener = context.mock(UserEventListener.class);

		assertTrue(userViewManager.addUserEventListener(listener));
		assertTrue(userViewManager.containsUserEventListener(listener));
		assertTrue(userViewManager.removeUserEventListener(listener));
		assertFalse(userViewManager.containsUserEventListener(listener));
	}

	@Test
	public void testProcessImpression() {
		User user = new User(QueryState.FOCUS_LEVEL_TWO, product);

		AdLink aliceAd = new AdLink(product, alice);
		AdLink bobAd = new AdLink(product, bob);
		AdLink eveAd = new AdLink(product, eve);

		Pricing pricing = new Pricing();
		pricing.setPrice(aliceAd, 1.0);
		pricing.setPrice(bobAd, 0.5);
		pricing.setPrice(eveAd, 0.25);

		Ranking ranking = new Ranking();
		ranking.add(aliceAd);
		ranking.add(bobAd);
		ranking.add(eveAd);

		Auction auction = new Auction();
		auction.setQuery(query);
		auction.setPricing(pricing);
		auction.setRanking(ranking);

		userViewManager.nextTimeUnit(0);

		context.checking(new Expectations() {
			{
				atLeast(1).of(recentConversionsTracker).getRecentConversions(
						bob);
				will(returnValue(0.0));
				atLeast(1).of(recentConversionsTracker).getRecentConversions(
						eve);
				will(returnValue(0.0));
			}
		});

		assertTrue(userViewManager.processImpression(user, query, auction));

		assertFalse(userViewManager.processImpression(user, new Query(),
				auction));
	}

}
