package tau.tac.adx.sim;

import tau.tac.adx.sim.report.publisher.AdxPublisherReport;

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
	public void broadcastReport(AdxPublisherReport report);
}
