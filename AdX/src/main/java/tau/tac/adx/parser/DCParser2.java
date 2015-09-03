package tau.tac.adx.parser;

import java.util.HashMap;
import java.util.Map;

import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.logtool.LogReader;
import tau.tac.adx.props.PublisherCatalogEntry;
import tau.tac.adx.props.ReservePriceInfo;
import tau.tac.adx.props.ReservePriceType;
import tau.tac.adx.report.publisher.AdxPublisherReport;
import tau.tac.adx.report.publisher.AdxPublisherReportEntry;
import tau.tac.adx.sim.AuctionReport;
import tau.tac.adx.sim.config.AdxConfigurationParser;
import edu.umich.eecs.tac.Parser;

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
public class DCParser2 extends Parser {

	static int FEATURE_COUNT = 5 + AdxConfigurationParser.publisherNames.length;

	Map<String, Integer> publisherNameToId = new HashMap<>();

	private double totalRevenue = 0;
	private int auctionCount = 0;
	private int auctionWinCount = 0;
	private int passedSecond = 0;
	private double avgDistance = 0;
	private boolean dc;
	private ReservePriceType reservePriceType;

	public DCParser2(LogReader reader, ConfigManager configManager) {
		super(reader);
		for (int i = 0; i < AdxConfigurationParser.publisherNames.length; i++) {
			publisherNameToId.put(AdxConfigurationParser.publisherNames[i], i);
		}
		totalRevenue = 0;
		auctionCount = 0;
		auctionWinCount = 0;
		passedSecond = 0;
		avgDistance = 0;
		dc = false;
		reservePriceType = null;
	}

	protected void dataUpdated(int type, Transportable content) {

		if (content instanceof AuctionReport && Math.random() < 0.05) {
			AuctionReport auctionReport = (AuctionReport) content;

			if (auctionReport.getFirstBid() > auctionReport.getReservePrice()) {
				double revenue;
				if (auctionReport.getSecondBid() > auctionReport
						.getReservePrice()) {
					revenue = auctionReport.getSecondBid();
					passedSecond++;
				} else {
					revenue = auctionReport.getReservePrice();
				}

				totalRevenue += revenue;
				auctionWinCount++;
			}
			avgDistance += (auctionReport.getReservePrice() - auctionReport
					.getFirstBid());
			auctionCount++;
		} else if (content instanceof AdxPublisherReport) {
			boolean maybeDC = false;
			AdxPublisherReport adxPublisherReport = (AdxPublisherReport) content;
			for (PublisherCatalogEntry publisherKey : adxPublisherReport.keys()) {
				AdxPublisherReportEntry entry = adxPublisherReport
						.getEntry(publisherKey);
				if (entry.getReservePriceBaseline() == 0) {
					maybeDC = true;
				} else {
					return;
				}
			}
			if (maybeDC) {
				dc = true;
			}
		}
		if (content instanceof ReservePriceInfo) {
			reservePriceType = ((ReservePriceInfo) content)
					.getReservePriceType();
		}
	}

	/**
	 * Invoked when the parse process ends.
	 */
	@Override
	protected void parseStopped() {
		if (auctionCount != 0) {
			avgDistance /= auctionCount;
			ReservePriceType type;
			if (reservePriceType != null) {
				type = reservePriceType;
			} else {
				if (passedSecond == auctionCount) {
					type = ReservePriceType.None;
				} else if (dc) {
					type = ReservePriceType.DC;
				} else {
					type = ReservePriceType.Adjustable;
				}
			}
			System.out
					.println(String
							.format("Reserve type - %s Revenue - %f distance - %f (Win count %d/%d, PassCount %d/%d)",
									type.toString(), totalRevenue, avgDistance,
									auctionWinCount, auctionCount,
									passedSecond, auctionCount));
			auctionCount = 0;
		}

	}

	@Override
	protected void message(int sender, int receiver, Transportable content) {
		// TODO Auto-generated method stub

	}

}
