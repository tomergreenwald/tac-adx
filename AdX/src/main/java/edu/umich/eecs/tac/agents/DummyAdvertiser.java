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
package edu.umich.eecs.tac.agents;

/**
 * @author Lee Callender, Patrick Jordan
 */
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.Message;
import se.sics.tasim.props.SimulationStatus;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.BidBundle;
import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryReport;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.SalesReport;

public class DummyAdvertiser extends Agent {

	private Logger log = Logger.global;

	private RetailCatalog retailCatalog;
	private String publisherAddress;
	private BidBundle bidBundle;

	private Query[] queries;
	private double[] impressions;
	private double[] clicks;
	private double[] conversions;
	private double[] values;

	public DummyAdvertiser() {
	}

	protected void messageReceived(Message message) {
		try {
			Transportable content = message.getContent();
			if (content instanceof QueryReport) {
//                                log.info("Dummy got Query");
				handleQueryReport((QueryReport) content);
			} else if (content instanceof SalesReport) {
				handleSalesReport((SalesReport) content);
			} else if (content instanceof SimulationStatus) {
				handleSimulationStatus((SimulationStatus) content);
			} else if (content instanceof RetailCatalog) {
				handleRetailCatalog((RetailCatalog) content);
			} else if (content instanceof AdvertiserInfo) {
				handleAdvertiserInfo((AdvertiserInfo) content);
			}
		} catch (NullPointerException e) {
			this.log.log(Level.SEVERE, "Null Message received.");
			return;
		}
	}

	private void handleSimulationStatus(SimulationStatus simulationStatus) {
		sendBidAndAds();
	}

	private void handleRetailCatalog(RetailCatalog retailCatalog) {
		this.retailCatalog = retailCatalog;
		generateQuerySpace();
	}

	private void handleAdvertiserInfo(AdvertiserInfo advertiserInfo) {
		publisherAddress = advertiserInfo.getPublisherId();

	}

	private void handleQueryReport(QueryReport queryReport) {
		for (int i = 0; i < queries.length; i++) {
			Query query = queries[i];

			int index = queryReport.indexForEntry(query);
			if (index >= 0) {
				impressions[i] += queryReport.getImpressions(index);
				clicks[i] += queryReport.getClicks(index);
			}
		}
	}

	private void handleSalesReport(SalesReport salesReport) {
		for (int i = 0; i < queries.length; i++) {
			Query query = queries[i];

			int index = salesReport.indexForEntry(query);
			if (index >= 0) {
				conversions[i] += salesReport.getConversions(index);
				values[i] += salesReport.getRevenue(index);
			}
		}
	}

	protected void simulationSetup() {

		bidBundle = new BidBundle();

		// Add the advertiser name to the logger name for convenient
		// logging. Note: this is usually a bad idea because the logger
		// objects will never be garbaged but since the dummy names always
		// are the same in TAC AA games, only a few logger objects
		// will be created.
		this.log = Logger.getLogger(DummyAdvertiser.class.getName() + '.'
				+ getName());
		log.fine("dummy " + getName() + " simulationSetup");
	}

	protected void simulationFinished() {
		bidBundle = null;
	}

	protected void sendBidAndAds() {
//            log.info("dummy bidding");
		bidBundle = new BidBundle();
		Ad ad = new Ad(null);

		for (int i = 0; i < queries.length; i++) {
			bidBundle.addQuery(queries[i], values[i] / clicks[i], ad);
//                        bidBundle.addQuery(queries[i], 100, ad);
		}

		if (bidBundle != null && publisherAddress != null) {
			sendMessage(publisherAddress, bidBundle);
		}
	}

	private void generateQuerySpace() {
		if (retailCatalog != null && queries == null) {
			Set<Query> queryList = new HashSet<Query>();

			for (Product product : retailCatalog) {
				// Create f0
				Query f0 = new Query();

				// Create f1's
				Query f1_manufacturer = new Query(product.getManufacturer(),
						null);
				Query f1_component = new Query(null, product.getComponent());

				// Create f2
				Query f2 = new Query(product.getManufacturer(), product
						.getComponent());

				queryList.add(f0);
				queryList.add(f1_manufacturer);
				queryList.add(f1_component);
				queryList.add(f2);
			}

			queries = queryList.toArray(new Query[0]);
			impressions = new double[queries.length];
			clicks = new double[queries.length];
			conversions = new double[queries.length];
			values = new double[queries.length];

			for (int i = 0; i < queries.length; i++) {
				impressions[i] = 100;
				clicks[i] = 9;
				conversions[i] = 1;
				values[i] = retailCatalog.getSalesProfit(0);
			}
		}
	}
}
