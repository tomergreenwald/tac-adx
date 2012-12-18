package tau.tac.adx.props.generators;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Logger;

import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.ads.properties.generators.AdTypeGenerator;
import tau.tac.adx.devices.Device;
import tau.tac.adx.devices.generators.DeviceGenerator;
import tau.tac.adx.generators.GenericGenerator;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.publishers.generators.AdxPublisherGenerator;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.generators.AdxUserGenerator;

import com.google.inject.Inject;

/**
 * A naive implementation of the {@link GenericGenerator} interface. Randomizes
 * each characteristic of the {@link AdxQuery}.
 * 
 * @author greenwald
 * 
 */
public class SimpleAdxQueryGenerator implements AdxQueryGenerator {

	/**
	 * {@link AdxUser} {@link GenericGenerator}.
	 */
	private final AdxUserGenerator userGenerator;
	/**
	 * {@link AdxUser} {@link AdxPublisher}.
	 */
	private final AdxPublisherGenerator publisherGenerator;
	/**
	 * {@link AdxUser} {@link Device}.
	 */
	private final DeviceGenerator deviceGenerator;
	/**
	 * {@link AdxUser} {@link AdType}.
	 */
	private final AdTypeGenerator adTypeGenerator;

	/**
	 * @param userGenerator
	 *            {@link AdxUser} {@link GenericGenerator}.
	 * @param publisherGenerator
	 *            {@link AdxPublisher} {@link GenericGenerator}.
	 * @param deviceGenerator
	 *            {@link Device} {@link GenericGenerator}.
	 * @param adTypeGenerator
	 *            {@link AdType} {@link GenericGenerator}.
	 */
	@Inject
	public SimpleAdxQueryGenerator(AdxUserGenerator userGenerator,
			AdxPublisherGenerator publisherGenerator,
			DeviceGenerator deviceGenerator, AdTypeGenerator adTypeGenerator) {
		super();
		this.userGenerator = userGenerator;
		this.publisherGenerator = publisherGenerator;
		this.deviceGenerator = deviceGenerator;
		this.adTypeGenerator = adTypeGenerator;
	}

	/**
	 * {@link Logger} instance.
	 */
	private final Logger logger = Logger.getLogger(this.getClass()
			.getCanonicalName());

	/**
	 * @see GenericGenerator#generate(int)
	 */
	@Override
	public Collection<AdxQuery> generate(int amount) {
		Collection<AdxQuery> publishers = new LinkedList<AdxQuery>();
		for (int i = 0; i < amount; i++) {
			publishers.add(getRandomAdxQuery());
		}
		logger.fine("Generated " + amount + " " + AdxQuery.class.getName()
				+ "s");
		return publishers;
	}

	/**
	 * @return A {@link Random} {@link AdxUser}.
	 */
	private AdxQuery getRandomAdxQuery() {
		AdxPublisher publisher = publisherGenerator.generate(1).iterator()
				.next();
		AdxUser user = userGenerator.generate(1).iterator().next();
		Device device = deviceGenerator.generate(1).iterator().next();
		AdType adType = adTypeGenerator.generate(1).iterator().next();
		AdxQuery query = new AdxQuery(publisher, user, device, adType);
		return query;
	}

}
