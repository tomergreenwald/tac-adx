/**
 * 
 */
package tau.tac.adx.util;

import java.util.Map;
import java.util.Random;

import org.junit.Test;

import tau.tac.adx.users.properties.Age;

/**
 * @author greenwald
 * 
 */
public class EnumGeneratorTest {

	/**
	 * {@link Random} instance.
	 */
	static final Random random = new Random();

	/**
	 * Test method for {@link tau.adx.utils.EnumGenerator#randomType()}.
	 */
	@Test
	public void testRandomType() {
		Map<Object, Integer> weigtMap = Utils.randomizeObjectWeightMap(
				Age.values(), TestConstants.MAX_WEIGHT);
		EnumGenerator<Object> enumGenerator = new EnumGenerator<Object>(weigtMap);
		Map<Object, Integer> typeCount = adxUtils.initEmptyMap(weigtMap
				.keySet());
		for (int i = 0; i < TestConstants.AMOUNT_TO_GENERATE; i++) {
			Object randomType = enumGenerator.randomType();
			typeCount.put(randomType, typeCount.get(randomType) + 1);
		}
		ObjectAssertion<Object> objectAsserion = new ObjectAssertion<Object>();
		objectAsserion.assertEqualDistribution(weigtMap, typeCount,
				TestConstants.EPSILON_RANGE);
	}

	/**
	 * Instance of the {@link AdxUtils} class used throughout the tests.
	 */
	AdxUtils<Object> adxUtils = new AdxUtils<Object>();
}
