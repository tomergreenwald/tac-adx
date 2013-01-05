/**
 * 
 */
package tau.tac.adx.auction.data;

import java.util.Collection;

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
	AuctionOrder auctionOrder;
	/**
	 * {@link AuctionPriceType}
	 */
	AuctionPriceType auctionPriceType;
	/**
	 * {@link Collection} of {@link BidInfo}s.
	 */
	Collection<BidInfo> bidInfoCollection;
	/**
	 * Reserve price value. {@link Double#NaN} if not existing.
	 */
	Double reservePrice;

	/**
	 * @param auctionOrder
	 * @param auctionPriceType
	 * @param bidInfoCollection
	 * @param reservePrice
	 */
	public AuctionData(AuctionOrder auctionOrder,
			AuctionPriceType auctionPriceType,
			Collection<BidInfo> bidInfoCollection, Double reservePrice) {
		super();
		this.auctionOrder = auctionOrder;
		this.auctionPriceType = auctionPriceType;
		this.bidInfoCollection = bidInfoCollection;
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
	 * @return the bidInfoCollection
	 */
	public Collection<BidInfo> getBidInfoCollection() {
		return bidInfoCollection;
	}

	/**
	 * @param bidInfoCollection
	 *            the bidInfoCollection to set
	 */
	public void setBidInfoCollection(Collection<BidInfo> bidInfoCollection) {
		this.bidInfoCollection = bidInfoCollection;
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

	/**
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
		result = prime
				* result
				+ ((bidInfoCollection == null) ? 0 : bidInfoCollection
						.hashCode());
		result = prime * result
				+ ((reservePrice == null) ? 0 : reservePrice.hashCode());
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
		AuctionData other = (AuctionData) obj;
		if (auctionOrder != other.auctionOrder)
			return false;
		if (auctionPriceType != other.auctionPriceType)
			return false;
		if (bidInfoCollection == null) {
			if (other.bidInfoCollection != null)
				return false;
		} else if (!bidInfoCollection.equals(other.bidInfoCollection))
			return false;
		if (reservePrice == null) {
			if (other.reservePrice != null)
				return false;
		} else if (!reservePrice.equals(other.reservePrice))
			return false;
		return true;
	}

}
