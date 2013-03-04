package tau.tac.adx.report.demand;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.tasim.props.SimpleContent;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.report.adn.MarketSegment;

public class InitialCampaignMessage extends SimpleContent {
	
	private static final long serialVersionUID = -5447083615716436823L;
	
	private int id;
	private Long reachImps;
	private int dayStart;
	private int dayEnd;
	private MarketSegment targetSegment;
	private String targetSegmentName;
	private double videoCoef;
	private double mobileCoef;
	private String serverId;
	
	public InitialCampaignMessage() {
	}

	public InitialCampaignMessage(Campaign campaign, String address) {
		this.id = campaign.getId();
		this.reachImps = campaign.getReachImps();
		this.dayStart = campaign.getDayStart();
		this.dayEnd = campaign.getDayEnd();
		this.targetSegment = campaign.getTargetSegment();
		this.videoCoef = campaign.getVideoCoef();
		this.mobileCoef = campaign.getMobileCoef();	
		this.serverId = address;
	}

	
	public InitialCampaignMessage(int id, Long reachImps, int dayStart, int dayEnd,
			MarketSegment targetSegment, double videoCoef, double mobileCoef) {
				
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

	public MarketSegment getTargetSegment() {
		return targetSegment;
	}

	public double getVideoCoef() {
		return videoCoef;
	}

	public double getMobileCoef() {
		return mobileCoef;
	}
	
	public String getServerId() {
		return serverId;
	}
	

	
	public String toString() {
		StringBuffer buf = new StringBuffer().append(getTransportName())
				.append('[').append(id).append(',').append(reachImps).append(',').append(dayStart)
				.append(',').append(dayEnd).append(',').append(targetSegment)
				.append(',').append(videoCoef).append(',').append(mobileCoef).append(',')
				.append(serverId).append(',');
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


	
	public void read(TransportReader reader) throws ParseException {
		if (isLocked()) {
			throw new IllegalStateException("locked");
		}
		id = reader.getAttributeAsInt("id");
		reachImps = reader.getAttributeAsLong("reachImps");
		dayStart = reader.getAttributeAsInt("dayStart");
		dayEnd = reader.getAttributeAsInt("dayEnd");
		targetSegmentName = reader.getAttribute("targetSegment");
		targetSegment = MarketSegment.valueOf(targetSegmentName);
		videoCoef = reader.getAttributeAsDouble("videoCoef");
		mobileCoef = reader.getAttributeAsDouble("mobileCoef");
		serverId = reader.getAttribute("serverId");
		super.read(reader);
	}
	
	
	public void write(TransportWriter writer) {
		writer.attr("id", id).attr("reachImps", reachImps).attr("dayStart", dayStart).attr(
				"dayEnd", dayEnd).attr("targetSegment",targetSegment.name()).
				attr("videoCoef", videoCoef).attr("mobileCoef",mobileCoef).attr("serverId",serverId);
		super.write(writer);
	}

	

}
