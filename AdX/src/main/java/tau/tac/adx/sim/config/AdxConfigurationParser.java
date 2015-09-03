package tau.tac.adx.sim.config;

import static tau.tac.adx.sim.TACAdxConstants.AD_NETOWRK_ROLE_ID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import se.sics.isl.util.ConfigManager;
import se.sics.tasim.sim.SimulationAgent;
import tau.tac.adx.ads.properties.AdAttributeProbabilityMaps;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.parser.Auctions.ReservePriceManagerBundle;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.props.ReservePriceType;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.publishers.reserve.MultiReservePriceManager;
import tau.tac.adx.publishers.reserve.PredeterminedReservePriceManager;
import tau.tac.adx.publishers.reserve.UserAdTypeReservePriceManager;
import tau.tac.adx.sim.TACAdxSimulation;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.generators.PopulationUserGenerator;
import tau.tac.adx.users.properties.AdxUserAttributeProbabilityMaps;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;
import edu.umich.eecs.tac.props.AdvertiserInfo;

/**
 * 
 * @author greenwald
 * 
 */
public class AdxConfigurationParser {

	private final ConfigManager config;
	private Random random;

	public static String[] publisherNames = { "yahoo", "cnn", "nyt", "hfn",
			"msn", "fox", "amazon", "ebay", "wallmart", "target", "bestbuy",
			"sears", "webmd", "ehow", "ask", "tripadvisor", "cnet", "weather" };

	private double[] ratings = { 0.16, 0.022, 0.031, 0.081, 0.182, 0.031,
			0.128, 0.085, 0.38, 0.02, 0.016, 0.016, 0.025, 0.025, 0.05, 0.016,
			0.17, 0.058 };

	private double[] adTypeText = { 0.7, 0.5, 0.5, 0.7, 0.5, 0.9, 0.7, 0.5,
			0.9, 0.7, 0.5, 0.9, 0.7, 0.5, 0.5, 0.9, 0.7, 0.5 };

	private double[] adTypeVideo = { 0.3, 0.5, 0.5, 0.3, 0.5, 0.1, 0.3, 0.5,
			0.1, 0.3, 0.5, 0.1, 0.3, 0.5, 0.5, 0.1, 0.3, 0.5 };

	private double[] age1 = { 0.122, 0.102, 0.092, 0.102, 0.102, 0.092, 0.092,
			0.092, 0.072, 0.092, 0.102, 0.092, 0.092, 0.102, 0.102, 0.082,
			0.122, 0.092 };
	private double[] age2 = { 0.171, 0.161, 0.151, 0.161, 0.161, 0.151, 0.151,
			0.161, 0.151, 0.171, 0.141, 0.121, 0.151, 0.151, 0.131, 0.161,
			0.151, 0.151 };
	private double[] age3 = { 0.167, 0.167, 0.167, 0.167, 0.167, 0.167, 0.167,
			0.157, 0.167, 0.177, 0.167, 0.167, 0.157, 0.157, 0.157, 0.177,
			0.157, 0.167 };
	private double[] age4 = { 0.184, 0.194, 0.194, 0.194, 0.194, 0.194, 0.194,
			0.194, 0.204, 0.184, 0.204, 0.204, 0.194, 0.194, 0.204, 0.204,
			0.184, 0.204 };
	private double[] age5 = { 0.164, 0.174, 0.174, 0.174, 0.174, 0.184, 0.184,
			0.174, 0.184, 0.173, 0.174, 0.184, 0.184, 0.174, 0.184, 0.174,
			0.174, 0.184 };
	private double[] age6 = { 0.192, 0.202, 0.202, 0.202, 0.202, 0.212, 0.212,
			0.222, 0.222, 0.202, 0.212, 0.232, 0.222, 0.222, 0.222, 0.212,
			0.212, 0.202 };

	private double[] genderMale = { 0.496, 0.486, 0.476, 0.466, 0.476, 0.486,
			0.476, 0.486, 0.456, 0.456, 0.476, 0.466, 0.456, 0.476, 0.486,
			0.466, 0.506, 0.476 };

	private double[] income1 = { 0.53, 0.48, 0.47, 0.47, 0.49, 0.46, 0.50,
			0.50, 0.17, 0.45, 0.465, 0.45, 0.46, 0.50, 0.50, 0.465, 0.48,
			0.455, };
	private double[] income2 = { 0.27, 0.27, 0.26, 0.27, 0.27, 0.26, 0.27,
			0.27, 0.47, 0.27, 0.26, 0.25, 0.265, 0.27, 0.28, 0.26, 0.265, 0.265 };
	private double[] income3 = { 0.13, 0.16, 0.17, 0.17, 0.16, 0.18, 0.15,
			0.15, 0.28, 0.19, 0.18, 0.20, 0.185, 0.15, 0.15, 0.175, 0.165,
			0.185 };
	private double[] income4 = { 0.07, 0.09, 0.1, 0.09, 0.08, 0.10, 0.08, 0.08,
			0.17, 0.09, 0.095, 0.10, 0.09, 0.08, 0.07, 0.10, 0.09, 0.095, };

