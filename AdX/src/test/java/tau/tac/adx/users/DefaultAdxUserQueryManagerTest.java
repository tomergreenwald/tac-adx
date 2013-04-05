/*
 * DefaultUserQueryManagerTest.java
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
package tau.tac.adx.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.users.generators.AdxUserGenerator;
import tau.tac.adx.util.Utils;
import edu.umich.eecs.tac.props.Product;

/**
 * @author Patrick Jordan
 */
public class DefaultAdxUserQueryManagerTest {
	private PublisherCatalog catalog;
	private Product product;
	private Random random;
	private String manufacturer;
	private String component;

	private List<AdxUser> users;
	private Map<Device, Integer> deviceDistributionMap;
	private Map<AdType, Integer> adTypeDistributionMap;
	AdxUserGenerator userGenerator = Utils.getInjector().getInstance(
			AdxUserGenerator.class);

	@Before
	public void setUp() {
		manufacturer = "ACME";
		component = "Widget";
		random = new Random(100);

		product = new Product(manufacturer, component);
		catalog = new PublisherCatalog();
		catalog.addPublisher(Mockito.mock(AdxPublisher.class));
		users = (List<AdxUser>) userGenerator.generate(100);
		deviceDistributionMap = new HashMap<Device, Integer>();
		deviceDistributionMap.put(Device.pc, random.nextInt(10));
		deviceDistributionMap.put(Device.mobile, random.nextInt(10));
		adTypeDistributionMap = new HashMap<AdType, Integer>();
		adTypeDistributionMap.put(AdType.text, random.nextInt(10));
		adTypeDistributionMap.put(AdType.video, random.nextInt(10));
	}

	@Test
	public void testConstructor() {

		assertNotNull(new DefaultAdxUserQueryManager(catalog, users,
				deviceDistributionMap, adTypeDistributionMap, random));
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorCatalogNull() {
		new DefaultAdxUserQueryManager(null, users, deviceDistributionMap,
				adTypeDistributionMap, random);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorUsersNull() {
		new DefaultAdxUserQueryManager(catalog, null, deviceDistributionMap,
				adTypeDistributionMap, random);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorDeviceNull() {
		new DefaultAdxUserQueryManager(catalog, users, null,
				adTypeDistributionMap, random);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorAdTypesNull() {
		new DefaultAdxUserQueryManager(catalog, users, deviceDistributionMap,
				null, random);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorRandomNull() {
		new DefaultAdxUserQueryManager(catalog, users, deviceDistributionMap,
				adTypeDistributionMap, null);
	}

	@Test
	public void testQueryBehavior() {
		DefaultAdxUserQueryManager manager = new DefaultAdxUserQueryManager(
				catalog, users, deviceDistributionMap, adTypeDistributionMap,
				random);
		manager.nextTimeUnit(0);

		AdxUser nsUser = userGenerator.generate(1).iterator().next();

		assertEquals(manager.generateQuery(nsUser), null);

		AdxQuery isQuery = manager.generateQuery(users.get(0));
		assertNotNull(isQuery);
	}
}
