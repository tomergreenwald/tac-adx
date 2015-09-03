/*
 * AdxBidBundleTest.java
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

import static edu.umich.eecs.tac.props.TransportableTestUtils.getBytesForTransportable;
import static edu.umich.eecs.tac.props.TransportableTestUtils.readFromBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Random;

import org.junit.Test;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import tau.tac.adx.props.generators.AdxQueryGenerator;
import tau.tac.adx.util.Utils;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.Product;

/**
 * @author Patrick Jordan
 */
public class AdxBidBundleTest {

	private final AdxQueryGenerator queryGenerator = Utils.getInjector()
			.getInstance(AdxQueryGenerator.class);

	@Test
	public void testAdxBidBundle() {
		AdxBidBundle bundle = new AdxBidBundle();
		AdxQuery query = queryGenerator.generate(1).iterator().next();
		Ad ad = new Ad();
		Product product = new Product();
		product.setComponent("c1");
		ad.setProduct(product);
		Random random = new Random();
		double bid = 20.1;

		int campaignID = random.nextInt();
		int weight = random.nextInt();
		bundle.addQuery(query, bid, ad, campaignID, weight);

		AdxQuery query2 = queryGenerator.generate(1).iterator().next();
		Ad ad2 = new Ad();
		Product product2 = new Product();
		product2.setComponent("c2");
		ad2.setProduct(product2);

		double bid2 = 50.5;
		bundle.addQuery(query2, bid2, ad2, campaignID, weight,
				AdxBidBundle.PERSISTENT_SPEND_LIMIT);

		assertEquals(bundle.size(), 2);

		bundle.setCampaignDailySpendLimit(100.5);
		bundle.setDailyLimit(query, 15.0);
		bundle.setDailyLimit(1, 200.0);

		int index = 0;
		assertEquals(bundle.getQuery(index), query);
		assertEquals(bundle.getBid(query), bid, 0);
		assertEquals(bundle.getBid(index), bid, 0);
		assertEquals(bundle.getAd(query), ad);
		assertEquals(bundle.getAd(index), ad);
		assertEquals(bundle.getCampaignDailySpendLimit(), 100.5, 0);
		assertEquals(bundle.getDailyLimit(query), 15.0, 0);
		assertEquals(bundle.getDailyLimit(1), 200.0, 0);
		assertEquals(campaignID, bundle.getCampaignId(index), 0);
		assertEquals(weight, bundle.getWeight(index), 0);

		bundle.setBid(query, 100.0);
		assertEquals(bundle.getBid(query), 100.0, 0);
		Ad ad3 = new Ad();
		Product product3 = new Product();
		product3.setComponent("c3");
		ad3.setProduct(product3);
		bundle.setAd(query, ad3);

		assertEquals(bundle.getAd(query), ad3);

		AdxQuery query4 = queryGenerator.generate(1).iterator().next();
		bundle.setBid(query4, 200.0);
		assertEquals(bundle.getBid(query4), 200.0, 0);

		AdxQuery query5 = queryGenerator.generate(1).iterator().next();
		Ad ad5 = new Ad();
		Product product5 = new Product();
		product.setComponent("c5");
		ad5.setProduct(product5);
		bundle.setAd(query5, ad5);
		assertEquals(bundle.getAd(query5), ad5);

		AdxQuery query6 = queryGenerator.generate(1).iterator().next();
		bundle.setDailyLimit(query6, 57.2);
		assertEquals(bundle.getDailyLimit(query6), 57.2, 0);

		AdxQuery query7 = queryGenerator.generate(1).iterator().next();
		Ad ad7 = new Ad();
		Product product7 = new Product();
		product.setComponent("c7");
		ad7.setProduct(product7);
		bundle.setBidAndAd(query7, 64.3, ad7);
		assertEquals(bundle.getAd(query7), ad7);
		assertEquals(bundle.getBid(query7), 64.3, 0);

		bundle.setBidAndAd(query7, 24.5, ad3);
		assertEquals(bundle.getAd(query7), ad3);
		assertEquals(bundle.getBid(query7), 24.5, 0);

		Iterator<AdxQuery> i = bundle.iterator();
		assertEquals(i.next(), query);
		assertEquals(i.next(), query2);

		boolean thrown = false;
		try {
			i.remove();
		} catch (UnsupportedOperationException e) {
			thrown = true;
		}
		if (!thrown)
			fail("remove should not be supported");
	}

	@Test
	public void testGets() {
		AdxBidBundle instance = new AdxBidBundle();
		assertNull(instance.getAd(null));
		assertEquals(instance.getBid(null), Double.NaN, 0);
		assertEquals(instance.getDailyLimit(null), Double.NaN, 0);
	}

	@Test
	public void testValidTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		AdxBidBundle bundle = new AdxBidBundle();

		assertNotNull(bundle);

		bundle.addQuery(queryGenerator.generate(1).iterator().next());
		AdxQuery q = queryGenerator.generate(1).iterator().next();
		Ad ad = new Ad();
		Product product = new Product();
		product.setComponent("c1");
		product.setManufacturer("man1");
		ad.setProduct(product);
		Random random = new Random();
		int campaignId = random.nextInt();
		int weight = random.nextInt();
		bundle.addQuery(q, 100.5, ad, campaignId, weight);

		assertEquals(bundle.size(), 2);

		byte[] buffer = getBytesForTransportable(writer, bundle);
		AdxBidBundle received = readFromBytes(reader, buffer,
				bundle.getTransportName());

		assertEquals(received.getCampaignDailySpendLimit(), Double.NaN, 0);
		assertNotNull(bundle);
		assertNotNull(received);
		assertEquals(received.size(), 2);

		bundle.lock();
		buffer = getBytesForTransportable(writer, bundle);
		received = readFromBytes(reader, buffer, "AdxBidBundle");

		assertNotNull(bundle);
		assertNotNull(received);
		assertEquals(received.size(), 2);

		assertEquals(bundle.getEntry(0), received.getEntry(0));
	}

	@Test
	public void testEmptyTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		AdxBidBundle bundle = new AdxBidBundle();

		assertNotNull(bundle);
		assertEquals(bundle.size(), 0);

		byte[] buffer = getBytesForTransportable(writer, bundle);
		AdxBidBundle received = readFromBytes(reader, buffer,
				bundle.getTransportName());

		assertNotNull(bundle);
		assertNotNull(received);
		assertEquals(received.size(), 0);

		bundle.lock();
		buffer = getBytesForTransportable(writer, bundle);
		received = readFromBytes(reader, buffer, bundle.getTransportName());

		assertNotNull(bundle);
		assertNotNull(received);
		assertEquals(received.size(), 0);
	}

	@Test(expected = IllegalStateException.class)
	public void testRemoveEntry() {
		AdxBidBundle bundle = new AdxBidBundle();

		assertEquals(bundle.size(), 0);

		bundle.addQuery(new AdxQuery());

		assertEquals(bundle.size(), 1);

		bundle.removeEntry(0);

		assertEquals(bundle.size(), 0);

		bundle.addQuery(new AdxQuery());
		bundle.lock();

		bundle.removeEntry(0);
	}

	@Test
	public void testGetEntry() {
		AdxBidBundle bundle = new AdxBidBundle();
		bundle.addQuery(new AdxQuery());

		assertNotNull(bundle.getEntry(new AdxQuery()));
		assertNull(bundle.getEntry(null));
	}
}
