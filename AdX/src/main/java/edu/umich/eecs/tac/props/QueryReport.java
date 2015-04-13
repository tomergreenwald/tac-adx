/*
 * QueryReport.java
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
import java.util.Set;
import java.util.Collections;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;

/**
 * Query report contains impressions, clicks, cost, average position, and ad displayed
 * by the advertiser for each query class during the period as well as the positions and
 * displayed ads of all advertisers during the period for each query class.
 *
 * @author Ben Cassell, Patrick Jordan, Lee Callender
 */
public class QueryReport extends AbstractQueryKeyedReportTransportable<QueryReport.QueryReportEntry> {

    /**
     * The serial version id.
     */
    private static final long serialVersionUID = -7957495904471250085L;

    /**
     * Creates a {@link QueryReportEntry} with the given {@link Query query} as the key.
     *
     * @param query the query key
     * @return a {@link QueryReportEntry} with the given {@link Query query} as the key.
     */
    protected final QueryReportEntry createEntry(final Query query) {
        QueryReportEntry entry = new QueryReportEntry();
        entry.setQuery(query);
        return entry;
    }

    /**
     * Returns the {@link QueryReportEntry} class.
     *
     * @return the {@link QueryReportEntry} class.
     */
    protected final Class entryClass() {
        return QueryReportEntry.class;
    }

    /**
     * Creates a new query report.
     */
    public QueryReport() {
    }

    /**
     * Adds a {@link QueryReportEntry} keyed with the specificed query and the associated viewing statistics.
     *
     * @param query               the query key.
     * @param regularImpressions  the number of regular impressions.
     * @param promotedImpressions the number of promoted impressions.
     * @param clicks              the number of clicks.
     * @param cost                the cost of the clicks.
     * @param positionSum         the sum of the positions over all impressions.
     */
    public final void addQuery(final Query query, final int regularImpressions, final int promotedImpressions,
                               final int clicks, final double cost, final double positionSum) {
        lockCheck();

        int index = addQuery(query);
        QueryReportEntry entry = getEntry(index);
        entry.setImpressions(regularImpressions, promotedImpressions);
        entry.setClicks(clicks);
        entry.setCost(cost);
        entry.setPositionSum(positionSum);
    }

