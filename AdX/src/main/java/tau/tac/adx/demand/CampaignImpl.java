package tau.tac.adx.demand;

import static edu.umich.eecs.tac.auction.AuctionUtils.hardSort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.devices.Device;
import tau.tac.adx.report.adn.MarketSegment;


public class CampaignImpl implements Campaign, Accumulator<CampaignStats> {
	private Logger log = Logger.getLogger(CampaignImpl.class.getName());	
	private final static double ERRA = 4.08577;
	private final static double ERRB = -3.08577;

	private final static Long DEFAULT_BUDGET_FACTOR = 1L;

	/* maintains quality score - notified upon campaign end*/
	protected QualityManager qualityManager;
	
	int id;
	/* contract attributes */
	Long reachImps;
	int dayStart;
	int dayEnd;
	MarketSegment targetSegment;
	double videoCoef;
	double mobileCoef;
	
	/* auction info */
	private final static double RESERVE_BUDGET_FACTOR = 1.0;
	private Map<String,Long> advertisersBids;
	
	/* set upon campaign allocation/auction*/
	Double budget;
	String advertiser;

	/* current day id and accounting info */
	protected int day;
	private CampaignStats todays;
	private CampaignStats totals;
	private SortedMap<Integer,CampaignStats> dayStats;
	
	
	
	public CampaignImpl(QualityManager qualityManager, int reachImps, int dayStart, int dayEnd,
			MarketSegment targetSegment, double videoCoef, double mobileCoef) {
		
		if (qualityManager == null)
			throw new NullPointerException("qualityManager cannot be null");

		id = hashCode();
		dayStats = new TreeMap<Integer,CampaignStats>();
		advertisersBids = new HashMap<String,Long>();
	    budget = null;
		advertiser = null;	
		
		day = 0;
		
		this.qualityManager = qualityManager;
		this.reachImps = (long)reachImps;
		this.dayStart = dayStart;
		this.dayEnd = dayEnd;
		this.targetSegment = targetSegment;
		this.videoCoef = videoCoef;
		this.mobileCoef = mobileCoef;
		
		todays = new CampaignStats(0.0,0.0,0.0);
	}	

	
	public Double getBudget() {
		return budget;
	}


	public Long getReachImps() {
		return reachImps;
	}
	public int getDayStart(){
		return dayStart;
		
	}
	public int getDayEnd(){
		return dayEnd;
		
	}
	public MarketSegment getTargetSegment(){
		return targetSegment;
		
	}
	public double getVideoCoef(){
		return videoCoef;
		
	}
	public double getMobileCoef(){
		return mobileCoef;
		
	}

	
	public void impress(MarketSegment segment, AdType adType, Device device, long costMillis) {
		if (isAllocated()) {
  		    todays.cost += costMillis/1000.0;

  		    double imps = (device == Device.mobile ? mobileCoef : 1) * (adType == AdType.video ? videoCoef : 1);
			
  		    if (segment == targetSegment) 
			  todays.tartgetedImps +=  imps;
			else  
			  todays.otherImps +=  imps;
		}
	}

	
	double effectiveReachRatio(double imps) {		
		double ratio = imps/reachImps;
		return (2.0/ERRA)*(Math.atan(ERRA*ratio + ERRB) - Math.atan(ERRB));		
	}
	
	
	@Override
	public void nextTimeUnit(int timeUnit) {		
		dayStats.put (day, todays);
		day = timeUnit;
		todays = new CampaignStats(0.0,0.0,0.0);
		if (day == dayEnd + 1) { /* was last day - update quality score */
			totals = getStats(dayStart, dayEnd);
			qualityManager.updateQualityScore(advertiser, effectiveReachRatio(totals.tartgetedImps));
		}
	}


	public String getAdvertiser() {
		return advertiser;
	}


	
	public void addAdvertiserBid(String advertiser, Long budgetBid) {
		/* bids above the reserve budget are not considered */
		if ((budgetBid > 0) && (budgetBid <= RESERVE_BUDGET_FACTOR*reachImps) )
			advertisersBids.put(advertiser, budgetBid);		
	}


	public Map<String, Long> getBiddingAdvertisers() {
		return advertisersBids;
	}

	
	public void allocateToAdvertiser(String advertiser) {
		budget = new Double(reachImps*DEFAULT_BUDGET_FACTOR);	
		this.advertiser  = advertiser; 
	}

	
	public void auction() {
		double bsecond;
		int advCount = advertisersBids.size();
		advertiser = "";
		if (advCount > 0) {			
			String[] advNames = new String[advCount];
			double[] qualityScores = new double[advCount];
			double[] bids = new double[advCount];
			double[] scores = new double[advCount];
			int[] indices = new int[advCount];
		
			int i=0;
			for (String advName : advertisersBids.keySet()) {
				advNames[i] = new String(advName);
				bids[i] = advertisersBids.get(advName);
				qualityScores[i] = qualityManager.getQualityScore(advName);
				scores[i] = qualityScores[i] / bids[i];
				indices[i] = i;
				i++;
			}

			hardSort(scores, indices);

			advertiser = advNames[indices[0]];
		
			double reserveScore = 1 / (RESERVE_BUDGET_FACTOR*reachImps);
			
			if (advCount == 1) 
				bsecond = reserveScore;
			else 
				bsecond = (scores[indices[1]] > reserveScore) ? scores[indices[1]] : reserveScore;
			
			budget = new Double(qualityScores[indices[0]] / bsecond);
		} 
	}
	

	
	public int getRemainingDays() {
		if (day > dayEnd) {
			return 0;
		} else {
			int cday = (day <= dayStart) ? dayStart :  day;
			return dayEnd - cday + 1;
		}
	}

	
	public boolean isActive() {
		return (isAllocated() && (day <= dayEnd) && (day >= dayStart));
	}


	public boolean isAllocated() {
		return (budget != null) && (advertiser != null);
	}

	public CampaignStats getStats(int timeUnitFrom, int timeUnitTo) {
	  CampaignStats current = (timeUnitTo >= day) ? todays : null; /* should add current stats to accumulated */   
	  SortedMap<Integer,CampaignStats> daysRangeStats = dayStats.subMap(timeUnitFrom, timeUnitTo + 1);   
	  return AccumulatorImpl.accumulate(this, new ArrayList<CampaignStats>(daysRangeStats.values()), new CampaignStats(0.0, 0.0, 0.0)).add(current);
	}
	
	
	public CampaignStats getTodayStats() {
		return todays;
	}

	public CampaignStats accumulate(CampaignStats interim, CampaignStats next) {
		return interim.add(next);
	}


	@Override
	public int getId() {
		return id;
	}


	@Override
	public String toString() {
		return "CampaignImpl [qualityManager=" + qualityManager + ", id=" + id
				+ ", reachImps=" + reachImps + ", dayStart=" + dayStart
				+ ", dayEnd=" + dayEnd + ", targetSegment=" + targetSegment
				+ ", videoCoef=" + videoCoef + ", mobileCoef=" + mobileCoef
				+ ", advertisersBids=" + advertisersBids + ", budget=" + budget
				+ ", advertiser=" + advertiser + ", day=" + day + ", todays="
				+ todays + ", totals=" + totals + ", dayStats=" + dayStats
				+ "]";
	}

}
