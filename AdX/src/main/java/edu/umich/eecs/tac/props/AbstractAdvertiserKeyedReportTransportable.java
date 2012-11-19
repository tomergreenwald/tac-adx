/*
 * AbstractAdvertiserKeyedReportTransportable.java
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
 * This class provides a skeletal implementation of a list containing
 * {@link edu.umich.eecs.tac.props.AdvertiserEntry advertiser entries} with
 * supporting methods for interacting entries specified by a given advertiser.
 *
 * @param <T> the advertiser entry class
 *
 * @author Patrick Jordan
 */
public abstract class AbstractAdvertiserKeyedReportTransportable<T extends AdvertiserEntry>
        extends AbstractKeyedEntryList<String, T> {

    /**
     * Adds a new key to the list. The {@link #createEntry} method creates the
     * new {@link AdvertiserEntry entry} with the specified advertiser. This
     * method delegates to {@link #addKey(Object)}.
     *
     * @param advertiser the advertiser used to add the new {@link AdvertiserEntry
     *                   advertiser entry}.
     * @return the index of the newly generated {@link AdvertiserEntry entry}.
     * @throws NullPointerException if the <code>advertiser</code> is <code>null</code>.
     */
    public final int addAdvertiser(final String advertiser) throws NullPointerException {
        return addKey(advertiser);
    }

    /**
     * Returns <code>true</code> if the advertiser key is in the list and
     * <code>false</code> otherwise. This method delegates to
     * {@link #containsKey(Object)}.
     *
     * @param advertiser the advertiser key to check for containment.
     * @return <code>true</code> if the key is in the list and
     *         <code>false</code> otherwise.
     */
    public final boolean containsAdvertiser(final String advertiser) {
        return containsKey(advertiser);
    }
}
