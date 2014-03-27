package tau.tac.adx.report.demand;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.tasim.props.SimpleContent;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.report.adn.MarketSegment;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class InitialCampaignMessage extends SimpleContent {

	private static final long serialVersionUID = -5447083615716436823L;

	/**
	 * The publisher key.
	 */
	private static final String MARKET_SEGMENT_KEY = "USER_KEY";

	private int id;
	private Long reachImps;
	private int dayStart;
	private int dayEnd;
	private Set<MarketSegment> targetSegment = new HashSet<MarketSegment>();
	private String targetSegmentName;
	private double videoCoef;
	private double mobileCoef;
	private String demandAgentAddress;
	private String adxAgentAddress;
	private long budgetMillis;

	public InitialCampaignMessage() {
	}

	public InitialCampaignMessage(Campaign campaign, String demandAgentAddress,
			String adxAgentAddress) {
		this.id = campaign.getId();
		this.reachImps = campaign.getReachImps();
		this.dayStart = campaign.getDayStart();
		this.dayEnd = campaign.getDayEnd();
		this.targetSegment = campaign.getTargetSegment();
		this.videoCoef = campaign.getVideoCoef();
		this.mobileCoef = campaign.getMobileCoef();
		this.demandAgentAddress = demandAgentAddress;
		this.adxAgentAddress = adxAgentAddress;
		this.budgetMillis = campaign.getBudgetMillis();
	}

	public InitialCampaignMessage(int id, Long reachImps, int dayStart,
			int dayEnd, Set<MarketSegment> targetSegment, double videoCoef,
			double mobileCoef, long budgetMillis) {

		this.id = id;
		this.reachImps = reachImps;
		this.dayStart = dayStart;
		this.dayEnd = dayEnd;
		this.targetSegment = targetSegment;
		this.videoCoef = videoCoef;
		this.mobileCoef = mobileCoef;
		this.budgetMillis = budgetMillis;
	}

	public int getId() {
		return id;
	}

	public Long getReachImps() {
		return reachImps;
	}

	public long getDayStart() {
		return dayStart;
	}

	public long getDayEnd() {
		return dayEnd;
	}

	public Set<MarketSegment> getTargetSegment() {
		return targetSegment;
	}

	public double getVideoCoef() {
		return videoCoef;
	}

	public double getMobileCoef() {
		return mobileCoef;
	}

	public String getDemandAgentAddress() {
		return demandAgentAddress;
	}

	public String getAdxAgentAddress() {
		return adxAgentAddress;
	}

	public long getBudgetMillis() {
		return budgetMillis;
	}

	
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer().append(getTransportName())
				.append('[').append(id).append(',').append(reachImps)
				.append(',').append(dayStart).append(',').append(dayEnd).append(',').append(budgetMillis)
				.append(',').append(targetSegment).append(',')
				.append(videoCoef).append(',').append(mobileCoef).append(',')
				.append(demandAgentAddress).append(',');
		return params(buf).append(']').toString();
	}

	// -------------------------------------------------------------------
	// Transportable (externalization support)
	// -------------------------------------------------------------------

	/**
	 * Returns the transport name used for externalization.
	 */
	@Override
	public String getTransportName() {
		return getClass().getName();
	}

	@Override
	public void read(TransportReader reader) throws ParseException {
		if (isLocked()) {
			throw new IllegalStateException("locked");
		}
		id = reader.getAttributeAsInt("id");
		reachImps = reader.getAttributeAsLong("reachImps");
		dayStart = reader.getAttributeAsInt("dayStart");
		dayEnd = reader.getAttributeAsInt("dayEnd");
		videoCoef = reader.getAttributeAsDouble("videoCoef");
		mobileCoef = reader.getAttributeAsDouble("mobileCoef");
		budgetMillis = reader.getAttributeAsLong("budgetMillis");
		demandAgentAddress = reader.getAttribute("demandAgentAddress");
		adxAgentAddress = reader.getAttribute("adxAgentAddress");
		while (reader.nextNode(MARKET_SEGMENT_KEY, false)) {
			targetSegment.add(MarketSegment.valueOf(reader
					.getAttribute(MARKET_SEGMENT_KEY)));
		}
		super.read(reader);
	}

	@Override
	public void write(TransportWriter writer) {
		writer.attr("id", id).attr("reachImps", reachImps)
				.attr("dayStart", dayStart).attr("dayEnd", dayEnd)
				.attr("videoCoef", videoCoef).attr("mobileCoef", mobileCoef).attr("budgetMillis", budgetMillis)
				.attr("demandAgentAddress", demandAgentAddress)
				.attr("adxAgentAddress", adxAgentAddress);
		for (MarketSegment marketSegment : targetSegment) {
			writer.node(MARKET_SEGMENT_KEY)
					.attr(MARKET_SEGMENT_KEY, marketSegment.toString())
					.endNode(MARKET_SEGMENT_KEY);
		}
		super.write(writer);
	}

}
