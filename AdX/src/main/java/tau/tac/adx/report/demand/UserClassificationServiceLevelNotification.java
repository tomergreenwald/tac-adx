package tau.tac.adx.report.demand;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.tasim.props.SimpleContent;
import tau.tac.adx.demand.UserClassificationServiceAdNetData;

public class UserClassificationServiceLevelNotification extends SimpleContent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2893212570481112391L;

	int    effectiveDay;
	double serviceLevel;
	double price;

	public UserClassificationServiceLevelNotification(int effectiveDay, double serviceLevel, double price) {
		this.effectiveDay = effectiveDay;
		this.serviceLevel = serviceLevel;
		this.price = price;		
	}
	
	public UserClassificationServiceLevelNotification(UserClassificationServiceAdNetData ucsData) {
		this.effectiveDay = ucsData.getEffectiveDay();
		this.serviceLevel = ucsData.getServiceLevel();
		this.price = ucsData.getPrice();				
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
	
	public String toString() {
		StringBuffer buf = new StringBuffer().append(getTransportName())
				.append('[').append(effectiveDay).append(',').append(serviceLevel).append(',').append(price).
				append(',');
		return params(buf).append(']').toString();
	}

	public void read(TransportReader reader) throws ParseException {
		if (isLocked()) {
			throw new IllegalStateException("locked");
		}
		effectiveDay = reader.getAttributeAsInt("effectiveDay");
		serviceLevel = reader.getAttributeAsDouble("serviceLevel");
		price = reader.getAttributeAsDouble("price");
		super.read(reader);
	}
	
	
	public void write(TransportWriter writer) {
		writer.attr("effectiveDay", effectiveDay).attr("serviceLevel", serviceLevel).attr("price", price);
		super.write(writer);
	}

	
	@Override
	public String getTransportName() {
		return "UserClassificationServiceBidNotification";
	}

}
