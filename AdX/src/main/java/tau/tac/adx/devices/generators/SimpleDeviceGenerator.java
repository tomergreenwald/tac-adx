package tau.tac.adx.devices.generators;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.util.EnumGenerator;

/**
 * A naive implementation of the {@link DeviceGenerator} interface.
 * 
 * @author greenwald
 * 
 */
public class SimpleDeviceGenerator implements DeviceGenerator {

	/**
	 * {@link AdType} {@link EnumGenerator}.
	 */
	private final EnumGenerator<Device> generator;

	/**
	 * Empty constructor.
	 */
	public SimpleDeviceGenerator() {
		Map<Device, Integer> weights = new HashMap<Device, Integer>();
		weights.put(Device.mobile, 1);
		weights.put(Device.pc, 1);
		generator = new EnumGenerator<Device>(weights);
	}

	/**
	 * @see tau.tac.adx.devices.generators.DeviceGenerator#generate(int)
	 */
	@Override
	public Collection<Device> generate(int amount) {

		Collection<Device> adTypes = new LinkedList<Device>();
		for (int i = 0; i < amount; i++) {
			adTypes.add(generator.randomType());
		}
		return adTypes;
	}
}