    /**
     * Sets the sum of the positions over all the impressions for a query.
     *
     * @param query       the query
     * @param positionSum the sum of the positions over all the impressions for a query.
     */
    public final void setPositionSum(final Query query, final double positionSum) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            index = addQuery(query);
        }

        setPositionSum(index, positionSum);

    }

    /**
     * Sets the sum of the positions over all the impressions for a query.
     *
     * @param index       the index of the query
     * @param positionSum the sum of the positions over all the impressions for a query.
     */
    public final void setPositionSum(final int index, final double positionSum) {
        lockCheck();
        getEntry(index).setPositionSum(positionSum);
    }

    /**
     * Sets the cost associated with the query.
     *
     * @param query the query
     * @param cost  the cost associated with the query.
     */
    public final void setCost(final Query query, final double cost) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            index = addQuery(query);
        }

        setCost(index, cost);

    }

    /**
     * Sets the cost associated with the query.
     *
     * @param index the query index
     * @param cost  the cost associated with the query.
     */
    public final void setCost(final int index, final double cost) {
        lockCheck();
        getEntry(index).setCost(cost);
    }

    /**
     * Sets the impressions associated with the query.
     *
     * @param query               the query
     * @param regularImpressions  the regular impressions
     * @param promotedImpressions the promoted impressions
     */
    public final void setImpressions(final Query query, final int regularImpressions, final int promotedImpressions) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            index = addQuery(query);
        }

        setImpressions(index, regularImpressions, promotedImpressions);

    }

    /**
     * Sets the impressions associated with the query.
     *
     * @param query               the query
     * @param regularImpressions  the regular impressions
     * @param promotedImpressions the promoted impressions
     * @param ad                  the ad shown
     * @param positionSum         the sum of positions over all impressions
     */
    public final void setImpressions(final Query query, final int regularImpressions, final int promotedImpressions,
                                     final Ad ad, final double positionSum) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            index = addQuery(query);
        }

        setImpressions(index, regularImpressions, promotedImpressions, ad, positionSum);
    }

    /**
     * Adds the impressions associated with the query.
     *
     * @param query    the query
     * @param regular  the reqular impressions
     * @param promoted the promoted impressions
     */
    public final void addImpressions(final Query query, final int regular, final int promoted) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            setImpressions(query, regular, promoted);
        } else {
            addImpressions(index, regular, promoted);
        }
    }

    /**
     * Adds the impressions associated with the query.
     *
     * @param query       the query
     * @param regular     the reqular impressions
     * @param promoted    the promoted impressions
     * @param ad          the ad shown
     * @param positionSum the sum of positions over all impressions
     */
    public final void addImpressions(final Query query, final int regular, final int promoted, final Ad ad,
                                     final double positionSum) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            setImpressions(query, regular, promoted, ad, positionSum);
        } else {
            addImpressions(index, regular, promoted, ad, positionSum);
        }
    }

    /**
     * Adds the impressions associated with the query.
     *
     * @param index    the query index
     * @param regular  the reqular impressions
     * @param promoted the promoted impressions
     */
    public final void addImpressions(final int index, final int regular, final int promoted) {
        lockCheck();

        getEntry(index).addImpressions(regular, promoted);

    }

    /**
     * Adds the impressions associated with the query.
     *
     * @param index       the query index
     * @param regular     the reqular impressions
     * @param promoted    the promoted impressions
     * @param ad          the ad shown
     * @param positionSum the sum of positions over all impressions
     */
    public final void addImpressions(final int index, final int regular, final int promoted, final Ad ad,
                                     final double positionSum) {
        lockCheck();

        getEntry(index).addImpressions(regular, promoted);
        getEntry(index).setAd(ad);
        getEntry(index).addPosition(positionSum);
    }

    /**
     * Sets the impressions associated with the query.
     *
     * @param index               the query index
     * @param regularImpressions  the regular impressions
     * @param promotedImpressions the promoted impressions
     */
    public final void setImpressions(final int index, final int regularImpressions, final int promotedImpressions) {
        lockCheck();
        getEntry(index).setImpressions(regularImpressions, promotedImpressions);
    }

    /**
     * Sets the impressions associated with the query.
     *
     * @param index       the query index
     * @param regular     the regular impressions
     * @param promoted    the promoted impressions
     * @param ad          the ad shown
     * @param positionSum the sum of positions over all impressions
     */
    public final void setImpressions(final int index, final int regular, final int promoted, final Ad ad,
                                     final double positionSum) {
        lockCheck();
        getEntry(index).setImpressions(regular, promoted);
        getEntry(index).setPositionSum(positionSum);
        getEntry(index).setAd(ad);
    }

    /**
     * Sets the clicks for associated query.
     *
     * @param query  the query
     * @param clicks the clicks
     */
    public final void setClicks(final Query query, final int clicks) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            index = addQuery(query);
        }

        setClicks(index, clicks);

    }

    /**
     * Sets the clicks for associated query.
     *
     * @param query  the query
     * @param clicks the clicks
     * @param cost   the cost
     */
    public final void setClicks(final Query query, final int clicks, final double cost) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            index = addQuery(query);
        }

        setClicks(index, clicks, cost);

    }

    /**
     * Sets the clicks for associated query.
     *
     * @param index  the query index
     * @param clicks the clicks
     */
    public final void setClicks(final int index, final int clicks) {
        lockCheck();
        getEntry(index).setClicks(clicks);
    }

    /**
     * Sets the clicks for associated query.
     *
     * @param index  the query index
     * @param clicks the clicks
     * @param cost   the cost
     */
    public final void setClicks(final int index, final int clicks, final double cost) {
        lockCheck();
        getEntry(index).setClicks(clicks);
        getEntry(index).setCost(cost);
    }

    /**
     * Adds the clicks to the associated query.
     *
     * @param query  the query
     * @param clicks the clicks
     */
    public final void addClicks(final Query query, final int clicks) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            setClicks(query, clicks);
        } else {
            addClicks(index, clicks);
        }
    }

    /**
     * Adds the clicks to the associated query.
     *
     * @param query  the query
     * @param clicks the clicks
     * @param cost   the cost
     */
    public final void addClicks(final Query query, final int clicks, final double cost) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            setClicks(query, clicks, cost);
        } else {
            addClicks(index, clicks, cost);
        }
    }

    /**
     * Adds the clicks to the associated query.
     *
     * @param index  the query index
     * @param clicks the clicks
     */
    public final void addClicks(final int index, final int clicks) {
        lockCheck();
        getEntry(index).addClicks(clicks);
    }

    /**
     * Adds the clicks to the associated query.
     *
     * @param index  the query index
     * @param clicks the clicks
     * @param cost   the cost
     */
    public final void addClicks(final int index, final int clicks, final double cost) {
        lockCheck();
        getEntry(index).addClicks(clicks);
        getEntry(index).addCost(cost);
    }

    /**
     * Adds the cost to the associated query.
     *
     * @param query the query
     * @param cost  the cost
     */
    public final void addCost(final Query query, final double cost) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            setCost(query, cost);
        } else {
            addCost(index, cost);
        }
    }

    /**
     * Adds the cost to the associated query.
     *
     * @param index the query index
     * @param cost  the cost
     */
    public final void addCost(final int index, final double cost) {
        lockCheck();
        getEntry(index).addCost(cost);
    }

    /**
     * Returns the average position for the associated query.
     *
     * @param query the query
     * @return the average position for the associated query.
     */
    public final double getPosition(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? Double.NaN : getPosition(index);
    }

    /**
     * Returns the average position for the associated query.
     *
     * @param index the query index
     * @return the average position for the associated query.
     */
    public final double getPosition(final int index) {
        return getEntry(index).getPosition();
    }

    /**
     * Returns the average CPC for the associated query.
     *
     * @param query the query
     * @return the average CPC for the associated query.
     */
    public final double getCPC(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? Double.NaN : getCPC(index);
    }

    /**
     * Returns the average CPC for the associated query.
     *
     * @param index the query index
     * @return the average CPC for the associated query.
     */
    public final double getCPC(final int index) {
        return getEntry(index).getCPC();
    }

    /**
     * Returns the total number of impressions for the associated query.
     *
     * @param query the query
     * @return the total number of impressions for the associated query.
     */
    public final int getImpressions(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? 0 : getImpressions(index);
    }

    /**
     * Returns the total number of impressions for the associated query.
     *
     * @param index the query index
     * @return the total number of impressions for the associated query.
     */
    public final int getImpressions(final int index) {
        return getEntry(index).getImpressions();
    }

    /**
     * Returns the total number of regular impressions for the associated query.
     *
     * @param query the query
     * @return the total number of regular impressions for the associated query.
     */
    public final int getRegularImpressions(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? 0 : getRegularImpressions(index);
    }

    /**
     * Returns the total number of regular impressions for the associated query.
     *
     * @param index the query index
     * @return the total number of regular impressions for the associated query.
     */
    public final int getRegularImpressions(final int index) {
        return getEntry(index).getRegularImpressions();
    }

    /**
     * Returns the total number of promoted impressions for the associated query.
     *
     * @param query the query
     * @return the total number of promoted impressions for the associated query.
     */
    public final int getPromotedImpressions(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? 0 : getPromotedImpressions(index);
    }

    /**
     * Returns the total number of promoted impressions for the associated query.
     *
     * @param index the query index
     * @return the total number of promoted impressions for the associated query.
     */
    public final int getPromotedImpressions(final int index) {
        return getEntry(index).getPromotedImpressions();
    }

    /**
     * Returns the total number of clicks for the associated query.
     *
     * @param query the query
     * @return the total number of clicks for the associated query.
     */
    public final int getClicks(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? 0 : getClicks(index);
    }

    /**
     * Returns the total number of clicks for the associated query.
     *
     * @param index the query index
     * @return the total number of clicks for the associated query.
     */
    public final int getClicks(final int index) {
        return getEntry(index).getClicks();
    }

    /**
     * Returns the total cost for the associated query.
     *
     * @param query the query
     * @return the total cost for the associated query.
     */
    public final double getCost(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? 0.0 : getCost(index);
    }

    /**
     * Returns the total cost for the associated query.
     *
     * @param index the query index
     * @return the total cost for the associated query.
     */
    public final double getCost(final int index) {
        return getEntry(index).getCost();
    }

    /**
     * Returns the average position for the associated query and advertiser.
     *
     * @param query      the query
     * @param advertiser the advertiser
     * @return the average position for the associated query and advertiser.
     */
    public final double getPosition(final Query query, final String advertiser) {
        int index = indexForEntry(query);

        return index < 0 ? Double.NaN : getPosition(index, advertiser);
    }

    /**
     * Returns the average position for the associated query and advertiser.
     *
     * @param index      the query index
     * @param advertiser the advertiser
     * @return the average position for the associated query and advertiser.
     */
    public final double getPosition(final int index, final String advertiser) {
        return getEntry(index).getPosition(advertiser);
    }

    /**
     * Sets the average position for the associated query and advertiser.
     *
     * @param query      the query
     * @param advertiser the advertiser
     * @param position   the average position for the associated query and advertiser.
     */
    public final void setPosition(final Query query, final String advertiser, final double position) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            index = addQuery(query);
        }

        setPosition(index, advertiser, position);
    }

    /**
     * Sets the average position for the associated query and advertiser.
     *
     * @param index      the query index
     * @param advertiser the advertiser
     * @param position   the average position for the associated query and advertiser.
     */
    public final void setPosition(final int index, final String advertiser, final double position) {
        lockCheck();

        getEntry(index).setPosition(advertiser, position);
    }

    /**
     * Returns the shown ad for the associated query.
     *
     * @param query the query
     * @return the shown ad for the associated query.
     */
    public final Ad getAd(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? null : getAd(index);
    }

    /**
     * Returns the shown ad for the associated query.
     *
     * @param index the query index
     * @return the shown ad for the associated query.
     */
    public final Ad getAd(final int index) {
        return getEntry(index).getAd();
    }

    /**
     * Sets the shown ad for the associated query.
     *
     * @param query the query
     * @param ad    the ad
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
     * Sets the shown ad for the associated query.
     *
     * @param index the query index
     * @param ad    the ad
     */
    public final void setAd(final int index, final Ad ad) {
        lockCheck();

        getEntry(index).setAd(ad);
    }

    /**
     * Returns the shown ad for the associated query and advertiser.
     *
     * @param query      the query
     * @param advertiser the advertiser
     * @return the shown ad for the associated query and advertiser.
     */
    public final Ad getAd(final Query query, final String advertiser) {
        int index = indexForEntry(query);

        return index < 0 ? null : getAd(index, advertiser);
    }

    /**
     * Returns the shown ad for the associated query and advertiser.
     *
     * @param index      the query index
     * @param advertiser the advertiser
     * @return the shown ad for the associated query and advertiser.
     */
    public final Ad getAd(final int index, final String advertiser) {
        return getEntry(index).getAd(advertiser);
    }

    /**
     * Sets the shown ad for the associated query and advertiser.
     *
     * @param query      the query
     * @param advertiser the advertiser
     * @param ad         the shown ad for the associated query and advertiser.
     */
    public final void setAd(final Query query, final String advertiser, final Ad ad) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            index = addQuery(query);
        }

        setAd(index, advertiser, ad);
    }

    /**
     * Sets the shown ad for the associated query and advertiser.
     *
     * @param index      the query index
     * @param advertiser the advertiser
     * @param ad         the shown ad for the associated query and advertiser.
     */
    public final void setAd(final int index, final String advertiser, final Ad ad) {
        lockCheck();

        getEntry(index).setAd(advertiser, ad);
    }

    /**
     * Sets the shown ad and average position for the associated query and advertiser.
     *
     * @param query      the query
     * @param advertiser the advertiser
     * @param ad         the shown ad for the associated query and advertiser.
     * @param position   the average position for the associated query and advertiser.
     */
    public final void setAdAndPosition(final Query query, final String advertiser, final Ad ad, final double position) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            index = addQuery(query);
        }

        setAdAndPosition(index, advertiser, ad, position);
    }

    /**
     * Sets the shown ad and average position for the associated query and advertiser.
     *
     * @param index      the query index
     * @param advertiser the advertiser
     * @param ad         the shown ad for the associated query and advertiser.
     * @param position   the average position for the associated query and advertiser.
     */
    public final void setAdAndPosition(final int index, final String advertiser, final Ad ad, final double position) {
        lockCheck();

        getEntry(index).setAdAndPosition(advertiser, ad, position);
    }

    /**
     * Returns the set of advertisers with data for the given query.
     *
     * @param query the query
     * @return the set of advertisers with data for the given query.
     */
    public final Set<String> advertisers(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? Collections.EMPTY_SET : advertisers(index);
    }

    /**
     * Returns the set of advertisers with data for the given query.
     *
     * @param index the query index
     * @return the set of advertisers with data for the given query.
     */
    public final Set<String> advertisers(final int index) {
        return getEntry(index).advertisers();
    }

    /**
     * Query report entry holds the impressions, clicks, cost, average position, and ad displayed
     * by the advertiser for each query class during the period as well as the positions and
     * displayed ads of all advertisers during the period.
     *
     * @author Patrick Jordan, Lee Callender
     */
    public static class QueryReportEntry extends AbstractQueryEntry {
        /**
         * The promoted impressions.
         */
        private int promotedImpressions;
        /**
         * The regular impressions.
         */
        private int regularImpressions;
        /**
         * The clicks.
         */
        private int clicks;
        /**
         * The cost.
         */
        private double cost;
        /**
         * The sum of prositions for all impressions.
         */
        private double positionSum;
        /**
         * The ad shown.
         */
        private Ad ad;

        /**
         * The display report containing all of the advertisers display information.
         */
        private DisplayReport displayReport;

        /**
         * Creates an empty report entry.
         */
        public QueryReportEntry() {
            positionSum = 0.0;
            cost = 0.0;
            displayReport = new DisplayReport();
        }

        /**
         * Returns the total number of impressions.
         *
         * @return the total number of impressions.
         */
        public final int getImpressions() {
            return promotedImpressions + regularImpressions;
        }

        /**
         * Returns the total number of promoted impressions.
         *
         * @return the total number of promoted impressions.
         */
        public final int getPromotedImpressions() {
            return promotedImpressions;
        }

        /**
         * Sets the total number of promoted impressions.
         *
         * @param promotedImpressions the total number of promoted impressions.
         */
        public final void setPromotedImpressions(final int promotedImpressions) {
            this.promotedImpressions = promotedImpressions;
        }

        /**
         * Returns the total number of regular impressions.
         *
         * @return the total number of regular impressions.
         */
        public final int getRegularImpressions() {
            return regularImpressions;
        }

        /**
         * Sets the total number of regular impressions.
         *
         * @param regularImpressions the total number of regular impressions.
         */
        public final void setRegularImpressions(final int regularImpressions) {
            this.regularImpressions = regularImpressions;
        }

        /**
         * Sets the total number of regular and promoted impressions.
         *
         * @param regularImpressions  the total number of regular impressions.
         * @param promotedImpressions the total number of promoted impressions.
         */
        final void setImpressions(final int regularImpressions, final int promotedImpressions) {
            this.regularImpressions = regularImpressions;
            this.promotedImpressions = promotedImpressions;
        }

        /**
         * Adds the regular and promoted impressions.
         *
         * @param regular  the regular impressions.
         * @param promoted the promoted impressions.
         */
        final void addImpressions(final int regular, final int promoted) {
            this.regularImpressions += regular;
            this.promotedImpressions += promoted;
        }

        /**
         * Returns the number of clicks.
         *
         * @return the number of clicks.
         */
        public final int getClicks() {
            return clicks;
        }

        /**
         * Sets the number of clicks.
         *
         * @param clicks the number of clicks.
         */
        final void setClicks(final int clicks) {
            this.clicks = clicks;
        }

        /**
         * Adds the number of clicks.
         *
         * @param clicks the number of clicks.
         */
        final void addClicks(final int clicks) {
            this.clicks += clicks;
        }

        /**
         * Returns the average position.
         *
         * @return the average position.
         */
        public final double getPosition() {
            return positionSum / getImpressions();
        }

        /**
         * Adds the position sum to the total position sum.
         *
         * @param position a sum over positions
         */
        final void addPosition(final double position) {
            this.positionSum += position;
        }

        /**
         * Sets the total position sum.
         *
         * @param positionSum the total position sum.
         */
        final void setPositionSum(final double positionSum) {
            this.positionSum = positionSum;
        }

        /**
         * Returns the total cost.
         *
         * @return the total cost.
         */
        public final double getCost() {
            return cost;
        }

        /**
         * Sets the total cost.
         *
         * @param cost the total cost.
         */
        final void setCost(final double cost) {
            this.cost = cost;
        }

        /**
         * Adds the cost to the total cost.
         *
         * @param cost the cost.
         */
        final void addCost(final double cost) {
            this.cost += cost;
        }

        /**
         * Returns the average CPC.
         *
         * @return the average CPC
         */
        public final double getCPC() {
            return cost / ((double) clicks);
        }

        /**
         * Returns the shown ad.
         *
         * @return the shown ad
         */
        public final Ad getAd() {
            return ad;
        }

        /**
         * Sets the shown ad.
         *
         * @param ad the shown ad
         */
        public final void setAd(final Ad ad) {
            this.ad = ad;
        }

        /**
         * Returns the average position of an advertiser.
         *
         * @param advertiser the advertiser.
         * @return the average position of an advertiser
         */
        public final double getPosition(final String advertiser) {
            return displayReport.getPosition(advertiser);
        }

        /**
         * Sets the average position of an advertiser.
         *
         * @param advertiser the advertiser.
         * @param position   the average position of an advertiser
         */
        public final void setPosition(final String advertiser, final double position) {
            displayReport.setPosition(advertiser, position);
        }

        /**
         * Returns the ad shown by an advertiser.
         *
         * @param advertiser the advertiser
         * @return the ad shown by an advertiser
         */
        public final Ad getAd(final String advertiser) {
            return displayReport.getAd(advertiser);
        }

        /**
         * Sets the ad shown by an advertiser.
         *
         * @param advertiser the advertiser
         * @param ad         the ad shown by an advertiser
         */
        public final void setAd(final String advertiser, final Ad ad) {
            displayReport.setAd(advertiser, ad);
        }

        /**
         * Sets the average position and the ad shown by an advertiser.
         *
         * @param advertiser the advertiser
         * @param ad         the ad shown by an advertiser
         * @param position   the average position
         */
        public final void setAdAndPosition(final String advertiser, final Ad ad, final double position) {
            displayReport.setAdAndPosition(advertiser, ad, position);
        }

        /**
         * Returns the set of displayed advertisers.
         *
         * @return the set of displayed advertisers.
         */
        public final Set<String> advertisers() {
            return displayReport.keys();
        }

        /**
         * Reads the query report entry information.
         *
         * @param reader the reader to read the state in from.
         * @throws ParseException if an exception occured the query report entry information.
         */
        @Override
        protected final void readEntry(final TransportReader reader) throws ParseException {
            this.regularImpressions = reader.getAttributeAsInt("regularImpressions", 0);
            this.promotedImpressions = reader.getAttributeAsInt("promotedImpressions", 0);
            this.clicks = reader.getAttributeAsInt("clicks", 0);
            this.positionSum = reader.getAttributeAsDouble("positionSum", 0.0);
            this.cost = reader.getAttributeAsDouble("cost", 0.0);

            if (reader.nextNode(Ad.class.getSimpleName(), false)) {
                this.ad = (Ad) reader.readTransportable();
            }

            reader.nextNode(DisplayReport.class.getSimpleName(), true);
            this.displayReport = (DisplayReport) reader.readTransportable();

        }

        /**
         * Writes the query report entry information to the writer.
         *
         * @param writer the writer to write the entry state to
         */
        @Override
        protected final void writeEntry(final TransportWriter writer) {
            writer.attr("regularImpressions", regularImpressions);
            writer.attr("promotedImpressions", promotedImpressions);
            writer.attr("clicks", clicks);
            writer.attr("positionSum", positionSum);
            writer.attr("cost", cost);

            if (ad != null) {
                writer.write(ad);
            }

            writer.write(displayReport);
        }

        /**
         * Returns the string representation of the query report entry.
         *
         * @return the string representation of the query report entry.
         */
        @Override
        public final String toString() {
            return String.format("(%s regular_impr: %d promoted_impr: %d clicks: %d pos: %f cpc: %f advertisers: %s)",
                    getQuery(), regularImpressions, promotedImpressions,
                    clicks, getPosition(), getCPC(), displayReport);
        }
    }

    /**
     * Display report entry contains the positions and displayed ads of the advertiser during the period.
     *
     * @author Patrick Jordan
     */
    public static class DisplayReportEntry extends AbstractAdvertiserEntry {
        /**
         * The shown ad.
         */
        private Ad ad;
        /**
         * The average position.
         */
        private double position;

        /**
         * Creates a new display report entry with a position set to <code>NaN</code> and a <code>null</code> ad.
         */
        public DisplayReportEntry() {
            position = Double.NaN;
        }

        /**
         * Returns the shown ad.
         *
         * @return the shown ad.
         */
        public final Ad getAd() {
            return ad;
        }

        /**
         * Sets the shown ad.
         *
         * @param ad the shown ad.
         */
        public final void setAd(final Ad ad) {
            this.ad = ad;
        }

        /**
         * Returns the average position.
         *
         * @return the average position.
         */
        public final double getPosition() {
            return position;
        }

        /**
         * Sets the average position.
         *
         * @param position the average position.
         */
        public final void setPosition(final double position) {
            this.position = position;
        }

        /**
         * Sets the shown ad and the average position.
         *
         * @param ad       the shown ad
         * @param position the average position.
         */
        public final void setAdAndPosition(final Ad ad, final double position) {
            this.ad = ad;
            this.position = position;
        }

        /**
         * Reads the display report entry information.
         *
         * @param reader the reader to read the state in from.
         * @throws ParseException if an exception occured the display report entry information.
         */
        @Override
        protected final void readEntry(final TransportReader reader) throws ParseException {
            this.position = reader.getAttributeAsDouble("position", Double.NaN);

            if (reader.nextNode(Ad.class.getSimpleName(), false)) {
                this.ad = (Ad) reader.readTransportable();
            }
        }

        /**
         * Writes the display report entry information to the writer.
         *
         * @param writer the writer to write the entry state to
         */
        @Override
        protected final void writeEntry(final TransportWriter writer) {
            writer.attr("position", position);

            if (ad != null) {
                writer.write(ad);
            }
        }
    }

    /**
     * Display report contains the positions and displayed ads of all advertisers during the period for each query
     * class.
     *
     * @author Patrick Jordan
     */
    public static class DisplayReport
            extends AbstractAdvertiserKeyedReportTransportable<QueryReport.DisplayReportEntry> {
        /**
         * Creates a {@link DisplayReportEntry} keyed with the supplied advertiser.
         * @param advertiser the advertiser
         * @return a {@link DisplayReportEntry} keyed with the supplied advertiser.
         */
        protected final DisplayReportEntry createEntry(final String advertiser) {
            DisplayReportEntry entry = new DisplayReportEntry();
            entry.setAdvertiser(advertiser);
            return entry;
        }

        /**
         * Returns the {@link DisplayReportEntry} class.
         * @return {@link DisplayReportEntry} class.
         */
        protected final Class entryClass() {
            return DisplayReportEntry.class;
        }

        /**
         * Returns the average position for the advertiser.
         * @param advertiser the advertiser
         * @return the average position for the advertiser
         */
        public final double getPosition(final String advertiser) {
            int index = indexForEntry(advertiser);

            return index < 0 ? Double.NaN : getPosition(index);
        }

        /**
         * Returns the average position for the advertiser.
         * @param index the advertiser index
         * @return the average position for the advertiser
         */
        public final double getPosition(final int index) {
            return getEntry(index).getPosition();
        }

        /**
         * Sets the average position for the advertiser.
         * @param advertiser the advertiser
         * @param position the average position for the advertiser
         */
        public final void setPosition(final String advertiser, final double position) {
            lockCheck();

            int index = indexForEntry(advertiser);

            if (index < 0) {
                index = addAdvertiser(advertiser);
            }

            setPosition(index, position);

        }

        /**
         * Sets the average position for the advertiser.
         * @param index the advertiser index
         * @param position the average position for the advertiser
         */
        public final void setPosition(final int index, final double position) {
            lockCheck();
            getEntry(index).setPosition(position);
        }

        /**
         * Returns the shown ad for the advertiser.
         * @param advertiser the advertiser
         * @return the shown ad for the advertiser.
         */
        public final Ad getAd(final String advertiser) {
            int index = indexForEntry(advertiser);

            return index < 0 ? null : getAd(index);
        }

        /**
         * Returns the shown ad for the advertiser.
         * @param index the advertiser index
         * @return the shown ad for the advertiser.
         */
        public final Ad getAd(final int index) {
            return getEntry(index).getAd();
        }

        /**
         * Sets the shown ad for the advertiser.
         * @param advertiser the advertiser
         * @param ad the shown ad for the advertiser.
         */
        public final void setAd(final String advertiser, final Ad ad) {
            lockCheck();

            int index = indexForEntry(advertiser);

            if (index < 0) {
                index = addAdvertiser(advertiser);
            }

            setAd(index, ad);

        }

        /**
         * Sets the shown ad for the advertiser.
         * @param index the advertiser index
         * @param ad the shown ad for the advertiser.
         */
        public final void setAd(final int index, final Ad ad) {
            lockCheck();
            getEntry(index).setAd(ad);
        }

        /**
         * Sets the shown ad and the average position for the advertiser.
         * @param advertiser the advertiser
         * @param ad the shown ad for the advertiser.
         * @param position the average position for the advertiser.
         */
        public final void setAdAndPosition(final String advertiser, final Ad ad, final double position) {
            lockCheck();

            int index = indexForEntry(advertiser);

            if (index < 0) {
                index = addAdvertiser(advertiser);
            }

            setAdAndPosition(index, ad, position);

        }

        /**
         * Sets the shown ad and the average position for the advertiser.
         * @param index the advertiser index
         * @param ad the shown ad for the advertiser.
         * @param position the average position for the advertiser.
         */
        public final void setAdAndPosition(final int index, final Ad ad, final double position) {
            lockCheck();
            getEntry(index).setAdAndPosition(ad, position);
        }
    }
}
