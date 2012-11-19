/*
 * QueryType.java
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

/**
 * This enumeration defined the possible query types.
 * <ul>
 * <li><code>FOCUS_LEVEL_ZERO</code> - specifies neither the manufacturer nor
 * the component</li>
 * <li><code>FOCUS_LEVEL_ONE</code> - specifies either the manufacturer or the
 * component, but not both</li>
 * <li><code>FOCUS_LEVEL_TWO</code> - specifies both the manufacturer or the
 * component</li>
 * </ul>
 *
 * @author Patrick Jordan
 */
public enum QueryType {
    /**
     * Focus level whose queries specifies neither the manufacturer nor the component.
     */
    FOCUS_LEVEL_ZERO,

    /**
     * Focus level whose queries specifies either the manufacturer or the component, but not both.
     */
    FOCUS_LEVEL_ONE,

    /**
     * Focus level whose queries specifies specifies both the manufacturer or the component.
     */
    FOCUS_LEVEL_TWO;

    /**
     * Returns the query type of the given query.
     *
     * @param query the query whose type is to be determined.
     * @return the query type of the given query.
     */
    public static QueryType value(final Query query) {
        int components = 0;

        if (query.getManufacturer() != null) {
            components++;
        }

        if (query.getComponent() != null) {
            components++;
        }

        return values()[components];
    }
}
