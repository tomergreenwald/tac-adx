/**
 * 
 */
package tau.tac.adx.props;

import java.text.ParseException;

import edu.umich.eecs.tac.props.AbstractTransportable;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.users.AdxUser;

/**
 * An {@link AdxUser} <b>query</b> in the simulation. Each {@link AdxQuery} is
 * composed of:
 * <ul>
 * {@link #publisher}
 * </ul>
 * 
 * @author greenwald
 * 
 */
public class AdxQuery extends AbstractTransportable {

	/**
	 * The ad type key.
	 */
	private static final String AD_TYPE_KEY = "AD_TYPE_KEY";

	/**
	 * The device key.
	 */
	private static final String DEVICE_KEY = "DEVICE_KEY";

	/**
	 * The publisher key.
	 */
	private static final String PUBLISHER_KEY = "PUBLISHER_KEY";

	/**
	 * The publisher key.
	 */
	private static final String USER_KEY = "USER_KEY";

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7210442551464879289L;

	/**
	 * Cached hashcode.
	 */
	private int hashCode;
	/**
	 * The queried {@link AdxPublisher}.
	 */
	private String publisher;

	/**
	 * {@link AdxUser} who this {@link AdxQuery} relates to.
	 */
	private int userId;

	/**
	 * Accessing {@link Device} used for the query.
	 */
	private Device device;

	/**
	 * Requested {@link AdType} for the query.
	 */
	private AdType adType;

	/**
	 * Class constructor.
	 * 
	 * @param publisher
	 *            The queried {@link AdxPublisher}.
	 * @param userId
	 *            {@link AdxUser} who this {@link AdxQuery} relates to.
	 * @param device
	 *            Accessing {@link Device} used for the query.
	 * @param adType
	 *            Requested {@link AdType} for the query.
	 */
	public AdxQuery(String publisher, int userId, Device device, AdType adType) {
		super();
		this.publisher = publisher;
		this.userId = userId;
		this.device = device;
		this.adType = adType;
		calculateHashCode();
	}

	/**
	 * Empty constructor.
	 */
	public AdxQuery() {
		super();
	}

	/**
	 * Class constructor.
	 * 
	 * @param publisher
	 *            The queried {@link AdxPublisher}.
	 * @param user
	 *            {@link AdxUser} who this {@link AdxQuery} relates to.
	 * @param device
	 *            Accessing {@link Device} used for the query.
	 * @param adType
	 *            Requested {@link AdType} for the query.
	 */
	public AdxQuery(AdxPublisher publisher, AdxUser user, Device device,
			AdType adType) {
		super();
		this.publisher = publisher.getName();
		this.userId = user.getUniqueId();
		this.device = device;
		this.adType = adType;
		calculateHashCode();
	}

	/**
	 * @return the publisher
	 */
	public String getPublisher() {
		return publisher;
	}

	/**
	 * @param publisher
	 *            the publisher to set
	 */
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	/**
	 * @return the user
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the user to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * @return the device
	 */
	public Device getDevice() {
		return device;
	}

	/**
	 * @param device
	 *            the device to set
	 */
	public void setDevice(Device device) {
		this.device = device;
	}

	/**
	 * @return the adType
	 */
	public AdType getAdType() {
		return adType;
	}

	/**
	 * @param adType
	 *            the adType to set
	 */
	public void setAdType(AdType adType) {
		this.adType = adType;
	}

	/**
	 * @see edu.umich.eecs.tac.props.AbstractTransportable#readWithLock(se.sics.isl.transport.TransportReader)
	 */
	@Override
	protected void readWithLock(TransportReader reader) throws ParseException {
		this.setAdType(AdType.valueOf(reader.getAttribute(AD_TYPE_KEY, null)));
		this.setDevice(Device.valueOf(reader.getAttribute(DEVICE_KEY, null)));
		this.setPublisher(reader.getAttribute(PUBLISHER_KEY, null));
		this.setUserId(reader.getAttributeAsInt(USER_KEY));
		calculateHashCode();
	}

	/**
	 * @see edu.umich.eecs.tac.props.AbstractTransportable#writeWithLock(se.sics.isl.transport.TransportWriter)
	 */
	@Override
	protected void writeWithLock(TransportWriter writer) {
		if (getAdType() != null) {
			writer.attr(AD_TYPE_KEY, getAdType().name());
		}
		if (getDevice() != null) {
			writer.attr(DEVICE_KEY, getDevice().name());
		}
		if (getPublisher() != null) {
			writer.attr(PUBLISHER_KEY, getPublisher());
		}
		if (getUserId() != 0) {
			writer.attr(USER_KEY, getUserId());
		}
	}

	/*	*//**
	 * @see java.lang.Object#hashCode()
	 */
	/*
	 * @Override public int hashCode() { final int prime = 31; int result = 1;
	 * result = prime * result + ((publisher == null) ? 0 :
	 * publisher.hashCode()); return result; }
	 */
	/**
	 * Returns the precalculated hash code.
	 * 
	 * @return precalculated hash code.
	 */
	@Override
	public final int hashCode() {
		return hashCode;
	}

	/**
	 * Returns the calculated hash code.
	 */
	protected final void calculateHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((publisher == null) ? 0 : publisher.hashCode());
		hashCode = result;
	}

	/**
	 * Returns a string representation of the query and the publisher.
	 * 
	 * @return a string representation of the query and the publisher.
	 */
	@Override
	public final String toString() {
		return String.format("(%s (%s))", this.getClass().getSimpleName(),
				getPublisher());
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
		AdxQuery other = (AdxQuery) obj;
		if (publisher == null) {
			if (other.publisher != null)
				return false;
		} else if (!publisher.equals(other.publisher))
			return false;
		return true;
	}

}
