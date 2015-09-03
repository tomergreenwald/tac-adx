/*
 * ManufacturerComponentComposableTest.java
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
 * 
 * @author Kemal Eren
 */
public class ManufacturerComponentComposableTest {

	/**
	 * Test methods setManufacturer and getManufacturer
	 */
	@Test
	public void testManufacturer() {
		ManufacturerComponentComposable instance = new ManufacturerComponentComposable();
		String expResult = null;
		String result = instance.getManufacturer();
		assertEquals(expResult, result);

		String manufacturer = "";
		expResult = "";
		instance.setManufacturer(manufacturer);
		result = instance.getManufacturer();
		assertEquals(expResult, result);

		manufacturer = "test_manufacturer";
		expResult = manufacturer;
		instance.setManufacturer(manufacturer);
		result = instance.getManufacturer();
		assertEquals(expResult, result);
	}

	/**
	 * Test methods getComonent and setComponent
	 */
	@Test
	public void testComponent() {
		ManufacturerComponentComposable instance = new ManufacturerComponentComposable();
		String expResult = null;
		String result = instance.getComponent();
		assertEquals(expResult, result);

		String component = "";
		instance.setComponent(component);
		assertEquals(expResult, result);

		component = "test_component";
		instance.setComponent(component);
		assertEquals(expResult, result);
	}

	@Test
	public void testEmptyTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		ManufacturerComponentComposable instance = new ManufacturerComponentComposable();

		byte[] buffer = getBytesForTransportable(writer, instance);
		ManufacturerComponentComposable received = readFromBytes(reader,
				buffer, "ManufacturerComponentComposable");

		assertNotNull(instance);
		assertNotNull(received);
		assertNull(instance.getComponent());

		instance.lock();
		received = new ManufacturerComponentComposable();

		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer,
				"ManufacturerComponentComposable");

		assertNotNull(instance);
		assertNotNull(received);
		assertNull(instance.getComponent());
	}

	@Test
	public void testValidTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		ManufacturerComponentComposable instance = new ManufacturerComponentComposable();
		String advertisor = "advertisor_1";
		String component = "componenent_1";
		instance.setComponent(component);
		instance.setComponent(component);

		byte[] buffer = getBytesForTransportable(writer, instance);
		ManufacturerComponentComposable received = readFromBytes(reader,
				buffer, "ManufacturerComponentComposable");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.getComponent(), received.getComponent());
		assertEquals(instance.getManufacturer(), received.getManufacturer());

		instance.lock();
		received = new ManufacturerComponentComposable();

		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer,
				"ManufacturerComponentComposable");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.getComponent(), received.getComponent());
		assertEquals(instance.getManufacturer(), received.getManufacturer());
	}

	@Test
	public void testHashCode() {
		ManufacturerComponentComposable instance = new ManufacturerComponentComposable();
		int expResult = 0;
		int result = instance.hashCode();
		assertEquals(result, expResult);

		String component = "test_component";
		String manufacturer = "test_manufacturer";
		instance.setComponent(component);
		instance.setManufacturer(manufacturer);

		expResult = manufacturer.hashCode() * 31 + component.hashCode();
		result = instance.hashCode();
		assertEquals(result, expResult);
	}

	@Test
	public void testToString() {
		ManufacturerComponentComposable instance = new ManufacturerComponentComposable();
		String expResult = "(ManufacturerComponentComposable (null,null))";
		String result = instance.toString();
		assertEquals(expResult, result);

		String component = "test_component";
		String manufacturer = "test_manufacturer";
		instance.setComponent(component);
		instance.setManufacturer(manufacturer);
		expResult = "(ManufacturerComponentComposable (test_manufacturer,test_component))";

		result = instance.toString();
		assertEquals(expResult, result);
	}

	@Test
	public void testComposableEquals() {
		ManufacturerComponentComposable o = null;
		ManufacturerComponentComposable instance = new ManufacturerComponentComposable();
		assertFalse(instance.composableEquals(o));

		assertTrue(instance.composableEquals(instance));
		assertFalse(instance.composableEquals(null));

		o = new ManufacturerComponentComposable();
		assertTrue(instance.composableEquals(o));

		o.lock();
		assertFalse(instance.composableEquals(o));
		assertFalse(o.composableEquals(instance));

		instance.lock();
		assertTrue(instance.composableEquals(o));

		instance = new ManufacturerComponentComposable();
		o = new ManufacturerComponentComposable();
		String manufacturer = "test_manufacturer";
		String component = "test_component";

		instance.setManufacturer(manufacturer);
		assertFalse(instance.composableEquals(o));
		assertFalse(o.composableEquals(instance));

		o.setManufacturer("manufacturer_1");
		assertFalse(instance.composableEquals(o));
		assertFalse(o.composableEquals(instance));

		o.setManufacturer(manufacturer);
		assertTrue(instance.composableEquals(o));

		manufacturer = "manufacturer_2";
		instance.setManufacturer(manufacturer);
		assertFalse(instance.composableEquals(o));
		assertFalse(o.composableEquals(instance));

		o.setManufacturer(manufacturer);
		instance.setComponent(component);
		assertFalse(instance.composableEquals(o));
		assertFalse(o.composableEquals(instance));

		o.setComponent(component);
		assertTrue(instance.composableEquals(o));

		component = "test_component_2";
		o.setComponent(component);
		assertFalse(instance.composableEquals(o));
		assertFalse(o.composableEquals(instance));

		instance.setComponent(component);
		o = new ManufacturerComponentComposable();
		assertFalse(instance.equals(o));
		assertFalse(o.equals(instance));
	}

}