/*
 * AuctionTest.java
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.text.ParseException;

import org.junit.Test;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import tau.tac.adx.props.AdxInfoContextFactory;
import tau.tac.adx.props.AdLink;

/**
 * 
 * @author Kemal Eren
 */
public class AuctionTest {

	/**
	 * Test of getRanking and setRanking methods
	 */
	@Test
	public void testRanking() {
		Auction instance = new Auction();
		Ranking expResult = null;
		Ranking result = instance.getRanking();
		assertEquals(expResult, result);

		expResult = new Ranking();
		AdLink ad = new AdLink();
		ad.setAdvertiser("test_advertiser");
		expResult.add(ad);
		instance.setRanking(expResult);
		result = instance.getRanking();
		assertEquals(expResult, result);
	}

	/**
	 * Test of getPricing and setPricing methods
	 */
	@Test
	public void testPricing() {
		Auction instance = new Auction();
		Pricing expResult = null;
		Pricing result = instance.getPricing();
		assertEquals(expResult, result);

		expResult = new Pricing();
		int price = 100;
		AdLink ad = new AdLink();
		ad.setAdvertiser("test_advertiser");
		expResult.setPrice(ad, price);
		instance.setPricing(expResult);
		result = instance.getPricing();
		assertEquals(expResult, result);
	}

	/**
	 * Test of getQuery and setQuery methods
	 */
	@Test
	public void testQuery() {
		Auction instance = new Auction();
		Query expResult = null;
		Query result = instance.getQuery();
		assertEquals(expResult, result);

		expResult = new Query();
		expResult.setComponent("test_component");
		instance.setQuery(expResult);
		result = instance.getQuery();
		assertEquals(expResult, result);
	}

	@Test
	public void testValidTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		Auction instance = new Auction();

		Ranking ranking = new Ranking();
		AdLink ad = new AdLink();
		ad.setAdvertiser("test_advertiser");
		ranking.add(ad);
		instance.setRanking(ranking);

		Query query = new Query();
		query.setComponent("test_component");
		instance.setQuery(query);

		Pricing pricing = new Pricing();
		int price = 100;
		pricing.setPrice(ad, price);
		instance.setPricing(pricing);

		byte[] buffer = getBytesForTransportable(writer, instance);
		Auction received = readFromBytes(reader, buffer, "Auction");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.getQuery().getComponent(), received.getQuery()
				.getComponent());
		assertEquals(instance.getRanking().size(), received.getRanking().size());

		instance.lock();
		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer, "Auction");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.getQuery().getComponent(), received.getQuery()
				.getComponent());
		assertEquals(instance.getRanking().size(), received.getRanking().size());
	}

	@Test
	public void testEmptyTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		Auction instance = new Auction();

		byte[] buffer = getBytesForTransportable(writer, instance);
		Auction received = readFromBytes(reader, buffer, "Auction");

		assertNotNull(instance);
		assertNotNull(received);
		assertNull(received.getQuery());
		assertNull(received.getRanking());
		assertNull(received.getPricing());

		instance.lock();
		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer, "Auction");

		assertNotNull(instance);
		assertNotNull(received);
		assertNull(received.getQuery());
		assertNull(received.getRanking());
		assertNull(received.getPricing());
	}

	@Test
	public void testWriteToLocked() {
		Auction instance = new Auction();
		instance.lock();
		int thrown = 0;
		try {
			instance.setPricing(new Pricing());
		} catch (IllegalStateException e) {
			thrown++;
		}
		try {
			instance.setQuery(new Query());
		} catch (IllegalStateException e) {
			thrown++;
		}
		try {
			instance.setRanking(new Ranking());
		} catch (IllegalStateException e) {
			thrown++;
		}
		if (thrown != 3) {
			fail("Succeeded in modifying locked instance");
		}
	}

}