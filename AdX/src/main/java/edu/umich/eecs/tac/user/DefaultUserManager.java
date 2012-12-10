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
package edu.umich.eecs.tac.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Message;
import edu.umich.eecs.tac.props.Auction;
import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.UserClickModel;
import edu.umich.eecs.tac.sim.Auctioneer;

/**
 * @author Patrick Jordan, Ben Cassell, Lee Callender
 */
public class DefaultUserManager implements UserManager {
	protected Logger log = Logger.getLogger(DefaultUserManager.class.getName());

	private final Object lock;

	private List<User> users;

	private Random random;

  private RetailCatalog retailCatalog;

  private UserQueryManager queryManager;

	private UserTransitionManager transitionManager;

	private UserViewManager viewManager;

	private UserClickModel userClickModel;

	private UsersInitializer usersInitializer;

	public DefaultUserManager(RetailCatalog retailCatalog,
			UserTransitionManager transitionManager,
			UserQueryManager queryManager, UserViewManager viewManager,
			int populationSize) {
		this(retailCatalog, transitionManager, queryManager, viewManager,
				populationSize, new Random());
	}

	public DefaultUserManager(RetailCatalog retailCatalog,
			UserTransitionManager transitionManager,
			UserQueryManager queryManager, UserViewManager viewManager,
			int populationSize, Random random) {
		lock = new Object();

		if (retailCatalog == null) {
			throw new NullPointerException("Retail catalog cannot be null");
		}

		if (transitionManager == null) {
			throw new NullPointerException(
					"User transition manager cannot be null");
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

		this.retailCatalog = retailCatalog;
    this.random = random;
		this.transitionManager = transitionManager;
		this.queryManager = queryManager;
		this.viewManager = viewManager;
		this.usersInitializer = new DefaultUsersInitializer(transitionManager);

		users = buildUsers(retailCatalog, populationSize);
	}

	private List<User> buildUsers(RetailCatalog catalog, int populationSize) {
		List<User> users = new ArrayList<User>();

		for (Product product : catalog) {
			for (int i = 0; i < populationSize; i++) {
				users.add(new User(QueryState.NON_SEARCHING, product));
			}
		}

		return users;
	}

	public void initialize(int virtualDays) {
		usersInitializer.initialize(users, virtualDays);
	}

	public void triggerBehavior(Auctioneer auctioneer) {

		synchronized (lock) {
			log.finest("START OF USER TRIGGER");

			Collections.shuffle(users, random);

			for (User user : users) {

				boolean transacted = handleSearch(user, auctioneer);

				handleTransition(user, transacted);
			}

			log.finest("FINISH OF USER TRIGGER");
		}

	}

	private boolean handleSearch(User user, Auctioneer auctioneer) {

		boolean transacted = false;

		Query query = generateQuery(user);

		if (query != null) {
			Auction auction = auctioneer.runAuction(query);

			transacted = handleImpression(query, auction, user);
		}

		return transacted;
	}

	private boolean handleImpression(Query query, Auction auction, User user) {
		return viewManager.processImpression(user, query, auction);
	}

	private void handleTransition(User user, boolean transacted) {
		user.setState(transitionManager.transition(user,transacted));
	}

	private Query generateQuery(User user) {
		return queryManager.generateQuery(user);
	}

	public boolean addUserEventListener(UserEventListener listener) {
		synchronized (lock) {
			return viewManager.addUserEventListener(listener);
		}
	}

	public boolean containsUserEventListener(UserEventListener listener) {
		synchronized (lock) {
			return viewManager.containsUserEventListener(listener);
		}
	}

	public boolean removeUserEventListener(UserEventListener listener) {
		synchronized (lock) {
			return viewManager.removeUserEventListener(listener);
		}
	}

	public void nextTimeUnit(int timeUnit) {
		viewManager.nextTimeUnit(timeUnit);
		queryManager.nextTimeUnit(timeUnit);
		transitionManager.nextTimeUnit(timeUnit);
	}

	public int[] getStateDistribution() {
		int[] distribution = new int[QueryState.values().length];

		for (User user : users) {
			distribution[user.getState().ordinal()]++;
		}

		return distribution;
	}

  public int[] getStateDistribution(Product product){
    int[] distribution = new int[QueryState.values().length];

		for (User user : users) {
      if(user.getProduct() == product){
        distribution[user.getState().ordinal()]++;
      }
    }

		return distribution;
  }

  public RetailCatalog getRetailCatalog(){
    return this.retailCatalog;
  }

  public UserClickModel getUserClickModel() {
		return userClickModel;
	}

	public void setUserClickModel(UserClickModel userClickModel) {
		this.userClickModel = userClickModel;
		viewManager.setUserClickModel(userClickModel);
	}

	public void messageReceived(Message message) {
		Transportable content = message.getContent();

		if (content instanceof UserClickModel) {
			setUserClickModel((UserClickModel) content);
		}
	}
}
