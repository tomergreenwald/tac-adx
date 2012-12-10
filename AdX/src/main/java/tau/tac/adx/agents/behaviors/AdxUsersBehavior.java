/*
 * DefaultUsersBehavior.java
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
package tau.tac.adx.agents.behaviors;

import java.util.Random;

import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.Message;
import se.sics.tasim.is.EventWriter;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.Ranking;
import edu.umich.eecs.tac.sim.AgentRepository;
import edu.umich.eecs.tac.sim.Auctioneer;
import edu.umich.eecs.tac.sim.Publisher;
import edu.umich.eecs.tac.user.DefaultDistributionBroadcaster;
import edu.umich.eecs.tac.user.DistributionBroadcaster;
import edu.umich.eecs.tac.user.UserBehaviorBuilder;
import edu.umich.eecs.tac.user.UserEventListener;
import edu.umich.eecs.tac.user.UserManager;
import edu.umich.eecs.tac.user.UsersBehavior;
import edu.umich.eecs.tac.user.UsersTransactor;
import edu.umich.eecs.tac.util.config.ConfigProxy;
import edu.umich.eecs.tac.util.config.ConfigProxyUtils;

/**
 * {@link UsersBehavior} implementation.
 * 
 * @author Patrick Jordan, Lee Callender
 */
public class AdxUsersBehavior implements UsersBehavior {

	/**
	 * {@link UserManager}.
	 */
	private UserManager userManager;

	/**
	 * {@link DistributionBroadcaster}.
	 */
	private DistributionBroadcaster distributionBroadcaster;

	/**
	 * Amount of virtual days.
	 */
	private int virtualDays;

	/**
	 * {@link ConfigProxy} used to configure an instance.
	 */
	private final ConfigProxy config;

	/**
	 * {@link AgentRepository} used to query and access data about {@link Agent}
	 * s.
	 */
	private final AgentRepository agentRepository;

	/**
	 * {@link UsersTransactor}.
	 */
	final UsersTransactor usersTransactor;

	/**
	 * @param config
	 *            {@link ConfigProxy} used to configure an instance.
	 * @param agentRepository
	 *            {@link AgentRepository} used to query and access data about
	 *            {@link Agent} s.
	 * @param usersTransactor
	 *            {@link UsersTransactor}.
	 */
	public AdxUsersBehavior(ConfigProxy config,
			AgentRepository agentRepository, UsersTransactor usersTransactor) {

		if (config == null) {
			throw new NullPointerException("config cannot be null");
		}

		this.config = config;

		if (agentRepository == null) {
			throw new NullPointerException("agent repository cannot be null");
		}

		this.agentRepository = agentRepository;

		if (usersTransactor == null) {
			throw new NullPointerException("users transactor cannot be null");
		}

		this.usersTransactor = usersTransactor;
	}

	/**
	 * @see edu.umich.eecs.tac.user.UsersBehavior#nextTimeUnit(int)
	 */
	@Override
	public void nextTimeUnit(int date) {

		if (date == 0) {
			userManager.initialize(virtualDays);
		}

		userManager.nextTimeUnit(date);

		userManager
				.triggerBehavior((Publisher) agentRepository.getPublishers()[0]
						.getAgent());
	}

