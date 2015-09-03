/*
 * RetailCatalogTest.java
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
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Test;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.publishers.generators.AdxPublisherGenerator;
import tau.tac.adx.util.Utils;

/**
 * @author Patrick Jordan
 */
public class PublisherCatalogTest {

	@Test
	public void testEmptyRetailCatalog() {
		PublisherCatalog catalog = new PublisherCatalog();
		assertEquals(catalog.size(), 0);
		assertFalse(catalog.iterator().hasNext());
	}

	@Test
	public void testEmptyTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		PublisherCatalog instance = new PublisherCatalog();
		byte[] buffer = getBytesForTransportable(writer, instance);
		PublisherCatalog received = readFromBytes(reader, buffer,
				PublisherCatalog.class.getSimpleName());

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(0, received.size());

		instance.lock();
		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer,
				PublisherCatalog.class.getSimpleName());

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

		PublisherCatalog catalog = new PublisherCatalog();
		AdxPublisherGenerator publisherGenerator = Utils.getInjector()
				.getInstance(AdxPublisherGenerator.class);
		AdxPublisher publisher = publisherGenerator.generate(1).iterator()
				.next();
		catalog.addPublisher(publisher);

		byte[] buffer = getBytesForTransportable(writer, catalog);
		PublisherCatalog received = readFromBytes(reader, buffer,
				PublisherCatalog.class.getSimpleName());

		assertNotNull(catalog);
		assertNotNull(received);
		assertEquals(1, received.getPublishers().size());
		assertEquals(catalog.createEntry(publisher), received.getPublishers()
				.get(0));
		assertEquals(1, received.size());

		catalog.lock();
		buffer = getBytesForTransportable(writer, catalog);
		received = readFromBytes(reader, buffer,
				PublisherCatalog.class.getSimpleName());

		assertNotNull(catalog);
		assertNotNull(received);
		assertEquals(1, received.getPublishers().size());
		assertEquals(catalog.createEntry(publisher), received.getPublishers()
				.get(0));
		assertEquals(1, received.size());
		assertTrue(received.locked());
	}
	//
	// @Test(expected = UnsupportedOperationException.class)
	// public void testRemoveEntry() {
	// new RetailCatalog().removeEntry(0);
	// }
}
