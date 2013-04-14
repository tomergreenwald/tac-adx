package tau.tac.adx.report.query;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import edu.umich.eecs.tac.props.AbstractQueryEntry;
import edu.umich.eecs.tac.props.Ad;

/**
 * Query report entry holds the impressions, clicks, cost, average position, and
 * ad displayed by the advertiser for each query class during the period as well
 * as the positions and displayed ads of all advertisers during the period.
 * 
 * @author Patrick Jordan, Lee Callender
 * @author greenwald
 */
public class AdxQueryReportEntry extends AbstractQueryEntry {
	/**
	 * The regular impressions.
	 */
	private int impressions;
	/**
	 * The cost.
	 */
	private double cost;
	/**
	 * The ad shown.
	 */
	private Ad ad;

	/**
	 * Returns the total number of impressions.
	 * 
	 * @return the total number of impressions.
	 */
	public final int getImpressions() {
		return impressions;
	}

	/**
	 * Sets the total number of impressions.
	 * 
	 * @param impressions
	 *            the total number of impressions.
	 */
	public final void setImpressions(final int impressions) {
		this.impressions = impressions;
	}

	/**
	 * Adds the impressions.
	 * 
	 * @param impressionCount
	 *            Amount of impressions to add.
	 */
	final void addImpressions(final int impressionCount) {
		this.impressions += impressionCount;
	}

	/**
	 * Returns the total cost.
	 * 
	 * @return the total cost.
	 */
	public final double getCost() {
		return cost;
	}

	/**
	 * Sets the total cost.
	 * 
	 * @param cost
	 *            the total cost.
	 */
	final void setCost(final double cost) {
		this.cost = cost;
	}

	/**
	 * Adds the cost to the total cost.
	 * 
	 * @param cost
	 *            the cost.
	 */
	final void addCost(final double cost) {
		this.cost += cost;
	}

	/**
	 * Returns the shown ad.
	 * 
	 * @return the shown ad
	 */
	public final Ad getAd() {
		return ad;
	}

	/**
	 * Sets the shown ad.
	 * 
	 * @param ad
	 *            the shown ad
	 */
	public final void setAd(final Ad ad) {
		this.ad = ad;
	}

	/**
	 * Returns the ad shown by an advertiser.
	 * 
	 * @param advertiser
	 *            the advertiser
	 * @return the ad shown by an advertiser
	 */
	public final Ad getAd(final String advertiser) {
		return ad;
	}

	/**
	 * Reads the query report entry information.
	 * 
	 * @param reader
	 *            the reader to read the state in from.
	 * @throws ParseException
	 *             if an exception occured the query report entry information.
	 */
	@Override
	protected final void readEntry(final TransportReader reader)
			throws ParseException {
		this.impressions = reader.getAttributeAsInt("regularImpressions", 0);
		this.cost = reader.getAttributeAsDouble("cost", 0.0);
		if (reader.nextNode(Ad.class.getSimpleName(), false)) {
			this.ad = (Ad) reader.readTransportable();
		}
	}

	/**
	 * Writes the query report entry information to the writer.
	 * 
	 * @param writer
	 *            the writer to write the entry state to
	 */
	@Override
	protected final void writeEntry(final TransportWriter writer) {
		writer.attr("regularImpressions", impressions);
		writer.attr("cost", cost);
		if (ad != null) {
			writer.write(ad);
		}
	}

	/**
	 * Returns the string representation of the query report entry.
	 * 
	 * @return the string representation of the query report entry.
	 */
	@Override
	public final String toString() {
		return String
				.format("(%s impressions: %d cost: %s)", impressions, cost);
	}
}