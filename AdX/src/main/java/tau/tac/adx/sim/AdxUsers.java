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

import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.AdNetworkReportSender;
import tau.tac.adx.report.publisher.AdxPublisherReport;
import tau.tac.adx.report.publisher.AdxPublisherReportSender;
import edu.umich.eecs.tac.sim.PublisherInfoSender;

/**
 * @author greenwald
 */
public abstract class AdxUsers extends Builtin implements
		AdxPublisherReportSender, AdNetworkReportSender {
	protected Logger log = Logger.getLogger(AdxUsers.class.getName());

	PublisherInfoSender[] publishers;

	public AdxUsers() {
		super(TACAdxConstants.ADX_AGENT_NAME);
	}

	@Override
	protected void setup() {
	}

	public abstract void sendReportsToAll();

	/**
	 * @see tau.tac.adx.report.publisher.AdxPublisherReportSender#broadcastPublisherReport(tau.tac.adx.report.publisher.AdxPublisherReport)
	 */
	@Override
	public void broadcastPublisherReport(AdxPublisherReport report) {
		getSimulation().broadcastPublisherReport(report);
	}

	/**
	 * @see tau.tac.adx.report.adn.AdNetworkReportSender#broadcastAdNetowrkReport(int,
	 *      AdNetworkReport)
	 */
	@Override
	public void broadcastAdNetowrkReport(String adNetworkName,
			AdNetworkReport report) {
		getSimulation().broadcastAdNetowrkReport(adNetworkName, report);
	}

}
