/*
 * DefaultSalesAnalyst.java
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

import edu.umich.eecs.tac.user.UserEventListener;
import edu.umich.eecs.tac.props.SalesReport;
import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.props.Ad;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.TACAAConstants;
import com.botbox.util.ArrayUtils;

import java.util.Map;
import java.util.Arrays;

import se.sics.tasim.is.EventWriter;

/**
 * @author Patrick Jordan, Lee Callender
 */
public class DefaultSalesAnalyst implements SalesAnalyst {
	private AgentRepository agentRepository;
	private SalesReportSender salesReportSender;

	private String[] accountNames;
	private int[][] accountConversions;
	private SalesReport[] salesReports;
	private int accountNumber; // number of accounts

	public DefaultSalesAnalyst(AgentRepository agentRepository,
			SalesReportSender salesReportSender, int accountNumber) {
		if (agentRepository == null) {
			throw new NullPointerException("Agent repository cannot be null");
		}

		this.agentRepository = agentRepository;

		if (salesReportSender == null) {
			throw new NullPointerException("Sales report sender cannot be null");
		}

		this.salesReportSender = salesReportSender;

		accountNames = new String[accountNumber];
		accountConversions = new int[accountNumber][];
		salesReports = new SalesReport[accountNumber];
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
			accountConversions = (int[][]) ArrayUtils.setSize(
					accountConversions, newSize);
			salesReports = (SalesReport[]) ArrayUtils.setSize(salesReports,
					newSize);
		}
		accountNames[accountNumber] = name;
		accountConversions[accountNumber] = new int[getAdvertiserInfo().get(name).getDistributionWindow()];
        int defaultConversions = getAdvertiserInfo().get(name).getDistributionCapacity() / getAdvertiserInfo().get(name).getDistributionWindow();
        Arrays.fill(accountConversions[accountNumber], defaultConversions);  
		return accountNumber++;
	}

	public double getRecentConversions(String name) {
		int index = ArrayUtils.indexOf(accountNames, 0, accountNumber, name);

		return index >= 0 ? sum(accountConversions[index]) : 0;
	}

	private int sum(int[] array) {
		int sum = 0;
		if (array != null) {
			for (int value : array) {
				sum += value;
			}
		}
		return sum;
	}

	protected int addConversions(String name, Query query, int conversions,
			double amount) {
		int index = ArrayUtils.indexOf(accountNames, 0, accountNumber, name);
		if (index < 0) {
			index = doAddAccount(name);
		}

		if (accountConversions[index] == null) {

            AdvertiserInfo advertiserInfo = getAdvertiserInfo().get(name);

            accountConversions[index] = new int[advertiserInfo.getDistributionWindow()];

            int defaultConversions = advertiserInfo.getDistributionCapacity() / advertiserInfo.getDistributionWindow();

            Arrays.fill(accountConversions[index], defaultConversions);            
		}

		accountConversions[index][0] += conversions;

		if (salesReports[index] == null) {
			salesReports[index] = new SalesReport();
		}

		int queryIndex = salesReports[index].indexForEntry(query);
		if (queryIndex < 0) {
			queryIndex = salesReports[index].addQuery(query);
		}
		salesReports[index].addConversions(queryIndex, conversions);
		salesReports[index].addRevenue(queryIndex, amount);

		return accountConversions[index][0];
	}

	public void sendSalesReportToAll() {
		for (int i = 0; i < accountNumber; i++) {
			SalesReport report = salesReports[i];
			if (report == null) {
				report = new SalesReport();
			} else {
				// Can not simply reset the bank report after sending it
				// because the message might be in a send queue or used in an
				// internal agent. Only option is to simply forget about it
				// and create a new bank report for the agent the next day.
				salesReports[i] = null;
			}

			salesReportSender.sendSalesReport(accountNames[i], report);

			salesReportSender.broadcastConversions(accountNames[i], accountConversions[i][0]);
		}

		updateConversionQueue();
	}

	private void updateConversionQueue() {
		for (int i = 0; i < accountConversions.length; i++) {
			for (int j = accountConversions[i].length - 2; j >= 0; j--) {
				accountConversions[i][j + 1] = accountConversions[i][j];
			}
			accountConversions[i][0] = 0;
		}
	}

	public void queryIssued(Query query) {
	}

	public void viewed(Query query, Ad ad, int slot, String advertiser,
			boolean isPromoted) {
	}

	public void clicked(Query query, Ad ad, int slot, double cpc,
			String advertiser) {
	}

	public void converted(Query query, Ad ad, int slot, double salesProfit,
			String advertiser) {
		addConversions(advertiser, query, 1, salesProfit);
	}

	protected Map<String, AdvertiserInfo> getAdvertiserInfo() {
		return agentRepository.getAdvertiserInfo();
	}

	public int size() {
		return accountNumber;
	}
}
