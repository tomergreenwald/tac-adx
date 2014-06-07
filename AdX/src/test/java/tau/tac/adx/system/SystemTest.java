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

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import se.sics.tasim.logtool.LogReader;
import tau.tac.adx.parser.LogVerifierParser;
import tau.tac.adx.parser.LogVerifierParser.AdvertiserData;

/**
 * @author Tomer Greenwald
 */
public class SystemTest {

	private static LogVerifierParser parser;

	@BeforeClass
	public static void runVerifier() throws FileNotFoundException, IOException,
			ParseException {
		LogReader logReader = new LogReader(
				new FileInputStream(
						"src\\test\\resources\\localhost_sim399.slg"));
		parser = new LogVerifierParser(logReader, null);
		parser.start();
		parser.stop();
	}

	// @Test
	// public void testEmptyReportEntry() throws FileNotFoundException,
	// IOException, ParseException {
	// HashMap<String, AdvertiserData> advDataMap = parser.getAdvData();
	// for(String advertiser : advDataMap.keySet()) {
	// AdvertiserData advertiserData = advDataMap.get(advertiser);
	// DayData[] daysData = advertiserData.daysData;
	// for (DayData dayData : daysData) {
	// if(dayData != null){
	// System.out.println(advertiser + " | " + dayData);
	// } else {
	// int i =0;
	// }
	// }
	// }
	// }

	@Test
	public void testQualityRating() {
		HashMap<String, AdvertiserData> advDataMap = parser.getAdvData();
		for (String advertiser : advDataMap.keySet()) {
			AdvertiserData advertiserData = advDataMap.get(advertiser);
			for (int verifiedDay = 1; verifiedDay < 60; verifiedDay++) {

				double expectedBalance = advertiserData.daysData[verifiedDay + 1].accumulatedRevenue
						- (advertiserData.daysData[verifiedDay - 1].adxAccumulatedCosts + advertiserData.daysData[verifiedDay + 1].ucsAccumulatedCosts);

				double reportedBalance = advertiserData.daysData[verifiedDay].reportedBalance;
				String message = getBasicInfoString(advertiser, verifiedDay)
						+ StringUtils.rightPad("BankStatus: ", 20)
						+ "ERROR: Balance Computation of day "
						+ verifiedDay
						+ " Diff: "
						+ (reportedBalance - expectedBalance)
						+ "  Reported : "
						+ reportedBalance
						+ " Expected: "
						+ expectedBalance
						+ "("
						+ advertiserData.daysData[verifiedDay].accumulatedRevenue
						+ " - "
						+ advertiserData.daysData[verifiedDay].adxAccumulatedCosts
						+ " - "
						+ advertiserData.daysData[verifiedDay].ucsAccumulatedCosts
						+ ")";
				Assert.assertEquals(message, expectedBalance, reportedBalance,
						0.1);
			}
		}
	}

	private String getBasicInfoString(String advertiser, int day) {
		return StringUtils.rightPad("" + day, 5) + "\t"
				+ StringUtils.rightPad(advertiser, 20) + "\t";
	}

}
