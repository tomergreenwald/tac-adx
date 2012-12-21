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
package edu.umich.eecs.tac.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;


import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.RetailCatalog;

/**
 * @author Patrick Jordan
 */
public class DefaultUserQueryManagerTest {
	private RetailCatalog catalog;
	private Product product;
	private Random random;
	private String manufacturer;
	private String component;

	@Before
	public void setUp() {
		manufacturer = "ACME";
		component = "Widget";

		product = new Product(manufacturer, component);

		catalog = new RetailCatalog();
		catalog.addProduct(product);
		catalog.setSalesProfit(product, 1.0);

		random = new Random(100);
	}

	@Test
	public void testConstructor() {
		assertNotNull(new DefaultUserQueryManager(catalog));
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorRetailCatalogNull() {
		new DefaultUserQueryManager(null);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorRandomNull() {
		new DefaultUserQueryManager(catalog, null);
	}

	@Test
	public void testQueryBehavior() {
		DefaultUserQueryManager manager = new DefaultUserQueryManager(catalog,
				random);
		manager.nextTimeUnit(0);

		User nsUser = new User();
		nsUser.setProduct(product);
		nsUser.setState(QueryState.NON_SEARCHING);

		assertEquals(manager.generateQuery(nsUser), null);

		User isUser = new User();
		isUser.setProduct(product);
		isUser.setState(QueryState.INFORMATIONAL_SEARCH);

		Query isQuery = manager.generateQuery(isUser);
		assertTrue(new Query().equals(isQuery)
				|| new Query(manufacturer, null).equals(isQuery)
				|| new Query(null, component).equals(isQuery)
				|| new Query(manufacturer, component).equals(isQuery));

		User f0User = new User();
		f0User.setProduct(product);
		f0User.setState(QueryState.FOCUS_LEVEL_ZERO);

		assertEquals(manager.generateQuery(f0User), new Query());

		User f1User = new User();
		f1User.setProduct(product);
		f1User.setState(QueryState.FOCUS_LEVEL_ONE);

		Query f1Query = manager.generateQuery(f1User);
		assertTrue(new Query(manufacturer, null).equals(f1Query)
				|| new Query(null, component).equals(f1Query));

		User f2User = new User();
		f2User.setProduct(product);
		f2User.setState(QueryState.FOCUS_LEVEL_TWO);

		assertEquals(manager.generateQuery(f2User), new Query(manufacturer,
				component));

		User tUser = new User();
		tUser.setProduct(product);
		tUser.setState(QueryState.TRANSACTED);

		assertEquals(manager.generateQuery(tUser), null);
	}
}
