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
}
