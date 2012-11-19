/*
 * AbstractTransportableEntry.java
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

import se.sics.isl.transport.Transportable;
import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;

import java.text.ParseException;

/**
 * This class provides a skeletal implementation of the
 * {@link AbstractKeyedEntry} abstract class, where the key is a
 * {@link Transportable} object.
 * @param <T> key type.
 * @author Patrick Jordan
 */
public abstract class AbstractTransportableEntry<T extends Transportable>
        extends AbstractKeyedEntry<T> {

    /**
     * Reads in the key from the {@link TransportReader reader}.
     *
     * @param reader the reader to read the data in.
     * @throws ParseException if a parse exception occurs when reading in the key.
     */
    protected final void readKey(final TransportReader reader) throws ParseException {
        if (reader.nextNode(keyNodeName(), false)) {
            setKey((T) reader.readTransportable());
        }
    }

    /**
     * Writes the key out to the {@link TransportWriter writer}.
     *
     * @param writer the writer to write the data out to.
     */
    protected final void writeKey(final TransportWriter writer) {
        if (getKey() != null) {
            writer.write(getKey());
        }
    }

    /**
     * Returns the transport name of the key node for externalization.
     *
     * @return the transport name of the key node.
     */
    protected abstract String keyNodeName();
}
