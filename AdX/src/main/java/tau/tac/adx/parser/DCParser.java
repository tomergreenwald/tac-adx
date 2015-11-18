package tau.tac.adx.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.logtool.LogReader;
import tau.tac.adx.parser.Auctions.AdType;
import tau.tac.adx.parser.Auctions.AdxQuery;
import tau.tac.adx.parser.Auctions.DataBundle;
import tau.tac.adx.parser.Auctions.Device;
import tau.tac.adx.parser.Auctions.MarketSegment;
import tau.tac.adx.playground.Utils;
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
public class DCParser extends Parser {

	static int FEATURE_COUNT = 5 + AdxConfigurationParser.publisherNames.length;

	private int counter = 0;
	private DataBundle.Builder builder = DataBundle.newBuilder();
	Map<String, Integer> publisherNameToId = new HashMap<>();

	public DCParser(LogReader reader, ConfigManager configManager) {
		super(reader);
		for (int i = 0; i < AdxConfigurationParser.publisherNames.length; i++) {
			publisherNameToId.put(AdxConfigurationParser.publisherNames[i], i);
		}
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

	protected double[] getFeatures(AdxQuery adxQuery) {
		double features[] = new double[5 + 3];
		features[0] = adxQuery.getMarketSegmentsList().contains(
				MarketSegment.MALE) ? 1 : -1;
		features[1] = adxQuery.getMarketSegmentsList().contains(
				MarketSegment.YOUNG) ? 1 : -1;
		features[2] = adxQuery.getMarketSegmentsList().contains(
				MarketSegment.LOW_INCOME) ? 1 : -1;
		features[3] = adxQuery.getDevice() == Device.MOBILE ? 1 : -1;
		features[4] = adxQuery.getAdtype() == AdType.TEXT ? 1 : -1;
		int publisherGroup = publisherNameToId.get(adxQuery.getPublisher())
				/ (publisherNameToId.size() / 3);
		for (int i = 0; i < 3; i++) {
			if (i == publisherGroup)
				features[5 + i] = 1;
			else
				features[5 + i] = -1;
		}
		return features;
	}

	/**
	 * Invoked when the parse process ends.
	 */
	@Override
	protected void parseStopped() {
//		try {
//			System.out.println("serializing");
//			File file = new File("resources\\log.protobuf");
//			file.delete();
//			FileOutputStream fileOutputStream = new FileOutputStream(file);
//			DataBundle build = builder.build();
//			int reportsCount = build.getReportsCount();
//
//			int m = reportsCount;
//			if (reportsCount <= 0) {
//				return;
//			}
//				double[][] featureVector = new double[reportsCount][8];
//				double[] initial_w = { 57.49234810035743, -57.49238391234895,
//						57.49232353552666, -6.531233873881703E-8,
//						57.49230814349496, 172.4770780079259,
//						172.47707787106208, 114.98477706925033 };
//			double[] b1 = new double[reportsCount];
//			double[] b2 = new double[reportsCount];
//			double gamma = .001;
//			double lambda = 0.001;
//			double Lambda = 1000.0;
//			for (int i = 0; i < build.getReportsCount(); i++) {
//				tau.tac.adx.parser.Auctions.AuctionReport report = build
//						.getReports(i);
//				AdxQuery adxQuery = report.getAdxQuery();
//				featureVector[i] = getFeatures(adxQuery);
//				// initial_w[i] = report.getReservedPrice();
//				b1[i] = report.getFirstBid();
//				b2[i] = report.getSecondsBid();
//			}
//			double[] dc = Utils.DC(m, featureVector, initial_w, b1, b2, gamma,
//					lambda, Lambda);
//
//			fileOutputStream.write(build.toByteArray());
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	@Override
	protected void message(int sender, int receiver, Transportable content) {
		// TODO Auto-generated method stub

	}

}
