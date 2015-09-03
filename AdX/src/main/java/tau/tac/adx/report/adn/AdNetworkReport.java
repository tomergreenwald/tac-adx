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

import java.util.HashMap;
import java.util.Map;

import tau.tac.adx.demand.Campaign;
import tau.tac.adx.messages.AuctionMessage;
import tau.tac.adx.props.AdxQuery;
import edu.umich.eecs.tac.props.AbstractKeyedEntryList;

public class AdNetworkReport extends
		AbstractKeyedEntryList<AdNetworkKey, AdNetworkReportEntry> {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = -7957495904471250085L;
	
	/**
	 * Caching map. Used instead of the AbstractKeyedEntryList data structure for fast querying.
	 */
	private Map<AdNetworkKey, AdNetworkReportEntry> entryMap = new HashMap<AdNetworkKey, AdNetworkReportEntry>();

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
		AdNetworkReportEntry entry = getEntry(index);
		entryMap.put(adNetworkKey, entry);
		return entry;
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
	 * Retrieves an {@link AdNetworkReportEntry} keyed with a
	 * {@link AdNetworkKey} from the cached map.
	 * 
	 * @param adNetworkKey
	 *            {@link AdNetworkKey}.
	 * @return {@link AdNetworkReportEntry}.
	 * 
	 */
	private AdNetworkReportEntry getCachedAdNetworkReportEntry(
			AdNetworkKey adNetworkKey) {
		return entryMap.get(adNetworkKey);
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
	private static AdNetworkKey getAdNetworkKey(AuctionMessage message, int campaignId) {
		AdxQuery query = message.getQuery();
		return new AdNetworkKey(message.getUser(), query.getPublisher(),
				query.getDevice(), query.getAdType(), campaignId);
	}

	/**
	 * Adds bid related data to the report.
	 * 
	 * @param auctionMessage
	 *            {@link AuctionMessage}.
	 * @param campaignId
	 *            {@link Campaign}.
	 * @param hasWon
	 */
	public void addBid(AuctionMessage auctionMessage, int campaignId, boolean hasWon) {
		AdNetworkKey adNetworkKey = getAdNetworkKey(auctionMessage, campaignId);
		AdNetworkReportEntry reportEntry = getCachedAdNetworkReportEntry(adNetworkKey);
		if (reportEntry == null) {
			reportEntry = addReportEntry(adNetworkKey);
		}
		reportEntry.addAuctionResult(auctionMessage.getAuctionResult(), hasWon);
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
