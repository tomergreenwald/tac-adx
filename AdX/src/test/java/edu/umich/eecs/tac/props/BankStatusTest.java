/*
 * BankStatusTest.java
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

import java.text.ParseException;

import org.junit.Test;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import tau.tac.adx.props.AdxInfoContextFactory;

/**
 * 
 * @author Kemal Eren
 */
public class BankStatusTest {

	@Test
	public void testIsLocked() {
		BankStatus instance = new BankStatus();
		boolean expResult = false;
		boolean result = instance.isLocked();
		assertEquals(expResult, result);
	}

	@Test
	public void testLock() {
		BankStatus instance = new BankStatus();
		instance.lock();
		boolean expResult = true;
		boolean result = instance.isLocked();
		assertEquals(expResult, result);
	}

	@Test
	public void testAccountBalance() {
		BankStatus instance = new BankStatus();
		double expResult = 0.0;
		double result = instance.getAccountBalance();
		assertEquals(expResult, result, 0);

		double b = 0.0;
		instance.setAccountBalance(b);
		result = instance.getAccountBalance();
		assertEquals(b, result, 0);

		b = 100.5;
		instance.setAccountBalance(b);
		result = instance.getAccountBalance();
		assertEquals(b, result, 0);

		instance = new BankStatus(100.0);
		assertEquals(instance.getAccountBalance(), 100.0, 0);
	}

	@Test
	public void testToString() {
		BankStatus instance = new BankStatus();
		String expResult = "BankStatus[0.000000]";
		String result = instance.toString();
		assertEquals(expResult, result);

		double b = 10.5;
		instance.setAccountBalance(b);
		expResult = "BankStatus[10.500000]";
		result = instance.toString();
		assertEquals(expResult, result);
	}

	@Test
	public void testValidTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		BankStatus instance = new BankStatus();
		instance.setAccountBalance(100.5);

		byte[] buffer = getBytesForTransportable(writer, instance);
		BankStatus received = readFromBytes(reader, buffer, "BankStatus");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.getAccountBalance(), received.getAccountBalance(), 0);

		instance.lock();
		received = new BankStatus();

		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer, "BankStatus");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.getAccountBalance(), received.getAccountBalance(), 0);
	}

	@Test
	public void testEmptyTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		BankStatus instance = new BankStatus();

		byte[] buffer = getBytesForTransportable(writer, instance);
		BankStatus received = readFromBytes(reader, buffer, "BankStatus");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.getAccountBalance(), received.getAccountBalance(), 0);

		instance.lock();
		received = new BankStatus();

		buffer = getBytesForTransportable(writer, instance);
		received = readFromBytes(reader, buffer, "BankStatus");

		assertNotNull(instance);
		assertNotNull(received);
		assertEquals(instance.getAccountBalance(), received.getAccountBalance(), 0);
	}

	@Test(expected = IllegalStateException.class)
	public void testWriteToLocked() {
		BankStatus instance = new BankStatus();
		instance.lock();
		instance.setAccountBalance(100.0);
	}
}