/*
 * QueryReportManagerImpl.java
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
import java.util.Map.Entry;
import java.util.logging.Logger;

import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.auction.data.AuctionState;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.users.AdxUser;

/**
 * @author Patrick Jordan, Lee Callender, Akshat Kaul
 */
public class AdNetworkReportManagerImpl implements AdNetworkReportManager {
	/**
	 * {@link Logger}.
	 */
	protected Logger log = Logger.getLogger(AdNetworkReportManagerImpl.class
			.getName());

	/**
	 * {@link AdNetworkReport}s, each matches and <b>AdNetwork</b>.
	 */
	private final Map<Integer, AdNetworkReport> adNetworkReports = new HashMap<Integer, AdNetworkReport>();

	/**
	 * The {@link AdNetworkReportSender}.
	 */
	private final AdNetworkReportSender adNetworkReportSender;

	/**
	 * Create a new publisher report manager
	 * 
	 * @param adNetworkReportSender
	 *            The {@link AdNetworkReportSender}.
	 */
	public AdNetworkReportManagerImpl(
			AdNetworkReportSender adNetworkReportSender) {
		this.adNetworkReportSender = adNetworkReportSender;
		log.info("AdxQueryReportManager created.");
	}

	// ------------------------------------------------------------------------------------------------------
	/**
	 * @see tau.tac.adx.report.publisher.AdxPublisherReportManager#size()
	 */
	@Override
	public int size() {
		return adNetworkReports.size();
	}

	/**
	 * @see tau.tac.adx.users.AdxUserEventListener#queryIssued(tau.tac.adx.props.AdxQuery)
	 */
	@Override
	public void queryIssued(AdxQuery query) {
		// No implementation needed.
	}

	/**
	 * @param auctionResult
	 * @param query
	 * @param user
	 * @see tau.tac.adx.users.AdxUserEventListener#auctionPerformed(AdxAuctionResult,
	 *      AdxQuery, AdxUser)
	 */
	@Override
	public void auctionPerformed(AdxAuctionResult auctionResult,
			AdxQuery query, AdxUser user) {
		if (auctionResult.getAuctionState() == AuctionState.AUCTION_COPMLETED) {
			int bidder = auctionResult.getWinningBidInfo().getBidder().getId();
			AdNetworkReport report = adNetworkReports.get(bidder);
			if (report == null) {
				report = new AdNetworkReport();
				adNetworkReports.put(bidder, report);
			}
			report.addBid(auctionResult, query, user);
		}
	}

	/**
	 * @see tau.tac.adx.report.publisher.AdxPublisherReportManager#sendReportsToAll()
	 */
	@Override
	public void sendReportsToAll() {
		for (Entry<Integer, AdNetworkReport> entry : adNetworkReports
				.entrySet()) {
			adNetworkReportSender.broadcastReport(entry.getKey(),
					entry.getValue());
		}
	}
}
