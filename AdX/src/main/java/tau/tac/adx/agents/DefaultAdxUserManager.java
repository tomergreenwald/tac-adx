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
import tau.tac.adx.AdxManager;
import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.messages.AuctionMessage;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.sim.AdxAuctioneer;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.AdxUserManager;
import tau.tac.adx.users.AdxUserQueryManager;

import com.google.common.eventbus.EventBus;

/**
 * @author greenwald
 */
public class DefaultAdxUserManager implements AdxUserManager {
	private Logger log = Logger.getLogger(DefaultAdxUserManager.class
			.getName());
	
	//nContimueMax
	private static int MAX_USER_DAILY_IMPRESSION = 6;

	private final Object lock;

	private final List<AdxUser> users;

	private final Random random;

	private final PublisherCatalog publisherCatalog;

	private final AdxUserQueryManager queryManager;
	
	public DefaultAdxUserManager(PublisherCatalog publisherCatalog,
			List<AdxUser> users, AdxUserQueryManager queryManager,
			int populationSize, EventBus eventBus) {
		lock = new Object();

		if (publisherCatalog == null) {
			throw new NullPointerException("Publisher catalog cannot be null");
		}

		if (users == null) {
			throw new NullPointerException("User list cannot be null");
		}

		if (queryManager == null) {
			throw new NullPointerException("User query manager cannot be null");
		}

		if (populationSize < 0) {
			throw new IllegalArgumentException(
					"Population size cannot be negative");
		}

		if (eventBus == null) {
			throw new NullPointerException("Event bus cannot be null");
		}

		this.publisherCatalog = publisherCatalog;
		this.random = new Random();
		this.queryManager = queryManager;
		this.users = users;
	}

	@Override
	public void initialize(@SuppressWarnings("unused") int virtualDays) {
		// Left blank intentionally
	}

	@Override
	public void triggerBehavior(AdxAuctioneer auctioneer) {

		synchronized (lock) {
			log.finest("START OF USER TRIGGER");

			Collections.shuffle(users, random);

			for(AdxPublisher publisher : AdxManager.getInstance().getPublishers()) {
				log.fine("Updating reserve prices for " + publisher.getName());
				publisher.getReservePriceManager().updateDailyBaselineAverage();
			}
			
			
			long pre;
			long post;
			
			//FIXME: remove debugging code
			log.fine("##################################### S-Invoking users activity");
			pre = System.currentTimeMillis();
			for (AdxUser user : users) {
                handleUserActivity(user, auctioneer);
			}
			post = System.currentTimeMillis();
			log.fine("##################################### E-Invoking users activity - total time in millis: " +(post - pre));
			// update publishers' reserve price
			log.finest("FINISH OF USER TRIGGER");
		}

	}
	
	/**
	 * Activates a user for at least one time, with a probability of
	 * {@link AdxUser#getpContinue()} for continuing browsing websites (
	 * {@link AdxPublisher}) each time after that.
	 * 
	 * @param user
	 * @param auctioneer
	 */
	protected void handleUserActivity(AdxUser user, AdxAuctioneer auctioneer) {
		int times = 0;
		do {
			times++;
			handleSearch(user, auctioneer);
		} while (user.getpContinue() > random.nextDouble() && times < MAX_USER_DAILY_IMPRESSION);
	}

	/**
	 * Activate user and generate queries and auctions for it. Causes the user
	 * to "browse" to a websites ({@link AdxPublisher}) and activate the
	 * Ad-Exchange auctioneer.
	 * 
	 * @param user
	 *            An {@link AdxUser} to generate a query for.
	 * @param auctioneer
	 */
	protected void handleSearch(AdxUser user, AdxAuctioneer auctioneer) {

		AdxQuery query = generateQuery(user);

		if (query != null) {
			AdxAuctionResult auctionResult = auctioneer.runAuction(query);
			AdxManager.getInstance().getSimulation().getEventBus().post(new AuctionMessage(auctionResult, query, user));
		} else {
			log.severe("Could not generate query for user - " + user);
		}
	}

	private AdxQuery generateQuery(AdxUser user) {
		return queryManager.generateQuery(user);
	}

	@Override
	public void nextTimeUnit(int timeUnit) {
		queryManager.nextTimeUnit(timeUnit);
	}

	@Override
	public void messageReceived(@SuppressWarnings("unused") Message message) {
		// Transportable content = message.getContent();
	}

	@Override
	public PublisherCatalog getPublisherCatalog() {
		return publisherCatalog;
	}
}
