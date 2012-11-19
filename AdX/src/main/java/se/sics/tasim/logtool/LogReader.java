/**
 * TAC Supply Chain Management Simulator
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001-2005 SICS AB. All rights reserved.
 *
 * SICS grants you the right to use, modify, and redistribute this
 * software for noncommercial purposes, on the conditions that you:
 * (1) retain the original headers, including the copyright notice and
 * this text, (2) clearly document the difference between any derived
 * software and the original, and (3) acknowledge your use of this
 * software in pertaining publications and reports.  SICS provides
 * this software "as is", without any warranty of any kind.  IN NO
 * EVENT SHALL SICS BE LIABLE FOR ANY DIRECT, SPECIAL OR INDIRECT,
 * PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSSES OR DAMAGES ARISING OUT
 * OF THE USE OF THE SOFTWARE.
 *
 * -----------------------------------------------------------------
 *
 * LogReader
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Thu Jan 23 15:20:09 2003
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.logtool;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.zip.GZIPInputStream;

import com.botbox.util.ArrayUtils;
import se.sics.isl.transport.BinaryTransportReader;
import se.sics.isl.transport.Context;
import se.sics.isl.transport.TransportReader;

/**
 * Utility for parsing server logs (not used by the AgentWare).
 */
public class LogReader {

	/** The address to the simulation coordinator */
	private static final String COORDINATOR = "coordinator";

	private static final int DEFAULT_MAX_BUFFER_SIZE = 5 * 1024 * 1024;

	private DataInputStream input;
	private BinaryTransportReader reader;
	private int maxBufferSize = DEFAULT_MAX_BUFFER_SIZE;
	private boolean dataRead = false;

	private byte[] buffer = new byte[2048];

	/** Simulation information */
	private int simID;
	private int uniqueID;
	private String simType;
	private String simParams;
	private long startTime;
	private int simLength;

	private String serverName;
	private String serverVersion;

	private int majorVersion;
	private int minorVersion;
	private boolean isComplete = false;
	// private boolean isFinished = false;
	// private boolean isScratched = false;

	private ParticipantInfo[] participants;
	private boolean isCancelled = false;

	public LogReader(InputStream in) throws IOException, ParseException {
		this(in, true);
	}

	private LogReader(InputStream in, boolean readHeader) throws IOException,
			ParseException {
		this.input = new DataInputStream(in);
		this.reader = new BinaryTransportReader();

		readTACTHeader();

		if (readHeader) {
			readHeader();
		}
	}

	private void readTACTHeader() throws IOException {
		input.readFully(buffer, 0, 8);

		if ((buffer[0] & 0xff) != 'T' || (buffer[1] & 0xff) != 'A'
				|| (buffer[2] & 0xff) != 'C' || (buffer[3] & 0xff) != 'T') {
			// Illegal header: probably wrong type of data file
			throw new IOException("not a simulation log file: '"
					+ new String(buffer, 0, 4) + '\'');
		} else {
			majorVersion = buffer[4] & 0xff;
			minorVersion = buffer[5] & 0xff;
		}
	}

	private void readHeader() throws IOException, ParseException {
		TransportReader reader = nextChunk();
		reader.nextNode("simulation", true);
		simID = reader.getAttributeAsInt("simID", -1);
		uniqueID = reader.getAttributeAsInt("id");
		simType = reader.getAttribute("type");
		simParams = reader.getAttribute("params", null);
		startTime = reader.getAttributeAsLong("startTime");
		simLength = reader.getAttributeAsInt("length") * 1000;
		serverName = reader.getAttribute("serverName", null);
		serverVersion = reader.getAttribute("version", null);
		reader.enterNode();

		// The Coordinator. How should this be handled??? FIX THIS!!! TODO
		int participantCount = 1;
		participants = new ParticipantInfo[15];
		participants[0] = new ParticipantInfo(0, COORDINATOR, -1, null, -1);

		while (reader.nextNode("participant", false)) {
			int index = reader.getAttributeAsInt("index");
			ParticipantInfo p = new ParticipantInfo(index, reader
					.getAttribute("address"), reader
					.getAttributeAsInt("id", -1), reader.getAttribute("name",
					null), reader.getAttributeAsInt("role"));
			if (index >= participants.length) {
				// Should check for ridiculous large indexes. FIX THIS!!! TODO
				participants = (ParticipantInfo[]) ArrayUtils.setSize(
						participants, index + 10);
			}
			if (participants[index] != null) {
				throw new ParseException("participant " + index
						+ " already set", 0); // Position?
				// FIX
				// THIS!!!
				// TODO
			}
			participants[index] = p;
			if (participantCount <= index) {
				participantCount = index + 1;
			}
		}

		// Perhaps should verify that all participants has been specified? FIX
		// THIS!!! TODO
		if (participantCount < participants.length) {
			participants = (ParticipantInfo[]) ArrayUtils.setSize(participants,
					participantCount);
		}
		reader.exitNode();
	}

