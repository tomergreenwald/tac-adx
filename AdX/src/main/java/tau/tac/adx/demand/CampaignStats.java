package tau.tac.adx.demand;

public class CampaignStats {
	double tartgetedImps;
	double otherImps;
	double cost;

	
	
	public CampaignStats(double timps, double oimps, double cost) {
		this.tartgetedImps = timps;
		this.otherImps = oimps;
		this.cost = cost;
	}
	public double getTargetedImps() {
		return tartgetedImps;
	}
	public double getOtherImps() {
		return otherImps;
	}
	public double getCost() {
		return cost;
	}
	
	public void setValues(CampaignStats other) {
		this.tartgetedImps = other.tartgetedImps;
		this.otherImps = other.otherImps;
		this.cost = other.cost;
	}

	
	CampaignStats add(CampaignStats other) {
		if (other != null) {
		  tartgetedImps += other.tartgetedImps;
		  otherImps += other.otherImps;
		  cost += other.cost;
		}
		return this;
	}
}
