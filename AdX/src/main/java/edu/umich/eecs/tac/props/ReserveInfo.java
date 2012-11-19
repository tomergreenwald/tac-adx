/*
 * ReserveInfo.java
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
import java.util.Arrays;

/**
 * This class contains the auction reserve information released to the user and publisher agents at the begining of the
 * game.
 *
 * @see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>
 * @author Patrick Jordan
 */
public class ReserveInfo extends AbstractTransportable {
    /**
     * The promoted reserve score (see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>).
     */
    private double[] promotedReserve;
    /**
     * The regular reserve score (see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>).
     */
    private double[] regularReserve;

    public ReserveInfo() {
        promotedReserve = new double[QueryType.values().length];
        regularReserve = new double[QueryType.values().length];
    }
    /**
     * Returns the promoted reserve score.
     * @see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>
     * @return the promoted reserve score.
     */

    public final double getPromotedReserve(final QueryType queryType) {
        return promotedReserve[queryType.ordinal()];
    }

    /**
     * Sets the promoted reserve score.
     * @see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>
     * @param promotedReserve the promoted reserve score.
     */
    public final void setPromotedReserve(final QueryType queryType, final double promotedReserve) {
        lockCheck();
        this.promotedReserve[queryType.ordinal()] = promotedReserve;
    }

    /**
     * Returns the regular reserve score.
     * @see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>
     * @return the regular reserve score.
     */
    public final double getRegularReserve(final QueryType queryType) {
        return regularReserve[queryType.ordinal()];
    }

    /**
     * Returns the regular reserve score.
     * @see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>
     * @param regularReserve the regular reserve score.
     */
    public final void setRegularReserve(final QueryType queryType, final double regularReserve) {
        lockCheck();
        this.regularReserve[queryType.ordinal()] = regularReserve;
    }

    /**
     * Reads the reserve parameter information from the reader.
     * @param reader the reader to read data from.
     * @throws ParseException if exception occured while reading parameters.
     */
    @Override
    protected final void readWithLock(final TransportReader reader) throws ParseException {
//        promotedReserve = reader.getAttributeAsDouble("promotedReserve", 0.0);
        for (QueryType type : QueryType.values()) {
            promotedReserve[type.ordinal()] = reader.getAttributeAsDouble(
                    String.format("promotedReserve[%s]", type.name()), 0.0);
        }

        for (QueryType type : QueryType.values()) {
            regularReserve[type.ordinal()] = reader.getAttributeAsDouble(
                    String.format("regularReserve[%s]", type.name()), 0.0);
        }
//        regularReserve = reader.getAttributeAsDouble("regularReserve", 0.0);
    }

    /**
     * Writes the reserve parameter information to the writer.
     * @param writer the writer to write data to.
     */
    @Override
    protected final void writeWithLock(final TransportWriter writer) {
//        writer.attr("promotedReserve", promotedReserve);
        for (QueryType type : QueryType.values()) {
            writer.attr(String.format("promotedReserve[%s]", type.name()),
                    promotedReserve[type.ordinal()]);
        }
        for (QueryType type : QueryType.values()) {
            writer.attr(String.format("regularReserve[%s]", type.name()),
                    regularReserve[type.ordinal()]);
        }
//        writer.attr("regularReserve", regularReserve);
    }

    /**
     * Checks to see if the parameter configuration is the same.
     *
     * @param o the object to check equality
     * @return whether the object has the same parameter configuration.
     */
    @Override
    public final boolean equals(final Object o) {
        if (this == o)  {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReserveInfo that = (ReserveInfo) o;

//        return Double.compare(that.promotedReserve, promotedReserve) == 0
//               && Double.compare(that.regularReserve, regularReserve) == 0;

//        if (Double.compare(that.promotedReserve, promotedReserve) != 0) {
//            return false;
//        }
        if (!Arrays.equals(promotedReserve, that.promotedReserve)) {
            return false;
        }

        if (!Arrays.equals(regularReserve, that.regularReserve)) {
            return false;
        }
        return true;

    }

    /**
     * Returns the hash code from the reserve parameters.
     * @return the hash code from the reserve parameters.
     * This needs to be corrected
     */
    @Override
    public final int hashCode() {
        int result=0;
//        long temp;
//        temp = promotedReserve != +0.0d ? Double.doubleToLongBits(promotedReserve) : 0L;
//        result = (int) (temp ^ (temp >>> 32));
//        result = Arrays.hashCode(promotedReserve);
//        result = 31 * result + Arrays.hashCode(regularReserve);

//        temp = regularReserve != +0.0d ? Double.doubleToLongBits(regularReserve) : 0L;
//        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
