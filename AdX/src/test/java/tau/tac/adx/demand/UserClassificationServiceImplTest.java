/**
 * 
 */
package tau.tac.adx.demand;

import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Tomer
 * 
 */
public class UserClassificationServiceImplTest {

	/**
	 * Test method for all methods.
	 */
	@Test
	public void testUCS() {
		UserClassificationServiceImpl impl = new UserClassificationServiceImpl();
		Random random = new Random();
		double bid1 = random.nextDouble() * 10;
		double bid2 = bid1 / 2;
		impl.updateAdvertiserBid("hello", bid1, 1);
		impl.updateAdvertiserBid("hello2", bid2, 1);
		Assert.assertNull(impl.getAdNetData("hello"));
		Assert.assertNull(impl.getAdNetData("hello2"));
		// generate auction results
		impl.auction(1, false);
		// copy them as todays results
		impl.auction(2, false);
		Assert.assertEquals(bid2, impl.getAdNetData("hello").getPrice());
		Assert.assertEquals(0.0, impl.getAdNetData("hello2").getPrice());
	}
}
