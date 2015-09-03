/*
 * AuctionInfoTest.java
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

import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import tau.tac.adx.props.AdxInfoContextFactory;

/**
 * @author Patrick Jordan
 */
public class AuctionInfoTest {
	private SlotInfo slotInfo;

	@Before
	public void setup() {
		slotInfo = new SlotInfo();
	}

	@Test
	public void testConstructor() {
		assertNotNull(slotInfo);
	}

	@Test
	public void testPromotedSlots() {
		assertEquals(slotInfo.getPromotedSlots(), 0);
		slotInfo.setPromotedSlots(1);
		assertEquals(slotInfo.getPromotedSlots(), 1);
	}

	@Test
	public void testRegularSlots() {
		assertEquals(slotInfo.getRegularSlots(), 0);
		slotInfo.setRegularSlots(1);
		assertEquals(slotInfo.getRegularSlots(), 1);
	}

	@Test
	public void testPromotedSlotBonus() {
		assertEquals(slotInfo.getPromotedSlotBonus(), 0.0, 0);
		slotInfo.setPromotedSlotBonus(1.0);
		assertEquals(slotInfo.getPromotedSlotBonus(), 1.0, 0);
	}

	@Test
	public void testValidTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		byte[] buffer = getBytesForTransportable(writer, slotInfo);
		SlotInfo received = readFromBytes(reader, buffer, "SlotInfo");

		assertNotNull(received);
		assertEquals(received, slotInfo);
	}

	@Test
	public void testEquals() {
		SlotInfo other = new SlotInfo();
		other.setPromotedSlotBonus(1.0);
		assertFalse(slotInfo.equals(other));

		other = new SlotInfo();
		other.setRegularSlots(1);
		assertFalse(slotInfo.equals(other));

		other = new SlotInfo();
		other.setPromotedSlots(1);
		assertFalse(slotInfo.equals(other));

		assertEquals(slotInfo, slotInfo);
		assertFalse(slotInfo.equals(null));
		assertFalse(slotInfo.equals(""));

		other = new SlotInfo();
		assertEquals(slotInfo.hashCode(), other.hashCode());

		slotInfo.setPromotedSlotBonus(1.0);

		other.setPromotedSlotBonus(1.0);

		assertEquals(slotInfo.hashCode(), other.hashCode());
	}
}
