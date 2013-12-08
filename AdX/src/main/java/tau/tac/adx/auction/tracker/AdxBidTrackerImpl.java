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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import tau.tac.adx.AdxManager;
import tau.tac.adx.bids.BidInfo;
import tau.tac.adx.bids.Bidder;
import tau.tac.adx.messages.CampaignLimitReached;
import tau.tac.adx.messages.CampaignLimitSet;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.AdxBidBundle.BidEntry;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.sim.TACAdxSimulation;

import com.botbox.util.ArrayUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.util.sampling.WheelSampler;

/**
 * @author greenwald
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
		queryBid[index].clearQueries();
		for (AdxQuery query : bundle) {
			BidEntry entry = bundle.getEntry(query);
			if (entry != null) {
				queryBid[index].doAddQuery(entry);
			}

			// TODO: uncomment me
			// if (dailyLimit >= 0.0) {
			// queryBid[index].setSpendLimit(query, dailyLimit);
			// }
		}

		// queryBid[index].updateBidSampler(campaignDistribution)
	}

	@Override
	public int size() {
		return advertisersCount;
	}

	private static class AdxQueryBid {
		private final String advertiser;
		private final double[] spendLimits;
		private final Set<BidEntry> querySet = new HashSet<AdxBidBundle.BidEntry>();
		private final Map<AdxQuery, WheelSampler<BidEntry>> queryMap = new HashMap<AdxQuery, WheelSampler<BidEntry>>();
		private final int queryCount;
		private double campaignSpendLimit;
		private final Bidder bidder;

		private final Set<Integer> excludedCampaigns = new HashSet<Integer>();

		public AdxQueryBid(final String advertiser, int queryCount) {
			this.advertiser = advertiser;
			spendLimits = new double[queryCount];
			this.queryCount = queryCount;
			campaignSpendLimit = DEFAULT_SPEND_LIMIT;
			bidder = new Bidder() {

				@Override
				public String getName() {
					return advertiser;
				}
			};
			TACAdxSimulation.eventBus.register(this);
		}

		@Subscribe
		public void limitReached(CampaignLimitReached message) {
			if (message.getAdNetwork().equals(advertiser)) {
				excludedCampaigns.add(message.getCampaignId());
				queryMap.clear();
			}
		}

		public void clearQueries() {
			querySet.clear();
			queryMap.clear();
			excludedCampaigns.clear();
		}

		public BidInfo generateBid(AdxQuery query) {
			WheelSampler<BidEntry> sampler = queryMap.get(query);
			if (sampler == null) {
				sampler = new WheelSampler<AdxBidBundle.BidEntry>();
				queryMap.put(query, sampler);
				Collection<BidEntry> filteredQueries = Collections2.filter(
						querySet, new BidPredicate(query, excludedCampaigns));
				for (BidEntry bidEntry : filteredQueries) {
					sampler.addState(bidEntry.getWeight(), bidEntry);
				}
			}

			BidEntry sample = sampler.getSample();
			if (sample == null) {
				return null;
			}
			BidInfo bidInfo = new BidInfo(sample.getBid(), bidder,
					sample.getAd(), sample.getMarketSegments(), AdxManager
							.getInstance().getCampaign(sample.getCampaignId()));
			return bidInfo;
		}

		private class BidPredicate implements Predicate<BidEntry> {

			private final AdxQuery adxQuery;
			private final Set<Integer> excludedCampaigns;

			public BidPredicate(AdxQuery adxQuery,
					Set<Integer> excludedCampaigns) {
				this.adxQuery = adxQuery;
				this.excludedCampaigns = excludedCampaigns;
			}

			@Override
			public boolean apply(BidEntry input) {
				return (((adxQuery.getMarketSegments().containsAll(
						input.getMarketSegments()))
				/* exclude campaigns over limit */
				&& (!excludedCampaigns.contains(input.getCampaignId()))));
			}
		}

		private synchronized void doAddQuery(BidEntry entry) {
			if (entry.getKey().getPublisher().startsWith(AdxBidBundle.CMP_DSL)) {
				/* it is a piggybacked set campaig limit command: notify */
				AdxManager
						.getInstance()
						.getSimulation()
						.getEventBus()
						.post(new CampaignLimitSet(false, entry.getCampaignId(),
								advertiser, entry.getWeight(), entry
										.getDailyLimit()));

			} else if (entry.getKey().getPublisher().startsWith(AdxBidBundle.CMP_TSL)) {
				/* it is a piggybacked set campain total limit command: notify */
				AdxManager
						.getInstance()
						.getSimulation()
						.getEventBus()
						.post(new CampaignLimitSet(true, entry.getCampaignId(),
								advertiser, entry.getWeight(), entry
										.getDailyLimit()));

			} else {
				querySet.add(entry);
			}
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

		public double getCampaignSpendLimit() {
			return campaignSpendLimit;
		}

		protected void setCampaignSpendLimit(double campaignSpendLimit) {
			this.campaignSpendLimit = campaignSpendLimit;
		}
	}
}
