package tau.tac.adx.props.generators;

import java.util.Collection;

import tau.tac.adx.generators.GenericGenerator;
import tau.tac.adx.props.AdxQuery;

/**
 * An interface to define {@link GenericGenerator}s for {@link AdxQuery}s.
 * 
 * @author greenwald
 */
public interface AdxQueryGenerator extends GenericGenerator<AdxQuery> {

	/**
	 * Returns a {@link Collection} of .
	 * 
	 * @param amount
	 *            THe amount of {@link AdxQuery}s to generate.
	 * 
	 * @return A {@link Collection} of generated objects.
	 */
	@Override
	public Collection<AdxQuery> generate(int amount);

}
