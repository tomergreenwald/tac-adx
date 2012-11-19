/*
 * SalesReport.java
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
 * Sales report is a daily report of the sales revenue and conversions an advertisers garners for each query class.
 *
 * @author Ben Cassell, Patrick Jordan, Lee Callender
 * @see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>
 */
public class SalesReport extends AbstractQueryKeyedReportTransportable<SalesReport.SalesReportEntry> {
    /**
     * The servial version id.
     */
    private static final long serialVersionUID = 3473199640271355791L;

    /**
     * Returns a {@link SalesReportEntry entry} with the {@link Query query} as the key.
     * @param query the query key.
     * @return a {@link SalesReportEntry entry} with the {@link Query query} as the key.
     */
    protected final SalesReportEntry createEntry(final Query query) {
        SalesReportEntry entry = new SalesReportEntry();
        entry.setQuery(query);
        return entry;
    }

    /**
     * Returns the {@link SalesReportEntry} class.
     * @return the {@link SalesReportEntry} class.
     */
    protected final Class entryClass() {
        return SalesReportEntry.class;
    }

    /**
     * Adds query data to the report.
     * @param query the query.
     * @param conversions the conversions.
     * @param revenue the revenue.
     */
    protected final void addQuery(final Query query, final int conversions, final double revenue) {
        int index = addQuery(query);
        SalesReportEntry entry = getEntry(index);
        entry.setQuery(query);
        entry.setConversions(conversions);
        entry.setRevenue(revenue);
    }

    /**
     * Adds conversions for a query.
     * @param query the query.
     * @param conversions the conversions.
     */
    public final void addConversions(final Query query, final int conversions) {
        int index = indexForEntry(query);

        if (index < 0) {
            setConversions(query, conversions);
        } else {
            addConversions(index, conversions);
        }
    }

    /**
     * Adds conversions for a query.
     * @param index the query index.
     * @param conversions the conversions.
     */
    public final void addConversions(final int index, final int conversions) {
        lockCheck();
        getEntry(index).addConversions(conversions);
    }

    /**
     * Adds revenue for a query.
     * @param query the query.
     * @param revenue the revenue.
     */
    public final void addRevenue(final Query query, final double revenue) {
        int index = indexForEntry(query);

        if (index < 0) {
            setRevenue(query, revenue);
        } else {
            addRevenue(index, revenue);
        }
    }

    /**
     * Adds revenue for a query.
     * @param index the query index.
     * @param revenue the revenue.
     */
    public final void addRevenue(final int index, final double revenue) {
        lockCheck();
        getEntry(index).addRevenue(revenue);
    }

    /**
     * Sets the conversions for a query.
     * @param query the query.
     * @param conversions the conversions.
     */
    public final void setConversions(final Query query, final int conversions) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            addQuery(query, conversions, 0.0);
        } else {
            setConversions(index, conversions);
        }
    }

    /**
     * Sets the conversions for a query.
     * @param index the query index.
     * @param conversions the conversions.
     */
    public final void setConversions(final int index, final int conversions) {
        lockCheck();
        getEntry(index).setConversions(conversions);
    }

    /**
     * Sets the revenue for a query.
     * @param query the query.
     * @param revenue the revenue.
     */
    public final void setRevenue(final Query query, final double revenue) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            addQuery(query, 0, revenue);
        } else {
            setRevenue(index, revenue);
        }
    }

    /**
     * Sets the revenue for a query.
     * @param index the query index.
     * @param revenue the revenue.
     */
    public final void setRevenue(final int index, final double revenue) {
        lockCheck();
        getEntry(index).setRevenue(revenue);
    }

    /**
     * Sets the conversions and revenue for a query.
     * @param query the query.
     * @param conversions the conversions.
     * @param revenue the revenue.
     */
    public final void setConversionsAndRevenue(final Query query, final int conversions, final double revenue) {
        lockCheck();

        int index = indexForEntry(query);

        if (index < 0) {
            addQuery(query, conversions, revenue);
        } else {
            setConversionsAndRevenue(index, conversions, revenue);
        }
    }

    /**
     * Sets the conversions and revenue for a query.
     * @param index the query index.
     * @param conversions the conversions.
     * @param revenue the revenue.
     */
    public final void setConversionsAndRevenue(final int index, final int conversions, final double revenue) {
        lockCheck();
        SalesReportEntry entry = getEntry(index);
        entry.setConversions(conversions);
        entry.setRevenue(revenue);
    }

    /**
     * Returns the conversions for a query.
     * @param query the query.
     * @return the conversions for a query.
     */
    public final int getConversions(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? 0 : getConversions(index);
    }

    /**
     * Returns the conversions for a query.
     * @param index the query index.
     * @return the conversions for a query.
     */
    public final int getConversions(final int index) {
        return getEntry(index).getConversions();
    }

    /**
     * Returns the revenue for a query.
     * @param query the query.
     * @return the revenue for a query.
     */
    public final double getRevenue(final Query query) {
        int index = indexForEntry(query);

        return index < 0 ? 0.0 : getRevenue(index);
    }

    /**
     * Returns the revenue for a query.
     * @param index the query index.
     * @return the revenue for a query.
     */
    public final double getRevenue(final int index) {
        return getEntry(index).getRevenue();
    }

    /**
     * Sales report entry holds the conversions and revenue for a query class.
     * @author Patrick Jordan
     */
    public static class SalesReportEntry extends AbstractQueryEntry {
        /**
         * The servial version id.
         */
        private static final long serialVersionUID = -3012145053844178964L;

        /**
         * The daily conversions.
         */
        private int conversions;

        /**
         * The daily revenue.
         */
        private double revenue;

        /**
         * Returns the conversions.
         * @return the conversions.
         */
        public final int getConversions() {
            return conversions;
        }

        /**
         * Sets the conversions.
         * @param conversions the conversions.
         */
        final void setConversions(final int conversions) {
            this.conversions = conversions;
        }

        /**
         * Adds the conversions.
         * @param conversions the conversions to add.
         */
        final void addConversions(final int conversions) {
            this.conversions += conversions;
        }

        /**
         * Returns the revenue.
         * @return the revenue.
         */
        public final double getRevenue() {
            return revenue;
        }

        /**
         * Sets the revenue.
         * @param revenue the revenue.
         */
        final void setRevenue(final double revenue) {
            this.revenue = revenue;
        }

        /**
         * Adds the revenue.
         * @param revenue the revenue.
         */
        final void addRevenue(final double revenue) {
            this.revenue += revenue;
        }

        /**
         * Reads the revenue and conversions from the reader.
         * @param reader the reader to read the state in from.
         * @throws ParseException if an exception occured reading the conversions and revenue.
         */
        @Override
        protected final void readEntry(final TransportReader reader) throws ParseException {
            this.conversions = reader.getAttributeAsInt("conversions", 0);
            this.revenue = reader.getAttributeAsDouble("revenue", 0.0);
        }

        /**
         * Writes the revenue and conversions to the writer.
         * @param writer the writer to write the entry state to
         */
        @Override
        protected final void writeEntry(final TransportWriter writer) {
            writer.attr("conversions", conversions);
            writer.attr("revenue", revenue);
        }

        /**
         * Returns the string representation of the sales report entry.
         * @return the string representation of the sales report entry.
         */
        @Override
        public final String toString() {
            return String.format("(%s conv: %d rev: %f)", getQuery(), conversions, revenue);
        }
    }
}
