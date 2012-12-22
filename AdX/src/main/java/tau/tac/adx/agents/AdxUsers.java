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

import java.util.Map;
import java.util.logging.Logger;

import se.sics.tasim.aw.Message;
import se.sics.tasim.sim.SimulationAgent;
import tau.tac.adx.agents.behaviors.AdxUsersBehavior;
import tau.tac.adx.sim.Users;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.SlotInfo;
import edu.umich.eecs.tac.sim.AgentRepository;
import edu.umich.eecs.tac.sim.SalesAnalyst;
import edu.umich.eecs.tac.user.UserEventListener;
import edu.umich.eecs.tac.user.UsersBehavior;
import edu.umich.eecs.tac.user.UsersTransactor;
import edu.umich.eecs.tac.util.config.ConfigProxy;

/**
 * @author Lee Callender, Patrick Jordan
 */
public class AdxUsers extends Users {
	/**
	 * enclosed {@link UsersBehavior}.
	 */
	private final UsersBehavior usersBehavior;

	/**
	 * Default constructor.
	 */
	public AdxUsers() {
		usersBehavior = new AdxUsersBehavior(new UsersConfigProxy(),
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

		this.log = Logger.getLogger(AdxUsers.class.getName());

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
	public boolean addUserEventListener(UserEventListener listener) {
		return usersBehavior.addUserEventListener(listener);
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Users#containsUserEventListener(edu.umich.eecs.tac.user.UserEventListener)
	 */
	@Override
	public boolean containsUserEventListener(UserEventListener listener) {
		return usersBehavior.containsUserEventListener(listener);
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Users#removeUserEventListener(edu.umich.eecs.tac.user.UserEventListener)
	 */
	@Override
	public boolean removeUserEventListener(UserEventListener listener) {
		return usersBehavior.removeUserEventListener(listener);
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Users#broadcastUserDistribution()
	 */
	@Override
	public void broadcastUserDistribution() {
		usersBehavior.broadcastUserDistribution(getIndex(), getEventWriter());
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
			return AdxUsers.this.getProperty(name);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getProperty(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public String getProperty(String name, String defaultValue) {
			return AdxUsers.this.getProperty(name, defaultValue);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsArray(java.lang.String)
		 */
		@Override
		public String[] getPropertyAsArray(String name) {
			return AdxUsers.this.getPropertyAsArray(name);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsArray(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public String[] getPropertyAsArray(String name, String defaultValue) {
			return AdxUsers.this.getPropertyAsArray(name, defaultValue);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsInt(java.lang.String,
		 *      int)
		 */
		@Override
		public int getPropertyAsInt(String name, int defaultValue) {
			return AdxUsers.this.getPropertyAsInt(name, defaultValue);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsIntArray(java.lang.String)
		 */
		@Override
		public int[] getPropertyAsIntArray(String name) {
			return AdxUsers.this.getPropertyAsIntArray(name);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsIntArray(java.lang.String,
		 *      java.lang.String)
		 */
		@Override
		public int[] getPropertyAsIntArray(String name, String defaultValue) {
			return AdxUsers.this.getPropertyAsIntArray(name, defaultValue);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsLong(java.lang.String,
		 *      long)
		 */
		@Override
		public long getPropertyAsLong(String name, long defaultValue) {
			return AdxUsers.this.getPropertyAsLong(name, defaultValue);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsFloat(java.lang.String,
		 *      float)
		 */
		@Override
		public float getPropertyAsFloat(String name, float defaultValue) {
			return AdxUsers.this.getPropertyAsFloat(name, defaultValue);
		}

		/**
		 * @see edu.umich.eecs.tac.util.config.ConfigProxy#getPropertyAsDouble(java.lang.String,
		 *      double)
		 */
		@Override
		public double getPropertyAsDouble(String name, double defaultValue) {
			return AdxUsers.this.getPropertyAsDouble(name, defaultValue);
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
			AdxUsers.this.transact(address, amount);
		}
	}

	/**
	 * {@link AgentRepository} proxy implementation.
	 * 
	 * @author Lee Callender, Patrick Jordan
	 * 
	 */
	protected class AgentRepositoryProxy implements AgentRepository {
		/**
		 * @see edu.umich.eecs.tac.sim.AgentRepository#getRetailCatalog()
		 */
		@Override
		public RetailCatalog getRetailCatalog() {
			return getSimulation().getRetailCatalog();
		}

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
	}
}
