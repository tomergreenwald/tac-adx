/**
 * TAC Supply Chain Management Simulator
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001-2003 SICS AB. All rights reserved.
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
 * LogWriter
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Jan 22 13:13:08 2003
 * Updated : $Date: 2008-04-04 11:21:06 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3954 $
 */
package se.sics.tasim.sim;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.BinaryTransportWriter;
import se.sics.isl.transport.Transportable;
import se.sics.tasim.is.EventWriter;

public class LogWriter extends EventWriter {

	private static final Logger log = Logger.getLogger(LogWriter.class
			.getName());

	private static final byte[] TACT_HEADER = { (byte) 'T', (byte) 'A',
			(byte) 'C', (byte) 'T', 0, // major version
			0, // minor version
			0, 0 // reserved
	};

	private EventWriter parentWriter;
	private DataOutputStream out;
	private BinaryTransportWriter writer;
	private boolean isClosed = false;

	public LogWriter(EventWriter parentWriter) {
		this(parentWriter, null);
	}

	public LogWriter(EventWriter parentWriter, OutputStream out) {
		if (parentWriter == null) {
			throw new NullPointerException();
		}
		this.parentWriter = parentWriter;
		if (out == null) {
			// No log file available
			isClosed = true;
		} else {
			this.out = new DataOutputStream(out);
			this.writer = new BinaryTransportWriter();

			// Write the TACT header
			try {
				this.out.write(TACT_HEADER);
			} catch (Exception e) {
				isClosed = true;
				log.log(Level.SEVERE, "could not initialize log", e);
				try {
					this.out.close();
				} catch (Exception e2) {
				}
				this.out = null;
			}
		}
	}

	public boolean isClosed() {
		return isClosed;
	}

	public synchronized void close() {
		if (!isClosed) {
			try {
				commit();
				if (!isClosed) {
					// Write the ending character to indicate that this is a
					// complete log and that the file has not been truncated.
					try {
						out.writeInt(0);
					} catch (Exception e) {
						log.log(Level.SEVERE, "could not write end of log", e);
					}

				}
			} finally {
				isClosed = true;
				try {
					out.close();
				} catch (Exception e) {
				}
				this.out = null;
				writer.clear();
			}
		}
	}

	public synchronized void commit() {
		if (!isClosed) {
			writer.finish();

			int size = writer.size();
			if (size > 0) {
				try {
					out.writeInt(writer.size());
					writer.write(out);
				} catch (IOException e) {
					isClosed = true;
					log.log(Level.SEVERE, "could not write to log", e);
					try {
						this.out.close();
					} catch (Exception e2) {
					}
					this.out = null;
				}
			}
			writer.clear();
		}
	}

	public synchronized void nextTimeUnit(int timeUnit, long time) {
		parentWriter.nextTimeUnit(timeUnit);
		if (!isClosed) {
			writer.node("nextTimeUnit").attr("unit", timeUnit).attr("time",
					time).endNode("nextTimeUnit");
		}
	}

	// -------------------------------------------------------------------
	// EventWriter API
	// -------------------------------------------------------------------

	public void participant(int id, int role, String name, int participantID) {
		parentWriter.participant(id, role, name, participantID);
	}

	public synchronized void nextTimeUnit(int timeUnit) {
		parentWriter.nextTimeUnit(timeUnit);
		if (!isClosed) {
			writer.node("nextTimeUnit").attr("unit", timeUnit).endNode(
					"nextTimeUnit");
		}
	}

	public synchronized void dataUpdated(int agent, int type, int value) {
		parentWriter.dataUpdated(agent, type, value);
		if (!isClosed) {
			writer.node("intUpdated").attr("agent", agent);
			if (type != 0) {
				writer.attr("type", type);
			}
			writer.attr("value", value).endNode("intUpdated");
		}
	}

	public synchronized void dataUpdated(int agent, int type, long value) {
		parentWriter.dataUpdated(agent, type, value);
		if (!isClosed) {
			writer.node("longUpdated").attr("agent", agent);
			if (type != 0) {
				writer.attr("type", type);
			}
			writer.attr("value", value).endNode("longUpdated");
		}
	}

	public synchronized void dataUpdated(int agent, int type, float value) {
		parentWriter.dataUpdated(agent, type, value);
		if (!isClosed) {
			writer.node("floatUpdated").attr("agent", agent);
			if (type != 0) {
				writer.attr("type", type);
			}
			writer.attr("value", value).endNode("floatUpdated");
		}
	}

