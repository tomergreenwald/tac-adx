/*
 * ProductTest.java
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.Test;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import tau.tac.adx.props.AdxInfoContextFactory;

/**
 * @author Patrick Jordan
 */
public class ProductTest {

	@Test
	public void testConstructor() {
		Product p = new Product();
		assertNotNull(p);
	}

	@Test
	public void testFocusLevelZeroProduct() {
		Product product = new Product();
		assertNotNull(product);
		assertNull(product.getManufacturer());
		assertNull(product.getComponent());
	}

	@Test
	public void testFocusLevelOneProduct() {
		String manufacturer = "Alice";
		String component = "Bob";

		Product manufacturerProduct = new Product();
		assertNotNull(manufacturerProduct);
		assertNull(manufacturerProduct.getManufacturer());
		assertNull(manufacturerProduct.getComponent());
		manufacturerProduct.setManufacturer(manufacturer);
		assertEquals(manufacturerProduct.getManufacturer(), manufacturer);
		assertNull(manufacturerProduct.getComponent());

		Product componentProduct = new Product();
		assertNotNull(componentProduct);
		assertNull(componentProduct.getManufacturer());
		assertNull(componentProduct.getComponent());
		componentProduct.setComponent(component);
		assertEquals(componentProduct.getComponent(), component);
		assertNull(componentProduct.getManufacturer());
	}

	@Test
	public void testFocusLevelTwoProduct() {
		String manufacturer = "Alice";
		String component = "Bob";

		Product product = new Product();
		assertNotNull(product);
		assertNull(product.getManufacturer());
		assertNull(product.getComponent());
		product.setManufacturer(manufacturer);
		product.setComponent(component);
		assertEquals(product.getManufacturer(), manufacturer);
		assertEquals(product.getComponent(), component);
	}

	@Test
	public void testTransportName() {
		assertEquals(new Product().getTransportName(), "Product");
	}

	@Test
	public void testToString() {
		Product product = new Product();
		product.setManufacturer("a");
		product.setComponent("b");
		assertEquals(product.toString(), "(Product (a,b))");
	}

	@Test
	public void testEquals() {
		Product f0 = new Product();
		assertFalse(f0.equals(new ManufacturerComponentComposable()));

		Product f1 = new Product();
		f1.setManufacturer("Alice");

		Product f1Duplicate = new Product();
		f1Duplicate.setManufacturer("Alice");

		Product f1NotDuplicate = new Product();
		f1NotDuplicate.setComponent("Bob");

		Product f2 = new Product();
		f2.setManufacturer("Alice");
		f2.setComponent("Bob");

		Product f2Duplicate = new Product();
		f2Duplicate.setManufacturer("Alice");
		f2Duplicate.setComponent("Bob");

		Product f2DuplicateLocked = new Product();
		f2Duplicate.setManufacturer("Alice");
		f2Duplicate.setComponent("Bob");
		f2DuplicateLocked.lock();

		assertNotNull(f0);
		assertEquals(f0, new Product());
		assertEquals(f1, f1Duplicate);
		assertEquals(f2, f2Duplicate);
		assertFalse(f1.equals(f2));
		assertTrue(f1.equals(f1));
		assertFalse(f1.equals(null));
		assertFalse(f1.equals(new Object()));
		assertEquals(f0.hashCode(), new Product().hashCode());
		assertEquals(f1.hashCode(), f1Duplicate.hashCode());
		assertFalse(f1.hashCode() == f1NotDuplicate.hashCode());
		assertFalse(f2.equals(f2DuplicateLocked));
		assertFalse(f1.equals(f1NotDuplicate));
		assertFalse(f1NotDuplicate.equals(f2));
		assertFalse(f1NotDuplicate.equals(f1));
		assertFalse(f1NotDuplicate.equals(f0));
		assertFalse(f0.equals(f1));
		assertFalse(f0.equals(f1NotDuplicate));
		assertFalse(f2.equals(f1));
		assertFalse(f2.equals(f1NotDuplicate));
		assertFalse(f2DuplicateLocked.hashCode() == f2.hashCode());
	}

	@Test
	public void testValidTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		Product product = new Product();
		product.setManufacturer("a");
		product.setComponent("b");

		byte[] buffer = getBytesForTransportable(writer, product);
		Product received = readFromBytes(reader, buffer, "Product");
		assertNotNull(product);
		assertNotNull(received);
		assertEquals(product, received);

		Product lockedProduct = new Product();
		product.setManufacturer("aa");
		product.setComponent("b");
		lockedProduct.lock();

		buffer = getBytesForTransportable(writer, lockedProduct);
		received = readFromBytes(reader, buffer, "Product");
		assertNotNull(lockedProduct);
		assertNotNull(received);
		assertEquals(lockedProduct, received);
	}

	@Test
	public void testEmptyTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		Product instance = new Product();
		byte[] buffer = getBytesForTransportable(writer, instance);
		Product received = readFromBytes(reader, buffer, "Product");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance, received);
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteToLocked() {
		Product product = new Product();
		product.lock();
		product.setManufacturer("a");
	}
}
