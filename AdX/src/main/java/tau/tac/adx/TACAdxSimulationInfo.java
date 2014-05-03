/*
 * TACAASimulationInfo.java
 *
 * COPYRIGHT  2008
 * THE REGENTS OF THE UNIVERSITY OF MICHIGAN
 * ALL RIGHTS RESERVED
 *
 * PERMISSION IS GRANTED TO USE, COPY, CREATE DERIVATIVE WORKS AND REDISTRIBUTE THIS
 * SOFTWARE AND SUCH DERIVATIVE WORKS FOR NONCOMMERCIAL EDUCATION AND RESEARCH
 * PURPOSES, SO LONG AS NO FEE IS CHARGED, AND SO LONG AS THE COPYRIGHT NOTICE
 * ABOVE, THIS GRANT OF PERMISSION, AND THE DISCLAIMER BELOW APPEAR IN ALL COPIES
 * MADE; AND SO LONG AS THE NAME OF THE UNIVERSITY OF MICHIGAN IS NOT USED IN ANY
 * ADVERTISING OR PUBLICITY PERTAINING TO THE USE OR DISTRIBUTION OF THIS SOFTWARE
 * WITHOUT SPECIFIC, WRITTEN PRIOR AUTHORIZATION.
 *
 * THIS SOFTWARE IS PROVIDED AS IS, WITHOUT REPRESENTATION FROM THE UNIVERSITY OF
 * MICHIGAN AS TO ITS FITNESS FOR ANY PURPOSE, AND WITHOUT WARRANTY BY THE
 * UNIVERSITY OF MICHIGAN OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT
 * LIMITATION THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE REGENTS OF THE UNIVERSITY OF MICHIGAN SHALL NOT BE LIABLE FOR ANY
 * DAMAGES, INCLUDING SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WITH
 * RESPECT TO ANY CLAIM ARISING OUT OF OR IN CONNECTION WITH THE USE OF THE SOFTWARE,
 * EVEN IF IT HAS BEEN OR IS HEREAFTER ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package tau.tac.adx;

import static tau.tac.adx.sim.TACAdxConstants.ADVERTISER;
import static tau.tac.adx.sim.TACAdxConstants.DU_BANK_ACCOUNT;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.logtool.LogReader;
import se.sics.tasim.logtool.ParticipantInfo;
import se.sics.tasim.props.ServerConfig;
import tau.tac.adx.sim.TACAdxConstants;

import com.botbox.util.ArrayUtils;

import edu.umich.eecs.tac.Parser;
import edu.umich.eecs.tac.Participant;
import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.SlotInfo;

/**
 * @author Patrick Jordan, Lee Callender
 */
public class TACAdxSimulationInfo extends Parser {

	private static final Logger log = Logger
			.getLogger(TACAdxSimulationInfo.class.getName());

	private int simID;
	private int uniqueID;
	private String simType;
	private String simParams;
	private long startTime;
	private int simLength;

	private String serverName;
	private String serverVersion;

	private ServerConfig serverConfig;

	private SlotInfo slotInfo;
	private RetailCatalog retailCatalog;

	private Participant[] participants;
	private Hashtable participantTable;

	private int[] agentRoles;
	private Participant[][] agentsPerRole;
	private int agentRoleNumber;

	private int currentDate = 0;

	private boolean isParsingExtended = false;

	// Note: the context must have been set in the log reader before
	// this object is created!
	public TACAdxSimulationInfo(LogReader logReader) throws IOException,
			ParseException {
		super(logReader);

		ParticipantInfo[] infos = logReader.getParticipants();
		participants = new Participant[infos == null ? 0 : infos.length];
		participantTable = new Hashtable();
		for (int i = 0, n = participants.length; i < n; i++) {
			ParticipantInfo info = infos[i];
			if (info != null) {
				participants[i] = new Participant(info);
				participantTable.put(info.getAddress(), participants[i]);
			}
		}
		simID = logReader.getSimulationID();
		uniqueID = logReader.getUniqueID();
		simType = logReader.getSimulationType();
		simParams = logReader.getSimulationParams();
		startTime = logReader.getStartTime();
		simLength = logReader.getSimulationLength();
		serverName = logReader.getServerName();
		serverVersion = logReader.getServerVersion();

		start();

		// Extract the rest of the information
		Participant[] advertisers = getParticipantsByRole(ADVERTISER);
		/*
		 * if (advertisers != null) { Participant m = advertisers[0]; StartInfo
		 * si = m.getStartInfo(); }
		 */
	}

	public String getServerName() {
		return serverName;
	}

	public String getServerVersion() {
		return serverVersion;
	}

	public int getUniqueID() {
		return uniqueID;
	}

	public int getSimulationID() {
		return simID;
	}

	public String getSimulationType() {
		return simType;
	}

	public long getStartTime() {
		return startTime;
	}

	public int getSimulationLength() {
		return simLength;
	}

	public ServerConfig getServerConfig() {
		return serverConfig;
	}

