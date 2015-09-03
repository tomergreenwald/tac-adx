package tau.tac.adx.report.publisher;


/**
 * @author greenwald
 */
public interface AdxPublisherReportSender {

	/**
	 * Broadcast the {@link AdxPublisherReport}.
	 * 
	 * @param report
	 *            {@link AdxPublisherReport} to be broadcasted.
	 */
	public void broadcastPublisherReport(AdxPublisherReport report);
}
