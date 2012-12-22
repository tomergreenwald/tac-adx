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
package edu.umich.eecs.tac.user;

import java.util.Random;

import se.sics.tasim.aw.Message;
import se.sics.tasim.is.EventWriter;
import tau.tac.adx.sim.Publisher;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.Ranking;
import edu.umich.eecs.tac.sim.AgentRepository;
import edu.umich.eecs.tac.sim.Auctioneer;
import edu.umich.eecs.tac.util.config.ConfigProxy;
import edu.umich.eecs.tac.util.config.ConfigProxyUtils;

/**
 * @author Patrick Jordan, Lee Callender
 */
public class DefaultUsersBehavior implements UsersBehavior {

	private UserManager userManager;

	private DistributionBroadcaster distributionBroadcaster;

	private int virtualDays;

	private ConfigProxy config;

	private AgentRepository agentRepository;

	private UsersTransactor usersTransactor;

	public DefaultUsersBehavior(ConfigProxy config,
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

	public void nextTimeUnit(int date) {

		if (date == 0) {
			userManager.initialize(virtualDays);
		}

		userManager.nextTimeUnit(date);

		userManager
				.triggerBehavior((Publisher) agentRepository.getPublishers()[0]
						.getAgent());
	}

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

	protected class ConversionMonitor implements UserEventListener {

		public void queryIssued(Query query) {
		}

		public void viewed(Query query, Ad ad, int slot, String advertiser,
				boolean isPromoted) {
		}

		public void clicked(Query query, Ad ad, int slot, double cpc,
				String advertiser) {
		}

		public void converted(Query query, Ad ad, int slot, double salesProfit,
				String advertiser) {
			usersTransactor.transact(advertiser, salesProfit);
		}
	}

	protected UserBehaviorBuilder<UserManager> createBuilder()
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		return ConfigProxyUtils.createObjectFromProperty(config,
				"usermanger.builder",
				"edu.umich.eecs.tac.user.DefaultUserManagerBuilder");
	}

	public void stopped() {
	}

	public void shutdown() {
	}

	public Ranking getRanking(Query query, Auctioneer auctioneer) {
		return auctioneer.runAuction(query).getRanking();
	}

	public void messageReceived(Message message) {
		userManager.messageReceived(message);
	}

	public boolean addUserEventListener(UserEventListener listener) {
		return userManager.addUserEventListener(listener);
	}

	public boolean containsUserEventListener(UserEventListener listener) {
		return userManager.containsUserEventListener(listener);
	}

	public boolean removeUserEventListener(UserEventListener listener) {
		return userManager.removeUserEventListener(listener);
	}

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
