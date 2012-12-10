/**
 * 
 */
package tau.tac.adx.ads.properties;

import java.util.Map;

/**
 * Probability maps for an <b>ad</b>.
 * 
 * @author greenwald
 * 
 */
public class AdAttributeProbabilityMaps {

	/**
	 * A probability {@link Map} for each {@link AdType}.
	 */
	Map<AdType, Double> adTypeDistribution;

	/**
	 * @param adTypeDistribution
	 */
	public AdAttributeProbabilityMaps(Map<AdType, Double> adTypeDistribution) {
		super();
		this.adTypeDistribution = adTypeDistribution;
	}

	/**
	 * @return the adTypeDistribution
	 */
	public Map<AdType, Double> getAdTypeDistribution() {
		return adTypeDistribution;
	}

	/**
	 * @param adTypeDistribution
	 *            the adTypeDistribution to set
	 */
	public void setAdTypeDistribution(Map<AdType, Double> adTypeDistribution) {
		this.adTypeDistribution = adTypeDistribution;
	}

}
