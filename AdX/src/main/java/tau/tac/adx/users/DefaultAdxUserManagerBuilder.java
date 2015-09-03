/*
 * DefaultUserManagerBuilder.java
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

import java.util.Random;

import tau.tac.adx.agents.DefaultAdxUserManager;
import tau.tac.adx.sim.AdxAgentRepository;
import edu.umich.eecs.tac.util.config.ConfigProxy;
import edu.umich.eecs.tac.util.config.ConfigProxyUtils;

/**
 * @author Patrick Jordan
 * @author greenwald
 */
public class DefaultAdxUserManagerBuilder implements
		AdxUserBehaviorBuilder<DefaultAdxUserManager> {
	private static final String ADX_BASE = "adx_usermanager";
	private static final String POPULATION_SIZE_KEY = "populationsize";
	private static final int POPULATION_SIZE_DEFAULT = 10000;
	private static final String VIEW_MANAGER_KEY = "viewmanager";
	private static final String QUERY_MANAGER_KEY = "querymanager";
	private static final String ADX_QUERY_MANAGER_DEFAULT = AdxUserQueryManagerBuilder.class
			.getName();

	@Override
	public DefaultAdxUserManager build(ConfigProxy userConfigProxy,
			AdxAgentRepository repository, Random random) {

		try {
			AdxUserBehaviorBuilder<DefaultAdxUserQueryManager> queryBuilder = ConfigProxyUtils
					.createObjectFromProperty(userConfigProxy, ADX_BASE + '.'
							+ QUERY_MANAGER_KEY, ADX_QUERY_MANAGER_DEFAULT);
			DefaultAdxUserQueryManager queryManager = queryBuilder.build(
					userConfigProxy, repository, random);

			int populationSize = userConfigProxy.getPropertyAsInt(ADX_BASE
					+ '.' + POPULATION_SIZE_KEY, POPULATION_SIZE_DEFAULT);
			return new DefaultAdxUserManager(repository.getPublisherCatalog(),
					repository.getUserPopulation(), queryManager,
					populationSize, repository.getEventBus());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
