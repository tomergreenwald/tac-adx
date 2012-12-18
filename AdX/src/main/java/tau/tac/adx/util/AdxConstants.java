package tau.tac.adx.util;


/**
 * A <b>constants</b> class for the AdX system.
 * 
 * @author greenwald
 * 
 */
public class AdxConstants {

	/**
	 * System-wide {@link EventBus} name.
	 */
	public static final String ADX_EVENT_BUS_NAME = "AdX";

	/**
	 * The price range is between 0 and 100. Was chosen arbitrary.
	 */
	public static final double MAX_AD_PRICE = 100;

	/**
	 * The price range is between 0 and 100 million. Was chosen arbitrary.
	 * 
	 * @see MonthlyContract
	 */
	public static final int MAX_MONTHLY_BUDGET = 100000000;

	/**
	 * A month lasts 30 days.
	 */
	public static final double MONTH_LENGTH = 30;

}
