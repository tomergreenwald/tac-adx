/*
 * DefaultUsersInitializerTest.java
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

import java.util.LinkedList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.umich.eecs.tac.props.Product;

/**
 * @author Patrick Jordan
 */
@RunWith(JMock.class)
public class DefaultUsersInitializerTest {
	private Mockery context;

	private UserTransitionManager userTransitionManager;

	private DefaultUsersInitializer initializer;

	private List<User> users;

	@Before
	public void setup() {
		context = new JUnit4Mockery();

		userTransitionManager = context.mock(UserTransitionManager.class);

		initializer = new DefaultUsersInitializer(userTransitionManager);

		users = new LinkedList<User>();

		for (int i = 0; i < 2; i++) {
			users.add(new User(QueryState.NON_SEARCHING, new Product()));
		}
	}

	@Test(expected = NullPointerException.class)
	public void testConstructor() {
		assertNotNull(initializer);
		new DefaultUsersInitializer(null);
	}

	@Test
	public void testInitialized() {
		context.checking(new Expectations() {
			{
				atLeast(1).of(userTransitionManager).nextTimeUnit(-1);
				atLeast(1).of(userTransitionManager).transition(users.get(0), false);
				will(returnValue(QueryState.INFORMATIONAL_SEARCH));
                atLeast(1).of(userTransitionManager).transition(users.get(1), false);
				will(returnValue(QueryState.INFORMATIONAL_SEARCH));
			}
		});

		for (User user : users) {
			assertEquals(user.getState(), QueryState.NON_SEARCHING);
		}

		initializer.initialize(users, 1);

		for (User user : users) {
			assertEquals(user.getState(), QueryState.INFORMATIONAL_SEARCH);
		}
	}
}
