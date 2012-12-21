package tau.tac.adx.props;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.isl.transport.Transportable;
import tau.tac.adx.publishers.AdxPublisher;

/**
 * {@link PublisherCatalog} entry class. Contains a single {@link AdxPublisher}
 * 's name.
 * 
 * @author greenwald
 * 
 */
public class PublisherCatalogEntry implements Transportable {

	/**
	 * Publisher name key.
	 */
	private static final String PUBLISHER_NAME_KEY = "PUBLISHER_NAME_KEY";
	/**
	 * Publisher name.
	 */
	private String publisherName;

	/**
	 * @param publisherName
	 *            Publisher name.
	 */
	public PublisherCatalogEntry(String publisherName) {
		this.publisherName = publisherName;
	}

	/**
	 * Empty constructor.
	 */
	public PublisherCatalogEntry() {
	}

	/**
	 * @param adxPublisher
	 *            {@link AdxPublisher}.
	 */
	public PublisherCatalogEntry(AdxPublisher adxPublisher) {
		this.publisherName = adxPublisher.getName();
	}

	/**
	 * @return the publisherName
	 */
	public String getPublisherName() {
		return publisherName;
	}

	/**
	 * @param publisherName
	 *            the publisherName to set
	 */
	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}

	/**
	 * @see se.sics.isl.transport.Transportable#getTransportName()
	 */
	@Override
	public String getTransportName() {
		return getClass().getSimpleName();
	}

	/**
	 * @see se.sics.isl.transport.Transportable#read(se.sics.isl.transport.TransportReader)
	 */
	@Override
	public void read(TransportReader reader) throws ParseException {
		publisherName = reader.getAttribute(PUBLISHER_NAME_KEY, null);
	}

	/**
	 * @see se.sics.isl.transport.Transportable#write(se.sics.isl.transport.TransportWriter)
	 */
	@Override
	public void write(TransportWriter writer) {
		if (publisherName != null) {
			writer.attr(PUBLISHER_NAME_KEY, publisherName);
		}
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((publisherName == null) ? 0 : publisherName.hashCode());
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
		PublisherCatalogEntry other = (PublisherCatalogEntry) obj;
		if (publisherName == null) {
			if (other.publisherName != null)
				return false;
		} else if (!publisherName.equals(other.publisherName))
			return false;
		return true;
	}

}