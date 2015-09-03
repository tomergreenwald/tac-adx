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
package edu.umich.eecs.tac.props;

import static edu.umich.eecs.tac.props.TransportableTestUtils.getBytesForTransportable;
import static edu.umich.eecs.tac.props.TransportableTestUtils.readFromBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Test;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import tau.tac.adx.props.AdxInfoContextFactory;
import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.RetailCatalog.RetailCatalogEntry;

/**
 * @author Patrick Jordan
 */
public class RetailCatalogTest {

	@Test
	public void testEmptyRetailCatalog() {
		RetailCatalog catalog = new RetailCatalog();

		assertEquals(catalog.getManufacturers().size(), 0);
		assertEquals(catalog.getComponents().size(), 0);

	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testIndexOutOfBounds() {
		RetailCatalog catalog = new RetailCatalog();
		catalog.getSalesProfit(-1);

	}

	@Test
	public void testSalesProfitForUncontainedProduct() {
		RetailCatalog catalog = new RetailCatalog();
		assertEquals(catalog.getSalesProfit(null), 0.0, 0);

	}

	@Test
	public void testUnitRetailCatalog() {
		RetailCatalog catalog = new RetailCatalog();
		assertEquals(catalog.getManufacturers().size(), 0);
		assertEquals(catalog.getComponents().size(), 0);

		Product product = new Product("m1", "c1");
		catalog.addProduct(product);
		catalog.setSalesProfit(product, 10.5);
		Product product2 = new Product("m1", "c2");
		catalog.addProduct(product2);
		catalog.setSalesProfit(1, 15.5);

		Product product3 = new Product("m2", "c3");
		catalog.setSalesProfit(product3, 15.5);

		assertEquals(catalog.getManufacturers().size(), 2);
		assertEquals(catalog.getComponents().size(), 3);
		assertEquals(catalog.size(), 3);

		assertEquals(catalog.getSalesProfit(product), 10.5, 0);
		assertEquals(catalog.getSalesProfit(1), 15.5, 0);
		assertEquals(catalog.getSalesProfit(new Product()), 0.0, 0);

		for (Product p : catalog) {
			p.equals(new Product("m1", "c1"));
		}

		assertEquals(catalog.entryClass(), RetailCatalogEntry.class);
	}

	@Test
	public void testEmptyTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		RetailCatalog instance = new RetailCatalog();

		byte[] buffer = getBytesForTransportable(writer, instance);
		RetailCatalog received = readFromBytes(reader, buffer, "RetailCatalog");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(0, received.size());

		instance.lock();
		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer, "RetailCatalog");

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

		RetailCatalog instance = new RetailCatalog();
		Product product = new Product("m1", "c1");
		instance.addProduct(product);

		byte[] buffer = getBytesForTransportable(writer, instance);
		RetailCatalog received = readFromBytes(reader, buffer, "RetailCatalog");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(received.getManufacturers().size(), 1);
		assertEquals(received.getComponents().size(), 1);
		assertEquals(received.size(), 1);

		instance.lock();
		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer, "RetailCatalog");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(received.getManufacturers().size(), 1);
		assertEquals(received.getComponents().size(), 1);
		assertEquals(received.size(), 1);
		assertTrue(received.isLocked());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRemoveEntry() {
		new RetailCatalog().removeEntry(0);
	}
}
