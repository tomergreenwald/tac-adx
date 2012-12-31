/*
 * DefaultUserViewManager.java
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
package tau.tac.adx.users;

import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalog;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.SlotInfo;
import edu.umich.eecs.tac.sim.RecentConversionsTracker;

/**
 * @author Patrick Jordan, Ben Cassell, Lee Callender
 */
public class DefaultAdxUserViewManager implements AdxUserViewManager {
	private final Logger log = Logger.getLogger(DefaultAdxUserViewManager.class
			.getName());

	private final AdxUserEventSupport eventSupport;

	private final Map<String, AdvertiserInfo> advertiserInfo;

	private final SlotInfo slotInfo;

	private final PublisherCatalog publisherCatalog;

	private final Random random;

	private final RecentConversionsTracker recentConversionsTracker;

	public DefaultAdxUserViewManager(PublisherCatalog publisherCatalog,
			RecentConversionsTracker recentConversionsTracker,
			Map<String, AdvertiserInfo> advertiserInfo, SlotInfo slotInfo) {
		this(publisherCatalog, recentConversionsTracker, advertiserInfo,
				slotInfo, new Random());
	}

	public DefaultAdxUserViewManager(PublisherCatalog publisherCatalog,
			RecentConversionsTracker recentConversionsTracker,
			Map<String, AdvertiserInfo> advertiserInfo, SlotInfo slotInfo,
			Random random) {
		if (publisherCatalog == null) {
			throw new NullPointerException("Publisher catalog cannot be null");
		}

		if (slotInfo == null) {
			throw new NullPointerException("Auction info cannot be null");
		}

		if (recentConversionsTracker == null) {
			throw new NullPointerException(
					"Recent conversions tracker cannot be null");
		}

		if (advertiserInfo == null) {
			throw new NullPointerException(
					"Advertiser information cannot be null");
		}

		if (random == null) {
			throw new NullPointerException("Random generator cannot be null");
		}

		this.publisherCatalog = publisherCatalog;
		this.random = random;
		this.recentConversionsTracker = recentConversionsTracker;
		this.advertiserInfo = advertiserInfo;
		this.slotInfo = slotInfo;
		eventSupport = new AdxUserEventSupport();
	}

	@Override
	public void nextTimeUnit(int timeUnit) {

	}

	/**
	 * @see tau.tac.adx.users.AdxUserViewManager#processImpression(tau.tac.adx.users.AdxUser,
	 *      tau.tac.adx.props.AdxQuery, tau.tac.adx.auction.AdxAuctionResult)
	 */
	@Override
	public void processImpression(AdxUser user, AdxQuery query,
			AdxAuctionResult auctionResult) {
		fireQueryIssued(query);

		AdxAuctionResult adxAuctionResult = auctionResult;
		fireAuctionPerformed(adxAuctionResult, query, user);
	}

	@Override
	public boolean addUserEventListener(AdxUserEventListener listener) {
		return eventSupport.addUserEventListener(listener);
	}

	@Override
	public boolean containsUserEventListener(AdxUserEventListener listener) {
		return eventSupport.containsUserEventListener(listener);
	}

	@Override
	public boolean removeUserEventListener(AdxUserEventListener listener) {
		return eventSupport.removeUserEventListener(listener);
	}

	private void fireQueryIssued(AdxQuery query) {
		eventSupport.fireQueryIssued(query);
	}

	/**
	 * Auction was performed by the <b>ADX</b> and results are given as
	 * parameters.
	 * 
	 * @param auctionResult
	 *            {@link AdxAuctionResult}.
	 * @param query
	 *            Issuing {@link AdxQuery}.
	 * @param user
	 *            Participating {@link AdxUser}.
	 */
	private void fireAuctionPerformed(AdxAuctionResult auctionResult,
			AdxQuery query, AdxUser user) {
		eventSupport.fireAuctionPerformed(auctionResult, query, user);
	}

}
