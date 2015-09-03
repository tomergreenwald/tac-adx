/*
 * PricingTest.java
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

import static edu.umich.eecs.tac.props.TransportableTestUtils.getBytesForTransportable;
import static edu.umich.eecs.tac.props.TransportableTestUtils.readFromBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import tau.tac.adx.props.AdxInfoContextFactory;
import tau.tac.adx.props.AdLink;

/**
 * 
 * @author Kemal Eren
 */
public class PricingTest {

	@Test
	public void testSetPrice() {
		Pricing instance = new Pricing();

		AdLink ad = null;
		double price = 0.00;

		boolean thrown = false;
		try {
			instance.setPrice(ad, price);
		} catch (NullPointerException e) {
			thrown = true;
		}
		if (!thrown) {
			fail("Should throw a NullPointerException if attempt to add null ad");
		}

		Product product = new Product("manufacturer_1", "component_1");
		String advertisor = "advertisor_1";
		ad = new AdLink(product, advertisor);
		instance.setPrice(ad, price);
		assertTrue(instance.adLinks().size() == 1);
		assertTrue(instance.adLinks().contains(ad));
		assertTrue(instance.getPrice(ad) == 0.00);

		price = 100.00;
		instance.setPrice(ad, price);
		assertTrue(instance.adLinks().size() == 1);
		assertTrue(instance.adLinks().contains(ad));
		assertTrue(instance.getPrice(ad) == 100.00);

		product = new Product("manufacturer_2", "component_2");
		advertisor = "advertisor_2";
		price = 200;
		AdLink ad2 = new AdLink(product, advertisor);
		instance.setPrice(ad2, price);
		assertTrue(instance.adLinks().size() == 2);
		assertTrue(instance.adLinks().contains(ad));
		assertTrue(instance.adLinks().contains(ad2));
		assertTrue(instance.getPrice(ad) == 100.00);
		assertTrue(instance.getPrice(ad2) == 200.00);
	}

	@Test
	public void testGetPrice() {
		Pricing instance = new Pricing();
		AdLink ad = null;
		assertTrue(Double.isNaN((instance.getPrice(ad))));

		double price = 0.00;
		Product product = new Product("manufacturer_1", "component_1");
		String advertisor = "advertisor_1";
		ad = new AdLink(product, advertisor);
		instance.setPrice(ad, price);
		assertTrue(instance.getPrice(ad) == 0.00);

		price = 100.00;
		instance.setPrice(ad, price);
		assertTrue(instance.getPrice(ad) == 100.00);

		product = new Product("manufacturer_2", "component_2");
		advertisor = "advertisor_2";
		ad = new AdLink(product, advertisor);
		assertTrue(Double.isNaN((instance.getPrice(ad))));

		instance.setPrice(ad, price);
		assertTrue(instance.getPrice(ad) == 100.00);
	}

	@Test
	public void testAds() {
		Pricing instance = new Pricing();
		Set<AdLink> expResult = new HashSet<AdLink>();
		assertEquals(expResult, instance.adLinks());

		Product product = new Product("manufacturer_1", "component_1");
		String advertisor = "advertisor_1";
		AdLink ad = new AdLink(product, advertisor);
		double price = 1.00;
		expResult = new HashSet<AdLink>();
		expResult.add(ad);
		instance.setPrice(ad, price);
		assertEquals(expResult, instance.adLinks());
		assertTrue(expResult.contains(ad));
		Iterator it = instance.adLinks().iterator();
		ad = (AdLink) it.next();
		assertTrue(instance.getPrice(ad) == 1.00);

		int total_ads = 100;
		for (int i = 2; i <= total_ads; i++) {
			String num = Integer.toString(i);
			product = new Product("manufacturer_" + num, "component_" + num);
			advertisor = "advertisor_" + num;
			ad = new AdLink(product, advertisor);
			price++;
			instance.setPrice(ad, price);
		}
		assertEquals(instance.adLinks().size(), total_ads);
		assertTrue(instance.adLinks().contains(ad));

		product = new Product("manufacturer_51", "component_51");
		advertisor = "advertisor_51";
		ad = new AdLink(product, advertisor);
		assertTrue(instance.adLinks().contains(ad));

		product = new Product("manufacturer_0", "component_0");
		advertisor = "advertisor_0";
		ad = new AdLink(product, advertisor);
		assertFalse(instance.adLinks().contains(ad));
	}

	@Test
	public void testValidTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		Pricing instance = new Pricing();
		Product product = new Product("manufacturer_1", "component_1");
		String advertisor = "advertisor_1";
		AdLink ad = new AdLink(product, advertisor);
		double price = 100.00;
		instance.setPrice(ad, price);

		byte[] buffer = getBytesForTransportable(writer, instance);
		Pricing received = readFromBytes(reader, buffer, "Pricing");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.adLinks().size(), received.adLinks().size());
		assertEquals(instance.getPrice(ad), received.getPrice(ad), 0);

		instance.lock();
		received = new Pricing();

		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer, "Pricing");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.adLinks().size(), received.adLinks().size());
		assertEquals(instance.getPrice(ad), received.getPrice(ad), 0);
	}

	@Test
	public void testEmptyTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		Pricing instance = new Pricing();

		byte[] buffer = getBytesForTransportable(writer, instance);
		Pricing received = readFromBytes(reader, buffer, "Pricing");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.adLinks().size(), received.adLinks().size());

		instance.lock();
		received = new Pricing();

		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer, "Pricing");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.adLinks().size(), received.adLinks().size());
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteToLocked() {
		Pricing instance = new Pricing();
		instance.lock();

		Product product = new Product("manufacturer_1", "component_1");
		String advertisor = "advertisor_1";
		AdLink ad = new AdLink(product, advertisor);
		double price = 100.00;

		instance.setPrice(ad, price);
	}
}