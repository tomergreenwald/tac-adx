package tau.tac.adx.report.demand;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.tasim.props.SimpleContent;

public class CampaignBidMessage extends SimpleContent {

	private static final long serialVersionUID = -4773426215378916401L;

	private int id;
	private Long budget;
	
	public CampaignBidMessage() {
	}
	
	public CampaignBidMessage(int id, Long budget) {				
		this.id = id;
		this.budget = budget;
	}

	public Long getBudget() {
		return budget;
	}

	public int getId() {
		return id;
	}

	
	public String toString() {
		StringBuffer buf = new StringBuffer().append(getTransportName())
				.append('[').append(id).append(',').append(budget).append(',');
		return params(buf).append(']').toString();
	}

	
	public void read(TransportReader reader) throws ParseException {
		if (isLocked()) {
			throw new IllegalStateException("locked");
		}
		id = reader.getAttributeAsInt("id");
		budget = reader.getAttributeAsLong("budget");
		super.read(reader);
	}
	
	
	public void write(TransportWriter writer) {
		writer.attr("id", id).attr("budget", budget);
		super.write(writer);
	}

	
	@Override
	public String getTransportName() {
		return "CampaignBid";
	}

}
