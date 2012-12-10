/*
 * QueryReportManagerImplTest.java
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
package edu.umich.eecs.tac.auction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryReport;
import edu.umich.eecs.tac.sim.QueryReportSender;

/**
 * @author Patrick Jordan
 */
public class QueryReportManagerImplTest {

	@Test
	public void testConstructor() {
		QueryReportManager queryReportManager = new QueryReportManagerImpl(
				new QueryReportSenderImpl(), 0);
		assertNotNull(queryReportManager);
	}

	@Test
	public void testAddAdvertiser() {
		QueryReportManager queryReportManager = new QueryReportManagerImpl(
				new QueryReportSenderImpl(), 0);

		String advertiser = "alice";

		queryReportManager.addAdvertiser(advertiser);

		assertEquals(queryReportManager.size(), 1, 0);

		queryReportManager.addAdvertiser(advertiser);

		assertEquals(queryReportManager.size(), 1, 0);

		for (int i = 0; i < 8; i++) {
			queryReportManager.addAdvertiser("" + i);
			assertEquals(queryReportManager.size(), i + 2, 0);
		}
	}

	@Test
	public void testSendQueryReports() {
		QueryReportManagerImpl queryReportManager = new QueryReportManagerImpl(
				new QueryReportSenderImpl(), 0);

		String alice = "alice";
		String bob = "bob";

		queryReportManager.addAdvertiser(alice);
		queryReportManager.addAdvertiser(bob);
		assertEquals(queryReportManager.size(), 2, 0);

		queryReportManager.sendQueryReportToAll();

		Query query = new Query();
		Ad ad = new Ad();

		queryReportManager.queryIssued(query);
		queryReportManager.viewed(query, ad, 1, alice, false);
		queryReportManager.clicked(query, ad, 1, 1.0, alice);
		queryReportManager.converted(query, ad, 1, 2.0, alice);

		queryReportManager.sendQueryReportToAll();

		queryReportManager.addClicks("c", query, 1, 1.0);
		queryReportManager.addImpressions("d", query, 1, 0, null, 1);
		queryReportManager.addImpressions("c", query, 0, 1, null, 1);
	}

	private static class QueryReportSenderImpl implements QueryReportSender {
		public void sendQueryReport(String advertiser, QueryReport report) {
		}

        public void broadcastImpressions(String advertiser, int impressions) {
        }

        public void broadcastClicks(String advertiser, int clicks) {
        }
    }
}
