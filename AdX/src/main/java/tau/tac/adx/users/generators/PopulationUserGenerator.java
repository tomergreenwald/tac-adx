package tau.tac.adx.users.generators;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import tau.tac.adx.generators.GenericGenerator;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.properties.AdxUserDistributionMaps;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;
import tau.tac.adx.util.EnumGenerator;

/**
 * An advanced implementation of the {@link GenericGenerator}.<br>
 * Each attribute of and {@link AdxUser} (e.g. {@link Age}, {@link Gender},
 * {@link Income}) is randomized according to a given set of distribution.
 * 
 * @author greenwald
 * 
 */
public class PopulationUserGenerator implements AdxUserGenerator {

	/** An {@link EnumGenerator} over {@link AdxUser} with probability. */
	private final EnumGenerator<AdxUser> userGenerator;
	/** Unique user id. */
	private int uniqueId;

	/**
	 * @param distributionMaps
	 */
	public PopulationUserGenerator(Map<AdxUser, Integer> weights) {
		userGenerator = new EnumGenerator<AdxUser>(weights);
	}

	/**
	 * @see GenericGenerator#generate(int)
	 */
	@Override
	public List<AdxUser> generate(int amount) {
		List<AdxUser> users = new LinkedList<AdxUser>();
		for (int i = 0; i < amount; i++) {
			users.add(getRandomUser());
		}
		logger.fine("Generated " + amount + " " + AdxUser.class.getName() + "s");
		return users;
	}

	/**
	 * @return A random {@link AdxUser} according to given {@link EnumGenerator}
	 *         s with distribution a {@link Map}s over {@link Age},
	 *         {@link Gender} and {@link Income}.
	 */
	private AdxUser getRandomUser() {
		//FIXME export to configuration
		double pContinue = 0.1;
		AdxUser user = (AdxUser) userGenerator.randomType().clone();
		user.setpContinue(pContinue);
		user.setUniqueId(uniqueId);
		uniqueId++;
		return user;
	}

	/**
	 * {@link Logger} instance.
	 */
	private final Logger logger = Logger.getLogger(this.getClass().getName());
}
