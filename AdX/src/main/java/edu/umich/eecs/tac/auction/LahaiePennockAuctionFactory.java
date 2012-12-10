/*
 * LahaiePennockAuctionFactory.java
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

import static edu.umich.eecs.tac.auction.AuctionUtils.generalizedSecondPrice;
import static edu.umich.eecs.tac.auction.AuctionUtils.hardSort;

import java.util.logging.Logger;

import tau.tac.adx.props.AdLink;
import edu.umich.eecs.tac.props.Auction;
import edu.umich.eecs.tac.props.Pricing;
import edu.umich.eecs.tac.props.PublisherInfo;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.Ranking;
import edu.umich.eecs.tac.props.ReserveInfo;
import edu.umich.eecs.tac.props.SlotInfo;
import edu.umich.eecs.tac.util.config.ConfigProxy;

/**
 * @author Patrick Jordan, Lee Callender
 */

public class LahaiePennockAuctionFactory implements AuctionFactory {

	private BidManager bidManager;

	private PublisherInfo publisherInfo;

	private SlotInfo slotInfo;

	private ReserveInfo reserveInfo;

	private Logger log = Logger.getLogger(LahaiePennockAuctionFactory.class
			.getName());

	public Auction runAuction(Query query) {
		String[] advertisers = bidManager.advertisers().toArray(new String[0]);
		double[] qualityScores = new double[advertisers.length];
		double[] bids = new double[advertisers.length];
		double[] scores = new double[advertisers.length];
		double[] weight = new double[advertisers.length];
		boolean[] promoted = new boolean[advertisers.length];
		AdLink[] ads = new AdLink[advertisers.length];
		int[] indices = new int[advertisers.length];
		double[] cpc = new double[advertisers.length];

		for (int i = 0; i < advertisers.length; i++) {
			bids[i] = bidManager.getBid(advertisers[i], query);
			qualityScores[i] = bidManager.getQualityScore(advertisers[i], query);
			ads[i] = bidManager.getAdLink(advertisers[i], query);
			weight[i] = Math.pow(qualityScores[i], publisherInfo.getSquashingParameter());
			scores[i] = weight[i] * bids[i];
			indices[i] = i;
			// log.finest("Advertiser: "+advertisers[i]+"\tScore: "+scores[i]);
		}

		// This currently runs for an infinite loop if scores are NaN
		hardSort(scores, indices);

		generalizedSecondPrice(indices, weight, bids, cpc, promoted, slotInfo
				.getPromotedSlots(), reserveInfo.getPromotedReserve(query.getType()), slotInfo
				.getRegularSlots(), reserveInfo.getRegularReserve(query.getType()));

		Ranking ranking = new Ranking();
		Pricing pricing = new Pricing();

		for (int i = 0; i < indices.length && i < slotInfo.getRegularSlots(); i++) {
			if (ads[indices[i]] != null && !Double.isNaN(cpc[indices[i]])) {
				AdLink ad = ads[indices[i]];
				double price = cpc[indices[i]];

				pricing.setPrice(ad, price);

				ranking.add(ad, promoted[indices[i]]);
			}
		}

		ranking.lock();
		pricing.lock();

		Auction auction = new Auction();
		auction.setQuery(query);
		auction.setPricing(pricing);
		auction.setRanking(ranking);

		auction.lock();

		return auction;
	}

	public void configure(ConfigProxy configProxy) {

	}

	public BidManager getBidManager() {
		return bidManager;
	}

	public void setBidManager(BidManager bidManager) {
		this.bidManager = bidManager;
	}

	public PublisherInfo getPublisherInfo() {
		return publisherInfo;
	}

	public void setPublisherInfo(PublisherInfo publisherInfo) {
		this.publisherInfo = publisherInfo;
	}

	public SlotInfo getSlotInfo() {
		return slotInfo;
	}

	public void setSlotInfo(SlotInfo slotInfo) {
		this.slotInfo = slotInfo;
	}

	public ReserveInfo getReserveInfo() {
		return reserveInfo;
	}

	public void setReserveInfo(ReserveInfo reserveInfo) {
		this.reserveInfo = reserveInfo;
	}
}
