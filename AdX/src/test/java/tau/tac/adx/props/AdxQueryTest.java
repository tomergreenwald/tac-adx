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
package tau.tac.adx.props;

import static edu.umich.eecs.tac.props.TransportableTestUtils.getBytesForTransportable;
import static edu.umich.eecs.tac.props.TransportableTestUtils.readFromBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;

import org.junit.Test;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import tau.tac.adx.props.generators.AdxQueryGenerator;
import tau.tac.adx.util.Utils;

import com.google.inject.Injector;

import edu.umich.eecs.tac.props.Query;

/**
 * @author Patrick Jordan
 */
public class AdxQueryTest {
	@Test
	public void testConstructor() {
		AdxQuery q = new AdxQuery();
		assertNotNull(q);
	}

	@Test
	public void testToString() {
		AdxQuery query = new AdxQuery();
		query.setPublisher("Publisher name");
		assertEquals(
				"AdxQuery [publisher=Publisher name, marketSegments=[], device=null, adType=null]",
				query.toString());
	}

	@Test
	public void testValidTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());
		Injector injector = Utils.getInjector();
		AdxQueryGenerator generator = injector
				.getInstance(AdxQueryGenerator.class);
		AdxQuery query = (AdxQuery) generator.generate(1).toArray()[0];

		byte[] buffer = getBytesForTransportable(writer, query);
		AdxQuery received = readFromBytes(reader, buffer,
				AdxQuery.class.getSimpleName());
		assertNotNull(query);
		assertNotNull(received);
		assertEquals(query, received);
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
