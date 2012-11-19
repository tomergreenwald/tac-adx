/*
 * DefaultUserTransitionManagerBuilder.java
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
import edu.umich.eecs.tac.sim.AgentRepository;

import java.util.Random;

/**
 * @author Patrick Jordan
 */
public class DefaultUserTransitionManagerBuilder implements UserBehaviorBuilder<UserTransitionManager> {
	private static final String STANDARD_KEY = "usermanager.usertransitionmanager.probability.standard";
	private static final String BURST_KEY = "usermanager.usertransitionmanager.probability.burst";
	private static final String BURST_PROBABILITY_KEY = "usermanager.usertransitionmanager.burstprobability";
        private static final String SUCCESSIVE_BURST_PROBABILITY_KEY = "usermanager.usertransitionmanager.successiveburstprobability";
        private static final String BURST_EFFECT_LENGTH = "usermanager.usertransitionmanager.bursteffectlength";
	private static final double BURST_PROBABILITY_DEFAULT = 0.05;

	public UserTransitionManager build(ConfigProxy userConfigProxy, AgentRepository repository, Random random) {
		DefaultUserTransitionManager transitionManager = new DefaultUserTransitionManager(repository.getRetailCatalog(),
                                                                                          random);

		// Construct standard probabilities
		for (QueryState from : QueryState.values()) {
			for (QueryState to : QueryState.values()) {
				double probability = userConfigProxy.getPropertyAsDouble(String.format("%s.%s.%s", STANDARD_KEY,
                                                                                                   from.toString(),
                                                                                                   to.toString()),
                                                                                                   Double.NaN);

				if (!Double.isNaN(probability) && probability > 0) {
					transitionManager.addStandardTransitionProbability(from, to, probability);
				}
			}
		}

		// Construct burst probabilities
		for (QueryState from : QueryState.values()) {
			for (QueryState to : QueryState.values()) {
				double probability = userConfigProxy.getPropertyAsDouble(String.format("%s.%s.%s", BURST_KEY,
                                                                                                   from.toString(),
                                                                                                   to.toString()),
                                                                                                   Double.NaN);

				if (!Double.isNaN(probability) && probability > 0) {
					transitionManager.addBurstTransitionProbability(from, to, probability);
				}
			}
		}
                double burstProbability=userConfigProxy.getPropertyAsDouble(BURST_PROBABILITY_KEY,BURST_PROBABILITY_DEFAULT);
                double successiveBurstProbability=userConfigProxy.getPropertyAsDouble(SUCCESSIVE_BURST_PROBABILITY_KEY,BURST_PROBABILITY_DEFAULT);
                int burstEffectLength=userConfigProxy.getPropertyAsInt(BURST_EFFECT_LENGTH,1);
		transitionManager.setBurstProbability(burstProbability,successiveBurstProbability,burstEffectLength);

		return transitionManager;
	}
}
