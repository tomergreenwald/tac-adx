package tau.tac.adx.bids;

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

	/**
	 * @param bidPrice
	 *            bid price
	 * @param bidder
	 *            {@link Bidder}
	 * @param bidProduct
	 *            {@link BidProduct}
	 */
	public BidInfo(double bidPrice, Bidder bidder, BidProduct bidProduct) {
		this.bid = bidPrice;
		this.bidder = bidder;
		this.bidProduct = bidProduct;
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

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new BidInfo(bid, bidder, bidProduct);
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
