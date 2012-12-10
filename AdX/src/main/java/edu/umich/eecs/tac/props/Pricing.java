/*
 * Pricing.java
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

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import tau.tac.adx.props.AdLink;

/**
 * Pricing represents the CPC's charged to the advertisers when a user clicks.
 * 
 * @author Patrick Jordan
 * @see <a href="http://aa.tradingagents.org/documentation">TAC
 *      Documentation</a>
 */
public class Pricing extends AbstractTransportable {
	/**
	 * The price entry transport name.
	 */
	private static final String PRICE_ENTRY_TRANSPORT_NAME = "PriceEntry";

	/**
	 * The ad link-price mapping for the auction.
	 */
	private final Map<AdLink, Double> prices;

	/**
	 * Creates an empty pricing.
	 */
	public Pricing() {
		prices = new HashMap<AdLink, Double>();
	}

	/**
	 * Sets the price of the {@link AdLink}.
	 * 
	 * @param ad
	 *            the ad link.
	 * @param price
	 *            the CPC.
	 * @throws NullPointerException
	 *             if the ad link is null.
	 */
	public final void setPrice(final AdLink ad, final double price)
			throws NullPointerException {
		lockCheck();

		if (ad == null) {
			throw new NullPointerException("ad cannot be null");
		}

		prices.put(ad, price);
	}

	/**
	 * Returns the CPC of the {@link AdLink}.
	 * 
	 * @param ad
	 *            the ad link.
	 * @return the CPC of the {@link AdLink}.
	 */
	public final double getPrice(final AdLink ad) {
		Double price = prices.get(ad);

		if (price == null) {
			return Double.NaN;
		} else {
			return price;
		}
	}

	/**
	 * Returns the set of ad links priced.
	 * 
	 * @return the set of ad links priced.
	 */
	public final Set<AdLink> adLinks() {
		return prices.keySet();
	}

	/**
	 * Reads the pricing information from the reader.
	 * 
	 * @param reader
	 *            the reader to read data from.
	 * @throws ParseException
	 *             if exception occurs when reading the mapping.
	 */
	@Override
	protected final void readWithLock(final TransportReader reader)
			throws ParseException {
		prices.clear();
		while (reader.nextNode(PRICE_ENTRY_TRANSPORT_NAME, false)) {
			readPriceEntry(reader);
		}
	}

	/**
	 * Writes the pricing information to the reader.
	 * 
	 * @param writer
	 *            the writer to write data to.
	 */
	@Override
	protected final void writeWithLock(final TransportWriter writer) {
		for (Map.Entry<AdLink, Double> entry : prices.entrySet()) {
			writePriceEntry(writer, entry.getValue(), entry.getKey());
		}
	}

	/**
	 * Writes the price entry to the writer.
	 * 
	 * @param writer
	 *            the writer to write data to.
	 * @param price
	 *            the CPC
	 * @param adLink
	 *            the ad link
	 */
	protected final void writePriceEntry(final TransportWriter writer,
			final Double price, final AdLink adLink) {
		writer.node(PRICE_ENTRY_TRANSPORT_NAME);

		writer.attr("price", price);
		writer.write(adLink);

		writer.endNode(PRICE_ENTRY_TRANSPORT_NAME);
	}

	/**
	 * Reads the price entry from the writer.
	 * 
	 * @param reader
	 *            the reader to read data from.
	 * @throws ParseException
	 *             if exception occurs when a price entry
	 */
	protected final void readPriceEntry(final TransportReader reader)
			throws ParseException {
		reader.enterNode();

		double price = reader.getAttributeAsDouble("price", Double.NaN);

		reader.nextNode(AdLink.class.getSimpleName(), true);

		AdLink adLink = (AdLink) reader.readTransportable();

		setPrice(adLink, price);

		reader.exitNode();
	}
}
