/*
 * BidTracker.java
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
package tau.tac.adx.auction.tracker;

import java.util.Set;

import tau.tac.adx.bids.BidInfo;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.AdxQuery;

/**
 * @author greenwald
 * @author Patrick Jordan
 */
public interface AdxBidTracker {
	/**
	 * Add the advertiser
	 * 
	 * @param advertiser
	 *            the advertiser
	 */
	void addAdvertiser(String advertiser);

	/**
	 * Sets the query space for the bids
	 * 
	 * @param space
	 *            the query space
	 */
	void initializeQuerySpace(Set<AdxQuery> space);

	/**
	 * Get the daily spend limit for the advertiser.
	 * 
	 * @param advertiser
	 *            the advertiser
	 * 
	 * @return the daily spend limit for the advertiser.
	 */
	double getDailySpendLimit(String advertiser);

	/**
	 * Get a {@link BidInfo} for the advertiser for a given query.
	 * 
	 * @param advertiser
	 *            the advertiser
	 * @param query
	 *            the query
	 * @return the bid for the advertiser for a given query.
	 */
	BidInfo getBidInfo(String advertiser, AdxQuery query);

	/**
	 * Get the daily spend limit for the advertiser for a given query.
	 * 
	 * @param advertiser
	 *            the advertiser
	 * @param query
	 *            the query
	 * @return the daily spend limit for the advertiser for a given query.
	 */
	// double getDailySpendLimit(String advertiser, AdxQuery query);

	/**
	 * Get the ad link for the given advertiser and query
	 * 
	 * @param advertiser
	 *            the advertiser
	 * @param query
	 *            the query
	 * @return the ad link for the given advertiser and query
	 */
	// AdLink getAdLink(String advertiser, AdxQuery query);

	/**
	 * Update the bid information
	 * 
	 * @param advertiser
	 *            the advertiser
	 * @param bundle
	 *            the bid bundle
	 */
	void updateBids(String advertiser, AdxBidBundle bundle);

	/**
	 * Get the number of advertisers tracked.
	 * 
	 * @return the number of advertisers tracked.
	 */
	int size();
}
