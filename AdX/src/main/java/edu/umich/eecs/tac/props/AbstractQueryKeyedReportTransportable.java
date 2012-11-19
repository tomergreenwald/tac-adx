/*
 * AbstractQueryKeyedReportTransportable.java
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

/**
 * This class provides a skeletal implementation of the {@link AbstractKeyedEntryList} abstract class, where the key is
 * a {@link Query} object.
 *
 * @param <T> the query entry class
 *
 * @author Patrick Jordan
 */
public abstract class AbstractQueryKeyedReportTransportable<T extends QueryEntry>
        extends AbstractKeyedEntryList<Query, T> {

    /**
     * Sole constructor. (For invocation by subclass constructors, typically
     * implicit.)
     */
    public AbstractQueryKeyedReportTransportable() {
    }

    /**
     * Add a query key.
     *
     * @param query the query key to be added.
     * @return the index of the key
     */
    public final int addQuery(final Query query) {
        return addKey(query);
    }

    /**
     * Check whether the query key exists in the key set.
     *
     * @param query the query to test containment.
     * @return <code>true</code> if the query key exists in the key set and
     *         <code>false</code> otherwise.
     */
    public final boolean containsQuery(final Query query) {
        return containsKey(query);
    }

    /**
     * Get the query key at the specified index.
     *
     * @param index the key index.
     * @return the query key at the specified index.
     */
    public final Query getQuery(final int index) {
        return getKey(index);
    }
}
