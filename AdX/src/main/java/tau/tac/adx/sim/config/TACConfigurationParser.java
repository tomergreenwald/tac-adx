package tau.tac.adx.sim.config;

import static edu.umich.eecs.tac.util.permutation.CapacityAssignmentPermutation.secretPermutation;
import static tau.tac.adx.sim.TACAdxConstants.ADVERTISER;
import static tau.tac.adx.sim.TACAdxConstants.AD_NETOWRK_ROLE_ID;
import static tau.tac.adx.sim.TACAdxConstants.PUBLISHER;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.util.ConfigManager;
import se.sics.tasim.is.SimulationInfo;
import se.sics.tasim.sim.SimulationAgent;
import tau.tac.adx.sim.TACAdxSimulation;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryType;
import edu.umich.eecs.tac.props.ReserveInfo;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.SlotInfo;
import edu.umich.eecs.tac.props.UserClickModel;
import edu.umich.eecs.tac.sim.CapacityType;

public class TACConfigurationParser {

	private final ConfigManager config;
	private final Random random;
	private final Logger log = Logger.getLogger(TACConfigurationParser.class
			.getName());

	/**
	 * @param config
	 */
	public TACConfigurationParser(ConfigManager config) {
		this.config = config;
		random = new Random();
	}

	public SlotInfo createSlotInfo() {
		SlotInfo slotInfo = new SlotInfo();

		int promotedSlotsMin = config.getPropertyAsInt(
				"publisher.promoted.slots.min", 0);
		int promotedSlotsMax = config.getPropertyAsInt(
				"publisher.promoted.slots.max", 0);
		int regularSlots = config
				.getPropertyAsInt("publisher.regular.slots", 0);
		double promotedSlotBonus = config.getPropertyAsDouble(
				"publisher.promoted.slotbonus", 0.0);

		slotInfo.setPromotedSlots(sample(promotedSlotsMin, promotedSlotsMax));

		slotInfo.setRegularSlots(regularSlots);
		slotInfo.setPromotedSlotBonus(promotedSlotBonus);

		return slotInfo;
	}

	public ReserveInfo createReserveInfo() {
		ReserveInfo reserveInfo = new ReserveInfo();

		double promotedReserveBoost = config.getPropertyAsDouble(
				"publisher.promoted.reserve.boost", 0.0);

		double regularReserveMinF0 = config.getPropertyAsDouble(
				"publisher.regular.reserve.FOCUS_LEVEL_ZERO.min", 0.0);
		double regularReserveMaxF0 = config.getPropertyAsDouble(
				"publisher.regular.reserve.FOCUS_LEVEL_ZERO.max", 0.0);
		double regularReserveMinF1 = config.getPropertyAsDouble(
				"publisher.regular.reserve.FOCUS_LEVEL_ONE.min", 0.0);
		double regularReserveMaxF1 = config.getPropertyAsDouble(
				"publisher.regular.reserve.FOCUS_LEVEL_ONE.max", 0.0);
		double regularReserveMinF2 = config.getPropertyAsDouble(
				"publisher.regular.reserve.FOCUS_LEVEL_TWO.min", 0.0);
		double regularReserveMaxF2 = config.getPropertyAsDouble(
				"publisher.regular.reserve.FOCUS_LEVEL_TWO.max", 0.0);

		reserveInfo.setRegularReserve(QueryType.FOCUS_LEVEL_ZERO,
				sample(regularReserveMinF0, regularReserveMaxF0));
		reserveInfo.setRegularReserve(QueryType.FOCUS_LEVEL_ONE,
				sample(regularReserveMinF1, regularReserveMaxF1));
		reserveInfo.setRegularReserve(QueryType.FOCUS_LEVEL_TWO,
				sample(regularReserveMinF2, regularReserveMaxF2));
		// reserveInfo.setRegularReserve(sample(regularReserveMin,
		// regularReserveMax));
		reserveInfo.setPromotedReserve(
				QueryType.FOCUS_LEVEL_ZERO,
				sample(reserveInfo
						.getRegularReserve(QueryType.FOCUS_LEVEL_ZERO),
						reserveInfo
								.getRegularReserve(QueryType.FOCUS_LEVEL_ZERO)
								+ promotedReserveBoost));
		reserveInfo
				.setPromotedReserve(
						QueryType.FOCUS_LEVEL_ONE,
						sample(reserveInfo
								.getRegularReserve(QueryType.FOCUS_LEVEL_ONE),
								reserveInfo
										.getRegularReserve(QueryType.FOCUS_LEVEL_ONE)
										+ promotedReserveBoost));
		reserveInfo
				.setPromotedReserve(
						QueryType.FOCUS_LEVEL_TWO,
						sample(reserveInfo
								.getRegularReserve(QueryType.FOCUS_LEVEL_TWO),
								reserveInfo
										.getRegularReserve(QueryType.FOCUS_LEVEL_TWO)
										+ promotedReserveBoost));

		return reserveInfo;
	}

