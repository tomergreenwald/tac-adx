/*
 * LahaiePennockAuctionFactoryTest.java
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import tau.tac.adx.props.AdLink;

import com.botbox.util.ArrayUtils;

import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.Auction;
import edu.umich.eecs.tac.props.BidBundle;
import edu.umich.eecs.tac.props.PublisherInfo;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.Ranking;
import edu.umich.eecs.tac.props.ReserveInfo;
import edu.umich.eecs.tac.props.SlotInfo;
import edu.umich.eecs.tac.util.config.ConfigProxy;

/**
 * @author Lee Callender
 */
public class LahaiePennockAuctionFactoryTest {
	private LahaiePennockAuctionFactory auctionFactory;
	private BidManager bidManager;
	private SlotInfo slotInfo;
	private ReserveInfo reserveInfo;
	private PublisherInfo publisherInfo;

	@Before
	public void setUp() {
		String[] advertisers = { "alice", "bob", "cathy", "don", "eve" };
		double[] bids = { 0.0, 0.25, 0.5, 0.75, 1.0 };
		double[] qualityScore = { 1.0, 1.0, 1.0, 1.0, 1.0 };

		bidManager = new SimpleBidManager(advertisers, bids, qualityScore);

		slotInfo = new SlotInfo();
		slotInfo.setRegularSlots(4);
		reserveInfo = new ReserveInfo();

		publisherInfo = new PublisherInfo();
		publisherInfo.setSquashingParameter(0.8);

		auctionFactory = new LahaiePennockAuctionFactory();
		auctionFactory.setBidManager(bidManager);
		auctionFactory.setSlotInfo(slotInfo);
		auctionFactory.setReserveInfo(reserveInfo);
		auctionFactory.setPublisherInfo(publisherInfo);
	}

	@Test
	public void testConstructor() {
		assertNotNull(auctionFactory);
	}

	@Test
	public void testGetSet() {
		auctionFactory.setBidManager(bidManager);
		auctionFactory.setSlotInfo(slotInfo);
		assertEquals(auctionFactory.getSlotInfo(), slotInfo);
		assertEquals(auctionFactory.getReserveInfo(), reserveInfo);
		assertEquals(auctionFactory.getBidManager(), bidManager);
		assertEquals(auctionFactory.getPublisherInfo(), publisherInfo);
	}

	@Test
	public void testConfigure() {
		auctionFactory.configure(new SimpleConfigProxy());
	}

	@Test
	public void testAuctions() {
		// Base auctionFactory
		Auction auction = auctionFactory
				.runAuction(new Query("apples", "seeds"));
		assertNotNull(auction);
		Ranking ranking = auction.getRanking();
		assertNotNull(ranking);
		assertEquals(ranking.size(), 4, 0);
		assertEquals(ranking.get(0), bidManager.getAdLink("eve", null));

		// Fewer participants than slots available
		SlotInfo ac = auctionFactory.getSlotInfo();
		ac.setRegularSlots(6);
		auction = auctionFactory.runAuction(new Query("apples", "seeds"));
		assertNotNull(ranking);
		ranking = auction.getRanking();
		assertNotNull(ranking);
		assertEquals(ranking.size(), 5, 0);
		assertEquals(ranking.get(0), bidManager.getAdLink("eve", null));
	}

	private class SimpleBidManager implements BidManager {
		private int size;
		private String[] advertisers;
		private double[] bids;
		private double[] qualityScore;
		private Set<String> setAdv;

		private final static double defaultBid = 0.0;
		private final static double defaultQuality = 1.0;

		public SimpleBidManager(String[] advertisers) {
			this.advertisers = advertisers.clone();
			size = this.advertisers.length;
			bids = new double[size];
			qualityScore = new double[size];

			for (int i = 0; i < size; i++) {
				bids[i] = defaultBid;
				qualityScore[i] = defaultQuality;
			}

			List list = Arrays.asList(advertisers);
			setAdv = new HashSet<String>(list);
		}

		public SimpleBidManager(String[] advertisers, double[] bids,
				double[] qualityScore) {
			this.advertisers = advertisers.clone();
			this.bids = bids.clone();
			this.qualityScore = qualityScore.clone();

			size = this.advertisers.length;

			List<String> list = Arrays.asList(advertisers);
			setAdv = new HashSet<String>(list);
		}

		public void addAdvertiser(String advertiser) {
		}

		public void setBid(String advertiser, double bid) {
			int index = ArrayUtils.indexOf(advertisers, 0, size, advertiser);
			bids[index] = bid;
		}

		public void setQualityScore(String advertiser, double quality) {
			int index = ArrayUtils.indexOf(advertisers, 0, size, advertiser);
			qualityScore[index] = quality;
		}

		public double getBid(String advertiser, Query query) {

			return bids[ArrayUtils.indexOf(advertisers, 0, size, advertiser)];
		}

		public double getQualityScore(String advertiser, Query query) {
			return qualityScore[ArrayUtils.indexOf(advertisers, 0, size,
					advertiser)];
		}

		public AdLink getAdLink(String advertiser, Query query) {
			AdLink returnme = new AdLink((Ad) null, advertiser);
			return returnme;
		}

		public void updateBids(String advertiser, BidBundle bundle) {
		}

		public Set<String> advertisers() {
			return setAdv;
		}

		public void nextTimeUnit(int i) {
		}

    public void applyBidUpdates(){}

  }

	private class SimpleConfigProxy implements ConfigProxy {
		public String getProperty(String name) {
			return null;
		}

		public String getProperty(String name, String defaultValue) {
			return null;
		}

		public String[] getPropertyAsArray(String name) {
			return null;
		}

		public String[] getPropertyAsArray(String name, String defaultValue) {
			return null;
		}

		public int getPropertyAsInt(String name, int defaultValue) {
			return 0;
		}

		public int[] getPropertyAsIntArray(String name) {
			return null;
		}

		public int[] getPropertyAsIntArray(String name, String defaultValue) {
			return null;
		}

		public long getPropertyAsLong(String name, long defaultValue) {
			return defaultValue;
		}

		public float getPropertyAsFloat(String name, float defaultValue) {
			return defaultValue;
		}

		public double getPropertyAsDouble(String name, double defaultValue) {
			return defaultValue;
		}
	}
}
