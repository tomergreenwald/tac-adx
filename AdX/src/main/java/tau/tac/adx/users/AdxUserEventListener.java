package tau.tac.adx.users;

import tau.tac.adx.props.AdxQuery;
import edu.umich.eecs.tac.props.Ad;

/**
 * Adx user event listener interface.
 * 
 * @author greenwald
 */
public interface AdxUserEventListener {
	/**
	 * @param query
	 *            Issued {@link AdxQuery}.
	 */
	void queryIssued(AdxQuery query);

	/**
	 * Viewed {@link Ad}.
	 * 
	 * @param query
	 *            {@link AdxQuery} which issued the <b>auction</b>.
	 * @param ad
	 *            {@link Ad}.
	 * @param advertiser
	 *            Advertiser name.
	 */
	void adViewed(AdxQuery query, Ad ad, String advertiser);
}