	private double[] devicePc = { 0.74, 0.76, 0.77, 0.78, 0.75, 0.76, 0.79,
			0.78, 0.82, 0.81, 0.8, 0.81, 0.76, 0.72, 0.72, 0.70, 0.73, 0.69 };
	private double[] deviceMobile = { 0.26, 0.24, 0.23, 0.22, 0.25, 0.24, 0.21,
			0.22, 0.18, 0.19, 0.2, 0.19, 0.24, 0.28, 0.28, 0.30, 0.27, 0.31 };

	private static double RESERVE_PRICE_INIT = 0.005;
	private static double RESERVE_PRICE_VARIANCE = 0.02;
	private static double RESERVE_PRICE_LEARN_RATE = 0.2;

	/**
	 * @param config
	 */
	public AdxConfigurationParser(ConfigManager config) {
		this.config = config;
		random = new Random();
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
		Map<AdxUser, Integer> weights = new HashMap<AdxUser, Integer>();
		int populationSize = config.getPropertyAsInt(
				"adxusers.population_size", 0);

		int populationTypesSize = config.getPropertyAsInt(
				"population.types.size", 0);
		for (int i = 1; i <= populationTypesSize; i++) {
			Age age = Age.valueOf(config.getProperty(String.format(
					"population.%s.age", i)));
			Gender gender = Gender.valueOf(config.getProperty(String.format(
					"population.%s.gender", i)));
			Income income = Income.valueOf(config.getProperty(String.format(
					"population.%s.income", i)));
			int probability = config.getPropertyAsInt(
					String.format("population.%s.probability", i), 0);
			double pContinue = config.getPropertyAsDouble(
					String.format("adxusers.pcontinue", i), 0);
			AdxUser adxUser = new AdxUser(age, gender, income, pContinue, 0);
			weights.put(adxUser, probability);
		}

		PopulationUserGenerator generator = new PopulationUserGenerator(weights);
		return generator.generate(populationSize);
	}

	/**
	 * Generates a {@link PublisherCatalog} from the configuration file.
	 * 
	 * @return {@link PublisherCatalog} parsed from the configuration file.
	 */
	public PublisherCatalog createPublisherCatalog() {
		Random r = new Random();

		PublisherCatalog catalog = new PublisherCatalog();
		String[] skus1 = config.getPropertyAsArray("publishers.list.1");
		String[] skus2 = config.getPropertyAsArray("publishers.list.2");
		String[] skus3 = config.getPropertyAsArray("publishers.list.3");

		/*
		 * select a subset for this instance : assuming items of skus are
		 * distinct
		 */
		Set<Integer> subsetskus = new HashSet<Integer>();
		int subsetsize = config.getPropertyAsInt("publishers.subset.size", 2);

		while (subsetskus.size() < subsetsize) {
			subsetskus.add(Integer.parseInt(skus1[r.nextInt(skus1.length)]));
		}
		while (subsetskus.size() < 2 * subsetsize) {
			subsetskus.add(Integer.parseInt(skus2[r.nextInt(skus2.length)]));
		}
		while (subsetskus.size() < 3 * subsetsize) {
			subsetskus.add(Integer.parseInt(skus3[r.nextInt(skus3.length)]));
		}

		int reservePriceManagerType = config.getPropertyAsInt(
				"publishers.reserve_price_manager", -1);
		String[] reservePriceManagerConfig = config.getPropertyAsArray(
				"publishers.reserve_price_manager_config", "");
		if (reservePriceManagerType == -1) {
			reservePriceManagerType = random.nextInt(3);
		}

		catalog.reservePriceType = ReservePriceType.values()[reservePriceManagerType];

		for (Integer sku : subsetskus) {
			String name = publisherNames[sku];
			double rating = ratings[sku];

			AdAttributeProbabilityMaps adAttributeProbabilityMaps = extractAdTypeAffiliation(sku);
			AdxUserAttributeProbabilityMaps adxUserAttributeProbabilityMaps = extractUserAffiliation(sku);
			Map<Device, Double> deviceAffiliation = extractDeviceAffiliation(sku);
			MultiReservePriceManager<AdxQuery> reservePriceManager = generateReservePriceManager(
					reservePriceManagerType, reservePriceManagerConfig);
			AdxPublisher publisher = new AdxPublisher(
					adxUserAttributeProbabilityMaps,
					adAttributeProbabilityMaps, deviceAffiliation, rating, 0,
					reservePriceManager, name);
			catalog.addPublisher(publisher);
		}

		catalog.lock();

		return catalog;
	}

