/**
 *
 */
package tau.tac.adx.publishers.reserve;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import tau.tac.adx.parser.Auctions;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.sim.config.AdxConfigurationParser;

/**
 * @author Tomer Greenwald
 */
public class PredeterminedReservePriceManager implements
        MultiReservePriceManager<AdxQuery> {
    static int counter = -1;
    static double reservePrice = 0.0008;

    private double[] coefficients;
    Map<String, Integer> publisherNameToId = new HashMap<>();

    /**
     * @param priceBundle
     */
    public PredeterminedReservePriceManager(
            double[] coefficients) {
        this.coefficients = coefficients;
        for (int i = 0; i < AdxConfigurationParser.publisherNames.length; i++) {
            publisherNameToId.put(AdxConfigurationParser.publisherNames[i], i);
        }
    }

    private Auctions.AdxQuery convertQuery(AdxQuery adxQuery) {
        List<Auctions.MarketSegment> marketSegments = new LinkedList<Auctions.MarketSegment>();
        for (tau.tac.adx.report.adn.MarketSegment marketSegment : adxQuery
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
        return protoAdxQuery;
    }

    /**
     * @see tau.tac.adx.publishers.reserve.MultiReservePriceManager#generateReservePrice(java.lang.Object)
     */
    @Override
    public double generateReservePrice(AdxQuery adxQuery) {
        return reservePrice;
//		double[] features = Utils.getFeatures(convertQuery(adxQuery), publisherNameToId);
//		double sum = 0;
//		for (int i = 0; i < features.length; i++) {
//			sum += features[i] * coefficients[i];
//		}
//		return sum;
    }

    /**
     * @see tau.tac.adx.publishers.reserve.MultiReservePriceManager#addImpressionForPrice(double,
     * java.lang.Object)
     */
    @Override
    public void addImpressionForPrice(double reservePrice, AdxQuery t) {
        // Left blank intentionally
    }

    /**
     * @see tau.tac.adx.publishers.reserve.MultiReservePriceManager#updateDailyBaselineAverage()
     */
    @Override
    public void updateDailyBaselineAverage() {
        // Left blank intentionally
    }

    /**
     * @see tau.tac.adx.publishers.reserve.MultiReservePriceManager#getDailyBaselineAverage(java.lang.Object)
     */
    @Override
    public double getDailyBaselineAverage(AdxQuery t) {
        return 0;
    }

    static FileOutputStream fos;

    static {
        try {
            fos = new FileOutputStream("t:\\log.txt");
        } catch (FileNotFoundException e) {

        }
    }

    public static void simulationStarted() {
        counter++;
        if (counter % 10 == 0) {
            reservePrice = 0.0000;
        }
        reservePrice += 0.00002;
        try {
            fos.write(String.format("Simulation #%d - reserve %f\n", counter, reservePrice).getBytes());
            fos.flush();
        } catch (IOException e) {

        }
    }
}
