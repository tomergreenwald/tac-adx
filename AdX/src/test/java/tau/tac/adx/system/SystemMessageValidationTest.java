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
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import se.sics.tasim.logtool.LogReader;
import tau.tac.adx.parser.LogVerifierParser;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;

/**
 * @author Tomer Greenwald
 * @author Mariano Schain
 */
@RunWith(Parameterized.class)
public class SystemMessageValidationTest {

	/**
	 * {@link LogVerifierParser} instance to be used for verification.
	 */
	private static LogVerifierParser parser;

	/**
	 * Day number for the parameterized tests.
	 */
	private int day;

	public SystemMessageValidationTest(int day) {
		this.day = day;
	}

	@Parameters(name = "day #{0}")
	public static Iterable<Object[]> data() throws FileNotFoundException,
			IOException, ParseException {
		LogReader logReader = new LogReader(new FileInputStream(
				"src/test/resources/localhost_sim10.slg"));
		parser = new LogVerifierParser(logReader, null);
		parser.start();
		parser.stop();
		logReader.close();
		List<Object[]> data = new LinkedList<Object[]>();
		for (int verifiedDay = 0; verifiedDay <= 60; verifiedDay++) {
			data.add(new Object[] { verifiedDay });
		}
		return data;
	}

	@Test
	public void testCampaignOpportunityStartDay() {
		CampaignOpportunityMessage campaignOpportunityMessage = parser.campaignOpportunityMessages
				.get(day);
		// During days 0-49 a campaign should be allocated every day
		Assert.assertFalse("No campaign was allocated today", day < 49
				&& campaignOpportunityMessage == null);
		// Starting from the 49th day it is possible that a campaign won't be
		// allocated
		// since its length is larger than the remaining game days
		Assume.assumeTrue("No campaign was allocated today",
				campaignOpportunityMessage != null);
		String message = "Expected campaign #"
				+ campaignOpportunityMessage.getId()
				+ " to begin at day #"
				+ (day + 2)
				+ " (two days after the campaign oppotunity message arrived) but it actually starts at day #"
				+ campaignOpportunityMessage.getDayStart();
		Assert.assertEquals(message, day + 2,
				campaignOpportunityMessage.getDayStart());
		Assert.assertTrue(campaignOpportunityMessage.getDayEnd() < 60);
	}

}
