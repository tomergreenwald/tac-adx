package tau.tac.adx.demand;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class UserClassificationServiceAdNetData {
	int effectiveDay;
	double serviceLevel;
	double price;

	double bid;
	int daySubmitted;

	public int getEffectiveDay() {
		return effectiveDay;
	}

	public double getServiceLevel() {
		return serviceLevel;
	}

	public double getPrice() {
		return price;
	}

	public void setBid(double bid, int day) {
		this.bid = bid;
		this.daySubmitted = day;
	}

	public void setAuctionResult(double price, double serviceLevel, int day) {
		this.price = price;
		this.serviceLevel = serviceLevel;
		this.effectiveDay = day;
	}

}
