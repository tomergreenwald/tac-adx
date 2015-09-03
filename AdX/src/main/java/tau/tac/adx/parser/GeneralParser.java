package tau.tac.adx.parser;

import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.logtool.LogReader;
import se.sics.tasim.logtool.ParticipantInfo;
import se.sics.tasim.props.SimulationStatus;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.demand.CampaignStats;
import tau.tac.adx.devices.Device;
import tau.tac.adx.props.AdxBidBundle;
import tau.tac.adx.props.AdxBidBundle.BidEntry;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.report.adn.AdNetworkKey;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.AdNetworkReportEntry;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.AdNetBidMessage;
import tau.tac.adx.report.demand.AdNetworkDailyNotification;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.CampaignReport;
import tau.tac.adx.report.demand.CampaignReportEntry;
import tau.tac.adx.report.demand.CampaignReportKey;
import tau.tac.adx.report.demand.InitialCampaignMessage;
import tau.tac.adx.sim.TACAdxConstants;
import tau.tac.adx.users.AdxUser;
import edu.umich.eecs.tac.Parser;
import edu.umich.eecs.tac.props.BankStatus;

/**
 * <code>GeneralParser</code> is a simple example of a TAC Adx parser that
 * prints out a variety of messages received in a simulation from the simulation
 * log file.
 * <p>
 * <p/>
 * The class <code>Parser</code> is inherited to provide base functionality for
 * TAC Adx log processing.
 * 
 * @author - greenwald
 * 
 * @see edu.umich.eecs.tac.Parser
 */
public class GeneralParser extends Parser {

	private int day = 0;
	private final String[] participantNames;
	private final boolean[] is_Advertiser;
	private final ParticipantInfo[] participants;
	private final ConfigManager configManager;

	private boolean ucs = false;
	private boolean rating = false;
	private boolean bank = false;
	private boolean campaign = false;
	private boolean adnet = false;
	private boolean all = false;

	private boolean verify = false;
	private HashMap<String, AdvertiserData> advData = new HashMap<String, AdvertiserData>();
	private HashMap<Integer, Integer> cmpStartDayById = new HashMap<Integer, Integer>();

	public GeneralParser(LogReader reader, ConfigManager configManager) {
		super(reader);
		this.configManager = configManager;

		// Print agent indexes/gather names
		System.out.println("****AGENT INDEXES****");
		participants = reader.getParticipants();
		if (participants == null) {
			throw new IllegalStateException("no participants");
		}
		int agent;
		participantNames = new String[participants.length];
		is_Advertiser = new boolean[participants.length];

		for (int i = 0, n = participants.length; i < n; i++) {
			ParticipantInfo info = participants[i];
			agent = info.getIndex();
			System.out.println(info.getName() + ": " + agent);
			participantNames[agent] = info.getName();
			if (info.getRole() == TACAdxConstants.AD_NETOWRK_ROLE_ID) {
				is_Advertiser[agent] = true;
				int simLength = reader.getSimulationLength() / 10000;
				String agentName = info.getName();
				AdvertiserData iData = new AdvertiserData(simLength + 2, agent,
						agentName);
				advData.put(agentName, iData);
			} else
				is_Advertiser[agent] = false;
		}

		System.out.println("**** AdX General Log Prser***");
		ucs = configManager.getPropertyAsBoolean("ucs", false);
		rating = configManager.getPropertyAsBoolean("rating", false);
		bank = configManager.getPropertyAsBoolean("bank", false);
		campaign = configManager.getPropertyAsBoolean("campaign", false);
		adnet = configManager.getPropertyAsBoolean("adnet", false);
		all = configManager.getPropertyAsBoolean("all", false);
		verify = configManager.getPropertyAsBoolean("verify", false);
	}

