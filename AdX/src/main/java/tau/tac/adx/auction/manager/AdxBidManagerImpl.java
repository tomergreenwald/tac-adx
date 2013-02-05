/*
 * BidManagerImpl.java
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
package tau.tac.adx.auction.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import se.sics.tasim.aw.Message;
import tau.tac.adx.auction.tracker.AdxBidTracker;
import tau.tac.adx.auction.tracker.AdxSpendTracker;
import tau.tac.adx.bids.BidInfo;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.AdxQuery;

import com.google.inject.Inject;

/**
 * @author Patrick Jordan, Lee Callender
 */
public class AdxBidManagerImpl implements AdxBidManager {

	private final Logger log = Logger.getLogger(AdxBidManagerImpl.class
			.getName());

	private final Set<String> advertisers;

	private final Set<String> advertisersView;

	private final List<Message> bidBundleList;

	private final AdxBidTracker bidTracker;

	private final AdxSpendTracker spendTracker;

	@Inject
	public AdxBidManagerImpl(AdxBidTracker bidTracker,
			AdxSpendTracker spendTracker) {

		if (bidTracker == null)
			throw new NullPointerException("bid tracker cannot be null");

		this.bidTracker = bidTracker;

		if (spendTracker == null)
			throw new NullPointerException("spend tracker cannot be null");

		this.spendTracker = spendTracker;

		advertisers = new HashSet<String>();
		advertisersView = Collections.unmodifiableSet(advertisers);
		bidBundleList = new ArrayList<Message>();
	}

	/**
	 * NOTE: isOverspent will only function correctly in this instance if
	 * auctions are computed for EVERY query and not cached.
	 */
	@Override
	public BidInfo getBidInfo(String advertiser, AdxQuery query) {
		BidInfo bidInfo = bidTracker.getBidInfo(advertiser, query);

		if (bidInfo == null || isOverspent(bidInfo.getBid(), advertiser, query))
			return null;
		else
			return bidInfo;
	}

	@Override
	public void updateBids(String advertiser, AdxBidBundle bundle) {

		// Store all of the BidBundles until nextTimeUnit.
		// We'll call actualUpdateBids method there.
		Message m = new Message(advertiser, advertiser, bundle);

		bidBundleList.add(m);
	}

	@Override
	public Set<String> advertisers() {
		return advertisers;
	}

	@Override
	public void nextTimeUnit(int timeUnit) {
	}

	@Override
	public void applyBidUpdates() {
		for (Message m : bidBundleList) {
			bidTracker.updateBids(m.getSender(), (AdxBidBundle) m.getContent());
		}

		bidBundleList.clear();
	}

	@Override
	public void addAdvertiser(String advertiser) {
		advertisers.add(advertiser);
		bidTracker.addAdvertiser(advertiser);
		spendTracker.addAdvertiser(advertiser);
	}

	private boolean isOverspent(double bid, String advertiser, AdxQuery query) {
		return false;
		// TODO: uncomment me
		// return (bid >= bidTracker.getDailySpendLimit(advertiser, query)
		// - spendTracker.getDailyCost(advertiser, query))
		// || (bid >= bidTracker.getDailySpendLimit(advertiser)
		// - spendTracker.getDailyCost(advertiser));
	}
}
