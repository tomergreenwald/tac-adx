/*
 * QueryTest.java
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
public class QueryTest {
	@Test
	public void testConstructor() {
		Query q = new Query();
		assertNotNull(q);
	}

	@Test
	public void testFocusLevelZeroQuery() {
		Query query = new Query();
		assertNotNull(query);
		assertNull(query.getManufacturer());
		assertNull(query.getComponent());
		assertEquals(query.getType(), QueryType.FOCUS_LEVEL_ZERO);
	}

	@Test
	public void testFocusLevelOneQuery() {
		String manufacturer = "Alice";
		String component = "Bob";

		Query manufacturerQuery = new Query();
		assertNotNull(manufacturerQuery);
		assertNull(manufacturerQuery.getManufacturer());
		assertNull(manufacturerQuery.getComponent());
		manufacturerQuery.setManufacturer(manufacturer);
		assertEquals(manufacturerQuery.getManufacturer(), manufacturer);
		assertNull(manufacturerQuery.getComponent());

		Query componentQuery = new Query();
		assertNotNull(componentQuery);
		assertNull(componentQuery.getManufacturer());
		assertNull(componentQuery.getComponent());
		componentQuery.setComponent(component);
		assertEquals(componentQuery.getComponent(), component);
		assertNull(componentQuery.getManufacturer());

		assertEquals(componentQuery.getType(), QueryType.FOCUS_LEVEL_ONE);
		assertEquals(manufacturerQuery.getType(), QueryType.FOCUS_LEVEL_ONE);
	}

	@Test
	public void testFocusLevelTwoQuery() {
		String manufacturer = "Alice";
		String component = "Bob";

		Query query = new Query();
		assertNotNull(query);
		assertNull(query.getManufacturer());
		assertNull(query.getComponent());
		query.setManufacturer(manufacturer);
		query.setComponent(component);
		assertEquals(query.getManufacturer(), manufacturer);
		assertEquals(query.getComponent(), component);

		assertEquals(query.getType(), QueryType.FOCUS_LEVEL_TWO);
	}

	@Test
	public void testTransportName() {
		assertEquals(new Query().getTransportName(), "Query");
	}

	@Test
	public void testToString() {
		Query query = new Query();
		query.setManufacturer("a");
		query.setComponent("b");
		assertEquals(query.toString(), "(Query (a,b))");
	}

	@Test
	public void testEquals() {
		Query f0 = new Query();

		assertFalse(f0.equals(null));
		assertFalse(f0.equals(new Product()));

		Query f1 = new Query();
		f1.setManufacturer("Alice");

		Query f1Duplicate = new Query();
		f1Duplicate.setManufacturer("Alice");

		Query f1NotDuplicate = new Query();
		f1NotDuplicate.setComponent("Bob");

		Query f2 = new Query();
		f2.setManufacturer("Alice");
		f2.setComponent("Bob");

		Query f2Duplicate = new Query();
		f2Duplicate.setManufacturer("Alice");
		f2Duplicate.setComponent("Bob");

		Query f2DuplicateLocked = new Query();
		f2Duplicate.setManufacturer("Alice");
		f2Duplicate.setComponent("Bob");
		f2DuplicateLocked.lock();

		assertNotNull(f0);
		assertEquals(f0, new Query());
		assertEquals(f1, f1Duplicate);
		assertEquals(f2, f2Duplicate);
		assertFalse(f1.equals(f2));
		assertTrue(f1.equals(f1));
		assertFalse(f1.equals(null));
		assertFalse(f1.equals(new Object()));
		assertEquals(f0.hashCode(), new Query().hashCode());
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

		Query query = new Query();
		query.setManufacturer("a");
		query.setComponent("b");

		byte[] buffer = getBytesForTransportable(writer, query);
		Query received = readFromBytes(reader, buffer, "Query");
		assertNotNull(query);
		assertNotNull(received);
		assertEquals(query, received);

		Query lockedQuery = new Query();
		query.setManufacturer("aa");
		query.setComponent("b");
		lockedQuery.lock();

		buffer = getBytesForTransportable(writer, lockedQuery);
		received = readFromBytes(reader, buffer, "Query");
		assertNotNull(lockedQuery);
		assertNotNull(received);
		assertEquals(lockedQuery, received);
	}

	@Test
	public void testEmptyTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		Query instance = new Query();
		byte[] buffer = getBytesForTransportable(writer, instance);
		Query received = readFromBytes(reader, buffer, "Query");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance, received);
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteToLocked() {
		Query query = new Query();
		query.lock();
		query.setManufacturer("a");
	}

}
