package tau.tac.adx.publishers.reserve;

import java.util.Set;

import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.report.adn.MarketSegment;

/**
 * Reserve price pair used to classify different reserve price to different
 * users and ads.
 * 
 * @author Tomer
 * 
 */
class ReservePriceType {

	/** {@link Set} of {@link MarketSegment}s. */
	private Set<MarketSegment> marketSegment;
	/** {@link AdType}. */
	private AdType adType;

	/**
	 * Constructor from {@link AdxQuery}.
	 * @param adxQuery {@link AdxQuery}.
	 * @param reservePriceManager TODO
	 */
	public ReservePriceType(AdxQuery adxQuery) {
		this.marketSegment = adxQuery.getMarketSegments();
		this.adType = adxQuery.getAdType();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((adType == null) ? 0 : adType.hashCode());
		result = prime * result
				+ ((marketSegment == null) ? 0 : marketSegment.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReservePriceType other = (ReservePriceType) obj;
		if (adType != other.adType)
			return false;
		if (marketSegment == null) {
			if (other.marketSegment != null)
				return false;
		} else if (!marketSegment.equals(other.marketSegment))
			return false;
		return true;
	}

}