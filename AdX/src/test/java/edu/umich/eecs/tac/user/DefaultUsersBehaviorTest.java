/*
 * DefaultUsersBehaviorTest.java
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
package edu.umich.eecs.tac.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Message;
import se.sics.tasim.is.EventWriter;
import se.sics.tasim.sim.SimulationAgent;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.Auction;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.SlotInfo;
import edu.umich.eecs.tac.sim.AgentRepository;
import edu.umich.eecs.tac.sim.Auctioneer;
import edu.umich.eecs.tac.sim.DummyTACAASimulation;
import edu.umich.eecs.tac.sim.SalesAnalyst;
import edu.umich.eecs.tac.user.DefaultUsersBehavior.ConversionMonitor;
import edu.umich.eecs.tac.util.config.ConfigProxy;

/**
 * @author Ben Cassell
 */
public class DefaultUsersBehaviorTest {

	protected DummyTACAASimulation dts;
	private DefaultUsersBehavior dub;

	@Before
	public void setUp() throws Exception {
		dts = new DummyTACAASimulation();
		dts.setup();
		dub = new DefaultUsersBehavior(new DummyUsersConfigProxy(),
				new AgentRepositoryProxy(), new UsersTransactorProxy());
		dub.setup();
	}

	@Test(expected = NullPointerException.class)
	public void testDefaultUsersBehaviorNPEA() {
		dub = new DefaultUsersBehavior(null, new AgentRepositoryProxy(),
				new UsersTransactorProxy());
	}

	@Test(expected = NullPointerException.class)
	public void testDefaultUsersBehaviorNPEB() {
		dub = new DefaultUsersBehavior(new DummyUsersConfigProxy(), null,
				new UsersTransactorProxy());
	}

	@Test(expected = NullPointerException.class)
	public void testDefaultUsersBehaviorNPEC() {
		dub = new DefaultUsersBehavior(new DummyUsersConfigProxy(),
				new AgentRepositoryProxy(), null);
	}

	@Test
	public void testNextTimeUnit() {
		dub.nextTimeUnit(0);
		dub.nextTimeUnit(1);
	}

	@Test
	public void testSetup() {
		//TODO
	}

	@Test
	public void testStopped() {
		dub.stopped();
	}

	@Test
	public void testShutdown() {
		dub.shutdown();
	}

	@Test
	public void testGetRanking() {
		DummyPublisher dumPub = new DummyPublisher();
		dub.getRanking(new Query("man", "com"), dumPub);
	}

	@Test
	public void testMessageReceived() {
		Query q = new Query("man", "com");
		Message m = new Message("dummy", q);
		dub.messageReceived(m);
	}

	@Test
	public void testAddUserEventListener() {
		dub.addUserEventListener(dts.getSalesAnalyst());
	}

	@Test
	public void testContainsUserEventListener() {
		assertFalse(dub.containsUserEventListener(dts.getSalesAnalyst()));
		dub.addUserEventListener(dts.getSalesAnalyst());
		assertTrue(dub.containsUserEventListener(dts.getSalesAnalyst()));
		ConversionMonitor cm = dub.new ConversionMonitor();
		assertFalse(dub.containsUserEventListener(cm));
		dub.removeUserEventListener(dts.getSalesAnalyst());
		assertFalse(dub.containsUserEventListener(dts.getSalesAnalyst()));
	}
	
	@Test
	public void testBroadcastUserDistribution() {
		dub.broadcastUserDistribution(0, new DummyEventWriter());
		dub.broadcastUserDistribution(0, new DummyEventWriter());
	}

	protected class DummyPublisher implements Auctioneer {

		public Auction runAuction(Query query) {
			return new Auction();
		}
		
	}
	
	protected class DummyEventWriter extends EventWriter {

		@Override
		public void dataUpdated(int agent, int type, int value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dataUpdated(int agent, int type, long value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dataUpdated(int agent, int type, float value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dataUpdated(int agent, int type, double value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dataUpdated(int agent, int type, String value) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dataUpdated(int agent, int type, Transportable content) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void dataUpdated(int type, Transportable content) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void intCache(int agent, int type, int[] cache) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void interaction(int fromAgent, int toAgent, int type) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void interactionWithRole(int fromAgent, int role, int type) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void nextTimeUnit(int timeUnit) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void participant(int agent, int role, String name,
				int participantID) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	protected class DummyUsersConfigProxy implements ConfigProxy {
		public String getProperty(String name) {
			return name;
		}

		public String getProperty(String name, String defaultValue) {
			return defaultValue;
		}

		public String[] getPropertyAsArray(String name) {
			return this.getPropertyAsArray(name, "name");
		}

		public String[] getPropertyAsArray(String name, String defaultValue) {
			char[] k = defaultValue.toCharArray();
			String[] i = new String[k.length];
			int j;
			for (j = 0; j < k.length; j++) {
				i[j] = String.valueOf(k[j]);
			}
			return i;
		}

		public int getPropertyAsInt(String name, int defaultValue) {
			return 0;
		}

		public int[] getPropertyAsIntArray(String name) {
			return this.getPropertyAsIntArray(name, "name");
		}

		public int[] getPropertyAsIntArray(String name, String defaultValue) {
			char[] k = defaultValue.toCharArray();
			int[] i = new int[k.length];
			int j;
			for (j = 0; j < k.length; j++) {
				i[j] = k[j];
			}
			return i;
		}

		public long getPropertyAsLong(String name, long defaultValue) {
			return defaultValue;
		}

		public float getPropertyAsFloat(String name, float defaultValue) {
			return defaultValue;
		}

		public double getPropertyAsDouble(String name, double defaultValue) {
			return defaultValue;
		}
	}

	protected class UsersTransactorProxy implements UsersTransactor {
		public void transact(String address, double amount) {
			this.transact(address, amount);
		}
	}

	protected class AgentRepositoryProxy implements AgentRepository {
		public RetailCatalog getRetailCatalog() {
			return dts.getRetailCatalog();
		}

		public SlotInfo getAuctionInfo() {
			return dts.getAuctionInfo();
		}

		public Map<String, AdvertiserInfo> getAdvertiserInfo() {
			return dts.getAdvertiserInfo();
		}

		public SimulationAgent[] getPublishers() {
			return dts.getPublishers();
		}

		public SimulationAgent[] getUsers() {
			return dts.getUsers();
		}

		public SalesAnalyst getSalesAnalyst() {
			return dts.getSalesAnalyst();
		}

		public int getNumberOfAdvertisers() {
			return dts.getNumberOfAdvertisers();
		}

		public String[] getAdvertiserAddresses() {
			return dts.getAdvertiserAddresses();
		}
	}
}
