package tau.tac.adx.sim.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.sics.isl.util.ConfigManager;
import tau.tac.adx.ads.properties.AdAttributeProbabilityMaps;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.publishers.reserve.ReservePriceManager;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.generators.SimpleUserGenerator;
import tau.tac.adx.users.properties.AdxUserAttributeProbabilityMaps;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;

public class AdxConfigurationParser {

	private final ConfigManager config;

	/**
	 * @param config
	 */
	public AdxConfigurationParser(ConfigManager config) {
		this.config = config;
	}

	/**
	 * Generates an {@link Device} distribution {@link Map}.
	 * 
	 * @param config
	 *            {@link ConfigManager}.
	 * @return An {@link AdType} distribution {@link Map}.
	 */
	public Map<AdType, Integer> createAdTypeDistributionMap() {
		Map<AdType, Integer> deviceDistribution = new HashMap<AdType, Integer>();
		deviceDistribution.put(AdType.text,
				config.getPropertyAsInt("adxusers.adtype.text", 0));
		deviceDistribution.put(AdType.video,
				config.getPropertyAsInt("adxusers.adtype.video", 0));
		return deviceDistribution;
	}

	/**
	 * Generates a {@link Device} distribution {@link Map}.
	 * 
	 * @param config
	 *            {@link ConfigManager}.
	 * @return A {@link Device} distribution {@link Map}.
	 */
	public Map<Device, Integer> createDeviceDistributionMap() {
		Map<Device, Integer> deviceDistribution = new HashMap<Device, Integer>();
		deviceDistribution.put(Device.pc,
				config.getPropertyAsInt("adxusers.device.pc", 0));
		deviceDistribution.put(Device.mobile,
				config.getPropertyAsInt("adxusers.device.mobile", 0));
		return deviceDistribution;
	}

	/**
	 * Generates a population of {@link AdxUser}s
	 * 
	 * @param config
	 *            {@link ConfigManager}.
	 * 
	 * @return {@link List} of {@link AdxUser}s.
	 */
	public List<AdxUser> createUserPopulation() {
		int populationSize = config.getPropertyAsInt(
				"adxusers.population_size", 0);
		SimpleUserGenerator generator = new SimpleUserGenerator();
		return generator.generate(populationSize);
	}

	/**
	 * Generates a {@link PublisherCatalog} from the configuration file.
	 * 
	 * @return {@link PublisherCatalog} parsed from the configuration file.
	 */
	public PublisherCatalog createPublisherCatalog() {
		PublisherCatalog catalog = new PublisherCatalog();

		String[] skus = config.getPropertyAsArray("publisher.rating");

		for (String sku : skus) {
			String name = config.getProperty(String.format("catalog.%s.name",
					sku));
			int rating = config.getPropertyAsInt(
					String.format("catalog.%s.rating", sku), 0);

			AdAttributeProbabilityMaps adAttributeProbabilityMaps = extractAdTypeAffiliation(sku);
			AdxUserAttributeProbabilityMaps adxUserAttributeProbabilityMaps = extractUserAffiliation(sku);
			Map<Device, Double> deviceAffiliation = extractDeviceAffiliation(sku);
			ReservePriceManager reservePriceManager = extractReservePriceInfo(sku);
			AdxPublisher publisher = new AdxPublisher(
					adxUserAttributeProbabilityMaps,
					adAttributeProbabilityMaps, deviceAffiliation, rating, 0,
					reservePriceManager, name);
			catalog.addPublisher(publisher);
		}

		catalog.lock();

		return catalog;
	}

	/**
	 * Extracts {@link ReservePriceManager} from configuration file.
	 * 
	 * @param config
	 *            {@link ConfigManager} to read properties from.
	 * @param sku
	 *            Current <b>sku</b> id.
	 * @return Extracted {@link ReservePriceManager}.
	 */
	private ReservePriceManager extractReservePriceInfo(String sku) {
		double dailyBaselineAverage = config.getPropertyAsDouble(String.format(
				"catalog.%s.reserve_price.daily_baseline_average", sku), 0);
		double baselineRange = config.getPropertyAsDouble(
				String.format("catalog.%s.reserve_price.baseline_range", sku),
				0);
		double updateCoefficient = config.getPropertyAsDouble(String.format(
				"catalog.%s.reserve_price.update_coeffecient", sku), 0);
		ReservePriceManager reservePriceManager = new ReservePriceManager(
				dailyBaselineAverage, baselineRange, updateCoefficient);
		return reservePriceManager;
	}

	/**
	 * Extracts {@link Device} affiliation from the configuration file as a
	 * {@link Map} between a {@link Device} and its popularity.
	 * 
	 * @param config
	 *            {@link ConfigManager} to read properties from.
	 * @param sku
	 *            Current <b>sku</b> id.
	 * @return {@link Device} affiliation {@link Map}.
	 */
	private Map<Device, Double> extractDeviceAffiliation(String sku) {
		Map<Device, Double> deviceDistribution = new HashMap<Device, Double>();
		deviceDistribution.put(
				Device.pc,
				config.getPropertyAsDouble(
						String.format("catalog.%s.device.pc", sku), 0));
		deviceDistribution.put(
				Device.mobile,
				config.getPropertyAsDouble(
						String.format("catalog.%s.device.mobile", sku), 0));
		return deviceDistribution;
	}

