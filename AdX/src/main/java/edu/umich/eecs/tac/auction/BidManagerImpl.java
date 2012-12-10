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
package edu.umich.eecs.tac.auction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import se.sics.tasim.aw.Message;
import tau.tac.adx.props.AdLink;
import edu.umich.eecs.tac.props.BidBundle;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.UserClickModel;

/**
 * @author Patrick Jordan, Lee Callender
 */
public class BidManagerImpl implements BidManager {

	private Logger log = Logger.getLogger(BidManagerImpl.class.getName());

	private Set<String> advertisers;

	private Set<String> advertisersView;

	private List<Message> bidBundleList;

	private UserClickModel userClickModel;

	private BidTracker bidTracker;

	private SpendTracker spendTracker;

	public BidManagerImpl(UserClickModel userClickModel, BidTracker bidTracker,
			SpendTracker spendTracker) {
		if (userClickModel == null)
			throw new NullPointerException("user click model cannot be null");

		this.userClickModel = userClickModel;

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
	public double getBid(String advertiser, Query query) {
		double bid = bidTracker.getBid(advertiser, query);

		if (isOverspent(bid, advertiser, query))
			return 0.0;
		else
			return bid;
	}

	public double getQualityScore(String advertiser, Query query) {
		int advertiserIndex = userClickModel.advertiserIndex(advertiser);
		int queryIndex = userClickModel.queryIndex(query);

		if (advertiserIndex < 0 || queryIndex < 0)
			return 1.0;
		else
			return userClickModel.getAdvertiserEffect(queryIndex,
					advertiserIndex);
	}

	public AdLink getAdLink(String advertiser, Query query) {
		return bidTracker.getAdLink(advertiser, query);
	}

	public void updateBids(String advertiser, BidBundle bundle) {

		// Store all of the BidBundles until nextTimeUnit.
		// We'll call actualUpdateBids method there.
		Message m = new Message(advertiser, advertiser, bundle);

		bidBundleList.add(m);
	}

	public Set<String> advertisers() {
		return advertisersView;
	}

    public void nextTimeUnit(int timeUnit) {        
    }

    public void applyBidUpdates() {
        for (Message m : bidBundleList) {
			bidTracker.updateBids(m.getSender(), (BidBundle) m.getContent());
		}

		bidBundleList.clear();
	}

	public void addAdvertiser(String advertiser) {
		advertisers.add(advertiser);
		bidTracker.addAdvertiser(advertiser);
		spendTracker.addAdvertiser(advertiser);
	}

	private boolean isOverspent(double bid, String advertiser, Query query) {
		return (bid >= bidTracker.getDailySpendLimit(advertiser, query)
				- spendTracker.getDailyCost(advertiser, query))
				|| (bid >= bidTracker.getDailySpendLimit(advertiser)
						- spendTracker.getDailyCost(advertiser));
	}
}
