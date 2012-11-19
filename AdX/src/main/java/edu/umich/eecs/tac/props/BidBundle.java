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
package edu.umich.eecs.tac.props;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;

/**
 * BidBundles specify all relevant bidding information for a given Advertister.
 * Each BidBundle contains a set of {@link BidEntry Bid Entries},
 * each for a given query. Each BidEntry contains:
 * <ul>
 * <li> {@link Ad} - The ad to be used for the given query.</li>
 * <li> Bid - The given bid to be used for the given query.</li>
 * <li> Daily Limit - The daily spend limit to be used for the given query</li>
 * </ul>
 * <p/>
 * <p>Each BidBundle also can contains a Campaign Daily Spend Limit, which
 * specifies the total amount of money an advertiser is willing to spend
 * over all queries daily.
 * <p/>
 * Advertisers typically send BidBundles on a daily basis to the Publisher.
 *
 * @author Ben Cassell, Patrick Jordan, Lee Callender
 */
public class BidBundle extends AbstractQueryKeyedReportTransportable<BidBundle.BidEntry> {
    /**
     * The persistent value for spend limit.
     * Publisher's reading a persistent value will ignore the value and instead use
     * yesterday's given spend limit in its place.
     */
    public static final double PERSISTENT_SPEND_LIMIT = Double.NaN;

    /**
     * The persistent value for bid.
     * Publisher's reading a persistent value will ignore the value and instead use
     * yesterday's given bid in its place.
     */
    public static final double PERSISTENT_BID = Double.NaN;

    /**
     * The persistent value for ad.
     * Publisher's reading a persistent value will ignore the value and instead use
     * yesterday's given Ad in its place.
     */
    public static final Ad PERSISTENT_AD = null;

    /**
     * Advertiser's wishing to have no spend limit should use the given NO_SPEND_LIMIT value.
     */
    public static final double NO_SPEND_LIMIT = Double.POSITIVE_INFINITY;

    /**
     * Advertiser's wishing not to be shown should use the given NO_SHOW_BID value.
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
     * Creates a new BidBundle. Sets the campaign daily spend
     * limit to the PERSISTENT_SPEND_LIMIT.
     */
    public BidBundle() {
        campaignDailySpendLimit = PERSISTENT_SPEND_LIMIT;
    }


    /**
     * Creates a {@link BidBundle.BidEntry} with the given {@link Query query} as the key.
     *
     * @param key the query key
     * @return a {@link BidBundle.BidEntry} with the given {@link Query query} as the key.
     */
    protected final BidEntry createEntry(final Query key) {
        BidEntry entry = new BidEntry();
        entry.setQuery(key);
        return entry;
    }

    /**
     * Returns the {@link BidEntry} class.
     *
     * @return the {@link BidEntry} class.
     */
    protected final Class entryClass() {
        return BidEntry.class;
    }

    /**
     * Adds a {@link BidEntry} keyed with the specified query and the
     * given bid and Ad. The spend limit is set to PERSISTENT_SPEND_LIMIT.
     *
     * @param query the query key.
     * @param bid   the bid value.
     * @param ad    the ad to be used.
     */
    public final void addQuery(final Query query, final double bid, final Ad ad) {
        addQuery(query, bid, ad, PERSISTENT_SPEND_LIMIT);
    }

    /**
     * Adds a {@link BidEntry} keyed with the specified query and the
     * given bid and Ad. The spend limit is set to PERSISTENT_SPEND_LIMIT.
     *
     * @param query      the query key.
     * @param bid        the bid value.
     * @param ad         the ad to be used.
     * @param dailyLimit the daily limit.
     */
    public final void addQuery(final Query query, final double bid, final Ad ad, final double dailyLimit) {
        int index = addQuery(query);
        BidEntry entry = getEntry(index);
        entry.setQuery(query);
        entry.setBid(bid);
        entry.setAd(ad);
        entry.setDailyLimit(dailyLimit);
    }

