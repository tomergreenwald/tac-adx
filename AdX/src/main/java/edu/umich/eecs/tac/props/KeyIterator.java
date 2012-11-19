/*
 * KeyIterator.java
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

import java.util.Iterator;

/**
 * Key iterator provides an iteration method that wraps a keyed entry iterator and returns the keys of those entries.
 *
 * @param <T> the key class
 *
 * @author Patrick Jordan
 */
public class KeyIterator<T> implements Iterator<T> {
    /**
     * The delegatee.
     */
    private Iterator< ? extends KeyedEntry< ? extends T>> delegateIterator;

    /**
     * Create a new key iterator that delegates to the supplied iterator.
     *
     * @param delegateIterator a new key iterator that delegates to the supplied iterator.
     */
    public KeyIterator(final Iterator< ? extends KeyedEntry< ? extends T>> delegateIterator) {

        if (delegateIterator == null) {
            throw new NullPointerException("delegate iterator cannot be null");
        }

        this.delegateIterator = delegateIterator;
    }

    /**
     * Returns whether another key is available.  This will return <code>true</code> if the delegated iterator has
     * another entry.
     *
     * @return whether another key is available.
     */
    public final boolean hasNext() {
        return delegateIterator.hasNext();
    }

    /**
     * Returns tne next key.
     * @return tne next key.
     */
    public final T next() {
        return delegateIterator.next().getKey();
    }

    /**
     * Throws an {@link UnsupportedOperationException} if called.
     *
     * @throws UnsupportedOperationException if the method is invoked.
     */
    public final void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "remove is not supported in this iterator");
    }
}
