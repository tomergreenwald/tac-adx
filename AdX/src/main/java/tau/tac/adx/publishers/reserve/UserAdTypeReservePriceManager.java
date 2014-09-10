/**
 * 
 */
package tau.tac.adx.publishers.reserve;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import tau.tac.adx.agents.DefaultAdxUserManager;
import tau.tac.adx.props.AdxQuery;

/**
 * A {@link MultiReservePriceManager} over an {@link AdxQuery}.
 * 
 * @author Tomer
 * 
 */
public class UserAdTypeReservePriceManager implements
		MultiReservePriceManager<AdxQuery> {

	private Logger log = Logger
			.getLogger(DefaultAdxUserManager.class.getName());

	private Map<ReservePriceType, BasicReservePriceManager> reservePriceManagers = new HashMap<ReservePriceType, BasicReservePriceManager>();

	/**
	 * A daily baseline to calculate <b>reserve price</b> according to. The
	 * initial average reserve price is randomly chosen or given, and reserve
	 * prices in subsequent days (for ads with the similar attributes) are
	 * adaptively set to maximize the publisher�s profit.
	 */
	private double dailyBaselineAverage;
	/**
	 * Allowed variance range for generated <b>reserve prices</b> by
	 * {@link #generateReservePrice()} from the {@link #dailyBaselineAverage}.
	 */
	private final double baselineRange;
	/**
	 * Determines how much the current {@link #dailyBaselineAverage} should be
	 * taken into consideration when calculating the new
	 * {@link #dailyBaselineAverage} according to the most profitable <b>reserve
	 * price</b> generated in a single day.
	 * 
	 * @see #updateData()
	 */
	private final double updateCoefficient;

	/**
	 * @param dailyBaselineAverage
	 *            A daily baseline to calculate <b>reserve price</b> according
	 *            to. The initial average reserve price is given, and reserve
	 *            prices in subsequent days (for ads with the similar
	 *            attributes) are adaptively set to maximize the publisher�s
	 *            profit.
	 * @param baselineRange
	 *            Allowed variance range for generated <b>reserve prices</b> by
	 *            {@link #generateReservePrice()} from the
	 *            {@link #dailyBaselineAverage}.
	 * @param updateCoefficient
	 *            Determines how much the current {@link #dailyBaselineAverage}
	 *            should be taken into consideration when calculating the new
	 *            {@link #dailyBaselineAverage} according to the most profitable
	 *            <b>reserve price</b> generated in a single day.
	 */
	public UserAdTypeReservePriceManager(double dailyBaselineAverage,
			double baselineRange, double updateCoefficient) {
		this.dailyBaselineAverage = dailyBaselineAverage;
		this.baselineRange = baselineRange;
		this.updateCoefficient = updateCoefficient;
	}

	@Override
	public double generateReservePrice(AdxQuery adxQuery) {
		BasicReservePriceManager reservePriceManager = getReservePriceManager(adxQuery);
		return reservePriceManager.generateReservePrice(adxQuery);
	}

	@Override
	public void addImpressionForPrice(double reservePrice, AdxQuery adxQuery) {
		BasicReservePriceManager reservePriceManager = getReservePriceManager(adxQuery);
		reservePriceManager.addImpressionForPrice(reservePrice, adxQuery);
	}

	@Override
	public void updateData() {
		for (ReservePriceType priceType : reservePriceManagers.keySet()) {
			reservePriceManagers.get(priceType).updateData();
			double updateDailyBaselineAverage = reservePriceManagers.get(
					priceType).getDailyBaselineAverage();
			log.fine("Updated reserve price for " + priceType + " to "
					+ updateDailyBaselineAverage);
		}
	}

	@Override
	public double getDailyBaselineAverage(AdxQuery adxQuery) {
		BasicReservePriceManager reservePriceManager = getReservePriceManager(adxQuery);
		return reservePriceManager.getDailyBaselineAverage();
	}

	/**
	 * Returns the matching {@link BasicReservePriceManager} for the given
	 * {@link AdxQuery} according to its properties.
	 * 
	 * @param adxQuery
	 *            {@link AdxQuery} used for its properties.
	 * @return The matching {@link BasicReservePriceManager}.
	 */
	private synchronized BasicReservePriceManager getReservePriceManager(
			AdxQuery adxQuery) {
		ReservePriceType type = getType(adxQuery);
		if (!reservePriceManagers.containsKey(type)) {
			BasicReservePriceManager reservePriceManager = new BasicReservePriceManager(
					dailyBaselineAverage, baselineRange, updateCoefficient);
			reservePriceManagers.put(type, reservePriceManager);
		}
		BasicReservePriceManager reservePriceManager = reservePriceManagers
				.get(type);
		return reservePriceManager;
	}

	/**
	 * Returns a {@link ReservePriceType} (a pair between user type and ad type)
	 * for a given {@link AdxQuery}.
	 * 
	 * @param adxQuery
	 *            {@link AdxQuery}.
	 * @return The matching {@link ReservePriceType}.
	 */
	private static ReservePriceType getType(AdxQuery adxQuery) {
		return new ReservePriceType(adxQuery);
	}

}
