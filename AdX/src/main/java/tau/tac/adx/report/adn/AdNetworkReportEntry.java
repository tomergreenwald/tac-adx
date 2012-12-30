package tau.tac.adx.report.adn;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.auction.AuctionState;
import edu.umich.eecs.tac.props.AbstractTransportableEntry;

/**
 * Holds data about bids for an {@link AdNetworkKey} such as: <li>Bid count</li>
 * <li>Win count</li><li>Total cost</li>
 * 
 * @author greenwald
 */
public class AdNetworkReportEntry extends
		AbstractTransportableEntry<AdNetworkKey> {
	/** serialVersionUID. */
	private static final long serialVersionUID = 5614233336418249331L;
	/** BID_COUNT. */
	private static final String BID_COUNT = "BID_COUNT";
	/** WIN_COUNT. */
	private static final String WIN_COUNT = "WIN_COUNT";
	/** COST_COUNT. */
	private static final String COST_COUNT = "COST_COUNT";
	/**
	 * Total number of bids.
	 */
	private int bidCount;
	/**
	 * Amount of bids won.
	 */
	private int winCount;
	/**
	 * Total cost of wins
	 */
	private double cost;

	/**
	 * @param key
	 *            AdNetworkKey.
	 */
	public AdNetworkReportEntry(AdNetworkKey key) {
		setKey(key);
	}

	/**
	 * @return the bidCount
	 */
	public int getBidCount() {
		return bidCount;
	}

	/**
	 * @param bidCount
	 *            the bidCount to set
	 */
	public void setBidCount(int bidCount) {
		this.bidCount = bidCount;
	}

	/**
	 * @return the winCount
	 */
	public int getWinCount() {
		return winCount;
	}

	/**
	 * @param winCount
	 *            the winCount to set
	 */
	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}

	/**
	 * @return the cost
	 */
	public double getCost() {
		return cost;
	}

	/**
	 * @param cost
	 *            the cost to set
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	/**
	 * Reads the pricing information from the reader.
	 * 
	 * @param reader
	 *            the reader to read data from.
	 * @throws ParseException
	 *             if exception occurs when reading the mapping.
	 */
	@Override
	protected final void readEntry(final TransportReader reader)
			throws ParseException {
		bidCount = reader.getAttributeAsInt(BID_COUNT);
		winCount = reader.getAttributeAsInt(WIN_COUNT);
		cost = reader.getAttributeAsInt(COST_COUNT);
	}

	/**
	 * Writes the pricing information to the reader.
	 * 
	 * @param writer
	 *            the writer to write data to.
	 */
	@Override
	protected final void writeEntry(final TransportWriter writer) {
		writer.attr(BID_COUNT, bidCount);
		writer.attr(WIN_COUNT, winCount);
		writer.attr(COST_COUNT, cost);
	}

	/**
	 * @see edu.umich.eecs.tac.props.AbstractTransportableEntry#keyNodeName()
	 */
	@Override
	protected String keyNodeName() {
		return AdNetworkKey.class.getName();
	}

	/**
	 * Adds data related to an {@link AdxAuctionResult}.
	 * 
	 * @param auctionResult
	 *            {@link AdxAuctionResult} to update entry with.
	 */
	public void addAuctionResult(AdxAuctionResult auctionResult) {
		if (auctionResult.getAuctionState() == AuctionState.AUCTION_COPMLETED) {
			winCount++;
			cost += auctionResult.getWinningPrice();
		}
		bidCount++;
	}

}