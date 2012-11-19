/*
 * UserPopulationState.java
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
 * @author Lee Callender
 */
public class UserPopulationState extends AbstractKeyedEntryList<Product, UserPopulationState.UserPopulationEntry> {
    private static final long serialVersionUID = 2656209779279027478L;

    public UserPopulationState() {
    }

    protected UserPopulationEntry createEntry(Product key) {
        return new UserPopulationEntry(key);  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected Class entryClass() {
        return UserPopulationEntry.class;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns the advertiser sales profit for the product. The sales profit is zero if the product is not in the retail
     * catalog.
     *
     * @param product the product
     * @return the advertiser sales profit for the product.
     */
    public final int[] getDistribution(final Product product) {
        int index = indexForEntry(product);

        return index < 0 ? null : getDistribution(index);
    }

    /**
     * Returns the advertiser sales profit for the product at the index.
     *
     * @param index the index for the product
     * @return the advertiser sales profit for the product.
     */
    public final int[] getDistribution(final int index) {
        return getEntry(index).getDistribution();
    }

    public void setDistribution(final Product product, final int[] distribution) {
        lockCheck();

        int index = indexForEntry(product);

        if (index < 0) {
            index = addProduct(product);
        }

        setDistribution(index, distribution);
    }

    public void setDistribution(final int index, final int[] distribution) {
        lockCheck();
        getEntry(index).setDistribution(distribution);
    }

    public final int addProduct(final Product product) throws IllegalStateException {
        return addKey(product);
    }

    public static class UserPopulationEntry extends AbstractTransportableEntry<Product> {
        /**
         * The serial version ID.
         */
        private static final long serialVersionUID = -4560192080485265951L;

        private int[] distribution;

        public UserPopulationEntry(Product key) {
            setProduct(key);
        }

        public UserPopulationEntry() {
        }

        public int[] getDistribution() {
            return distribution;
        }

        public void setDistribution(int[] distribution) {
            this.distribution = distribution;
        }

        /**
         * Returns the product.
         *
         * @return the product.
         */
        public final Product getProduct() {
            return getKey();
        }

        /**
         * Sets the product.
         *
         * @param product the product.
         */
        protected final void setProduct(final Product product) {
            setKey(product);
        }

        protected String keyNodeName() {
            return Product.class.getSimpleName();  //To change body of implemented methods use File | Settings | File Templates.
        }

        protected void readEntry(TransportReader reader) throws ParseException {
            distribution = reader.getAttributeAsIntArray("distribution");
        }

        protected void writeEntry(TransportWriter writer) {
            writer.attr("distribution", distribution);
        }
    }
}
