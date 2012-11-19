/*
 * Publisher.java
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
package edu.umich.eecs.tac.sim;


import edu.umich.eecs.tac.props.QueryReport;
import se.sics.isl.transport.Transportable;

import java.util.logging.Logger;
import static edu.umich.eecs.tac.TACAAConstants.*;
import edu.umich.eecs.tac.TACAAConstants;

/**
 * @author Lee Callender, Patrick Jordan
 */
public abstract class Publisher extends Builtin implements QueryReportSender,
		Auctioneer, PublisherInfoSender {
	private static final String CONF = "publisher.";

	protected Logger log = Logger.getLogger(Publisher.class.getName());

	public Publisher() {
		super(CONF);
	}

	protected void sendToAdvertisers(Transportable content) {
		sendToRole(ADVERTISER, content);
	}

	public abstract void sendQueryReportsToAll();

	// DEBUG FINALIZE REMOVE THIS!!! REMOVE THIS!!!
	protected void finalize() throws Throwable {
		Logger.global.info("PUBLISHER " + getName() + " IS BEING GARBAGED");
		super.finalize();
	}

	protected void charge(String advertiser, double amount) {
		getSimulation().transaction(advertiser, getAddress(), amount);
	}

	public void sendQueryReport(String advertiser, QueryReport report) {
		getSimulation().sendQueryReport(advertiser, report);        
	}

	public void sendPublisherInfo(String advertiser) {
		sendMessage(advertiser, getPublisherInfo());
	}

	public void sendPublisherInfoToAll() {
		for (String advertiser : getAdvertiserAddresses()) {
			sendPublisherInfo(advertiser);
		}

        getEventWriter().dataUpdated(getSimulation().agentIndex(this.getAddress()), TACAAConstants.DU_PUBLISHER_INFO, getPublisherInfo());
	}

    public void broadcastImpressions(String advertiser, int impressions) {
        getSimulation().broadcastImpressions(advertiser, impressions);
    }

    public void broadcastClicks(String advertiser, int clicks) {
        getSimulation().broadcastClicks(advertiser, clicks);
    }

    public abstract void applyBidUpdates();
}
