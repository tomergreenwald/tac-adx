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
 * AdminContent
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Wed Feb 12 17:01:32 2003
 * Updated : $Date: 2008-04-04 21:07:49 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3982 $
 */
package se.sics.tasim.props;

import java.text.ParseException;

import se.sics.isl.transport.Context;
import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;

/**
 * <code>AdminContent</code> is used for administrative communication with the
 * server.
 * 
 * A few examples of its usage:
 * <ul>
 * <li>logging in
 * <li>retrieving server time
 * <li>checking for coming games
 * <li>joining games
 * </ul>
 * 
 * <p>
 * <b>Warning:</b> serialized objects of this class might not be compatible with
 * future versions. Only use serialization of this class for temporary storage
 * or RMI using the same version of the class.
 */
public class AdminContent extends SimpleContent {

	private static final long serialVersionUID = -6827270443299040674L;

	public static Context createContext() {
		Context context = new Context("admincontext");
		context.addTransportable(new AdminContent());
		return context;
	}

	/** Administration types */
	public static final int NONE = 0;
	public static final int ERROR = 1;
	public static final int PING = 2;
	public static final int PONG = 3;
	public static final int AUTH = 4;
	public static final int SERVER_TIME = 5;
	public static final int NEXT_SIMULATION = 6;
	public static final int JOIN_SIMULATION = 7;
	public static final int QUIT = 8;

	private static final String[] TYPE_NAMES = { "<none>", "error", "ping",
			"pong", "auth", "server time", "next simulation",
			"join simulation", "quit" };

	public static String getTypeAsString(int type) {
		return (type >= 0 && type < TYPE_NAMES.length) ? TYPE_NAMES[type]
				: Integer.toString(type);
	}

	/** Error types */
	public static final int NO_ERROR = 0;
	public static final int NOT_SUPPORTED = 1;
	public static final int NOT_AUTH = 2;
	public static final int NO_SIMULATION_CREATED = 3;

	private static final String[] ERROR_NAMES = { "no error", "not supported",
			"not authenticated", "no simulation created" };

	public static String getErrorAsString(int errorType) {
		return (errorType >= 0 && errorType < ERROR_NAMES.length) ? ERROR_NAMES[errorType]
				: Integer.toString(errorType);
	}

	private int type = NONE;
	private int error = NO_ERROR;
	private String errorReason = null;

	public AdminContent() {
	}

	public AdminContent(int type) {
		this(type, NO_ERROR, null);
	}

	public AdminContent(int type, int error) {
		this(type, error, null);
	}

	public AdminContent(int type, int error, String errorReason) {
		this.type = type;
		this.error = error;
		this.errorReason = errorReason;
	}

	public int getType() {
		return type;
	}

	public boolean isError() {
		return error != NO_ERROR;
	}

	public int getError() {
		return error;
	}

	public String getErrorReason() {
		return errorReason;
	}

	public void setError(int error) {
		setError(error, null);
	}

	public void setError(int error, String errorReason) {
		if (isLocked()) {
			throw new IllegalStateException("locked");
		}
		this.error = error;
		this.errorReason = errorReason;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer().append(getTransportName())
				.append('[').append(getTypeAsString(type));
		if (isError()) {
			buf.append(',').append(getErrorAsString(error));
			if (errorReason != null) {
				buf.append(',').append(errorReason);
			}
		}
		buf.append(',');
		return params(buf).append(']').toString();
	}

	/*****************************************************************************
	 * Transportable (externalization support)
	 ****************************************************************************/

	/**
	 * Returns the transport name used for externalization.
	 */
	public String getTransportName() {
		return "adminContent";
	}

	public void read(TransportReader reader) throws ParseException {
		if (isLocked()) {
			throw new IllegalStateException("locked");
		}
		type = reader.getAttributeAsInt("type");
		error = reader.getAttributeAsInt("error", NO_ERROR);
		if (error != NO_ERROR) {
			errorReason = reader.getAttribute("reason", null);
		}
		super.read(reader);
	}

	public void write(TransportWriter writer) {
		writer.attr("type", type);
		if (error != NO_ERROR) {
			writer.attr("error", error);
			if (errorReason != null) {
				writer.attr("reason", errorReason);
			}
		}
		super.write(writer);
	}

} // AdminContent
