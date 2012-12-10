/**
 * 
 */
package tau.tac.adx.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author greenwald
 * @param <T>
 *            Generic type to define generation methods.
 * 
 */
public class MapGenerator<T> {

	/**
	 * Instance of the random class.
	 */
	Random random = new Random();

	/**
	 * Generate a distribution {@link Map} according to a given <b>key set</b>.
	 * 
	 * @param objects
	 *            Generated {@link Map}'s key set.
	 * @param maxWeight
	 *            Max weight per object
	 * @return A {@link Map} with a <b>key set</b> of <b>objects</b> and a
	 *         <b>weight</b> for each key.
	 */
	public Map<T, Integer> randomizeWeightMap(T[] objects, int maxWeight) {
		Map<T, Integer> weights = new HashMap<T, Integer>();
		for (T object : objects) {
			weights.put(object, random.nextInt(maxWeight));
		}
		return weights;
	}

	/**
	 * Generate a probability {@link Map} according to a given <b>key set</b>.
	 * 
	 * @param objects
	 *            Generated {@link Map}'s key set.
	 * @return A {@link Map} with a <b>key set</b> of <b>objects</b> and a
	 *         <b>random double</b> for each key.
	 */
	public Map<T, Double> randomizeProbabilityMap(T[] objects) {
		Map<T, Double> weights = new HashMap<T, Double>();
		for (T object : objects) {
			weights.put(object, random.nextDouble());
		}
		return weights;
	}

}
