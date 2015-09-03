/*
 * AdxBidManagerImplTest.java
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
package tau.tac.adx.auction.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import tau.tac.adx.auction.tracker.AdxBidTracker;
import tau.tac.adx.auction.tracker.AdxSpendTracker;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.generators.AdxQueryGenerator;
import tau.tac.adx.util.Utils;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.Product;

/**
 * @author Patrick Jordan, Lee Callender
 */
@RunWith(JMock.class)
public class AdxBidManagerImplTest {
	private Mockery context;

	private String alice;
	private String bob;
	private String eve;

	private AdxQuery q1;
	private AdxQuery q2;
	private AdxQuery q3;

	private double q1Bid;
	private double q2Bid;
	private double q3Bid;

	private Ad adAlice;
	private Ad adBob;
	private Ad adEve;

	private AdxBidTracker AdxBidTracker;
	private AdxSpendTracker AdxSpendTracker;
	private AdxBidManagerImpl AdxBidManager;

	@Before
	public void setUp() {
		context = new JUnit4Mockery();

		alice = "alice";
		bob = "bob";
		eve = "eve";
		AdxQueryGenerator generator = Utils.getInjector().getInstance(
				AdxQueryGenerator.class);
		Collection<AdxQuery> queries = generator.generate(3);
		Iterator<AdxQuery> iterator = queries.iterator();
		q1 = iterator.next();
		q2 = iterator.next();
		q3 = iterator.next();

		q1Bid = 0.50;
		q2Bid = 0.75;
		q3Bid = 1.00;

		adAlice = new Ad();
		adBob = new Ad(new Product("bob", ""));
		adEve = new Ad(new Product("eve", ""));

		AdxBidTracker = context.mock(AdxBidTracker.class);
		AdxSpendTracker = context.mock(AdxSpendTracker.class);

		AdxBidManager = new AdxBidManagerImpl(AdxBidTracker, AdxSpendTracker);
	}

	@Test
	public void testConstructor() {
		assertNotNull(AdxBidManager);
	}

	@Test(expected = NullPointerException.class)
	public void testUserAdxBidTrackerNull() {
		assertNotNull(new AdxBidManagerImpl(null, AdxSpendTracker));
	}

	@Test(expected = NullPointerException.class)
	public void testUserAdxSpendTrackerNull() {
		assertNotNull(new AdxBidManagerImpl(AdxBidTracker, null));
	}

