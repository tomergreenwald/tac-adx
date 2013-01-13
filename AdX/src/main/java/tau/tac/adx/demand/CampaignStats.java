package tau.tac.adx.demand;

public class CampaignStats {
	double tartgetedImps;
	double otherImps;
	double cost;
	CampaignStats(double timps, double oimps, double cost) {
		this.tartgetedImps = timps;
		this.otherImps = oimps;
		this.cost = cost;
	}
	double getTargetedImps() {
		return tartgetedImps;
	}
	double getOtherImps() {
		return otherImps;
	}
	double getCost() {
		return cost;
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
