package tau.tac.adx.parser;

import org.apache.commons.lang3.StringUtils;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.logtool.LogReader;
import se.sics.tasim.logtool.ParticipantInfo;
import se.sics.tasim.props.SimulationStatus;
import se.sics.tasim.props.StartInfo;
import tau.tac.adx.sim.TACAdxConstants;
import edu.umich.eecs.tac.Parser;
import edu.umich.eecs.tac.props.BankStatus;
import edu.umich.eecs.tac.props.RetailCatalog;

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
public class BankStatusParser extends Parser {
	private int day = 0;
	private final String[] participantNames;
	private final boolean[] is_Advertiser;
	private final ParticipantInfo[] participants;

	public BankStatusParser(LogReader reader) {
		super(reader);

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

		System.out.println("****BANK STATUS DATA****");
		System.out.println(StringUtils.rightPad("Agent", 20)
				+ "\tDay\tBank Status");

	}

	// -------------------------------------------------------------------
	// Callbacks from the parser.
	// Please see the class edu.umich.eecs.tac.Parser for more callback
	// methods.
	// -------------------------------------------------------------------

	/**
	 * Invoked when a message to a specific receiver is encountered in the log
	 * file. Example of this is the offers sent by the manufacturers to the
	 * customers.
	 * 
	 * @param sender
	 *            the sender of the message
	 * @param receiver
	 *            the receiver of the message
	 * @param content
	 *            the message content
	 */
	@Override
	protected void message(int sender, int receiver, Transportable content) {
		if (content instanceof BankStatus
				&& participants[receiver].getRole() == TACAdxConstants.AD_NETOWRK_ROLE_ID) {

			BankStatus status = (BankStatus) content;

			System.out.println(StringUtils.rightPad(participantNames[receiver],
					20) + "\t" + day + "\t" + (int) status.getAccountBalance());

		} else if (content instanceof SimulationStatus) {
			SimulationStatus ss = (SimulationStatus) content;
			day = ss.getCurrentDate();
		}
	}

	@Override
	protected void dataUpdated(int type, Transportable content) {
		if (content instanceof StartInfo) {
			StartInfo info = (StartInfo) content;

			// Do stuff with StartInfo
		} else if (content instanceof RetailCatalog) {
			RetailCatalog catalog = (RetailCatalog) content;

			// Do stuff with RetailCatalog
		}
	}
}
