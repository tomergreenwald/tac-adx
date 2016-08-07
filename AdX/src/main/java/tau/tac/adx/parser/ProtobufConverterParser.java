package tau.tac.adx.parser;

import edu.umich.eecs.tac.Parser;
import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.logtool.LogReader;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.sim.AuctionReport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * <code>GeneralParser</code> is a simple example of a TAC Adx parser that
 * prints out a variety of messages received in a simulation from the simulation
 * log file.
 * <p>
 * <p>
 * The class <code>Parser</code> is inherited to provide base functionality for
 * TAC Adx log processing.
 *
 * @author - greenwald
 * @see Parser
 */
public class ProtobufConverterParser extends Parser {

    static int simulationCounter = 0;
    static Auctions.DataBundle.Builder dataBundle = Auctions.DataBundle.newBuilder();
    private int day;

    public ProtobufConverterParser(LogReader reader, ConfigManager configManager) {
        super(reader);
        simulationCounter++;
    }

    public static void serialize() {

        try {
            System.out.println("serializing");
            File file = new File(
                    "T:\\log.protobuf");
            file.delete();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(dataBundle.build().toByteArray());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void dataUpdated(int type, Transportable content) {
        if (content instanceof AuctionReport) {

            AuctionReport report = (AuctionReport) content;

            AuctionReport auctionReport = (AuctionReport) content;
            Auctions.AuctionReport.Builder auctionReportBuilder = Auctions.AuctionReport
                    .newBuilder();
            auctionReportBuilder.setFirstBid(auctionReport.getFirstBid());
            auctionReportBuilder.setSecondBid(auctionReport.getSecondBid());
            auctionReportBuilder.setReservePrice(auctionReport
                    .getReservePrice());
            auctionReportBuilder.setDay(day);
            AdxQuery adxQuery = auctionReport.getAdxQuery();
            List<Auctions.MarketSegment> marketSegments = new LinkedList<Auctions.MarketSegment>();
            for (MarketSegment marketSegment : adxQuery
                    .getMarketSegments()) {
                marketSegments.add(Auctions.MarketSegment.valueOf(marketSegment
                        .ordinal()));
            }
            Auctions.AdxQuery protoAdxQuery = Auctions.AdxQuery.newBuilder()
                    .setPublisher(adxQuery.getPublisher())
                    .addAllMarketSegments(marketSegments)
                    .setDevice(Auctions.Device.valueOf(adxQuery.getDevice().ordinal()))
                    .setAdtype(Auctions.AdType.valueOf(adxQuery.getAdType().ordinal()))
                    .build();
            auctionReportBuilder.setAdxQuery(protoAdxQuery);
            dataBundle.addReports(auctionReportBuilder);
        }

    }

    protected void parseStopped() {

    }

    @Override
    protected void message(int sender, int receiver, Transportable content) {

    }

    @Override
    protected void nextDay(int date, long serverTime) {
        day = date;
    }
}
