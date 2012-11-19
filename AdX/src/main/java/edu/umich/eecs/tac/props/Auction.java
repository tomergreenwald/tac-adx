/*
 * Auction.java
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

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;

import java.text.ParseException;

/**
 * Auction contains a {@link Ranking} and a {@link Pricing} for a {@link Query}.
 *
 * @author Patrick Jordan, Lee Callender
 */
public class Auction extends AbstractTransportable {
    /**
     * The ranking for the auction.
     */
    private Ranking ranking;
    /**
     * The pricing for the auction.
     */
    private Pricing pricing;
    /**
     * The user query used to generate the auction.
     */
    private Query query;

    /**
     * Return the ranking.
     * @return the ranking.
     */
    public final Ranking getRanking() {
        return ranking;
    }

    /**
     * Sets the ranking.
     * @param ranking the ranking.
     */
    public final void setRanking(final Ranking ranking) {
        lockCheck();
        this.ranking = ranking;
    }

    /**
     * Returns the pricing.
     * @return the pricing.
     */
    public final Pricing getPricing() {
        return pricing;
    }

    /**
     * Sets the pricing.
     * @param pricing the pricing.
     */
    public final void setPricing(final Pricing pricing) {
        lockCheck();
        this.pricing = pricing;
    }

    /**
     * Returns the query.
     * @return the query.
     */
    public final Query getQuery() {
        return query;
    }

    /**
     * Sets the query.
     * @param query the query.
     */
    public final void setQuery(final Query query) {
        lockCheck();
        this.query = query;
    }

    /**
     * Reads the ranking and pricing from the reader.
     * @param reader the reader to read data from.
     * @throws ParseException if an exception occurs reading the ranking and pricing.
     */
    @Override
    protected final void readWithLock(final TransportReader reader) throws ParseException {
        if (reader.nextNode(Ranking.class.getSimpleName(), false)) {
            this.ranking = (Ranking) reader.readTransportable();
        }

        if (reader.nextNode(Pricing.class.getSimpleName(), false)) {
            this.pricing = (Pricing) reader.readTransportable();
        }

        if (reader.nextNode(Query.class.getSimpleName(), false)) {
            this.query = (Query) reader.readTransportable();
        }
    }

    /**
     * Writes the ranking and pricing to the writer.
     * @param writer the writer to write data to.
     */
    @Override
    protected final void writeWithLock(final TransportWriter writer) {
        if (ranking != null) {
            writer.write(ranking);
        }

        if (pricing != null) {
            writer.write(pricing);
        }

        if (query != null) {
            writer.write(query);
        }
    }
}
