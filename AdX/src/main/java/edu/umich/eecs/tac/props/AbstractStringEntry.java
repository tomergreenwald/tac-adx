/*
 * AbstractStringEntry.java
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
 * This class provides a skeletal implementation of the
 * {@link AbstractKeyedEntry} abstract class, where the key is a
 * {@link se.sics.isl.transport.Transportable Transportable} object.
 *
 * @author Patrick Jordan
 */
public abstract class AbstractStringEntry extends AbstractKeyedEntry<String> {
    /**
     * The key node name used for reading and writing as a transport.
     */
    private static final String KEY_NODE = "AbstractStringEntryKeyNode";

    /**
     * The key attribue used for reading and writing the key as an attribute.
     */
    private static final String KEY_ATTRIBUTE = "AbstractStringEntryKey";

    /**
     * Reads in a "key" node and sets the key to the value of the backing
     * attribute.
     *
     * @param reader the reader to read data from.
     * @throws ParseException if exeption occurs reading the key node and attribute.
     */
    protected final void readKey(final TransportReader reader) throws ParseException {

        // Read in the key node. The node must exist.
        reader.nextNode(KEY_NODE, true);

        // Grab the key attribute value
        setKey(reader.getAttribute(KEY_ATTRIBUTE, null));
    }

    /**
     * Creates and writes a "key" node and sets the backing attribute value to
     * the key.
     *
     * @param writer the writer to write data to.
     */
    protected final void writeKey(final TransportWriter writer) {

        // Create a "key" node.
        writer.node(KEY_NODE);

        // Write the key if it is non-null
        if (getKey() != null) {

            // Write the non-null key atribute
            writer.attr(KEY_ATTRIBUTE, getKey());

        }

        // Close the key node.
        writer.endNode(KEY_NODE);
    }
}