	// @Test
	// public void testGetBid() {
	// context.checking(new Expectations() {
	// {
	// atLeast(1).of(AdxSpendTracker).getDailyCost(alice);
	// will(returnValue(0.0));
	// atLeast(1).of(AdxSpendTracker).getDailyCost(alice, q1);
	// will(returnValue(0.0));
	// atLeast(1).of(AdxSpendTracker).getDailyCost(alice, q2);
	// will(returnValue(0.0));
	// atLeast(1).of(AdxSpendTracker).getDailyCost(alice, q3);
	// will(returnValue(0.0));
	//
	// atLeast(1).of(AdxSpendTracker).getDailyCost(bob);
	// will(returnValue(0.0));
	// atLeast(1).of(AdxSpendTracker).getDailyCost(bob, q1);
	// will(returnValue(0.0));
	// atLeast(1).of(AdxSpendTracker).getDailyCost(bob, q2);
	// will(returnValue(0.0));
	// atLeast(1).of(AdxSpendTracker).getDailyCost(bob, q3);
	// will(returnValue(0.0));
	//
	// atLeast(1).of(AdxBidTracker).getBid(alice, q1);
	// will(returnValue(q1Bid));
	// atLeast(1).of(AdxBidTracker).getBid(alice, q2);
	// will(returnValue(q2Bid));
	// atLeast(1).of(AdxBidTracker).getBid(alice, q3);
	// will(returnValue(q3Bid));
	// atLeast(0).of(AdxBidTracker).getDailySpendLimit(alice, q1);
	// will(returnValue(0.75));
	// atLeast(0).of(AdxBidTracker).getDailySpendLimit(alice, q2);
	// will(returnValue(0.75));
	// atLeast(0).of(AdxBidTracker).getDailySpendLimit(alice, q3);
	// will(returnValue(0.75));
	// atLeast(0).of(AdxBidTracker).getDailySpendLimit(alice);
	// will(returnValue(1.00));
	//
	// atLeast(1).of(AdxBidTracker).getBid(bob, q1);
	// will(returnValue(q1Bid));
	// atLeast(1).of(AdxBidTracker).getBid(bob, q2);
	// will(returnValue(q2Bid));
	// atLeast(1).of(AdxBidTracker).getBid(bob, q3);
	// will(returnValue(q3Bid));
	// atLeast(0).of(AdxBidTracker).getDailySpendLimit(bob, q1);
	// will(returnValue(1.00));
	// atLeast(0).of(AdxBidTracker).getDailySpendLimit(bob, q2);
	// will(returnValue(1.00));
	// atLeast(0).of(AdxBidTracker).getDailySpendLimit(bob, q3);
	// will(returnValue(1.00));
	// atLeast(0).of(AdxBidTracker).getDailySpendLimit(bob);
	// will(returnValue(0.75));
	//
	// }
	// });
	//
	// // Normal bid
	// assertEquals(AdxBidManager.getBid(alice, q1), q1Bid, 0.000000001);
	//
	// // AdxQuery overspent bid
	// assertEquals(AdxBidManager.getBid(alice, q2), 0.0, 0.000000001);
	//
	// // Global overspent bid
	// assertEquals(AdxBidManager.getBid(alice, q3), 0.0, 0.000000001);
	//
	// // Normal bid
	// assertEquals(AdxBidManager.getBid(bob, q1), q1Bid, 0.000000001);
	//
	// // Global overspent bid
	// assertEquals(AdxBidManager.getBid(bob, q2), 0.0, 0.000000001);
	//
	// // AdxQuery overspent bid
	// assertEquals(AdxBidManager.getBid(bob, q3), 0.0, 0.000000001);
	// }

	@Test
	public void testNextTimeUnit() {
		final AdxBidBundle AdxBidBundle = new AdxBidBundle();

		context.checking(new Expectations() {
			{
				atLeast(1).of(AdxBidTracker).updateBids(alice, AdxBidBundle);
			}
		});

		AdxBidManager.updateBids(alice, AdxBidBundle);
		// AdxBidManager.nextTimeUnit(0);
		AdxBidManager.applyBidUpdates();
	}

	@Test
	public void testAddAdvertisers() {
		context.checking(new Expectations() {
			{
				oneOf(AdxBidTracker).addAdvertiser(alice);
				oneOf(AdxBidTracker).addAdvertiser(bob);
				oneOf(AdxBidTracker).addAdvertiser(eve);

				oneOf(AdxSpendTracker).addAdvertiser(alice);
				oneOf(AdxSpendTracker).addAdvertiser(bob);
				oneOf(AdxSpendTracker).addAdvertiser(eve);
			}
		});

		AdxBidManager.addAdvertiser(alice);
		AdxBidManager.addAdvertiser(bob);
		AdxBidManager.addAdvertiser(eve);

		Set<String> advertisers = new HashSet<String>();
		advertisers.add(alice);
		advertisers.add(bob);
		advertisers.add(eve);

		assertEquals(advertisers, AdxBidManager.advertisers());

	}

	// @Test
	// public void testAdLink() {
	// final AdLink aliceLink = new AdLink(adAlice.getProduct(), alice);
	// final AdLink bobLink = new AdLink(adBob.getProduct(), bob);
	// final AdLink eveLink = new AdLink(adEve.getProduct(), eve);
	//
	// context.checking(new Expectations() {
	// {
	// oneOf(AdxBidTracker).getAdLink(alice, q1);
	// will(returnValue(aliceLink));
	// oneOf(AdxBidTracker).getAdLink(bob, q2);
	// will(returnValue(bobLink));
	// oneOf(AdxBidTracker).getAdLink(eve, q3);
	// will(returnValue(eveLink));
	// }
	// });
	//
	// assertEquals(aliceLink, AdxBidManager.getAdLink(alice, q1));
	// assertEquals(bobLink, AdxBidManager.getAdLink(bob, q2));
	// assertEquals(eveLink, AdxBidManager.getAdLink(eve, q3));
	// }
}
