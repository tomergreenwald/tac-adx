package tau.tac.adx.report.adn;


/**
 * @author greenwald
 */
public interface AdNetworkReportSender {

	/**
	 * Broadcast the {@link AdNetworkReport}.
	 * 
	 * @param report
	 *            {@link AdNetworkReport} to be broadcasted.
	 */
	public void broadcastReport(AdNetworkReport report);
}
