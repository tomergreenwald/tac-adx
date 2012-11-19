/**
 * SICS ISL Java Utilities
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
 * Transportable
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Oct 08 15:58:16 2002
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.transport;

import java.text.ParseException;

public interface Transportable {

	/**
	 * Returns the transport name used for externalization.
	 */
	public String getTransportName();

	/**
	 * Reads the state for this transportable from the specified reader.
	 * 
	 * @param reader
	 *            the reader to read data from
	 * @throws ParseException
	 *             if a parse error occurs
	 */
	public void read(TransportReader reader) throws ParseException;

	/**
	 * Writes the state for this transportable to the specified writer.
	 * 
	 * @param writer
	 *            the writer to write data to
	 */
	public void write(TransportWriter writer);

} // Transportable
