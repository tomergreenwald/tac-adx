/*
 * QueryReportTest.java
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.BinaryTransportWriter;
import tau.tac.adx.props.AdxInfoContextFactory;

public class QueryReportTest {
	@Test
	public void testEmptyQueryReportEntry() {
		QueryReport.QueryReportEntry entry = new QueryReport.QueryReportEntry();
		assertNotNull(entry);
	}

	@Test
	public void testEmptyQueryReport() {
		QueryReport report = new QueryReport();
		assertNotNull(report);
		assertFalse(report.containsQuery(new Query()));
	}

	@Test
	public void testBasicQueryReportEntry() {
		QueryReport.QueryReportEntry entry = new QueryReport.QueryReportEntry();
		assertNotNull(entry);

		assertNull(entry.getQuery());
		entry.setQuery(new Query());
		assertNotNull(entry.getQuery());

		assertEquals(entry.getRegularImpressions(), 0);
		assertEquals(entry.getPromotedImpressions(), 0);
        assertEquals(entry.getImpressions(), 0);
		entry.setImpressions(1, 0);
		assertEquals(entry.getRegularImpressions(), 1);
		assertEquals(entry.getPromotedImpressions(), 0);
        assertEquals(entry.getImpressions(), 1);
		entry.setImpressions(0, 1);
		assertEquals(entry.getRegularImpressions(), 0);
		assertEquals(entry.getPromotedImpressions(), 1);
        assertEquals(entry.getImpressions(), 1);
		entry.setRegularImpressions(1);
        entry.setPromotedImpressions(1);
		assertEquals(entry.getRegularImpressions(), 1);
		assertEquals(entry.getPromotedImpressions(), 1);
        assertEquals(entry.getImpressions(), 2);
		assertEquals(entry.getClicks(), 0);
		entry.setClicks(2);
		assertEquals(entry.getClicks(), 2);

		assertEquals(entry.getCPC(), 0.0, 0);
		entry.setCost(3.0);
		assertEquals(entry.getCost(), 3.0, 0);
		assertEquals(entry.getCPC(), 1.5, 0);

		assertEquals(entry.getPosition(), 0.0, 0);
		entry.addPosition(4.0);
		assertEquals(entry.getPosition(), 2.0, 0);

		assertEquals(entry.getTransportName(), "QueryReportEntry");
		assertEquals(
				entry.toString(),
				"((Query (null,null)) regular_impr: 1 promoted_impr: 1 clicks: 2 pos: 2.000000 cpc: 1.500000 advertisers: (DisplayReport))");

	}

	@Test
	public void testValidTransportOfQueryReportEntry() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		QueryReport.QueryReportEntry entry = new QueryReport.QueryReportEntry();
		entry.setQuery(new Query());
		entry.setImpressions(1, 1);
		entry.setClicks(2);
		entry.setCost(3.0);
		entry.addPosition(4.0);

		byte[] buffer = getBytesForTransportable(writer, entry);
		QueryReport.QueryReportEntry received = readFromBytes(reader, buffer,
				"QueryReportEntry");

		assertNotNull(entry);
		assertNotNull(received);

		assertEquals(received.getQuery(), new Query());
		assertEquals(received.getRegularImpressions(), 1);
		assertEquals(received.getPromotedImpressions(), 1);
        assertEquals(received.getImpressions(), 2);
		assertEquals(received.getClicks(), 2);
		assertEquals(received.getCost(), 3.0, 0);
		assertEquals(received.getCPC(), 1.5, 0);
		assertEquals(received.getPosition(), 2.0, 0);
		assertEquals(entry.getTransportName(), "QueryReportEntry");

	}

	@Test
	public void testValidTransportOfNullQueryReportEntry()
			throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		QueryReport.QueryReportEntry entry = new QueryReport.QueryReportEntry();
		entry.setImpressions(1, 1);
		entry.setClicks(2);
		entry.setCost(3.0);
		entry.addPosition(4.0);

		byte[] buffer = getBytesForTransportable(writer, entry);
		QueryReport.QueryReportEntry received = readFromBytes(reader, buffer,
				"QueryReportEntry");

		assertNotNull(entry);
		assertNotNull(received);

		assertNull(received.getQuery());
		assertEquals(received.getRegularImpressions(), 1);
		assertEquals(received.getPromotedImpressions(), 1);
		assertEquals(received.getClicks(), 2);
		assertEquals(received.getCost(), 3.0, 0);
		assertEquals(received.getCPC(), 1.5, 0);
		assertEquals(received.getPosition(), 2.0, 0);

		assertEquals(received.getTransportName(), "QueryReportEntry");
		assertEquals(
				received.toString(),
				"(null regular_impr: 1 promoted_impr: 1 clicks: 2 pos: 2.000000 cpc: 1.500000 advertisers: (DisplayReport))");
	}

	@Test
	public void testValidTransportOfQueryReport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		QueryReport report = new QueryReport();
		report.setClicks(new Query(), 2);
		report.lock();

		byte[] buffer = getBytesForTransportable(writer, report);
		QueryReport received = readFromBytes(reader, buffer, "QueryReport");

		assertNotNull(report);
		assertNotNull(received);
		assertEquals(report.size(), 1);
		assertEquals(received.size(), 1);
		assertEquals(received.getClicks(new Query()), 2);

		assertEquals(report.getTransportName(), "QueryReport");

		buffer = getBytesForTransportable(writer, new QueryReport());
		received = readFromBytes(reader, buffer, "QueryReport");
		assertFalse(received.isLocked());
	}

	@Test(expected = NullPointerException.class)
	public void testAddQueryToQueryReport() {
		QueryReport report = new QueryReport();
		assertEquals(report.size(), 0);

		report.addQuery(new Query());
		assertEquals(report.size(), 1);
		assertTrue(report.containsQuery(new Query()));

		Query q = new Query();
		q.setComponent("c1");
		report.addQuery(q, 10, 5, 100, 40.0, 4.0);
		assertEquals(report.size(), 2);
		assertTrue(report.containsQuery(q));

		report.addQuery(null);
	}

	@Test(expected = NullPointerException.class)
	public void testSetConversions() {
		QueryReport report = new QueryReport();
		assertEquals(report.size(), 0);

		report.setImpressions(new Query(), 2, 1);
		assertEquals(report.size(), 1);
		assertTrue(report.containsQuery(new Query()));
		assertEquals(report.getRegularImpressions(new Query()), 2);
		assertEquals(report.getPromotedImpressions(new Query()), 1);

		report.setImpressions(new Query(), 3, 2);
		assertEquals(report.size(), 1);
		assertEquals(report.getRegularImpressions(new Query()), 3);
		assertEquals(report.getPromotedImpressions(new Query()), 2);

		report.setImpressions(null, 2, 2);
	}

	@Test(expected = NullPointerException.class)
	public void testSetRevenue() {
		QueryReport report = new QueryReport();
		assertEquals(report.size(), 0);

		report.setCost(new Query(), 1.0);
		assertEquals(report.size(), 1);
		assertTrue(report.containsQuery(new Query()));
		assertEquals(report.getCPC(new Query()), Double.POSITIVE_INFINITY, 0);

		report.setCost(new Query(), 3.0);
		assertEquals(report.size(), 1);
		assertEquals(report.getCPC(new Query()), Double.POSITIVE_INFINITY, 0);

		report.setCost(null, 2);
	}

	@Test
	public void testSalesReportGettersForEmptyQueries() {
		QueryReport report = new QueryReport();
		assertEquals(report.size(), 0);

		assertEquals(report.getRegularImpressions(null), 0);
		assertEquals(report.getPromotedImpressions(null), 0);
		assertEquals(report.getClicks(null), 0);
		assertEquals(report.getCost(null), 0.0, 0);
		assertEquals(report.getCPC(null), Double.NaN, 0);
		assertEquals(report.getPosition(null), Double.NaN, 0);
	}

	@Test
	public void testQueryReportToString() {
		QueryReport report = new QueryReport();
		report.addQuery(new Query());
		assertEquals(
				report.toString(),
				"(QueryReport ((Query (null,null)) regular_impr: 0 promoted_impr: 0 clicks: 0 pos: NaN cpc: NaN advertisers: (DisplayReport)))");
	}

	@Test
	public void testQueryReportAdders() {
		QueryReport report = new QueryReport();

		Query query = new Query();

		assertEquals(report.getRegularImpressions(query), 0);
		assertEquals(report.getPromotedImpressions(query), 0);
		assertEquals(report.getCPC(query), Double.NaN, 0);
		report.addImpressions(query, 3, 2);
		report.addClicks(query, 3);
		assertEquals(report.getRegularImpressions(query), 3);
		assertEquals(report.getPromotedImpressions(query), 2);
        assertEquals(report.getImpressions(query), 5);
		assertEquals(report.getClicks(query), 3);
		report.addImpressions(query, 2, 3);
		report.addClicks(query, 3);
		assertEquals(report.getRegularImpressions(query), 5);
		assertEquals(report.getPromotedImpressions(query), 5);
        assertEquals(report.getImpressions(query), 10);
		assertEquals(report.getClicks(query), 6);

		report = new QueryReport();
		report.addClicks(query, 3);
		report.addImpressions(query, 10, 1);
		assertEquals(report.getClicks(query), 3);
		assertEquals(report.getRegularImpressions(query), 10);
		assertEquals(report.getPromotedImpressions(query), 1);
        assertEquals(report.getImpressions(query), 11);
	}

	@Test
	public void testPosition() {
		QueryReport instance = new QueryReport();
		instance.setPositionSum(new Query(), 0);
		instance.setPositionSum(0, 3.2);
		assertEquals(instance.getPosition(0), Double.POSITIVE_INFINITY, 0);

		Query q = new Query();
		q.setComponent("c1");
		instance.setPositionSum(q, 4.0);
		instance.setImpressions(q, 1, 1);
		assertEquals(instance.getPosition(q), 2.0, 0);

		instance.setPositionSum(q, 2.0);
		assertEquals(instance.getPosition(q), 1.0, 0);
	}

	@Test
	public void testClicks() {
		QueryReport instance = new QueryReport();
		Query query = new Query();
		query.setComponent("c1");
		instance.setClicks(query, 3);
		assertEquals(instance.getClicks(query), 3);
		instance.setClicks(query, 4);
		assertEquals(instance.getClicks(query), 4);
	}

	@Test
	public void testDisplayResult() {
		QueryReport.DisplayReport report = new QueryReport.DisplayReport();

		String advertiser = "alice";

		report.addAdvertiser(advertiser);

		assertEquals(report.size(), 1);

		assertNull(report.getAd(null));
		assertNull(report.getAd(advertiser));

		Ad ad = new Ad();

		report.setAd(advertiser, ad);
		assertEquals(report.getAd(advertiser), new Ad());

		report.setPosition(advertiser, 1.0);
		assertEquals(report.getPosition(advertiser), 1.0, 0);

		report.setAdAndPosition(advertiser, null, 0.0);
		assertEquals(report.getAd(advertiser), null);
		assertEquals(report.getPosition(advertiser), 0.0, 0);
		assertTrue(Double.isNaN(report.getPosition(null)));

		report = new QueryReport.DisplayReport();
		report.setAd(advertiser, ad);
		assertEquals(report.getAd(advertiser), new Ad());

		report = new QueryReport.DisplayReport();
		report.setPosition(advertiser, 1.0);
		assertEquals(report.getPosition(advertiser), 1.0, 0);

		report = new QueryReport.DisplayReport();
		report.setAdAndPosition(advertiser, ad, 1.0);
		assertEquals(report.getAd(advertiser), new Ad());
		assertEquals(report.getPosition(advertiser), 1.0, 0);
	}

	@Test
	public void testDisplayResultEntry() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		QueryReport.DisplayReportEntry entry = new QueryReport.DisplayReportEntry();
		entry.setAdvertiser("alice");

		byte[] buffer = getBytesForTransportable(writer, entry);
		QueryReport.DisplayReportEntry received = readFromBytes(reader, buffer,
				"DisplayReportEntry");

		assertEquals(entry.getAd(), received.getAd());
		assertEquals(entry.getPosition(), received.getPosition(), 0);
		assertEquals("alice", received.getAdvertiser());
		entry.setAd(new Ad());

		buffer = getBytesForTransportable(writer, entry);
		received = readFromBytes(reader, buffer, "DisplayReportEntry");

		assertEquals(new Ad(), received.getAd());

		entry.setPosition(1.0);

		buffer = getBytesForTransportable(writer, entry);
		received = readFromBytes(reader, buffer, "DisplayReportEntry");

		assertEquals(1.0, received.getPosition(), 0);
	}

	@Test
	public void testQueryReportEntry() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		QueryReport.QueryReportEntry entry = new QueryReport.QueryReportEntry();

		byte[] buffer = getBytesForTransportable(writer, entry);
		QueryReport.QueryReportEntry received = readFromBytes(reader, buffer,
				"QueryReportEntry");

		assertNotNull(received);

		String alice = "alice";
		String bob = "bob";

		double aliceCost = 1.0;

		double alicePosition = 2.0;
		double bobPosition = 3.0;

		Set<String> advertisers = new HashSet<String>(Arrays.asList(alice, bob));

		Ad aliceAd = new Ad();
		Ad bobAd = new Ad(new Product("m", "c"));

		entry.addCost(aliceCost);
		entry.addCost(aliceCost);

		entry.setAd(new Ad(new Product("mm", "cc")));

		entry.setAd(alice, aliceAd);
		entry.setPosition(alice, alicePosition);
		entry.setAdAndPosition(bob, bobAd, bobPosition);

		buffer = getBytesForTransportable(writer, entry);
		received = readFromBytes(reader, buffer, "QueryReportEntry");

		assertEquals(received.advertisers(), new HashSet<String>(Arrays.asList(
				alice, bob)));

		assertEquals(received.getPosition(alice), alicePosition, 0);
		assertEquals(received.getPosition(bob), bobPosition, 0);

		assertEquals(received.getAd(alice), new Ad());
		assertEquals(received.getAd(bob), new Ad(new Product("m", "c")));

		assertEquals(received.getAd(), new Ad(new Product("mm", "cc")));

		assertEquals(received.getCost(), 2 * aliceCost, 0);
	}

	@Test
	public void testBasicQueryReportSetImpressions() {
		QueryReport report = new QueryReport();

		Query query = new Query();
		String advertiser = "alice";
		Ad ad = new Ad();
		double position = 1.0;
		int impressions = 10;
		int promotedImpressions = 5;
		int clicks = 3;
		int conversions = 1;

        assertEquals(report.getImpressions(query), 0);
		report.setImpressions(query, impressions, promotedImpressions, ad, position * (impressions + promotedImpressions));

		assertEquals(report.getAd(query), ad);
		assertEquals(report.getRegularImpressions(query), impressions);
		assertEquals(report.getPromotedImpressions(query), promotedImpressions);
		assertEquals(report.getPosition(query), position, 0);

		report.setImpressions(query, impressions, promotedImpressions, ad, position * (impressions + promotedImpressions));

		assertEquals(report.getAd(query), ad);
		assertEquals(report.getRegularImpressions(query), impressions);
		assertEquals(report.getPromotedImpressions(query), promotedImpressions);
		assertEquals(report.getPosition(query), position, 0);
	}

	@Test
	public void testBasicQueryReportAddImpressions() {
		QueryReport report = new QueryReport();

		Query query = new Query();
		String advertiser = "alice";
		Ad ad = new Ad();
		double position = 1.0;
		int impressions = 10;
		int promotedImpressions = 5;
		int clicks = 3;
		int conversions = 1;

		report.addImpressions(query, (impressions - promotedImpressions), promotedImpressions, ad, position * impressions);
		report.addImpressions(query, (impressions - promotedImpressions), promotedImpressions, ad, position * impressions);

		assertEquals(report.getAd(query), ad);
		assertEquals(report.getImpressions(query), 2 * impressions);
		assertEquals(report.getPromotedImpressions(query), 2 * promotedImpressions);
		assertEquals(report.getPosition(query), position, 0);
	}

	@Test
	public void testBasicQueryReportSetClicks() {
		QueryReport report = new QueryReport();

		Query query = new Query();
		String advertiser = "alice";
		Ad ad = new Ad();
		double position = 1.0;
		int impressions = 10;
		int promotedImpressions = 5;
		int clicks = 3;
		int conversions = 1;
		double cost = 5.0;

		report.setClicks(query, clicks, cost);
		report.setImpressions(query, impressions, promotedImpressions, ad,
				position);

		assertEquals(report.getClicks(query), clicks);
		assertEquals(report.getCost(query), cost, 0);
		assertEquals(report.getCPC(query), cost / clicks, 0);

		report.setClicks(query, clicks, cost);

		assertEquals(report.getClicks(query), clicks);
		assertEquals(report.getCost(query), cost, 0);
		assertEquals(report.getCPC(query), cost / clicks, 0);
	}

	@Test
	public void testBasicQueryReportAddClicks() {
		QueryReport report = new QueryReport();

		Query query = new Query();
		String advertiser = "alice";
		Ad ad = new Ad();
		double position = 1.0;
		int impressions = 10;
		int promotedImpressions = 5;
		int clicks = 3;
		int conversions = 1;
		double cost = 5.0;

		report.addClicks(query, clicks, cost);
		report.addClicks(query, clicks, cost);
		report.setImpressions(query, impressions, promotedImpressions, ad,
				position);

		assertEquals(report.getClicks(query), 2 * clicks);
		assertEquals(report.getCost(query), 2 * cost, 0);
		assertEquals(report.getCPC(query), cost / clicks, 0);

	}

	@Test
	public void testBasicQueryReportAddCost() {
		QueryReport report = new QueryReport();

		Query query = new Query();
		String advertiser = "alice";
		Ad ad = new Ad();
		double position = 1.0;
		int impressions = 10;
		int clicks = 3;
		int conversions = 1;
		double cost = 5.0;

		report.addCost(query, cost);
		report.addCost(query, cost);

		assertEquals(report.getCost(query), 2 * cost, 0);

	}

	@Test
	public void testBasicQueryReportPosition() {
		QueryReport report = new QueryReport();

		Query query = new Query();
		String advertiser = "alice";
		Ad ad = new Ad();
		double position = 1.0;
		int impressions = 10;
		int clicks = 3;
		int conversions = 1;
		double cost = 5.0;

		report.setPosition(query, advertiser, position);

		assertEquals(report.getPosition(query, advertiser), position, 0);
		assertEquals(report.getPosition(null, advertiser), Double.NaN, 0);

		report.setPosition(query, advertiser, position);

		assertEquals(report.getPosition(query, advertiser), position, 0);
	}

	@Test
	public void testBasicQueryReportAd() {
		QueryReport report = new QueryReport();

		Query query = new Query();
		String advertiser = "alice";
		Ad ad = new Ad();
		double position = 1.0;
		int impressions = 10;
		int clicks = 3;
		int conversions = 1;
		double cost = 5.0;

		report.setAd(query, ad);

		assertEquals(report.getAd(query), ad);

		report.setAd(query, ad);

		assertEquals(report.getAd(query), ad);

		assertNull(report.getAd(null));

		report = new QueryReport();

		report.setAd(query, advertiser, ad);

		assertEquals(report.getAd(query, advertiser), ad);

		assertNull(report.getAd(null, advertiser));

		report.setAd(query, advertiser, ad);

		assertEquals(report.getAd(query, advertiser), ad);
	}

	@Test
	public void testBasicQueryReportAdAndPosition() {
		QueryReport report = new QueryReport();

		Query query = new Query();
		String advertiser = "alice";
		Ad ad = new Ad();
		double position = 1.0;
		int impressions = 10;
		int clicks = 3;
		int conversions = 1;
		double cost = 5.0;

		report.setAdAndPosition(query, advertiser, ad, position);

		assertEquals(report.getAd(query, advertiser), ad);
		assertEquals(report.getPosition(query, advertiser), position, 0);

		report.setAdAndPosition(query, advertiser, ad, position);

		assertEquals(report.getAd(query, advertiser), ad);
		assertEquals(report.getPosition(query, advertiser), position, 0);
	}

	@Test
	public void testBasicQueryAdvertiserTransport() throws ParseException {
		BinaryTransportWriter writer = new BinaryTransportWriter();
		BinaryTransportReader reader = new BinaryTransportReader();
		reader.setContext(new AdxInfoContextFactory().createContext());

		Query query = new Query();
		String alice = "alice";
		String bob = "bob";

		double alicePosition = 2.0;
		double bobPosition = 3.0;

		Set<String> advertisers = new HashSet<String>(Arrays.asList(alice, bob));

		Ad aliceAd = new Ad();
		Ad bobAd = new Ad(new Product("m", "c"));

		QueryReport report = new QueryReport();

		report.setAdAndPosition(query, alice, aliceAd, alicePosition);
		report.setAdAndPosition(query, bob, bobAd, bobPosition);

		byte[] buffer = getBytesForTransportable(writer, report);
		QueryReport received = readFromBytes(reader, buffer, "QueryReport");

		assertEquals(received.getAd(query, alice), aliceAd);
		assertEquals(received.getAd(query, bob), bobAd);

		assertEquals(received.getPosition(query, alice), alicePosition, 0);
		assertEquals(received.getPosition(query, bob), bobPosition, 0);

		assertEquals(received.advertisers(query), advertisers);
		assertEquals(received.advertisers(null), Collections.EMPTY_SET);
	}
}
