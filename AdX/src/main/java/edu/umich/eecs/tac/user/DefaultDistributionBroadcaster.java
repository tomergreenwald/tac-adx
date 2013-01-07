/*
 * DefaultDistributionBroadcaster.java
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

import se.sics.tasim.is.EventWriter;
import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.UserPopulationState;

/**
 * @author Patrick Jordan, Lee Callender
 */
public class DefaultDistributionBroadcaster implements DistributionBroadcaster {
	private UserManager userManager;

	public DefaultDistributionBroadcaster(UserManager userManager) {

		if (userManager == null) {
			throw new NullPointerException("user manager cannot be null");
		}

		this.userManager = userManager;
	}

  /**
   * For the time being, this broadcasts both the total user distribution plus
   * the distribution over all product populations
   * @param usersIndex
   * @param eventWriter
   */
  public void broadcastUserDistribution(int usersIndex,
			EventWriter eventWriter) {
		int[] distribution = userManager.getStateDistribution();

    QueryState[] states = QueryState.values();

		for (int i = 0; i < distribution.length; i++) {
			switch (states[i]) {
			case NON_SEARCHING:
				eventWriter.dataUpdated(usersIndex,
						TACAdxConstants.DU_NON_SEARCHING, distribution[i]);
				break;
			case INFORMATIONAL_SEARCH:
				eventWriter
						.dataUpdated(usersIndex,
								TACAdxConstants.DU_INFORMATIONAL_SEARCH,
								distribution[i]);
				break;
			case FOCUS_LEVEL_ZERO:
				eventWriter.dataUpdated(usersIndex,
						TACAdxConstants.DU_FOCUS_LEVEL_ZERO, distribution[i]);
				break;
			case FOCUS_LEVEL_ONE:
				eventWriter.dataUpdated(usersIndex,
						TACAdxConstants.DU_FOCUS_LEVEL_ONE, distribution[i]);
				break;
			case FOCUS_LEVEL_TWO:
				eventWriter.dataUpdated(usersIndex,
						TACAdxConstants.DU_FOCUS_LEVEL_TWO, distribution[i]);
				break;
			case TRANSACTED:
				eventWriter.dataUpdated(usersIndex,
						TACAdxConstants.DU_TRANSACTED, distribution[i]);
				break;
			default:
				break;
			}
		}

    //This is inefficient implementation, but works for now
    //We can change if we have time/it's an issue
    UserPopulationState ups = new UserPopulationState();
    for(Product product: userManager.getRetailCatalog()){
        ups.setDistribution(product, userManager.getStateDistribution(product));
    }
    ups.lock();

    eventWriter.dataUpdated(usersIndex, TACAdxConstants.TYPE_NONE, ups);
  }
}
