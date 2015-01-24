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
import tau.tac.adx.parser.LogVerifierParser.DayData;
import tau.tac.adx.report.adn.AdNetworkKey;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.demand.CampaignReport;
import tau.tac.adx.report.demand.CampaignReportKey;

/**
 * @author Tomer Greenwald
 * @author Mariano Schain
 */
@RunWith(Parameterized.class)
public class SystemCodePersistencyTest {

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

	public SystemCodePersistencyTest(int day, String advertiser) {
		this.day = day;
		this.advertiser = advertiser;
	}

	@Parameters(name = "day #{0}, {1}")
	public static Iterable<Object[]> data() throws FileNotFoundException,
			IOException, ParseException {
		LogReader logReader = new LogReader(new FileInputStream(
				"src/test/resources/localhost_sim10.slg"));
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
	public void testBankBalance() {
		Assume.assumeTrue("Only days 1-59 are validated", day > 0 && day < 59);
		HashMap<String, AdvertiserData> advDataMap = parser.getAdvData();
		AdvertiserData advertiserData = advDataMap.get(advertiser);
		double expectedBalance = advertiserData.daysData[day + 1].accumulatedRevenue
				- (advertiserData.daysData[day - 1].adxAccumulatedCosts + advertiserData.daysData[day + 1].ucsAccumulatedCosts);

		double reportedBalance = advertiserData.daysData[day].reportedBalance;
		Assert.assertEquals(expectedBalance, reportedBalance, 0.1);
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
					Assert.assertEquals(getCampaignString(advertiserData),
							dailyLimit, cost, 0.1);
				}
			}
		}
	}

	@Test
	public void testAdNetVsCampaignReportCosts() {
		AdvertiserData advertiserData = parser.getAdvData().get(advertiser);
		Assume.assumeTrue("Only days with active campaigns are validated",
				(advertiserData.daysData[day].cmpWon)
						&& (day <= advertiserData.daysData[day].cmpDayEnd));
		double adxCost = advertiserData.daysData[day].cmpAdxCost;
		double adnetCost = advertiserData.daysData[day].adxAdnetReportedCosts;
		Assert.assertEquals(getCampaignString(advertiserData), adxCost,
				adnetCost, 0.03);
	}

	private String getCampaignString(AdvertiserData advertiserData) {
		return "Campaign #" + advertiserData.daysData[day].cmpId;
	}

	@Test
	public void testQualityRatingVsExpected() {
		AdvertiserData advertiserData = parser.getAdvData().get(advertiser);
		double actualQualityRating = advertiserData.daysData[day].qualityRating;
		double expectedQualityRating = advertiserData.daysData[day].expectedQualityRating;
		Assert.assertEquals(expectedQualityRating, actualQualityRating, 0.1);
	}

	@Test
	public void testCampaignReportOutOfRange() {
		AdvertiserData advertiserData = parser.getAdvData().get(advertiser);
		CampaignReport campaignReport = advertiserData.daysData[day].campaignReport;
		Assume.assumeTrue("No campaign report was avaialable",
				campaignReport != null);
		for (CampaignReportKey campaignReportKey : campaignReport) {
			Integer campaignId = campaignReportKey.getCampaignId();
			int campaignFirstDay = parser.cmpStartDayById.get(campaignId);
			long campaignLastDay = advertiserData.daysData[campaignFirstDay].cmpDayEnd;
			String message = "Received campaign report while campaign #"
					+ campaignId + " was not active (active range ["
					+ campaignFirstDay + ", " + campaignLastDay + "])";
			Assert.assertTrue(message, (day - 1 <= campaignLastDay)
					&& (day - 1 >= campaignFirstDay));
		}
	}

	@Test
	public void testCampaignReportForWrongAdvertiser() {
		AdvertiserData advertiserData = parser.getAdvData().get(advertiser);
		CampaignReport campaignReport = advertiserData.daysData[day].campaignReport;
		Assume.assumeTrue("No campaign report was avaialable",
				campaignReport != null);
		for (CampaignReportKey campaignReportKey : campaignReport) {
			Integer campaignId = campaignReportKey.getCampaignId();
			int campaignFirstDay = parser.cmpStartDayById.get(campaignId);
			Assert.assertTrue(
					"Received a report for a campaign that the advertiser did not own",
					advertiserData.daysData[campaignFirstDay].cmpWon);
		}
	}

	@Test
	public void testAdnetRportOutOfRange() {
		AdvertiserData advertiserData = parser.getAdvData().get(advertiser);
		AdNetworkReport adnetReport = advertiserData.daysData[day].adnetReport;
		Assume.assumeTrue("No adnet report was avaialable", adnetReport != null);
		for (AdNetworkKey adnetReportKey : adnetReport) {
			Integer campaignId = adnetReportKey.getCampaignId();
			int campaignFirstDay = parser.cmpStartDayById.get(campaignId);
			long campaignLastDay = advertiserData.daysData[campaignFirstDay].cmpDayEnd;
			String message = "Received adnet report while campaign #"
					+ campaignId + " was not active (active range ["
					+ campaignFirstDay + ", " + campaignLastDay + "])";
			Assert.assertTrue(message, (day - 1 > campaignFirstDay)
					|| (day - 1 < campaignLastDay));
		}
	}

	@Test
	public void testAdnetReportForWrongAdvertiser() {
		AdvertiserData advertiserData = parser.getAdvData().get(advertiser);
		AdNetworkReport adnetReport = advertiserData.daysData[day].adnetReport;
		Assume.assumeTrue("No adnet report was avaialable", adnetReport != null);
		for (AdNetworkKey adnetReportKey : adnetReport) {
			Integer campaignId = adnetReportKey.getCampaignId();
			int campaignFirstDay = parser.cmpStartDayById.get(campaignId);
			Assert.assertTrue(
					"Received a report for a campaign that the advertiser did not own",
					advertiserData.daysData[campaignFirstDay].cmpWon);
		}
	}

	@Test
	public void testReceivedMessages() {
		AdvertiserData advertiserData = parser.getAdvData().get(advertiser);
		DayData dayData = advertiserData.daysData[day];
		if (day == 0) {
			Assert.assertNotNull(
					"InitialCampaignMessage should be sent on the first day",
					dayData.initialCampaignMessage);
		} else {
			Assert.assertNull(
					"InitialCampaignMessage should only be sent on the first day",
					dayData.initialCampaignMessage);
		}
		if (day < 2) {
			Assert.assertNull("CampaignReport should only be sent from day 2+",
					dayData.campaignReport);
		} else {
			Assert.assertNotNull("CampaignReport should be sent from day 2+",
					dayData.campaignReport);
		}
		if (day == 0) {
			Assert.assertNull(
					"AdNetworkDailyNotification should only be sent from day 1+",
					dayData.dailyNotification);
		} else {
			Assert.assertNotNull(
					"AdNetworkDailyNotification should be sent from day 1+",
					dayData.dailyNotification);
		}
		Assert.assertNotNull("SimulationStatus should be sent every day",
				dayData.simulationStatus);
		if (day < 2) {
			Assert.assertNull(
					"AdNetworkDailyNotification should only be sent from day 1+",
					dayData.adnetReport);
		} else {
			if (dayData.adnetReport == null) {
				Assert.assertNotNull(
						"AdNetworkDailyNotification should be sent from day 1+ when at least one campaign is active.",
						dayData.campaignReport);
			}
		}
		Assert.assertNotNull(dayData.bankStatus);
	}
}
