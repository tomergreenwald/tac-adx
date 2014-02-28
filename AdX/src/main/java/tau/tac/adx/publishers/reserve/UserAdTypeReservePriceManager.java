/**
 * 
 */
package tau.tac.adx.publishers.reserve;

import java.util.HashMap;
import java.util.Map;

import tau.tac.adx.props.AdxQuery;

/**
 * A {@link MultiReservePriceManager} over an {@link AdxQuery}.
 * 
 * @author Tomer
 * 
 */
public class UserAdTypeReservePriceManager implements
		MultiReservePriceManager<AdxQuery> {

	private Map<ReservePriceType, ReservePriceManager> reservePriceManagers = new HashMap<ReservePriceType, ReservePriceManager>();

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
	 * @see #updateDailyBaselineAverage()
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
		ReservePriceManager reservePriceManager = getReservePriceManager(adxQuery);
		return reservePriceManager.generateReservePrice();
	}

	@Override
	public void addImpressionForPrice(double reservePrice, AdxQuery adxQuery) {
		ReservePriceManager reservePriceManager = getReservePriceManager(adxQuery);
		reservePriceManager.addImpressionForPrice(reservePrice);
	}

	@Override
	public void updateDailyBaselineAverage() {
		for(ReservePriceManager reservePriceManager : reservePriceManagers.values()){
			reservePriceManager.updateDailyBaselineAverage();
		}
	}

	@Override
	public double getDailyBaselineAverage(AdxQuery adxQuery) {
		ReservePriceManager reservePriceManager = getReservePriceManager(adxQuery);
		return reservePriceManager.getDailyBaselineAverage();
	}

	/**
	 * Returns the matching {@link ReservePriceManager} for the given
	 * {@link AdxQuery} according to its properties.
	 * 
	 * @param adxQuery
	 *            {@link AdxQuery} used for its properties.
	 * @return The matching {@link ReservePriceManager}.
	 */
	private ReservePriceManager getReservePriceManager(AdxQuery adxQuery) {
		ReservePriceType type = getType(adxQuery);
		if (!reservePriceManagers.containsKey(type)) {
			ReservePriceManager reservePriceManager = new ReservePriceManager(
					dailyBaselineAverage, baselineRange, updateCoefficient);
			;
			reservePriceManagers.put(type, reservePriceManager);
		}
		ReservePriceManager reservePriceManager = reservePriceManagers
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
	private ReservePriceType getType(AdxQuery adxQuery) {
		return new ReservePriceType(adxQuery);
	}

}
