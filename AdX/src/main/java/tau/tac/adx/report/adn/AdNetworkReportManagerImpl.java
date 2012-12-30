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

import java.util.logging.Logger;

import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.report.publisher.AdxPublisherReportManager;
import tau.tac.adx.report.publisher.AdxPublisherReportSender;
import edu.umich.eecs.tac.props.Ad;

/**
 * @author Patrick Jordan, Lee Callender, Akshat Kaul
 */
public class AdNetworkReportManagerImpl implements AdxPublisherReportManager {
	/**
	 * {@link Logger}.
	 */
	protected Logger log = Logger.getLogger(AdNetworkReportManagerImpl.class
			.getName());

	/**
	 * The query reports
	 */
	private final AdNetworkReport publisherReport = new AdNetworkReport();

	/**
	 * The {@link AdxPublisherReportSender}.
	 */
	private final AdxPublisherReportSender publisherReportSender;

	/**
	 * Create a new publisher report manager
	 * 
	 * @param publisherReportSender
	 *            The {@link AdxPublisherReportSender}.
	 */
	public AdNetworkReportManagerImpl(
			AdxPublisherReportSender publisherReportSender) {
		this.publisherReportSender = publisherReportSender;
		log.info("AdxQueryReportManager created.");
	}

	// ------------------------------------------------------------------------------------------------------
	/**
	 * @see tau.tac.adx.report.publisher.AdxPublisherReportManager#size()
	 */
	@Override
	public int size() {
		return publisherReport.size();
	}

	/**
	 * @see tau.tac.adx.users.AdxUserEventListener#queryIssued(tau.tac.adx.props.AdxQuery)
	 */
	@Override
	public void queryIssued(AdxQuery query) {
		publisherReport.addQuery(query);
	}

	/**
	 * @see tau.tac.adx.users.AdxUserEventListener#adViewed(tau.tac.adx.props.AdxQuery,
	 *      edu.umich.eecs.tac.props.Ad, java.lang.String)
	 */
	@Override
	public void adViewed(AdxQuery query, Ad ad, String advertiserName) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see tau.tac.adx.report.publisher.AdxPublisherReportManager#sendQueryReportToAll()
	 */
	@Override
	public void sendQueryReportToAll() {
		publisherReportSender.broadcastReport(publisherReport);
	}
}
