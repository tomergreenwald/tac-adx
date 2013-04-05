/**
  DefaultUserManagerTest.java
 
  COPYRIGHT  2008
  THE REGENTS OF THE UNIVERSITY OF MICHIGAN
  ALL RIGHTS RESERVED
 
  PERMISSION IS GRANTED TO USE, COPY, CREATE DERIVATIVE WORKS AND REDISTRIBUTE THIS
  SOFTWARE AND SUCH DERIVATIVE WORKS FOR NONCOMMERCIAL EDUCATION AND RESEARCH
  PURPOSES, SO LONG AS NO FEE IS CHARGED, AND SO LONG AS THE COPYRIGHT NOTICE
  ABOVE, THIS GRANT OF PERMISSION, AND THE DISCLAIMER BELOW APPEAR IN ALL COPIES
  MADE; AND SO LONG AS THE NAME OF THE UNIVERSITY OF MICHIGAN IS NOT USED IN ANY
  ADVERTISING OR PUBLICITY PERTAINING TO THE USE OR DISTRIBUTION OF THIS SOFTWARE
  WITHOUT SPECIFIC, WRITTEN PRIOR AUTHORIZATION.
 
  THIS SOFTWARE IS PROVIDED AS IS, WITHOUT REPRESENTATION FROM THE UNIVERSITY OF
  MICHIGAN AS TO ITS FITNESS FOR ANY PURPOSE, AND WITHOUT WARRANTY BY THE
  UNIVERSITY OF MICHIGAN OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT
  LIMITATION THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  PURPOSE. THE REGENTS OF THE UNIVERSITY OF MICHIGAN SHALL NOT BE LIABLE FOR ANY
  DAMAGES, INCLUDING SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WITH
  RESPECT TO ANY CLAIM ARISING OUT OF OR IN CONNECTION WITH THE USE OF THE SOFTWARE,
  EVEN IF IT HAS BEEN OR IS HEREAFTER ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package tau.tac.adx.users;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.agents.DefaultAdxUserManager;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.sim.AdxAuctioneer;
import tau.tac.adx.sim.TACAdxConstants;
import tau.tac.adx.users.generators.AdxUserGenerator;
import tau.tac.adx.util.Utils;

import com.google.common.eventbus.EventBus;

import edu.umich.eecs.tac.props.Product;

/**
 * @author Patrick Jordan
 * @author greenwald
 */
public class DefaultAdxUserManagerTest {
	private DefaultAdxUserManager userManager;

	private AdxUserQueryManager queryManager;

	private int populationSize;

	private Random random;

	private Product product;

	private PublisherCatalog publisherCatalog;
	private List<AdxUser> users;
	private AdxAuctioneer auctioneer;

	private EventBus eventBus;

	@Before
	public void setup() {
		product = new Product("man", "com");
		random = new Random();
		AdxUserGenerator userGenerator = Utils.getInjector().getInstance(
				AdxUserGenerator.class);
		populationSize = 10;
		users = (List<AdxUser>) userGenerator.generate(populationSize);

		publisherCatalog = new PublisherCatalog();
		publisherCatalog.addPublisher(mock(AdxPublisher.class));
		Map<Device, Integer> deviceDistributionMap = new HashMap<Device, Integer>();
		deviceDistributionMap.put(Device.pc, random.nextInt(10) + 1);
		deviceDistributionMap.put(Device.mobile, random.nextInt(10) + 1);
		Map<AdType, Integer> adTypeDistributionMap = new HashMap<AdType, Integer>();
		adTypeDistributionMap.put(AdType.text, random.nextInt(10) + 1);
		adTypeDistributionMap.put(AdType.video, random.nextInt(10) + 1);
		queryManager = new DefaultAdxUserQueryManager(publisherCatalog, users,
				deviceDistributionMap, adTypeDistributionMap, random);
		// queryManager = mock(AdxUserQueryManager.class);
		auctioneer = mock(AdxAuctioneer.class);

		eventBus = new EventBus(TACAdxConstants.ADX_EVENT_BUS_NAME);
		userManager = new DefaultAdxUserManager(publisherCatalog, users,
				queryManager, populationSize, eventBus);
	}

	@Test
	public void testConstuctor() {
		assertNotNull(userManager);
		assertNotNull(new DefaultAdxUserManager(publisherCatalog, users,
				queryManager, populationSize, eventBus));
	}

	@Test(expected = NullPointerException.class)
	public void testConstuctorPublisherCatalogNull() {
		new DefaultAdxUserManager(null, users, queryManager, populationSize,
				eventBus);
	}

	@Test(expected = NullPointerException.class)
	public void testConstuctorUsersNull() {
		new DefaultAdxUserManager(publisherCatalog, null, queryManager,
				populationSize, eventBus);
	}

	@Test(expected = NullPointerException.class)
	public void testConstuctorQueryManagerNull() {
		new DefaultAdxUserManager(publisherCatalog, users, null,
				populationSize, eventBus);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstuctorNegativePopulationSize() {
		new DefaultAdxUserManager(publisherCatalog, users, queryManager, -1,
				eventBus);
	}

	@Test
	public void testTriggerBehavior() {
		userManager.nextTimeUnit(0);
		AdxAuctioneer auctioneer = mock(AdxAuctioneer.class);
		userManager.triggerBehavior(auctioneer);
		Mockito.verify(auctioneer, Mockito.atLeast(populationSize)).runAuction(
				(AdxQuery) Mockito.any());
	}

}
