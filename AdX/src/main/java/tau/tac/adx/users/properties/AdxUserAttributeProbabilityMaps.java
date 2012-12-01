/**
 * 
 */
package tau.tac.adx.users.properties;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import tau.tac.adx.users.AdxUser;

/**
 * Probability maps for each property of an {@link AdxUser}.
 * 
 * @author greenwald
 * 
 */
@Data
@AllArgsConstructor
public class AdxUserAttributeProbabilityMaps {

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
