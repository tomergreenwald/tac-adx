/**
 * 
 */
package tau.tac.adx.auction.data;

import java.util.Collection;
import java.util.List;

import tau.tac.adx.bids.BidInfo;

/**
 * All data needed to perform a single <b>auction</b> in the system.
 * 
 * @author greenwald
 * 
 */
public class AuctionData {

	/**
	 * {@link AuctionOrder}.
	 */
	private AuctionOrder auctionOrder;
	/**
	 * {@link AuctionPriceType}
	 */
	private AuctionPriceType auctionPriceType;
	/**
	 * {@link Collection} of {@link BidInfo}s.
	 */
	private List<BidInfo> bidInfos;
	/**
	 * Reserve price value. {@link Double#NaN} if not existing.
	 */
	private Double reservePrice;

	/**
	 * @param auctionOrder
	 * @param auctionPriceType
	 * @param bidInfos
	 * @param reservePrice
	 */
	public AuctionData(AuctionOrder auctionOrder,
			AuctionPriceType auctionPriceType,
			List<BidInfo> bidInfos, Double reservePrice) {
		super();
		this.auctionOrder = auctionOrder;
		this.auctionPriceType = auctionPriceType;
		this.bidInfos = bidInfos;
		this.reservePrice = reservePrice;
	}

	/**
	 * @return the auctionOrder
	 */
	public AuctionOrder getAuctionOrder() {
		return auctionOrder;
	}

	/**
	 * @param auctionOrder
	 *            the auctionOrder to set
	 */
	public void setAuctionOrder(AuctionOrder auctionOrder) {
		this.auctionOrder = auctionOrder;
	}

	/**
	 * @return the auctionPriceType
	 */
	public AuctionPriceType getAuctionPriceType() {
		return auctionPriceType;
	}

	/**
	 * @param auctionPriceType
	 *            the auctionPriceType to set
	 */
	public void setAuctionPriceType(AuctionPriceType auctionPriceType) {
		this.auctionPriceType = auctionPriceType;
	}

	/**
	 * @return the bidInfos
	 */
	public List<BidInfo> getBidInfos() {
		return bidInfos;
	}

	/**
	 * @param bidInfos the bidInfos to set
	 */
	public void setBidInfos(List<BidInfo> bidInfos) {
		this.bidInfos = bidInfos;
	}

	/**
	 * @return the reservePrice
	 */
	public Double getReservePrice() {
		return reservePrice;
	}

	/**
	 * @param reservePrice
	 *            the reservePrice to set
	 */
	public void setReservePrice(Double reservePrice) {
		this.reservePrice = reservePrice;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((auctionOrder == null) ? 0 : auctionOrder.hashCode());
		result = prime
				* result
				+ ((auctionPriceType == null) ? 0 : auctionPriceType.hashCode());
		result = prime * result
				+ ((reservePrice == null) ? 0 : reservePrice.hashCode());
		return result;
	}

	/* (non-Javadoc)
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
		AuctionData other = (AuctionData) obj;
		if (auctionOrder != other.auctionOrder)
			return false;
		if (auctionPriceType != other.auctionPriceType)
			return false;
		if (reservePrice == null) {
			if (other.reservePrice != null)
				return false;
		} else if (!reservePrice.equals(other.reservePrice))
			return false;
		return true;
	}


}
