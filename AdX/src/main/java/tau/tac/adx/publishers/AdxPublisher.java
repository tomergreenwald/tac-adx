/**
 * 
 */
package tau.tac.adx.publishers;

import java.text.ParseException;
import java.util.Map;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import tau.tac.adx.ads.properties.AdAttributeProbabilityMaps;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.publishers.reserve.ReservePriceManager;
import tau.tac.adx.sim.Publisher;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.properties.AdxUserAttributeProbabilityMaps;
import edu.umich.eecs.tac.props.KeyedEntry;

/**
 * Defines a publisher in the Ad-Exchange system.<br>
 * A publisher is defined by the following properties: <li>
 * {@link AdxUserAttributeProbabilityMaps Probability maps} to define how likely
 * a user with a certain attribute is to visit the {@link Publisher}.</li> <li>
 * A probability {@link Map} of how likely an ad of type {@link AdType} is to be
 * shown by this {@link Publisher}.</li> <li>
 * The relative popularity of web site w, i.e., the probability of an arbitrary
 * user visiting the web site.</li> <li>{@link Double pImpressions} Every user
 * visit to a web site results in one or more impression (i.e., ad opportunities
 * ) according to a predefined probability distribution.</li> <li>
 * The {@link AdxPublisher}'s {@link ReservePriceManager}.</li>
 * 
 * @author greenwald
 * 
 */
