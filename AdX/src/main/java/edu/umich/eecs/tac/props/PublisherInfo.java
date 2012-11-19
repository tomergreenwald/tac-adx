/*
 * PublisherInfo.java
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
 * This class contains the publisher information released to the advertisers at the beginning of the game.
 *
 * @author Patrick Jordan
 */
public class PublisherInfo extends AbstractTransportable {
    /**
     * The squashing parameter used in the auctions.
     */
    private double squashingParameter;

    /**
     * Returns the squashing parameter value used in the simulation.
     * @return the squashing parameter value used in the simulation.
     */
    public final double getSquashingParameter() {
        return squashingParameter;
    }

    /**
     * Sets the squashing parameter value used in the simulation.
     *
     * @param squashingParameter the squashing parameter value used in the simulation.
     */
    public final void setSquashingParameter(final double squashingParameter) {
        lockCheck();
        this.squashingParameter = squashingParameter;
    }

    /**
     * Reads the squashing parameter value from the reader.
     * @param reader the reader to read data from.
     *
     * @throws ParseException if an exception is thrown when reading the attribute
     */
    @Override
    protected final void readWithLock(final TransportReader reader) throws ParseException {
        squashingParameter = reader.getAttributeAsDouble("squashingParameter", 0.0);
    }

    /**
     * Writes the squashing parameter value to the writer.
     * @param writer the writer to write data to.
     */
    @Override
    protected final void writeWithLock(final TransportWriter writer) {
        writer.attr("squashingParameter", squashingParameter);
    }
}
