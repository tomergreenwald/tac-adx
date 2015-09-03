/*
 * UserUtilsTest.java
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

import static edu.umich.eecs.tac.user.UserUtils.calculateClickProbability;
import static edu.umich.eecs.tac.user.UserUtils.calculateConversionProbability;
import static edu.umich.eecs.tac.user.UserUtils.findAdvertiserEffect;
import static edu.umich.eecs.tac.user.UserUtils.modifyOdds;
import static edu.umich.eecs.tac.user.UserUtils.modifyOddsForComponentSpecialty;
import static edu.umich.eecs.tac.user.UserUtils.modifySalesProfitForManufacturerSpecialty;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.BeforeClass;
import org.junit.Test;

import tau.tac.adx.props.AdLink;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.Product;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.QueryType;
import edu.umich.eecs.tac.props.UserClickModel;

/**
 * @author Patrick Jordan
 */
public class UserUtilsTest {
	private static Product product;
	private static User user;
	private static Query query;
	private static AdvertiserInfo advertiserInfo;
	private static AdLink genericAdLink;
	private static AdLink focusedAdLink;
	private static AdLink focusedWrongAdLink;
	private static String advertiser;
	private static String manufacturer;
	private static String component;
	private static UserClickModel userClickModel;

	@BeforeClass
	public static void setup() {
		manufacturer = "man";
		component = "com";
		product = new Product(manufacturer, component);
		user = new User(QueryState.NON_SEARCHING, product);
		query = new Query();
		advertiserInfo = new AdvertiserInfo();
		advertiserInfo.setDistributionCapacity(2);
		advertiserInfo.setFocusEffects(QueryType.FOCUS_LEVEL_ZERO, 0.5);
		advertiserInfo.setDistributionCapacityDiscounter(0.5);
		advertiserInfo.setComponentBonus(2.0);
		advertiserInfo.setComponentSpecialty(component);
		advertiserInfo.setTargetEffect(0.5);

		advertiser = "alice";
		genericAdLink = new AdLink((Product) null, advertiser);
		focusedAdLink = new AdLink(product, advertiser);
		focusedWrongAdLink = new AdLink(new Product(manufacturer, "not"
				+ component), advertiser);

		userClickModel = new UserClickModel(new Query[] { query },
				new String[] { advertiser });
		userClickModel.setAdvertiserEffect(0, 0, 0.8);

	}

	@Test(expected = IllegalAccessException.class)
	public void testConstructor() throws NoSuchMethodException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		Constructor constructor = UserUtils.class.getDeclaredConstructor();
		constructor.newInstance();
	}

	@Test
	public void testModifyOdds() {
		assertEquals(modifyOdds(0.5, 2.0), 2.0 / 3.0, 0.000001);
	}

	@Test
	public void testModifySalesProfitForManufacturerSpecialty() {
		assertEquals(modifySalesProfitForManufacturerSpecialty(user,
				manufacturer, 0.5, 1.0), 1.5, 0.0000001);
		assertEquals(modifySalesProfitForManufacturerSpecialty(user, "not"
				+ manufacturer, 0.5, 1.0), 1.0, 0.0000001);
	}

	@Test
	public void testModifyOddsForComponentSpecialty() {
		assertEquals(
				modifyOddsForComponentSpecialty(user, component, 1.0, 0.5),
				2.0 / 3.0, 0.0000001);
		assertEquals(modifyOddsForComponentSpecialty(user, "not" + component,
				1.0, 0.5), 0.5, 0.0000001);
	}

	@Test
	public void testCalculateConversionProbability() {
		assertEquals(calculateConversionProbability(user, query,
				advertiserInfo, 1.0), 0.75, 0.0000001);
	}

	@Test
	public void testCalculateClickProbability() {
		assertEquals(calculateClickProbability(user, genericAdLink.getAd(),
				advertiserInfo.getTargetEffect(), 0.0, 0.8), 0.8, 0.0000001);
		assertEquals(calculateClickProbability(user, focusedAdLink.getAd(),
				advertiserInfo.getTargetEffect(), 0.0, 0.8),
				0.8571428571428572, 0.0000001);
		assertEquals(calculateClickProbability(user, focusedWrongAdLink.getAd(),
				advertiserInfo.getTargetEffect(), 0.0, 0.8),
				0.7272727272727273, 0.0000001);

		assertEquals(calculateClickProbability(user, genericAdLink.getAd(),
				advertiserInfo.getTargetEffect(), 0.5, 0.8),
				0.8571428571428572, 0.0000001);
		assertEquals(calculateClickProbability(user, focusedAdLink.getAd(),
				advertiserInfo.getTargetEffect(), 0.5, 0.8), 0.9, 0.0000001);
		assertEquals(calculateClickProbability(user, focusedWrongAdLink.getAd(),
				advertiserInfo.getTargetEffect(), 0.5, 0.8), 0.8, 0.0000001);
	}

	@Test
	public void testFindAdvertiserEffect() {

		assertEquals(
				findAdvertiserEffect(query, genericAdLink, userClickModel),
				0.8, 0.0000001);
		assertEquals(findAdvertiserEffect(query, new AdLink(product, "bob"),
				userClickModel), 0.0, 0.0000001);
		assertEquals(findAdvertiserEffect(new Query("a", "b"), genericAdLink,
				userClickModel), 0.0, 0.0000001);
	}
}
