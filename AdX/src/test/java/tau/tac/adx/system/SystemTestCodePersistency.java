/*
 * SalesReportTest.java
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
package tau.tac.adx.system;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import se.sics.tasim.logtool.LogReader;
import tau.tac.adx.demand.CampaignStats;
import tau.tac.adx.parser.LogVerifierParser;
import tau.tac.adx.parser.LogVerifierParser.AdvertiserData;

/**
 * @author Tomer Greenwald
 * @author Mariano Schain
 */
@RunWith(Parameterized.class)
public class SystemTestCodePersistency {

	/**
	 * {@link LogVerifierParser} instance to be used for verification.
	 */
	private static LogVerifierParser parser;

	/**
	 * Day number for the parameterized tests.
	 */
	private int day;
	/**
	 * Advertiser name for the parameterized tests.
	 */
	private String advertiser;

	public SystemTestCodePersistency(int day, String advertiser) {
		this.day = day;
		this.advertiser = advertiser;
	}

	@Parameters(name = "day #{0}, {1}")
	public static Iterable<Object[]> data() throws FileNotFoundException,
			IOException, ParseException {
		LogReader logReader = new LogReader(new FileInputStream(
				"src\\test\\resources\\localhost_sim399.slg"));
		parser = new LogVerifierParser(logReader, null);
		parser.start();
		parser.stop();
		List<Object[]> data = new LinkedList<Object[]>();
		HashMap<String, AdvertiserData> advDataMap = parser.getAdvData();
		for (int verifiedDay = 0; verifiedDay <= 60; verifiedDay++) {
			for (String advertiser : advDataMap.keySet()) {
				data.add(new Object[] { verifiedDay, advertiser });
			}
		}
		return data;
	}

	@Test
	public void testQualityRating() {
		Assume.assumeTrue("Only days 1-59 are validated", day > 0 && day < 59);
		HashMap<String, AdvertiserData> advDataMap = parser.getAdvData();
		AdvertiserData advertiserData = advDataMap.get(advertiser);
		double expectedBalance = advertiserData.daysData[day + 1].accumulatedRevenue
				- (advertiserData.daysData[day - 1].adxAccumulatedCosts + advertiserData.daysData[day + 1].ucsAccumulatedCosts);

		double reportedBalance = advertiserData.daysData[day].reportedBalance;
		String message = qualityRatingErrorMessage(advertiser, advertiserData,
				day, expectedBalance, reportedBalance);
		Assert.assertEquals(message, expectedBalance, reportedBalance, 0.1);
	}

	private String qualityRatingErrorMessage(String advertiser,
			AdvertiserData advertiserData, int verifiedDay,
			double expectedBalance, double reportedBalance) {
		return getBasicInfoString() + StringUtils.rightPad("BankStatus: ", 20)
				+ "ERROR: Balance Computation of day " + verifiedDay
				+ " Diff: " + (reportedBalance - expectedBalance)
				+ "  Reported : " + reportedBalance + " Expected: "
				+ expectedBalance + "("
				+ advertiserData.daysData[verifiedDay].accumulatedRevenue
				+ " - "
				+ advertiserData.daysData[verifiedDay].adxAccumulatedCosts
				+ " - "
				+ advertiserData.daysData[verifiedDay].ucsAccumulatedCosts
				+ ")";
	}

	@Test
	public void testDailyLimitCost() {
		HashMap<String, AdvertiserData> advDataMap = parser.getAdvData();
		AdvertiserData advertiserData = advDataMap.get(advertiser);
		for (Integer campaignId : advertiserData.daysData[day].cmpBudgetDailyLimits
				.keySet()) {
			HashMap<Integer, CampaignStats> cmpDailyStats = advertiserData.daysData[parser.cmpStartDayById
					.get(campaignId)].cmpDailyStats;
			int campaignStartDay = parser.cmpStartDayById.get(campaignId);
			if (!cmpDailyStats.isEmpty() && day >= campaignStartDay) {
				Double dailyLimit = advertiserData.daysData[day].cmpBudgetDailyLimits
						.get(campaignId);

				double cost;
				// since the daily stats aggregate results we need to
				// subtract two consecutive days to get the result for a
				// single day.
				if (day > campaignStartDay) {
					cost = cmpDailyStats.get(day).getCost()
							- cmpDailyStats.get(day - 1).getCost();
				} else {
					cost = cmpDailyStats.get(day).getCost();
				}

				if (cost > dailyLimit) {
					String message = dailyLimitErrorMessage(advertiser, day,
							campaignId, dailyLimit, cost);
					Assert.assertEquals(message, dailyLimit, cost, 0.1);
				}
			}
		}
	}

	private String dailyLimitErrorMessage(String advertiser, int verifiedDay,
			Integer campaignId, Double dailyLimit, double cost) {
		return getBasicInfoString()
				+ StringUtils.rightPad("CampaignLimit: ", 20)
				+ "ERROR: impressions cost over the daily limit. Cmapaign #"
				+ campaignId + " cost: " + cost + " limit: " + dailyLimit;
	}

	@Test
	public void testAdNetVsCampaignReportCosts() {
		AdvertiserData advertiserData = parser.getAdvData().get(advertiser);
		Assume.assumeTrue("Only days with active campaigns are validated",
				(advertiserData.daysData[day].cmpWon)
						&& (day <= advertiserData.daysData[day].cmpDayEnd));
		double adxCost = advertiserData.daysData[day].cmpAdxCost;
		double adnetCost = advertiserData.daysData[day].adxAdnetReportedCosts;
		String message = reportCostComparisonMessage(advertiserData);
		Assert.assertEquals(message, adxCost, adnetCost, 0.03);
	}

	private String reportCostComparisonMessage(AdvertiserData advertiserData) {
		return getBasicInfoString()
				+ StringUtils.rightPad("AdNetworkReport: ", 20)
				+ "ERROR: Cost Computation AdnetReport vs CmpReport - "
				+ advertiserData.daysData[day].cmpId + " Reported cmp: "
				+ advertiserData.daysData[day].cmpAdxCost + " AdNet: "
				+ advertiserData.daysData[day].adxAdnetReportedCosts;
	}

	private String getBasicInfoString() {
		return StringUtils.rightPad("" + day, 5) + "\t"
				+ StringUtils.rightPad(advertiser, 20) + "\t";
	}

}
