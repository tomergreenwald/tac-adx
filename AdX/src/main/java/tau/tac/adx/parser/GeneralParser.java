package tau.tac.adx.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.logtool.LogReader;
import tau.tac.adx.auction.tracker.AdxBidTrackerImpl.AdxQueryBid;
import tau.tac.adx.parser.Auctions.AdType;
import tau.tac.adx.parser.Auctions.AdxQuery;
import tau.tac.adx.parser.Auctions.DataBundle;
import tau.tac.adx.parser.Auctions.Device;
import tau.tac.adx.parser.Auctions.MarketSegment;
import tau.tac.adx.sim.AuctionReport;
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
public class GeneralParser extends Parser {

	private int counter = 0;
	private DataBundle.Builder builder = DataBundle.newBuilder();

	public GeneralParser(LogReader reader, ConfigManager configManager) {
		super(reader);
	}

	protected void dataUpdated(int type, Transportable content) {

		if (content instanceof AuctionReport) {
			AuctionReport auctionReport = (AuctionReport) content;
			tau.tac.adx.parser.Auctions.AuctionReport.Builder auctionReportBuilder = tau.tac.adx.parser.Auctions.AuctionReport
					.newBuilder();
			auctionReportBuilder.setFirstBid(auctionReport.getFirstBid());
			auctionReportBuilder.setSecondsBid(auctionReport.getSecondBid());
			auctionReportBuilder.setReservedPrice(auctionReport
					.getReservePrice());
			tau.tac.adx.props.AdxQuery adxQuery = auctionReport.getAdxQuery();
			List<MarketSegment> marketSegments = new LinkedList<Auctions.MarketSegment>();
			for (tau.tac.adx.report.adn.MarketSegment marketSegment : adxQuery
					.getMarketSegments()) {
				marketSegments.add(MarketSegment.valueOf(marketSegment
						.ordinal()));
			}
			AdxQuery protoAdxQuery = AdxQuery.newBuilder()
					.setPublisher(adxQuery.getPublisher())
					.addAllMarketSegments(marketSegments)
					.setDevice(Device.valueOf(adxQuery.getDevice().ordinal()))
					.setAdtype(AdType.valueOf(adxQuery.getAdType().ordinal()))
					.build();
			auctionReportBuilder.setAdxQuery(protoAdxQuery);
			builder.addReports(auctionReportBuilder);
			counter++;
			if (counter % 10000 == 0) {
				System.out.println(counter);
			}
		}
	}

	/**
	 * Invoked when the parse process ends.
	 */
	@Override
	protected void parseStopped() {
		try {
			System.out.println("serializing");
			File file = new File(
					"C:\\Users\\Tomer\\git\\tac-adx\\AdX\\resources\\log.protobuf");
			file.delete();
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			fileOutputStream.write(builder.build().toByteArray());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void message(int sender, int receiver, Transportable content) {
		// TODO Auto-generated method stub

	}

}