public class AdxPublisher implements KeyedEntry<AdxPublisher> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8433553718771938842L;

	/**
	 * A probability {@link Map} of how likely an ad of type {@link AdType} is
	 * to be shown by this {@link Publisher}.
	 */
	private Map<AdType, Double> adTypeDistribution;

	/**
	 * A probability {@link Map} of how likely a {@link AdxUser user} with
	 * {@link Device} is to visit this {@link Publisher}.
	 */
	private Map<Device, Double> deviceProbabilityMap;

	/**
	 * {@link AdxPublisher Publisher's} name.
	 */
	private String name;

	/**
	 * Every user visit to a web site results in one or more impression (i.e.,
	 * ad opportunities ) according to a predefined probability distribution.
	 */
	private double pImpressions;

	/**
	 * {@link AdxUserAttributeProbabilityMaps Probability maps} to define how
	 * likely a user with a certain attribute is to visit the {@link Publisher}.
	 */
	private AdxUserAttributeProbabilityMaps probabilityMaps;

	/**
	 * The relative popularity of web site w, i.e., the probability of an
	 * arbitrary user visiting the web site.
	 */
	private double relativePopularity;

	/**
	 * The {@link AdxPublisher}'s {@link ReservePriceManager}.
	 */
	private ReservePriceManager reservePriceManager;

	/**
	 * @param probabilityMaps
	 *            {@link AdxUserAttributeProbabilityMaps} to define how likely a
	 *            user with a certain attribute is to visit the
	 *            {@link Publisher}.
	 * @param adAttributeProbabilityMaps
	 *            {@link AdAttributeProbabilityMaps} of how likely an ad of type
	 *            {@link AdType} is to be shown by this {@link Publisher}.
	 * @param deviceProbabilityMap
	 *            A probability {@link Map} of how likely a {@link AdxUser user}
	 *            with {@link Device} is to visit this {@link Publisher}.
	 * @param relativePopularity
	 *            The relative popularity of web site w, i.e., the probability
	 *            of an arbitrary user visiting the web site.
	 * @param pImpressions
	 *            Every user visit to a web site results in one or more
	 *            impression (i.e., ad opportunities ) according to a predefined
	 *            probability distribution.
	 * @param reservePriceManager
	 *            The {@link AdxPublisher}'s {@link ReservePriceManager}.
	 * @param name
	 *            {@link AdxPublisher Publisher's} name.
	 */
	public AdxPublisher(AdxUserAttributeProbabilityMaps probabilityMaps,
			AdAttributeProbabilityMaps adAttributeProbabilityMaps,
			Map<Device, Double> deviceProbabilityMap,
			double relativePopularity, double pImpressions,
			ReservePriceManager reservePriceManager, String name) {
		this.probabilityMaps = probabilityMaps;
		this.adTypeDistribution = adAttributeProbabilityMaps
				.getAdTypeDistribution();
		this.relativePopularity = relativePopularity;
		this.pImpressions = pImpressions;
		this.reservePriceManager = reservePriceManager;
		this.deviceProbabilityMap = deviceProbabilityMap;
		this.name = name;
	}

	/**
	 * @return the adTypeDistribution
	 */
	public Map<AdType, Double> getAdTypeDistribution() {
		return adTypeDistribution;
	}

	/**
	 * @return the deviceProbabilityMap
	 */
	public Map<Device, Double> getDeviceProbabilityMap() {
		return deviceProbabilityMap;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the pImpressions
	 */
	public double getpImpressions() {
		return pImpressions;
	}

	/**
	 * @return the probabilityMaps
	 */
	public AdxUserAttributeProbabilityMaps getProbabilityMaps() {
		return probabilityMaps;
	}

	/**
	 * @return the relativePopularity
	 */
	public double getRelativePopularity() {
		return relativePopularity;
	}

	/**
	 * @return the reservePriceManager
	 */
	public ReservePriceManager getReservePriceManager() {
		return reservePriceManager;
	}

	/**
	 * @param adTypeDistribution
	 *            the adTypeDistribution to set
	 */
	public void setAdTypeDistribution(Map<AdType, Double> adTypeDistribution) {
		this.adTypeDistribution = adTypeDistribution;
	}

	/**
	 * @param deviceProbabilityMap
	 *            the deviceProbabilityMap to set
	 */
	public void setDeviceProbabilityMap(Map<Device, Double> deviceProbabilityMap) {
		this.deviceProbabilityMap = deviceProbabilityMap;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param pImpressions
	 *            the pImpressions to set
	 */
	public void setpImpressions(double pImpressions) {
		this.pImpressions = pImpressions;
	}

	/**
	 * @param probabilityMaps
	 *            the probabilityMaps to set
	 */
	public void setProbabilityMaps(
			AdxUserAttributeProbabilityMaps probabilityMaps) {
		this.probabilityMaps = probabilityMaps;
	}

	/**
	 * @param relativePopularity
	 *            the relativePopularity to set
	 */
	public void setRelativePopularity(double relativePopularity) {
		this.relativePopularity = relativePopularity;
	}

	/**
	 * @param reservePriceManager
	 *            the reservePriceManager to set
	 */
	public void setReservePriceManager(ReservePriceManager reservePriceManager) {
		this.reservePriceManager = reservePriceManager;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((adTypeDistribution == null) ? 0 : adTypeDistribution
						.hashCode());
		result = prime
				* result
				+ ((deviceProbabilityMap == null) ? 0 : deviceProbabilityMap
						.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		long temp;
		temp = Double.doubleToLongBits(pImpressions);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((probabilityMaps == null) ? 0 : probabilityMaps.hashCode());
		temp = Double.doubleToLongBits(relativePopularity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		AdxPublisher other = (AdxPublisher) obj;
		if (adTypeDistribution == null) {
			if (other.adTypeDistribution != null)
				return false;
		} else if (!adTypeDistribution.equals(other.adTypeDistribution))
			return false;
		if (deviceProbabilityMap == null) {
			if (other.deviceProbabilityMap != null)
				return false;
		} else if (!deviceProbabilityMap.equals(other.deviceProbabilityMap))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(pImpressions) != Double
				.doubleToLongBits(other.pImpressions))
			return false;
		if (probabilityMaps == null) {
			if (other.probabilityMaps != null)
				return false;
		} else if (!probabilityMaps.equals(other.probabilityMaps))
			return false;
		if (Double.doubleToLongBits(relativePopularity) != Double
				.doubleToLongBits(other.relativePopularity))
			return false;
		return true;
	}

	/**
	 * @see se.sics.isl.transport.Transportable#getTransportName()
	 */
	@Override
	public String getTransportName() {
		return getClass().getName();
	}

	/**
	 * @see se.sics.isl.transport.Transportable#read(se.sics.isl.transport.TransportReader)
	 */
	@Override
	public synchronized void read(TransportReader reader) throws ParseException {
		AdxPublisher publisherReader = (AdxPublisher) reader
				.readTransportable();
		adTypeDistribution = publisherReader.getAdTypeDistribution();
		deviceProbabilityMap = publisherReader.getDeviceProbabilityMap();
		name = publisherReader.getName();
		pImpressions = publisherReader.getpImpressions();
		probabilityMaps = publisherReader.getProbabilityMaps();
		relativePopularity = publisherReader.getRelativePopularity();
		reservePriceManager = publisherReader.getReservePriceManager();
	}

	/**
	 * @see se.sics.isl.transport.Transportable#write(se.sics.isl.transport.TransportWriter)
	 */
	@Override
	public synchronized void write(TransportWriter writer) {
		writer.write(this);
	}

	/**
	 * @see edu.umich.eecs.tac.props.KeyedEntry#getKey()
	 */
	@Override
	public AdxPublisher getKey() {
		return this;
	}

	/**
	 * Determines how likely a {@link AdxUser user} is to visit this
	 * {@link AdxPublisher publisher}.
	 * 
	 * @param user
	 *            {@link AdxUser}.
	 * @return How likely the {@link AdxUser user} is to visit this
	 *         {@link AdxPublisher publisher} with {@link Device device}.
	 */
	public double userAffiliation(AdxUser user) {
		double d = probabilityMaps.getAgeDistribution().get(user.getAge())
				* probabilityMaps.getGenderDistribution().get(user.getGender())
				* probabilityMaps.getIncomeDistribution().get(user.getIncome())
				* relativePopularity;
		return d;
	}

}