	public Participant getParticipant(int agentIndex) {
		Participant p;
		if (agentIndex >= participants.length
				|| ((p = participants[agentIndex]) == null)) {
			throw new IllegalArgumentException("no participant " + agentIndex);
		}
		return p;
	}

	public Participant getParticipant(String address) {
		Participant p = (Participant) participantTable.get(address);
		if (p == null) {
			throw new IllegalArgumentException("no participant " + address);
		}
		return p;
	}

	public int getParticipantCount() {
		return participants.length;
	}

	public Participant[] getParticipants() {
		return participants;
	}

	public Participant[] getParticipantsByRole(int role) {
		int index = ArrayUtils.indexOf(agentRoles, 0, agentRoleNumber, role);
		if (index < 0) {
			if (agentRoles == null) {
				agentRoles = new int[5];
				agentsPerRole = new Participant[5][];
			} else if (agentRoleNumber == agentRoles.length) {
				agentRoles = ArrayUtils
						.setSize(agentRoles, agentRoleNumber + 5);
				agentsPerRole = (Participant[][]) ArrayUtils.setSize(
						agentsPerRole, agentRoleNumber + 5);
			}

			ArrayList list = new ArrayList();
			for (int i = 0, n = participants.length; i < n; i++) {
				Participant a = participants[i];
				if ((a != null) && (a.getInfo().getRole() == role)) {
					list.add(a);
				}
			}

			index = agentRoleNumber;
			agentsPerRole[agentRoleNumber] = list.size() > 0 ? (Participant[]) list
					.toArray(new Participant[list.size()])
					: null;
			agentRoles[agentRoleNumber++] = role;
		}
		return agentsPerRole[index];
	}

	// -------------------------------------------------------------------
	// Parser callback handling
	// -------------------------------------------------------------------

	protected void messageToRole(int sender, int role, Transportable content) {
		for (int i = 0, n = participants.length; i < n; i++) {
			if (participants[i].getInfo().getRole() == role) {
				participants[i].messageReceived(currentDate, sender, content);
			}
		}

		if (sender != 0) {
			getParticipant(sender)
					.messageSentToRole(currentDate, role, content);
		}
	}

	protected void message(int sender, int receiver, Transportable content) {
		// Ignore messages to the coordinator
		if (receiver != 0) {
			getParticipant(receiver).messageReceived(currentDate, sender,
					content);
			if (sender != 0) {
				getParticipant(sender).messageSent(currentDate, receiver,
						content);
			}
		}
	}

	protected void data(Transportable object) {
		if (object instanceof ServerConfig) {
			this.serverConfig = (ServerConfig) object;
		}
	}

	protected void dataUpdated(int agentIndex, int type, int value) {
		switch (type) {
		case TACAdxConstants.DU_AD_NETWORK_WIN_COUNT:
			impressions(agentIndex, value);
			break;
		}
	}

	protected void dataUpdated(int agentIndex, int type, long value) {
	}

	protected void dataUpdated(int agentIndex, int type, double value) {
		switch (type) {
		case TACAdxConstants.DU_AD_NETWORK_REVENUE:
			revenue(agentIndex, value);
			break;
		case TACAdxConstants.DU_AD_NETWORK_ADX_EXPENSE:
			adxCost(agentIndex, value);
			break;
		case TACAdxConstants.DU_AD_NETWORK_UCS_EXPENSE:
			ucsCost(agentIndex, value);
			break;
		case TACAdxConstants.DU_AD_NETWORK_QUALITY_RATING:
			qualityRating(agentIndex, value);
			break;
		case TACAdxConstants.DU_AD_NETWORK_BANK_ACCOUNT:
			Participant p = getParticipant(agentIndex);
			p.setResult(value);
			break;
		}
	}

	protected void dataUpdated(int agent, int type, float value) {
	}

	protected void dataUpdated(int agent, int type, String value) {
	}

	protected void dataUpdated(int agent, int type, Transportable content) {
	}
	

	protected void dataUpdated(int type, Transportable object) {
		if (object instanceof SlotInfo) {
			this.slotInfo = (SlotInfo) object;
		} else if (object instanceof RetailCatalog) {
			this.retailCatalog = (RetailCatalog) object;
		}
	}
	
	private void qualityRating(int agentIndex, double value) {
		Participant p = getParticipant(agentIndex);
		p.setQualityRating(value);
	}
	
	private void ucsCost(int agentIndex, double value) {
		Participant p = getParticipant(agentIndex);
		p.addUCSCost(value);
	}
	
	private void adxCost(int agentIndex, double value) {
		Participant p = getParticipant(agentIndex);
		p.addADXCost(value);
	}

	private void revenue(int agentIndex, double value) {
		Participant p = getParticipant(agentIndex);
		p.addRevenue(value);
	}

	protected void impressions(int agent, long amount) {
		Participant agentInfo = getParticipant(agent);
		agentInfo.addImpressions(amount);
	}

	protected void nextDay(int date, long serverTime) {
		this.currentDate = date;
	}

	protected void unhandledNode(String nodeName) {
		// Ignore anything else for now
		log.warning("ignoring unhandled node '" + nodeName + '\'');
	}
} // TACSCMSimulationInfo