	private double sample(double min, double max) {
		return min + random.nextDouble() * (max - min);
	}

	private int sample(int min, int max) {
		return min + (max > min ? random.nextInt(max - min) : 0);
	}

	public RetailCatalog createRetailCatalog() {
		RetailCatalog catalog = new RetailCatalog();

		String[] skus = config.getPropertyAsArray("catalog.sku");

		for (String sku : skus) {
			String manufacturer = config.getProperty(String.format(
					"catalog.%s.manufacturer", sku));
			String component = config.getProperty(String.format(
					"catalog.%s.component", sku));
			double salesProfit = config.getPropertyAsDouble(
					String.format("catalog.%s.salesProfit", sku), 0.0);

			Product product = new Product(manufacturer, component);

			catalog.setSalesProfit(product, salesProfit);
		}

		catalog.lock();

		return catalog;
	}

	public void initializeAdvertisers(TACAdxSimulation tacAdxSimulation) {
		Random r = new Random();

		SimulationAgent[] publishers = tacAdxSimulation.getAgents(PUBLISHER);

		String publisherAddress = "publisher";
		if (publishers != null && publishers.length > 0
				&& publishers[0] != null) {
			publisherAddress = publishers[0].getAddress();
		}

		int highValue = config.getPropertyAsInt("advertiser.capacity.high", 0);
		int medValue = config.getPropertyAsInt("advertiser.capacity.med", 0);
		int lowValue = config.getPropertyAsInt("advertiser.capacity.low", 0);

		int highCount = config.getPropertyAsInt(
				"advertiser.capacity.highCount", 0);
		int lowCount = config.getPropertyAsInt("advertiser.capacity.lowCount",
				0);

		double manufacturerBonus = config.getPropertyAsDouble(
				"advertiser.specialization.manufacturerBonus", 0.0);
		double componentBonus = config.getPropertyAsDouble(
				"advertiser.specialization.componentBonus", 0.0);
		double decayRate = config.getPropertyAsDouble(
				"advertiser.capacity.distribution_capacity_discounter", 1.0);
		double targetEffect = config.getPropertyAsDouble(
				"advertiser.targeteffect", 0.5);
		int window = config.getPropertyAsInt("advertiser.capacity.window", 7);

		double focusEffectF0 = config.getPropertyAsDouble(
				"advertiser.focuseffect.FOCUS_LEVEL_ZERO", 1.0);
		double focusEffectF1 = config.getPropertyAsDouble(
				"advertiser.focuseffect.FOCUS_LEVEL_ONE", 1.0);
		double focusEffectF2 = config.getPropertyAsDouble(
				"advertiser.focuseffect.FOCUS_LEVEL_TWO", 1.0);

		SimulationAgent[] adnetowrkAgents = tacAdxSimulation
				.getAgents(AD_NETOWRK_ROLE_ID);
		for (int i = 0; i < adnetowrkAgents.length; i++) {
			tacAdxSimulation.getAdxAdvertiserAddresses()[i] = adnetowrkAgents[i]
					.getAddress();
		}
		// Initialize advertisers..
		SimulationAgent[] advertisers = tacAdxSimulation.getAgents(ADVERTISER);
		tacAdxSimulation
				.setAdvertiserInfoMap(new HashMap<String, AdvertiserInfo>());

		if (advertisers != null) {

			// Create capacities and either assign or randomize
			int[] capacities = new int[advertisers.length];

			// check simulationInfo
			SimulationInfo info = tacAdxSimulation.getSimulationInfo();

			if (tacAdxSimulation.getCompetition() == null) {

				log.log(Level.INFO, "Using random capacity assigments");
				for (int i = 0; i < highCount && i < capacities.length; i++) {
					capacities[i] = highValue;
				}

				for (int i = highCount; i < highCount + lowCount
						&& i < capacities.length; i++) {
					capacities[i] = lowValue;
				}

				for (int i = highCount + lowCount; i < capacities.length; i++) {
					capacities[i] = medValue;
				}

				for (int i = 0; i < capacities.length; i++) {
					int rindex = i + r.nextInt(capacities.length - i);

					int sw = capacities[i];
					capacities[i] = capacities[rindex];
					capacities[rindex] = sw;

				}

			} else {

				int secret = config.getPropertyAsInt("game.secret", 0);

				CapacityType[] types = secretPermutation(secret,
						info.getSimulationID(), tacAdxSimulation
								.getCompetition().getStartSimulationID());

				log.log(Level.INFO, "Using permuted capacity assigments");

				for (int i = 0; i < capacities.length; i++) {
					int cap = 0;

					switch (types[i]) {
					case LOW:
						cap = lowValue;
						break;
					case MED:
						cap = medValue;
						break;
					case HIGH:
						cap = highValue;
						break;
					}

					capacities[i] = cap;
				}
			}

			for (int i = 0, n = advertisers.length; i < n; i++) {
				SimulationAgent agent = advertisers[i];

				String agentAddress = agent.getAddress();
				tacAdxSimulation.getAdvertiserAddresses()[i] = agentAddress;

				AdvertiserInfo advertiserInfo = new AdvertiserInfo();
				advertiserInfo.setAdvertiserId(agentAddress);
				advertiserInfo.setPublisherId(publisherAddress);
				advertiserInfo.setDistributionCapacity(capacities[i]);
				advertiserInfo.setDistributionCapacityDiscounter(decayRate);
				advertiserInfo.setComponentSpecialty(tacAdxSimulation
						.getComponents()[r.nextInt(tacAdxSimulation
						.getComponents().length)]);
				advertiserInfo.setComponentBonus(componentBonus);
				advertiserInfo.setManufacturerSpecialty(tacAdxSimulation
						.getManufacturers()[r.nextInt(tacAdxSimulation
						.getManufacturers().length)]);
				advertiserInfo.setManufacturerBonus(manufacturerBonus);
				advertiserInfo.setDistributionWindow(window);
				advertiserInfo.setTargetEffect(targetEffect);
				advertiserInfo.setFocusEffects(QueryType.FOCUS_LEVEL_ZERO,
						focusEffectF0);
				advertiserInfo.setFocusEffects(QueryType.FOCUS_LEVEL_ONE,
						focusEffectF1);
				advertiserInfo.setFocusEffects(QueryType.FOCUS_LEVEL_TWO,
						focusEffectF2);
				advertiserInfo.lock();

				tacAdxSimulation.getAdvertiserInfoMap().put(agentAddress,
						advertiserInfo);

				// Create bank account for the advertiser
				// tacAdxSimulation.getBank().addAccount(agentAddress);
				tacAdxSimulation.getSalesAnalyst().addAccount(agentAddress);
			}
		}
	}

