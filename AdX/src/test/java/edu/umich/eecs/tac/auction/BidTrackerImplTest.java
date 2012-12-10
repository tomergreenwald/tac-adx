/*
 * BidTrackerImplTest.java
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
package edu.umich.eecs.tac.auction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import tau.tac.adx.props.AdLink;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.BidBundle;
import edu.umich.eecs.tac.props.Query;

/**
 * @author Patrick Jordan
 */
public class BidTrackerImplTest {

	@Test
	public void testConstructor() {
		BidTrackerImpl bidTracker = new BidTrackerImpl();

		assertNotNull(bidTracker);
	}

	@Test
	public void testAddAdvertiser() {
		BidTrackerImpl bidTracker = new BidTrackerImpl();

		String advertiser = "Alice";

		assertEquals(bidTracker.size(), 0, 0);
		bidTracker.addAdvertiser(advertiser);
		assertEquals(bidTracker.size(), 1, 0);
		bidTracker.addAdvertiser(advertiser);
		assertEquals(bidTracker.size(), 1, 0);

		for (int i = 0; i < 8; i++) {
			bidTracker.addAdvertiser("" + i);
			assertEquals(bidTracker.size(), i + 2, 0);
		}
	}

	@Test
	public void testInitializeQuerySpace() {
		BidTrackerImpl bidTracker = new BidTrackerImpl();

		Set<Query> querySpace = new HashSet<Query>();

		bidTracker.initializeQuerySpace(querySpace);

		bidTracker.initializeQuerySpace(querySpace);

	}

	@Test
	public void testGetDailySpendLimits() {
		BidTrackerImpl bidTracker = new BidTrackerImpl();

		String advertiser = "alice";
		Query query = new Query();

		assertEquals(bidTracker.getDailySpendLimit(advertiser), Double.POSITIVE_INFINITY, 0.0);
		assertEquals(bidTracker.getDailySpendLimit(advertiser, query), Double.POSITIVE_INFINITY, 0.0);

		bidTracker.addAdvertiser(advertiser);

		assertEquals(bidTracker.getDailySpendLimit(advertiser), Double.POSITIVE_INFINITY, 0.0);
		assertEquals(bidTracker.getDailySpendLimit(advertiser), Double.POSITIVE_INFINITY, 0.0);

		advertiser = "bob";
		bidTracker.addAdvertiser(advertiser);
		assertEquals(bidTracker.getDailySpendLimit(advertiser, query), Double.POSITIVE_INFINITY, 0.0);
		assertEquals(bidTracker.getDailySpendLimit(advertiser, query), Double.POSITIVE_INFINITY, 0.0);
	}

	@Test
	public void testGetBids() {
		BidTrackerImpl bidTracker = new BidTrackerImpl();

		String advertiser = "alice";
		Query query = new Query();

		assertEquals(bidTracker.getBid(advertiser, query), 0.0, 0.0);

		bidTracker.addAdvertiser(advertiser);

		assertEquals(bidTracker.getBid(advertiser, query), 0.0, 0.0);
		assertEquals(bidTracker.getBid(advertiser, query), 0.0, 0.0);
	}

	@Test
	public void testGetAdLink() {
		BidTrackerImpl bidTracker = new BidTrackerImpl();

		String advertiser = "alice";
		Query query = new Query();

		AdLink adLink = new AdLink(new Ad(), advertiser);

		assertEquals(bidTracker.getAdLink(advertiser, query), adLink);

		bidTracker.addAdvertiser(advertiser);

		assertEquals(bidTracker.getAdLink(advertiser, query), adLink);
		assertEquals(bidTracker.getAdLink(advertiser, query), adLink);
	}

	@Test
	public void testUpdateBids() {
		BidTrackerImpl bidTracker = new BidTrackerImpl();

		Set<Query> querySpace = new HashSet<Query>();
		Query query1 = new Query("1", "");
		Query query2 = new Query("2", "");
		Query query3 = new Query("3", "");

		querySpace.add(query1);
		querySpace.add(query2);
		querySpace.add(query3);

		bidTracker.initializeQuerySpace(querySpace);

		String advertiser1 = "alice";
		BidBundle bundle1 = new BidBundle();

		bidTracker.updateBids(advertiser1, bundle1);
		bidTracker.updateBids(advertiser1, bundle1);

		String advertiser2 = "bob";
		BidBundle bundle2 = new BidBundle();

		bundle2.setBid(query1, 1.0);
		bundle2.setCampaignDailySpendLimit(1.0);

		bundle2.setAd(query2, new Ad());

		bundle2.setDailyLimit(query3, 1.0);

		bidTracker.addAdvertiser(advertiser2);
		bidTracker.updateBids(advertiser2, bundle2);

		assertEquals(bidTracker.getBid(advertiser2, query1), 1.0, 0.0);
		assertEquals(bidTracker.getAdLink(advertiser2, query2), new AdLink(new Ad(), advertiser2));
		assertEquals(bidTracker.getDailySpendLimit(advertiser2, query3), 1.0,0.0);

		bidTracker.updateBids(advertiser2, bundle2);
		assertEquals(bidTracker.getBid(advertiser2, query1), 1.0, 0.0);
		assertEquals(bidTracker.getAdLink(advertiser2, query2), new AdLink(new Ad(), advertiser2));
		assertEquals(bidTracker.getDailySpendLimit(advertiser2, query3), 1.0, 0.0);
	}

}
