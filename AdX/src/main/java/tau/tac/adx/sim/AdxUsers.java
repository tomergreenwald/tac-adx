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
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.AdNetworkReportSender;
import tau.tac.adx.report.publisher.AdxPublisherReport;
import tau.tac.adx.report.publisher.AdxPublisherReportSender;
import edu.umich.eecs.tac.sim.PublisherInfoSender;

/**
 * @author Lee Callender, Patrick Jordan
 */
public abstract class AdxUsers extends Builtin implements
		AdxPublisherReportSender, AdNetworkReportSender {
	private static final String CONF = "adxusers.";

	protected Logger log = Logger.getLogger(AdxUsers.class.getName());

	PublisherInfoSender[] publishers;

	public AdxUsers() {
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

	public abstract void sendReportsToAll();

	protected void transact(String advertiser, double amount) {
		getSimulation().transaction(getAddress(), advertiser, amount);
	}

	/**
	 * @see tau.tac.adx.report.publisher.AdxPublisherReportSender#broadcastReport(tau.tac.adx.report.publisher.AdxPublisherReport)
	 */
	@Override
	public void broadcastReport(AdxPublisherReport report) {
		getSimulation().broadcastReport(report);
	}

	/**
	 * @see tau.tac.adx.report.adn.AdNetworkReportSender#broadcastReport(int,
	 *      AdNetworkReport)
	 */
	@Override
	public void broadcastReport(int adNetworkId, AdNetworkReport report) {
		getSimulation().broadcastReport(adNetworkId, report);
	}

	/**
	 * Applies bid updates.
	 */
	public abstract void applyBidUpdates();
}
