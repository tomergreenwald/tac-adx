/*
 * Parser.java
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
package edu.umich.eecs.tac;

import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Logger;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.Transportable;
import se.sics.isl.transport.ContextFactory;
import se.sics.tasim.logtool.LogReader;
import se.sics.tasim.logtool.ParticipantInfo;
import edu.umich.eecs.tac.props.AAInfo;
import se.sics.tasim.props.ServerConfig;
import static edu.umich.eecs.tac.TACAAConstants.*;

/**
 * The abstract class <code>Parser</code> is a base class that helps with the
 * parsing of TACAA log files.
 * <p>
 * <p/>
 * As a log file is parsed, the <code>Parser</code> will invoke different
 * methods for the various information found in the log file. These methods
 * should be overridden to handle the information.
 * <p>
 * <p/>
 * The order of the method invokations is important and mirrors the order of the
 * information in the log file.
 * <p/>
 * NOTE: This is a modification of Parser.java from TAC SCM
 * 
 * @author Patrick Jordan, Lee Callender
 */
public abstract class Parser {

	private static final Logger log = Logger.getLogger(Parser.class.getName());

	private static final String CONFIG_NAME = new ServerConfig()
			.getTransportName();

	private final LogReader logReader;

	protected Parser(LogReader logReader) {
		this.logReader = logReader;
		ContextFactory aaInfo = new AAInfo(); // Make sure this works
		this.logReader.setContext(aaInfo.createContext());
	}

	/**
	 * Returns the log reader for this log file.
	 */
	protected LogReader getReader() {
		return logReader;
	}

	/**
	 * Starts the log parsing procedure.
	 * 
	 * @throws IOException
	 *             if an error occurs
	 * @throws ParseException
	 *             if an error occurs
	 */
	public final void start() throws IOException, ParseException {
		try {
			parseStarted();
			while (logReader.hasMoreChunks()) {
				TransportReader reader = logReader.nextChunk();
				handleNodes(reader);
			}
		} finally {
			stop();
		}
	}

	private void handleNodes(TransportReader reader) throws ParseException {
		while (reader.nextNode(false)) {

			if (reader.isNode("intUpdated")) {
				int type = reader.getAttributeAsInt("type", 0);
				int agentIndex = reader.getAttributeAsInt("agent", -1);
				int value = reader.getAttributeAsInt("value");
				if (agentIndex >= 0) {
					dataUpdated(agentIndex, type, value);
				}

			} else if (reader.isNode("longUpdated")) {
				int type = reader.getAttributeAsInt("type", 0);
				int agentIndex = reader.getAttributeAsInt("agent", -1);
				long value = reader.getAttributeAsLong("value");
				if (agentIndex >= 0) {
					dataUpdated(agentIndex, type, value);
				}

			} else if (reader.isNode("floatUpdated")) {
				int type = reader.getAttributeAsInt("type", 0);
				int agentIndex = reader.getAttributeAsInt("agent", -1);
				float value = reader.getAttributeAsFloat("value");
				if (agentIndex >= 0) {
					dataUpdated(agentIndex, type, value);
				}

			} else if (reader.isNode("doubleUpdated")) {
				int type = reader.getAttributeAsInt("type", 0);
				int agentIndex = reader.getAttributeAsInt("agent", -1);
				double value = reader.getAttributeAsDouble("value");
				if (agentIndex >= 0) {
					dataUpdated(agentIndex, type, value);
				}

			} else if (reader.isNode("stringUpdated")) {
				int type = reader.getAttributeAsInt("type", 0);
				int agentIndex = reader.getAttributeAsInt("agent", -1);
				String value = reader.getAttribute("value");
				if (agentIndex >= 0) {
					dataUpdated(agentIndex, type, value);
				}

			} else if (reader.isNode("messageToRole")) {
				int sender = reader.getAttributeAsInt("sender");
				int role = reader.getAttributeAsInt("role");
				reader.enterNode();
				reader.nextNode(true);
				Transportable content = reader.readTransportable();
				reader.exitNode();
				messageToRole(sender, role, content);

			} else if (reader.isNode("message")) {
				int receiver = reader.getAttributeAsInt("receiver");
				// Ignore messages to the coordinator
				if (receiver != 0) {
					int sender = reader.getAttributeAsInt("sender");
					reader.enterNode();
					reader.nextNode(true);

					Transportable content = reader.readTransportable();
					reader.exitNode();

					message(sender, receiver, content);
				}

			} else if (reader.isNode("objectUpdated")) {
				int agentIndex = reader.getAttributeAsInt("agent", -1);
				int type = reader.getAttributeAsInt("type", 0);
				reader.enterNode();
				reader.nextNode(true);
				Transportable content = reader.readTransportable();
				reader.exitNode();
				if (agentIndex >= 0) {
					dataUpdated(agentIndex, type, content);
				} else {
					dataUpdated(type, content);
				}

      //Transaction exists here because it is not contained within a Message
      } else if (reader.isNode("transaction")) {
				int source = reader.getAttributeAsInt("source");
				int recipient = reader.getAttributeAsInt("recipient");
				double amount = reader.getAttributeAsDouble("amount");
				transaction(source, recipient, amount);

			} else if (reader.isNode("nextTimeUnit")) {
				int date = reader.getAttributeAsInt("unit");
				long time = reader.getAttributeAsLong("time", 0L);
				nextDay(date, time);

			} else if (reader.isNode(CONFIG_NAME)) {
				Transportable content = reader.readTransportable();
				data(content);

			} else {
				unhandledNode(reader.getNodeName());
			}
		}
	}

