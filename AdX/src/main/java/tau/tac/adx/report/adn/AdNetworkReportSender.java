package tau.tac.adx.report.adn;

import tau.tac.adx.bids.Bidder;

/**
 * @author greenwald
 */
public interface AdNetworkReportSender {

	/**
	 * Broadcast the {@link AdNetworkReport}.
	 * 
	 * @param adNetworkId
	 *            {@link Bidder} adNetwork id.
	 * 
	 * @param report
	 *            {@link AdNetworkReport} to be broadcasted.
	 */
	public void broadcastAdNetowrkReport(String adNetworkId, AdNetworkReport report);
}
