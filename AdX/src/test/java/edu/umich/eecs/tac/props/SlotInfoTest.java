/*
 * SlotInfoTest.java
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
public class SlotInfoTest {
	private ReserveInfo reserveInfo;

	@Before
	public void setup() {
		reserveInfo = new ReserveInfo();
	}

	@Test
	public void testConstructor() {
		assertNotNull(reserveInfo);
	}

	@Test
	public void testPromotedReserve() {
            for (QueryType type : QueryType.values()) {
//                System.out.println(type+" "+reserveInfo.getPromotedReserve(type));
		assertEquals(reserveInfo.getPromotedReserve(type), 0.0, 0);
		reserveInfo.setPromotedReserve(type, 1.0);
		assertEquals(reserveInfo.getPromotedReserve(type), 1.0, 0);
            }
	}

	@Test
	public void testRegularReserve() {
            for (QueryType type : QueryType.values()) {
		assertEquals(reserveInfo.getRegularReserve(type), 0.0, 0);
		reserveInfo.setRegularReserve(type, 1.0);
		assertEquals(reserveInfo.getRegularReserve(type), 1.0, 0);
            }
	}

	@Test
	public void testValidTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		byte[] buffer = getBytesForTransportable(writer, reserveInfo);
		ReserveInfo received = readFromBytes(reader, buffer, "ReserveInfo");

		assertNotNull(received);
		assertEquals(received, reserveInfo);
	}

	@Test
	public void testEquals() {
		ReserveInfo other = new ReserveInfo();
                for (QueryType type : QueryType.values()) {
                    other.setPromotedReserve(type,1.0);
                    assertFalse(reserveInfo.equals(other));

                    other = new ReserveInfo();
                    other.setRegularReserve(type,1.0);
                    assertFalse(reserveInfo.equals(other));

                    assertEquals(reserveInfo, reserveInfo);
                    assertFalse(reserveInfo.equals(null));
                    assertFalse(reserveInfo.equals(""));

                    other = new ReserveInfo();
                    assertEquals(reserveInfo.hashCode(), other.hashCode());

                    reserveInfo.setPromotedReserve(type,1.0);
                    reserveInfo.setRegularReserve(type,1.0);

                    other.setPromotedReserve(type,1.0);
                    other.setRegularReserve(type,1.0);
                    assertEquals(reserveInfo.hashCode(), other.hashCode());
                }
	}
}
