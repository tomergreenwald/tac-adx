/*
 * BankStatus.java
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

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;

/**
 * The bank status class holds an agent's account balance at a bank.
 * @author Ben Cassell, Lee Callender
 */
public class BankStatus extends AbstractTransportable {

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = -6576269032652384128L;

    /**
     * The account balance.
     */
    private double balance;

    /**
     * Create a new bank status object with a balance of zero.
     */
    public BankStatus() {
        balance = 0.0;
    }

    /**
     * Create a new bank status object with the supplied balance.
     *
     * @param b the balance
     */
    public BankStatus(final double b) {
        balance = b;
    }

    /**
     * Returns the account balance.
     * @return the account balance.
     */
    public final double getAccountBalance() {
        return balance;
    }

    /**
     * Sets the account balance.
     * @param b the account balance.
     */
    public final void setAccountBalance(final double b) {
        lockCheck();
        balance = b;
    }

    /**
     * Creates a string with the account balance.
     * @return a string with the account balance.
     */
    @Override
    public final String toString() {
        return String.format("%s[%f]", getTransportName(), balance);
    }

    /**
     * Read the balance parameter.
     * @param reader the reader to read from
     * @throws ParseException if a parse exception occurs reading the balance.
     */
    @Override
    protected final void readWithLock(final TransportReader reader) throws ParseException {
        balance = reader.getAttributeAsDouble("balance", 0.0);
    }

    /**
     * Write the balance parameter.
     * @param writer the writer to write to.
     */
    @Override
    protected final void writeWithLock(final TransportWriter writer) {
        writer.attr("balance", balance);
    }

}
