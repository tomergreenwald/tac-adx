/**
 * 
 */
package tau.tac.adx.util;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;

/**
 * Provides assertion methods for a given generic type <b>T</b>.
 * 
 * @author greenwald
 * @param <T>
 *            Generic type to define assertion for.
 * 
 */
public class ObjectAssertion<T> {
	/**
	 * Compares two given {@link Map maps} and asserts that all keys in the maps
	 * are distributed similarily between the,.
	 * 
	 * @param sourceMap
	 *            Source map to compare values from. Generally a map with known
	 *            value distribution.
	 * @param destMap
	 *            Dest map to compare values from. Generally a map with unknow
	 *            value distribution, that a user would like to compare with
	 *            <b>sourceMap</b>.
	 * @param epsilon
	 *            Maximal allowed difference between corresponding
	 *            distributions.
	 */
	public void assertEqualDistribution(Map<T, Integer> sourceMap,
			Map<T, Integer> destMap, double epsilon) {
		int expectedWeightSum = AdxUtils.sum(sourceMap.values());
		int resultWeightSum = AdxUtils.sum(destMap.values());
		for (Entry<T, Integer> entry : destMap.entrySet()) {
			double expectedPercentage = (sourceMap.get(entry.getKey()) / (double) expectedWeightSum);
			double actualPercentage = (entry.getValue() / (double) resultWeightSum);
			Assert.assertEquals(expectedPercentage, actualPercentage, epsilon);
		}
	}

	/**
	 * Compares two given {@link Map maps} and asserts that all keys in the maps
	 * are distributed similarily between the,.
	 * 
	 * @param sourceMap
	 *            Source map to compare values from. Generally a map with known
	 *            value distribution.
	 * @param destMap
	 *            Dest map to compare values from. Generally a map with unknow
	 *            value distribution, that a user would like to compare with
	 *            <b>sourceMap</b>.
	 * @param epsilon
	 *            Maximal allowed difference between corresponding
	 *            distributions.
	 */
	public void assertEqualDistributionDoubles(Map<T, Double> sourceMap,
			Map<T, Integer> destMap, double epsilon) {
		double expectedWeightSum = AdxUtils.sumDoubles(sourceMap.values());
		int resultWeightSum = AdxUtils.sum(destMap.values());
		for (Entry<T, Integer> entry : destMap.entrySet()) {
			double expectedPercentage = (sourceMap.get(entry.getKey()) / expectedWeightSum);
			double actualPercentage = (entry.getValue() / (double) resultWeightSum);
			Assert.assertEquals(expectedPercentage, actualPercentage, epsilon);
		}
	}
}
