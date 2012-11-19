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
package edu.umich.eecs.tac.user;

import edu.umich.eecs.tac.util.config.ConfigProxy;
import edu.umich.eecs.tac.util.config.ConfigProxyUtils;
import edu.umich.eecs.tac.sim.AgentRepository;
import edu.umich.eecs.tac.props.RetailCatalog;

import java.util.Random;

/**
 * @author Patrick Jordan
 */
public class DefaultUserManagerBuilder implements
		UserBehaviorBuilder<UserManager> {
	private static final String BASE = "usermanager";
	private static final String POPULATION_SIZE_KEY = "populationsize";
	private static final int POPULATION_SIZE_DEFAULT = 10000;
	private static final String VIEW_MANAGER_KEY = "viewmanager";
	private static final String VIEW_MANAGER_DEFAULT = "edu.umich.eecs.tac.user.DefaultUserViewManagerBuilder";
	private static final String TRANSITION_MANAGER_KEY = "transitionmanager";
	private static final String TRANSITION_MANAGER_DEFAULT = "edu.umich.eecs.tac.user.DefaultUserTransitionManagerBuilder";
	private static final String QUERY_MANAGER_KEY = "querymanager";
	private static final String QUERY_MANAGER_DEFAULT = "edu.umich.eecs.tac.user.DefaultUserQueryManagerBuilder";

	public UserManager build(ConfigProxy userConfigProxy,
			AgentRepository repository, Random random) {

		RetailCatalog retailCatalog = repository.getRetailCatalog();

		try {
			UserBehaviorBuilder<UserTransitionManager> transitionBuilder = ConfigProxyUtils
					.createObjectFromProperty(userConfigProxy, BASE + '.'
							+ TRANSITION_MANAGER_KEY,
							TRANSITION_MANAGER_DEFAULT);
			UserTransitionManager transitionManager = transitionBuilder.build(
					userConfigProxy, repository, random);

			UserBehaviorBuilder<UserQueryManager> queryBuilder = ConfigProxyUtils
					.createObjectFromProperty(userConfigProxy, BASE + '.'
							+ QUERY_MANAGER_KEY, QUERY_MANAGER_DEFAULT);
			UserQueryManager queryManager = queryBuilder.build(userConfigProxy,
					repository, random);

			UserBehaviorBuilder<UserViewManager> viewBuilder = ConfigProxyUtils
					.createObjectFromProperty(userConfigProxy, BASE + '.'
							+ VIEW_MANAGER_KEY, VIEW_MANAGER_DEFAULT);
			UserViewManager viewManager = viewBuilder.build(userConfigProxy,
					repository, random);

			int populationSize = userConfigProxy.getPropertyAsInt(BASE + '.'
					+ POPULATION_SIZE_KEY, POPULATION_SIZE_DEFAULT);

			return new DefaultUserManager(retailCatalog, transitionManager,
					queryManager, viewManager, populationSize, random);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