	/**
	 * @see edu.umich.eecs.tac.user.UsersBehavior#setup()
	 */
	@Override
	public void setup() {
		virtualDays = config.getPropertyAsInt("virtual_days", 0);

		try {
			// Create the user manager
			UserBehaviorBuilder<UserManager> managerBuilder = createBuilder();

			userManager = managerBuilder.build(config, agentRepository,
					new Random());

			addUserEventListener(new ConversionMonitor());

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @author greenwald
	 * 
	 */
	protected class ConversionMonitor implements UserEventListener {

		/**
		 * @see edu.umich.eecs.tac.user.UserEventListener#queryIssued(edu.umich.eecs.tac.props.Query)
		 */
		@Override
		public void queryIssued(Query query) {
			// no implementation.
		}

		/**
		 * @see edu.umich.eecs.tac.user.UserEventListener#viewed(edu.umich.eecs.tac.props.Query,
		 *      edu.umich.eecs.tac.props.Ad, int, java.lang.String, boolean)
		 */
		@Override
		public void viewed(Query query, Ad ad, int slot, String advertiser,
				boolean isPromoted) {
			// no implementation.
		}

		/**
		 * @see edu.umich.eecs.tac.user.UserEventListener#clicked(edu.umich.eecs.tac.props.Query,
		 *      edu.umich.eecs.tac.props.Ad, int, double, java.lang.String)
		 */
		@Override
		public void clicked(Query query, Ad ad, int slot, double cpc,
				String advertiser) {
			// no implementation.
		}

		/**
		 * @see edu.umich.eecs.tac.user.UserEventListener#converted(edu.umich.eecs.tac.props.Query,
		 *      edu.umich.eecs.tac.props.Ad, int, double, java.lang.String)
		 */
		@Override
		public void converted(Query query, Ad ad, int slot, double salesProfit,
				String advertiser) {
			usersTransactor.transact(advertiser, salesProfit);
		}
	}

	/**
	 * Generates a {@link UserBehaviorBuilder}.
	 * 
	 * @return A {@link UserBehaviorBuilder}.
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected UserBehaviorBuilder<UserManager> createBuilder()
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		return ConfigProxyUtils.createObjectFromProperty(config,
				"usermanger.builder",
				"edu.umich.eecs.tac.user.DefaultUserManagerBuilder");
	}

	/**
	 * @see edu.umich.eecs.tac.user.UsersBehavior#stopped()
	 */
	@Override
	public void stopped() {
		// no implementation.
	}

	/**
	 * @see edu.umich.eecs.tac.user.UsersBehavior#shutdown()
	 */
	@Override
	public void shutdown() {
		// no implementation.
	}

	/**
	 * @see edu.umich.eecs.tac.user.UsersBehavior#getRanking(edu.umich.eecs.tac.props.Query,
	 *      edu.umich.eecs.tac.sim.Auctioneer)
	 */
	@Override
	public Ranking getRanking(Query query, Auctioneer auctioneer) {
		return auctioneer.runAuction(query).getRanking();
	}

	/**
	 * @see edu.umich.eecs.tac.user.UsersBehavior#messageReceived(se.sics.tasim.aw.Message)
	 */
	@Override
	public void messageReceived(Message message) {
		userManager.messageReceived(message);
	}

	/**
	 * @see edu.umich.eecs.tac.user.UsersBehavior#addUserEventListener(edu.umich.eecs.tac.user.UserEventListener)
	 */
	@Override
	public boolean addUserEventListener(UserEventListener listener) {
		return userManager.addUserEventListener(listener);
	}

	/**
	 * @see edu.umich.eecs.tac.user.UsersBehavior#containsUserEventListener(edu.umich.eecs.tac.user.UserEventListener)
	 */
	@Override
	public boolean containsUserEventListener(UserEventListener listener) {
		return userManager.containsUserEventListener(listener);
	}

	/**
	 * @see edu.umich.eecs.tac.user.UsersBehavior#removeUserEventListener(edu.umich.eecs.tac.user.UserEventListener)
	 */
	@Override
	public boolean removeUserEventListener(UserEventListener listener) {
		return userManager.removeUserEventListener(listener);
	}

	/**
	 * @see edu.umich.eecs.tac.user.DistributionBroadcaster#broadcastUserDistribution(int,
	 *      se.sics.tasim.is.EventWriter)
	 */
	@Override
	public void broadcastUserDistribution(int usersIndex,
			EventWriter eventWriter) {

		if (distributionBroadcaster == null) {
			distributionBroadcaster = new DefaultDistributionBroadcaster(
					userManager);
		}

		distributionBroadcaster.broadcastUserDistribution(usersIndex,
				eventWriter);
	}
}
