/*
 * UserEventSupportTest.java
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import tau.tac.adx.props.AdLink;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.Query;

/**
 * @author Patrick Jordan
 */
@RunWith(JMock.class)
public class UserEventSupportTest {
	private Mockery context;

	private UserEventListener listener;
	private Query query;
	private String advertiser;
	private AdLink adLink;
    private Ad ad;
	private double cpc;
	private int slot;
	private double salesProfit;

    @Before
	public void setup() {
		context = new JUnit4Mockery();
		listener = context.mock(UserEventListener.class);
		query = new Query();
		advertiser = "alice";
        ad = new Ad();
		adLink = new AdLink(ad, advertiser);
		cpc = 1.0;
		slot = 2;
		salesProfit = 3.0;
	}

	@Test
	public void testConstructor() {
		assertNotNull(new UserEventSupport());
	}

	@Test
	public void testAddListener() {
		UserEventSupport support = new UserEventSupport();

		assertFalse(support.containsUserEventListener(listener));
		support.addUserEventListener(listener);
		assertTrue(support.containsUserEventListener(listener));
	}

	@Test
	public void testRemoveListener() {
		UserEventSupport support = new UserEventSupport();

		support.addUserEventListener(listener);

		assertTrue(support.containsUserEventListener(listener));

		support.removeUserEventListener(listener);

		assertFalse(support.containsUserEventListener(listener));
	}

	@Test
	public void testFireQueryIssued() {
		UserEventSupport support = new UserEventSupport();

		support.addUserEventListener(listener);

		context.checking(new Expectations() {
			{
				oneOf(listener).queryIssued(null);
			}
		});

		support.fireQueryIssued(null);
	}

	@Test
	public void testFireAdViewed() {

		UserEventSupport support = new UserEventSupport();

		support.addUserEventListener(listener);

		context.checking(new Expectations() {
			{
				oneOf(listener).viewed(query, ad, slot, advertiser, false);
			}
		});

		support.fireAdViewed(query, adLink, slot, false);
	}

	@Test
	public void testFfireAdClicked() {

		UserEventSupport support = new UserEventSupport();

		support.addUserEventListener(listener);

		context.checking(new Expectations() {
			{
				oneOf(listener).clicked(query, ad, slot, cpc, advertiser);
			}
		});

		support.fireAdClicked(query, adLink, slot, cpc);
	}

	@Test
	public void testFireAdConverted() {
		UserEventSupport support = new UserEventSupport();

		support.addUserEventListener(listener);

		context.checking(new Expectations() {
			{
				oneOf(listener).converted(query, ad, slot, salesProfit, advertiser);
			}
		});

		support.fireAdConverted(query, adLink, slot, salesProfit);
	}
}
