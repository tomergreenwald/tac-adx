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
package tau.tac.adx.agents;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import tau.tac.adx.AdxManager;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.auction.AdxAuctionResult;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.sim.AdxAuctioneer;
import tau.tac.adx.sim.TACAdxConstants;
import tau.tac.adx.sim.TACAdxSimulation;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.AdxUserQueryManager;
import tau.tac.adx.users.DefaultAdxUserQueryManager;
import tau.tac.adx.users.generators.AdxUserGenerator;
import tau.tac.adx.util.Utils;

import com.google.common.eventbus.EventBus;

import edu.umich.eecs.tac.props.Product;

/**
 * @author Patrick Jordan
 * @author greenwald
 */
public class DefaultAdxUserManagerTest {
	private final class AdxAuctioneerSpy implements AdxAuctioneer {
		public int count = 0;

		@Override
		public AdxAuctionResult runAuction(AdxQuery query) {
			count++;
			return null;
		}

		@Override
		public void applyBidUpdates() {}
	}

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
		populationSize = 10000;
		users = (List<AdxUser>) userGenerator.generate(populationSize);

		publisherCatalog = new PublisherCatalog();
		publisherCatalog.addPublisher(mock(AdxPublisher.class,
				Mockito.RETURNS_DEEP_STUBS));
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
		TACAdxSimulation simulation = mock(TACAdxSimulation.class,
				Mockito.RETURNS_DEEP_STUBS);
		AdxManager.getInstance().setSimulation(simulation);
		userManager.triggerBehavior(auctioneer);
		Mockito.verify(auctioneer, Mockito.atLeast(populationSize)).runAuction(
				(AdxQuery) Mockito.any());
	}

	@Test
	public void testHandleSearch() {
		for (AdxUser user : users) {
			AdxAuctioneer auctioneer2 = mock(AdxAuctioneer.class);
			userManager.handleSearch(user, auctioneer2);
			verify(auctioneer2, times(1)).runAuction((AdxQuery) notNull());
		}
	}

	@Test
	public void testHandleUserActivity() {
		double pContinue = 0.3;
		int contimueMax = 6;
		int times = 10000;
		int counts[] = new int[contimueMax + 1];
		for (int i = 0; i < times; i++) {
			//setup
			TACAdxSimulation simulation = mock(TACAdxSimulation.class, Mockito.RETURNS_DEEP_STUBS);
			AdxManager.getInstance().setSimulation(simulation );
			AdxUser adxUser = mock(AdxUser.class);
			when(adxUser.getpContinue()).thenReturn(pContinue);
			AdxUserQueryManager queryManager = mock(AdxUserQueryManager.class);
			when(queryManager.generateQuery(adxUser)).thenReturn(mock(AdxQuery.class));
			DefaultAdxUserManager manager = new DefaultAdxUserManager(
					mock(PublisherCatalog.class), mock(List.class),
					queryManager, 100000, mock(EventBus.class));
			AdxAuctioneerSpy adxAuctioneerSpy = new AdxAuctioneerSpy();
			//test
			manager.handleUserActivity(adxUser, adxAuctioneerSpy);
			//validate MAX_USER_DAILY_IMPRESSION (6)
			org.junit.Assert.assertThat(adxAuctioneerSpy.count, lessThanOrEqualTo(contimueMax));
			if (adxAuctioneerSpy.count <= counts.length) {
				counts[adxAuctioneerSpy.count]++;
			}
		}
		//validate User Continuation Probability (0.3)
		for (int i = 1; i < counts.length - 1; i++) {
			double probability = Math.pow((pContinue), i - 1) * (1-pContinue);
			if(i == counts.length) {
				probability /= (1-pContinue); //The last slot gets every option
			}
			org.junit.Assert.assertEquals("Cycle " + i, probability * times, counts[i], times * 0.05);
		}
	}

}
