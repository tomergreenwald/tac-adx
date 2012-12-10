/**
 * 
 */
package tau.tac.adx.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * @author greenwald
 * @param <T>
 *            An {@link Enum} to generate.
 * 
 */
public class EnumGenerator<T> {
	/**
	 * A distribution {@link Map} of values and their weight.
	 */
	private final Map<T, Integer> weights;
	/**
	 * The combined size of all the <b>weights</b>.
	 */
	private final int size;
	/**
	 * An instance of the {@link Random} class.
	 */
	private final Random random = new Random();

	/**
	 * @param weights
	 *            A distribution {@link Map} of values and their weight.
	 */
	public EnumGenerator(Map<T, Integer> weights) {
		this.weights = weights;
		size = AdxUtils.sum(weights.values());
	}

	/**
	 * @return A random type.
	 */
	public T randomType() {
		int randomNum = random.nextInt(size);
		int currentWeightSumm = 0;
		for (Entry<T, Integer> currentValue : weights.entrySet()) {
			if (randomNum >= currentWeightSumm
					&& randomNum < (currentWeightSumm + currentValue.getValue())) {
				return currentValue.getKey();
			}
			currentWeightSumm += currentValue.getValue();
		}

		return null;
	}
}