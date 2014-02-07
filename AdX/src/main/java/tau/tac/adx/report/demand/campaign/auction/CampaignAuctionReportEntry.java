package tau.tac.adx.report.demand.campaign.auction;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import edu.umich.eecs.tac.props.AbstractTransportableEntry;

/**
 * 
 * @author Tomer Greenwald
 * 
 */
public class CampaignAuctionReportEntry extends
		AbstractTransportableEntry<CampaignAuctionReportKey> {

	private static final long serialVersionUID = 2856461805359063646L;
	
	/**Actual bid key. */
	private static final String ACTUAL_BID_KEY = "ACTUAL_BID_KEY";
	/** Effective bid key. */
	private static final String EFFECTIVE_BID_KEY = "EFFECTIVE_BID_KEY";

	/** Actual bid placed by the ad network. */
	private double actualBid;
	/** Effective bid (with regards to ad network's rating). */
	private double effectiveBid;

	
	/**
	 * Default constructor (for serialization purposes).
	 */
	public CampaignAuctionReportEntry() {
		super();
	}
	

	/**
	 * Constructor.
	 * @param key {@link CampaignAuctionReportKey}
	 */
	public CampaignAuctionReportEntry(CampaignAuctionReportKey key) {
		super();
		setKey(key);
	}
	
	

	/**
	 * @param actualBid Actual bid placed by the ad network.
	 * @param effctiveBid Effective bid (with regards to ad network's rating).
	 */
	public CampaignAuctionReportEntry(double actualBid, double effctiveBid) {
		super();
		this.actualBid = actualBid;
		this.effectiveBid = effctiveBid;
	}

	/**
	 * @return the actualBid
	 */
	public double getActualBid() {
		return actualBid;
	}

	/**
	 * @param actualBid the actualBid to set
	 */
	public void setActualBid(double actualBid) {
		this.actualBid = actualBid;
	}

	/**
	 * @return the effctiveBid
	 */
	public double getEffctiveBid() {
		return effectiveBid;
	}

	/**
	 * @param effctiveBid the effctiveBid to set
	 */
	public void setEffctiveBid(double effctiveBid) {
		this.effectiveBid = effctiveBid;
	}

	/**
	 * Reads the actual and effective bids from the reader.
	 * 
	 * @param reader
	 *            the reader to read data from.
	 * @throws ParseException
	 *             if exception occurs when reading the mapping.
	 */
	@Override
	protected final void readEntry(final TransportReader reader)
			throws ParseException {
		actualBid = reader.getAttributeAsDouble(ACTUAL_BID_KEY);
		effectiveBid = reader.getAttributeAsDouble(EFFECTIVE_BID_KEY);
	}

	/**
	 * Writes the pricing information to the reader.
	 * 
	 * @param writer
	 *            the writer to write data to.
	 */
	@Override
	protected final void writeEntry(final TransportWriter writer) {
		writer.attr(ACTUAL_BID_KEY, actualBid);
		writer.attr(EFFECTIVE_BID_KEY, effectiveBid);
	}

	/**
	 * @see edu.umich.eecs.tac.props.AbstractTransportableEntry#keyNodeName()
	 */
	@Override
	protected String keyNodeName() {
		return CampaignAuctionReportKey.class.getName();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(actualBid);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(effectiveBid);
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
		CampaignAuctionReportEntry other = (CampaignAuctionReportEntry) obj;
		if (Double.doubleToLongBits(actualBid) != Double
				.doubleToLongBits(other.actualBid))
			return false;
		if (Double.doubleToLongBits(effectiveBid) != Double
				.doubleToLongBits(other.effectiveBid))
			return false;
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CampaignAuctionReportEntry [actualBid=" + actualBid
				+ ", effectiveBid=" + effectiveBid + "]";
	}

}
