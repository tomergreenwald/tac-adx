/*
 * Auctioneer.java
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


import java.util.logging.Logger;

import se.sics.tasim.is.SimulationInfo;

import com.botbox.util.ArrayUtils;

import edu.umich.eecs.tac.props.BankStatus;

/**
 * @author Lee Callender, Ben Cassell
 */
public class Bank {
//Commented out bankStatus[]; so long as bankStatus only contains amounts,
//this array is doing nothing.  If in the future this becomes more like SCM, i.e.
//there are interests and penalties, then this array becomes meaningful

	private BankStatusSender bankStatusSender;
	private String[] accountNames;
	private double[] accountAmounts;
	// private BankStatus[] bankStatus;
	private int accountNumber; // number of accounts
	private SimulationInfo simulationInfo;

	public Bank(BankStatusSender bankStatusSender,
			SimulationInfo simulationInfo, int accountNumber) {
		this.bankStatusSender = bankStatusSender;
		accountNames = new String[accountNumber];
		accountAmounts = new double[accountNumber];
		this.simulationInfo = simulationInfo;
	}

	public void addAccount(String name) {
		int index = ArrayUtils.indexOf(accountNames, 0, accountNumber, name);
		if (index < 0) {
			doAddAccount(name);
		}
	}

	private synchronized int doAddAccount(String name) {
		if (accountNumber == accountNames.length) {
			int newSize = accountNumber + 8;
			accountNames = (String[]) ArrayUtils.setSize(accountNames, newSize);
			accountAmounts = ArrayUtils.setSize(accountAmounts, newSize);
			// bankStatus = (BankStatus[]) ArrayUtils.setSize(bankStatus,
			// newSize);
		}
		accountNames[accountNumber] = name;
		accountAmounts[accountNumber] = 0.0d;
		return accountNumber++;
	}

	public double getAccountStatus(String name) {
		int index = ArrayUtils.indexOf(accountNames, 0, accountNumber, name);
		return index >= 0 ? accountAmounts[index] : 0.0d;
	}

	public double deposit(String name, double amount) {
		int index = ArrayUtils.indexOf(accountNames, 0, accountNumber, name);
		if (index < 0) {
			index = doAddAccount(name);
		}
		accountAmounts[index] += amount;
		return accountAmounts[index];
	}

	public double withdraw(String name, double amount) {
		return deposit(name, -amount);
	}

	public void sendBankStatusToAll() {
		for (int i = 0; i < accountNumber; i++) {
			BankStatus status = new BankStatus();
			// BankStatus status = bankStatus[i];
			// if (status == null) {
			// status = new BankStatus();
			// } else {
			// // Can not simply reset the bank status after sending it
			// // because the message might be in a send queue or used in an
			// // internal agent. Only option is to simply forget about it
			// // and create a new bank status for the agent the next day.
			// bankStatus[i] = null;
			// }
			status.setAccountBalance(accountAmounts[i]);
			bankStatusSender.sendBankStatus(accountNames[i], status);
		}
	}

	// DEBUG FINALIZE REMOVE THIS!!!
	protected void finalize() throws Throwable {
		Logger.global.info("BANK FOR SIMULATION "
				+ simulationInfo.getSimulationID() + " IS BEING GARBAGED");
		super.finalize();
	}

} // Bank
