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

import java.util.Map;
import java.util.logging.Logger;

import se.sics.tasim.aw.Message;
import se.sics.tasim.sim.SimulationAgent;
import tau.tac.adx.sim.Publisher;
import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.auction.BidBundleWriter;
import edu.umich.eecs.tac.auction.ClickCharger;
import edu.umich.eecs.tac.auction.DefaultPublisherBehavior;
import edu.umich.eecs.tac.auction.PublisherBehavior;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.Auction;
import edu.umich.eecs.tac.props.BidBundle;
import edu.umich.eecs.tac.props.PublisherInfo;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.sim.AgentRepository;
import edu.umich.eecs.tac.sim.SalesAnalyst;
import edu.umich.eecs.tac.util.config.ConfigProxy;

/**
 * @author Lee Callender, Patrick Jordan
 */
public class DefaultPublisher extends Publisher {

	/**
	 * The publisher behavior
	 */
	private final PublisherBehavior publisherBehavior;

	public DefaultPublisher() {
		publisherBehavior = new DefaultPublisherBehavior(
				new PublisherConfigProxy(), new AgentRepositoryProxy(), this,
				new ClickChargerProxy(), new BidBundleWriterProxy());
	}

	@Override
	public void nextTimeUnit(int date) {
		publisherBehavior.nextTimeUnit(date);
	}

	@Override
	protected void setup() {
		this.log = Logger.getLogger(DefaultPublisher.class.getName());

		publisherBehavior.setup();

		addTimeListener(this);
	}

	@Override
	protected void stopped() {
		removeTimeListener(this);

		publisherBehavior.stopped();
	}

	@Override
	protected void shutdown() {
		publisherBehavior.stopped();
	}

	@Override
	protected void messageReceived(Message message) {
		publisherBehavior.messageReceived(message);
	}

	@Override
	public PublisherInfo getPublisherInfo() {
		return publisherBehavior.getPublisherInfo();
	}

	void setPublisherInfo(PublisherInfo publisherInfo) {
		publisherBehavior.setPublisherInfo(publisherInfo);
	}

	@Override
	public void sendQueryReportsToAll() {
		publisherBehavior.sendQueryReportsToAll();
	}

	@Override
	public Auction runAuction(Query query) {
		return publisherBehavior.runAuction(query);
	}

	@Override
	public void applyBidUpdates() {
		publisherBehavior.applyBidUpdates();
	}

	protected class PublisherConfigProxy implements ConfigProxy {

		@Override
		public String getProperty(String name) {
			return DefaultPublisher.this.getProperty(name);
		}

		@Override
		public String getProperty(String name, String defaultValue) {
			return DefaultPublisher.this.getProperty(name, defaultValue);
		}

		@Override
		public String[] getPropertyAsArray(String name) {
			return DefaultPublisher.this.getPropertyAsArray(name);
		}

		@Override
		public String[] getPropertyAsArray(String name, String defaultValue) {
			return DefaultPublisher.this.getPropertyAsArray(name, defaultValue);
		}

		@Override
		public int getPropertyAsInt(String name, int defaultValue) {
			return DefaultPublisher.this.getPropertyAsInt(name, defaultValue);
		}

		@Override
		public int[] getPropertyAsIntArray(String name) {
			return DefaultPublisher.this.getPropertyAsIntArray(name);
		}

		@Override
		public int[] getPropertyAsIntArray(String name, String defaultValue) {
			return DefaultPublisher.this.getPropertyAsIntArray(name,
					defaultValue);
		}

		@Override
		public long getPropertyAsLong(String name, long defaultValue) {
			return DefaultPublisher.this.getPropertyAsLong(name, defaultValue);
		}

		@Override
		public float getPropertyAsFloat(String name, float defaultValue) {
			return DefaultPublisher.this.getPropertyAsFloat(name, defaultValue);
		}

		@Override
		public double getPropertyAsDouble(String name, double defaultValue) {
			return DefaultPublisher.this
					.getPropertyAsDouble(name, defaultValue);
		}
	}

	protected class AgentRepositoryProxy implements AgentRepository {

		@Override
		public Map<String, AdvertiserInfo> getAdvertiserInfo() {
			return getSimulation().getAdvertiserInfo();
		}

		@Override
		public SimulationAgent[] getPublishers() {
			return getSimulation().getPublishers();
		}

		@Override
		public SalesAnalyst getSalesAnalyst() {
			return null;
		}

		@Override
		public int getNumberOfAdvertisers() {
			return getSimulation().getNumberOfAdvertisers();
		}

		@Override
		public String[] getAdvertiserAddresses() {
			return getSimulation().getAdvertiserAddresses();
		}
	}

	protected class ClickChargerProxy implements ClickCharger {
		@Override
		public void charge(String advertiser, double cpc) {
			DefaultPublisher.this.charge(advertiser, cpc);
		}
	}

	protected class BidBundleWriterProxy implements BidBundleWriter {
		@Override
		public void writeBundle(String advertiser, BidBundle bundle) {

			int agentIndex = DefaultPublisher.this.getSimulation().agentIndex(
					advertiser);

			DefaultPublisher.this.getEventWriter().dataUpdated(agentIndex,
					TACAdxConstants.DU_BIDS, bundle);
		}
	}
}
