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
import org.mockito.Mockito;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import tau.tac.adx.AdxManager;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.auction.data.AuctionState;
import tau.tac.adx.bids.BidInfo;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxInfoContextFactory;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.generators.AdxQueryGenerator;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.publishers.generators.AdxPublisherGenerator;
import tau.tac.adx.publishers.generators.SimplePublisherGenerator;
import tau.tac.adx.report.adn.AdNetworkKey;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.AdNetworkReportEntry;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.generators.SimpleUserGenerator;
import tau.tac.adx.util.Utils;

/**
 * @author Patrick Jordan
 */
public class AdNetworkReportTest {

	@Test
	public void testEmptyReportEntry() {
		AdNetworkReportEntry entry = new AdNetworkReportEntry(null);
		assertNotNull(entry);
	}

	@Test
	public void testEmptyReport() {
		AdNetworkReport report = new AdNetworkReport();
		assertNotNull(report);
		assertEquals(0, report.size());
	}

	@Test
	public void testBasicReportEntry() {
		AdNetworkReportEntry entry = new AdNetworkReportEntry(null);
		assertNotNull(entry);

		assertNull(entry.getKey());
		entry.setKey(mock(AdNetworkKey.class));
		assertNotNull(entry.getKey());

		assertEquals(0, entry.getBidCount());
		assertEquals(0, entry.getWinCount());
		assertEquals(0, (int) entry.getCost());

		assertEquals(entry.getClass().getSimpleName(), entry.getTransportName());
		assertEquals("AdNetworkReportEntry [bidCount=0, winCount=0, cost=0.0]",
				entry.toString());
	}

	@Test
	public void testValidTransportOfReportEntry() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());
		Random random = new Random();
		String publisherName = "publisher #" + random.nextInt();
		MarketSegment segment = MarketSegment.values()[random
				.nextInt(MarketSegment.values().length - 1)];
		Device device = Device.values()[random.nextInt(1)];
		AdType adType = AdType.values()[random.nextInt(1)];
		AdxUser adxUser = new SimpleUserGenerator().generate(1).get(0);
		int campaignId = (int) (Math.random() * 100);
		AdNetworkKey key = new AdNetworkKey(adxUser, publisherName, device,
				adType, campaignId);
		AdNetworkReportEntry entry = new AdNetworkReportEntry(key);
		entry.setBidCount(random.nextInt());
		entry.setCost(random.nextDouble());
		entry.setWinCount(random.nextInt());

		byte[] buffer = getBytesForTransportable(writer, entry);
		AdNetworkReportEntry received = readFromBytes(reader, buffer,
				entry.getTransportName());

		assertNotNull(entry);
		assertNotNull(received);

		assertEquals(entry.getBidCount(), received.getBidCount());
		assertEquals(entry.getCost(), received.getCost(), 0);
		assertEquals(entry.getWinCount(), received.getWinCount());
		assertEquals(entry.getKey(), received.getKey());
		assertEquals(entry.getClass().getSimpleName(), entry.getTransportName());
	}

	@Test
	public void testValidTransportOfReport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());
		AdNetworkReport report = new AdNetworkReport();
		report.lock();

		byte[] buffer = getBytesForTransportable(writer, report);
		AdNetworkReport received = readFromBytes(reader, buffer,
				report.getTransportName());

		assertNotNull(report);
		assertNotNull(received);
		assertEquals(report.size(), 0);
		assertEquals(received.size(), 0);

		buffer = getBytesForTransportable(writer, new AdNetworkReport());
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
		MarketSegment segment = MarketSegment.values()[random
				.nextInt(MarketSegment.values().length - 1)];
		Device device = Device.values()[random.nextInt(1)];
		AdType adType = AdType.values()[random.nextInt(1)];
		AdxUser adxUser = new SimpleUserGenerator().generate(1).get(0);
		int campaignId = (int) (Math.random() * 100);
		AdNetworkKey key = new AdNetworkKey(adxUser, publisherName, device,
				adType, campaignId);
		AdNetworkReportEntry entry = new AdNetworkReportEntry(key);
		entry.setBidCount(random.nextInt());
		entry.setCost(random.nextDouble());
		entry.setWinCount(random.nextInt());
		AdNetworkReport report = new AdNetworkReport();
		AuctionState auctionState = AuctionState.values()[random
				.nextInt(AuctionState.values().length - 1)];
		double bidPrice = random.nextDouble();
		Campaign campaign = Mockito.mock(Campaign.class);
		Mockito.when(campaign.getId()).thenReturn(campaignId);
		BidInfo winningBidInfo = new BidInfo(bidPrice, null, null, segment,
				campaign);
		Double winningPrice = random.nextDouble();
		AdxAuctionResult auctionResult = new AdxAuctionResult(auctionState,
				winningBidInfo, winningPrice, null);
		SimpleUserGenerator userGenerator = new SimpleUserGenerator();
		AdxUser user = userGenerator.generate(1).get(0);
		AdxQueryGenerator generator = Utils.getInjector().getInstance(
				AdxQueryGenerator.class);
		AdxQuery query = generator.generate(1).iterator().next();
		report.addBid(auctionResult, query, user, false);
		report.lock();

		byte[] buffer = getBytesForTransportable(writer, report);
		AdNetworkReport received = readFromBytes(reader, buffer,
				report.getTransportName());

		assertNotNull(report);
		assertNotNull(received);
		// assertEquals(MarketSegment.extractSegment(user).size(),
		// report.size());
		assertEquals(report.size(), received.size());
		campaignId = (int) (Math.random() * 100);
		AdNetworkKey adNetworkKey = new AdNetworkKey(adxUser, publisherName,
				device, adType, campaignId);
		assertEquals(report.getAdNetworkReportEntry(adNetworkKey),
				received.getAdNetworkReportEntry(adNetworkKey));

		buffer = getBytesForTransportable(writer, new AdNetworkReport());
		received = readFromBytes(reader, buffer, report.getTransportName());
		assertFalse(received.isLocked());
	}

	@Test(expected = NullPointerException.class)
	public void testAddQueryToReport() {
		AdNetworkReport report = new AdNetworkReport();

		report.addBid(null, null, null, false);
	}
}
