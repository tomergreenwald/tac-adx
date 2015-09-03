package tau.tac.adx.report.publisher;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalogEntry;
import tau.tac.adx.publishers.AdxPublisher;
import edu.umich.eecs.tac.props.AbstractTransportableEntry;

/**
 * Query report entry holds the impressions, clicks, cost, average position, and
 * ad displayed by the advertiser for each query class during the period as well
 * as the positions and displayed ads of all advertisers during the period.
 * 
 * @author Patrick Jordan, Lee Callender
 * @author greenwald
 */
public class AdxPublisherReportEntry extends
		AbstractTransportableEntry<PublisherCatalogEntry> {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = -6459026589028276792L;

	/**
	 * The price entry transport name.
	 */
	private static final String AD_TYPE_ORIENTATION_ENTRY_TRANSPORT_NAME = "AdTypeOrientation";

	/**
	 * The {@link PublisherCatalogEntry} transport name.
	 */
	private static final String PUBLISHER_CATALOG_NAME_ENTRY_TRANSPORT_NAME = "PublisherCatalogEntry";
	
	/**
	 * The popularity transport name.
	 */
	private static final String POPULARITY_TRANSPORT_NAME = "PopularityEntry";

	/** Reserve price baseline key. */
	private static String RESERVE_PRICE_BASELINE_KEY = "RESERVE_PRICE_BASELINE_KEY";
	
	/** Active reserve price for the report. */
	private double reservePriceBaseline;

	/**
	 * {@link AdxPublisher}'s popularity - number of visits to the publisher.
	 */
	private int popularity;
	/**
	 * The ad shown.
	 */
	private Map<AdType, Integer> adTypeOrientation;

	/**
	 * @param key
	 */
	public AdxPublisherReportEntry(PublisherCatalogEntry key) {
		super();
		adTypeOrientation = new HashMap<AdType, Integer>();
		adTypeOrientation.put(AdType.text, 0);
		adTypeOrientation.put(AdType.video, 0);
		setKey(key);
	}

	public AdxPublisherReportEntry() {
		super();
		adTypeOrientation = new HashMap<AdType, Integer>();
		adTypeOrientation.put(AdType.text, 0);
		adTypeOrientation.put(AdType.video, 0);

	}

	/**
	 * @return the publisherName
	 */
	public String getPublisherName() {
		return getKey().getPublisherName();
	}

	/**
	 * @return the popularity of the publisher - the number of times it was visited by users
	 */
	public int getPopularity() {
		return popularity;
	}

	/**
	 * @return the adTypeOrientation
	 */
	public Map<AdType, Integer> getAdTypeOrientation() {
		return adTypeOrientation;
	}

	/**
	 * @param popularity
	 *            the popularity to set
	 */
	public void setPopularity(int popularity) {
		this.popularity = popularity;
	}

	/**
	 * @param adTypeOrientation
	 *            the adTypeOrientation to set
	 */
	public void setAdTypeOrientation(Map<AdType, Integer> adTypeOrientation) {
		this.adTypeOrientation = adTypeOrientation;
	}

	/**
	 * @return the reservePriceBaseline
	 */
	public double getReservePriceBaseline() {
		return reservePriceBaseline;
	}

	/**
	 * @param reservePriceBaseline the reservePriceBaseline to set
	 */
	public void setReservePriceBaseline(double reservePriceBaseline) {
		this.reservePriceBaseline = reservePriceBaseline;
	}

	/**
	 * Reads the pricing information from the reader.
	 * 
	 * @param reader
	 *            the reader to read data from.
	 * @throws ParseException
	 *             if exception occurs when reading the mapping.
	 */
	@Override
	protected final void readEntry(final TransportReader reader)
			throws ParseException {
		adTypeOrientation.clear();
		String attribute = reader
				.getAttribute(PUBLISHER_CATALOG_NAME_ENTRY_TRANSPORT_NAME);
		reservePriceBaseline = reader.getAttributeAsDouble(RESERVE_PRICE_BASELINE_KEY);
		setKey(new PublisherCatalogEntry(attribute));
		popularity = reader.getAttributeAsInt(POPULARITY_TRANSPORT_NAME);
		while (reader.nextNode(AD_TYPE_ORIENTATION_ENTRY_TRANSPORT_NAME, false)) {
			readAdTypeEntry(reader);
		}

	}

	/**
	 * Writes the pricing information to the reader.
	 * 
	 * @param writer
	 *            the writer to write data to.
	 */
	@Override
	protected final void writeEntry(final TransportWriter writer) {
		writer.attr(PUBLISHER_CATALOG_NAME_ENTRY_TRANSPORT_NAME, getKey()
				.getPublisherName());
		writer.attr(POPULARITY_TRANSPORT_NAME, popularity);
		writer.attr(RESERVE_PRICE_BASELINE_KEY, reservePriceBaseline);

		for (Entry<AdType, Integer> entry : adTypeOrientation.entrySet()) {
			writeAdTypeEntry(writer, entry.getValue(), entry.getKey());
		}
	}

	/**
	 * Writes the {@link AdType} entry to the writer.
	 * 
	 * @param writer
	 *            the writer to write data to.
	 * @param orientation
	 *            the {@link AdType} popularity
	 * @param adType
	 *            the {@link AdType}
	 */
	protected final void writeAdTypeEntry(final TransportWriter writer,
			final Integer orientation, final AdType adType) {
		writer.node(AD_TYPE_ORIENTATION_ENTRY_TRANSPORT_NAME);
		writer.attr("Oreintation", orientation);
		writer.attr("AdType", adType.toString());
		writer.endNode(AD_TYPE_ORIENTATION_ENTRY_TRANSPORT_NAME);
	}

	/**
	 * Reads the {@link AdType} entry from the reader.
	 * 
	 * @param reader
	 *            the reader to read data from.
	 * @throws ParseException
	 *             if exception occurs when a price entry
	 */
	protected final void readAdTypeEntry(final TransportReader reader)
			throws ParseException {
		reader.enterNode();
		int orientation = reader.getAttributeAsInt("Oreintation", 0);
		AdType adType = AdType.valueOf(reader.getAttribute("AdType"));
		adTypeOrientation.put(adType, orientation);
		reader.exitNode();
	}

	/**
	 * @see edu.umich.eecs.tac.props.AbstractTransportableEntry#keyNodeName()
	 */
	@Override
	protected String keyNodeName() {
		return AD_TYPE_ORIENTATION_ENTRY_TRANSPORT_NAME;
	}

	/**
	 * Adds data related to a publisher query.
	 * 
	 * @param query
	 *            {@link AdxQuery}.
	 */
	public void addQuery(AdxQuery query) {
		this.popularity++;
		adTypeOrientation.put(query.getAdType(),
				adTypeOrientation.get(query.getAdType()) + 1);
	}

	/**
	 * Returns the string representation of the sales report entry.
	 * 
	 * @return the string representation of the sales report entry.
	 */
	@Override
	public final String toString() {
		return String.format(
				"(publisher: %s popularity: %d video: %d text: %d)",
				getPublisherName(), getPopularity(), getAdTypeOrientation()
						.get(AdType.video),
				getAdTypeOrientation().get(AdType.text));
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((adTypeOrientation == null) ? 0 : adTypeOrientation
						.hashCode());
		result = prime * result + popularity;
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
		AdxPublisherReportEntry other = (AdxPublisherReportEntry) obj;
		if (adTypeOrientation == null) {
			if (other.adTypeOrientation != null)
				return false;
		} else if (!adTypeOrientation.equals(other.adTypeOrientation))
			return false;
		if (popularity != other.popularity)
			return false;
		return true;
	}

}