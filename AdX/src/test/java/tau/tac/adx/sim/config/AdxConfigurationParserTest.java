/**
 * 
 */
package tau.tac.adx.sim.config;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import se.sics.isl.util.ConfigManager;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.props.PublisherCatalogEntry;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;

/**
 * @author Tomer
 *
 */
public class AdxConfigurationParserTest {

	private AdxConfigurationParser parser;

	@Before
	public void setUp() {
		ConfigManager configManager = new ConfigManager();
		configManager.loadConfiguration("adx-server/config/tac13adx_sim.conf");
		parser = new AdxConfigurationParser(configManager);
	}

	/**
	 * Test method for
	 * {@link tau.tac.adx.sim.config.AdxConfigurationParser#createPublisherCatalog()}
	 * .
	 */
	@Test
	public void testCreatePublisherCatalog() {
		PublisherCatalog publisherCatalog = parser.createPublisherCatalog();
		List<PublisherCatalogEntry> publishers = publisherCatalog
				.getPublishers();
		Assert.assertEquals(6, publishers.size());
		Set<String> names = new HashSet<String>();
		for (PublisherCatalogEntry entry : publishers) {
			assertTrue(names.add(entry.getPublisherName()));
		}
	}

	/**
	 * Test method for
	 * {@link tau.tac.adx.sim.config.AdxConfigurationParser#createUserPopulation()}
	 * .
	 */
	@Test
	public void testCreateUserPopulation() {
		for (int i = 0; i < 100; i++) {
			List<AdxUser> population = parser.createUserPopulation();
			Assert.assertEquals(10000, population.size());
			Map<UserType, Integer> typeCount = new HashMap<AdxConfigurationParserTest.UserType, Integer>();
			int count;
			for (AdxUser user : population) {
				UserType type = new UserType(user);
				if (!typeCount.containsKey(type)) {
					typeCount.put(type, 0);
				}
				typeCount.put(type, typeCount.get(type) + 1);
			}

			count = (Integer) typeCount.get(new UserType(Age.Age_65_PLUS,
					Gender.female, Income.very_high));
			Assert.assertEquals(18.0 / population.size(),
					count / population.size(), 0.06);

			count = (Integer) typeCount.get(new UserType(Age.Age_18_24,
					Gender.male, Income.low));
			Assert.assertEquals(526.0 / population.size(),
					count / population.size(), 0.06);

			count = (Integer) typeCount.get(new UserType(Age.Age_55_64,
					Gender.male, Income.high));
			Assert.assertEquals(157.0 / population.size(),
					count / population.size(), 0.06);

			count = (Integer) typeCount.get(new UserType(Age.Age_25_34,
					Gender.female, Income.low));
			Assert.assertEquals(460.0 / population.size(),
					count / population.size(), 0.06);
		}
	}

	private class UserType {
		private Age age;
		private Gender gender;
		private Income income;

		/**
		 * Constructor.
		 */
		public UserType(AdxUser user) {
			super();
			this.age = user.getAge();
			this.gender = user.getGender();
			this.income = user.getIncome();
		}

		/**
		 * Constructor.
		 */
		public UserType(Age age, Gender gender, Income income) {
			super();
			this.age = age;
			this.gender = gender;
			this.income = income;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((age == null) ? 0 : age.hashCode());
			result = prime * result
					+ ((gender == null) ? 0 : gender.hashCode());
			result = prime * result
					+ ((income == null) ? 0 : income.hashCode());
			return result;
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UserType other = (UserType) obj;
			if (age != other.age)
				return false;
			if (gender != other.gender)
				return false;
			if (income != other.income)
				return false;
			return true;
		}
	}

}