	private MultiReservePriceManager<AdxQuery> generateReservePriceManager(
			int reservePriceManagerType, String[] reservePriceManagerConfig) {
		switch (reservePriceManagerType) {
		case 0:
			return new PredeterminedReservePriceManager(new double[] { 0, 0, 0,
					0, 0, 0, 0, 0 });
		case 1:
			return new UserAdTypeReservePriceManager(RESERVE_PRICE_INIT,
					RESERVE_PRICE_VARIANCE, RESERVE_PRICE_LEARN_RATE);
		case 2:
			ReservePriceManagerBundle priceBundle;
			double[] coefficients = new double[reservePriceManagerConfig.length];
			for (int i = 0; i < coefficients.length; i++) {
				coefficients[i] = Double
						.parseDouble(reservePriceManagerConfig[i]);
			}
			return new PredeterminedReservePriceManager(coefficients);
		case 3:
			throw new RuntimeException(
					"Reserve price manager type not supported yet - "
							+ reservePriceManagerType);
		default:
			throw new RuntimeException(
					"Reserve price manager type is not in range - "
							+ reservePriceManagerType);
		}
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
	private Map<Device, Double> extractDeviceAffiliation(Integer sku) {
		Map<Device, Double> deviceDistribution = new HashMap<Device, Double>();
		deviceDistribution.put(Device.pc, devicePc[sku]);
		deviceDistribution.put(Device.mobile, deviceMobile[sku]);
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
	private AdxUserAttributeProbabilityMaps extractUserAffiliation(Integer sku) {
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
	private Map<Income, Double> extractIncomeDistribution(Integer sku) {
		Map<Income, Double> ageDistribution = new HashMap<Income, Double>();
		ageDistribution.put(Income.low, income1[sku]);
		ageDistribution.put(Income.medium, income2[sku]);
		ageDistribution.put(Income.high, income3[sku]);
		ageDistribution.put(Income.very_high, income4[sku]);
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
	private Map<Gender, Double> extractGenderDistribution(Integer sku) {
		Map<Gender, Double> genderDistribution = new HashMap<Gender, Double>();
		double maleAffiliation = genderMale[sku];
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
	private Map<Age, Double> extractAgeDistribution(Integer sku) {
		Map<Age, Double> ageDistribution = new HashMap<Age, Double>();
		ageDistribution.put(Age.Age_18_24, age1[sku]);
		ageDistribution.put(Age.Age_25_34, age2[sku]);
		ageDistribution.put(Age.Age_35_44, age3[sku]);
		ageDistribution.put(Age.Age_45_54, age4[sku]);
		ageDistribution.put(Age.Age_55_64, age5[sku]);
		ageDistribution.put(Age.Age_65_PLUS, age6[sku]);
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
	private AdAttributeProbabilityMaps extractAdTypeAffiliation(Integer sku) {
		Map<AdType, Double> adTypeDistribution = new HashMap<AdType, Double>();
		adTypeDistribution.put(AdType.text, adTypeText[sku]);
		adTypeDistribution.put(AdType.video, adTypeVideo[sku]);
		AdAttributeProbabilityMaps adAttributeProbabilityMaps = new AdAttributeProbabilityMaps(
				adTypeDistribution);
		return adAttributeProbabilityMaps;
	}

	public void initializeAdvertisers(TACAdxSimulation tacAdxSimulation) {
		// Initialize advertisers..
		SimulationAgent[] adnetowrkAgents = tacAdxSimulation
				.getAgents(AD_NETOWRK_ROLE_ID);
		tacAdxSimulation
				.setAdvertiserInfoMap(new HashMap<String, AdvertiserInfo>());

		for (int i = 0, n = adnetowrkAgents.length; i < n; i++) {
			tacAdxSimulation.getAdxAdvertiserAddresses()[i] = adnetowrkAgents[i]
					.getAddress();
			SimulationAgent agent = adnetowrkAgents[i];

			String agentAddress = agent.getAddress();
			tacAdxSimulation.getAdvertiserAddresses()[i] = agentAddress;

			AdvertiserInfo advertiserInfo = new AdvertiserInfo();
			advertiserInfo.setAdvertiserId(agentAddress);
			advertiserInfo.lock();

			tacAdxSimulation.getAdvertiserInfoMap().put(agentAddress,
					advertiserInfo);

			// Create bank account for the advertiser
			tacAdxSimulation.getBank().addAccount(agentAddress);
		}
	}

}