	@Override
	protected void message(int sender, int receiver, Transportable content) {

		if (verify) {
			/*
			 * if (content instanceof SimulationStatus) { SimulationStatus ss =
			 * (SimulationStatus) content; day = ss.getCurrentDate(); }
			 */
			if (content instanceof AdxBidBundle) {
				AdxBidBundle bundle = (AdxBidBundle) content;
				AdvertiserData receiverData = advData
						.get(participantNames[sender]);
				for (AdxQuery key : bundle.keys()) {
					if (key.getPublisher().startsWith(AdxBidBundle.CMP_DSL)
							|| key.getPublisher().startsWith(
									AdxBidBundle.CMP_TSL)) {
						BidEntry entry = bundle.getEntry(key);
						receiverData.daysData[day].cmpBudgetDailyLimits.put(
								entry.getCampaignId(), entry.getDailyLimit());
						System.out.println(getBasicInfoString(sender)
								+ StringUtils.rightPad("AdxBidBundle: ", 20)
								+ "campaign #" + entry.getCampaignId()
								+ " requested daily spend limit at "
								+ entry.getDailyLimit());
					} else if (key.getPublisher().startsWith(
							AdxBidBundle.CMP_TSL)
							|| key.getPublisher().startsWith(
									AdxBidBundle.CMP_TSL)) {
						BidEntry entry = bundle.getEntry(key);
						receiverData.daysData[day].cmpBudgetTotalLimits.put(
								entry.getCampaignId(), entry.getDailyLimit());
						System.out.println(getBasicInfoString(sender)
								+ StringUtils.rightPad("AdxBidBundle: ", 20)
								+ "campaign #" + entry.getCampaignId()
								+ " requested total spend limit at "
								+ entry.getDailyLimit());
					}
				}
			}
			if (content instanceof AdNetBidMessage) {
				AdNetBidMessage dailyAdNetBid = (AdNetBidMessage) content;
				AdvertiserData senderData = advData
						.get(participantNames[sender]);
				senderData.daysData[day + 2].ucsBid = dailyAdNetBid.getUcsBid();
				senderData.daysData[day + 2].cmpBid = dailyAdNetBid
						.getCampaignBudget();
			}

			/* cmp data is at "startDay" daysData */
			if (content instanceof CampaignOpportunityMessage) {
				CampaignOpportunityMessage dailyCmpOp = (CampaignOpportunityMessage) content;
				AdvertiserData receiverData = advData
						.get(participantNames[receiver]);

				receiverData.daysData[day + 2].cmpId = dailyCmpOp.getId();
				receiverData.daysData[day + 2].cmpWon = false;
				receiverData.daysData[day + 2].cmpReach = dailyCmpOp
						.getReachImps();
				receiverData.daysData[day + 2].cmpDayEnd = dailyCmpOp
						.getDayEnd();
				receiverData.daysData[day + 2].cmpMobileCoef = dailyCmpOp
						.getMobileCoef();
				receiverData.daysData[day + 2].cmpVideoCoef = dailyCmpOp
						.getVideoCoef();
				receiverData.daysData[day + 2].cmpTargetSegment = dailyCmpOp
						.getTargetSegment();

				cmpStartDayById.put(dailyCmpOp.getId(),
						(int) (dailyCmpOp.getDayStart()));
				if (dailyCmpOp.getDayStart() != day + 2) {
					/* ERROR: start day not two days after cmpOp */
					System.out.println(getBasicInfoString(receiver)
							+ StringUtils.rightPad("CmpOp: ", 20)
							+ "ERROR: start day not two days after cmpOp");
				}

				receiverData.daysData[day + 2].cmpRev = 0;
				receiverData.daysData[day + 2].adxAdnetReportedCosts = 0;
				receiverData.daysData[day + 2].adxAdnetReportedImpressions = 0;
				receiverData.daysData[day + 2].adxAdnetReportedTargetedImpressions = 0;
			}

			if (content instanceof BankStatus) {
				if (day > 1) {
					BankStatus dailyBank = (BankStatus) content;
					AdvertiserData receiverData = advData
							.get(participantNames[receiver]);
					receiverData.daysData[day - 1].reportedBalance = dailyBank
							.getAccountBalance();

					System.out.println(getBasicInfoString(receiver)
							+ StringUtils.rightPad("BankStatus: ", 20)
							+ "Balance for day " + (day - 1) + "  Reported : "
							+ dailyBank.getAccountBalance());

					if (day > 2) { /* verify reposted balance for day-2 */
						int verifiedDay = day - 2;

						double expectedBalance = receiverData.daysData[verifiedDay + 1].accumulatedRevenue
								- (receiverData.daysData[verifiedDay - 1].adxAccumulatedCosts + receiverData.daysData[verifiedDay + 1].ucsAccumulatedCosts);

						if (Math.abs(expectedBalance
								- receiverData.daysData[verifiedDay].reportedBalance) > 0.01) {
							/* ERROR: Quality Rating Computation */
							double reportedBalance = receiverData.daysData[verifiedDay].reportedBalance;
							System.out
									.println(getBasicInfoString(receiver)
											+ StringUtils.rightPad(
													"BankStatus: ", 20)
											+ "ERROR: Balance Computation of day "
											+ verifiedDay
											+ " Diff: "
											+ (reportedBalance - expectedBalance)
											+ "  Reported : "
											+ reportedBalance
											+ " Expected: "
											+ expectedBalance
											+ "("
											+ receiverData.daysData[verifiedDay].accumulatedRevenue
											+ " - "
											+ receiverData.daysData[verifiedDay].adxAccumulatedCosts
											+ " - "
											+ receiverData.daysData[verifiedDay].ucsAccumulatedCosts
											+ ")");
						}
					}
				}

			}

			if (content instanceof AdNetworkDailyNotification) {
				AdNetworkDailyNotification dailyNotification = (AdNetworkDailyNotification) content;
				AdvertiserData receiverData = advData
						.get(participantNames[receiver]);

				if (Math.abs(receiverData.daysData[day].qualityRating
						- dailyNotification.getQualityScore()) > 0.01) {
					/* ERROR: Quality Rating Computation */
					System.out.println(getBasicInfoString(receiver)
							+ StringUtils.rightPad("AdNetNotification: ", 20)
							+ "ERROR: Quality Rating Computation - "
							+ "Reported Rating: "
							+ dailyNotification.getQualityScore()
							+ " Expected: "
							+ receiverData.daysData[day].qualityRating);
				}
				receiverData.daysData[day].qualityRating = dailyNotification
						.getQualityScore();

				System.out
						.println(getBasicInfoString(receiver)
								+ StringUtils
										.rightPad(
												"AdNetNotification: UCS costs accumulated ",
												20)
								+ " on day "
								+ day
								+ ": "
								+ dailyNotification.getPrice()
								+ " (was "
								+ receiverData.daysData[day].ucsAccumulatedCosts
								+ "  Now "
								+ (receiverData.daysData[day].ucsAccumulatedCosts
										+ dailyNotification.getPrice() + ")"));

				receiverData.daysData[day].ucsAccumulatedCosts += dailyNotification
						.getPrice();

				receiverData.daysData[day + 1].ucsLevel = dailyNotification
						.getServiceLevel();

				receiverData.daysData[day + 1].cmpWon = dailyNotification
						.getCostMillis() != 0;
				if (receiverData.daysData[day + 1].cmpWon) {
					receiverData.daysData[day + 1].cmpBudget = dailyNotification
							.getCostMillis() / 1000.0;
					receiverData.daysData[day + 1].cmpImpsTgt = 0;
					receiverData.daysData[day + 1].cmpImpsNonTgt = 0;
				}
			}

			if (content instanceof InitialCampaignMessage) {
				InitialCampaignMessage initialCmp = (InitialCampaignMessage) content;
				AdvertiserData receiverData = advData
						.get(participantNames[receiver]);

				receiverData.daysData[1].qualityRating = 1.0;
				receiverData.daysData[1].ucsAccumulatedCosts = 0;
				receiverData.daysData[1].ucsLevel = 1.0;

				receiverData.daysData[1].adxAccumulatedCosts = 0;

				receiverData.daysData[1].accumulatedRevenue = 0;

				receiverData.daysData[1].cmpId = initialCmp.getId();

				cmpStartDayById.put(receiverData.daysData[1].cmpId, 1);
				receiverData.daysData[1].cmpWon = true;
				receiverData.daysData[1].cmpReach = initialCmp.getReachImps();
				receiverData.daysData[1].cmpBudget = initialCmp
						.getBudgetMillis() / 1000.0;
				receiverData.daysData[1].cmpDayEnd = initialCmp.getDayEnd();
				receiverData.daysData[1].cmpImpsNonTgt = 0;
				receiverData.daysData[1].cmpImpsTgt = 0;
				receiverData.daysData[1].cmpRev = 0;
				receiverData.daysData[1].cmpAdxCost = 0;

				receiverData.daysData[1].adxAdnetReportedCosts = 0;
				receiverData.daysData[1].adxAdnetReportedImpressions = 0;
				receiverData.daysData[1].adxAdnetReportedTargetedImpressions = 0;

				receiverData.daysData[day + 1].cmpMobileCoef = initialCmp
						.getMobileCoef();
				receiverData.daysData[day + 1].cmpVideoCoef = initialCmp
						.getVideoCoef();
				receiverData.daysData[day + 1].cmpTargetSegment = initialCmp
						.getTargetSegment();
			}

			if (content instanceof CampaignReport) {
				CampaignReport cmpReport = (CampaignReport) content;
				AdvertiserData receiverData = advData
						.get(participantNames[receiver]);

				for (CampaignReportKey rKey : cmpReport.keys()) {

					CampaignReportEntry rEntry = cmpReport
							.getCampaignReportEntry(rKey);
					int cmpId = rEntry.getKey().getCampaignId();
					int cmpStartDay = cmpStartDayById.get(cmpId);
					if (receiverData.daysData[cmpStartDay].cmpWon) {
						if ((day - 1 <= receiverData.daysData[cmpStartDay].cmpDayEnd)
								&& (day - 1 >= cmpStartDay)) {
							receiverData.daysData[cmpStartDay].cmpDailyStats
									.put((day - 1),
											new CampaignStats(rEntry
													.getCampaignStats()
													.getTargetedImps(), rEntry
													.getCampaignStats()
													.getOtherImps(), rEntry
													.getCampaignStats()
													.getCost()));

							receiverData.daysData[cmpStartDay].cmpAdxCost = rEntry
									.getCampaignStats().getCost();

							System.out.println(getBasicInfoString(receiver)
									+ StringUtils.rightPad(
											"CmpReport: AdX Costs", 20)
									+ " on day " + (day - 1) + " #"
									+ rKey.getCampaignId() + " totaled "
									+ rEntry.getCampaignStats().getCost());

						} else {
							/* ERROR: reported campaign day out of range */
							System.out
									.println(getBasicInfoString(receiver)
											+ StringUtils.rightPad(
													"CmpReport: ", 20)
											+ "ERROR: reported campaign day out of range");
						}

						if (day - 1 == receiverData.daysData[cmpStartDay].cmpDayEnd) { /*
																						 * reported
																						 * cmp
																						 * last
																						 * day
																						 */
							receiverData.daysData[day].qualityRating = updatedQualityScore(
									rEntry.getCampaignStats().getTargetedImps(),
									receiverData.daysData[cmpStartDay].cmpReach,
									receiverData.daysData[day].qualityRating);

							receiverData.daysData[cmpStartDay].cmpERR = eRR(
									rEntry.getCampaignStats().getTargetedImps(),
									receiverData.daysData[cmpStartDay].cmpReach);

							double cmpRev = receiverData.daysData[cmpStartDay].cmpERR
									* receiverData.daysData[cmpStartDay].cmpBudget;

							System.out
									.println(getBasicInfoString(receiver)
											+ StringUtils.rightPad(
													"CmpReport - Ended. ", 20)
											+ "Reach: "
											+ receiverData.daysData[cmpStartDay].cmpReach
											+ " Achieved: "
											+ rEntry.getCampaignStats()
													.getTargetedImps()
											+ " ERR: "
											+ receiverData.daysData[cmpStartDay].cmpERR
											+ " Revenue added on day "
											+ day
											+ ": "
											+ cmpRev
											+ " (was "
											+ receiverData.daysData[day].accumulatedRevenue
											+ "now "
											+ (receiverData.daysData[day].accumulatedRevenue + cmpRev)
											+ ")");

							receiverData.daysData[day].accumulatedRevenue += cmpRev;

						}

					} else { /* ERROR: reported on non-won campaign */
						System.out.println(getBasicInfoString(receiver)
								+ StringUtils.rightPad("CmpReport: ", 20)
								+ "ERROR: reported on non-won campaign");
					}

				}

				receiverData.daysData[day - 1].adxAccumulatedCosts = 0;
				for (int i = 1; i < day; i++) {
					if (receiverData.daysData[i].cmpWon) {
						receiverData.daysData[day - 1].adxAccumulatedCosts += receiverData.daysData[i].cmpAdxCost;
					}
				}

				System.out.println(getBasicInfoString(receiver)
						+ StringUtils.rightPad(
								"CmpReport: Accumulated AdX Costs", 20)
						+ " on day " + (day - 1) + " totaled "
						+ receiverData.daysData[day - 1].adxAccumulatedCosts);

			}

			if (content instanceof AdNetworkReport) {
				AdNetworkReport adNetworkReport = (AdNetworkReport) content;
				AdvertiserData receiverData = advData
						.get(participantNames[receiver]);

				for (AdNetworkKey adNetworkKey : adNetworkReport.keys()) {
					AdNetworkReportEntry reportEntry = adNetworkReport
							.getEntry(adNetworkKey);

					int cmpId = reportEntry.getKey().getCampaignId();
					int cmpStartDay = cmpStartDayById.get(cmpId);

					if (receiverData.daysData[cmpStartDay].cmpWon) {

						if ((day - 1 <= receiverData.daysData[cmpStartDay].cmpDayEnd)
								&& (day - 1 >= cmpStartDay)) {

							System.out
									.println(getBasicInfoString(receiver)
											+ StringUtils.rightPad(
													"AdNetworkReport: ", 20)
											+ " reporting campaign "
											+ cmpId
											+ " day "
											+ (day - 1)
											+ " of "
											+ "["
											+ cmpStartDay
											+ ","
											+ receiverData.daysData[cmpStartDay].cmpDayEnd
											+ "]");

							receiverData.daysData[cmpStartDay].adxAdnetReportedCosts += reportEntry
									.getCost() / 1000;

							double coef = 1.0;
							if (reportEntry.getKey().getAdType() == AdType.video) {
								coef *= receiverData.daysData[cmpStartDay].cmpVideoCoef;
							}

							if (reportEntry.getKey().getDevice() == Device.mobile) {
								coef *= receiverData.daysData[cmpStartDay].cmpMobileCoef;
							}

							double imps = coef * reportEntry.getWinCount();

							receiverData.daysData[cmpStartDay].adxAdnetReportedImpressions += imps;

							Set<MarketSegment> userSegment = MarketSegment
									.extractSegment(new AdxUser(reportEntry
											.getKey().getAge(), reportEntry
											.getKey().getGender(), reportEntry
											.getKey().getIncome(), 0, 0));

							if (userSegment
									.contains(receiverData.daysData[cmpStartDay].cmpTargetSegment)) {
								receiverData.daysData[cmpStartDay].adxAdnetReportedTargetedImpressions += imps;
							}

						} else {
							/* ERROR: reported campaign day out of range */
							System.out
									.println(getBasicInfoString(receiver)
											+ StringUtils.rightPad(
													"AdNetworkReport: ", 20)
											+ "ERROR: reported campaign "
											+ cmpId
											+ " day "
											+ (day - 1)
											+ " out of range"
											+ "["
											+ cmpStartDay
											+ ","
											+ receiverData.daysData[cmpStartDay].cmpDayEnd
											+ "]");
						}
					} else {
						/* ERROR: reported non won campaign */
						System.out.println(getBasicInfoString(receiver)
								+ StringUtils.rightPad("AdNetworkReport: ", 20)
								+ "ERROR: reported non won campaign");
					}

				}

				int d = day - 1;
				for (Integer campaignId : receiverData.daysData[d].cmpBudgetDailyLimits
						.keySet()) {
					HashMap<Integer, CampaignStats> cmpDailyStats = receiverData.daysData[cmpStartDayById
							.get(campaignId)].cmpDailyStats;
					if (!cmpDailyStats.isEmpty()) {
						Double dailyLimit = receiverData.daysData[d].cmpBudgetDailyLimits
								.get(campaignId);

						double cost;
						// since the daily stats aggregate results we need to
						// subtract two consecutive days to get the result for a
						// single day.
						if (d > cmpStartDayById.get(campaignId)) {
							cost = cmpDailyStats.get(d).getCost()
									- cmpDailyStats.get(d - 1).getCost();
						} else {
							cost = cmpDailyStats.get(d).getCost();
						}
						if (cost > dailyLimit && (cost - dailyLimit) > 0.1) {
							/* ERROR: Cost Computation AdnetReport vs CmpReport */
							System.out
									.println(getBasicInfoString(receiver)
											+ StringUtils.rightPad(
													"CampaignLimit: ", 20)
											+ "ERROR: impressions cost over the daily limit. Cmapaign #"
											+ campaignId + " cost: " + cost
											+ " limit: " + dailyLimit);
						}
					}
				}
				if ((receiverData.daysData[d].cmpWon)
						&& (day - 1 <= receiverData.daysData[d].cmpDayEnd)) {
					// for each active won campaign
					if (Math.abs(receiverData.daysData[d].cmpAdxCost
							- receiverData.daysData[d].adxAdnetReportedCosts) > 0.03) {
						/* ERROR: Cost Computation AdnetReport vs CmpReport */
						System.out
								.println(getBasicInfoString(receiver)
										+ StringUtils.rightPad(
												"AdNetworkReport: ", 20)
										+ "ERROR: Cost Computation AdnetReport vs CmpReport - "
										+ receiverData.daysData[d].cmpId
										+ " Reported cmp: "
										+ receiverData.daysData[d].cmpAdxCost
										+ " AdNet: "
										+ receiverData.daysData[d].adxAdnetReportedCosts);
					}

					// if (Math.abs(receiverData.daysData[d].cmpImpsTgt -
					// receiverData.daysData[d].adxAdnetReportedTargetedImpressions)
					// > 0.03) {
					// /* ERROR: Tgt Imps Computation AdnetReport vs CmpReport
					// */
					// System.out.println(getBasicInfoString(receiver)
					// + StringUtils.rightPad("AdNetworkReport: ", 20)
					// +
					// "ERROR: Tgt Imps Computation AdnetReport vs CmpReport - "
					// + receiverData.daysData[d].cmpId
					// + " Reported cmp: " + receiverData.daysData[d].cmpImpsTgt
					// + " AdNet: " +
					// receiverData.daysData[d].adxAdnetReportedTargetedImpressions
					// );
					// }
					//
					//
					// if (Math.abs(receiverData.daysData[d].cmpImpsNonTgt +
					// receiverData.daysData[d].cmpImpsTgt -
					// receiverData.daysData[d].adxAdnetReportedImpressions) >
					// 0.03) {
					// /* ERROR: All Imps Computation AdnetReport vs CmpReport
					// */
					// System.out.println(getBasicInfoString(receiver)
					// + StringUtils.rightPad("AdNetworkReport: ", 20)
					// +
					// "ERROR: All Imps Computation AdnetReport vs CmpReport - "
					// + receiverData.daysData[d].cmpId
					// + " Reported cmp: " +
					// (receiverData.daysData[d].cmpImpsNonTgt +
					// receiverData.daysData[d].cmpImpsTgt)
					// + " AdNet: " +
					// receiverData.daysData[d].adxAdnetReportedImpressions
					// );
					// }
				}
			}

		} else {
			if (all) {
				System.out.println(getBasicInfoString(receiver)
						+ StringUtils.rightPad(content.getClass()
								.getSimpleName() + ": ", 30) + content);
			}
			if (!all && content instanceof AdNetworkDailyNotification) {
				if (ucs) {
					AdNetworkDailyNotification dailyNotification = (AdNetworkDailyNotification) content;
					System.out.println(getBasicInfoString(receiver)
							+ StringUtils.rightPad("UCS level: ", 20)
							+ dailyNotification.getServiceLevel());
				}
				if (rating) {
					AdNetworkDailyNotification dailyNotification = (AdNetworkDailyNotification) content;
					System.out.println(getBasicInfoString(receiver)
							+ StringUtils.rightPad("Quality rating: ", 20)
							+ dailyNotification.getQualityScore());
				}
			} else if (!all && bank && content instanceof BankStatus) {
				BankStatus status = (BankStatus) content;
				System.out.println(getBasicInfoString(receiver)
						+ StringUtils.rightPad("Bank balance: ", 20)
						+ status.getAccountBalance());
			} else if (!all && campaign && content instanceof CampaignReport) {
				CampaignReport campaignReport = (CampaignReport) content;
				for (CampaignReportKey campaignReportKey : campaignReport) {
					CampaignReportEntry reportEntry = campaignReport
							.getEntry(campaignReportKey);
					CampaignStats campaignStats = reportEntry
							.getCampaignStats();
					System.out.println(getBasicInfoString(receiver)
							+ StringUtils.rightPad("Campaign report: ", 20)
							+ StringUtils.rightPad(
									"#" + campaignReportKey.getCampaignId(), 5)
							+ "\t Targeted Impressions: "
							+ StringUtils.rightPad(
									"" + campaignStats.getTargetedImps(), 5)
							+ "\t Non Targeted Impressions: "
							+ StringUtils.rightPad(
									"" + campaignStats.getOtherImps(), 5)
							+ "\t Cost: " + campaignStats.getCost());
				}

			} else if (!all && adnet && content instanceof AdNetworkReport) {
				AdNetworkReport adNetworkReport = (AdNetworkReport) content;
				for (AdNetworkKey adNetworkKey : adNetworkReport) {
					AdNetworkReportEntry reportEntry = adNetworkReport
							.getEntry(adNetworkKey);
					System.out.println(getBasicInfoString(receiver)
							+ StringUtils.rightPad("Ad Network report: ", 20)
							+ adNetworkKey
							+ ","
							+ "Bid count: "
							+ StringUtils.rightPad(
									"" + reportEntry.getBidCount(), 5)
							+ "\t Win count: "
							+ StringUtils.rightPad(
									"" + reportEntry.getWinCount(), 5)
							+ "\t Cost: "
							+ StringUtils.rightPad("" + reportEntry.getCost(),
									5));
				}

			} else if (content instanceof SimulationStatus) {
				SimulationStatus ss = (SimulationStatus) content;
				day = ss.getCurrentDate();
			}
		}
	}

