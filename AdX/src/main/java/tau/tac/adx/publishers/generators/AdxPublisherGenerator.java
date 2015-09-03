package tau.tac.adx.publishers.generators;

import java.util.Collection;

import tau.tac.adx.generators.GenericGenerator;
import tau.tac.adx.publishers.AdxPublisher;

/**
 * An interface to define {@link GenericGenerator}s for {@link AdxPublisher}s.
 * 
 * @author greenwald
 */
public interface AdxPublisherGenerator extends GenericGenerator<AdxPublisher> {

	/**
	 * Returns a {@link Collection} of .
	 * 
	 * @param amount
	 *            THe amount of {@link AdxPublisher}s to generate.
	 * 
	 * @return A {@link Collection} of generated objects.
	 */
	@Override
	public Collection<AdxPublisher> generate(int amount);

}
