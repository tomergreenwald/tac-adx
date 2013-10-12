package tau.tac.adx.parser;

import org.apache.commons.lang3.StringUtils;

import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.logtool.LogReader;
import se.sics.tasim.logtool.ParticipantInfo;
import se.sics.tasim.props.SimulationStatus;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.Parser;

/**
 * <code>BankStatusParser</code> is a simple example of a TAC AA parser that
 * prints out all advertiser's BankStatus received in a simulation from the
 * simulation log file.
 * <p>
 * <p/>
 * The class <code>Parser</code> is inherited to provide base functionality for
 * TAC AA log processing.
 * 
 * @author - Lee Callender
 * 
 * @see edu.umich.eecs.tac.Parser
 */
public class GeneralParser extends Parser {

	private int day = 0;
	private final String[] participantNames;
	private final boolean[] is_Advertiser;
	private final ParticipantInfo[] participants;
	private final ConfigManager configManager;

	private boolean ucs = false;
	private boolean rating = false;

	public GeneralParser(LogReader reader, ConfigManager configManager) {
		super(reader);
		this.configManager = configManager;

		// Print agent indexes/gather names
		System.out.println("****AGENT INDEXES****");
		participants = reader.getParticipants();
		if (participants == null) {
			throw new IllegalStateException("no participants");
		}
		int agent;
		participantNames = new String[participants.length];
		is_Advertiser = new boolean[participants.length];
		for (int i = 0, n = participants.length; i < n; i++) {
			ParticipantInfo info = participants[i];
			agent = info.getIndex();
			System.out.println(info.getName() + ": " + agent);
			participantNames[agent] = info.getName();
			if (info.getRole() == TACAdxConstants.ADVERTISER) {
				is_Advertiser[agent] = true;
			} else
				is_Advertiser[agent] = false;
		}

		System.out.println("****General Log Prser***");
		ucs = configManager.getPropertyAsBoolean("ucs", false);
		rating = configManager.getPropertyAsBoolean("rating", false);
		// System.out.println(StringUtils.rightPad("Day", 20) + "\t"
		// + StringUtils.rightPad("Agent", 20) + "\tQuality Score");
	}

	@Override
	protected void dataUpdated(int type, Transportable content) {

	}

	@Override
	protected void message(int sender, int receiver, Transportable content) {
		if (content instanceof AdNetworkDailyNotification) {
			if (ucs) {
				AdNetworkDailyNotification dailyNotification = (AdNetworkDailyNotification) content;
				System.out.println(StringUtils.rightPad("" + day, 5) + "\t"
						+ StringUtils.rightPad(participantNames[receiver], 20)
						+ "\t" + StringUtils.rightPad("UCS Level: ", 20)
						+ dailyNotification.getServiceLevel());
			}
			if (rating) {
				AdNetworkDailyNotification dailyNotification = (AdNetworkDailyNotification) content;
				System.out.println(StringUtils.rightPad("" + day, 5) + "\t"
						+ StringUtils.rightPad(participantNames[receiver], 20)
						+ "\t" + StringUtils.rightPad("Quality rating: ", 20)
						+ dailyNotification.getQualityScore());
			}
		} else if (content instanceof SimulationStatus) {
			SimulationStatus ss = (SimulationStatus) content;
			day = ss.getCurrentDate();
		}
	}
}
