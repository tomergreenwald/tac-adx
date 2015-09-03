package tau.tac.adx.ads.properties.generators;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.util.EnumGenerator;

/**
 * A naive implementation of the {@link AdTypeGenerator} interface.
 * 
 * @author greenwald
 * 
 */
public class SimpleAdTypeGenerator implements AdTypeGenerator {

	/**
	 * {@link AdType} {@link EnumGenerator}.
	 */
	private final EnumGenerator<AdType> generator;

	/**
	 * Empty constructor.
	 */
	public SimpleAdTypeGenerator() {
		Map<AdType, Integer> weights = new HashMap<AdType, Integer>();
		weights.put(AdType.text, 1);
		weights.put(AdType.video, 1);
		generator = new EnumGenerator<AdType>(weights);
	}

	/**
	 * @see tau.tac.adx.ads.properties.generators.AdTypeGenerator#generate(int)
	 */
	@Override
	public Collection<AdType> generate(int amount) {

		Collection<AdType> adTypes = new LinkedList<AdType>();
		for (int i = 0; i < amount; i++) {
			adTypes.add(generator.randomType());
		}
		return adTypes;
	}
}
