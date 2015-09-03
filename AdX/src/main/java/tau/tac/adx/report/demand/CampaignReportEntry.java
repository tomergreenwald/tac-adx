package tau.tac.adx.report.demand;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import tau.tac.adx.demand.CampaignStats;
import tau.tac.adx.report.adn.AdNetworkKey;
import edu.umich.eecs.tac.props.AbstractTransportableEntry;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class CampaignReportEntry extends
		AbstractTransportableEntry<CampaignReportKey> {

	private static final long serialVersionUID = 2856461805359063656L;
	/** CAMPAIGN_STATS. */
	private static final String TGT_IMPS = "TGT_IMPS";
	private static final String NTG_IMPS = "NTG_IMPS";
	private static final String COST = "COST";

	private CampaignStats stats = new CampaignStats(0, 0, 0);

	/**
	 * @param key
	 *            AdNetworkKey.
	 */
	public CampaignReportEntry(CampaignReportKey key) {
		setKey(key);
	}

	/**
	 */
	public CampaignReportEntry() {
	}

	/**
	 * @return the bidCount
	 */
	public CampaignStats getCampaignStats() {
		return stats;
	}

	/**
	 * @param bidCount
	 *            the bidCount to set
	 */
	public void setCampaignStats(CampaignStats other) {
		stats.setValues(other);
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
		stats = new CampaignStats(reader.getAttributeAsDouble(TGT_IMPS),
				reader.getAttributeAsDouble(NTG_IMPS),
				reader.getAttributeAsDouble(COST));

		reader.nextNode(CampaignReportKey.class.getCanonicalName(), true);
		setKey((CampaignReportKey) reader.readTransportable());
	}

	/**
	 * Writes the pricing information to the reader.
	 * 
	 * @param writer
	 *            the writer to write data to.
	 */
	@Override
	protected final void writeEntry(final TransportWriter writer) {
		writer.attr(TGT_IMPS, stats.getTargetedImps());
		writer.attr(NTG_IMPS, stats.getOtherImps());
		writer.attr(COST, stats.getCost());
		writer.write(getKey());
	}

	/**
	 * @see edu.umich.eecs.tac.props.AbstractTransportableEntry#keyNodeName()
	 */
	@Override
	protected String keyNodeName() {
		return AdNetworkKey.class.getName();
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
		CampaignReportEntry other = (CampaignReportEntry) obj;
		if (stats.getTargetedImps() != other.stats.getTargetedImps())
			return false;

		if (stats.getOtherImps() != other.stats.getOtherImps())
			return false;

		if (stats.getCost() != other.stats.getCost())
			return false;

		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CampaignReportEntry [tgt imps=" + stats.getTargetedImps()
				+ ", ntg imps=" + stats.getOtherImps() + ", cost="
				+ stats.getCost() + "]";
	}

}
