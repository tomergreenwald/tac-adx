/**
 * 
 */
package tau.tac.adx.users.properties;

import java.util.Map;

import tau.tac.adx.users.AdxUser;

/**
 * Distribution maps for each property of an {@link AdxUser}.
 * 
 * @author greenwald
 * 
 */
public class AdxUserDistributionMaps {

	/**
	 * @param ageDistribution
	 * @param genderDistribution
	 * @param incomeDistribution
	 */
	public AdxUserDistributionMaps(Map<Age, Integer> ageDistribution,
			Map<Gender, Integer> genderDistribution,
			Map<Income, Integer> incomeDistribution) {
		super();
		this.ageDistribution = ageDistribution;
		this.genderDistribution = genderDistribution;
		this.incomeDistribution = incomeDistribution;
	}

	/**
	 * @return the ageDistribution
	 */
	public Map<Age, Integer> getAgeDistribution() {
		return ageDistribution;
	}

	/**
	 * @param ageDistribution
	 *            the ageDistribution to set
	 */
	public void setAgeDistribution(Map<Age, Integer> ageDistribution) {
		this.ageDistribution = ageDistribution;
	}

	/**
	 * @return the genderDistribution
	 */
	public Map<Gender, Integer> getGenderDistribution() {
		return genderDistribution;
	}

	/**
	 * @param genderDistribution
	 *            the genderDistribution to set
	 */
	public void setGenderDistribution(Map<Gender, Integer> genderDistribution) {
		this.genderDistribution = genderDistribution;
	}

	/**
	 * @return the incomeDistribution
	 */
	public Map<Income, Integer> getIncomeDistribution() {
		return incomeDistribution;
	}

	/**
	 * @param incomeDistribution
	 *            the incomeDistribution to set
	 */
	public void setIncomeDistribution(Map<Income, Integer> incomeDistribution) {
		this.incomeDistribution = incomeDistribution;
	}

	/**
	 * A distribution {@link Map} for each {@link Age} type.
	 */
	Map<Age, Integer> ageDistribution;
	/**
	 * A distribution {@link Map} for each {@link Gender} type.
	 */
	Map<Gender, Integer> genderDistribution;
	/**
	 * A distribution {@link Map} for each {@link Income} type.
	 */
	Map<Income, Integer> incomeDistribution;

}
