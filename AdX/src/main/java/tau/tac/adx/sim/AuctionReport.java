/**
 * 
 */
package tau.tac.adx.sim;

import java.text.ParseException;

import edu.umich.eecs.tac.props.Ranking;
import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.isl.transport.Transportable;
import tau.tac.adx.props.AdxQuery;

/**
 * @author Tomer
 *
 */
public class AuctionReport implements Transportable {

	/** FIRST_BID_KEY. */
	private static final String FIRST_BID_KEY = "FIRST_BID_KEY";
	/** FIRST_BID_KEY. */
	private static final String SECOND_BID_KEY = "SECOND_BID_KEY";
	/** RESERVE_PRICE_KEY. */
	private static final String RESERVE_PRICE_KEY = "RESERVE_PRICE_KEY";

	private double firstBid;
	private double secondBid;
	private double reservePrice;
	private AdxQuery adxQuery;

	/**
	 * @param firstBid
	 * @param secondBid
	 * @param reservePrice
	 * @param adxQuery
	 */
	public AuctionReport(double firstBid, double secondBid,
			double reservePrice, AdxQuery adxQuery) {
		super();
		this.firstBid = firstBid;
		this.secondBid = secondBid;
		this.reservePrice = reservePrice;
		this.adxQuery = adxQuery;
	}

	/**
	 * @return the firstBid
	 */
	public double getFirstBid() {
		return firstBid;
	}

	/**
	 * @param firstBid the firstBid to set
	 */
	public void setFirstBid(double firstBid) {
		this.firstBid = firstBid;
	}

	/**
	 * @return the secondBid
	 */
	public double getSecondBid() {
		return secondBid;
	}

	/**
	 * @param secondBid the secondBid to set
	 */
	public void setSecondBid(double secondBid) {
		this.secondBid = secondBid;
	}

	/**
	 * @return the reservePrice
	 */
	public double getReservePrice() {
		return reservePrice;
	}

	/**
	 * @param reservePrice the reservePrice to set
	 */
	public void setReservePrice(double reservePrice) {
		this.reservePrice = reservePrice;
	}

	/**
	 * @return the adxQuery
	 */
	public AdxQuery getAdxQuery() {
		return adxQuery;
	}

	/**
	 * @param adxQuery the adxQuery to set
	 */
	public void setAdxQuery(AdxQuery adxQuery) {
		this.adxQuery = adxQuery;
	}

	/**
	 * Constructor.
	 */
	public AuctionReport() {
		super();
	}

	@Override
	public String getTransportName() {
		return getClass().getSimpleName();
	}

	@Override
	public void read(TransportReader reader) throws ParseException {
		firstBid = reader.getAttributeAsDouble(FIRST_BID_KEY);
		secondBid = reader.getAttributeAsDouble(SECOND_BID_KEY);
		reservePrice = reader.getAttributeAsDouble(RESERVE_PRICE_KEY);
		if (reader.nextNode(AdxQuery.class.getSimpleName(), false)) {
            adxQuery = (AdxQuery) reader.readTransportable();
        }
	}

	@Override
	public void write(TransportWriter writer) {
		writer.attr(FIRST_BID_KEY, firstBid).attr(SECOND_BID_KEY, secondBid)
				.attr(RESERVE_PRICE_KEY, reservePrice).write(adxQuery);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((adxQuery == null) ? 0 : adxQuery.hashCode());
		long temp;
		temp = Double.doubleToLongBits(firstBid);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(reservePrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(secondBid);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		AuctionReport other = (AuctionReport) obj;
		if (adxQuery == null) {
			if (other.adxQuery != null)
				return false;
		} else if (!adxQuery.equals(other.adxQuery))
			return false;
		if (Double.doubleToLongBits(firstBid) != Double
				.doubleToLongBits(other.firstBid))
			return false;
		if (Double.doubleToLongBits(reservePrice) != Double
				.doubleToLongBits(other.reservePrice))
			return false;
		if (Double.doubleToLongBits(secondBid) != Double
				.doubleToLongBits(other.secondBid))
			return false;
		return true;
	}

}
