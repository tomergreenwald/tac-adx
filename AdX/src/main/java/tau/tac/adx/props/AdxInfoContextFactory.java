/*
 * ContextFactory.java
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
package tau.tac.adx.props;

import se.sics.isl.transport.Context;
import se.sics.isl.transport.ContextFactory;
import se.sics.tasim.props.AdminContent;
import se.sics.tasim.props.Alert;
import se.sics.tasim.props.Ping;
import se.sics.tasim.props.ServerConfig;
import se.sics.tasim.props.SimulationStatus;
import se.sics.tasim.props.StartInfo;
import tau.tac.adx.report.adn.AdNetworkKey;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.AdNetworkReportEntry;
import tau.tac.adx.report.demand.AdNetBidMessage;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.CampaignReport;
import tau.tac.adx.report.demand.CampaignReportEntry;
import tau.tac.adx.report.demand.CampaignReportKey;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReport;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReportEntry;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReportKey;
import tau.tac.adx.report.publisher.AdxPublisherReport;
import tau.tac.adx.report.publisher.AdxPublisherReportEntry;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.Auction;
import edu.umich.eecs.tac.props.BankStatus;
import edu.umich.eecs.tac.props.BidBundle;
import edu.umich.eecs.tac.props.ManufacturerComponentComposable;
import edu.umich.eecs.tac.props.Pricing;
import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.PublisherInfo;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryReport;
import edu.umich.eecs.tac.props.Ranking;
import edu.umich.eecs.tac.props.ReserveInfo;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.SalesReport;
import edu.umich.eecs.tac.props.SlotInfo;
import edu.umich.eecs.tac.props.UserClickModel;
import edu.umich.eecs.tac.props.UserPopulationState;

/**
 * AAInfo is a context factory that provides the allowable transports for the
 * TAC/AA simulation.
 * 
 * @author Lee Callender, Patrick Jordan
 * @author greenwald, Mariano Schain
 */
public class AdxInfoContextFactory implements ContextFactory {
	/**
	 * Basic context name.
	 */
	private static final String ADX_CONTEXT_NAME = "adxcontext";

	/**
	 * Cache of the last created context (since contexts should be constants).
	 */
	private static Context lastContext;

	/**
	 * Creates a new AA context factory.
	 */
	public AdxInfoContextFactory() {
	}

	/**
	 * Adds the allowable transports to the context.
	 * 
	 * @return the base context with new transports added.
	 */
	@Override
	public final Context createContext() {
		return createContext(null);
	}

	/**
	 * Creates the allowable transports in a {@link Context}.
	 * 
	 * @param parentContext
	 *            the parent context
	 * @return the context with new transports added.
	 */
	@Override
	public final Context createContext(final Context parentContext) {
		Context con = lastContext;
		if (con != null && con.getParent() == parentContext) {
			return con;
		}
		con = new Context(ADX_CONTEXT_NAME, parentContext);
		con.addTransportable(new Ping());
		con.addTransportable(new Alert());
		con.addTransportable(new BankStatus());
		con.addTransportable(new AdminContent());
		con.addTransportable(new SimulationStatus());
		con.addTransportable(new StartInfo());
		con.addTransportable(new SlotInfo());
		con.addTransportable(new ReserveInfo());
		con.addTransportable(new PublisherInfo());
		con.addTransportable(new ServerConfig());
		con.addTransportable(new Query());
		con.addTransportable(new Product());
		con.addTransportable(new Ad());
		con.addTransportable(new AdLink());
		con.addTransportable(new SalesReport());
		con.addTransportable(new SalesReport.SalesReportEntry());
		con.addTransportable(new QueryReport());
		con.addTransportable(new QueryReport.QueryReportEntry());
		con.addTransportable(new QueryReport.DisplayReportEntry());
		con.addTransportable(new QueryReport.DisplayReport());
		con.addTransportable(new RetailCatalog());
		con.addTransportable(new RetailCatalog.RetailCatalogEntry());
		con.addTransportable(new BidBundle());
		con.addTransportable(new BidBundle.BidEntry());
		con.addTransportable(new Ranking());
		con.addTransportable(new Ranking.Slot());
		con.addTransportable(new Pricing());
		con.addTransportable(new UserClickModel());
		con.addTransportable(new Auction());
		con.addTransportable(new AdvertiserInfo());
		con.addTransportable(new ManufacturerComponentComposable());
		con.addTransportable(new UserPopulationState());
		con.addTransportable(new UserPopulationState.UserPopulationEntry());

		con.addTransportable(new AdxQuery());
		con.addTransportable(new PublisherCatalog());
		con.addTransportable(new PublisherCatalogEntry());
		con.addTransportable(new AdxPublisherReportEntry());
		con.addTransportable(new AdxPublisherReport());
		con.addTransportable(new AdNetworkReportEntry());
		con.addTransportable(new AdNetworkReport());
		con.addTransportable(new AdNetworkKey());
		con.addTransportable(new AdxBidBundle());
		con.addTransportable(new AdxBidBundle.BidEntry());
		con.addTransportable(new InitialCampaignMessage());
		con.addTransportable(new CampaignOpportunityMessage());
		con.addTransportable(new CampaignReport());
		con.addTransportable(new CampaignReportEntry());
		con.addTransportable(new CampaignReportKey());
		con.addTransportable(new AdNetworkDailyNotification());
		con.addTransportable(new AdNetBidMessage());
		con.addTransportable(new CampaignAuctionReport());
		con.addTransportable(new CampaignAuctionReportEntry());
		con.addTransportable(new CampaignAuctionReportKey());

		// Cache the last context
		lastContext = con;
		return con;
	}
}
