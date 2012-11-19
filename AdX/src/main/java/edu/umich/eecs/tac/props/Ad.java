/*
 * Ad.java
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
 * This class represents an advertisement in the TAC/AA scenario. Advertisements
 * can be generic or targeted depending on whether the ad specifies a product.
 * Advertisers will primarily use this class with {@link BidBundle} in specifying
 * which advertisements to display for individual queries.
 *
 *
 * @author Patrick Jordan, Lee Callender
 */
public class Ad extends AbstractTransportable {
    /**
     * The product the ad uses to determine targeting.  A generic Ad has a <code>null</code> product.
     */
    private Product product;

    /**
     * Creates a generic ad.
     */
    public Ad() {
    }

    /**
     * Creates a targeted ad if <code>product</code> is not null. The ad is generic if the <code>product</code> is null.
     * @param product the targeting product. The ad is generic if the <code>product</code> is null.
     */
    public Ad(final Product product) {
        this.product = product;
    }

    /**
     * Returns <code>true</code> if the ad is generic and <code>false</code> if the ad is targeted.
     *
     * @return <code>true</code> if the ad is generic and <code>false</code> if the ad is targeted.
     */
    public final boolean isGeneric() {
        return product == null;
    }

    /**
     * Returns the product the ad is targeting. The product is <code>null</code> if the ad is generic.
     *
     * @return the product the ad is targeting. The product is <code>null</code> if the ad is generic.
     */
    public final Product getProduct() {
        return product;
    }

    /**
     * Sets the product for the ad. Setting the product to <code>null</code> sets the ad as generic.
     *
     * @param product the product for the ad. Setting the product to <code>null</code> sets the ad as generic.
     * @throws IllegalStateException if the ad is locked.
     */
    public final void setProduct(final Product product) throws IllegalStateException {
        lockCheck();
        this.product = product;
    }

    /**
     * Reads the product from the reader.
     * @param reader the reader to read data from.
     * @throws ParseException if there was an exception reading the product.
     */
    @Override
    protected final void readWithLock(final TransportReader reader) throws ParseException {
        if (reader.nextNode(Product.class.getSimpleName(), false)) {
            this.product = (Product) reader.readTransportable();
        }
    }

    /**
     * Writes the product to the writer.
     * @param writer the writer to write data to.
     */
    @Override
    protected final void writeWithLock(final TransportWriter writer) {
        if (product != null) {
            writer.write(product);
        }
    }

    /**
     * Creates a string that displays whether the ad is generic and the product, if targeted.
     * @return a string that displays whether the ad is generic and the product, if targeted.
     */
    @Override
    public final String toString() {
        return String.format("(Ad generic:%s product:%s)", isGeneric(), getProduct());
    }

    /**
     * Returns <code>true</code> if the object is an Ad and has the same product or lack thereof.
     * @param o the object to compare.
     * @return <code>true</code> if the object is an Ad and has the same product or lack thereof.
     */
    @Override
    public final boolean equals(final Object o) {
        if (this == o)  {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Ad ad = (Ad) o;

        return !(product != null ? !product.equals(ad.product) : ad.product != null);
    }

    /**
     * Returns a hash code based on the contained product.
     * @return a hash code based on the contained product.
     */
    @Override
    public final int hashCode() {
        return (product != null ? product.hashCode() : 0);
    }
}
