/**
 * 
 */
package tau.tac.adx.users.generators;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;
import tau.tac.adx.util.TestConstants;

/**
 * @author greenwald
 * 
 */
public class SimpleUserGeneratorTest {

	/** Unique user id. */
	private static AtomicInteger uniqueId = new AtomicInteger();

	/**
	 * Test method for
	 * {@link tau.adx.common.users.generators.SimpleUserGenerator#generate(int)}
	 * .
	 */
	@Test
	public void testGenerate() {
		SimpleUserGenerator generator = new SimpleUserGenerator(Math.random());
		int amount = Math.abs(new Random()
				.nextInt(TestConstants.AMOUNT_TO_GENERATE));
		Collection<AdxUser> users = generator.generate(amount);
		Assert.assertEquals(amount, users.size());
	}

	/**
	 * Test method for
	 * {@link tau.adx.common.users.generators.SimpleUserGenerator#generateAllPossibleUsers()}
	 * .
	 */
	@Test
	public void testGenerateAllPossibleUsers() {
		Collection<AdxUser> allPossibleUsers = SimpleUserGenerator
				.generateAllPossibleUsers();
		for (Age age : Age.values()) {
			for (Gender gender : Gender.values()) {
				for (Income income : Income.values()) {
					Assert.assertTrue(allPossibleUsers.contains(new AdxUser(
							age, gender, income, Double.NaN, uniqueId
									.incrementAndGet())));
				}
			}
		}
	}
}
