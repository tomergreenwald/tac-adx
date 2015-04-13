/*
 * BidBundle.java
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
package tau.tac.adx.props;

import java.text.ParseException;
import java.util.Set;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import tau.tac.adx.AdxManager;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.bids.BidInfo;
import tau.tac.adx.devices.Device;
import tau.tac.adx.report.adn.MarketSegment;
import edu.umich.eecs.tac.props.Ad;

/**
 * BidBundles specify all relevant bidding information for a given Advertister.
 * Each BidBundle contains a set of {@link BidEntry Bid Entries}, each for a
 * given query. Each BidEntry contains:
 * <ul>
 * <li> {@link Ad} - The ad to be used for the given query.</li>
 * <li>Bid - The given bid to be used for the given query.</li>
 * <li>Daily Limit - The daily spend limit to be used for the given query</li>
 * </ul>
 * <p/>
 * <p>
 * Each BidBundle also can contains a Campaign Daily Spend Limit, which
 * specifies the total amount of money an advertiser is willing to spend over
 * all queries daily.
 * <p/>
 * Advertisers typically send BidBundles on a daily basis to the Publisher.
 * 
 * @author Ben Cassell, Patrick Jordan, Lee Callender
 * @author greenwald, Mariano Schain
 */
public class AdxBidBundle extends
		AdxAbstractQueryKeyedReportTransportable<AdxBidBundle.BidEntry> {
	/**
	 * The persistent value for spend limit. Publisher's reading a persistent
	 * value will ignore the value and instead use yesterday's given spend limit
	 * in its place.
	 */
	public static final double PERSISTENT_SPEND_LIMIT = Double.NaN;

	/**
	 * The persistent value for bid. Publisher's reading a persistent value will
	 * ignore the value and instead use yesterday's given bid in its place.
	 */
	public static final double PERSISTENT_BID = Double.NaN;

	/**
	 * identifies the query as a piggybacked set limit indication
	 */
	public static final String CMP_DSL = "CMP_SET_DAILY_LIMIT";

	/**
	 * identifies the query as a piggybacked set total limit indication
	 */
	public static final String CMP_TSL = "CMP_SET_TOTAL_LIMIT";

	/**
	 * The persistent value for ad. Publisher's reading a persistent value will
	 * ignore the value and instead use yesterday's given Ad in its place.
	 */
	public static final Ad PERSISTENT_AD = null;

	private static final int PERSISTENT_WEIGHT = 1;

	/**
	 * Advertiser's wishing to have no spend limit should use the given
	 * NO_SPEND_LIMIT value.
	 */
	public static final double NO_SPEND_LIMIT = Double.POSITIVE_INFINITY;

	/**
	 * Advertiser's wishing not to be shown should use the given NO_SHOW_BID
	 * value.
	 */
	public static final double NO_SHOW_BID = 0.0;

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 5057969669832603679L;

	/**
	 * The campaign daily spend limit.
	 */
	private double campaignDailySpendLimit;

	/**
	 * Advertiser id.
	 */
	private int advertiserId;

	/**
	 * Creates a new BidBundle. Sets the campaign daily spend limit to the
	 * PERSISTENT_SPEND_LIMIT.
	 */
	public AdxBidBundle() {
		campaignDailySpendLimit = PERSISTENT_SPEND_LIMIT;
	}

	/**
	 * Creates a {@link AdxBidBundle.BidEntry} with the given {@link AdxQuery
	 * query} as the key.
	 * 
	 * @param key
	 *            the query key
	 * @return a {@link AdxBidBundle.BidEntry} with the given {@link AdxQuery
	 *         query} as the key.
	 */
	@Override
	protected final BidEntry createEntry(final AdxQuery key) {
		BidEntry entry = new BidEntry();
		entry.setQuery(key);
		return entry;
	}

	/**
	 * Returns the {@link BidEntry} class.
	 * 
	 * @return the {@link BidEntry} class.
	 */
	@Override
	protected final Class<BidEntry> entryClass() {
		return BidEntry.class;
	}

	/**
	 * Adds a {@link BidEntry} keyed with the specified query and the given bid
	 * and Ad. The spend limit is set to PERSISTENT_SPEND_LIMIT.
	 * 
	 * @param query
	 *            the query key.
	 * @param bid
	 *            the bid value.
	 * @param ad
	 *            the ad to be used.
	 */
	public final void addQuery(final AdxQuery query, final double bid,
			final Ad ad, int campaignId, int weight) {
		addQuery(query, bid, ad, campaignId, weight, PERSISTENT_SPEND_LIMIT);
	}

	/**
	 * Adds a {@link BidEntry} keyed with the specified query and the given bid
	 * and Ad. The spend limit is set to PERSISTENT_SPEND_LIMIT.
	 * 
	 * @param query
	 *            the query key.
	 * @param bid
	 *            the bid value.
	 * @param ad
	 *            the ad to be used.
	 * @param dailyLimit
	 *            the daily limit.
	 */
	public final void addQuery(final AdxQuery query, final double bid,
			final Ad ad, int campaignId, int weight, final double dailyLimit) {
		int index = addQuery(query);
		BidEntry entry = getEntry(index);
		entry.setQuery(query);
		entry.setBid(bid);
		entry.setAd(ad);
		entry.setDailyLimit(dailyLimit);
		entry.setCampaignId(campaignId);
		entry.setWeight(weight);
	}

	/**
	 * Sets the bid for a query.
	 * 
	 * @param query
	 *            the query.
	 * @param bid
	 *            the bid.
	 */
	public final void setBid(final AdxQuery query, final double bid) {
		lockCheck();

		int index = indexForEntry(query);

		if (index < 0) {
			index = addQuery(query);
		}

		setBid(index, bid);
	}

	/**
	 * Sets the bid for a query.
	 * 
	 * @param index
	 *            the index of the query.
	 * @param bid
	 *            the bid.
	 */
	public final void setBid(final int index, final double bid) {
		lockCheck();
		getEntry(index).setBid(bid);
	}

	/**
	 * Sets the ad for a query.
	 * 
	 * @param query
	 *            the query.
	 * @param ad
	 *            the ad.
	 */
	public final void setAd(final AdxQuery query, final Ad ad) {
		lockCheck();

		int index = indexForEntry(query);

		if (index < 0) {
			index = addQuery(query);
		}

		setAd(index, ad);
	}

	/**
	 * Sets the ad for a query.
	 * 
	 * @param index
	 *            the index of the query.
	 * @param ad
	 *            the ad.
	 */
	public final void setAd(final int index, final Ad ad) {
		lockCheck();
		getEntry(index).setAd(ad);
	}

	/**
	 * Sets the daily spend limit for a query.
	 * 
	 * @param query
	 *            the query.
	 * @param dailyLimit
	 *            the daily spend limit.
	 */
	public final void setDailyLimit(final AdxQuery query,
			final double dailyLimit) {
		lockCheck();

		int index = indexForEntry(query);

		if (index < 0) {
			index = addQuery(query);
		}

		setDailyLimit(index, dailyLimit);
	}

	/**
	 * Sets the daily spend limit for a query.
	 * 
	 * @param index
	 *            the index of the query.
	 * @param dailyLimit
	 *            the daily spend limit.
	 */
	public final void setDailyLimit(final int index, final double dailyLimit) {
		lockCheck();
		getEntry(index).setDailyLimit(dailyLimit);
	}

	/**
	 * Sets the bid and ad for a query.
	 * 
	 * @param query
	 *            the query.
	 * @param bid
	 *            the bid.
	 * @param ad
	 *            the ad.
	 */
	public final void setBidAndAd(final AdxQuery query, final double bid,
			final Ad ad) {
		lockCheck();

		int index = indexForEntry(query);

		if (index < 0) {
			index = addQuery(query);
		}

		setBidAndAd(index, bid, ad);

	}

	/**
	 * Sets the bid and ad for a query.
	 * 
	 * @param index
	 *            the index of the query.
	 * @param bid
	 *            the bid.
	 * @param ad
	 *            the ad.
	 */
	public final void setBidAndAd(final int index, final double bid, final Ad ad) {
		lockCheck();
		BidEntry entry = getEntry(index);
		entry.setBid(bid);
		entry.setAd(ad);
	}

	/**
	 * Returns the bid for the associated query.
	 * 
	 * @param query
	 *            the query.
	 * @return the bid.
	 */
	public final double getBid(final AdxQuery query) {
		int index = indexForEntry(query);

		return index < 0 ? PERSISTENT_BID : getBid(index);
	}

	/**
	 * Returns the bid for the associated query.
	 * 
	 * @param index
	 *            the index of the query.
	 * @return the bid.
	 */
	public final double getBid(final int index) {
		return getEntry(index).getBid();
	}

	/**
	 * Returns the weight for the associated query.
	 * 
	 * @param query
	 *            the query.
	 * @return the weight.
	 */
	public final double getWeight(final AdxQuery query) {
		int index = indexForEntry(query);

		return index < 0 ? PERSISTENT_BID : getWeight(index);
	}

	/**
	 * Returns the weight for the associated query.
	 * 
	 * @param index
	 *            the index of the query.
	 * @return the weight.
	 */
	public final double getWeight(final int index) {
		return getEntry(index).getWeight();
	}

	/**
	 * Returns the campaign id for the associated query.
	 * 
	 * @param query
	 *            the query.
	 * @return the weight.
	 */
	public final double getCampaignId(final AdxQuery query) {
		int index = indexForEntry(query);

		return index < 0 ? PERSISTENT_BID : getCampaignId(index);
	}

	/**
	 * Returns the campaign id for the associated query.
	 * 
	 * @param index
	 *            the index of the query.
	 * @return the campaign id.
	 */
	public final double getCampaignId(final int index) {
		return getEntry(index).getCampaignId();
	}

	/**
	 * Returns the ad for the associated query.
	 * 
	 * @param query
	 *            the query.
	 * @return the ad.
	 */
	public final Ad getAd(final AdxQuery query) {
		int index = indexForEntry(query);

		return index < 0 ? PERSISTENT_AD : getAd(index);
	}

	public BidInfo getBidInfo(AdxQuery query) {
		int index = indexForEntry(query);
		if (index < 0) {
			return null;
		}
		BidEntry bidEntry = getEntry(index);
		BidInfo bidInfo = new BidInfo(bidEntry.getBid(), AdxManager
				.getInstance().getBidder(advertiserId), bidEntry.getAd(),
				bidEntry.getMarketSegments(), AdxManager.getInstance()
						.getCampaign(bidEntry.getCampaignId()));
		return bidInfo;
	}

	/**
	 * Returns the ad for the associated query.
	 * 
	 * @param index
	 *            the index of the query.
	 * @return the ad.
	 */
	public final Ad getAd(final int index) {
		return getEntry(index).getAd();
	}

	/**
	 * Returns the daily spend limit for the associated query.
	 * 
	 * @param query
	 *            the query.
	 * @return the daily spend limit.
	 */
	public final double getDailyLimit(final AdxQuery query) {
		int index = indexForEntry(query);

		return index < 0 ? PERSISTENT_SPEND_LIMIT : getDailyLimit(index);
	}

	/**
	 * Returns the daily spend limit for the associated query.
	 * 
	 * @param index
	 *            the index of the query.
	 * @return the daily spend limit.
	 */
	public final double getDailyLimit(final int index) {
		return getEntry(index).getDailyLimit();
	}

	/**
	 * Returns the campaign daily spend limit for the associated query.
	 * 
	 * @return the campaign daily spend limit.
	 */
	public final double getCampaignDailySpendLimit() {
		return campaignDailySpendLimit;
	}

	/**
	 * Sets the daily spend limit for a specified campaign.
	 * 
	 * @param campaignId
	 * @param campaignDailySpendLimit
	 *            the campaign daily spend limit.
	 */
	public void setCampaignDailyLimit(int campaignId, int impstogo,
			double campaignDailySpendLimit) {
		addQuery(new AdxQuery(CMP_DSL + campaignId,
				MarketSegment.FEMALE, Device.mobile, AdType.text),
				0, new Ad(null), campaignId, impstogo, campaignDailySpendLimit);
	}

	public void setCampaignTotalLimit(int campaignId, int totalimps,
			double campaignTotalSpendLimit) {
		addQuery(new AdxQuery(CMP_TSL + campaignId,
				MarketSegment.FEMALE, Device.mobile, AdType.text),
				0, new Ad(null), campaignId, totalimps, campaignTotalSpendLimit);
	}

	/**
	 * Sets the campaign daily spend limit.
	 * 
	 * @param campaignDailySpendLimit
	 *            the campaign daily spend limit.
	 */
	public final void setCampaignDailySpendLimit(
			final double campaignDailySpendLimit) {
		lockCheck();
		this.campaignDailySpendLimit = campaignDailySpendLimit;
	}

	/**
	 * Reads the campaign daily spend limit from the reader.
	 * 
	 * @param reader
	 *            the reader that is read from.
	 * @throws ParseException
	 *             if exception occurs before reading the campaign daily spend
	 *             limit
	 */
	@Override
	protected final void readBeforeEntries(final TransportReader reader)
			throws ParseException {
		this.campaignDailySpendLimit = reader.getAttributeAsDouble(
				"campaignDailySpendLimit", PERSISTENT_SPEND_LIMIT);
		this.advertiserId = reader.getAttributeAsInt("advertiserId", -1);
	}

	/**
	 * Writes the campaign daily spend limit to the writer.
	 * 
	 * @param writer
	 *            the writer that is written to.
	 */
	@Override
	protected final void writeBeforeEntries(final TransportWriter writer) {
		writer.attr("campaignDailySpendLimit", campaignDailySpendLimit);
		writer.attr("advertiserId", advertiserId);
	}

	/**
	 * Appends the campaign daily spend limit.
	 * 
	 * @param builder
	 *            the builder to append for the {@link #toString()}
	 */
	@Override
	protected final void toStringBeforeEntries(final StringBuilder builder) {
		builder.append(" limit: ").append(campaignDailySpendLimit);
	}

	/**
	 * Each BidEntry contains:
	 * <ul>
	 * <li> {@link Ad} - The ad to be used for the given query.</li>
	 * <li>Bid - The given bid to be used for the given query.</li>
	 * <li>Daily Limit - The daily spend limit to be used for the given query</li>
	 * </ul>
	 * 
	 * @author Patrick Jordan, Lee Callender
	 */
	public static class BidEntry extends AdxAbstractQueryEntry {
		/**
		 * KEY_NODE_TRANSPORT_NAME
		 */
		private static final long serialVersionUID = -3565785779410778142L;
		/**
		 * The ad to use for display.
		 */
		private Ad ad;
		/**
		 * The bid for the query class.
		 */
		private double bid;
		/**
		 * The spend limit for the query class.
		 */
		private double dailyLimit;

		/**
		 * Campaign ID.
		 */
		private int campaignId;

		/**
		 * Campaign weight
		 */
		private int weight;

		/**
		 * Creates a bid entry with all values set to their persistent value.
		 */
		public BidEntry() {
			this.bid = PERSISTENT_BID;
			this.dailyLimit = PERSISTENT_SPEND_LIMIT;
			this.ad = PERSISTENT_AD;
			this.weight = PERSISTENT_WEIGHT;
		}

		public BidEntry(Ad ad, double bid, double dailyLimit, int campaignId,
				int weight) {
			super();
			this.ad = ad;
			this.bid = bid;
			this.dailyLimit = dailyLimit;
			this.campaignId = campaignId;
			this.weight = weight;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((ad == null) ? 0 : ad.hashCode());
			long temp;
			temp = Double.doubleToLongBits(bid);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + campaignId;
			temp = Double.doubleToLongBits(dailyLimit);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + weight;
			return result;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BidEntry other = (BidEntry) obj;
			if (ad == null) {
				if (other.ad != null)
					return false;
			} else if (!ad.equals(other.ad))
				return false;
			if (Double.doubleToLongBits(bid) != Double
					.doubleToLongBits(other.bid))
				return false;
			if (campaignId != other.campaignId)
				return false;
			if (Double.doubleToLongBits(dailyLimit) != Double
					.doubleToLongBits(other.dailyLimit))
				return false;
			if (weight != other.weight)
				return false;
			return true;
		}

		public int getWeight() {
			return weight;
		}

		public void setWeight(int weight) {
			this.weight = weight;
		}

		/**
		 * Returns the ad for display for the query class.
		 * 
		 * @return the ad for display.
		 */
		public final Ad getAd() {
			return ad;
		}

		/**
		 * Sets the ad for the query class. Use <code>PERSISTENT_AD</code> to
		 * persist the prior day's value.
		 * 
		 * @param ad
		 *            the ad.
		 */
		public final void setAd(final Ad ad) {
			this.ad = ad;
		}

		/**
		 * Returns the bid for the query class.
		 * 
		 * @return the bid for the query class.
		 */
		public final double getBid() {
			return bid;
		}

		/**
		 * Sets the bid for the query class. Use <code>PERSISTENT_BID</code> to
		 * persist the prior day's value.
		 * 
		 * @param bid
		 *            the bid for the query class
		 */
		public final void setBid(final double bid) {
			this.bid = bid;
		}

		/**
		 * @return the campaignId
		 */
		public int getCampaignId() {
			return campaignId;
		}

		/**
		 * @param campaignId
		 *            the campaignId to set
		 */
		public void setCampaignId(int campaignId) {
			this.campaignId = campaignId;
		}

		/**
		 * @return the marketSegment
		 */
		public Set<MarketSegment> getMarketSegments() {
			return getKey().getMarketSegments();
		}

		/**
		 * Reads the bid entry state from the reader.
		 * 
		 * @param reader
		 *            the reader to read the state in from.
		 * @throws ParseException
		 *             if an exception occurs when reading the bid entry state
		 */
		@Override
		protected final void readEntry(final TransportReader reader)
				throws ParseException {
			this.bid = reader.getAttributeAsDouble("bid", PERSISTENT_BID);
			this.dailyLimit = reader.getAttributeAsDouble("dailyLimit",
					PERSISTENT_SPEND_LIMIT);
			this.campaignId = reader.getAttributeAsInt("campaignId", 0);
			this.weight = reader.getAttributeAsInt("weight", 1);
			if (reader.nextNode("Ad", false)) {
				this.ad = (Ad) reader.readTransportable();
			}
		}

		/**
		 * Writes the bid entry state to the writer.
		 * 
		 * @param writer
		 *            the writer to write the entry state to
		 */
		@Override
		protected final void writeEntry(final TransportWriter writer) {
			writer.attr("bid", bid);
			writer.attr("dailyLimit", dailyLimit);
			writer.attr("campaignId", campaignId);
			writer.attr("weight", weight);
			if (ad != null) {
				writer.write(ad);
			}
		}

		/**
		 * Returns the daily spend limit for the query class.
		 * 
		 * @return the daily spend limit for the query class.
		 */
		public final double getDailyLimit() {
			return dailyLimit;
		}

		/**
		 * Sets the daily spend limit for the query class.
		 * 
		 * @param dailyLimit
		 *            the daily spend limit for the query class
		 */
		public final void setDailyLimit(final double dailyLimit) {
			this.dailyLimit = dailyLimit;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "BidEntry [ad=" + ad + ", bid=" + bid + ", dailyLimit="
					+ dailyLimit + " , weight=" + weight + "]";
		}

	}

}
