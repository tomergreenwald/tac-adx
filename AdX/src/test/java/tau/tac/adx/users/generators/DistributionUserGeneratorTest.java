/**
 * 
 */
package tau.tac.adx.users.generators;

import java.util.Collection;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.properties.AdxUserDistributionMaps;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;
import tau.tac.adx.util.AdxUtils;
import tau.tac.adx.util.MapGenerator;
import tau.tac.adx.util.ObjectAssertion;
import tau.tac.adx.util.TestConstants;

/**
 * @author greenwald
 * 
 */
public class DistributionUserGeneratorTest {

	/**
	 * Generate user population according to random probabilities for each
	 * {@link AdxUser} <b>attribute</b>.
	 */
	@Before
	public void setUp() {
		distributionMaps = randomizeAdxUserDistributionMaps();
		DistributionUserGenerator generator = new DistributionUserGenerator(
				distributionMaps);
		users = generator.generate(TestConstants.AMOUNT_TO_GENERATE);
	}

	/**
	 * Test method for
	 * {@link tau.adx.common.users.generators.DistributionUserGenerator#generate(int)}
	 * .
	 */
	@Test
	public void testAgeGeneration() {

		Map<Age, Integer> ageValues = summarizeAgeValues(users);
		ObjectAssertion<Age> ageAssetion = new ObjectAssertion<Age>();
		ageAssetion.assertEqualDistribution(
				distributionMaps.getAgeDistribution(), ageValues,
				TestConstants.EPSILON_RANGE);
	}

	/**
	 * Test method for
	 * {@link tau.adx.common.users.generators.DistributionUserGenerator#generate(int)}
	 * .
	 */
	@Test
	public void testGenderGeneration() {

		Map<Gender, Integer> genderValues = summarizeGenderValues(users);
		ObjectAssertion<Gender> genderAssetion = new ObjectAssertion<Gender>();
		genderAssetion.assertEqualDistribution(
				distributionMaps.getGenderDistribution(), genderValues,
				TestConstants.EPSILON_RANGE);
	}

	/**
	 * Test method for
	 * {@link tau.adx.common.users.generators.DistributionUserGenerator#generate(int)}
	 * .
	 */
	@Test
	public void testIncomeGeneration() {

		Map<Income, Integer> inceomValues = summarizeIncomeValues(users);
		ObjectAssertion<Income> incomeAssertion = new ObjectAssertion<Income>();
		incomeAssertion.assertEqualDistribution(
				distributionMaps.getIncomeDistribution(), inceomValues,
				TestConstants.EPSILON_RANGE);
	}

	/**
	 * Summarizes age values for a given {@link Collection} of {@link AdxUser}.
	 * 
	 * @param users
	 *            {@link Collection} of {@link AdxUser users}.
	 * @return A summarized mapping between each {@link Age} type and its sum
	 *         among the users.
	 */
	private Map<Age, Integer> summarizeAgeValues(Collection<AdxUser> users) {
		AdxUtils<Age> adxUtils = new AdxUtils<Age>();
		Map<Age, Integer> map = adxUtils.initEmptyMap(Age.values());
		for (AdxUser adxUser : users) {
			Age age = adxUser.getAge();
			map.put(age, map.get(age) + 1);
		}
		return map;
	}

	/**
	 * Summarizes gender values for a given {@link Collection} of
	 * {@link AdxUser}.
	 * 
	 * @param users
	 *            {@link Collection} of {@link AdxUser users}.
	 * @return A summarized mapping between each {@link Gender} type and its sum
	 *         among the users.
	 */
	private Map<Gender, Integer> summarizeGenderValues(Collection<AdxUser> users) {
		AdxUtils<Gender> adxUtils = new AdxUtils<Gender>();
		Map<Gender, Integer> map = adxUtils.initEmptyMap(Gender.values());
		for (AdxUser adxUser : users) {
			Gender gender = adxUser.getGender();
			map.put(gender, map.get(gender) + 1);
		}
		return map;
	}

	/**
	 * Summarizes income values for a given {@link Collection} of
	 * {@link AdxUser}.
	 * 
	 * @param users
	 *            {@link Collection} of {@link AdxUser users}.
	 * @return A summarized mapping between each {@link Income} type and its sum
	 *         among the users.
	 */
	private Map<Income, Integer> summarizeIncomeValues(Collection<AdxUser> users) {
		AdxUtils<Income> adxUtils = new AdxUtils<Income>();
		Map<Income, Integer> map = adxUtils.initEmptyMap(Income.values());
		for (AdxUser adxUser : users) {
			Income income = adxUser.getIncome();
			map.put(income, map.get(income) + 1);
		}
		return map;
	}

	/**
	 * Randomizes {@link AdxUserDistributionMaps}.
	 * 
	 * @return Randomized {@link AdxUserDistributionMaps}.
	 */
	private AdxUserDistributionMaps randomizeAdxUserDistributionMaps() {
		MapGenerator<Age> ageMapGenerator = new MapGenerator<Age>();
		Map<Age, Integer> ageWeigtMap = ageMapGenerator
				.randomizeWeightMap(Age.values(), TestConstants.MAX_WEIGHT);
		MapGenerator<Gender> genderMapGenerator = new MapGenerator<Gender>();
		Map<Gender, Integer> genderWeigtMap = genderMapGenerator
				.randomizeWeightMap(Gender.values(),
						TestConstants.MAX_WEIGHT);
		MapGenerator<Income> incomeMapGenerator = new MapGenerator<Income>();

		Map<Income, Integer> incomeWeigtMap = incomeMapGenerator
				.randomizeWeightMap(Income.values(),
						TestConstants.MAX_WEIGHT);
		AdxUserDistributionMaps distributionMaps = new AdxUserDistributionMaps(
				ageWeigtMap, genderWeigtMap, incomeWeigtMap);
		return distributionMaps;
	}

	/**
	 * A collection of {@link AdxUser users} used for tests.
	 */
	Collection<AdxUser> users;
	/**
	 * {@link AdxUserDistributionMaps} used for tests.
	 */
	AdxUserDistributionMaps distributionMaps;

}
