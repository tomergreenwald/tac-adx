package tau.tac.adx.bids;

import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.users.AdxUser;

/**
 * Contains data about who a given
 * {@link edu.umich.eecs.tac.props.BidBundle.BidEntry} is targeted at.
 * 
 * @author greenwald
 * 
 */
public class BidTarget {
	/**
	 * Targeted {@link AdxUser} by the {@link BidTarget}.
	 */
	private final AdxUser adxUser;
	/**
	 * Targeted {@link AdxPublisher} by the {@link BidTarget}.
	 */
	private final AdxPublisher adxPublisher;

	/**
	 * @param adxUser
	 * @param adxPublisher
	 */
	public BidTarget(AdxUser adxUser, AdxPublisher adxPublisher) {
		super();
		this.adxUser = adxUser;
		this.adxPublisher = adxPublisher;
	}

	/**
	 * @return the adxUser
	 */
	public AdxUser getAdxUser() {
		return adxUser;
	}

	/**
	 * @return the adxPublisher
	 */
	public AdxPublisher getAdxPublisher() {
		return adxPublisher;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((adxPublisher == null) ? 0 : adxPublisher.hashCode());
		result = prime * result + ((adxUser == null) ? 0 : adxUser.hashCode());
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
		BidTarget other = (BidTarget) obj;
		if (adxPublisher == null) {
			if (other.adxPublisher != null)
				return false;
		} else if (!adxPublisher.equals(other.adxPublisher))
			return false;
		if (adxUser == null) {
			if (other.adxUser != null)
				return false;
		} else if (!adxUser.equals(other.adxUser))
			return false;
		return true;
	}

}
