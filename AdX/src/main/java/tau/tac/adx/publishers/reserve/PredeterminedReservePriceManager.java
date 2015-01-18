/**
 * 
 */
package tau.tac.adx.publishers.reserve;

import tau.tac.adx.props.AdxQuery;

/**
 * @author Tomer Greenwald
 *
 */
public class PredeterminedReservePriceManager implements
		MultiReservePriceManager<AdxQuery> {

	/**
	 * @see tau.tac.adx.publishers.reserve.MultiReservePriceManager#generateReservePrice(java.lang.Object)
	 */
	@Override
	public double generateReservePrice(AdxQuery t) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see tau.tac.adx.publishers.reserve.MultiReservePriceManager#addImpressionForPrice(double, java.lang.Object)
	 */
	@Override
	public void addImpressionForPrice(double reservePrice, AdxQuery t) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see tau.tac.adx.publishers.reserve.MultiReservePriceManager#updateDailyBaselineAverage()
	 */
	@Override
	public void updateDailyBaselineAverage() {
		return;
	}

	/**
	 * @see tau.tac.adx.publishers.reserve.MultiReservePriceManager#getDailyBaselineAverage(java.lang.Object)
	 */
	@Override
	public double getDailyBaselineAverage(AdxQuery t) {
		return 0;
	}

}
