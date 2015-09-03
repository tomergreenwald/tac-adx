package tau.tac.adx.users.generators;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import tau.tac.adx.generators.GenericGenerator;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;

/**
 * A naive implementation of the {@link GenericGenerator} interface. Randomizes
 * each characteristic of the {@link AdxUser}.
 * 
 * @author greenwald
 * 
 */
public class SimpleUserGenerator implements AdxUserGenerator {

	/** Unique user id. */
	private static AtomicInteger uniqueId = new AtomicInteger();
	
	/** User Continuation Probability. */
	private double pContinue;

	public SimpleUserGenerator(double pContinue) {
		this.pContinue = pContinue;
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
	 * Generates a collection of all possible {@link AdxUser uesrs} according to
	 * the possible values for a single {@link AdxUser user}:<li>age</li><li>
	 * gender</li><li>income</li>
	 * 
	 * @return A collection of all possible {@link AdxUser uesrs}.
	 */
	public static Collection<AdxUser> generateAllPossibleUsers() {
		Collection<AdxUser> users = new LinkedList<AdxUser>();
		for (Age age : Age.values()) {
			for (Gender gender : Gender.values()) {
				for (Income income : Income.values()) {
					users.add(new AdxUser(age, gender, income, Double.NaN,
							uniqueId.incrementAndGet()));
				}
			}
		}
		return users;
	}

	/**
	 * @return A {@link Random} {@link AdxUser}.
	 */
	private AdxUser getRandomUser() {
		Age age = randomAge();
		Gender gender = randomGender();
		Income income = randomIncome();
		AdxUser user = new AdxUser(age, gender, income, pContinue,
				uniqueId.incrementAndGet());
		return user;
	}

	/**
	 * @return A random {@link Income}.
	 */
	private Income randomIncome() {
		Random random = new Random();
		return Income.values()[(random.nextInt(Income.values().length))];
	}

	/**
	 * @return A random {@link Gender}.
	 */
	private Gender randomGender() {
		Random random = new Random();
		return Gender.values()[(random.nextInt(Gender.values().length))];
	}

	/**
	 * @return A random {@link Age}.
	 */
	private Age randomAge() {
		Random random = new Random();
		return Age.values()[(random.nextInt(Age.values().length))];
	}

	/**
	 * {@link Logger} instance.
	 */
	private final Logger logger = Logger.getLogger(this.getClass().getName());
}
