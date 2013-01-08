/**
 * 
 */
package tau.tac.adx.messages;

import com.google.common.collect.BiMap;

/**
 * Default interface for every {@link Contract} message in the <b>Ad
 * Exchange</b> system.
 * 
 * @author greenwald
 * 
 */
public class ClassificationStatusMessage implements AdxMessage {

	private final BiMap<String, Integer> adNetworkClassification;

	/**
	 * @param adNetworkClassification
	 */
	public ClassificationStatusMessage(
			BiMap<String, Integer> adNetworkClassification) {
		super();
		this.adNetworkClassification = adNetworkClassification;
	}

	/**
	 * @return the adNetworkClassification
	 */
	public BiMap<String, Integer> getAdNetworkClassification() {
		return adNetworkClassification;
	}
}
