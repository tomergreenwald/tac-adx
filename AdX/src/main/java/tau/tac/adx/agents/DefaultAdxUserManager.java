/*
 * DefaultUserManager.java
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

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import se.sics.tasim.aw.Message;
import tau.tac.adx.Adx;
import tau.tac.adx.auction.AuctionResult;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.props.TacQuery;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.AdxUserEventListener;
import tau.tac.adx.users.AdxUserManager;
import tau.tac.adx.users.AdxUserViewManager;
import tau.tac.adx.users.TacUser;
import tau.tac.adx.users.UserQueryManager;
import tau.tac.adx.users.generators.SimpleUserGenerator;
import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.sim.Auctioneer;
import edu.umich.eecs.tac.user.QueryState;
import edu.umich.eecs.tac.user.User;
import edu.umich.eecs.tac.user.UserTransitionManager;

/**
 * @author Patrick Jordan, Ben Cassell, Lee Callender
 */
public class DefaultAdxUserManager implements AdxUserManager {
	protected Logger log = Logger.getLogger(DefaultAdxUserManager.class
			.getName());

	private final Object lock;

	private final List<AdxUser> users;

	private final Random random;

	private final PublisherCatalog publisherCatalog;

	private final UserQueryManager<Adx> queryManager;

	private final AdxUserViewManager viewManager;

	public DefaultAdxUserManager(PublisherCatalog publisherCatalog,
			UserTransitionManager transitionManager,
			UserQueryManager queryManager, AdxUserViewManager viewManager,
			int populationSize) {
		this(publisherCatalog, queryManager, viewManager, populationSize,
				new Random());
	}

	public DefaultAdxUserManager(PublisherCatalog publisherCatalog,
			UserQueryManager queryManager, AdxUserViewManager viewManager,
			int populationSize, Random random) {
		lock = new Object();

		if (publisherCatalog == null) {
			throw new NullPointerException("Publisher catalog cannot be null");
		}

		if (queryManager == null) {
			throw new NullPointerException("User query manager cannot be null");
		}

		if (viewManager == null) {
			throw new NullPointerException("User view manager cannot be null");
		}

		if (populationSize < 0) {
			throw new IllegalArgumentException(
					"Population size cannot be negative");
		}

		if (random == null) {
			throw new NullPointerException(
					"Random number generator cannot be null");
		}

		this.publisherCatalog = publisherCatalog;
		this.random = random;
		this.queryManager = queryManager;
		this.viewManager = viewManager;
		SimpleUserGenerator generator = new SimpleUserGenerator();
		users = generator.generate(populationSize);
	}

	@Override
	public void initialize(int virtualDays) {
	}

	@Override
	public void triggerBehavior(Auctioneer auctioneer) {

		synchronized (lock) {
			log.finest("START OF USER TRIGGER");

			Collections.shuffle(users, random);

			for (AdxUser user : users) {
				handleUserActivity(user);
			}

			log.finest("FINISH OF USER TRIGGER");
		}

	}

	/**
	 * Activates a user for at least one time, with a probability of
	 * {@link AdxUser#getpContinue()} for continuing browsing websites (
	 * {@link AdxPublisher}) each time after that.
	 * 
	 * @param user
	 */
	private void handleUserActivity(AdxUser user) {
		do {
			handleSearch(user);
		} while (user.getpContinue() > random.nextDouble());
	}

	/**
	 * Activate user and generate queries and auctions for it. Causes the user
	 * to "browse" to a websites ({@link AdxPublisher}) and activate the
	 * Ad-Exchange auctioneer.
	 * 
	 * @param user
	 *            An {@link AdxUser} to generate a query for.
	 */
	private boolean handleSearch(AdxUser user) {

		boolean transacted = false;

		TacQuery<Adx> query = generateQuery(user);

		if (query != null) {
			// Auction auction = auctioneer.runAuction(query);

			// AuctionResult<Adx> auction;
			// transacted = handleImpression(query, auction, user);
		}

		return transacted;
	}

	private boolean handleImpression(TacQuery<Adx> query,
			AuctionResult<Adx> auctionResult, TacUser<Adx> user) {
		return viewManager.processImpression(user, query, auctionResult);
	}

	private TacQuery<Adx> generateQuery(AdxUser user) {
		return queryManager.generateQuery(user);
	}

	@Override
	public boolean addUserEventListener(AdxUserEventListener listener) {
		synchronized (lock) {
			return viewManager.addUserEventListener(listener);
		}
	}

	@Override
	public boolean containsUserEventListener(AdxUserEventListener listener) {
		synchronized (lock) {
			return viewManager.containsUserEventListener(listener);
		}
	}

	@Override
	public boolean removeUserEventListener(AdxUserEventListener listener) {
		synchronized (lock) {
			return viewManager.removeUserEventListener(listener);
		}
	}

	@Override
	public void nextTimeUnit(int timeUnit) {
		viewManager.nextTimeUnit(timeUnit);
		queryManager.nextTimeUnit(timeUnit);
	}

	@Override
	public int[] getStateDistribution() {
		int[] distribution = new int[QueryState.values().length];

		for (User user : users) {
			distribution[user.getState().ordinal()]++;
		}

		return distribution;
	}

	@Override
	public int[] getStateDistribution(Product product) {
		int[] distribution = new int[QueryState.values().length];

		for (User user : users) {
			if (user.getProduct() == product) {
				distribution[user.getState().ordinal()]++;
			}
		}

		return distribution;
	}

	@Override
	public void messageReceived(Message message) {
		// Transportable content = message.getContent();
	}

	@Override
	public PublisherCatalog getPublisherCatalog() {
		return publisherCatalog;
	}
}
