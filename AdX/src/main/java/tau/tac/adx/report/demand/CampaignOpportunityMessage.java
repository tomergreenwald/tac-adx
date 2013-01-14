package tau.tac.adx.report.demand;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.tasim.props.SimpleContent;
import tau.tac.adx.demand.Campaign;

public class CampaignOpportunityMessage extends SimpleContent {
	private static final long serialVersionUID = -2501549665857756943L;
	
	private int id;
	private Long reachImps;
	private int dayStart;
	private int dayEnd;
	private int targetSegment;
	private double videoCoef;
	private double mobileCoef;
	
	public CampaignOpportunityMessage() {
	}

	public CampaignOpportunityMessage(Campaign campaign) {
		this.id = campaign.getId();
		this.reachImps = campaign.getReachImps();
		this.dayStart = campaign.getDayStart();
		this.dayEnd = campaign.getDayEnd();
		this.targetSegment = campaign.getTargetSegment();
		this.videoCoef = campaign.getVideoCoef();
		this.mobileCoef = campaign.getMobileCoef();		
	}

	
	public CampaignOpportunityMessage(int id, Long reachImps, int dayStart, int dayEnd,
			int targetSegment, double videoCoef, double mobileCoef) {
				
		this.id = id;
		this.reachImps = reachImps;
		this.dayStart = dayStart;
		this.dayEnd = dayEnd;
		this.targetSegment = targetSegment;
		this.videoCoef = videoCoef;
		this.mobileCoef = mobileCoef;
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

	public int getTargetSegment() {
		return targetSegment;
	}

	public double getVideoCoef() {
		return videoCoef;
	}

	public double getMobileCoef() {
		return mobileCoef;
	}
	
	
	
	public String toString() {
		StringBuffer buf = new StringBuffer().append(getTransportName())
				.append('[').append(id).append(',').append(reachImps).append(',').append(dayStart)
				.append(',').append(dayEnd).append(',').append(targetSegment).
				append(',').append(videoCoef).append(',').append(mobileCoef).append(',');
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
		return "CampaignOpportunity";
	}


	
	public void read(TransportReader reader) throws ParseException {
		if (isLocked()) {
			throw new IllegalStateException("locked");
		}
		id = reader.getAttributeAsInt("id");
		reachImps = reader.getAttributeAsLong("reachImps");
		dayStart = reader.getAttributeAsInt("dayStart");
		dayEnd = reader.getAttributeAsInt("dayEnd");
		targetSegment = reader.getAttributeAsInt("targetSegment");
		videoCoef = reader.getAttributeAsDouble("videoCoef");
		mobileCoef = reader.getAttributeAsDouble("mobileCoef");
		super.read(reader);
	}
	
	
	public void write(TransportWriter writer) {
		writer.attr("id", id).attr("reachImps", reachImps).attr("dayStart", dayStart).attr(
				"dayEnd", dayEnd).attr("targetSegment",targetSegment).
				attr("videoCoef", videoCoef).attr("mobileCoef",mobileCoef);
		super.write(writer);
	}

}
