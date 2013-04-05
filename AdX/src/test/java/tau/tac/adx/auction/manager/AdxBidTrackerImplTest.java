/*
 * AdxBidTrackerImplTest.java
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

import org.junit.Test;

import tau.tac.adx.auction.tracker.AdxBidTrackerImpl;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.generators.AdxQueryGenerator;
import tau.tac.adx.util.Utils;
import edu.umich.eecs.tac.props.Ad;

/**
 * @author Patrick Jordan
 */
public class AdxBidTrackerImplTest {

	@Test
	public void testConstructor() {
		AdxBidTrackerImpl AdxBidTracker = new AdxBidTrackerImpl();

		assertNotNull(AdxBidTracker);
	}

	@Test
	public void testAddAdvertiser() {
		AdxBidTrackerImpl AdxBidTracker = new AdxBidTrackerImpl();

		String advertiser = "Alice";

		assertEquals(AdxBidTracker.size(), 0, 0);
		AdxBidTracker.addAdvertiser(advertiser);
		assertEquals(AdxBidTracker.size(), 1, 0);
		AdxBidTracker.addAdvertiser(advertiser);
		assertEquals(AdxBidTracker.size(), 1, 0);

		for (int i = 0; i < 8; i++) {
			AdxBidTracker.addAdvertiser("" + i);
			assertEquals(AdxBidTracker.size(), i + 2, 0);
		}
	}

	@Test
	public void testInitializeAdxQuerySpace() {
		AdxBidTrackerImpl AdxBidTracker = new AdxBidTrackerImpl();

		Set<AdxQuery> querySpace = new HashSet<AdxQuery>();

		AdxBidTracker.initializeQuerySpace(querySpace);

		AdxBidTracker.initializeQuerySpace(querySpace);

	}

	// TODO: uncomment me
	// @Test
	// public void testGetDailySpendLimits() {
	// AdxBidTrackerImpl AdxBidTracker = new AdxBidTrackerImpl();
	//
	// String advertiser = "alice";
	// AdxQuery query = new AdxQuery();
	//
	// assertEquals(AdxBidTracker.getDailySpendLimit(advertiser),
	// Double.POSITIVE_INFINITY, 0.0);
	// assertEquals(AdxBidTracker.getDailySpendLimit(advertiser, query),
	// Double.POSITIVE_INFINITY, 0.0);
	//
	// AdxBidTracker.addAdvertiser(advertiser);
	//
	// assertEquals(AdxBidTracker.getDailySpendLimit(advertiser),
	// Double.POSITIVE_INFINITY, 0.0);
	// assertEquals(AdxBidTracker.getDailySpendLimit(advertiser),
	// Double.POSITIVE_INFINITY, 0.0);
	//
	// advertiser = "bob";
	// AdxBidTracker.addAdvertiser(advertiser);
	// assertEquals(AdxBidTracker.getDailySpendLimit(advertiser, query),
	// Double.POSITIVE_INFINITY, 0.0);
	// assertEquals(AdxBidTracker.getDailySpendLimit(advertiser, query),
	// Double.POSITIVE_INFINITY, 0.0);
	// }

	@Test
	public void testGetBids() {
		AdxBidTrackerImpl AdxBidTracker = new AdxBidTrackerImpl();

		String advertiser = "alice";
		AdxQuery query = new AdxQuery();

		assertEquals(AdxBidTracker.getBidInfo(advertiser, query), null);

		AdxBidTracker.addAdvertiser(advertiser);

		// assertEquals(AdxBidTracker.getBid(advertiser, query), 0.0, 0.0);
		// assertEquals(AdxBidTracker.getBid(advertiser, query), 0.0, 0.0);
	}

	// @Test
	// public void testGetAdLink() {
	// AdxBidTrackerImpl AdxBidTracker = new AdxBidTrackerImpl();
	//
	// String advertiser = "alice";
	// AdxQuery query = new AdxQuery();
	//
	// AdLink adLink = new AdLink(new Ad(), advertiser);
	//
	// assertEquals(AdxBidTracker.getAdLink(advertiser, query), adLink);
	//
	// AdxBidTracker.addAdvertiser(advertiser);
	//
	// assertEquals(AdxBidTracker.getAdLink(advertiser, query), adLink);
	// assertEquals(AdxBidTracker.getAdLink(advertiser, query), adLink);
	// }

	@Test
	public void testUpdateBids() {
		AdxBidTrackerImpl AdxBidTracker = new AdxBidTrackerImpl();

		Set<AdxQuery> querySpace = new HashSet<AdxQuery>();
		AdxQueryGenerator generator = Utils.getInjector().getInstance(
				AdxQueryGenerator.class);
		Collection<AdxQuery> queries = generator.generate(3);
		Iterator<AdxQuery> iterator = queries.iterator();
		AdxQuery query1 = iterator.next();
		AdxQuery query2 = iterator.next();
		AdxQuery query3 = iterator.next();

		querySpace.add(query1);
		querySpace.add(query2);
		querySpace.add(query3);

		AdxBidTracker.initializeQuerySpace(querySpace);

		String advertiser1 = "alice";
		AdxBidBundle bundle1 = new AdxBidBundle();

		AdxBidTracker.updateBids(advertiser1, bundle1);
		AdxBidTracker.updateBids(advertiser1, bundle1);

		String advertiser2 = "bob";
		AdxBidBundle bundle2 = new AdxBidBundle();

		bundle2.setBid(query1, 1.0);
		bundle2.setCampaignDailySpendLimit(1.0);

		bundle2.setAd(query2, new Ad());

		bundle2.setDailyLimit(query3, 1.0);

		AdxBidTracker.addAdvertiser(advertiser2);
		AdxBidTracker.updateBids(advertiser2, bundle2);

		// assertEquals(AdxBidTracker.getBid(advertiser2, query1), 1.0, 0.0);
		// assertEquals(AdxBidTracker.getAdLink(advertiser2, query2), new
		// AdLink(
		// new Ad(), advertiser2));
		// assertEquals(AdxBidTracker.getDailySpendLimit(advertiser2, query3),
		// 1.0, 0.0);
		//
		// AdxBidTracker.updateBids(advertiser2, bundle2);
		// assertEquals(AdxBidTracker.getBid(advertiser2, query1), 1.0, 0.0);
		// assertEquals(AdxBidTracker.getAdLink(advertiser2, query2), new
		// AdLink(
		// new Ad(), advertiser2));
		// assertEquals(AdxBidTracker.getDailySpendLimit(advertiser2, query3),
		// 1.0, 0.0);
	}

}
