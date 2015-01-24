/**
 * 
 */
package tau.tac.adx.publishers.reserve;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.parser.Auctions.AdxQueryPricing;
import tau.tac.adx.parser.Auctions.ReservePriceManagerBundle;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.report.adn.MarketSegment;

/**
 * @author Tomer Greenwald
 *
 */
public class PredeterminedReservePriceManager implements
		MultiReservePriceManager<AdxQuery> {

	private Map<AdxQuery, Double> pricing = new HashMap<>();

	/**
	 * @param priceBundle
	 */
	public PredeterminedReservePriceManager(
			ReservePriceManagerBundle priceBundle) {
		for (AdxQueryPricing queryPricing : priceBundle
				.getAdxQueryPricingsList()) {
			String publisher = queryPricing.getAdxQuery().getPublisher();
			Set<MarketSegment> user = new HashSet<>();
			for (tau.tac.adx.parser.Auctions.MarketSegment marketSegment : queryPricing
					.getAdxQuery().getMarketSegmentsList()) {
				user.add(MarketSegment.values()[marketSegment.getNumber()]);
			}
			Device device = Device.values()[queryPricing.getAdxQuery()
					.getDevice().getNumber()];
			AdType adType = AdType.values()[queryPricing.getAdxQuery()
					.getAdtype().getNumber()];
			AdxQuery adxQuery = new AdxQuery(publisher, user, device, adType);
			pricing.put(adxQuery, (double) queryPricing.getReservePrice());
		}
	}

	/**
	 * @see tau.tac.adx.publishers.reserve.MultiReservePriceManager#generateReservePrice(java.lang.Object)
	 */
	@Override
	public double generateReservePrice(AdxQuery adxQuery) {
		if (!pricing.containsKey(adxQuery)) {
			return pricing.put(adxQuery, 0.0);
		}
		return pricing.get(adxQuery);
	}

	/**
	 * @see tau.tac.adx.publishers.reserve.MultiReservePriceManager#addImpressionForPrice(double,
	 *      java.lang.Object)
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

}
