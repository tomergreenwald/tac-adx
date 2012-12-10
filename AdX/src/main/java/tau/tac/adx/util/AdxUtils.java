package tau.tac.adx.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class.
 * 
 * @author greenwald
 * @param <T>
 *            Generic type to define assertion for.
 * 
 */
public class AdxUtils<T> {

	/**
	 * Multiplier for decimal operations.
	 */
	private static final int DECIMAL_MULTIPLIER = 10;

	/**
	 * Formats a double to have only a given amount of digits after the decimal
	 * point.
	 * 
	 * @param d
	 *            {@link Double} to format.
	 * @param digitsAfterPoint
	 *            Number of digits after decimal point.
	 * @return Formatted number.
	 */
	public static double cutDouble(double d, int digitsAfterPoint) {
		return d * Math.pow(DECIMAL_MULTIPLIER, digitsAfterPoint)
				/ Math.pow(DECIMAL_MULTIPLIER, digitsAfterPoint);
	}

	/**
	 * Sumc a {@link Collection} of {@link Integer}s.
	 * 
	 * @param collection
	 *            Collection to sum.
	 * @return The sum of elements in a given collection.
	 */
	public static int sum(Collection<Integer> collection) {
		int sum = 0;
		for (Integer value : collection)
			sum += value;
		return sum;
	}

	/**
	 * Sums a {@link Collection} of {@link Integer}s.
	 * 
	 * @param collection
	 *            Collection to sum.
	 * @return The sum of elements in a given collection.
	 */
	public static double sumDoubles(Collection<Double> collection) {
		double sum = 0;
		for (Double value : collection)
			sum += value;
		return sum;
	}

	/**
	 * Determines whether two number differ by less than a third number.
	 * 
	 * @param first
	 *            First number.
	 * @param second
	 *            Second number.
	 * @param epsilon
	 *            Difference range.
	 * @return <code>true</code> if given numbers differ by less than
	 *         <b>epsilon</b>
	 */
	public static boolean withinEpsilon(double first, double second,
			double epsilon) {
		return Math.abs(first - second) < epsilon;
	}

	/**
	 * Initializes an empty {@link Map} according to given key {@link Set}.
	 * 
	 * @param iterable
	 *            Key {@link Set} to initialize map by.
	 * @return Initialized {@link Map}.
	 */
	public Map<T, Integer> initEmptyMap(Iterable<T> iterable) {
		Map<T, Integer> map = new HashMap<T, Integer>();
		for (T object : iterable) {
			map.put(object, 0);
		}
		return map;
	}

	/**
	 * Initializes an empty {@link Map} according to given key array.
	 * 
	 * @param values
	 *            Key array to initialize map by.
	 * @return Initialized {@link Map}.
	 */
	public Map<T, Integer> initEmptyMap(T[] values) {
		Map<T, Integer> map = new HashMap<T, Integer>();
		for (T object : values) {
			map.put(object, 0);
		}
		return map;
	}
}
