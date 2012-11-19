/*
 * AbstractKeyedEntryList.java
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

import java.util.*;

/**
 * This class provides a skeletal implementation of a list containing
 * {@link edu.umich.eecs.tac.props.KeyedEntry keyed entries} with supporting
 * methods for interacting entries specified by a given key.
 *
 * @param <T> key class
 * @param <S> entry class
 * @author Patrick Jordan
 */
public abstract class AbstractKeyedEntryList<T, S extends KeyedEntry<T>>
        extends AbstractTransportableEntryListBacking<S> implements Iterable<T> {

    /**
     * Returns the index for the entry specified by the key.
     *
     * @param key the key for the entry to be found.
     * @return the index for the entry specified by the key. <code>-1</code> if the <code>key</code> is not in the list.
     */
    public final int indexForEntry(final T key) {

        for (int i = 0; i < size(); i++) {

            if (getEntry(i).getKey().equals(key)) {

                return i;

            }

        }

        return -1;
    }

    /**
     * Returns an iterator over the keys in the list.
     *
     * @return an iterator over the keys in the list.
     */
    public final Iterator<T> iterator() {
        return new KeyIterator<T>(getEntries().iterator());
    }

    /**
     * Returns <code>true</code> if the key is in the list and
     * <code>false</code> otherwise.
     *
     * @param key the key to check for containment.
     * @return <code>true</code> if the key is in the list and <code>false</code> otherwise.
     */
    public final boolean containsKey(final T key) {
        return indexForEntry(key) > -1;
    }

    /**
     * Adds a new key to the list. The {@link #createEntry} method creates the
     * new entry with the specified key.
     *
     * @param key the key used to add the new entry.
     * @return the index of the newly generated entry.
     * @throws NullPointerException if the <code>key</code> is <code>null</code>.
     */
    protected final int addKey(final T key) throws NullPointerException {

        if (key == null) {
            throw new NullPointerException("Key cannot be null");
        }

        return addEntry(createEntry(key));
    }

    /**
     * Creates a new entry with the given key.
     *
     * @param key the key for the created entry.
     * @return the created entry with the given key.
     */
    protected abstract S createEntry(T key);

    /**
     * Returns the set of keys for the entries. A new set is created each time the method is called.
     *
     * @return the set of keys for the entries.
     */
    public final Set<T> keys() {
        Set<T> keys = new HashSet<T>();

        for (int i = 0; i < size(); i++) {
            keys.add(getEntry(i).getKey());
        }

        return keys;
    }

    /**
     * Returns the key for the entry at the <code>index</code>.
     *
     * @param index the index for the entry.
     * @return the key for the entry at the <code>index</code>.
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size()).
     */
    protected final T getKey(final int index) throws IndexOutOfBoundsException {
        return getEntry(index).getKey();
    }

    /**
     * Returns the entry with the specified key.
     *
     * @param key the key used to identify the entry.
     * @return the entry with the specified key or <code>null</code> if the key is not found.
     */
    protected final S getEntry(final T key) {
        int index = indexForEntry(key);

        if (index < 0) {
            return null;
        } else {
            return getEntry(index);
        }
    }
}
