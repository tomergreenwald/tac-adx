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
package edu.umich.eecs.tac.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.util.sampling.MutableSampler;
import edu.umich.eecs.tac.util.sampling.Sampler;
import edu.umich.eecs.tac.util.sampling.WheelSampler;

/**
 * @author Patrick Jordan
 */
public class DefaultUserQueryManager implements UserQueryManager {
	private Map<User, Sampler<Query>> querySamplers;

	public DefaultUserQueryManager(RetailCatalog catalog) {
		this(catalog, new Random());
	}

	public DefaultUserQueryManager(RetailCatalog catalog, Random random) {
		if (catalog == null) {
			throw new NullPointerException("Retail catalog cannot be null");
		}

		if (random == null) {
			throw new NullPointerException(
					"Random number generator cannot be null");
		}

		querySamplers = buildQuerySamplers(catalog, random);
	}

	public Query generateQuery(User user) {
		if (user.isSearching()) {
			return querySamplers.get(user).getSample();
		} else {
			return null;
		}
	}

	public void nextTimeUnit(int timeUnit) {

	}

	private Map<User, Sampler<Query>> buildQuerySamplers(RetailCatalog catalog,
			Random random) {
		// PRJ: Use query samplers so that new Query objects are not
		// instantiated after each invocation of generateQuery.

		// The number of possible search state users is 4 times the number of
		// products.
		Map<User, Sampler<Query>> map = new HashMap<User, Sampler<Query>>(
				catalog.size() * 4);

		for (Product product : catalog) {
			// Queries
			Query f0 = new Query(null, null);
			Query f1_manufacturer = new Query(product.getManufacturer(), null);
			Query f1_component = new Query(null, product.getComponent());
			Query f2 = new Query(product.getManufacturer(), product
					.getComponent());

			// Querying users
			User isUser = new User();
			isUser.setProduct(product);
			isUser.setState(QueryState.INFORMATIONAL_SEARCH);
			User f0User = new User();
			f0User.setProduct(product);
			f0User.setState(QueryState.FOCUS_LEVEL_ZERO);
			User f1User = new User();
			f1User.setProduct(product);
			f1User.setState(QueryState.FOCUS_LEVEL_ONE);
			User f2User = new User();
			f2User.setProduct(product);
			f2User.setState(QueryState.FOCUS_LEVEL_TWO);

			MutableSampler<Query> isSampler = new WheelSampler<Query>(random);
			isSampler.addState(1.0, f0);
			isSampler.addState(0.5, f1_component);
			isSampler.addState(0.5, f1_manufacturer);
			isSampler.addState(1.0, f2);

			MutableSampler<Query> f0Sampler = new WheelSampler<Query>(random);
			f0Sampler.addState(1.0, f0);

			MutableSampler<Query> f1Sampler = new WheelSampler<Query>(random);
			f1Sampler.addState(1.0, f1_component);
			f1Sampler.addState(1.0, f1_manufacturer);

			MutableSampler<Query> f2Sampler = new WheelSampler<Query>(random);
			f2Sampler.addState(1.0, f2);

			map.put(isUser, isSampler);
			map.put(f0User, f0Sampler);
			map.put(f1User, f1Sampler);
			map.put(f2User, f2Sampler);
		}

		return map;
	}
}
