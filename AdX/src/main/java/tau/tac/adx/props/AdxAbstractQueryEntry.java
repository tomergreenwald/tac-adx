/*
 * AbstractQueryEntry.java
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

import edu.umich.eecs.tac.props.AbstractTransportableEntry;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryEntry;

/**
 * This class provides a skeletal implementation of the {@link QueryEntry}
 * interface.
 * 
 * @author greenwald
 * @author Patrick Jordan
 */
public abstract class AdxAbstractQueryEntry extends
		AbstractTransportableEntry<AdxQuery> implements AdxQueryEntry {

	/**
	 * Returns the query key.
	 * 
	 * @return the query key.
	 */
	@Override
	public final AdxQuery getQuery() {
		return getKey();
	}

	/**
	 * Sets the key to the given query.
	 * 
	 * @param query
	 *            the query key.
	 */
	public final void setQuery(final AdxQuery query) {
		setKey(query);
	}

	/**
	 * Returns the transport name of the query key for externalization.
	 * 
	 * @return the {@link Class#getSimpleName() simple name} of the
	 *         {@link Query} class.
	 */
	@Override
	protected final String keyNodeName() {
		return AdxQuery.class.getSimpleName();
	}
}
