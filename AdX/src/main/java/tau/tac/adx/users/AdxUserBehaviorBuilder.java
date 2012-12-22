/*
 * UserBehaviorBuilder.java
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

import java.util.Map;
import java.util.Random;

import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.sim.AdxAgentRepository;
import edu.umich.eecs.tac.util.config.ConfigProxy;

/**
 * User behavior builders construct user behaviors from a configuration.
 * 
 * @author greenwald
 * @param <T>
 *            {@link #build(ConfigProxy, AdxAgentRepository, Map, Map, Random)}
 *            type.
 */
public interface AdxUserBehaviorBuilder<T> {

	/**
	 * Build a user behavior from a configuration.
	 * 
	 * @param userConfigProxy
	 *            the configuration proxy
	 * @param repository
	 *            the repository of agents
	 * @param deviceDeistributionMap
	 *            {@link Device} distribution map. Each {@link Device} is
	 *            associated with its relative popularity.
	 * @param adTypeDeistributionMap
	 *            {@link AdType} distribution map. Each {@link AdType} is
	 *            associated with its relative popularity.
	 * @param random
	 *            the random number generator
	 * @return a built user behavior
	 */
	public T build(ConfigProxy userConfigProxy, AdxAgentRepository repository,
			Random random);
}