	/**
	 * Extracts {@link AdxUserAttributeProbabilityMaps} from the configuration
	 * file
	 * 
	 * @param config
	 *            {@link ConfigManager} to read properties from.
	 * @param sku
	 *            Current <b>sku</b> id.
	 * @return {@link AdxUserAttributeProbabilityMaps}.
	 */
	private AdxUserAttributeProbabilityMaps extractUserAffiliation(String sku) {
		Map<Age, Double> ageDistribution = extractAgeDistribution(sku);
		Map<Gender, Double> genderDistribution = extractGenderDistribution(sku);
		Map<Income, Double> incomeDistribution = extractIncomeDistribution(sku);
		AdxUserAttributeProbabilityMaps adxUserAttributeProbabilityMaps = new AdxUserAttributeProbabilityMaps(
				ageDistribution, genderDistribution, incomeDistribution);
		return adxUserAttributeProbabilityMaps;
	}

	/**
	 * Extracts {@link Income} affiliation from the configuration file as a
	 * {@link Map} between a {@link Income} and its popularity.
	 * 
	 * @param config
	 *            {@link ConfigManager} to read properties from.
	 * @param sku
	 *            Current <b>sku</b> id.
	 * @return {@link Income} affiliation {@link Map}.
	 */
	private Map<Income, Double> extractIncomeDistribution(String sku) {
		Map<Income, Double> ageDistribution = new HashMap<Income, Double>();
		ageDistribution.put(
				Income.low,
				config.getPropertyAsDouble(
						String.format("catalog.%s.income.low", sku), 0));
		ageDistribution.put(
				Income.medium,
				config.getPropertyAsDouble(
						String.format("catalog.%s.income.medium", sku), 0));
		ageDistribution.put(
				Income.high,
				config.getPropertyAsDouble(
						String.format("catalog.%s.income.high", sku), 0));
		ageDistribution.put(
				Income.very_high,
				config.getPropertyAsDouble(
						String.format("catalog.%s.income.very_high", sku), 0));
		return ageDistribution;

	}

	/**
	 * Extracts {@link Gender} affiliation from the configuration file as a
	 * {@link Map} between a {@link Gender} and its popularity.
	 * 
	 * @param config
	 *            {@link ConfigManager} to read properties from.
	 * @param sku
	 *            Current <b>sku</b> id.
	 * @return {@link Gender} affiliation {@link Map}.
	 */
	private Map<Gender, Double> extractGenderDistribution(String sku) {
		Map<Gender, Double> genderDistribution = new HashMap<Gender, Double>();
		double maleAffiliation = config.getPropertyAsDouble(
				String.format("catalog.%s.gender.male", sku), 0);
		genderDistribution.put(Gender.male, maleAffiliation);
		genderDistribution.put(Gender.female, 1 - maleAffiliation);
		return genderDistribution;
	}

	/**
	 * Extracts {@link Age} affiliation from the configuration file as a
	 * {@link Map} between a {@link Age} and its popularity.
	 * 
	 * @param config
	 *            {@link ConfigManager} to read properties from.
	 * @param sku
	 *            Current <b>sku</b> id.
	 * @return {@link Age} affiliation {@link Map}.
	 */
	private Map<Age, Double> extractAgeDistribution(String sku) {
		Map<Age, Double> ageDistribution = new HashMap<Age, Double>();
		ageDistribution.put(
				Age.Age_18_24,
				config.getPropertyAsDouble(
						String.format("catalog.%s.age.age1", sku), 0));
		ageDistribution.put(
				Age.Age_25_34,
				config.getPropertyAsDouble(
						String.format("catalog.%s.age.age2", sku), 0));
		ageDistribution.put(
				Age.Age_35_44,
				config.getPropertyAsDouble(
						String.format("catalog.%s.age.age3", sku), 0));
		ageDistribution.put(
				Age.Age_45_54,
				config.getPropertyAsDouble(
						String.format("catalog.%s.age.age4", sku), 0));
		ageDistribution.put(
				Age.Age_55_64,
				config.getPropertyAsDouble(
						String.format("catalog.%s.age.age5", sku), 0));
		ageDistribution.put(
				Age.Age_65_PLUS,
				config.getPropertyAsDouble(
						String.format("catalog.%s.age.age6", sku), 0));
		return ageDistribution;
	}

	/**
	 * Extracts {@link AdAttributeProbabilityMaps} from the configuration file
	 * 
	 * @param config
	 *            {@link ConfigManager} to read properties from.
	 * @param sku
	 *            Current <b>sku</b> id.
	 * @return {@link AdAttributeProbabilityMaps}.
	 */
	private AdAttributeProbabilityMaps extractAdTypeAffiliation(String sku) {
		Map<AdType, Double> adTypeDistribution = new HashMap<AdType, Double>();
		adTypeDistribution.put(
				AdType.text,
				config.getPropertyAsDouble(
						String.format("catalog.%s.adtype.text", sku), 0));
		adTypeDistribution.put(
				AdType.video,
				config.getPropertyAsDouble(
						String.format("catalog.%s.adtype.video", sku), 0));
		AdAttributeProbabilityMaps adAttributeProbabilityMaps = new AdAttributeProbabilityMaps(
				adTypeDistribution);
		return adAttributeProbabilityMaps;
	}
}
