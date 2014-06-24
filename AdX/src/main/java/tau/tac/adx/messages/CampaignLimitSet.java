package tau.tac.adx.messages;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class CampaignLimitSet implements AdxMessage {
	private boolean isTotal;
	private int campaignId;
	private String AdNetwork;
	private double budgetLimit;
	private final int impressionLimit;

	public CampaignLimitSet(boolean isTotal, int campaignId, String adNet, int impressionLimit,
			double budgetLimit) {
		super();
		this.isTotal = isTotal;
		this.campaignId = campaignId;
		this.AdNetwork = adNet;
		this.budgetLimit = budgetLimit;
		this.impressionLimit = impressionLimit;
	}

	/**
	 * @return the isTotal (otherwise - daily)
	 */
	public boolean getIsTotal() {
		return isTotal;
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
