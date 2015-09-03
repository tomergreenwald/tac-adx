/*
 * DefaultUserTransitionManagerTest.java
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

import java.util.Random;

import org.junit.Before;
import org.junit.Test;


import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.RetailCatalog;

/**
 * @author Patrick Jordan
 */
public class DefaultUserTransitionManagerTest {
	private DefaultUserTransitionManager userTransitionManager;
	private Random random;
    private RetailCatalog retailCatalog;
    private User user;
    private Product product;

	@Before
	public void setup() {
        product = new Product("man","com");
        retailCatalog = new RetailCatalog();
        retailCatalog.addProduct(product);
        user = new User(QueryState.NON_SEARCHING, product);
		random = new Random(100);
		userTransitionManager = new DefaultUserTransitionManager(retailCatalog, random);

		userTransitionManager.setBurstProbability(0.3,0.6,3);

		userTransitionManager.addStandardTransitionProbability(
				QueryState.NON_SEARCHING, QueryState.INFORMATIONAL_SEARCH, 0.2);
		userTransitionManager.addStandardTransitionProbability(
				QueryState.NON_SEARCHING, QueryState.NON_SEARCHING, 0.8);
		userTransitionManager.addBurstTransitionProbability(
				QueryState.NON_SEARCHING, QueryState.INFORMATIONAL_SEARCH, 0.4);
		userTransitionManager.addBurstTransitionProbability(
				QueryState.NON_SEARCHING, QueryState.NON_SEARCHING, 0.6);
	}

	@Test
	public void testConstructors() {
		assertNotNull(new DefaultUserTransitionManager(retailCatalog));
		assertNotNull(userTransitionManager);
	}

	@Test(expected = NullPointerException.class)
	public void testConstructorRandomNull() {
		new DefaultUserTransitionManager(null);
	}



	@Test
	public void testTransitions() {
		assertEquals(userTransitionManager.getBurstProbability(), 0.3, 0.00001);

		boolean burst = userTransitionManager.isBurst(user.getProduct());

		int nsCount = 0;
		int isCount = 0;

		for (int i = 0; i < 1000; i++) {
			QueryState state = userTransitionManager.transition(user, false);

			switch (state) {
			case NON_SEARCHING:
				nsCount++;
				break;
			case INFORMATIONAL_SEARCH:
				isCount++;
				break;
			}
		}

		if (burst) {
			assertEquals(nsCount, 600, 0);
			assertEquals(isCount, 400, 0);
		} else {
			assertEquals(nsCount, 809, 0);
			assertEquals(isCount, 191, 0);
		}

		while (userTransitionManager.isBurst(product) == burst) {
			userTransitionManager.nextTimeUnit(0);
		}

		nsCount = 0;
		isCount = 0;

		for (int i = 0; i < 1000; i++) {
			QueryState state = userTransitionManager.transition(user, false);

			switch (state) {
			case NON_SEARCHING:
				nsCount++;
				break;
			case INFORMATIONAL_SEARCH:
				isCount++;
				break;
			}
		}

		if (!burst) {
			assertEquals(nsCount, 594, 0);
			assertEquals(isCount, 406, 0);
		} else {
			assertEquals(nsCount, 800, 0);
			assertEquals(isCount, 200, 0);
		}

		assertEquals(userTransitionManager.transition(user,true), QueryState.TRANSACTED);
	}
}
