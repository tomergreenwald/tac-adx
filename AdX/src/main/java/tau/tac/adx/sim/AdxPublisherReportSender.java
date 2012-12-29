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
<<<<<<< HEAD
	public void broadcastReport(AdxPublisherReport report);
=======
	void broadcastReport(AdxPublisherReport report);
>>>>>>> branch 'agents' of https://tomerg@code.google.com/p/tac-adx
}