	public String[] createManufacturers(RetailCatalog retailCatalog) {
		return retailCatalog.getManufacturers().toArray(new String[0]);
	}

	public String[] createComponents(RetailCatalog retailCatalog) {
		return retailCatalog.getComponents().toArray(new String[0]);
	}

	public UserClickModel createUserClickModel(TACAdxSimulation tacAdxSimulation) {

		String[] advertisers = tacAdxSimulation.getAdvertiserAddresses();
		Set<Query> queryList = new HashSet<Query>();

		for (Product product : tacAdxSimulation.getRetailCatalog()) {
			// Create f0
			Query f0 = new Query();

			// Create f1's
			Query f1Manufacturer = new Query(product.getManufacturer(), null);
			Query f1Component = new Query(null, product.getComponent());

			// Create f2
			Query f2 = new Query(product.getManufacturer(),
					product.getComponent());

			queryList.add(f0);
			queryList.add(f1Manufacturer);
			queryList.add(f1Component);
			queryList.add(f2);
		}

		Query[] queries = queryList.toArray(new Query[0]);

		UserClickModel clickModel = new UserClickModel(queries, advertisers);

		Random random = tacAdxSimulation.getRandom();

		for (int queryIndex = 0; queryIndex < clickModel.queryCount(); queryIndex++) {
			double continuationLow = config.getPropertyAsDouble(String.format(
					"users.clickbehavior.continuationprobability.%s.low",
					clickModel.query(queryIndex).getType()), 0.1);
			double continuationHigh = config.getPropertyAsDouble(String.format(
					"users.clickbehavior.continuationprobability.%s.high",
					clickModel.query(queryIndex).getType()), 0.9);
			double effectLow = config.getPropertyAsDouble(String.format(
					"users.clickbehavior.advertisereffect.%s.low", clickModel
							.query(queryIndex).getType()), 0.1);
			double effectHigh = config.getPropertyAsDouble(String.format(
					"users.clickbehavior.advertisereffect.%s.high", clickModel
							.query(queryIndex).getType()), 0.9);

			double continuationProbability = Math.max(
					Math.min(1.0, random.nextDouble()
							* (continuationHigh - continuationLow)
							+ continuationLow), 0.0);

			clickModel.setContinuationProbability(queryIndex,
					continuationProbability);

			for (int advertiserIndex = 0; advertiserIndex < clickModel
					.advertiserCount(); advertiserIndex++) {
				double effect = Math.max(
						Math.min(1.0, random.nextDouble()
								* (effectHigh - effectLow) + effectLow), 0.0);
				clickModel.setAdvertiserEffect(queryIndex, advertiserIndex,
						effect);
			}
		}

		clickModel.lock();

		return clickModel;
	}
}
