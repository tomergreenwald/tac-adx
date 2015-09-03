/*
 * RankingTest.java
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
public class RankingTest {

	int num_ads;

	public RankingTest() {
		num_ads = 35;
	}

	public Ranking getRanking() {
		Ranking ranking = new Ranking();
		for (int i = 0; i < num_ads; i++) {
			Product product = new Product("manufacturer_" + i, "component_" + i);
			AdLink ad = new AdLink(product, "advertiser_" + i);
			ranking.add(ad, i % 2 == 1);
		}
		return ranking;
	}

	@Test
	public void testAdd() {
		AdLink ad = null;
		Ranking instance = new Ranking();
		instance.add(ad);
		assertEquals(instance.size(), 1);

		instance = getRanking();
		assertEquals(instance.size(), num_ads);
	}

	@Test
	public void testSet() {
		int position = 0;
		AdLink ad = null;
		Ranking instance = new Ranking();

		boolean promoted = false;

		boolean thrown = false;
		try {
			instance.set(position, ad, promoted);
		} catch (IndexOutOfBoundsException e) {
			thrown = true;
		}
		if (!thrown) {
			fail("Expected IndexOutOfBoundsException");
		}

		instance = getRanking();

		Product product = new Product("manufacturer_111", "component_111");
		ad = new AdLink(product, "advertiser_111");
		instance.set(0, ad, promoted);

		product = new Product("manufacturer_222", "component_222");
		ad = new AdLink(product, "advertiser_333");
		instance.set(17, ad, promoted);

		product = new Product("manufacturer_333", "component_333");
		ad = new AdLink(product, "advertiser_333");
		instance.set(34, ad, promoted);

		thrown = false;
		try {
			instance.set(35, ad, promoted);
		} catch (IndexOutOfBoundsException e) {
			thrown = true;
		}
		if (!thrown) {
			fail("Expected IndexOutOfBoundsException");
		}

		thrown = false;
		try {
			instance.set(-1, ad, promoted);
		} catch (IndexOutOfBoundsException e) {
			thrown = true;
		}
		if (!thrown) {
			fail("Expected IndexOutOfBoundsException");
		}
	}

	@Test
	public void testGet() {
		int position = 0;
		Ranking instance = new Ranking();
		AdLink expResult = null;

		AdLink result;
		boolean thrown = false;
		try {
			result = instance.get(position);
		} catch (IndexOutOfBoundsException e) {
			thrown = true;
		}
		if (!thrown) {
			fail("Expected IndexOutOfBoundsException");
		}

		instance = getRanking();

		Product product = new Product("manufacturer_0", "component_0");
		expResult = new AdLink(product, "advertiser_0");
		result = instance.get(position);
		assertEquals(result, expResult);
		assertFalse(instance.isPromoted(position));

		position = 13;
		product = new Product("manufacturer_13", "component_13");
		expResult = new AdLink(product, "advertiser_13");
		result = instance.get(position);
		assertEquals(result, expResult);

		position = -1;
		thrown = false;
		try {
			result = instance.get(position);
		} catch (IndexOutOfBoundsException e) {
			thrown = true;
		}
		if (!thrown) {
			fail("Expected IndexOutOfBoundsException");
		}

		position = 35;
		thrown = false;
		try {
			result = instance.get(position);
		} catch (IndexOutOfBoundsException e) {
			thrown = true;
		}
		if (!thrown) {
			fail("Expected IndexOutOfBoundsException");
		}
	}

	@Test
	public void testPositionForAd() {
		Product product = new Product("manufacturer_0", "component_0");
		AdLink ad = new AdLink(product, "advertiser_0");
		Ranking instance = getRanking();
		int expResult = 0;
		int result = instance.positionForAd(ad);
		assertEquals(expResult, result);

		product = new Product("manufacturer_9", "component_9");
		ad = new AdLink(product, "advertiser_9");
		instance = getRanking();
		expResult = 9;
		result = instance.positionForAd(ad);
		assertEquals(expResult, result);

		product = new Product("manufacturer_34", "component_34");
		ad = new AdLink(product, "advertiser_34");
		instance = getRanking();
		expResult = 34;
		result = instance.positionForAd(ad);
		assertEquals(expResult, result);

		product = new Product("manufacturer_9", "component_9");
		ad = new AdLink(product, "advertiser_10");
		instance = getRanking();
		expResult = -1;
		result = instance.positionForAd(ad);
		assertEquals(expResult, result);

		product = new Product("", "");
		ad = new AdLink(product, "");
		instance = getRanking();
		expResult = -1;
		result = instance.positionForAd(ad);
		assertEquals(expResult, result);

		product = new Product(null, null);
		ad = new AdLink(product, null);
		instance = getRanking();
		expResult = -1;
		result = instance.positionForAd(ad);
		assertEquals(expResult, result);
	}

	@Test
	public void testSize() {
		Ranking instance = new Ranking();
		int expResult = 0;
		int result = instance.size();
		assertEquals(expResult, result);

		instance = getRanking();
		expResult = 35;
		result = instance.size();
		assertEquals(expResult, result);
	}

	@Test
	public void testToString() {
		Ranking instance = new Ranking();
		String expResult = "[]";
		String result = instance.toString();
		assertEquals(expResult, result);

		int num_ads = 3;
		for (int i = 0; i < num_ads; i++) {
			Product product = new Product("manufacturer_" + i, "component_" + i);
			AdLink ad = new AdLink(product, "advertiser_" + i);
			instance.add(ad);
		}

		expResult = "[[0: (AdLink advertiser:advertiser_0 ad:(Ad generic:false product:(Product (manufacturer_0,component_0))))][1: (AdLink advertiser:advertiser_1 ad:(Ad generic:false product:(Product (manufacturer_1,component_1))))][2: (AdLink advertiser:advertiser_2 ad:(Ad generic:false product:(Product (manufacturer_2,component_2))))]]";
		result = instance.toString();
		assertEquals(expResult, result);
	}

	@Test
	public void testEmptyTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		Ranking instance = new Ranking();

		byte[] buffer = getBytesForTransportable(writer, instance);
		Ranking received = readFromBytes(reader, buffer, "Ranking");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(0, received.size());

		instance.lock();
		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer, "Ranking");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(0, received.size());
	}

	@Test
	public void testValidTransport() throws ParseException,
			IndexOutOfBoundsException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		Ranking instance = getRanking();

		byte[] buffer = getBytesForTransportable(writer, instance);
		Ranking received = readFromBytes(reader, buffer, "Ranking");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.get(0).getAdvertiser(), received.get(0)
				.getAdvertiser());
		assertEquals(instance.size(), received.size());

		instance.lock();
		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer, "Ranking");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.get(0).getAdvertiser(), received.get(0)
				.getAdvertiser());
		assertEquals(instance.size(), received.size());
	}

	@Test
	public void testWriteToLocked() {
		Ranking instance = new Ranking();
		instance.lock();

		Product product = new Product("manufacturer_1", "component_1");
		String advertisor = "advertisor_1";
		AdLink ad = new AdLink(product, advertisor);

		int thrown = 0;
		try {
			instance.add(ad);
		} catch (IllegalStateException e) {
			thrown++;
		}
		try {
			instance.set(0, ad, false);
		} catch (IllegalStateException e) {
			thrown++;
		}
		if (thrown != 2) {
			fail("Modified locked instance");
		}
	}

	@Test
	public void testEmptySlotTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		Ranking.Slot instance = new Ranking.Slot();

		byte[] buffer = getBytesForTransportable(writer, instance);
		Ranking.Slot received = readFromBytes(reader, buffer, "Slot");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.isPromoted(), received.isPromoted());
		assertEquals(instance.getAdLink(), received.getAdLink());

		instance.setAdLink(new AdLink());
		instance.setPromoted(true);

		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer, "Slot");

		assertEquals(instance.isPromoted(), received.isPromoted());
		assertEquals(instance.getAdLink(), received.getAdLink());
	}
}