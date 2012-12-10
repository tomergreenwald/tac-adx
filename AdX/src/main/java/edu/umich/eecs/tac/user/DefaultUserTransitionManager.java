/*
 * DefaultUserTransitionManager.java
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
import java.util.logging.Logger;

import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.util.sampling.MutableSampler;
import edu.umich.eecs.tac.util.sampling.WheelSampler;

/**
 * @author Patrick Jordan
 */
public class DefaultUserTransitionManager implements UserTransitionManager {
    	protected Logger log = Logger.getLogger(DefaultUserTransitionManager.class.getName());

	private Map<QueryState, MutableSampler<QueryState>> standardSamplers;
	private Map<QueryState, MutableSampler<QueryState>> burstSamplers;

    private double burstProbability;
    private double successiveBurstProbability;
    private int burstEffectLength;
    private boolean[] bursts;
    private int[] burstEffectCounter;
	private Random random;
    private RetailCatalog retailCatalog;

	public DefaultUserTransitionManager(RetailCatalog retailCatalog) {
		this(retailCatalog, new Random());
	}

	public DefaultUserTransitionManager(RetailCatalog retailCatalog, Random random) {
		if (random == null) {
			throw new NullPointerException("Random number generator cannot be null");
		}

        if(retailCatalog==null) {
            throw new NullPointerException("retail catalog cannot be null");
        }
		standardSamplers = new HashMap<QueryState, MutableSampler<QueryState>>(QueryState.values().length);
		burstSamplers = new HashMap<QueryState, MutableSampler<QueryState>>(QueryState.values().length);
		this.random = random;

        this.retailCatalog = retailCatalog;
        bursts = new boolean[retailCatalog.size()];
        burstEffectCounter=new int[retailCatalog.size()];
        for(int i=0;i<burstEffectCounter.length;i++)
        {
            burstEffectCounter[i]=0;
        }
		updateBurst();
	}

	public void nextTimeUnit(int timeUnit) {
		updateBurst();
	}

	public void addStandardTransitionProbability(QueryState from, QueryState to, double probability) {
		MutableSampler<QueryState> sampler = standardSamplers.get(from);
		if (sampler == null) {
			sampler = new WheelSampler<QueryState>(random);
			standardSamplers.put(from, sampler);
		}

		sampler.addState(probability, to);
	}

	public void addBurstTransitionProbability(QueryState from, QueryState to, double probability) {
		MutableSampler<QueryState> sampler = burstSamplers.get(from);
		if (sampler == null) {
			sampler = new WheelSampler<QueryState>(random);
			burstSamplers.put(from, sampler);
		}

		sampler.addState(probability, to);
	}

	public double getBurstProbability() {
		return burstProbability;
	}

	public void setBurstProbability(double burstProbability, double successiveBurstProbability, int burstEffectLength) {
		this.burstProbability = burstProbability;
                this.successiveBurstProbability = successiveBurstProbability;
                this.burstEffectLength = burstEffectLength;
        }

	public QueryState transition(User user, boolean transacted) {
		if (transacted)
			return QueryState.TRANSACTED;
		else if (bursts[retailCatalog.indexForEntry(user.getProduct())])
			return burstSamplers.get(user.getState()).getSample();
		else
			return standardSamplers.get(user.getState()).getSample();
	}

	private void updateBurst() {

            for(int i = 0 ; i < bursts.length; i++) {
                if(burstEffectCounter[i]>0)
                    bursts[i] = random.nextDouble() < successiveBurstProbability;

                else
                    bursts[i] = random.nextDouble() < burstProbability;

                if(bursts[i]){
                    burstEffectCounter[i]=burstEffectLength;
                }
                else{
                    burstEffectCounter[i]=burstEffectCounter[i]>0?burstEffectCounter[i]-1:burstEffectCounter[i];
                }
            }
	}

	public boolean isBurst(Product product) {
        return bursts[retailCatalog.indexForEntry(product)];
    }
}
