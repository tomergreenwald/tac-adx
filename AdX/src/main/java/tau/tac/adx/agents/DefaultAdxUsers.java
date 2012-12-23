/*
 * DefaultUsers.java
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
package tau.tac.adx.agents;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import se.sics.tasim.aw.Message;
import se.sics.tasim.sim.SimulationAgent;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.agents.behaviors.DefaultAdxUsersBehavior;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.sim.AdxAgentRepository;
import tau.tac.adx.sim.AdxUsers;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.AdxUserEventListener;
import tau.tac.adx.users.AdxUsersBehavior;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.SlotInfo;
import edu.umich.eecs.tac.sim.AgentRepository;
import edu.umich.eecs.tac.sim.SalesAnalyst;
import edu.umich.eecs.tac.user.UsersBehavior;
import edu.umich.eecs.tac.user.UsersTransactor;
import edu.umich.eecs.tac.util.config.ConfigProxy;

/**
 * @author Lee Callender, Patrick Jordan
 */
public class DefaultAdxUsers extends AdxUsers {
	/**
	 * enclosed {@link UsersBehavior}.
	 */
	private final AdxUsersBehavior usersBehavior;

	/**
	 * Default constructor.
	 */
	public DefaultAdxUsers() {
		usersBehavior = new DefaultAdxUsersBehavior(new UsersConfigProxy(),
				new AgentRepositoryProxy(), new UsersTransactorProxy());
	}

