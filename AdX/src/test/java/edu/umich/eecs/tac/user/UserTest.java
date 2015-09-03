/*
 * UserTest.java
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.umich.eecs.tac.props.Product;

/**
 * @author Patrick Jordan
 */
public class UserTest {
	@Test
	public void testConstructors() {
		assertNotNull(new User());
		assertNotNull(new User(QueryState.NON_SEARCHING, new Product()));
	}

	@Test
	public void testGeneral() {
		User emptyUser = new User();

		assertNull(emptyUser.getState());
		assertNull(emptyUser.getProduct());

		emptyUser.setProduct(new Product());
		emptyUser.setState(QueryState.NON_SEARCHING);

		assertEquals(emptyUser.getState(), QueryState.NON_SEARCHING);
		assertEquals(emptyUser.getProduct(), new Product());

		assertFalse(emptyUser.isSearching());
		assertFalse(emptyUser.isTransacting());

		User user = new User(QueryState.FOCUS_LEVEL_ONE, new Product());

		assertTrue(user.isSearching());
		assertTrue(user.isTransacting());

		assertEquals(new User(), new User());
		assertEquals(user, user);
		assertFalse(user.equals(null));
		assertFalse(user.equals("test"));
		assertFalse(user.equals(new User()));
		assertFalse(new User().equals(user));
		assertFalse(new User(null, new Product()).equals(user));
		assertEquals(new User(QueryState.FOCUS_LEVEL_ONE, new Product())
				.hashCode(), user.hashCode(), 0);
		assertEquals(new User(QueryState.FOCUS_LEVEL_ONE, new Product()), user);

		new User(QueryState.NON_SEARCHING, null).hashCode();
		new User(null, new Product()).hashCode();
	}
}
