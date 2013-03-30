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

import java.util.Map;
import java.util.logging.Logger;

import se.sics.tasim.aw.Message;
import se.sics.tasim.sim.SimulationAgent;
import tau.tac.adx.sim.Users;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.sim.AgentRepository;
import edu.umich.eecs.tac.sim.SalesAnalyst;
import edu.umich.eecs.tac.user.DefaultUsersBehavior;
import edu.umich.eecs.tac.user.UserEventListener;
import edu.umich.eecs.tac.user.UsersBehavior;
import edu.umich.eecs.tac.user.UsersTransactor;
import edu.umich.eecs.tac.util.config.ConfigProxy;

/**
 * @author Lee Callender, Patrick Jordan
 */
public class DefaultUsers extends Users {
	private final UsersBehavior usersBehavior;

	public DefaultUsers() {
		usersBehavior = new DefaultUsersBehavior(new UsersConfigProxy(),
				new AgentRepositoryProxy(), new UsersTransactorProxy());
	}

	@Override
	public void nextTimeUnit(int date) {
		usersBehavior.nextTimeUnit(date);
	}

	@Override
	protected void setup() {
		super.setup();

		this.log = Logger.getLogger(DefaultUsers.class.getName());

		usersBehavior.setup();
	}

	@Override
	protected void stopped() {
		usersBehavior.stopped();
	}

	@Override
	protected void shutdown() {
		usersBehavior.shutdown();
	}

	@Override
	protected void messageReceived(Message message) {
		usersBehavior.messageReceived(message);
	}

	@Override
	public boolean addUserEventListener(UserEventListener listener) {
		return usersBehavior.addUserEventListener(listener);
	}

	@Override
	public boolean containsUserEventListener(UserEventListener listener) {
		return usersBehavior.containsUserEventListener(listener);
	}

	@Override
	public boolean removeUserEventListener(UserEventListener listener) {
		return usersBehavior.removeUserEventListener(listener);
	}

	@Override
	public void broadcastUserDistribution() {
		usersBehavior.broadcastUserDistribution(getIndex(), getEventWriter());
	}

	protected class UsersConfigProxy implements ConfigProxy {
		@Override
		public String getProperty(String name) {
			return DefaultUsers.this.getProperty(name);
		}

		@Override
		public String getProperty(String name, String defaultValue) {
			return DefaultUsers.this.getProperty(name, defaultValue);
		}

		@Override
		public String[] getPropertyAsArray(String name) {
			return DefaultUsers.this.getPropertyAsArray(name);
		}

		@Override
		public String[] getPropertyAsArray(String name, String defaultValue) {
			return DefaultUsers.this.getPropertyAsArray(name, defaultValue);
		}

		@Override
		public int getPropertyAsInt(String name, int defaultValue) {
			return DefaultUsers.this.getPropertyAsInt(name, defaultValue);
		}

		@Override
		public int[] getPropertyAsIntArray(String name) {
			return DefaultUsers.this.getPropertyAsIntArray(name);
		}

		@Override
		public int[] getPropertyAsIntArray(String name, String defaultValue) {
			return DefaultUsers.this.getPropertyAsIntArray(name, defaultValue);
		}

		@Override
		public long getPropertyAsLong(String name, long defaultValue) {
			return DefaultUsers.this.getPropertyAsLong(name, defaultValue);
		}

		@Override
		public float getPropertyAsFloat(String name, float defaultValue) {
			return DefaultUsers.this.getPropertyAsFloat(name, defaultValue);
		}

		@Override
		public double getPropertyAsDouble(String name, double defaultValue) {
			return DefaultUsers.this.getPropertyAsDouble(name, defaultValue);
		}
	}

	protected class UsersTransactorProxy implements UsersTransactor {
		@Override
		public void transact(String address, double amount) {
			DefaultUsers.this.transact(address, amount);
		}
	}

	protected class AgentRepositoryProxy implements AgentRepository {

		@Override
		public Map<String, AdvertiserInfo> getAdvertiserInfo() {
			return getSimulation().getAdvertiserInfo();
		}

		@Override
		public SimulationAgent[] getPublishers() {
			return getSimulation().getPublishers();
		}

		@Override
		public SalesAnalyst getSalesAnalyst() {
			return null;
		}

		@Override
		public int getNumberOfAdvertisers() {
			return getSimulation().getNumberOfAdvertisers();
		}

		@Override
		public String[] getAdvertiserAddresses() {
			return getSimulation().getAdvertiserAddresses();
		}
	}
}
