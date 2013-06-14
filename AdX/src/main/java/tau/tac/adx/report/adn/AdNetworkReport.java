/*
 * QueryReport.java
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
package tau.tac.adx.report.adn;

import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.users.AdxUser;
import edu.umich.eecs.tac.props.AbstractKeyedEntryList;

public class AdNetworkReport extends
		AbstractKeyedEntryList<AdNetworkKey, AdNetworkReportEntry> {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = -7957495904471250085L;

	/**
	 * Returns the {@link AdNetworkReportEntry} class.
	 * 
	 * @return the {@link AdNetworkReportEntry} class.
	 */
	@Override
	protected final Class<AdNetworkReportEntry> entryClass() {
		return AdNetworkReportEntry.class;
	}

	/**
	 * @see edu.umich.eecs.tac.props.AbstractKeyedEntryList#createEntry(java.lang.Object)
	 */
	@Override
	protected AdNetworkReportEntry createEntry(AdNetworkKey key) {
		return new AdNetworkReportEntry(key);
	}

	/**
	 * Adds an {@link AdNetworkKey} to the report.
	 * 
	 * @param adNetworkKey
	 *            {@link AdNetworkKey}.
	 * @return {@link AdNetworkReportEntry}.
	 * 
	 */
	public AdNetworkReportEntry addReportEntry(AdNetworkKey adNetworkKey) {
		lockCheck();
		int index = addKey(adNetworkKey);
		return getEntry(index);
	}

	/**
	 * Retrieves an {@link AdNetworkReportEntry} keyed with a
	 * {@link AdNetworkKey}.
	 * 
	 * @param adNetworkKey
	 *            {@link AdNetworkKey}.
	 * @return {@link AdNetworkReportEntry}.
	 * 
	 */
	public AdNetworkReportEntry getAdNetworkReportEntry(
			AdNetworkKey adNetworkKey) {
		return getEntry(adNetworkKey);
	}

	/**
	 * Generates an {@link AdNetworkKey} according to given parameters.
	 * 
	 * @param marketSegment
	 *            {@link MarketSegment}.
	 * @param query
	 *            {@link AdxQuery}.
	 * @param auctionResult
	 * @return Corresponding {@link AdNetworkKey}.
	 */
	private AdNetworkKey getAdNetworkKey(AdxUser adxUser, AdxQuery query,
			AdxAuctionResult auctionResult) {
		return new AdNetworkKey(adxUser, query.getPublisher(),
				query.getDevice(), query.getAdType(), auctionResult
						.getWinningBidInfo().getCampaign().getId());
	}

	/**
	 * Adds bid related data to the report.
	 * 
	 * @param auctionResult
	 *            {@link AdxAuctionResult}.
	 * @param query
	 *            {@link AdxQuery}.
	 * @param user
	 *            {@link AdxUser}.
	 * @param hasWon
	 */
	public void addBid(AdxAuctionResult auctionResult, AdxQuery query,
			AdxUser user, boolean hasWon) {
		AdNetworkKey adNetworkKey = getAdNetworkKey(user, query, auctionResult);
		AdNetworkReportEntry reportEntry = getAdNetworkReportEntry(adNetworkKey);
		if (reportEntry == null) {
			reportEntry = addReportEntry(adNetworkKey);
		}
		reportEntry.addAuctionResult(auctionResult, hasWon);
	}

	/**
	 * Calculates daily expenses sum.
	 * 
	 * @return Daily cost.
	 */
	public double getDailyCost() {
		double result = 0;
		for (AdNetworkReportEntry adNetworkReportEntry : getEntries()) {
			result = result + adNetworkReportEntry.getCost();
		}
		return result / 1000;
	}
}
