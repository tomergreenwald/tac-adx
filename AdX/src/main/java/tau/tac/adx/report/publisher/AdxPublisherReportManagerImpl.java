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
package tau.tac.adx.report.publisher;

import java.util.logging.Logger;

import tau.tac.adx.messages.AuctionMessage;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * @author Patrick Jordan, Lee Callender, Akshat Kaul
 * @author greenwald
 */
public class AdxPublisherReportManagerImpl implements AdxPublisherReportManager {
	/**
	 * {@link Logger}.
	 */
	protected Logger log = Logger.getLogger(AdxPublisherReportManagerImpl.class
			.getName());

	/**
	 * The query reports
	 */
	private final AdxPublisherReport publisherReport = new AdxPublisherReport();

	/**
	 * The {@link AdxPublisherReportSender}.
	 */
	private final AdxPublisherReportSender publisherReportSender;

	/**
	 * Create a new publisher report manager
	 * 
	 * @param publisherReportSender
	 *            The {@link AdxPublisherReportSender}.
	 * @param eventBus
	 *            Global {@link EventBus}.
	 */
	public AdxPublisherReportManagerImpl(
			AdxPublisherReportSender publisherReportSender, EventBus eventBus) {
		this.publisherReportSender = publisherReportSender;
		eventBus.register(this);
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
	 * @param message
	 *            {@link AuctionMessage}.
	 */
	@Subscribe
	public void queryIssued(AuctionMessage message) {
		publisherReport.addQuery(message.getQuery());
	}

	/**
	 * @see tau.tac.adx.report.publisher.AdxPublisherReportManager#sendReportsToAll()
	 */
	@Override
	public void sendReportsToAll() {
		publisherReportSender.broadcastPublisherReport(publisherReport);
	}

}
