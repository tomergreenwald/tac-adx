/*
 * AbstractTransportableEntryListBacking.java
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
import se.sics.isl.transport.Transportable;

import java.util.*;
import java.text.ParseException;

/**
 * This class provides a skeletal implementation of a list of transportables.
 * The class if backed by an {@link ArrayList}.
 *
 * @param <S> the transportable class this object is backing.
 *
 * @author Patrick Jordan
 */
public abstract class AbstractTransportableEntryListBacking<S extends Transportable> extends AbstractTransportable {
    /**
     * The list holding the entries.
     */
    private List<S> entries;

    /**
     * Sole constructor. (For invocation by subclass constructors, typically implicit.)
     * <p/>
     * Constructs an empty backing list with an initial capacity of ten.
     */
    protected AbstractTransportableEntryListBacking() {
        this.entries = new ArrayList<S>();
    }

    /**
     * Returns the backing entry list.
     * @return the backing entry list.
     */
    protected final List<S> getEntries() {
        return entries;
    }

    /**
     * Return the number of entries in the list.
     *
     * @return the number of entries in the list.
     */
    public final int size() {
        return entries.size();
    }

    /**
     * Appends the builder before the entries are written to the builder.
     *
     * @param builder the builder to append for the {@link #toString()}
     */
    protected void toStringBeforeEntries(final StringBuilder builder) {
    }

    /**
     * Returns a string that lists in the form (SimpleName [before] (entry1) ... (entryN) [after]), where
     * (entry1) is the contents of the toString method for the entry.
     *
     * @return a string created from appending the toString method in the entries.
     */
    public final String toString() {
        StringBuilder builder = new StringBuilder("(");
        builder.append(this.getClass().getSimpleName());
        toStringBeforeEntries(builder);
        for (S entry : entries) {
            builder.append(' ').append(entry);
        }
        toStringAfterEntries(builder);
        builder.append(')');

        return builder.toString();
    }

    /**
     * Appends the builder after the entries are written to the builder.
     *
     * @param builder the builder to append for the {@link #toString()}
     */
    protected void toStringAfterEntries(final StringBuilder builder) {
    }

    /**
     * Reads from the reader before the entry nodes have been read.
     *
     * @param reader the reader that is read from.
     *
     * @throws ParseException if a parse exception occurs before reading in the entries.
     */
    protected void readBeforeEntries(final TransportReader reader) throws ParseException {
    }

    /**
     * Reads from the reader after the entry nodes have been read.
     *
     * @param reader the reader that is read from.
     *
     * @throws ParseException if a parse exception occurs after reading in the entries.
     */
    protected void readAfterEntries(final TransportReader reader) throws ParseException {
    }

    /**
     * Reads a list of entry transportables from the reader.
     *
     * @param reader the reader to read the data in.
     * @throws ParseException if a parse exception occurs when reading in the entries.
     */
    protected final void readWithLock(final TransportReader reader) throws ParseException {
        readBeforeEntries(reader);

        while (reader.nextNode(entryClass().getSimpleName(), false)) {
            addEntry((S) reader.readTransportable());
        }

        readAfterEntries(reader);
    }

    /**
     * Writes a list of entry transportables to the writer.
     *
     * @param writer the writer to write the data out to.
     */
    protected final void writeWithLock(final TransportWriter writer) {
        writeBeforeEntries(writer);

        for (S reportEntry : entries) {
            writer.write(reportEntry);
        }

        writeAfterEntries(writer);
    }

    /**
     * Writes to the writer after the entry nodes have been written.  Implementing classes should not
     * write attributes without enclosing them in nodes.
     *
     * @param writer the writer that is written to.
     */
    protected void writeAfterEntries(final TransportWriter writer) {
    }

    /**
     * Writes to the writer before the entry nodes have been written.
     *
     * @param writer the writer that is written to.
     */
    protected void writeBeforeEntries(final TransportWriter writer) {
    }

    /**
     * Returns the entry at the specified index in this list.
     *
     * @param index the index of entry to return
     * @return the entry at the specified index.
     * @throws IndexOutOfBoundsException if the index is out of range <code>(index < 0 || index >= size())</code>.
     */
    protected final S getEntry(final int index) throws IndexOutOfBoundsException {
        return entries.get(index);
    }

    /**
     * Removes the entry at the specified index in this list. Shifts any
     * subsequent entries to the left (subtracts one from their indices).
     * Returns the element that was removed from the list.
     *
     * @param index the index of the element to removed.
     * @throws IllegalStateException if the object is locked
     */
    protected final void removeEntry(final int index) throws IllegalStateException {
        beforeRemoveEntry(index);
        lockCheck();

        entries.remove(index);
        afterRemoveEntry(index);
    }

    /**
     * Callback that is invoked before the entry is removed from the backing list in the {@link #removeEntry(int)} call.
     * @param index the index to remove.
     */
    protected void beforeRemoveEntry(final int index) {
    }

    /**
     * Callback that is invoked after the entry is removed from the backing list in the {@link #removeEntry(int)} call.
     * @param index the index to remove.
     */
    protected void afterRemoveEntry(final int index) {
    }

    /**
     * Appends the specified entry to the end of this list.
     *
     * @param entry the entry to be appended to the list
     * @return the index of the new entry. <code>-1</code> if the entry was not
     *         added.
     * @throws IllegalStateException if the object is locked
     */
    protected final int addEntry(final S entry) throws IllegalStateException {
        beforeAddEntry(entry);

        lockCheck();

        // This will always return true for an ArrayList
        entries.add(entry);

        afterAddEntry(entry);

        return size() - 1;
    }

    /**
     * Callback that is invoked before the entry is added to the backing list in the {@link #removeEntry(int)} call.
     *
     * @param entry the entry to be added.
     *
     * @throws IllegalStateException if the method is modifying state
     */
    protected void beforeAddEntry(final S entry) throws IllegalStateException {
    }

    /**
     * Callback that is invoked before the entry is added to the backing list in the {@link #removeEntry(int)} call.
     *
     * @param entry the entry to be added.
     *
     * @throws IllegalStateException if the method is modifying state
     */
    protected void afterAddEntry(final S entry) throws IllegalStateException {
    }

    /**
     * Returns class of the entries. The {@link Class#getSimpleName()} simple
     * name} of the class will determine how the entries are read in by the
     * {@link TransportReader}. Implementing classes should return the class of
     * the generic parameter <code>T</code>.
     *
     * @return the class of the entries.
     */
    protected abstract Class entryClass();
}
