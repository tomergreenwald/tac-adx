/*
 * DefaultUserQueryManagerBuilderTest.java
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

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import edu.umich.eecs.tac.util.config.ConfigProxy;
import edu.umich.eecs.tac.sim.AgentRepository;
import edu.umich.eecs.tac.sim.SalesAnalyst;
import edu.umich.eecs.tac.props.*;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;

import se.sics.tasim.sim.SimulationAgent;

/**
 * @author Patrick Jordan
 */
public class DefaultUserQueryManagerBuilderTest {
	private DefaultUserQueryManagerBuilder builder;
	private ConfigProxy userConfigProxy;
	private AgentRepository repository;
	private Random random;
	private RetailCatalog catalog;
	private SalesAnalyst salesAnalyst;
	private Map<String, AdvertiserInfo> advertiserInfo;
	private SlotInfo slotInfo;

	@Before
	public void setup() {
		catalog = new RetailCatalog();
		advertiserInfo = new HashMap<String, AdvertiserInfo>();

		builder = new DefaultUserQueryManagerBuilder();

		slotInfo = new SlotInfo();

		salesAnalyst = new SalesAnalyst() {

			public void addAccount(String name) {
			}

			public void sendSalesReportToAll() {
			}

			public void queryIssued(Query query) {
			}

			public void viewed(Query query, Ad ad, int slot, String advertiser,
					boolean isPromoted) {
			}

			public void clicked(Query query, Ad ad, int slot, double cpc,
					String advertiser) {
			}

			public void converted(Query query, Ad ad, int slot,
					double salesProfit, String advertiser) {
			}

			public double getRecentConversions(String name) {
				return 0;
			}
		};

		userConfigProxy = new ConfigProxy() {

			public String getProperty(String name) {
				return null;
			}

			public String getProperty(String name, String defaultValue) {
				return null;
			}

			public String[] getPropertyAsArray(String name) {
				return new String[0];
			}

			public String[] getPropertyAsArray(String name, String defaultValue) {
				return new String[0];
			}

			public int getPropertyAsInt(String name, int defaultValue) {
				return 0;
			}

			public int[] getPropertyAsIntArray(String name) {
				return new int[0];
			}

			public int[] getPropertyAsIntArray(String name, String defaultValue) {
				return new int[0];
			}

			public long getPropertyAsLong(String name, long defaultValue) {
				return 0;
			}

			public float getPropertyAsFloat(String name, float defaultValue) {
				return 0;
			}

			public double getPropertyAsDouble(String name, double defaultValue) {
				return 0;
			}
		};

		repository = new AgentRepository() {

			public RetailCatalog getRetailCatalog() {
				return catalog;
			}

			public Map<String, AdvertiserInfo> getAdvertiserInfo() {
				return advertiserInfo;
			}

			public SimulationAgent[] getPublishers() {
				return new SimulationAgent[0];
			}

			public SimulationAgent[] getUsers() {
				return new SimulationAgent[0];
			}

			public SalesAnalyst getSalesAnalyst() {
				return salesAnalyst;
			}

			public int getNumberOfAdvertisers() {
				return advertiserInfo.size();
			}

			public SlotInfo getAuctionInfo() {
				return slotInfo;
			}

			public String[] getAdvertiserAddresses() {
				return advertiserInfo.keySet().toArray(new String[0]);
			}
		};

		random = new Random();
	}

	@Test
	public void testConstructor() {
		assertNotNull(builder);
	}

	@Test
	public void testBuild() {
		UserQueryManager manager = builder.build(userConfigProxy, repository,
				random);
		assertNotNull(manager);
	}
}
