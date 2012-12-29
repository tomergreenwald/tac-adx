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
import se.sics.tasim.sim.SimulationAgent;
import tau.tac.adx.auction.AdxPublisherReportManager;
import tau.tac.adx.auction.AdxPublisherReportManagerImpl;
import tau.tac.adx.sim.AdxAgentRepository;
import tau.tac.adx.sim.AdxPublisherReportSender;
import tau.tac.adx.sim.AdxUsers;
import tau.tac.adx.sim.Publisher;
import tau.tac.adx.users.AdxUserBehaviorBuilder;
import tau.tac.adx.users.AdxUserEventListener;
import tau.tac.adx.users.AdxUserManager;
import tau.tac.adx.users.AdxUsersBehavior;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.Ranking;
import edu.umich.eecs.tac.sim.AgentRepository;
import edu.umich.eecs.tac.sim.Auctioneer;
import edu.umich.eecs.tac.user.DistributionBroadcaster;
import edu.umich.eecs.tac.user.UserBehaviorBuilder;
import edu.umich.eecs.tac.user.UserManager;
import edu.umich.eecs.tac.user.UsersBehavior;
import edu.umich.eecs.tac.util.config.ConfigProxy;
import edu.umich.eecs.tac.util.config.ConfigProxyUtils;

/**
 * {@link UsersBehavior} implementation.
 * 
 * @author Patrick Jordan, Lee Callender
 */
public class DefaultAdxUsersBehavior implements AdxUsersBehavior {

	/**
	 * {@link UserManager}.
	 */
	private AdxUserManager userManager;

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
	private final AdxAgentRepository agentRepository;

	/**
	 * {@link AdxPublisherReportManager}.
	 */
	AdxPublisherReportManager publisherReportManager;

	/**
	 * {@link AdxPublisherReportSender}.
	 */
	AdxPublisherReportSender publisherReportSender;

	/**
	 * @param config
	 *            {@link ConfigProxy} used to configure an instance.
	 * @param agentRepository
	 *            {@link AdxAgentRepository} used to query and access data about
	 *            {@link Agent} s.
	 * @param publisherReportSender
	 *            {@link AdxPublisherReportSender}.
	 */
	public DefaultAdxUsersBehavior(ConfigProxy config,
			AdxAgentRepository agentRepository,
			AdxPublisherReportSender publisherReportSender) {

		if (config == null) {
			throw new NullPointerException("config cannot be null");
		}

		this.config = config;

		if (agentRepository == null) {
			throw new NullPointerException("agent repository cannot be null");
		}

		this.agentRepository = agentRepository;

		if (publisherReportSender == null) {
			throw new NullPointerException(
					"Publisher report sender cannot be null");
		}

		this.publisherReportSender = publisherReportSender;
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
			AdxUserBehaviorBuilder<AdxUserManager> managerBuilder = createBuilder();

			userManager = managerBuilder.build(config, agentRepository,
					new Random());

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		publisherReportManager = createQueryReportManager();
	}

	private AdxPublisherReportManager createQueryReportManager() {
		AdxPublisherReportManager queryReportManager = new AdxPublisherReportManagerImpl(
				publisherReportSender);

		for (SimulationAgent agent : agentRepository.getAdxUsers()) {
			AdxUsers users = (AdxUsers) agent.getAgent();
			users.addUserEventListener(queryReportManager);
		}

		return queryReportManager;
	}

	/**
	 * Generates a {@link UserBehaviorBuilder}.
	 * 
	 * @return A {@link UserBehaviorBuilder}.
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected AdxUserBehaviorBuilder<AdxUserManager> createBuilder()
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		return ConfigProxyUtils.createObjectFromProperty(config,
				"adxusermanger.builder",
				"tau.tac.adx.users.DefaultAdxUserManagerBuilder");
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
	public boolean addUserEventListener(AdxUserEventListener listener) {
		return userManager.addUserEventListener(listener);
	}

	/**
	 * @see edu.umich.eecs.tac.user.UsersBehavior#containsUserEventListener(edu.umich.eecs.tac.user.UserEventListener)
	 */
	@Override
	public boolean containsUserEventListener(AdxUserEventListener listener) {
		return userManager.containsUserEventListener(listener);
	}

	/**
	 * @see edu.umich.eecs.tac.user.UsersBehavior#removeUserEventListener(edu.umich.eecs.tac.user.UserEventListener)
	 */
	@Override
	public boolean removeUserEventListener(AdxUserEventListener listener) {
		return userManager.removeUserEventListener(listener);
	}

	/**
	 * @see tau.tac.adx.users.AdxUsersBehavior#sendQueryReportsToAll()
	 */
	@Override
	public void sendQueryReportsToAll() {
		publisherReportManager.sendQueryReportToAll();
	}

}
