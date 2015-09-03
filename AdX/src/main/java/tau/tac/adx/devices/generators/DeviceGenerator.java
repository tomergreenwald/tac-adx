package tau.tac.adx.devices.generators;

import java.util.Collection;

import tau.tac.adx.devices.Device;
import tau.tac.adx.generators.GenericGenerator;

/**
 * An interface to define {@link GenericGenerator}s for {@link Device}s.
 * 
 * @author greenwald
 */
public interface DeviceGenerator extends GenericGenerator<Device> {

	/**
	 * Returns a {@link Collection} of .
	 * 
	 * @param amount
	 *            THe amount of {@link Device}s to generate.
	 * 
	 * @return A {@link Collection} of generated objects.
	 */
	@Override
	public Collection<Device> generate(int amount);

}
