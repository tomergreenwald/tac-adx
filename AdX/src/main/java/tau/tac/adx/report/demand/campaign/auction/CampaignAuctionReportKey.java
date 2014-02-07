package tau.tac.adx.report.demand.campaign.auction;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Agent;
import tau.tac.adx.agents.SampleAdNetwork;

/**
 * 
 * @author Tomer Greenwald
 * 
 */
public class CampaignAuctionReportKey implements Transportable {
	
	/** Ad net name key. */
	private static final String AD_NET_NAME_KEY = "AD_NET_NAME_KEY";
	
	/**
	 * Implemented ADX ad network {@link Agent} (e.g. {@link SampleAdNetwork}). 
	 */
	private String adnetName;

	public CampaignAuctionReportKey(String adnetName) {
		super();
		this.adnetName = adnetName;
	}

	public CampaignAuctionReportKey() {
	}


	/**
	 * @return the adnetName
	 */
	public String getAdnetName() {
		return adnetName;
	}

	/**
	 * @param adnetName the adnetName to set
	 */
	public void setAdnetName(String adnetName) {
		this.adnetName = adnetName;
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
		adnetName = reader.getAttribute(AD_NET_NAME_KEY, null);
	}

	/**
	 * @see se.sics.isl.transport.Transportable#write(se.sics.isl.transport.TransportWriter)
	 */
	@Override
	public void write(TransportWriter writer) {
		writer.attr(AD_NET_NAME_KEY, adnetName);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CampaignAuctionReportKey [adnetName=" + adnetName + "]";
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((adnetName == null) ? 0 : adnetName.hashCode());
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
		CampaignAuctionReportKey other = (CampaignAuctionReportKey) obj;
		if (adnetName == null) {
			if (other.adnetName != null)
				return false;
		} else if (!adnetName.equals(other.adnetName))
			return false;
		return true;
	}

}
