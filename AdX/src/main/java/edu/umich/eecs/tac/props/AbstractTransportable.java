/*
 * AbstractTransportable.java
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

import java.io.Serializable;
import java.text.ParseException;

/**
 * This class provides a skeletal implementation of the {@link Transportable}
 * interface by providing a locking mechanism. Inheriting classes should issue
 * {@link #lockCheck} when setting attributes.
 * <p/>
 * Inheriting classes should implement the {@link #readWithLock} and
 * {@link #writeWithLock} methods.
 *
 * @author Patrick Jordan
 */
public abstract class AbstractTransportable implements Transportable,
        Serializable {
    /**
     * The locked variable designates whether or not the transportable is
     * mutable.
     */
    private boolean locked;

    /**
     * Make the transportable immutable.
     */
    public final void lock() {
        locked = true;
    }

    /**
     * Returns whether the transportable is immutable.
     *
     * @return <code>true</code> if the transportable is locked,
     *         <code>false</code> otherwise.
     */
    protected final boolean isLocked() {
        return locked;
    }

    /**
     * Before writing an attribute value, {@link #lockCheck} should be called.
     * This method will throw an {@link IllegalStateException illegal state
     * exception} if a write is called on a locked object.
     *
     * @throws IllegalStateException throws exception if object is locked.
     */
    protected final void lockCheck() throws IllegalStateException {

        if (isLocked()) {
            throw new IllegalStateException("locked");
        }

    }

    /**
     * Reads the state for this transportable from the specified reader.
     *
     * @param reader the reader to read data from
     * @throws ParseException if a parse error occurs
     */
    public final void read(final TransportReader reader) throws ParseException {
        lockCheck();

        boolean lock = reader.getAttributeAsInt("lock", 0) > 0;

        readWithLock(reader);

        if (lock) {
            lock();
        }
    }

    /**
     * Writes the state for this transportable to the specified writer.
     *
     * @param writer the writer to write data to
     */
    public final void write(final TransportWriter writer) {

        if (isLocked()) {

            writer.attr("lock", 1);

        }

        writeWithLock(writer);
    }

    /**
     * Returns the transport name for externalization of an implementing
     * {@link AbstractTransportable} will return the {@link Class#getSimpleName
     * simple name} of the implementing class.
     *
     * @return the transport name
     */
    public final String getTransportName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Reads the state of the {@link Transportable transportable}. Implementing
     * classes should read in attributes first and then any sub-nodes.
     *
     * @param reader the reader to read data from.
     * @throws ParseException if a parse error occurs
     */
    protected abstract void readWithLock(TransportReader reader)
            throws ParseException;

    /**
     * Writes the state of the {@link Transportable transportable}. Implementing
     * classes should write out attributes first and then any sub-nodes.
     *
     * @param writer the writer to write data to.
     */
    protected abstract void writeWithLock(TransportWriter writer);
}