	public int getSimulationID() {
		return simID;
	}

	public int getUniqueID() {
		return uniqueID;
	}

	public String getSimulationType() {
		return simType;
	}

	public String getSimulationParams() {
		return simParams;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return startTime + simLength;
	}

	/**
	 * Returns the length of the simulation in milliseconds.
	 */
	public int getSimulationLength() {
		return simLength;
	}

	// public boolean isFinished() {
	// return isFinished;
	// }

	// public void setFinished(boolean isFinished) {
	// this.isFinished = isFinished;
	// }

	// public boolean isScratched() {
	// return isScratched;
	// }

	// public void setScratched(boolean isScratched) {
	// this.isScratched = isScratched;
	// }

	/**
	 * Returns true if the log file has been successfully read to its end
	 */
	public boolean isComplete() {
		return isComplete;
	}

	public ParticipantInfo[] getParticipants() {
		return participants;
	}

	public String getServerName() {
		return serverName;
	}

	public String getServerVersion() {
		return serverVersion;
	}

	public int getMaxBufferSize() {
		return maxBufferSize;
	}

	public void setMaxBufferSize(int maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}

	public void setContext(Context context) {
		if (reader != null) {
			reader.setContext(context);
		}
	}

	public synchronized boolean hasMoreChunks() throws IOException {
		if (!dataRead) {
			read();
		}
		return dataRead;
	}

	public synchronized TransportReader nextChunk() throws IOException {
		if (!dataRead) {
			read();
		}
		if (dataRead) {
			dataRead = false;
			return reader;
		} else {
			throw new EOFException();
		}
	}

	public boolean isClosed() {
		return reader != null;
	}

	public synchronized void close() {
		if (reader != null) {
			dataRead = false;
			reader = null;
			try {
				input.close();
			} catch (Exception e) {
				// Ignore any problems when closing the file
			} finally {
				input = null;
			}
		}
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	public void cancel() {
		if (!isCancelled) {
			isCancelled = true;
			if (!isClosed()) {
				close();
			}
		}
	}

	// Note: MAY ONLY BE CALLED SYNCHRONIZED
	private void read() throws IOException {
		if (reader == null) {
			dataRead = false;
			return;
		}

		int p1 = input.read();
		int p2 = input.read();
		int p3 = input.read();
		int p4 = input.read();
		int flag = p1 | p2 | p3 | p4;
		if (flag < 0) {
			// No more data in the file (unexpected EOF)
			throw new EOFException();

		} else if (flag == 0) {
			// End of complete log indicator.
			isComplete = true;
			if (input.read() >= 0) {
				// Something is wrong. More data after the log complete sign.

				// What should we do here if more data was found???
				System.err.println("LogReader: "
						+ "unexpected data after log complete data");
			}
			close();

		} else {
			int len = (p1 << 24) + (p2 << 16) + (p3 << 8) + p4;
			if (len > maxBufferSize) {
				throw new IOException("too large data block: " + len);
			} else if (buffer.length <= len) {
				buffer = new byte[len + 1024];
			}
			input.readFully(buffer, 0, len);
			reader.setMessage(buffer, 0, len);
			// if (len > 512000) {
			// try {
			// if (reader.nextNode(false)) {
			// System.out.println("======================================================================");
			// System.out.println("LARGE NODE (" + len + "): " +
			// reader.getNodeName());
			// reader.printMessage();

			// System.out.println("======================================================================");
			// } else
			// System.err.println("LARGE NODE (" + len + ") (BUT NO NODE)");
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// reader.setMessage(buffer, 0, len);
			// }
			dataRead = true;
		}
	}

	static void generateXML(InputStream input) throws IOException,
			ParseException {
		generateXML(input, true);
	}

	static void generateXML(InputStream input, boolean showChunkSeparator)
			throws IOException, ParseException {
		LogReader reader = new LogReader(input, false);
		try {
			System.out.println("<simulationLog>");
			while (reader.hasMoreChunks()) {
				BinaryTransportReader r = (BinaryTransportReader) reader
						.nextChunk();
				if (showChunkSeparator) {
					System.out
							.println("<!-- - - - - - - - - - - - - - - - - - - - -->");
				}
				r.printMessage();
			}
			System.out.println("</simulationLog>");
		} finally {
			reader.close();
		}
	}

	// -------------------------------------------------------------------
	// Simple main that dumps a specified log file as XML to standard out
	// -------------------------------------------------------------------

	public static void main(String[] args) throws IOException, ParseException {
		if (args.length != 1) {
			System.out.println("Usage: LogReader file");
			System.exit(1);
		}

		InputStream in = new FileInputStream(args[0]);
		if (args[0].endsWith(".gz")) {
			in = new GZIPInputStream(in);
		}
		generateXML(in);
	}

} // LogReader
