/*
 * UserClickModel.java
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
import java.util.List;
import java.util.LinkedList;

/**
 * UserClickModel the parameters that generate user viewing behavior.
 * These parameters include (for each query class):
 * <ul>
 *  <li> Advertiser effects</li>
 *  <li> Continuation probabilities</li>
 * </ul>
 * @see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>
 *
 * @author Patrick Jordan
 */
public class UserClickModel extends AbstractTransportable {
    /**
     * The advertiser effects (see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>).
     */
    private double[][] advertiserEffects;
    /**
     * The continuation probabilities (see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>).
     */
    private double[] continuationProbabilities;
    /**
     * The query classes.
     */
    private Query[] queries;
    /**
     * The advertiser addresses.
     */
    private String[] advertisers;

    /**
     * Creates an empty user click model with no query classes nor advertisers.
     */
    public UserClickModel() {
        this(new Query[0], new String[0]);
    }

    /**
     * Creates a user click model initialized with the {@link Query queries} and advertisers.
     * @param queries the initial queries
     * @param advertisers the initial advertisers.
     */
    public UserClickModel(final Query[] queries, final String[] advertisers) {
        if (queries == null) {
            throw new NullPointerException("queries cannot be null");
        }

        if (advertisers == null) {
            throw new NullPointerException("advertisers cannot be null");
        }

        this.queries = queries;
        this.advertisers = advertisers;
        advertiserEffects = new double[queries.length][advertisers.length];
        continuationProbabilities = new double[queries.length];
    }

    /**
     * Returns the advertiser count.
     * @return the advertiser count.
     */
    public final int advertiserCount() {
        return advertisers.length;
    }

    /**
     * Returns the advertiser at the index.
     * @param index the index into the advertiser set.
     * @return the advertiser at the index.
     */
    public final String advertiser(final int index) {
        return advertisers[index];
    }

    /**
     * Returns the index for an advertiser and <code>-1</code> if the advertiser is not in the model.
     * @param advertiser the advertiser.
     * @return the index for an advertiser and <code>-1</code> if the advertiser is not in the model.
     */
    public final int advertiserIndex(final String advertiser) {
        for (int index = 0; index < advertisers.length; index++) {
            if (advertisers[index].equals(advertiser)) {
                return index;
            }
        }

        return -1;
    }

    /**
     * Returns the query count.
     * @return the query count.
     */
    public final int queryCount() {
        return queries.length;
    }

    /**
     * Returns the query at the index.
     * @param index the index into the query set.
     * @return the query at the index.
     */
    public final Query query(final int index) {
        return queries[index];
    }

    /**
     * Returns the index for a query and <code>-1</code> if the query is not in the model.
     * @param query the query.
     * @return the index for a query and <code>-1</code> if the query is not in the model.
     */
    public final int queryIndex(final Query query) {
        for (int index = 0; index < queries.length; index++) {
            if (queries[index].equals(query)) {
                return index;
            }
        }

        return -1;
    }

    /**
     * Returns the continuation probability for the query at the index.
     * @param queryIndex the query index.
     * @return the continuation probability for the query at the index.
     * @see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>
     */
    public final double getContinuationProbability(final int queryIndex) {
        return continuationProbabilities[queryIndex];
    }

    /**
     * Sets the continuation probability for the query at the index.
     * @param queryIndex the query index.
     * @param probability the continuation probability.
     * @see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>
     */
    public final void setContinuationProbability(final int queryIndex, final double probability) {
        lockCheck();
        continuationProbabilities[queryIndex] = probability;
    }

    /**
     * Returns the advertiser effect for the query and advertiser at the indices.
     * @param queryIndex the query index.
     * @param advertiserIndex the advertiser index.
     * @return the advertiser effect for the query and advertiser at the indices.
     * @see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>
     */
    public final double getAdvertiserEffect(final int queryIndex, final int advertiserIndex) {
        return advertiserEffects[queryIndex][advertiserIndex];
    }

    /**
     * Sets the advertiser effect for the query and advertiser at the indices.
     * @param queryIndex the query index.
     * @param advertiserIndex the advertiser index.
     * @param effect the advertiser effect.
     * @see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>
     */
    public final void setAdvertiserEffect(final int queryIndex, final int advertiserIndex, final double effect) {
        lockCheck();
        advertiserEffects[queryIndex][advertiserIndex] = effect;
    }

    /**
     * Reads the queries, advertisers, advertiser effects, and continuation probabilities from the reader.
     * @param reader the reader to read data from.
     * @throws ParseException if an exception occurs reading the parameters.
     */
    @Override
    protected final void readWithLock(final TransportReader reader) throws ParseException {
        List<Query> queryList = new LinkedList<Query>();

        String queryName = Query.class.getSimpleName();
        while (reader.nextNode(queryName, false)) {
            queryList.add((Query) reader.readTransportable());
        }

        queries = queryList.toArray(new Query[0]);

        List<String> advertiserList = new LinkedList<String>();
        while (reader.nextNode("advertiser", false)) {
            advertiserList.add(reader.getAttribute("name"));
        }

        advertisers = advertiserList.toArray(new String[0]);

        advertiserEffects = new double[queries.length][advertisers.length];
        continuationProbabilities = new double[queries.length];

        while (reader.nextNode("continuationProbability", false)) {
            int index = reader.getAttributeAsInt("index");
            double probability = reader.getAttributeAsDouble("probability");

            setContinuationProbability(index, probability);
        }

        while (reader.nextNode("advertiserEffect", false)) {
            int queryIndex = reader.getAttributeAsInt("queryIndex");
            int advertiserIndex = reader.getAttributeAsInt("advertiserIndex");
            double effect = reader.getAttributeAsDouble("effect");

            setAdvertiserEffect(queryIndex, advertiserIndex, effect);
        }
    }

    /**
     * Writes the queries, advertisers, advertiser effects, and continuation probabilities to the writer.
     * @param writer the writer to write data to.
     */
    @Override
    protected final void writeWithLock(final TransportWriter writer) {
        for (Query query : queries) {
            writer.write(query);
        }

        for (String advertiser : advertisers) {
            writer.node("advertiser").attr("name", advertiser).endNode("advertiser");
        }

        for (int queryIndex = 0; queryIndex < queries.length; queryIndex++) {
            writer.node("continuationProbability").attr("index", queryIndex)
                    .attr("probability", continuationProbabilities[queryIndex]).endNode("continuationProbability");
        }

        for (int queryIndex = 0; queryIndex < queries.length; queryIndex++) {
            for (int advertiserIndex = 0; advertiserIndex < advertisers.length; advertiserIndex++) {
                writer.node("advertiserEffect").attr("queryIndex", queryIndex)
                        .attr("advertiserIndex", advertiserIndex)
                        .attr("effect", advertiserEffects[queryIndex][advertiserIndex]).endNode("advertiserEffect");
            }
        }
    }
}
