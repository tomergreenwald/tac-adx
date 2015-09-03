/*
 * TACAAConstants.java
 *
 * COPYRIGHT  20008
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
package tau.tac.adx.sim;

import tau.tac.adx.agents.DemandAgent;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReport;
import tau.tac.adx.report.publisher.AdxPublisherReport;

import com.google.common.eventbus.EventBus;

/**
 * TACAAConstants is used to define any constants used by the TAC AA
 * Simulations.
 * 
 * @author SICS, Patrick Jordan, Lee Callender
 * @author Mariano Schain, greenwald
 */
public final class TACAdxConstants {

	/**
	 * Sole constructor (should not be invoked).
	 */
	private TACAdxConstants() {
	}

	/**
	 * System-wide {@link EventBus} name.
	 */
	public static final String ADX_EVENT_BUS_NAME = "AdX";

	/**
	 * The price range is between 0 and 100. Was chosen arbitrary.
	 */
	public static final double MAX_SIMPLE_PUBLISHER_AD_PRICE = 100;

	/**
	 * The simulation supported types.
	 */
	public static final String[] SUPPORTED_TYPES = { "tac13adx" };
	/**
	 * Human readable message.
	 */
	public static final int TYPE_NONE = 0;
	/**
	 * Human readable message.
	 */
	public static final int TYPE_MESSAGE = 1;
	/**
	 * Human readable warning.
	 */
	public static final int TYPE_WARNING = 2;
	/**
	 * The average network response time for a specific agent (int).
	 */
	public static final int DU_NETWORK_AVG_RESPONSE = 64;
	/**
	 * The last network response time for a specific agent (int).
	 */
	public static final int DU_NETWORK_LAST_RESPONSE = 65;
	/**
	 * The bank account status for a specific agent (int or long or double).
	 */
	public static final int DU_BANK_ACCOUNT = 100;
	/**
	 * The number of non-searching users.
	 */
	public static final int DU_NON_SEARCHING = 200;
	/**
	 * The number of informational-search users.
	 */
	public static final int DU_INFORMATIONAL_SEARCH = 201;
	/**
	 * The number of focus level zero users.
	 */
	public static final int DU_FOCUS_LEVEL_ZERO = 202;
	/**
	 * The number of focus level one users.
	 */
	public static final int DU_FOCUS_LEVEL_ONE = 203;
	/**
	 * The number of focus level two users.
	 */
	public static final int DU_FOCUS_LEVEL_TWO = 204;
	/**
	 * The number of transacted users.
	 */
	public static final int DU_TRANSACTED = 205;
	/**
	 * The bid of an advertiser.
	 */
	public static final int DU_BIDS = 300;
	/**
	 * The impressions an advertiser receives.
	 */
	public static final int DU_IMPRESSIONS = 301;
	/**
	 * The clicks an advertiser receives.
	 */
	public static final int DU_CLICKS = 302;
	/**
	 * The conversions an advertiser receives.
	 */
	public static final int DU_CONVERSIONS = 303;
	/**
	 * The query report for an advertiser.
	 */
	public static final int DU_QUERY_REPORT = 304;
	/**
	 * The sales report for an advertiser.
	 */
	public static final int DU_SALES_REPORT = 305;
	/**
	 * The publisher information for an advertiser.
	 */
	public static final int DU_PUBLISHER_INFO = 306;
	/**
	 * The slot information for an advertiser.
	 */
	public static final int DU_ADVERTISER_INFO = 307;
	/**
	 * @see {@link AdxPublisherReport}.
	 */
	public static final int DU_PUBLISHER_QUERY_REPORT = 400;
	/**
	 * @see {@link AdNetworkReport}.
	 */
	public static final int DU_AD_NETWORK_REPORT = 401;
	/**
	 * The bid of an advertiser.
	 */
	public static final int DU_ADX_BIDS = 402;

	/**
	 * The initial campaign allocated to an advertiser.
	 */
	public static final int DU_INITIAL_CAMPAIGN = 403;

	/**
	 * The campaign opportunity announced to the advertisers.
	 */
	public static final int DU_CAMPAIGN_OPPORTUNITY = 404;

	/**
	 * The campaign report announced to each advertiser.
	 */
	public static final int DU_CAMPAIGN_REPORT = 405;

	/**
	 * The user classification auction result announced to each advertiser.
	 */
	public static final int DU_DEMAND_DAILY_REPORT = 406;

	/**
	 * AdNet's win count.
	 */
	public static final int DU_AD_NETWORK_WIN_COUNT = 407;

	/**
	 * AdNet's quality rating.
	 */
	public static final int DU_AD_NETWORK_QUALITY_RATING = 408;

	/**
	 * AdNet's revenue.
	 */
	public static final int DU_AD_NETWORK_REVENUE = 409;

	/**
	 * AdNet's expense.
	 */
	public static final int DU_AD_NETWORK_EXPENSE = 410;

	/**
	 * AdNet's expense.
	 */
	public static final int DU_AD_NETWORK_UCS_EXPENSE = 411;

	/**
	 * AdNet's expense.
	 */
	public static final int DU_AD_NETWORK_ADX_EXPENSE = 412;

	/**
	 * The bank account status for a specific agent (int or long or double).
	 */
	public static final int DU_AD_NETWORK_BANK_ACCOUNT = 413;
	
	/**
	 * @see {@link CampaignAuctionReport}.
	 */
	public static final int DU_CAMPAIGN_AUCTION_REPORT = 414;

	/**
	 * The TAC AA Publisher role.
	 */
	public static final int PUBLISHER = 0;
	/**
	 * The TAC AA Advertiser role.
	 */
	public static final int ADVERTISER = 1;
	/**
	 * The TAC AA User role.
	 */
	public static final int USERS = 2;
	/**
	 * The TAC Adx role.
	 */
	public static final int ADX_AGENT_ROLE_ID = 3;
	// Mariano
	/**
	 * The {@link DemandAgent} role.
	 */
	public static final int DEMAND_AGENT_ROLE_ID = 4;
	/**
	 * The ad network role.
	 */
	public static final int AD_NETOWRK_ROLE_ID = 5;
	/** {@link DemandAgent} name. */
	public static final String DEMAND_AGENT_NAME = "demand";
	/** {@link AdxUsers} name. */
	public static final String ADX_AGENT_NAME = "adxusers";
	/**
	 * The TAC AA participant roles as human readable names.
	 */
	public static final String[] ROLE_NAME = { "Publisher", "Advertiser",
			"User", "ADX Agent", "Demand Agent", "Ad Newtork" };
}
