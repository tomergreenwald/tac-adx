/*
 * DefaultPublisher.java
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
package edu.umich.eecs.tac.agents;

import edu.umich.eecs.tac.sim.Publisher;
import edu.umich.eecs.tac.sim.AgentRepository;
import edu.umich.eecs.tac.sim.SalesAnalyst;
import edu.umich.eecs.tac.TACAAConstants;
import edu.umich.eecs.tac.util.config.ConfigProxy;
import edu.umich.eecs.tac.auction.*;
import edu.umich.eecs.tac.props.*;
import se.sics.tasim.aw.Message;
import se.sics.tasim.sim.SimulationAgent;

import java.util.logging.Logger;
import java.util.*;

/**
 * @author Lee Callender, Patrick Jordan
 */
public class DefaultPublisher extends Publisher {

	/**
	 * The publisher behavior
	 */
	private PublisherBehavior publisherBehavior;

	public DefaultPublisher() {
		publisherBehavior = new DefaultPublisherBehavior(
				new PublisherConfigProxy(), new AgentRepositoryProxy(), this,
				new ClickChargerProxy(), new BidBundleWriterProxy());
	}

	public void nextTimeUnit(int date) {
		publisherBehavior.nextTimeUnit(date);
	}

	protected void setup() {
		this.log = Logger.getLogger(DefaultPublisher.class.getName());

		publisherBehavior.setup();

		addTimeListener(this);
	}

	protected void stopped() {
		removeTimeListener(this);

		publisherBehavior.stopped();
	}

	protected void shutdown() {
		publisherBehavior.stopped();
	}

	protected void messageReceived(Message message) {
		publisherBehavior.messageReceived(message);
	}

	public PublisherInfo getPublisherInfo() {
		return publisherBehavior.getPublisherInfo();
	}

	void setPublisherInfo(PublisherInfo publisherInfo) {
		publisherBehavior.setPublisherInfo(publisherInfo);
	}

	public void sendQueryReportsToAll() {
		publisherBehavior.sendQueryReportsToAll();
	}

	public Auction runAuction(Query query) {
		return publisherBehavior.runAuction(query);
	}

    public void applyBidUpdates() {
        publisherBehavior.applyBidUpdates();
    }

    protected class PublisherConfigProxy implements ConfigProxy {

		public String getProperty(String name) {
			return DefaultPublisher.this.getProperty(name);
		}

		public String getProperty(String name, String defaultValue) {
			return DefaultPublisher.this.getProperty(name, defaultValue);
		}

		public String[] getPropertyAsArray(String name) {
			return DefaultPublisher.this.getPropertyAsArray(name);
		}

		public String[] getPropertyAsArray(String name, String defaultValue) {
			return DefaultPublisher.this.getPropertyAsArray(name, defaultValue);
		}

		public int getPropertyAsInt(String name, int defaultValue) {
			return DefaultPublisher.this.getPropertyAsInt(name, defaultValue);
		}

		public int[] getPropertyAsIntArray(String name) {
			return DefaultPublisher.this.getPropertyAsIntArray(name);
		}

		public int[] getPropertyAsIntArray(String name, String defaultValue) {
			return DefaultPublisher.this.getPropertyAsIntArray(name,
					defaultValue);
		}

		public long getPropertyAsLong(String name, long defaultValue) {
			return DefaultPublisher.this.getPropertyAsLong(name, defaultValue);
		}

		public float getPropertyAsFloat(String name, float defaultValue) {
			return DefaultPublisher.this.getPropertyAsFloat(name, defaultValue);
		}

		public double getPropertyAsDouble(String name, double defaultValue) {
			return DefaultPublisher.this
					.getPropertyAsDouble(name, defaultValue);
		}
	}

	protected class AgentRepositoryProxy implements AgentRepository {
		public RetailCatalog getRetailCatalog() {
			return getSimulation().getRetailCatalog();
		}

		public SlotInfo getAuctionInfo() {
			return getSimulation().getAuctionInfo();
		}

		public Map<String, AdvertiserInfo> getAdvertiserInfo() {
			return getSimulation().getAdvertiserInfo();
		}

		public SimulationAgent[] getPublishers() {
			return getSimulation().getPublishers();
		}

		public SimulationAgent[] getUsers() {
			return getSimulation().getUsers();
		}

		public SalesAnalyst getSalesAnalyst() {
			return getSimulation().getSalesAnalyst();
		}

		public int getNumberOfAdvertisers() {
			return getSimulation().getNumberOfAdvertisers();
		}

		public String[] getAdvertiserAddresses() {
			return getSimulation().getAdvertiserAddresses();
		}
	}

	protected class ClickChargerProxy implements ClickCharger {
		public void charge(String advertiser, double cpc) {
			DefaultPublisher.this.charge(advertiser, cpc);
		}
	}

	protected class BidBundleWriterProxy implements BidBundleWriter {
		public void writeBundle(String advertiser, BidBundle bundle) {

			int agentIndex = DefaultPublisher.this.getSimulation().agentIndex(
					advertiser);

			DefaultPublisher.this.getEventWriter().dataUpdated(agentIndex,
					TACAAConstants.DU_BIDS, bundle);
		}
	}
}