	/**
	 * @see se.sics.tasim.aw.TimeListener#nextTimeUnit(int)
	 */
	@Override
	public void nextTimeUnit(int date) {
		usersBehavior.nextTimeUnit(date);
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Users#setup()
	 */
	@Override
	protected void setup() {
		super.setup();

		this.log = Logger.getLogger(DefaultAdxUsers.class.getName());

		usersBehavior.setup();
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Builtin#stopped()
	 */
	@Override
	protected void stopped() {
		usersBehavior.stopped();
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Builtin#shutdown()
	 */
	@Override
	protected void shutdown() {
		usersBehavior.shutdown();
	}

	/**
	 * @see se.sics.tasim.aw.Agent#messageReceived(se.sics.tasim.aw.Message)
	 */
	@Override
	protected void messageReceived(Message message) {
		usersBehavior.messageReceived(message);
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Users#addUserEventListener(edu.umich.eecs.tac.user.UserEventListener)
	 */
	@Override
	public boolean addUserEventListener(AdxUserEventListener listener) {
		return usersBehavior.addUserEventListener(listener);
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Users#containsUserEventListener(edu.umich.eecs.tac.user.UserEventListener)
	 */
	@Override
	public boolean containsUserEventListener(AdxUserEventListener listener) {
		return usersBehavior.containsUserEventListener(listener);
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Users#removeUserEventListener(edu.umich.eecs.tac.user.UserEventListener)
	 */
	@Override
	public boolean removeUserEventListener(AdxUserEventListener listener) {
		return usersBehavior.removeUserEventListener(listener);
	}

	/**
	 * {@link ConfigProxy} implementation.
	 * 
	 * @author Lee Callender, Patrick Jordan
	 * 
	 */
	protected class UsersConfigProxy implements ConfigProxy {
		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getProperty(java.lang.String)
		 */
		@Override
		public String getProperty(String name) {
			return DefaultAdxUsers.this.getProperty(name);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getProperty(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public String getProperty(String name, String defaultValue) {
			return DefaultAdxUsers.this.getProperty(name, defaultValue);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsArray(java.lang.String)
		 */
		@Override
		public String[] getPropertyAsArray(String name) {
			return DefaultAdxUsers.this.getPropertyAsArray(name);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsArray(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public String[] getPropertyAsArray(String name, String defaultValue) {
			return DefaultAdxUsers.this.getPropertyAsArray(name, defaultValue);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsInt(java.lang.String,
		 *      int)
		 */
		@Override
		public int getPropertyAsInt(String name, int defaultValue) {
			return DefaultAdxUsers.this.getPropertyAsInt(name, defaultValue);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsIntArray(java.lang.String)
		 */
		@Override
		public int[] getPropertyAsIntArray(String name) {
			return DefaultAdxUsers.this.getPropertyAsIntArray(name);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsIntArray(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public int[] getPropertyAsIntArray(String name, String defaultValue) {
			return DefaultAdxUsers.this.getPropertyAsIntArray(name,
					defaultValue);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsLong(java.lang.String,
		 *      long)
		 */
		@Override
		public long getPropertyAsLong(String name, long defaultValue) {
			return DefaultAdxUsers.this.getPropertyAsLong(name, defaultValue);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsFloat(java.lang.String,
		 *      float)
		 */
		@Override
		public float getPropertyAsFloat(String name, float defaultValue) {
			return DefaultAdxUsers.this.getPropertyAsFloat(name, defaultValue);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsDouble(java.lang.String,
		 *      double)
		 */
		@Override
		public double getPropertyAsDouble(String name, double defaultValue) {
			return DefaultAdxUsers.this.getPropertyAsDouble(name, defaultValue);
		}
	}

	/**
	 * {@link UsersTransactor} proxy implementation.
	 * 
	 * @author Lee Callender, Patrick Jordan
	 * 
	 */
	protected class UsersTransactorProxy implements UsersTransactor {
		/**
		 * @see edu.umich.eecs.tac.user.UsersTransactor#transact(java.lang.String,
		 *      double)
		 */
		@Override
		public void transact(String address, double amount) {
			DefaultAdxUsers.this.transact(address, amount);
		}
	}

	/**
	 * {@link AgentRepository} proxy implementation.
	 * 
	 * @author Lee Callender, Patrick Jordan
	 * 
	 */
	protected class AgentRepositoryProxy implements AdxAgentRepository {

		/**
		 * @see edu.umich.eecs.tac.sim.AgentRepository#getAuctionInfo()
		 */
		@Override
		public SlotInfo getAuctionInfo() {
			return getSimulation().getAuctionInfo();
		}

		/**
		 * @see edu.umich.eecs.tac.sim.AgentRepository#getAdvertiserInfo()
		 */
		@Override
		public Map<String, AdvertiserInfo> getAdvertiserInfo() {
			return getSimulation().getAdvertiserInfo();
		}

		/**
		 * @see edu.umich.eecs.tac.sim.AgentRepository#getPublishers()
		 */
		@Override
		public SimulationAgent[] getPublishers() {
			return getSimulation().getPublishers();
		}

		/**
		 * @see edu.umich.eecs.tac.sim.AgentRepository#getUsers()
		 */
		@Override
		public SimulationAgent[] getUsers() {
			return getSimulation().getUsers();
		}

		/**
		 * @see edu.umich.eecs.tac.sim.AgentRepository#getSalesAnalyst()
		 */
		@Override
		public SalesAnalyst getSalesAnalyst() {
			return getSimulation().getSalesAnalyst();
		}

		/**
		 * @see edu.umich.eecs.tac.sim.AgentRepository#getNumberOfAdvertisers()
		 */
		@Override
		public int getNumberOfAdvertisers() {
			return getSimulation().getNumberOfAdvertisers();
		}

		/**
		 * @see edu.umich.eecs.tac.sim.AgentRepository#getAdvertiserAddresses()
		 */
		@Override
		public String[] getAdvertiserAddresses() {
			return getSimulation().getAdvertiserAddresses();
		}

		@Override
		public PublisherCatalog getPublisherCatalog() {
			return getSimulation().getPublisherCatalog();
		}

		@Override
		public SimulationAgent[] getAdxUsers() {
			return getSimulation().getAdxUsers();
		}

		@Override
		public List<AdxUser> getUserPopulation() {
			return getSimulation().getUserPopulation();
		}

		@Override
		public Map<Device, Integer> getDeviceDistributionMap() {
			return getSimulation().getDeviceDistributionMap();
		}

		@Override
		public Map<AdType, Integer> getAdTypeDistributionMap() {
			return getSimulation().getAdTypeDistributionMap();
		}

		@Override
		public RetailCatalog getRetailCatalog() {
			return getSimulation().getRetailCatalog();
		}
	}
}
