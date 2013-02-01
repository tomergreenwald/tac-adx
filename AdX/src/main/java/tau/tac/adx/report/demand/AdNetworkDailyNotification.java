package tau.tac.adx.report.demand;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.tasim.props.SimpleContent;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.demand.UserClassificationServiceAdNetData;

public class AdNetworkDailyNotification extends SimpleContent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2893212570481112391L;

	/* user classification service data */
	int effectiveDay;
	double serviceLevel;
	double price;

	/* campaign allocation data */
	int campaignId;
	String winner;
	double cost;

	public AdNetworkDailyNotification(int effectiveDay, double serviceLevel,
			double price, int campaignId, String winner, long cost) {
		this.effectiveDay = effectiveDay;
		this.serviceLevel = serviceLevel;
		this.price = price;
		this.campaignId = campaignId;
		this.winner = winner;
		this.cost = cost;
	}

	public AdNetworkDailyNotification(
			UserClassificationServiceAdNetData ucsData, Campaign campaign) {
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
			this.cost = campaign.getBudget()==null?0:campaign.getBudget();
		} else {
			this.campaignId = 0;			
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

	public int getCampaignId() {
		return campaignId;
	}

	public String getWinner() {
		return winner;
	}

	public double getCost() {
		return cost;
	}


	public void read(TransportReader reader) throws ParseException {
		if (isLocked()) {
			throw new IllegalStateException("locked");
		}
		effectiveDay = reader.getAttributeAsInt("effectiveDay");
		serviceLevel = reader.getAttributeAsDouble("serviceLevel");
		price = reader.getAttributeAsDouble("price");
		campaignId = reader.getAttributeAsInt("campaignId");
		winner = reader.getAttribute("winner");
		cost = reader.getAttributeAsDouble("cost");

		super.read(reader);
	}

	public void write(TransportWriter writer) {
		writer.attr("effectiveDay", effectiveDay)
				.attr("serviceLevel", serviceLevel).attr("price", price)
				.attr("campaignId", campaignId).attr("winner", winner)
				.attr("cost", cost);
		super.write(writer);
	}

	@Override
	public String getTransportName() {
		return "UserClassificationServiceBidNotification";
	}

	@Override
	public String toString() {
		return "AdNetworkDailyNotification [effectiveDay=" + effectiveDay
				+ ", serviceLevel=" + serviceLevel + ", price=" + price
				+ ", campaignId=" + campaignId + ", winner=" + winner
				+ ", cost=" + cost + "]";
	}

}
