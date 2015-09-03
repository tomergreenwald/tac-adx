package tau.tac.adx.agents;

import java.util.Set;

import tau.tac.adx.demand.CampaignStats;
import tau.tac.adx.report.adn.MarketSegment;
import tau.tac.adx.report.demand.CampaignOpportunityMessage;
import tau.tac.adx.report.demand.InitialCampaignMessage;

public class CampaignData {
	/* campaign attributes as set by server */
	Long reachImps;
	long dayStart;
	long dayEnd;
	Set<MarketSegment> targetSegment;
	double videoCoef;
	double mobileCoef;
	int id;

	/* campaign info as reported */
	CampaignStats stats;
	double budget;

	public CampaignData(InitialCampaignMessage icm) {
		reachImps = icm.getReachImps();
		dayStart = icm.getDayStart();
		dayEnd = icm.getDayEnd();
		targetSegment = icm.getTargetSegment();
		videoCoef = icm.getVideoCoef();
		mobileCoef = icm.getMobileCoef();
		id = icm.getId();

		stats = new CampaignStats(0, 0, 0);
		budget = 0.0;
	}

	public void setBudget(double d) {
		budget = d;
	}

	public CampaignData(CampaignOpportunityMessage com) {
		dayStart = com.getDayStart();
		dayEnd = com.getDayEnd();
		id = com.getId();
		reachImps = com.getReachImps();
		targetSegment = com.getTargetSegment();
		mobileCoef = com.getMobileCoef();
		videoCoef = com.getVideoCoef();
		stats = new CampaignStats(0, 0, 0);
		budget = 0.0;
	}

	@Override
	public String toString() {
		return "Campaign ID " + id + ": " + "day " + dayStart + " to " + dayEnd
				+ " " + MarketSegment.names(targetSegment) + ", reach: "
				+ reachImps + " coefs: (v=" + videoCoef + ", m=" + mobileCoef
				+ ")";
	}

	int impsTogo() {
		return (int) Math.max(0, reachImps - stats.getTargetedImps());
	}

	void setStats(CampaignStats s) {
		stats.setValues(s);
	}

	/**
	 * @return the reachImps
	 */
	public Long getReachImps() {
		return reachImps;
	}

	/**
	 * @param reachImps
	 *            the reachImps to set
	 */
	public void setReachImps(Long reachImps) {
		this.reachImps = reachImps;
	}

	/**
	 * @return the dayStart
	 */
	public long getDayStart() {
		return dayStart;
	}

	/**
	 * @param dayStart
	 *            the dayStart to set
	 */
	public void setDayStart(long dayStart) {
		this.dayStart = dayStart;
	}

	/**
	 * @return the dayEnd
	 */
	public long getDayEnd() {
		return dayEnd;
	}

	/**
	 * @param dayEnd
	 *            the dayEnd to set
	 */
	public void setDayEnd(long dayEnd) {
		this.dayEnd = dayEnd;
	}

	/**
	 * @return the targetSegment
	 */
	public Set<MarketSegment> getTargetSegment() {
		return targetSegment;
	}

	/**
	 * @param targetSegment
	 *            the targetSegment to set
	 */
	public void setTargetSegment(Set<MarketSegment> targetSegment) {
		this.targetSegment = targetSegment;
	}

	/**
	 * @return the videoCoef
	 */
	public double getVideoCoef() {
		return videoCoef;
	}

	/**
	 * @param videoCoef
	 *            the videoCoef to set
	 */
	public void setVideoCoef(double videoCoef) {
		this.videoCoef = videoCoef;
	}

	/**
	 * @return the mobileCoef
	 */
	public double getMobileCoef() {
		return mobileCoef;
	}

	/**
	 * @param mobileCoef
	 *            the mobileCoef to set
	 */
	public void setMobileCoef(double mobileCoef) {
		this.mobileCoef = mobileCoef;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the stats
	 */
	public CampaignStats getStats() {
		return stats;
	}

	/**
	 * @return the budget
	 */
	public double getBudget() {
		return budget;
	}

}