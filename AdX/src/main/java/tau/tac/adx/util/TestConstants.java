/**
 * 
 */
package tau.tac.adx.util;

import tau.tac.adx.users.AdxUser;

/**
 * Constants used for testing.
 * 
 * @author greenwald
 * 
 */
public class TestConstants {

	/**
	 * Amount of {@link AdNetwork ad networks} to generate.
	 */
	public static final int AD_NETWORK_COUNT = 16;
	/**
	 * Amount of types to generate.
	 */
	static public final int AMOUNT_TO_GENERATE = 10000;
	/**
	 * Max weight per value,
	 */
	static public final int MAX_WEIGHT = 100;
	/**
	 * Allowed range of mistake when calculating.
	 */
	public static final int EPSILON_RANGE = (int) (AMOUNT_TO_GENERATE * 0.0002);
	/**
	 * Amount of {@link AdxBidBundle bid bundles} to generate.
	 */
	public static final int BID_BUNDLE_COUNT = 32;
	/**
	 * Shold minor attributes (for {@link AdxUser}) be ignored.
	 */
	public static final boolean IGNORE_MINOR_ATTRIBUTES = true;
	/**
	 * Amount of {@link AdxPublisher publishers} to generate.
	 */
	public static final int PUBLISHER_COUNT = 16;
	/**
	 * Amount of {@link AdxUser users} to generate.
	 */
	public static final int USER_COUNT = 100000;

}
