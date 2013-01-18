package tau.tac.adx.report.adn;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.isl.transport.Transportable;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.publishers.AdxPublisher;

/**
 * {@link AdNetworkReport}'s key. Each is a single combination of properties to
 * holds data for in the <b>report</b>.
 * 
 * @author greenwald
 * 
 */
public class AdNetworkKey implements Transportable {

	/** MARKET_SEGMENT_KEY. */
	private static final String MARKET_SEGMENT_KEY = "MARKET_SEGMENT_KEY";
	/** PUBLISHER_KEY. */
	private static final String PUBLISHER_KEY = "PUBLISHER_KEY";
	/** DEVICE_KEY. */
	private static final String DEVICE_KEY = "DEVICE_KEY";
	/** AD_TYPE_KEY. */
	private static final String AD_TYPE_KEY = "AD_TYPE_KEY";

	/**
	 * {@link MarketSegment}.
	 */
	private MarketSegment segment;

	/**
	 * {@link AdxPublisher}'s name.
	 */
	private String publisher;

	/**
	 * {@link Device}.
	 */
	private Device device;

	/**
	 * {@link AdType}.
	 */
	private AdType adType;

	/**
	 * @param segment
	 *            {@link MarketSegment}.
	 * @param publisher
	 *            {@link AdxPublisher}'s name.
	 * @param device
	 *            {@link Device}.
	 * @param adType
	 *            {@link AdType}.
	 */
	public AdNetworkKey(MarketSegment segment, String publisher, Device device,
			AdType adType) {
		super();
		this.segment = segment;
		this.publisher = publisher;
		this.device = device;
		this.adType = adType;
	}

	public AdNetworkKey() {
	}

	/**
	 * @return the segment
	 */
	public MarketSegment getSegment() {
		return segment;
	}

	/**
	 * @param segment
	 *            the segment to set
	 */
	public void setSegment(MarketSegment segment) {
		this.segment = segment;
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
		segment = MarketSegment.valueOf(reader.getAttribute(MARKET_SEGMENT_KEY,
				null));
		publisher = reader.getAttribute(PUBLISHER_KEY, null);
		device = Device.valueOf(reader.getAttribute(DEVICE_KEY, null));
		adType = AdType.valueOf(reader.getAttribute(AD_TYPE_KEY, null));

	}

	/**
	 * @see se.sics.isl.transport.Transportable#write(se.sics.isl.transport.TransportWriter)
	 */
	@Override
	public void write(TransportWriter writer) {
		if (segment != null) {
			writer.attr(MARKET_SEGMENT_KEY, segment.toString());
		}
		if (publisher != null) {
			writer.attr(PUBLISHER_KEY, publisher);
		}
		if (device != null) {
			writer.attr(DEVICE_KEY, device.toString());
		}
		if (adType != null) {
			writer.attr(AD_TYPE_KEY, adType.toString());
		}
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((adType == null) ? 0 : adType.hashCode());
		result = prime * result + ((device == null) ? 0 : device.hashCode());
		result = prime * result
				+ ((publisher == null) ? 0 : publisher.hashCode());
		result = prime * result + ((segment == null) ? 0 : segment.hashCode());
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
		AdNetworkKey other = (AdNetworkKey) obj;
		if (adType != other.adType)
			return false;
		if (device != other.device)
			return false;
		if (publisher == null) {
			if (other.publisher != null)
				return false;
		} else if (!publisher.equals(other.publisher))
			return false;
		if (segment != other.segment)
			return false;
		return true;
	}

}
