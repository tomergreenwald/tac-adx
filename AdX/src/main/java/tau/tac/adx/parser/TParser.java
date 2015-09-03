package tau.tac.adx.parser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.logtool.LogReader;
import tau.tac.adx.auction.manager.AdxBidManager;
import tau.tac.adx.auction.manager.AdxBidManagerImpl;
import tau.tac.adx.auction.tracker.AdxBidTracker;
import tau.tac.adx.auction.tracker.AdxBidTrackerImpl;
import tau.tac.adx.auction.tracker.AdxSpendTracker;
import tau.tac.adx.auction.tracker.AdxSpendTrackerImpl;
import tau.tac.adx.messages.AuctionMessage;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.AdxBidBundle.BidEntry;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalogEntry;
import tau.tac.adx.props.ReservePriceInfo;
import tau.tac.adx.props.ReservePriceType;
import tau.tac.adx.report.adn.AdNetworkKey;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.AdNetworkReportEntry;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.report.publisher.AdxPublisherReport;
import tau.tac.adx.report.publisher.AdxPublisherReportEntry;
import tau.tac.adx.sim.AuctionReport;
import tau.tac.adx.sim.config.AdxConfigurationParser;
import tau.tac.adx.users.AdxUser;
import edu.umich.eecs.tac.Parser;
import edu.umich.eecs.tac.props.BankStatus;
import edu.umich.eecs.tac.user.QueryState;

/**
 * <code>GeneralParser</code> is a simple example of a TAC Adx parser that
 * prints out a variety of messages received in a simulation from the simulation
 * log file.
 * <p>
 * <p/>
 * The class <code>Parser</code> is inherited to provide base functionality for
 * TAC Adx log processing.
 * 
 * @author - greenwald
 * 
 * @see edu.umich.eecs.tac.Parser
 */
public class TParser extends Parser {

	FileOutputStream fos;
	int day = 0;
	Random random = new Random();

	Map<Integer, List<AdxBidBundle>> map = new HashMap<>();

	public TParser(LogReader reader, ConfigManager configManager) {
		super(reader);
		try {
			fos = new FileOutputStream("C:\\temp\\2015_05_06\\out\\file.txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void dataUpdated(int type, Transportable content) {
	}

	/**
	 * Invoked when the parse process ends.
	 */
	@Override
	protected void parseStopped() {

	}

	static int max = 0;

	@Override
	protected void message(int sender, int receiver, Transportable content) {

		// TODO Auto-generated method stub
		if (content instanceof AdNetworkReport) {
			AdNetworkReport adNetworkReport = (AdNetworkReport) content;

			for (AdNetworkKey adnetKey : adNetworkReport.keys()) {

				AdNetworkReportEntry entry = adNetworkReport
						.getAdNetworkReportEntry(adnetKey);

				double age = adnetKey.getAge().ordinal();
				double gender = adnetKey.getGender().ordinal();
				double income = adnetKey.getIncome().ordinal();
				double device = adnetKey.getDevice().ordinal();
				double adType = adnetKey.getAdType().ordinal();
				double campaignId = adnetKey.getCampaignId();
				double publisher = adnetKey.getPublisher().hashCode();
				double winCount = (int) (entry.getWinCount() / 10);
				double cost = 0;
				AdxUser user = new AdxUser(adnetKey.getAge(),
						adnetKey.getGender(), adnetKey.getIncome(), 0, 0);
				AdxQuery query = new AdxQuery(adnetKey.getPublisher(), user,
						adnetKey.getDevice(), adnetKey.getAdType());
				for (AdxBidBundle bundle : map.get(receiver)) {
					for (int i = 0; i < bundle.size(); i++) {
						BidEntry entry2 = bundle.getEntry(i);
						AdxQuery key = entry2.getKey();
						if (key.getAdType() == query.getAdType()
								&& key.getDevice() == query.getDevice()
								&& key.getPublisher() == query.getPublisher()
								&& query.getMarketSegments().containsAll(
										key.getMarketSegments()))
							cost = bundle.getBid(query);
						System.out.println(key);
					}
				}

				// AdxBidBundle adxBidBundle = bidBundles.get(receiver);
				// double cost = adxBidBundle.getBid(query);
				// if(cost > 0) {
				// int i= 0;
				// }
				// if (winCount > 0) {
				// int i = 0;
				// adxBidBundle.getBid(query);
				// }
				if (cost != 0) {
					int i =0;
				}

				String format = String.format(
						"%f %f %f %f %f %f %f %f %f %f\n", (double) day, age,
						gender, income, device, adType, campaignId, publisher,
						cost, winCount);
				try {
					fos.write(format.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else if (content instanceof AdxBidBundle) {
			if (!map.containsKey(sender)) {
				map.put(sender, new LinkedList<AdxBidBundle>());
			}
			map.get(sender).add((AdxBidBundle) content);
		} else if (content instanceof CampaignOpportunityMessage) {
			day = ((CampaignOpportunityMessage) content).getDay();
		}
	}

}
