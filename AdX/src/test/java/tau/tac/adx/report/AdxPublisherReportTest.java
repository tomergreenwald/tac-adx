/*
 * SalesReportTest.java
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
package tau.tac.adx.report;

import static edu.umich.eecs.tac.props.TransportableTestUtils.getBytesForTransportable;
import static edu.umich.eecs.tac.props.TransportableTestUtils.readFromBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.text.ParseException;
import java.util.Random;

import org.junit.Test;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import tau.tac.adx.AdxManager;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxInfoContextFactory;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalogEntry;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.publishers.generators.AdxPublisherGenerator;
import tau.tac.adx.publishers.generators.SimplePublisherGenerator;
import tau.tac.adx.report.publisher.AdxPublisherReport;
import tau.tac.adx.report.publisher.AdxPublisherReportEntry;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.generators.AdxUserGenerator;
import tau.tac.adx.users.generators.SimpleUserGenerator;

/**
 * @author Patrick Jordan
 */
public class AdxPublisherReportTest {

	@Test
	public void testEmptyReportEntry() {
		AdxPublisherReportEntry entry = new AdxPublisherReportEntry(null);
		assertNotNull(entry);
	}

	@Test
	public void testEmptyReport() {
		AdxPublisherReport report = new AdxPublisherReport();
		assertNotNull(report);
		assertEquals(0, report.size());
	}

	@Test
	public void testBasicReportEntry() {
		AdxPublisherReportEntry entry = new AdxPublisherReportEntry(null);
		assertNotNull(entry);

		assertNull(entry.getKey());
		entry.setKey(mock(PublisherCatalogEntry.class));
		assertNotNull(entry.getKey());

		assertEquals(0, entry.getPopularity());
		assertEquals(0, (int) entry.getAdTypeOrientation().get(AdType.text));
		assertEquals(0, (int) entry.getAdTypeOrientation().get(AdType.video));

		assertEquals(entry.getTransportName(), entry.getClass().getSimpleName());
		assertEquals("(publisher: null popularity: 0 video: 0 text: 0)",
				entry.toString());
	}

	@Test
	public void testValidTransportOfReportEntry() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());
		Random random = new Random();
		String publisherName = "publisher #" + random.nextInt();
		PublisherCatalogEntry key = new PublisherCatalogEntry(publisherName);
		AdxPublisherReportEntry entry = new AdxPublisherReportEntry(key);

		byte[] buffer = getBytesForTransportable(writer, entry);
		AdxPublisherReportEntry received = readFromBytes(reader, buffer,
				entry.getTransportName());

		assertNotNull(entry);
		assertNotNull(received);

		assertEquals(entry.getAdTypeOrientation(),
				received.getAdTypeOrientation());
		assertEquals(entry.getKey(), received.getKey());
		assertEquals(entry.getPopularity(), received.getPopularity());
		assertEquals(entry.getPublisherName(), received.getPublisherName());
		assertEquals(entry.getClass().getSimpleName(), entry.getTransportName());
	}

	@Test
	public void testValidTransportOfReport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());
		AdxPublisherReport report = new AdxPublisherReport();
		report.lock();

		byte[] buffer = getBytesForTransportable(writer, report);
		AdxPublisherReport received = readFromBytes(reader, buffer,
				report.getTransportName());

		assertNotNull(report);
		assertNotNull(received);
		assertEquals(report.size(), 0);
		assertEquals(received.size(), 0);

		buffer = getBytesForTransportable(writer, new AdxPublisherReport());
		received = readFromBytes(reader, buffer, report.getTransportName());
		assertFalse(received.isLocked());
	}

	@Test
	public void testValidTransportOfReportWithValues() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());
		AdxPublisherGenerator publisherGenerator = new SimplePublisherGenerator();
		AdxPublisher publisher = publisherGenerator.generate(1).iterator()
				.next();
		AdxManager.getInstance().addPublisher(publisher);
		Random random = new Random();
		String publisherName = "publisher #" + random.nextInt();
		PublisherCatalogEntry key = new PublisherCatalogEntry(publisherName);
		AdxPublisherReportEntry entry = new AdxPublisherReportEntry(key);
		AdxPublisherReport report = new AdxPublisherReport();
		AdxUserGenerator userGenerator = new SimpleUserGenerator(random.nextDouble());
		AdxUser user = userGenerator.generate(1).iterator().next();
		Device device = Device.values()[random.nextInt(1)];
		AdType adType = AdType.values()[random.nextInt(1)];
		AdxQuery query = new AdxQuery(publisher, user, device, adType);
		report.addQuery(query);
		report.lock();

		byte[] buffer = getBytesForTransportable(writer, report);
		AdxPublisherReport received = readFromBytes(reader, buffer,
				report.getTransportName());

		assertNotNull(report);
		assertNotNull(received);
		assertEquals(1, report.size());
		assertEquals(report.size(), received.size());
		assertEquals(report.getPublisherReportEntry(new PublisherCatalogEntry(
				publisher)),
				received.getPublisherReportEntry(new PublisherCatalogEntry(
						publisher)));

		buffer = getBytesForTransportable(writer, new AdxPublisherReport());
		received = readFromBytes(reader, buffer, report.getTransportName());
		assertFalse(received.isLocked());
	}

	@Test(expected = NullPointerException.class)
	public void testAddQueryToReport() {
		AdxPublisherReport report = new AdxPublisherReport();

		report.addQuery(null);
	}
}
