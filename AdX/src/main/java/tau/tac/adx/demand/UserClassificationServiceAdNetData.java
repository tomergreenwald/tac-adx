package tau.tac.adx.demand;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class UserClassificationServiceAdNetData {
	private int effectiveDay;
	private double serviceLevel;
	private double price;

	private double bid;
	private int daySubmitted;

	/**
	 * @param effectiveDay
	 * @param serviceLevel
	 * @param price
	 * @param bid
	 * @param daySubmitted
	 */
	public UserClassificationServiceAdNetData(int effectiveDay,
			double serviceLevel, double price, double bid, int daySubmitted) {
		super();
		this.effectiveDay = effectiveDay;
		this.serviceLevel = serviceLevel;
		this.price = price;
		this.bid = bid;
		this.daySubmitted = daySubmitted;
	}
	
	/**
	 * 
	 */
	public UserClassificationServiceAdNetData() {
		super();
	}

	public UserClassificationServiceAdNetData clone() {
		return new UserClassificationServiceAdNetData(effectiveDay, serviceLevel, price, bid, daySubmitted);
	}

	public int getEffectiveDay() {
		return effectiveDay;
	}

	public double getServiceLevel() {
		return serviceLevel;
	}

	public double getPrice() {
		return price;
	}

	/**
	 * @return the bid
	 */
	public double getBid() {
		return bid;
	}

	/**
	 * @param bid the bid to set
	 */
	public void setBid(double bid) {
		this.bid = bid;
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
	
	public String logToString() {
		return "[efday=" + effectiveDay + ", level=" + serviceLevel
				+ ", price=" + price + "] ";
	}

}
