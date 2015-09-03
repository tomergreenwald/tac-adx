package tau.tac.adx.ads.properties.generators;

import java.util.Collection;

import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.generators.GenericGenerator;

/**
 * An interface to define {@link GenericGenerator}s for {@link AdType}s.
 * 
 * @author greenwald
 */
public interface AdTypeGenerator extends GenericGenerator<AdType> {

	/**
	 * Returns a {@link Collection} of .
	 * 
	 * @param amount
	 *            THe amount of {@link AdType}s to generate.
	 * 
	 * @return A {@link Collection} of generated objects.
	 */
	@Override
	public Collection<AdType> generate(int amount);

}