	private String getBasicInfoString(int receiver) {
		return StringUtils.rightPad("" + day, 5) + "\t"
				+ StringUtils.rightPad(participantNames[receiver], 20) + "\t";
	}

	@Override
	protected void nextDay(int date, long serverTime) {
		day = date;
		System.out.println(StringUtils.rightPad("Next day : ", 20) + date
				+ " srarting");

		/* advance advertisers data */
		if (day > 1) {
			for (String advName : advData.keySet()) {
				AdvertiserData aData = advData.get(advName);
				aData.daysData[day].qualityRating = aData.daysData[day - 1].qualityRating;
				aData.daysData[day].ucsAccumulatedCosts = aData.daysData[day - 1].ucsAccumulatedCosts;
				aData.daysData[day].accumulatedRevenue = aData.daysData[day - 1].accumulatedRevenue;

				System.out.println(" Advancing from day " + (day - 1)
						+ " to day " + day + " for " + advName + ": "
						+ "  ucsAccumulatedCosts: "
						+ aData.daysData[day].ucsAccumulatedCosts
						+ "  adxAccumulatedCosts: "
						+ aData.daysData[day].adxAccumulatedCosts
						+ "  accumulatedRevenue: "
						+ aData.daysData[day].accumulatedRevenue);

			}
		}
	}

