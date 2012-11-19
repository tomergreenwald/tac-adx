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
package edu.umich.eecs.tac.agents;

import edu.umich.eecs.tac.sim.*;
import edu.umich.eecs.tac.user.*;
import edu.umich.eecs.tac.TACAAConstants;
import edu.umich.eecs.tac.util.config.ConfigProxy;
import edu.umich.eecs.tac.props.*;
import se.sics.tasim.aw.Message;
import se.sics.tasim.sim.SimulationAgent;

import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Lee Callender, Patrick Jordan
 */
public class DefaultUsers extends Users {
	private UsersBehavior usersBehavior;

	public DefaultUsers() {
		usersBehavior = new DefaultUsersBehavior(new UsersConfigProxy(),
				new AgentRepositoryProxy(), new UsersTransactorProxy());
	}

	public void nextTimeUnit(int date) {
		usersBehavior.nextTimeUnit(date);
	}

	protected void setup() {
		super.setup();

		this.log = Logger.getLogger(DefaultUsers.class.getName());

		usersBehavior.setup();
	}

	protected void stopped() {
		usersBehavior.stopped();
	}

	protected void shutdown() {
		usersBehavior.shutdown();
	}

	protected void messageReceived(Message message) {
		usersBehavior.messageReceived(message);
	}

	public boolean addUserEventListener(UserEventListener listener) {
		return usersBehavior.addUserEventListener(listener);
	}

	public boolean containsUserEventListener(UserEventListener listener) {
		return usersBehavior.containsUserEventListener(listener);
	}

	public boolean removeUserEventListener(UserEventListener listener) {
		return usersBehavior.removeUserEventListener(listener);
	}

	public void broadcastUserDistribution() {
		usersBehavior.broadcastUserDistribution(getIndex(), getEventWriter());
	}

	protected class UsersConfigProxy implements ConfigProxy {
		public String getProperty(String name) {
			return DefaultUsers.this.getProperty(name);
	}

		public String getProperty(String name, String defaultValue) {
			return DefaultUsers.this.getProperty(name, defaultValue);
		}

		public String[] getPropertyAsArray(String name) {
			return DefaultUsers.this.getPropertyAsArray(name);
		}

		public String[] getPropertyAsArray(String name, String defaultValue) {
			return DefaultUsers.this.getPropertyAsArray(name, defaultValue);
		}

		public int getPropertyAsInt(String name, int defaultValue) {
			return DefaultUsers.this.getPropertyAsInt(name, defaultValue);
		}

		public int[] getPropertyAsIntArray(String name) {
			return DefaultUsers.this.getPropertyAsIntArray(name);
		}

		public int[] getPropertyAsIntArray(String name, String defaultValue) {
			return DefaultUsers.this.getPropertyAsIntArray(name, defaultValue);
		}

		public long getPropertyAsLong(String name, long defaultValue) {
			return DefaultUsers.this.getPropertyAsLong(name, defaultValue);
		}

		public float getPropertyAsFloat(String name, float defaultValue) {
			return DefaultUsers.this.getPropertyAsFloat(name, defaultValue);
		}

		public double getPropertyAsDouble(String name, double defaultValue) {
			return DefaultUsers.this.getPropertyAsDouble(name, defaultValue);
		}
	}

	protected class UsersTransactorProxy implements UsersTransactor {
		public void transact(String address, double amount) {
			DefaultUsers.this.transact(address, amount);
		}
	}

	protected class AgentRepositoryProxy implements AgentRepository {
		public RetailCatalog getRetailCatalog() {
			return getSimulation().getRetailCatalog();
		}

		public SlotInfo getAuctionInfo() {
			return getSimulation().getAuctionInfo();
		}

		public Map<String, AdvertiserInfo> getAdvertiserInfo() {
			return getSimulation().getAdvertiserInfo();
		}

		public SimulationAgent[] getPublishers() {
			return getSimulation().getPublishers();
		}

		public SimulationAgent[] getUsers() {
			return getSimulation().getUsers();
		}

		public SalesAnalyst getSalesAnalyst() {
			return getSimulation().getSalesAnalyst();
		}

		public int getNumberOfAdvertisers() {
			return getSimulation().getNumberOfAdvertisers();
		}

		public String[] getAdvertiserAddresses() {
			return getSimulation().getAdvertiserAddresses();
		}
	}
}
