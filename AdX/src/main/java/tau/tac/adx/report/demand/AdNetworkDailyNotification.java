package tau.tac.adx.report.demand;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.tasim.props.SimpleContent;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.demand.UserClassificationServiceAdNetData;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class AdNetworkDailyNotification extends SimpleContent {

	/**
	 * 
	 */
	public AdNetworkDailyNotification() {
		super();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -2893212570481112391L;

	/* user classification service data */
	int effectiveDay;
	double serviceLevel;
	double price;

	/* quality score data */
	double qualityScore;

	/* campaign allocation data */
	int campaignId;
	String winner;
	long costMillis;

	public AdNetworkDailyNotification(int effectiveDay, double serviceLevel,
			double price, double qualityScore, int campaignId, String winner,
			long costMillis) {
		this.effectiveDay = effectiveDay;
		this.serviceLevel = serviceLevel;
		this.price = price;
		this.qualityScore = qualityScore;
		this.campaignId = campaignId;
		this.winner = winner;
		this.costMillis = costMillis;
	}

	public AdNetworkDailyNotification(
			UserClassificationServiceAdNetData ucsData, Campaign campaign,
			double qualityScore) {

		this.qualityScore = qualityScore;

		if (ucsData != null) {
			this.effectiveDay = ucsData.getEffectiveDay();
			this.serviceLevel = ucsData.getServiceLevel();
			this.price = ucsData.getPrice();
		} else {
			this.effectiveDay = 0;
		}

		if (campaign != null) {
			this.campaignId = campaign.getId();
			this.winner = campaign.getAdvertiser();
			this.costMillis = campaign.getBudgetMillis();
		} else {
			this.campaignId = 0;
			this.winner = "NONE";
			this.costMillis = 0;			
		}
	}

	public int getEffectiveDay() {
		return effectiveDay;
	}

	public double getServiceLevel() {
		return serviceLevel;
	}

	public double getPrice() {
		return price;
	}

	public double getQualityScore() {
		return qualityScore;
	}

	public int getCampaignId() {
		return campaignId;
	}

	public String getWinner() {
		return winner;
	}

	public long getCostMillis() {
		return costMillis;
	}

	public void zeroCost() {
		costMillis = 0;
	}

	@Override
	public void read(TransportReader reader) throws ParseException {
		if (isLocked()) {
			throw new IllegalStateException("locked");
		}
		effectiveDay = reader.getAttributeAsInt("effectiveDay");
		serviceLevel = reader.getAttributeAsDouble("serviceLevel");
		price = reader.getAttributeAsDouble("price");
		qualityScore = reader.getAttributeAsDouble("qualityScore");
		campaignId = reader.getAttributeAsInt("campaignId");
		winner = reader.getAttribute("winner");
		costMillis = reader.getAttributeAsLong("costMillis");

		super.read(reader);
	}

	@Override
	public void write(TransportWriter writer) {
		writer.attr("effectiveDay", effectiveDay)
				.attr("serviceLevel", serviceLevel).attr("price", price)
				.attr("qualityScore", qualityScore)
				.attr("campaignId", campaignId).attr("winner", winner)
				.attr("costMillis", costMillis);
		super.write(writer);
	}

	@Override
	public String getTransportName() {
		return getClass().getName();
	}

	@Override
	public String toString() {
		return "AdNetworkDailyNotification [effectiveDay=" + effectiveDay
				+ ", serviceLevel=" + serviceLevel + ", price=" + price
				+ ", qualityScore=" + qualityScore + ", campaignId="
				+ campaignId + ", winner=" + winner + ", costMillis=" + costMillis + "]";
	}

}
