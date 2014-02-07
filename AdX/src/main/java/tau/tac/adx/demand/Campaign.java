package tau.tac.adx.demand;

/**
 * Encapsulates an advertising campaign's life-cycle:
 * Upon creation may be allocated to a predefined advertiser, 
 * or advertisers may bid for the budget (auctioned to the lowest bidder).
 * Impressions indicated are accounted for during every day the campaign is in effect.
 * At campaign end the resulting quality is indicated to an associated quality manager.  
 */
import java.util.Map;
import java.util.Set;

import se.sics.tasim.aw.TimeListener;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.campaign.auction.CampaignAuctionReport;
import tau.tac.adx.users.AdxUser;

/**
 * 
 * @author Mariano Schain
 * 
 */
public interface Campaign extends TimeListener {
	/**
	 * Adds an advertiser's bid to be considered in the auction
	 * 
	 * @param advertiser
	 *            - advertiser id
	 * @param budgetBid
	 *            - the related bid for the campaigns total budget
	 */
	void addAdvertiserBid(String advertiser, Long budgetBid);

	/**
	 * 
	 * @return ids of bidding advertisers and related bids
	 */
	Map<String, Long> getBiddingAdvertisers();

	/**
	 * Contract is allocated to specified advertiser, bypassing auction budget
	 * is set to reflect unit cost (1) per impression
	 * 
	 * @param advertiser
	 *            - advertiser id
	 */
	void allocateToAdvertiser(String advertiser);

	/**
	 * Conduct auction among bidding advertisers. Auction results in allocating
	 * to the winning advertiser and setting the contract's related budget.
	 * 
	 * @return  {@link CampaignAuctionReport}.
	 */
	CampaignAuctionReport auction();

	/**
	 * 
	 * @return id of allocated advertiser
	 */
	String getAdvertiser();

	/**
	 * 
	 * @return contract budget
	 */
	Double getBudget();

	boolean isOverTodaysLimit();

	/**
	 * Update an impression allocated to this campaign
	 * 
	 * @param adxUser
	 * @param adType
	 * @param device
	 * @param double1
	 *            : paid to the Publisher as determined by AdX auction (in milli
	 *            units)
	 */
	void impress(AdxUser adxUser, AdType adType, Device device, double cost);

	/**
	 * 
	 * @return active campaign days remaining (including current, that is: 1 if
	 *         last day, 0 if campaign already over, campaign length if not
	 *         started or started today)
	 */
	int getRemainingDays();

	/**
	 * 
	 * @return true if current day is not before start day and not after end day
	 */
	boolean isActive();

	/**
	 * 
	 * @return true if should send report about campaign at current day 
	 */
	boolean shouldReport();

	/**
	 * 
	 * @return true is advertiser and budget were set (by auction or otherwise)
	 */
	boolean isAllocated();

	/**
	 * 
	 * @param timeUnitFrom
	 *            Inclusive!
	 * @param timeUnitTo
	 *            Inclusive! Note: range should not include current day!
	 * @return costs and impressions for the past days range
	 */
	CampaignStats getStats(int timeUnitFrom, int timeUnitTo);

	CampaignStats getTodayStats();

	Long getReachImps();

	int getDayStart();

	int getDayEnd();

	Set<MarketSegment> getTargetSegment();

	double getVideoCoef();

	double getMobileCoef();

	int getId();

	void preNextTimeUnit(int date);

	CampaignStats getTotals();

	int getImpressionLimit();

	void registerToEventBus();

	Double getBudgetlimit();

	String logToString();

	Double getTotalBudgetlimit();

	int getTotalImpressionLimit();

	boolean isOverTotalLimits();

	void setRandomAllocPr(Double rap);

}
