/**
 * 
 */
package tau.tac.adx.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author greenwald
 * 
 */
public class Utils {

	/**
	 * Initializes an {@link Injector} with an {@link AbstractModule}
	 * implementation.
	 */
	static Injector injector = Guice.createInjector(new AdxTestModule());
	/**
	 * {@link Random} instance.
	 */
	static final Random random = new Random();

	/**
	 * @return The default {@link Injector}.
	 */
	public static Injector getInjector() {
		return injector;
	}

	/**
	 * Generated a distribution {@link Map} according to a given <b>key set</b>.
	 * 
	 * @param objects
	 *            Generated {@link Map}'s key set.
	 * @param maxWeight
	 *            Max weight per object
	 * @return A {@link Map} with a <b>key set</b> of <b>objects</b> and a
	 *         <b>weight</b> for each value.
	 */
	public static Map<Object, Integer> randomizeObjectWeightMap(
			Object[] objects, int maxWeight) {
		Map<Object, Integer> weights = new HashMap<Object, Integer>();
		for (Object object : objects) {
			weights.put(object, maxWeight);
		}
		return weights;
	}
	
	
	
}
