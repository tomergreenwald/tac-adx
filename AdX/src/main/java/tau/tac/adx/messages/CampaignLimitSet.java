package tau.tac.adx.messages;

public class CampaignLimitSet implements AdxMessage {
	int campaignId;
	String AdNetwork;
	double budgetLimit;
	private int impressionLimit;
	
	public CampaignLimitSet(int campaignId, String adNet, int impressionLimit, double budgetLimit) {
		super();
		this.campaignId = campaignId;
		this.AdNetwork = adNet;
		this.budgetLimit = budgetLimit;
		this.impressionLimit = impressionLimit;
	}

	/**
	 * @return the campaignId
	 */
	public int getCampaignId() {
		return campaignId;
	}

	/**
	 * @return the adNetwork
	 */
	public String getAdNetwork() {
		return AdNetwork;
	}

	/**
	 * @return the budgetLimit
	 */
	public double getBudgetLimit() {
		return budgetLimit;
	}

	/**
	 * @return the impressionLimit
	 */
	public int getImpressionLimit() {
		return impressionLimit;
	}

	
}
