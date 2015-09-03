/**
 * 
 */
package tau.tac.adx.users.properties;

import java.util.Map;

import tau.tac.adx.users.AdxUser;

/**
 * Probability maps for each property of an {@link AdxUser}.
 * 
 * @author greenwald
 * 
 */
public class AdxUserAttributeProbabilityMaps {

	/**
	 * @param ageDistribution
	 * @param genderDistribution
	 * @param incomeDistribution
	 */
	public AdxUserAttributeProbabilityMaps(Map<Age, Double> ageDistribution,
			Map<Gender, Double> genderDistribution,
			Map<Income, Double> incomeDistribution) {
		super();
		this.ageDistribution = ageDistribution;
		this.genderDistribution = genderDistribution;
		this.incomeDistribution = incomeDistribution;
	}

	/**
	 * @return the ageDistribution
	 */
	public Map<Age, Double> getAgeDistribution() {
		return ageDistribution;
	}

	/**
	 * @param ageDistribution
	 *            the ageDistribution to set
	 */
	public void setAgeDistribution(Map<Age, Double> ageDistribution) {
		this.ageDistribution = ageDistribution;
	}

	/**
	 * @return the genderDistribution
	 */
	public Map<Gender, Double> getGenderDistribution() {
		return genderDistribution;
	}

	/**
	 * @param genderDistribution
	 *            the genderDistribution to set
	 */
	public void setGenderDistribution(Map<Gender, Double> genderDistribution) {
		this.genderDistribution = genderDistribution;
	}

	/**
	 * @return the incomeDistribution
	 */
	public Map<Income, Double> getIncomeDistribution() {
		return incomeDistribution;
	}

	/**
	 * @param incomeDistribution
	 *            the incomeDistribution to set
	 */
	public void setIncomeDistribution(Map<Income, Double> incomeDistribution) {
		this.incomeDistribution = incomeDistribution;
	}

	/**
	 * A probability {@link Map} for each {@link Age}.
	 */
	Map<Age, Double> ageDistribution;
	/**
	 * A probability {@link Map} for each {@link Gender}.
	 */
	Map<Gender, Double> genderDistribution;
	/**
	 * A probability {@link Map} for each {@link Income}.
	 */
	Map<Income, Double> incomeDistribution;

}