    /**
     * Sets the bid for a query.
     *
     * @param query the query.
     * @param bid   the bid.
     */
    public final void setBid(final Query query, final double bid) {
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
     * @param index the index of the query.
     * @param bid   the bid.
     */
    public final void setBid(final int index, final double bid) {
        lockCheck();
        getEntry(index).setBid(bid);
    }

    /**
     * Sets the ad for a query.
     *
     * @param query the query.
     * @param ad    the ad.
     */
    public final void setAd(final Query query, final Ad ad) {
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
     * @param index the index of the query.
     * @param ad    the ad.
     */
    public final void setAd(final int index, final Ad ad) {
        lockCheck();
        getEntry(index).setAd(ad);
    }

    /**
     * Sets the daily spend limit for a query.
     *
     * @param query      the query.
     * @param dailyLimit the daily spend limit.
     */
    public final void setDailyLimit(final Query query, final double dailyLimit) {
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
     * @param index      the index of the query.
     * @param dailyLimit the daily spend limit.
     */
    public final void setDailyLimit(final int index, final double dailyLimit) {
        lockCheck();
        getEntry(index).setDailyLimit(dailyLimit);
    }

    /**
     * Sets the bid and ad for a query.
     *
     * @param query the query.
     * @param bid   the bid.
     * @param ad    the ad.
     */
    public final void setBidAndAd(final Query query, final double bid, final Ad ad) {
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
     * @param index the index of the query.
     * @param bid   the bid.
     * @param ad    the ad.
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
     * @param query the query.
     * @return the bid.
     */
    public final double getBid(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? PERSISTENT_BID : getBid(index);
    }

    /**
     * Returns the bid for the associated query.
     *
     * @param index the index of the query.
     * @return the bid.
     */
    public final double getBid(final int index) {
        return getEntry(index).getBid();
    }

    /**
     * Returns the ad for the associated query.
     *
     * @param query the query.
     * @return the ad.
     */
    public final Ad getAd(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? PERSISTENT_AD : getAd(index);
    }

    /**
     * Returns the ad for the associated query.
     *
     * @param index the index of the query.
     * @return the ad.
     */
    public final Ad getAd(final int index) {
        return getEntry(index).getAd();
    }

    /**
     * Returns the daily spend limit for the associated query.
     *
     * @param query the query.
     * @return the daily spend limit.
     */
    public final double getDailyLimit(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? PERSISTENT_SPEND_LIMIT : getDailyLimit(index);
    }

    /**
     * Returns the daily spend limit for the associated query.
     *
     * @param index the index of the query.
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
     * Sets the campaign daily spend limit.
     *
     * @param campaignDailySpendLimit the campaign daily spend limit.
     */
    public final void setCampaignDailySpendLimit(final double campaignDailySpendLimit) {
        lockCheck();
        this.campaignDailySpendLimit = campaignDailySpendLimit;
    }

    /**
     * Reads the campaign daily spend limit from the reader.
     * @param reader the reader that is read from.
     * @throws ParseException if exception occurs before reading the campaign daily spend limit
     */
    @Override
    protected final void readBeforeEntries(final TransportReader reader) throws ParseException {
        this.campaignDailySpendLimit = reader.getAttributeAsDouble("campaignDailySpendLimit", PERSISTENT_SPEND_LIMIT);
    }

    /**
     * Writes the campaign daily spend limit to the writer.
     * @param writer the writer that is written to.
     */
    @Override
    protected final void writeBeforeEntries(final TransportWriter writer) {
        writer.attr("campaignDailySpendLimit", campaignDailySpendLimit);
    }

    /**
     * Appends the campaign daily spend limit.
     * @param builder the builder to append for the {@link #toString()}
     */
    @Override
    protected final void toStringBeforeEntries(final StringBuilder builder) {
        builder.append(" limit: ").append(campaignDailySpendLimit);
    }

    /**
     * Each BidEntry contains:
     * <ul>
     * <li> {@link Ad} - The ad to be used for the given query.</li>
     * <li> Bid - The given bid to be used for the given query.</li>
     * <li> Daily Limit - The daily spend limit to be used for the given query</li>
     * </ul>
     *
     * @author Patrick Jordan, Lee Callender
     */
    public static class BidEntry extends AbstractQueryEntry {
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
         * Creates a bid entry with all values set to their persistent value.
         */
        public BidEntry() {
            this.bid = PERSISTENT_BID;
            this.dailyLimit = PERSISTENT_SPEND_LIMIT;
            this.ad = PERSISTENT_AD;
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
         * Sets the ad for the query class.  Use <code>PERSISTENT_AD</code> to persist the prior day's value.
         *
         * @param ad the ad.
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
         * Sets the bid for the query class.  Use <code>PERSISTENT_BID</code> to persist the prior day's value.
         * @param bid the bid for the query class
         */
        public final void setBid(final double bid) {
            this.bid = bid;
        }

        /**
         * Reads the bid entry state from the reader.
         * @param reader the reader to read the state in from.
         * @throws ParseException if an exception occurs when reading the bid entry state
         */
        @Override
        protected final void readEntry(final TransportReader reader) throws ParseException {
            this.bid = reader.getAttributeAsDouble("bid", PERSISTENT_BID);
            this.dailyLimit = reader.getAttributeAsDouble("dailyLimit", PERSISTENT_SPEND_LIMIT);
            if (reader.nextNode("Ad", false)) {
                this.ad = (Ad) reader.readTransportable();
            }
        }

        /**
         * Writes the bid entry state to the writer.
         * @param writer the writer to write the entry state to
         */
        @Override
        protected final void writeEntry(final TransportWriter writer) {
            writer.attr("bid", bid);
            writer.attr("dailyLimit", dailyLimit);
            if (ad != null) {
                writer.write(ad);
            }
        }

        /**
         * Returns the daily spend limit for the query class.
         * @return the daily spend limit for the query class.
         */
        public final double getDailyLimit() {
            return dailyLimit;
        }

        /**
         * Sets the daily spend limit for the query class.
         * @param dailyLimit the daily spend limit for the query class
         */
        public final void setDailyLimit(final double dailyLimit) {
            this.dailyLimit = dailyLimit;
        }

        /**
         * Returns a string representation for the bid entry.
         * @return a string representation for the bid entry.
         */
        @Override
        public final String toString() {
            return String.format("(Bid query:%s ad:%s bid: %f limit: %f)", getQuery(), ad, bid, dailyLimit);
        }
    }

}
