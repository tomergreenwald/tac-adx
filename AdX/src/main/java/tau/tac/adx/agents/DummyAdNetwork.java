/*
 * DummyAdvertiser.java
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
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.Message;
import se.sics.tasim.props.SimulationStatus;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.props.PublisherCatalogEntry;
import tau.tac.adx.report.adn.MarketSegment;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.AdvertiserInfo;

/**
 * @author Lee Callender, Patrick Jordan
 */

public class DummyAdNetwork extends Agent {

	private final Logger log = Logger.global;

	private PublisherCatalog publisherCatalog;
	private String publisherAddress;
	private AdxBidBundle bidBundle;

	private AdxQuery[] queries;
	private double[] impressions;
	private double[] clicks;
	private double[] conversions;
	private double[] values;

	public DummyAdNetwork() {
	}

	@Override
	protected void messageReceived(Message message) {
		try {
			Transportable content = message.getContent();
			// if (content instanceof AdNetworkReport) {
			// // log.info("Dummy got AdxQuery");
			// handleAdNetworkReport((AdNetworkReport) content);
			// } else if (content instanceof AdxPublisherReport) {
			// handleSalesReport((AdNetworkReport) content);
			// } else if (content instanceof SimulationStatus) {
			// handleSimulationStatus((SimulationStatus) content);
			// } else if (content instanceof PublisherCatalog) {
			// handleRetailCatalog((PublisherCatalog) content);
			// } else if (content instanceof AdvertiserInfo) {
			// handleAdvertiserInfo((AdvertiserInfo) content);
			// }
		} catch (NullPointerException e) {
			this.log.log(Level.SEVERE, "Null Message received.");
			return;
		}
	}

	private void handleSimulationStatus(SimulationStatus simulationStatus) {
		sendBidAndAds();
	}

	private void handleRetailCatalog(PublisherCatalog publisherCatalog) {
		this.publisherCatalog = publisherCatalog;
		generateAdxQuerySpace();
	}

	private void handleAdvertiserInfo(AdvertiserInfo advertiserInfo) {
		publisherAddress = advertiserInfo.getPublisherId();

	}

	// private void handleAdNetworkReport(AdNetworkReport queryReport) {
	// for (int i = 0; i < queries.length; i++) {
	// AdxQuery query = queries[i];
	// queryReport.g
	// int index = queryReport.indexForEntry(query);
	// if (index >= 0) {
	// impressions[i] += queryReport.getImpressions(index);
	// clicks[i] += queryReport.getClicks(index);
	// }
	// }
	// }

	// private void handleSalesReport(SalesReport salesReport) {
	// for (int i = 0; i < queries.length; i++) {
	// AdxQuery query = queries[i];
	//
	// int index = salesReport.indexForEntry(query);
	// if (index >= 0) {
	// conversions[i] += salesReport.getConversions(index);
	// values[i] += salesReport.getRevenue(index);
	// }
	// }
	// }

	@Override
	protected void simulationSetup() {

		bidBundle = new AdxBidBundle();

		// Add the advertiser name to the logger name for convenient
		// logging. Note: this is usually a bad idea because the logger
		// objects will never be garbaged but since the dummy names always
		// are the same in TAC AA games, only a few logger objects
		// will be created.
		// this.log = Logger.getLogger(DummyAdNetwork.class.getName() + '.'
		// + getName());
		log.fine("dummy " + getName() + " simulationSetup");
	}

	@Override
	protected void simulationFinished() {
		bidBundle = null;
	}

	protected void sendBidAndAds() {
		// log.info("dummy bidding");
		bidBundle = new AdxBidBundle();
		Ad ad = new Ad(null);

		for (int i = 0; i < queries.length; i++) {
			bidBundle.addQuery(queries[i], values[i] / clicks[i], ad);
			// bidBundle.addAdxQuery(queries[i], 100, ad);
		}

		if (bidBundle != null && publisherAddress != null) {
			sendMessage(publisherAddress, bidBundle);
		}
	}

	private void generateAdxQuerySpace() {
		if (publisherCatalog != null && queries == null) {
			Set<AdxQuery> queryList = new HashSet<AdxQuery>();
			Random random = new Random();
			for (PublisherCatalogEntry publisherCatalogEntry : publisherCatalog) {
				// Create f0
				AdxQuery f0 = new AdxQuery();

				// Create f1's
				AdxQuery f1_manufacturer = new AdxQuery(
						publisherCatalogEntry.getPublisherName(),
						Collections.singletonList(MarketSegment.values()[random
								.nextInt(MarketSegment.values().length)]),
						null, null);
				AdxQuery f1_component = new AdxQuery(
						publisherCatalogEntry.getPublisherName(),
						Collections.singletonList(MarketSegment.values()[random
								.nextInt(MarketSegment.values().length)]),
						Device.values()[random.nextInt(Device.values().length)],
						AdType.values()[random.nextInt(AdType.values().length)]);

				// Create f2
				AdxQuery f2 = new AdxQuery(
						publisherCatalogEntry.getPublisherName(),
						Collections.singletonList(MarketSegment.values()[random
								.nextInt(MarketSegment.values().length)]),
						Device.values()[random.nextInt(Device.values().length)],
						AdType.values()[random.nextInt(AdType.values().length)]);

				queryList.add(f0);
				queryList.add(f1_manufacturer);
				queryList.add(f1_component);
				queryList.add(f2);
			}

			queries = queryList.toArray(new AdxQuery[0]);
			impressions = new double[queries.length];
			clicks = new double[queries.length];
			conversions = new double[queries.length];
			values = new double[queries.length];

			for (int i = 0; i < queries.length; i++) {
				impressions[i] = 100;
				clicks[i] = 9;
				conversions[i] = 1;
			}
		}
	}
}