	private class DayData {
		double qualityRating;

		int cmpId;
		boolean cmpWon;
		long cmpBid;
		double cmpBudget;
		long cmpReach;
		HashMap<Integer, Double> cmpBudgetDailyLimits = new HashMap<Integer, Double>();
		HashMap<Integer, Double> cmpBudgetTotalLimits = new HashMap<Integer, Double>();
		double cmpMobileCoef;
		double cmpVideoCoef;
		double cmpImpsTgt;
		double cmpImpsNonTgt;
		double cmpERR;
		double cmpRev;
		long cmpDayEnd;
		double cmpAdxCost;
		HashMap<Integer, CampaignStats> cmpDailyStats;
		Set<MarketSegment> cmpTargetSegment;

		double adxAccumulatedCosts;

		double adxAdnetReportedCosts;
		double adxAdnetReportedImpressions;
		double adxAdnetReportedTargetedImpressions;

		double accumulatedRevenue;

		double reportedBalance;

		double ucsBid;
		double ucsLevel;
		double ucsAccumulatedCosts;

		DayData() {
			cmpDailyStats = new HashMap<Integer, CampaignStats>();
		}
	}

	private class AdvertiserData {
		DayData daysData[];
		String name;
		String aka;

		AdvertiserData(int days, int index, String agentName) {
			daysData = new DayData[days];
			for (int i = 0; i < days; i++)
				daysData[i] = new DayData();
			aka = new String("adv" + (index - 2));
			name = new String(agentName);
		}
	}

	private final static double ERRA = 4.08577;
	private final static double ERRB = -3.08577;
	private final static double MU = 0.6;

	private double eRR(double imps, double reachImps) {
		double ratio = imps / reachImps;
		return (2.0 / ERRA)
				* (Math.atan(ERRA * ratio + ERRB) - Math.atan(ERRB));
	}

	private double updatedQualityScore(double imps, double reachImps,
			double prevScore) {
		return (1 - MU) * prevScore + MU * eRR(imps, reachImps);
	}

}
