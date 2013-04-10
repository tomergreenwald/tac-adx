package tau.tac.adx.messages;

public class CampaignLimitSet implements AdxMessage {
	int campaignId;
	String AdNetwork;
	double limit;
	
	public CampaignLimitSet(int campaignId, String adNet, double limit) {
		super();
		this.campaignId = campaignId;
		this.AdNetwork = adNet;
		this.limit = limit;
	}
	
	public int getCampaignId() {
		return campaignId;
	}

	public String getAdNetwork() {
		return AdNetwork;
	}

	public double getLimit() {
		return limit;
	}

	
}
