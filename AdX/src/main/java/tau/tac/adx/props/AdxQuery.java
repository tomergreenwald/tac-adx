/**
 * 
 */
package tau.tac.adx.props;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
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
	 * The publisher key.
	 */
	private static final String PUBLISHER_KEY = "publisher";

	/**
	 * Cached hashcode.
	 */
	private int hashCode;
	/**
	 * The string representing the publisher. May be <code>null</code>.
	 */
	private String publisher;

	/**
	 * @param publisher
	 */
	public AdxQuery(String publisher) {
		super();
		this.publisher = publisher;
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
	 * @see edu.umich.eecs.tac.props.AbstractTransportable#readWithLock(se.sics.isl.transport.TransportReader)
	 */
	@Override
	protected void readWithLock(TransportReader reader) throws ParseException {
		this.setPublisher(reader.getAttribute(PUBLISHER_KEY, null));
		calculateHashCode();
	}

	/**
	 * @see edu.umich.eecs.tac.props.AbstractTransportable#writeWithLock(se.sics.isl.transport.TransportWriter)
	 */
	@Override
	protected void writeWithLock(TransportWriter writer) {
		if (getPublisher() != null) {
			writer.attr(PUBLISHER_KEY, getPublisher());
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
