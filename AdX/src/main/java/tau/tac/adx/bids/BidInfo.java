package tau.tac.adx.bids;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import tau.tac.adx.demand.Campaign;
import tau.tac.adx.report.adn.MarketSegment;
import edu.umich.eecs.tac.props.Ad;

/**
 * Contains information related to a <b>bid</b>, such as the bid <b>price</b>
 * and the associated {@link Ad}.
 * 
 * @author greenwald
 * 
 */
public class BidInfo implements Cloneable {

	/**
	 * Bid price.
	 */
	private final double bid;

	/**
	 * Associated {@link Bidder}.
	 */
	private final Bidder bidder;
	/**
	 * Associated {@link BidProduct}.
	 */
	private final BidProduct bidProduct;

	/** Associated {@link MarketSegment}. */
	private Set<MarketSegment> marketSegments;

	/** Associated {@link Campaign}. */
	private Campaign campaign;

	/**
	 * @param bidPrice
	 *            bid price
	 * @param bidder
	 *            {@link Bidder}
	 * @param bidProduct
	 *            {@link BidProduct}
	 * @param marketSegment
	 *            Associated {@link MarketSegment}
	 * @param campaign
	 *            Associated {@link Campaign}
	 */
	public BidInfo(double bidPrice, Bidder bidder, BidProduct bidProduct,
			Set<MarketSegment> marketSegments, Campaign campaign) {
		this.bid = bidPrice;
		this.bidder = bidder;
		this.bidProduct = bidProduct;
		this.marketSegments = marketSegments;
		this.campaign = campaign;
	}
	
	/**
	 * @param bidPrice
	 *            bid price
	 * @param bidder
	 *            {@link Bidder}
	 * @param bidProduct
	 *            {@link BidProduct}
	 * @param marketSegment
	 *            Associated {@link MarketSegment}
	 * @param campaign
	 *            Associated {@link Campaign}
	 */
	public BidInfo(double bidPrice, Bidder bidder, BidProduct bidProduct,
			MarketSegment marketSegment, Campaign campaign) {
		this.bid = bidPrice;
		this.bidder = bidder;
		this.bidProduct = bidProduct;
		this.marketSegments = new HashSet<MarketSegment>();
		this.marketSegments.add(marketSegment);
		this.campaign = campaign;
	}

	/**
	 * @return the bid
	 */
	public double getBid() {
		return bid;
	}

	/**
	 * @return the bidder
	 */
	public Bidder getBidder() {
		return bidder;
	}

	/**
	 * @return the bidProduct
	 */
	public BidProduct getBidProduct() {
		return bidProduct;
	}


	public Set<MarketSegment> getMarketSegments() {
		return marketSegments;
	}

	public void setMarketSegments(Set<MarketSegment> marketSegments) {
		this.marketSegments = marketSegments;
	}

	/**
	 * @return the campaign
	 */
	public Campaign getCampaign() {
		return campaign;
	}

	/**
	 * @param campaign
	 *            the campaign to set
	 */
	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new BidInfo(bid, bidder, bidProduct, marketSegments, campaign);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(bid);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((bidProduct == null) ? 0 : bidProduct.hashCode());
		result = prime * result + ((bidder == null) ? 0 : bidder.hashCode());
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
		BidInfo other = (BidInfo) obj;
		if (Double.doubleToLongBits(bid) != Double.doubleToLongBits(other.bid))
			return false;
		if (bidProduct == null) {
			if (other.bidProduct != null)
				return false;
		} else if (!bidProduct.equals(other.bidProduct))
			return false;
		if (bidder == null) {
			if (other.bidder != null)
				return false;
		} else if (!bidder.equals(other.bidder))
			return false;
		return true;
	}
}