	public synchronized void dataUpdated(int agent, int type, double value) {
		parentWriter.dataUpdated(agent, type, value);
		if (!isClosed) {
			writer.node("doubleUpdated").attr("agent", agent);
			if (type != 0) {
				writer.attr("type", type);
			}
			writer.attr("value", value).endNode("doubleUpdated");
		}
	}

	public synchronized void dataUpdated(int agent, int type, String value) {
		parentWriter.dataUpdated(agent, type, value);
		if (!isClosed) {
			writer.node("stringUpdated").attr("agent", agent);
			if (type != 0) {
				writer.attr("type", type);
			}
			writer.attr("value", value).endNode("stringUpdated");
		}
	}

	public synchronized void dataUpdated(int agent, int type,
			Transportable value) {
		parentWriter.dataUpdated(agent, type, value);
		if (!isClosed) {
			writer.node("objectUpdated").attr("agent", agent);
			if (type != 0) {
				writer.attr("type", type);
			}
			writer.write(value);
			writer.endNode("objectUpdated");
		}
	}

	public synchronized void dataUpdated(int type, Transportable value) {
		parentWriter.dataUpdated(type, value);
		if (!isClosed) {
			writer.node("objectUpdated");
			if (type != 0) {
				writer.attr("type", type);
			}
			writer.write(value);
			writer.endNode("objectUpdated");
		}
	}

	public void interaction(int fromAgent, int toAgent, int type) {
		parentWriter.interaction(fromAgent, toAgent, type);
	}

	public void interactionWithRole(int fromAgent, int role, int type) {
		parentWriter.interactionWithRole(fromAgent, role, type);
	}

	// -------------------------------------------------------------------
	// Logging of messages and other transportables
	// Will only be logged and not sent to parent event writer
	// -------------------------------------------------------------------

	public synchronized void message(int sender, int receiver,
			Transportable content, long time) {
		if (!isClosed) {
			writer.node("message").attr("sender", sender).attr("receiver",
					receiver).attr("time", time);
			// This method will handle the node level checking for us
			writer.write(content);
			writer.endNode("message");
		}
	}

	public synchronized void messageToRole(int sender, int role,
			Transportable content, long time) {
		if (!isClosed) {
			writer.node("messageToRole").attr("sender", sender).attr("role",
					role).attr("time", time);
			// This method will handle the node level checking for us
			writer.write(content);
			writer.endNode("messageToRole");
		}
	}

	// Ignore here... only for viewewrs...
	public void intCache(int agent, int type, int[] cache) {
	}

	public synchronized LogWriter write(Transportable content) {
		if (!isClosed) {
			// This method will handle the node level checking for us
			writer.write(content);
		}
		return this;
	}

	// -------------------------------------------------------------------
	// "Manual" Transport writer API
	// MUST SYNCHRONIZED ON THIS OBJECT TO USE THIS API!!!!
	// -------------------------------------------------------------------

	// NOTE: MUST BE SYNCHRONIZED ON THIS OBJECT TO BE CALLED
	public LogWriter node(String name) {
		if (!isClosed) {
			writer.node(name);
		}
		return this;
	}

	// NOTE: MUST BE SYNCHRONIZED ON THIS OBJECT TO BE CALLED
	public LogWriter endNode(String name) {
		if (!isClosed) {
			writer.endNode(name);
		}
		return this;
	}

	// NOTE: MUST BE SYNCHRONIZED ON THIS OBJECT TO BE CALLED
	public LogWriter attr(String name, int value) {
		if (!isClosed) {
			writer.attr(name, value);
		}
		return this;
	}

	// NOTE: MUST BE SYNCHRONIZED ON THIS OBJECT TO BE CALLED
	public LogWriter attr(String name, long value) {
		if (!isClosed) {
			writer.attr(name, value);
		}
		return this;
	}

	// NOTE: MUST BE SYNCHRONIZED ON THIS OBJECT TO BE CALLED
	public LogWriter attr(String name, float value) {
		if (!isClosed) {
			writer.attr(name, value);
		}
		return this;
	}

	// NOTE: MUST BE SYNCHRONIZED ON THIS OBJECT TO BE CALLED
	public LogWriter attr(String name, double value) {
		if (!isClosed) {
			writer.attr(name, value);
		}
		return this;
	}

	// NOTE: MUST BE SYNCHRONIZED ON THIS OBJECT TO BE CALLED
	public LogWriter attr(String name, String value) {
		if (!isClosed) {
			writer.attr(name, value);
		}
		return this;
	}

} // LogWriter
