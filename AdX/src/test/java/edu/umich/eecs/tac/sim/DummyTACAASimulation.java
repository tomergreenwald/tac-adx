/*
 * DummyTACAASimulation.java
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

import java.util.HashMap;
import java.util.Map;

import se.sics.tasim.sim.SimulationAgent;

import edu.umich.eecs.tac.agents.DefaultPublisher;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.SlotInfo;
import edu.umich.eecs.tac.props.BankStatus;
import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.SalesReport;

/**
 * @author Ben Cassell
 */
public class DummyTACAASimulation implements BankStatusSender {

	private RetailCatalog rc;
	private SlotInfo ai;
	private SalesAnalyst sa;
	private Map<String, AdvertiserInfo> advertiserInfo = new HashMap<String, AdvertiserInfo>();
	private SimulationAgent[] ps;

	public void sendBankStatus(String accountName, BankStatus status) {
		return;
	}

	public final void setup() {
		rc = new RetailCatalog();
		rc.addProduct(new Product("man", "com"));
		ai = new SlotInfo();
		SimpleAgentRepository repository = new SimpleAgentRepository();
		SimpleSalesReportSender salesReportSender = new SimpleSalesReportSender();
		sa = new DefaultSalesAnalyst(repository, salesReportSender, 1);
		ps = new SimulationAgent[1];
		ps[0] = new SimulationAgent(new DefaultPublisher(), "dp");
	}

	public RetailCatalog getRetailCatalog() {
		return rc;
	}

	public SlotInfo getAuctionInfo() {
		return ai;
	}

	public Map<String, AdvertiserInfo> getAdvertiserInfo() {
		return advertiserInfo;
	}

	public SimulationAgent[] getPublishers() {
		return ps;
	}

	public SimulationAgent[] getUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	public SalesAnalyst getSalesAnalyst() {
		return sa;
	}

	public int getNumberOfAdvertisers() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String[] getAdvertiserAddresses() {
		// TODO Auto-generated method stub
		return null;
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
			return sa;
		}

		public SlotInfo getAuctionInfo() {
			return ai;
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
		}
	}
}
