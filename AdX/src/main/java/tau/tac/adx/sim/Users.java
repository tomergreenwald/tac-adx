/*
 * Users.java
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
package tau.tac.adx.sim;

import java.util.logging.Logger;

import se.sics.tasim.sim.SimulationAgent;
import edu.umich.eecs.tac.sim.PublisherInfoSender;
import edu.umich.eecs.tac.user.UserEventListener;

/**
 * @author Lee Callender, Patrick Jordan
 */
public abstract class Users extends Builtin {
	private static final String CONF = "users.";

	protected Logger log = Logger.getLogger(Users.class.getName());

	PublisherInfoSender[] publishers;

	public Users() {
		super(CONF);
	}

	@Override
	protected void setup() {
		SimulationAgent[] publish = getSimulation().getPublishers();
		publishers = new PublisherInfoSender[publish.length];
		for (int i = 0, n = publish.length; i < n; i++) {
			publishers[i] = (PublisherInfoSender) publish[i].getAgent();
		}
	}

	public abstract void broadcastUserDistribution();

	// DEBUG FINALIZE REMOVE THIS!!! REMOVE THIS!!!
	@Override
	protected void finalize() throws Throwable {
		Logger.global.info("USER " + getName() + " IS BEING GARBAGED");
		super.finalize();
	}

	public abstract boolean addUserEventListener(UserEventListener listener);

	public abstract boolean containsUserEventListener(UserEventListener listener);

	public abstract boolean removeUserEventListener(UserEventListener listener);

	protected void transact(String advertiser, double amount) {
		getSimulation().transaction(getAddress(), advertiser, amount);
	}
}
