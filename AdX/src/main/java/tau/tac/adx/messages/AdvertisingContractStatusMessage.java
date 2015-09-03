/**
 * 
 */
package tau.tac.adx.messages;

/**
 * Default interface for every {@link Contract} message in the <b>Ad
 * Exchange</b> system.
 * 
 * @author greenwald
 * 
 */
public class AdvertisingContractStatusMessage implements AdxMessage {

	private final Contract contract;

	/**
	 * @param contract
	 */
	public AdvertisingContractStatusMessage(Contract contract) {
		super();
		this.contract = contract;
	}

	/**
	 * @return the contract
	 */
	public Contract getContract() {
		return contract;
	}
}
