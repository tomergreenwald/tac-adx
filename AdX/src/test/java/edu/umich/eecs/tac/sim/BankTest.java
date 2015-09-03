/*
 * BankTest.java
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
package edu.umich.eecs.tac.sim;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import se.sics.isl.util.IllegalConfigurationException;

/**
 * @author Ben Cassell
 */
public class BankTest {

	private Bank bank;
	private DummyTACAASimulation dummy;

	@Before
	public void setUp() throws IllegalConfigurationException {
		dummy = new DummyTACAASimulation();
		bank = new Bank(dummy, null, 1);
	}

	@Test
	public void testAddAccount() {
		bank.addAccount("Joe's Plumbing");
		bank.deposit("Joe's Plumbing", 123.45);
		assertTrue(bank.getAccountStatus("Joe's Plumbing") == 123.45);
		assertTrue(bank.getAccountStatus("Tito's Building Supply") == 0.0);
		bank.addAccount("Joe's Plumbing");
		assertFalse(bank.getAccountStatus("Joe's Plumbing") == 0.0);
		bank.addAccount("Tito's Building Supply");
		assertTrue(bank.getAccountStatus("Tito's Building Supply") == 0.0);
	}

	@Test
	public void testDeposit() {
		bank.deposit("Maverick Enterprises", 23000000);
		assertTrue(bank.getAccountStatus("Maverick Enterprises") == 23000000);
		bank.withdraw("Maverick Enterprises", 20000000);
		assertTrue(bank.getAccountStatus("Maverick Enterprises") == 3000000);
		bank.addAccount("Change Unlimited");
		bank.deposit("Change Unlimited", 00.01);
		assertTrue(bank.getAccountStatus("Maverick Enterprises") == 3000000);
		assertTrue(bank.getAccountStatus("Change Unlimited") == 00.01);
	}

	@Test
	public void testSendBankStatusToAll() {
		bank.addAccount("Joe's Plumbing");
		bank.deposit("Joe's Plumbing", 123.45);
		bank.sendBankStatusToAll();
	}

}
