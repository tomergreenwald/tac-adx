/*
 * RetailCatalog.java
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
import java.util.*;


/**
 * The class holds the available products, which the users have preferences
 * over. In addition, the advertiser sales profit per conversion is given for
 * each product.
 *
 * @author Patrick Jordan, Lee Callender
 * @see <a href="http://aa.tradingagents.org/documentation">TAC Documentation</a>
 */
public class RetailCatalog extends AbstractKeyedEntryList<Product, RetailCatalog.RetailCatalogEntry> {

    private static final long serialVersionUID = 6299454928374287377L;

    /**
     * The supported manufacturers.
     */
    private Set<String> manufacturers;
    /**
     * The supported components.
     */
    private Set<String> components;

    /**
     * Creates a empty retail catalog.
     */
    public RetailCatalog() {
        manufacturers = new TreeSet<String>();
        components = new TreeSet<String>();
    }

    /**
     * Returns the set of manufacturers for the products.
     *
     * @return the set of manufacturers for the products.
     */
    public final Set<String> getManufacturers() {
        return manufacturers;
    }

    /**
     * Returns the set of components for the products.
     *
     * @return the set of components for the products.
     */
    public final Set<String> getComponents() {
        return components;
    }

    /**
     * Returns the advertiser sales profit for the product. The sales profit is zero if the product is not in the retail
     * catalog.
     *
     * @param product the product
     * @return the advertiser sales profit for the product.
     */
    public final double getSalesProfit(final Product product) {
        int index = indexForEntry(product);

        return index < 0 ? 0.0 : getSalesProfit(index);
    }

    /**
     * Returns the advertiser sales profit for the product at the index.
     *
     * @param index the index for the product
     * @return the advertiser sales profit for the product.
     */
    public final double getSalesProfit(final int index) {
        return getEntry(index).getSalesProfit();
    }

    /**
     * Sets the sales profit for the product.
     *
     * @param product the product whose sales profit is being set.
     * @param salesProfit the sales profit for the product.
     * @throws IllegalStateException if the retail catalog is locked.
     */
    public final void setSalesProfit(final Product product, final double salesProfit) throws IllegalStateException {
        lockCheck();

        int index = indexForEntry(product);

        if (index < 0) {
            index = addProduct(product);
        }

        setSalesProfit(index, salesProfit);
    }

    /**
     * Sets the sales profit for the product.
     *
     * @param index       the index for the product
     * @param salesProfit the sales profit for the product.
     * @throws IllegalStateException if the retail catalog is locked.
     */
    public final void setSalesProfit(final int index, final double salesProfit) throws IllegalStateException {
        lockCheck();
        getEntry(index).setSalesProfit(salesProfit);
    }

    /**
     * Adds the product to the retail catalog. This method delegates to {@link #addKey(Object)} .
     *
     * @param product the product to add.
     * @return the index of the newly added product.
     * @throws IllegalStateException if the retail catalog is locked.
     */
    public final int addProduct(final Product product) throws IllegalStateException {
        return addKey(product);
    }

    /**
     * Adds the the manufacturer and component of the supporting entry.
     * @param entry the entry to be added.
     *
     * @throws IllegalStateException if the retail catalog is locked.
     */
    @Override
    protected final void afterAddEntry(final RetailCatalogEntry entry) throws IllegalStateException {
        manufacturers.add(entry.getProduct().getManufacturer());
        components.add(entry.getProduct().getComponent());
    }

    /**
     * Creates a retail catalog entry for a product.
     * @param key the key for the created entry.
     * @return a retail catalog entry for a product.
     */
    @Override
    protected final RetailCatalogEntry createEntry(final Product key) {
        return new RetailCatalogEntry(key);
    }

    /**
     * Throws an {@link UnsupportedOperationException}.
     * @param index the index to remove.
     * @throws UnsupportedOperationException throws exception.
     */
    @Override
    protected final void beforeRemoveEntry(final int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Cannot remove retail catalog entry");
    }

    /**
     * Returns the {@link RetailCatalogEntry} class.
     * @return the {@link RetailCatalogEntry} class.
     */
    @Override
    protected final Class entryClass() {
        return RetailCatalogEntry.class;
    }

    /**
     * The retail catalog entry holds the sales profit for a {@link Product}.
     */
    public static class RetailCatalogEntry extends AbstractTransportableEntry<Product> {
        /**
         * The serial version ID.
         */
        private static final long serialVersionUID = -1140097762238141476L;
        /**
         * The sales profit.
         */
        private double salesProfit;

        /**
         * Creates a retail catalog entry.
         */
        public RetailCatalogEntry() {
        }

        /**
         * Creates a retail catalog entry for a product.
         * @param product the product.
         */
        public RetailCatalogEntry(final Product product) {
            setProduct(product);
        }

        /**
         * Returns the product.
         * @return the product.
         */
        public final Product getProduct() {
            return getKey();
        }

        /**
         * Sets the product.
         * @param product the product.
         */
        protected final void setProduct(final Product product) {
            setKey(product);
        }

        /**
         * Returns the sales profit.
         * @return the sales profit.
         */
        public final double getSalesProfit() {
            return salesProfit;
        }

        /**
         * Sets the sales profit.
         * @param salesProfit the sales profit.
         */
        protected final void setSalesProfit(final double salesProfit) {
            this.salesProfit = salesProfit;
        }

        /**
         * Reads the sales profit from the reader.
         * @param reader the reader to read the state in from.
         * @throws ParseException if an exception occurs reading the sales profit.
         */
        @Override
        protected final void readEntry(final TransportReader reader) throws ParseException {
            salesProfit = reader.getAttributeAsDouble("salesProfit", 0.0);
        }

        /**
         * Writes the sales profit to the writer.
         * @param writer the writer to write the entry state to
         */
        @Override
        protected final void writeEntry(final TransportWriter writer) {
            writer.attr("salesProfit", salesProfit);
        }

        /**
         * Returns the {@link Product} class.
         * @return the {@link Product} class.
         */
        @Override
        protected final String keyNodeName() {
            return Product.class.getSimpleName();
        }
    }
}
