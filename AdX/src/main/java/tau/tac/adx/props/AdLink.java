/*
 * AdLink.java
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

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import tau.tac.adx.bids.BidProduct;
import edu.umich.eecs.tac.props.AbstractTransportable;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.Product;

/**
 * This class represents an ad link. It contains an {@link Ad} as well as a
 * string for the advertiser's address. Note that ad links are created by the
 * publisher from ads specified in an advertisers bid bundle and thus are not
 * directly used by the advertiser.
 * 
 * @author Lee Callender, Patrick Jordan
 */

public class AdLink extends AbstractTransportable implements BidProduct {
	/**
	 * The advertiser address.
	 */
	private String advertiser;
	/**
	 * The ad to be shown with the link.
	 */
	private Ad ad;

	/**
	 * Creates a generic ad link. Advertiser's address is initialized to
	 * <code> null </code> and a <code>null</code> ad.
	 */
	public AdLink() {
	}

	/**
	 * Creates an ad link with the supplied {@link Product} and advertiser. An
	 * {@link Ad} with the given product is created.
	 * 
	 * @param product
	 *            the product.
	 * @param advertiser
	 *            the advertiser address.
	 */
	public AdLink(final Product product, final String advertiser) {
		this(new Ad(product), advertiser);
	}

	/**
	 * Creates an ad link from a given {@link Ad ad} and advertiser.
	 * 
	 * @param ad
	 *            the ad.
	 * @param advertiser
	 *            the advertiser.
	 */
	public AdLink(final Ad ad, final String advertiser) {
		this.ad = ad;
		this.advertiser = advertiser;
	}

	/**
	 * Returns the advertiser's address.
	 * 
	 * @return the advertiser's address.
	 */
	public final String getAdvertiser() {
		return advertiser;
	}

	/**
	 * Returns the {@link Ad ad} backing the ad link.
	 * 
	 * @return the ad.
	 */
	public final Ad getAd() {
		return ad;
	}

	/**
	 * Sets the {@link Ad ad} backing the ad link.
	 * 
	 * @param ad
	 *            the ad.
	 */
	public final void setAd(final Ad ad) {
		this.ad = ad;
	}

	/**
	 * Specify and advertiser's address for this ad link.
	 * 
	 * @param advertiser
	 *            the advertiser's address contained in the ad link.
	 * @throws IllegalStateException
	 *             if the ad link is locked.
	 */
	public final void setAdvertiser(final String advertiser)
			throws IllegalStateException {
		lockCheck();
		this.advertiser = advertiser;
	}

	/**
	 * Reads the advertiser's address and {@link Ad ad} from the reader.
	 * 
	 * @param reader
	 *            the reader to read data from.
	 * @throws ParseException
	 *             if an exception occured reading the advertiser and {@link Ad
	 *             ad}.
	 */
	@Override
	protected final void readWithLock(final TransportReader reader)
			throws ParseException {
		advertiser = reader.getAttribute("advertiser", null);

		if (reader.nextNode(Ad.class.getSimpleName(), false)) {
			this.ad = (Ad) reader.readTransportable();
		}
	}

	/**
	 * Writes the advertiser's address and {@link Ad ad} to the writer.
	 * 
	 * @param writer
	 *            the writer to write data to.
	 */
	@Override
	protected final void writeWithLock(final TransportWriter writer) {
		if (advertiser != null) {
			writer.attr("advertiser", advertiser);
		}

		if (ad != null) {
			writer.write(ad);
		}
	}

	/**
	 * Returns <code>true</code> if the object is an {@link AdLink} and has the
	 * same {@link Ad ad} and advertiser.
	 * 
	 * @param o
	 *            the object to compare.
	 * @return <code>true</code> if the object is an {@link AdLink} and has the
	 *         same {@link Ad ad} and advertiser.
	 */
	@Override
	public final boolean equals(final Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		AdLink adLink = (AdLink) o;

		if (ad != null ? !ad.equals(adLink.ad) : adLink.ad != null) {
			return false;
		}

		if (advertiser != null ? !advertiser.equals(adLink.advertiser)
				: adLink.advertiser != null) {
			return false;
		}

		return true;
	}

	/**
	 * Returns a hash code based on the contained {@link Ad ad} and advertiser.
	 * 
	 * @return a hash code based on the contained {@link Ad ad} and advertiser.
	 */
	@Override
	public final int hashCode() {
		int result = advertiser != null ? advertiser.hashCode() : 0;
		result = 31 * result + (ad != null ? ad.hashCode() : 0);
		return result;
	}

	/**
	 * Returns a string representation of the ad link.
	 * 
	 * @return a string representation of the ad link.
	 */
	@Override
	public final String toString() {
		return String.format("(AdLink advertiser:%s ad:%s)", getAdvertiser(),
				getAd());
	}
}
