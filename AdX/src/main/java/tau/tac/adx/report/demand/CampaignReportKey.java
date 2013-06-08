package tau.tac.adx.report.demand;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.isl.transport.Transportable;
import tau.tac.adx.report.adn.MarketSegment;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class CampaignReportKey implements Transportable {
	/** CAMPAIGN_ID_KEY. */
	private static final String CAMPAIGN_ID_KEY = "CAMPAIGN_ID_KEY";
	/**
	 * {@link MarketSegment}.
	 */
	private Integer campaignId;

	public CampaignReportKey(Integer id) {
		super();
		this.campaignId = id;
	}

	public CampaignReportKey() {
	}

	/**
	 * @return the segment
	 */
	public Integer getCampaignId() {
		return campaignId;
	}

	/**
	 * @param segment
	 *            the segment to set
	 */
	public void setCampaignId(Integer id) {
		this.campaignId = id;
	}

	/**
	 * @see se.sics.isl.transport.Transportable#getTransportName()
	 */
	@Override
	public String getTransportName() {
		return getClass().getName();
	}

	/**
	 * @see se.sics.isl.transport.Transportable#read(se.sics.isl.transport.TransportReader)
	 */
	@Override
	public void read(TransportReader reader) throws ParseException {
		campaignId = Integer
				.valueOf(reader.getAttribute(CAMPAIGN_ID_KEY, null));
	}

	/**
	 * @see se.sics.isl.transport.Transportable#write(se.sics.isl.transport.TransportWriter)
	 */
	@Override
	public void write(TransportWriter writer) {
		if (campaignId != null) {
			writer.attr(CAMPAIGN_ID_KEY, campaignId.toString());
		}
	}

	@Override
	public String toString() {
		return "CampaignReportKey [campaignId=" + campaignId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((campaignId == null) ? 0 : campaignId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CampaignReportKey other = (CampaignReportKey) obj;
		if (campaignId == null) {
			if (other.campaignId != null)
				return false;
		} else if (!campaignId.equals(other.campaignId))
			return false;
		return true;
	}

}
