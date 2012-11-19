/*
 * AbstractKeyedEntry.java
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
package edu.umich.eecs.tac.props;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;

import java.text.ParseException;

/**
 * This class provides a skeletal implementation of the {@link KeyedEntry}
 * interface.
 *
 * @param <T> key type
 *
 * @author Patrick Jordan
 */
public abstract class AbstractKeyedEntry<T> implements KeyedEntry<T> {
    /**
     * The key for the entry.
     */
    private T key;

    /**
     * Returns the key for the entry.
     *
     * @return the key for the entry.
     */
    public final T getKey() {
        return key;
    }

    /**
     * Sets the key for the entry.
     *
     * @param key the key for the entry.
     */
    public final void setKey(final T key) {
        this.key = key;
    }

    /**
     * Returns the {@link Class#getSimpleName() simple name} of the implementing
     * class.
     *
     * @return the {@link Class#getSimpleName() simple name} of the implementing
     *         class.
     */
    public final String getTransportName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Reads the state from the {@link TransportReader}. The {@link #readEntry}
     * method will be called first and then the {@link #readKey}.
     *
     * @param reader the reader to read the state in from.
     * @throws ParseException if a parse exception occurs
     */
    public final void read(final TransportReader reader) throws ParseException {
        readEntry(reader);

        readKey(reader);
    }

    /**
     * Writes the state to the {@link TransportWriter}. The {@link #writeEntry}
     * method will be called first and then the {@link #writeKey}.
     *
     * @param writer the writer to write the state to
     */
    public final void write(final TransportWriter writer) {
        writeEntry(writer);

        writeKey(writer);
    }

    /**
     * Reads the entry state from the {@link TransportReader}. The attributes
     * should be read in first, then the nodes.
     *
     * @param reader the reader to read the state in from.
     * @throws ParseException if a parse exception occurs
     */
    protected abstract void readEntry(TransportReader reader)
            throws ParseException;

    /**
     * Reads the entry key from the {@link TransportReader}. This method requirs
     * that the key be in node form.
     *
     * @param reader the reader to read the key in from.
     * @throws ParseException if a parse exception occurs
     */
    protected abstract void readKey(TransportReader reader)
            throws ParseException;

    /**
     * Writes the entry state to the {@link TransportWriter}. The attributes
     * should be written in first, then the nodes.
     *
     * @param writer the writer to write the entry state to
     */
    protected abstract void writeEntry(TransportWriter writer);

    /**
     * Writes the entry key to the {@link TransportWriter}. The key must be
     * written in node form.
     *
     * @param writer the writer to write the key to
     */
    protected abstract void writeKey(TransportWriter writer);
}
