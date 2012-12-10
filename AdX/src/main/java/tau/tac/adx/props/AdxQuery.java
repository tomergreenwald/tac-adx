/**
 * 
 */
package tau.tac.adx.props;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.users.AdxUser;
import edu.umich.eecs.tac.props.AbstractTransportable;

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
	private AdxPublisher publisher;

	/**
	 * {@link AdxUser} who this {@link AdxQuery} relates to.
	 */
	private AdxUser user;

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
		this.publisher = publisher;
		this.user = user;
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
	 * @return the publisher
	 */
	public AdxPublisher getPublisher() {
		return publisher;
	}

	/**
	 * @param publisher
	 *            the publisher to set
	 */
	public void setPublisher(AdxPublisher publisher) {
		this.publisher = publisher;
	}

	public AdxUser getUser() {
		return user;
	}

	public void setUser(AdxUser user) {
		this.user = user;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public AdType getAdType() {
		return adType;
	}

	public void setAdType(AdType adType) {
		this.adType = adType;
	}

	/**
	 * @see edu.umich.eecs.tac.props.AbstractTransportable#readWithLock(se.sics.isl.transport.TransportReader)
	 */
	@Override
	protected void readWithLock(TransportReader reader) throws ParseException {
		AdxQuery query = (AdxQuery) reader.readTransportable();
		this.adType = query.adType;
		this.device = query.device;
		this.publisher=query.publisher;
		this.user=query.user;
		calculateHashCode();
	}

	/**
	 * @see edu.umich.eecs.tac.props.AbstractTransportable#writeWithLock(se.sics.isl.transport.TransportWriter)
	 */
	@Override
	protected void writeWithLock(TransportWriter writer) {
		writer.write(this);
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
