/**
 * 
 */
package tau.tac.adx.messages;

/**
 * @author greenwald
 * 
 */
public class Contract {

	private final String adNetowrk;// Maybe we should create an AdNetwork
									// class/interface.

	private final int contractId;

	private final int contractLength;

	/**
	 * @param adNetowrk
	 * @param contractId
	 * @param contractLength
	 * @param daysLeft
	 */
	public Contract(String adNetowrk, int contractId, int contractLength,
			int daysLeft) {
		super();
		this.adNetowrk = adNetowrk;
		this.contractId = contractId;
		this.contractLength = contractLength;
		this.daysLeft = daysLeft;
	}

	private final int daysLeft;

	/**
	 * @return the adNetowrk
	 */
	public String getAdNetowrk() {
		return adNetowrk;
	}

	/**
	 * @return the contractId
	 */
	public int getContractId() {
		return contractId;
	}

	/**
	 * @return the contractLength
	 */
	public int getContractLength() {
		return contractLength;
	}

	/**
	 * @return the daysLeft
	 */
	public int getDaysLeft() {
		return daysLeft;
	}

	// contract targets?

}
