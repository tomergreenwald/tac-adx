/*
 * AdLinkTest.java
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Test;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.Product;

/**
 * 
 * @author Kemal Eren
 */
public class AdLinkTest {
	@Test
	public void testAdvertiser() {
		AdLink instance = new AdLink();
		String result = instance.getAdvertiser();
		assertNull(result);
		instance.setAdvertiser("abc");
		result = instance.getAdvertiser();
		String expResult = "abc";
		assertEquals(expResult, result);
	}

	@Test
	public void testConstructor() {
		AdLink first = new AdLink();
		AdLink second = new AdLink((Ad) null, null);

		assertEquals(first, second);

		first = new AdLink();
		first.setAd(new Ad());

		second = new AdLink(new Ad(), null);

		assertEquals(first, second);
	}

	@Test
	public void testEmptyTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		AdLink instance = new AdLink();

		byte[] buffer = getBytesForTransportable(writer, instance);
		AdLink received = readFromBytes(reader, buffer, "AdLink");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance, received);

		instance.lock();
		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer, "AdLink");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance, received);
	}

	@Test
	public void testValidTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		Product product = new Product();
		product.setComponent("comp_1");
		product.setManufacturer("man_1");
		AdLink instance = new AdLink(product, "advertiser_1");

		byte[] buffer = getBytesForTransportable(writer, instance);
		AdLink received = readFromBytes(reader, buffer, "AdLink");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance, received);

		instance.lock();
		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer, "AdLink");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance, received);
	}

	@Test
	public void testEquals() {

		AdLink instance = new AdLink();
		assertTrue(instance.equals(instance));

		assertFalse(instance.equals(new Product()));

		Object o = null;
		assertFalse(instance.equals(o));

		AdLink instance_2 = new AdLink();
		assertTrue(instance.equals(instance_2));

		instance_2.setAdvertiser("abc");
		instance_2.setAd(new Ad(new Product("123", "xyz")));
		assertFalse(instance.equals(instance_2));

		instance.setAdvertiser("abc");
		assertFalse(instance.equals(instance_2));

		instance.setAd(new Ad(new Product("123", "xyz")));
		assertTrue(instance.equals(instance_2));

		instance.setAdvertiser("abcd");
		assertFalse(instance.equals(instance_2));

		instance = new AdLink(new Ad(), null);

		assertFalse(instance.equals(new AdLink(new Ad(), "bob")));
	}

	@Test
	public void testHashCode() {
		AdLink instance = new AdLink();
		assertEquals(instance.hashCode(), new AdLink().hashCode());
	}

	@Test
	public void testToString() {
		AdLink instance = new AdLink();
		String expResult = "(AdLink advertiser:null ad:null)";
		String result = instance.toString();
		assertEquals(expResult, result);

		String advertiser = "abc";
		instance.setAdvertiser(advertiser);
		Product product = new Product("123", "xyz");
		instance.setAd(new Ad(product));
		expResult = "(AdLink advertiser:abc ad:(Ad generic:false product:(Product (123,xyz))))";
		result = instance.toString();
		assertEquals(expResult, result);
	}

}