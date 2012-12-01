/**
 * 
 */
package tau.tac.adx.users.properties;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import tau.tac.adx.users.AdxUser;

/**
 * Distribution maps for each property of an {@link AdxUser}.
 * 
 * @author greenwald
 * 
 */
@Data
@AllArgsConstructor
public class AdxUserDistributionMaps {

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
