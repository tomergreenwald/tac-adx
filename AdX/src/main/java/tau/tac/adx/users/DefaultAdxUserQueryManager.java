/*
 * DefaultUserQueryManager.java
 *
 * COPYRIGHT  2008
 * THE REGENTS OF THE UNIVERSITY OF MICHIGAN
 * ALL RIGHTS RESERVED
 *
 * PERMISSION IS GRANTED TO USE, COPY, CREATE DERIVATIVE WORKS AND REDISTRIBUTE THIS
 * SOFTWARE AND SUCH DERIVATIVE WORKS FOR NONCOMMERCIAL EDUCATION AND RESEARCH
 * PURPOSES, SO LONG AS NO FEE IS CHARGED, AND SO LONG AS THE COPYRIGHT NOTICE
 * ABOVE, THIS GRANT OF PERMISSION, AND THE DISCLAIMER BELOW APPEAR IN ALL COPIES
 * MADE; AND SO LONG AS THE NAME OF THE UNIVERSITY OF MICHIGAN IS NOT USED IN ANY
 * ADVERTISING OR PUBLICITY PERTAINING TO THE USE OR DISTRIBUTION OF THIS SOFTWARE
 * WITHOUT SPECIFIC, WRITTEN PRIOR AUTHORIZATION.
 *
 * THIS SOFTWARE IS PROVIDED AS IS, WITHOUT REPRESENTATION FROM THE UNIVERSITY OF
 * MICHIGAN AS TO ITS FITNESS FOR ANY PURPOSE, AND WITHOUT WARRANTY BY THE
 * UNIVERSITY OF MICHIGAN OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT
 * LIMITATION THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE REGENTS OF THE UNIVERSITY OF MICHIGAN SHALL NOT BE LIABLE FOR ANY
 * DAMAGES, INCLUDING SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WITH
 * RESPECT TO ANY CLAIM ARISING OUT OF OR IN CONNECTION WITH THE USE OF THE SOFTWARE,
 * EVEN IF IT HAS BEEN OR IS HEREAFTER ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package tau.tac.adx.users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import tau.tac.adx.AdxManager;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.props.PublisherCatalogEntry;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.util.EnumGenerator;

import com.google.inject.Inject;

import edu.umich.eecs.tac.user.User;
import edu.umich.eecs.tac.util.sampling.Sampler;
import edu.umich.eecs.tac.util.sampling.WheelSampler;

/**
 * {@link AdxUserQueryManager} implementation.
 * 
 * @author greenwald
 * 
 */
public class DefaultAdxUserQueryManager implements AdxUserQueryManager {

	/**
	 * {@link Map} between {@link User}s and an {@link AdxQuery} {@link Sampler}
	 * .
	 */
	private final Map<AdxUser, Sampler<AdxQuery>> querySamplers;

	/**
	 * {@link Device} distribution map. Each {@link Device} is associated with
	 * its relative popularity.
	 */
	private final Map<Device, Integer> deviceDeistributionMap;
	/**
	 * {@link AdType} distribution map. Each {@link AdType} is associated with
	 * its relative popularity.
	 */
	private final Map<AdType, Integer> adTypeDeistributionMap;

	/**
	 * @param catalog
	 *            {@link PublisherCatalog}.
	 * @param users
	 *            {@link List} of {@link AdxUser}s.
	 * @param deviceDistributionMap
	 *            {@link Device} distribution map. Each {@link Device} is
	 *            associated with its relative popularity.
	 * @param adTypeDistributionMap
	 *            {@link AdType} distribution map. Each {@link AdType} is
	 *            associated with its relative popularity.
	 * @param random
	 *            {@link Random}.
	 */
	@Inject
	public DefaultAdxUserQueryManager(PublisherCatalog catalog,
			List<AdxUser> users, Map<Device, Integer> deviceDistributionMap,
			Map<AdType, Integer> adTypeDistributionMap, Random random) {
		if (catalog == null) {
			throw new NullPointerException("Publisher catalog cannot be null");
		}

		if (users == null) {
			throw new NullPointerException("User list cannot be null");
		}

		if (random == null) {
			throw new NullPointerException(
					"Random number generator cannot be null");
		}

		if (deviceDistributionMap == null) {
			throw new NullPointerException(
					"Device distribution map cannot be null");
		}

		if (adTypeDistributionMap == null) {
			throw new NullPointerException(
					"Ad Type distribution map cannot be null");
		}
		this.deviceDeistributionMap = deviceDistributionMap;
		this.adTypeDeistributionMap = adTypeDistributionMap;
		querySamplers = buildQuerySamplers(catalog, users, random);
	}

	/**
	 * @see tau.tac.adx.users.AdxUserQueryManager#generateQuery(TacUser)
	 */
	@Override
	public AdxQuery generateQuery(AdxUser user) {
		Sampler<AdxQuery> sampler = querySamplers.get(user);
		if (sampler == null) {
			return null;
		}
		return sampler.getSample();
	}

	/**
	 * @see se.sics.tasim.aw.TimeListener#nextTimeUnit(int)
	 */
	@Override
	public void nextTimeUnit(int timeUnit) {
		// no implementation needed.
	}

	/**
	 * Builds a {@link Map} between {@link User}s and an {@link AdxQuery}
	 * {@link Sampler}.
	 * 
	 * @param catalog
	 *            {@link PublisherCatalog}.
	 * @param users
	 *            {@link List} of {@link AdxUser}s.
	 * @param random
	 *            {@link Random}.
	 * @return A {@link Map} between {@link User}s and an {@link AdxQuery}
	 *         {@link Sampler}.
	 */
	private Map<AdxUser, Sampler<AdxQuery>> buildQuerySamplers(
			PublisherCatalog catalog, List<AdxUser> users, Random random) {
		EnumGenerator<Device> deviceGenerator = new EnumGenerator<Device>(
				deviceDeistributionMap);
		EnumGenerator<AdType> adTypeGenerator = new EnumGenerator<AdType>(
				adTypeDeistributionMap);
		Map<AdxUser, Sampler<AdxQuery>> samplingMap = new HashMap<AdxUser, Sampler<AdxQuery>>();

		for (AdxUser user : users) {
			WheelSampler<AdxQuery> sampler = new WheelSampler<AdxQuery>(random);
			for (PublisherCatalogEntry publisherEntry : catalog) {
				Device device = deviceGenerator.randomType();
				AdType adType = adTypeGenerator.randomType();
				AdxQuery query = new AdxQuery(
						publisherEntry.getPublisherName(), user, device, adType);
				AdxPublisher publisher = AdxManager.getInstance().getPublisher(publisherEntry
						.getPublisherName());
				double weight = publisher.userAffiliation(user);
				sampler.addState(weight, query);
			}
			samplingMap.put(user, sampler);
		}
		return samplingMap;

	}

}
