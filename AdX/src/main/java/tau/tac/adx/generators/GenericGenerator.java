package tau.tac.adx.generators;

import java.util.Collection;

/**
 * An interface to define how generators in the system should behave.<br>
 * Every implementation of this class must include the {@link #generate(int)}
 * function.
 * 
 * @author greenwald
 * @param <T>
 *            {@link Object} to generate.
 * 
 */
public interface GenericGenerator<T> {

	/**
	 * Returns a {@link Collection} of .
	 * 
	 * @param amount
	 *            THe amount of objects to generate.
	 * 
	 * @return A {@link Collection} of generated objects.
	 */
	public Collection<T> generate(int amount);

}
