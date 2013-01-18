/*
 * BidTrackerImpl.java
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
package tau.tac.adx.auction.tracker;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import tau.tac.adx.bids.BidInfo;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.AdxQuery;

import com.botbox.util.ArrayUtils;
import com.google.inject.Inject;

import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.util.sampling.WheelSampler;

/**
 * @author Patrick Jordan
 */
public class AdxBidTrackerImpl implements AdxBidTracker {
	private final static double DEFAULT_SPEND_LIMIT = Double.POSITIVE_INFINITY;
	private final static double DEFAULT_BID = 0.0;
	private final static Ad DEFAULT_AD = new Ad();

	private final Logger logger = Logger.getLogger(AdxBidTrackerImpl.class
			.getName());

	private String[] advertisers;
	private int advertisersCount;
	private AdxQueryBid[] queryBid;
	private AdxQuery[] querySpace;

	@Inject
	public AdxBidTrackerImpl() {
		this(0);
	}

	public AdxBidTrackerImpl(int advertisersCount) {
		this.advertisersCount = advertisersCount;
		advertisers = new String[advertisersCount];
		queryBid = new AdxQueryBid[advertisersCount];
	}

	@Override
	public void initializeQuerySpace(Set<AdxQuery> space) {
		if (querySpace == null) {
			querySpace = space.toArray(new AdxQuery[0]);
		} else {
			logger.warning("Attempt to re-initialize query space");
		}
	}

	private synchronized int doAddAdvertiser(String advertiser) {
		if (advertisersCount == advertisers.length) {
			int newSize = advertisersCount + 8;
			advertisers = (String[]) ArrayUtils.setSize(advertisers, newSize);
			queryBid = (AdxQueryBid[]) ArrayUtils.setSize(queryBid, newSize);
		}

		advertisers[advertisersCount] = advertiser;

		return advertisersCount++;
	}

	@Override
	public void addAdvertiser(String advertiser) {
		int index = ArrayUtils.indexOf(advertisers, 0, advertisersCount,
				advertiser);
		if (index < 0) {
			doAddAdvertiser(advertiser);
		}
	}

	@Override
	public double getDailySpendLimit(String advertiser) {
		int index = ArrayUtils.indexOf(advertisers, 0, advertisersCount,
				advertiser);

		if (index < 0) {
			return DEFAULT_SPEND_LIMIT;
		}

		if (queryBid[index] == null) {
			queryBid[index] = new AdxQueryBid(advertiser, 0);
		}

		return queryBid[index].getCampaignSpendLimit();
	}

	// TODO:uncomment me
	// @Override
	// public double getDailySpendLimit(String advertiser, AdxQuery query) {
	// int index = ArrayUtils.indexOf(advertisers, 0, advertisersCount,
	// advertiser);
	//
	// if (index < 0) {
	// return DEFAULT_SPEND_LIMIT;
	// }
	//
	// if (queryBid[index] == null) {
	// queryBid[index] = new AdxQueryBid(advertiser, 0);
	// }
	//
	// return queryBid[index].getSpendLimits(query);
	// }

	@Override
	public BidInfo getBidInfo(String advertiser, AdxQuery query) {
		int index = ArrayUtils.indexOf(advertisers, 0, advertisersCount,
				advertiser);

		if (index < 0) {
			return null;
		}

		if (queryBid[index] == null) {
			queryBid[index] = new AdxQueryBid(advertiser, 0);
		}

		return queryBid[index].generateBid(query);
	}

	// @Override
	// public AdLink getAdLink(String advertiser, AdxQuery query) {
	// int index = ArrayUtils.indexOf(advertisers, 0, advertisersCount,
	// advertiser);
	//
	// if (index < 0) {
	// return new AdLink(DEFAULT_AD, advertiser);
	// }
	//
	// if (queryBid[index] == null) {
	// queryBid[index] = new AdxQueryBid(advertiser, 0);
	// }
	//
	// return queryBid[index].getAdLink(query);
	// }

	@Override
	public void updateBids(String advertiser, AdxBidBundle bundle) {
		int index = ArrayUtils.indexOf(advertisers, 0, advertisersCount,
				advertiser);

		if (index < 0) {
			index = doAddAdvertiser(advertiser);
		}

		if (queryBid[index] == null) {
			queryBid[index] = new AdxQueryBid(advertiser, 0);
		}

		if (!Double.isNaN(bundle.getCampaignDailySpendLimit())) {
			queryBid[index].setCampaignSpendLimit(bundle
					.getCampaignDailySpendLimit());
		}

		for (AdxQuery query : querySpace) {
			BidInfo bidInfo = bundle.getBidInfo(query);
			if (bidInfo != null) {
				queryBid[index].doAddQuery(bidInfo, query);
			}

			// TODO: uncomment me
			// if (dailyLimit >= 0.0) {
			// queryBid[index].setSpendLimit(query, dailyLimit);
			// }
		}
	}

	@Override
	public int size() {
		return advertisersCount;
	}

	private static class AdxQueryBid {
		private final String advertiser;
		private final double[] spendLimits;
		private WheelSampler<Map<AdxQuery, BidInfo>> bidsSampler;
		Map<Campaign, Map<AdxQuery, BidInfo>> campaignMap;
		private final int queryCount;
		private double campaignSpendLimit;

		public AdxQueryBid(String advertiser, int queryCount) {
			this.advertiser = advertiser;
			spendLimits = new double[queryCount];
			bidsSampler = new WheelSampler<Map<AdxQuery, BidInfo>>();
			this.queryCount = queryCount;
			campaignSpendLimit = DEFAULT_SPEND_LIMIT;
			campaignMap = new HashMap<Campaign, Map<AdxQuery, BidInfo>>();
		}

		public BidInfo generateBid(AdxQuery query) {
			return bidsSampler.getSample().get(query);
		}

		private synchronized void doAddQuery(BidInfo bidInfo, AdxQuery query) {
			Map<AdxQuery, BidInfo> campaignBids = campaignMap.get(bidInfo
					.getCampaign());
			if (campaignBids == null) {
				campaignBids = new HashMap<AdxQuery, BidInfo>();
				campaignMap.put(bidInfo.getCampaign(), campaignBids);
			}
			campaignBids.put(query, bidInfo);
		}

		// TODO: uncomment me
		// protected void setSpendLimit(AdxQuery query, double spendLimit) {
		// int index = ArrayUtils.indexOf(queries, 0, queryCount, query);
		//
		// if (index < 0) {
		// index = doAddQuery(query);
		// }
		//
		// this.spendLimits[index] = spendLimit;
		// }
		//
		// protected double getSpendLimits(AdxQuery query) {
		// int index = ArrayUtils.indexOf(queries, 0, queryCount, query);
		//
		// if (index < 0) {
		// return Double.POSITIVE_INFINITY;
		// }
		//
		// return this.spendLimits[index];
		// }

		public void updateBidSampler(Map<Campaign, Integer> campaignDistribution) {
			bidsSampler = new WheelSampler<Map<AdxQuery, BidInfo>>();
			for (Entry<Campaign, Integer> entry : campaignDistribution
					.entrySet()) {
				bidsSampler.addState(entry.getValue(),
						campaignMap.get(entry.getKey()));
			}
		}

		public double getCampaignSpendLimit() {
			return campaignSpendLimit;
		}

		protected void setCampaignSpendLimit(double campaignSpendLimit) {
			this.campaignSpendLimit = campaignSpendLimit;
		}
	}
}