	/**
	 * Stops the parser and closes all open files.
	 */
	public final void stop() {
		logReader.close();
		parseStopped();
	}

	// -------------------------------------------------------------------
	// Parser callbacks
	// -------------------------------------------------------------------

	/**
	 * Invoked when the parse process starts.
	 */
	protected void parseStarted() {
	}

	/**
	 * Invoked when the parse process ends.
	 */
	protected void parseStopped() {
	}

	/**
	 * Invoked when a message to all participants with a specific role is
	 * encountered in the log file. Example of this are the BidBundles sent by the
	 * advertisers to the publisher each day. The default implementation will
	 * invoke the <code>message</code> method for all agents with the specific
	 * role. Roles include PUBLISHER, ADVERTISER, and USER from
   * <code>TACAAConstants</code>.
	 * 
	 * @param sender
	 *            the sender of the message
	 * @param role
	 *            the role of all receivers
	 * @param content
	 *            the message content
	 */
	protected void messageToRole(int sender, int role, Transportable content) {
		ParticipantInfo[] infos = logReader.getParticipants();
		if (infos != null) {
			for (int i = 0, n = infos.length; i < n; i++) {
				if (infos[i].getRole() == role) {
					message(sender, infos[i].getIndex(), content);
				}
			}
		}
	}

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
	protected abstract void message(int sender, int receiver,
			Transportable content);

	/**
	 * Invoked when some general data is encountered in the log file. An example
	 * of this is the server configuration for the simulation.
	 * 
	 * @param object
	 *            the data container
	 * @see se.sics.tasim.props.ServerConfig
	 */
	protected void data(Transportable object) {
	}

	/**
	 * Invoked when a data update is encountered in the log file.
	 * 
	 * @param agent
	 *            the agent for which the data was updated
	 * @param type
	 *            the type of the data
	 * @param value
	 *            the data value
	 */
	protected void dataUpdated(int agent, int type, int value) {
	}

	/**
	 * Invoked when a data update is encountered in the log file.
	 * 
	 * @param agent
	 *            the agent for which the data was updated
	 * @param type
	 *            the type of the data
	 * @param value
	 *            the data value
	 */
	protected void dataUpdated(int agent, int type, long value) {
	}

	/**
	 * Invoked when a data update is encountered in the log file.
	 * 
	 * @param agent
	 *            the agent for which the data was updated
	 * @param type
	 *            the type of the data
	 * @param value
	 *            the data value
	 */
	protected void dataUpdated(int agent, int type, float value) {
	}

	/**
	 * Invoked when a data update is encountered in the log file.
	 * 
	 * @param agent
	 *            the agent for which the data was updated
	 * @param type
	 *            the type of the data
	 * @param value
	 *            the data value
	 */
	protected void dataUpdated(int agent, int type, double value) {
	}

	/**
	 * Invoked when a data update is encountered in the log file.
	 * 
	 * @param agent
	 *            the agent for which the data was updated
	 * @param type
	 *            the type of the data
	 * @param value
	 *            the data value
	 */
	protected void dataUpdated(int agent, int type, String value) {
	}

	/**
	 * Invoked when a data update is encountered in the log file.
	 * 
	 * @param agent
	 *            the agent for which the data was updated
	 * @param type
	 *            the type of the data
	 */
	protected void dataUpdated(int agent, int type, Transportable content) {
	}

	/**
	 * Invoked when a data update is encountered in the log file. Examples of
	 * this is the <code>StartInfo</code> and <code>RetailCatalog</code> in the
   * beginning of a simulation.
	 * 
	 * @param type
	 *            the type of the data
	 * @param content
	 *            the data value
	 */
	protected void dataUpdated(int type, Transportable content) {
	}

	/**
	 * Invoked when a transaction is encountered in the log file.
	 * 
	 * @param source
	 *            the source which receives the payment
	 * @param recipient
	 *            the paying recipient
	 * @param amount
	 *            the transacted amount
	 */
	protected void transaction(int source, int recipient, double amount) {
	}

	/**
	 * Invoked when a new day notification is encountered in the log file.
	 * 
	 * @param date
	 *            the new day in the simulation
	 * @param serverTime
	 *            the server time at that point in the simulation
	 */
	protected void nextDay(int date, long serverTime) {
	}

	/**
	 * Invoked when an unknown (unhandled) node is encountered in the log file.
	 * The default implementation simply outputs a warning.
	 * 
	 * @param nodeName
	 *            the name of the unhandled node.
	 */
	protected void unhandledNode(String nodeName) {
		// Ignore anything else for now
		log.warning("ignoring unhandled node '" + nodeName + '\'');
	}

} // Parser
