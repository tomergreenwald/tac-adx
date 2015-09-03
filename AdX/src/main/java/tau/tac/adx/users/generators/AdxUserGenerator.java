package tau.tac.adx.users.generators;

import java.util.Collection;

import tau.tac.adx.generators.GenericGenerator;
import tau.tac.adx.users.AdxUser;

/**
 * An interface to define {@link GenericGenerator}s for {@link AdxUser}s.
 * 
 * @author greenwald
 */
public interface AdxUserGenerator extends GenericGenerator<AdxUser> {

	/**
	 * Returns a {@link Collection} of .
	 * 
	 * @param amount
	 *            THe amount of {@link AdxUser}s to generate.
	 * 
	 * @return A {@link Collection} of generated objects.
	 */
	@Override
	public Collection<AdxUser> generate(int amount);

}
