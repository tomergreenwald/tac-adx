/*
 * DefaultUserManagerTest.java
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;


import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.Auction;
import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.UserClickModel;
import edu.umich.eecs.tac.sim.Auctioneer;

/**
 * @author Patrick Jordan
 */
public class DefaultUserManagerTest {
	private DefaultUserManager userManager;

	private RetailCatalog retailCatalog;

	private UserTransitionManager transitionManager;

	private UserQueryManager queryManager;

	private UserViewManager viewManager;

	private int populationSize;

	private Random random;

	private Product product;

	@Before
	public void setup() {
		product = new Product("man", "com");

		retailCatalog = new RetailCatalog();
		retailCatalog.addProduct(product);

		transitionManager = new SimpleUserTransitionManager();
		queryManager = new SimpleUserQueryManager();
		viewManager = new SimpleUserViewManager();
		populationSize = 2;
		random = new Random();

		userManager = new DefaultUserManager(retailCatalog, transitionManager,
				queryManager, viewManager, populationSize, random);
	}

	@Test
	public void testConstuctor() {
		assertNotNull(userManager);
		assertNotNull(new DefaultUserManager(retailCatalog, transitionManager,
				queryManager, viewManager, populationSize));
	}

	@Test(expected = NullPointerException.class)
	public void testConstuctorRetailCatalogNull() {
		new DefaultUserManager(null, transitionManager, queryManager,
				viewManager, populationSize);
	}

	@Test(expected = NullPointerException.class)
	public void testConstuctorTransitionManagerNull() {
		new DefaultUserManager(retailCatalog, null, queryManager, viewManager,
				populationSize);
	}

	@Test(expected = NullPointerException.class)
	public void testConstuctorQueryManagerNull() {
		new DefaultUserManager(retailCatalog, transitionManager, null,
				viewManager, populationSize);
	}

	@Test(expected = NullPointerException.class)
	public void testConstuctorViewManagerNull() {
		new DefaultUserManager(retailCatalog, transitionManager, queryManager,
				null, populationSize);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstuctorNegativePopulationSize() {
		new DefaultUserManager(retailCatalog, transitionManager, queryManager,
				viewManager, -1);
	}

	@Test(expected = NullPointerException.class)
	public void testConstuctorRandomNull() {
		new DefaultUserManager(retailCatalog, transitionManager, queryManager,
				viewManager, populationSize, null);
	}

	@Test
	public void testTriggerBehavior() {
		userManager.nextTimeUnit(0);

		Auctioneer auctioneer = new SimpleAuctioneer();

		userManager.triggerBehavior(auctioneer);
	}

	@Test
	public void testListener() {
		UserEventListener listener = new SimpleUserEventListener();

		userManager.addUserEventListener(listener);
		userManager.containsUserEventListener(listener);
		userManager.removeUserEventListener(listener);
	}

	@Test
	public void testClickModel() {
		UserClickModel userClickModel = new UserClickModel();
		userManager.setUserClickModel(userClickModel);
		assertSame(userClickModel, userManager.getUserClickModel());
	}

	@Test
	public void testStateDistribution() {
		int[] distribution = userManager.getStateDistribution();

		assertNotNull(distribution);
		assertEquals(distribution[0], populationSize, 0);
		for (int i = 1; i < distribution.length; i++) {
			assertEquals(distribution[i], 0, 0);
		}
	}

	public class SimpleUserTransitionManager implements UserTransitionManager {

		public QueryState transition(User user, boolean transacted) {
			return QueryState.NON_SEARCHING;
		}

		public void nextTimeUnit(int timeUnit) {

		}
	}

	public class SimpleUserQueryManager implements UserQueryManager {
		boolean flag = false;

		public Query generateQuery(User user) {
			flag ^= true;

			return flag ? null : new Query();
		}

		public void nextTimeUnit(int timeUnit) {

		}
	}

	public class SimpleUserViewManager implements UserViewManager {

		public boolean processImpression(User user, Query query, Auction auction) {
			return false;
		}

		public boolean addUserEventListener(UserEventListener listener) {
			return true;
		}

		public boolean containsUserEventListener(UserEventListener listener) {
			return true;
		}

		public boolean removeUserEventListener(UserEventListener listener) {
			return true;
		}

		public UserClickModel getUserClickModel() {
			return null;
		}

		public void setUserClickModel(UserClickModel userClickModel) {
		}

		public void nextTimeUnit(int timeUnit) {
		}
	}

	public class SimpleAuctioneer implements Auctioneer {

		public Auction runAuction(Query query) {
			return null;
		}
	}

	public class SimpleUserEventListener implements UserEventListener {

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

		}
	}
}
