/*
 * DefaultSalesAnalystTest.java
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
package edu.umich.eecs.tac.sim;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import edu.umich.eecs.tac.props.*;

import java.util.Map;
import java.util.HashMap;

import se.sics.tasim.sim.SimulationAgent;

/**
 * @author Patrick Jordan, Ben Cassell
 */
public class DefaultSalesAnalystTest {
	DefaultSalesAnalyst salesAnalyst;
	AgentRepository repository;
	SalesReportSender salesReportSender;
	Map<String, AdvertiserInfo> advertiserInfo;

	String alice;

	private SlotInfo slotInfo;

	@Before
	public void setup() {
		slotInfo = new SlotInfo();

		repository = new SimpleAgentRepository();
		salesReportSender = new SimpleSalesReportSender();

		advertiserInfo = new HashMap<String, AdvertiserInfo>();
		AdvertiserInfo info = new AdvertiserInfo();
		info.setDistributionWindow(2);
		info.setDistributionCapacity(10);

		alice = "alice";
		advertiserInfo.put(alice, info);

		salesAnalyst = new DefaultSalesAnalyst(repository, salesReportSender, 1);

	}

	@Test
	public void testConstructor() {
		assertNotNull(salesAnalyst);
	}

	@Test
	public void testAddAccount() {
		assertEquals(salesAnalyst.size(), 0, 0);
		salesAnalyst.addAccount(alice);
		assertEquals(salesAnalyst.size(), 1, 0);
	}

	@Test
	public void testConversions() {
		salesAnalyst.addAccount(alice);
		
		salesAnalyst.addConversions(alice, new Query(), 10, 1.0);

		assertEquals(salesAnalyst.getRecentConversions(alice), 10.0, 0.0);

		salesAnalyst.addConversions(alice, new Query(), 12, 2.0);

		assertEquals(salesAnalyst.getRecentConversions(alice), 22.0, 0.0);

		salesAnalyst.sendSalesReportToAll();

		assertEquals(salesAnalyst.getRecentConversions(alice), 22.0, 0.0);

		salesAnalyst.addConversions(alice, new Query(), 5, 2.0);

		assertEquals(salesAnalyst.getRecentConversions(alice), 27.0, 0.0);

		salesAnalyst.sendSalesReportToAll();

		assertEquals(salesAnalyst.getRecentConversions(alice), 5.0, 0.0);
	}

	public class SimpleAgentRepository implements AgentRepository {
		public RetailCatalog getRetailCatalog() {
			return null;
		}

		public Map<String, AdvertiserInfo> getAdvertiserInfo() {
			return advertiserInfo;
		}

		public SimulationAgent[] getPublishers() {
			return new SimulationAgent[0];
		}

		public SimulationAgent[] getUsers() {
			return new SimulationAgent[0];
		}

		public SalesAnalyst getSalesAnalyst() {
			return salesAnalyst;
		}

		public SlotInfo getAuctionInfo() {
			return slotInfo;
		}

		public int getNumberOfAdvertisers() {
			return advertiserInfo.size();
		}

		public String[] getAdvertiserAddresses() {
			return advertiserInfo.keySet().toArray(new String[0]);
		}
	}

	public class SimpleSalesReportSender implements SalesReportSender {
		public void sendSalesReport(String advertiser, SalesReport report) {
		}

		public void broadcastConversions(String advertiser, int conversions) {
			// assertEquals(conversions,1);
		}
	}
}
