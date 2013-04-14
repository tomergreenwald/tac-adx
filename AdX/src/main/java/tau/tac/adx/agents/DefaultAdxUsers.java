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

import java.util.logging.Logger;

import se.sics.tasim.aw.Message;
import tau.tac.adx.AdxManager;
import tau.tac.adx.agents.behaviors.DefaultAdxUsersBehavior;
import tau.tac.adx.auction.AdxBidBundleWriter;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.sim.AdxUsers;
import tau.tac.adx.sim.TACAdxConstants;
import tau.tac.adx.users.AdxUsersBehavior;
import edu.umich.eecs.tac.user.UsersBehavior;
import edu.umich.eecs.tac.util.config.ConfigProxy;

/**
 * @author greenwald
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
				AdxManager.getInstance().getSimulation(), this, this,
				new BidBundleWriterProxy());
	}

	@Override
	public void nextTimeUnit(int date) {
		usersBehavior.nextTimeUnit(date);
		if (date > 0) {
			sendReportsToAll();
		}
	}

	public void preNextTimeUnit(int date) {
	}

	/**
	 * @see edu.umich.eecs.tac.sim.Users#setup()
	 */
	@Override
	protected void setup() {
		super.setup();

		this.log = Logger.getLogger(DefaultAdxUsers.class.getName());
		AdxManager.getInstance().setAdxAgentAddress(getAddress());
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
	 * @see tau.tac.adx.sim.AdxUsers#sendReportsToAll()
	 */
	@Override
	public void sendReportsToAll() {
		usersBehavior.sendReportsToAll();
	}

	protected class BidBundleWriterProxy implements AdxBidBundleWriter {
		@Override
		public void writeBundle(String advertiser, AdxBidBundle bundle) {

			int agentIndex = getSimulation().agentIndex(advertiser);

			getEventWriter().dataUpdated(agentIndex,
					TACAdxConstants.DU_ADX_BIDS, bundle);
		}
	}
}
