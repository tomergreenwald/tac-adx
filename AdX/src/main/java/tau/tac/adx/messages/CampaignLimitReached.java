package tau.tac.adx.messages;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class CampaignLimitReached implements AdxMessage {
	int campaignId;
	String AdNetwork;

	public CampaignLimitReached(int campaignId, String adNet) {
		super();
		this.campaignId = campaignId;
		this.AdNetwork = adNet;
	}

	public int getCampaignId() {
		return campaignId;
	}

	public String getAdNetwork() {
		return AdNetwork;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CampaignLimitReached [campaignId=" + campaignId
				+ ", AdNetwork=" + AdNetwork + "]";
	}

}
