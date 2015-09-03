package tau.tac.adx.report.adn;

import java.text.ParseException;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import se.sics.isl.transport.Transportable;
import tau.tac.adx.ads.properties.AdType;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.devices.Device;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;

/**
 * {@link AdNetworkReport}'s key. Each is a single combination of properties to
 * holds data for in the <b>report</b>.
 * 
 * @author greenwald
 * 
 */
public class AdNetworkKey implements Transportable {

	/** PUBLISHER_KEY. */
	private static final String PUBLISHER_KEY = "PUBLISHER_KEY";
	/** DEVICE_KEY. */
	private static final String DEVICE_KEY = "DEVICE_KEY";
	/** AD_TYPE_KEY. */
	private static final String AD_TYPE_KEY = "AD_TYPE_KEY";
	private static final String GENDER_TYPE_KEY = "GENDER_TYPE_KEY";
	private static final String INCOME_TYPE_KEY = "INCOME_TYPE_KEY";
	private static final String AGE_TYPE_KEY = "AGE_TYPE_KEY";
	private static final String CMAPAIGN_ID_TYPE_KEY = "CMAPAIGN_ID_TYPE_KEY";

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AdNetworkKey [age=" + age + ", income=" + income + ", gender="
				+ gender + ", publisher=" + publisher + ", device=" + device
				+ ", adType=" + adType + ", campaignId=" + campaignId + "]";
	}

	/**
	 * {@link Age}.
	 */
	private Age age;

	/**
	 * {@link Income}.
	 */
	private Income income;

	/**
	 * {@link Gender}.
	 */
	private Gender gender;

	/**
	 * {@link AdxPublisher}'s name.
	 */
	private String publisher;

	/**
	 * {@link Device}.
	 */
	private Device device;

	/**
	 * {@link AdType}.
	 */
	private AdType adType;

	/**
	 * {@link Campaign#getId()}.
	 */
	private int campaignId;

	/**
	 * @param segment
	 *            {@link MarketSegment}.
	 * @param publisher
	 *            {@link AdxPublisher}'s name.
	 * @param device
	 *            {@link Device}.
	 * @param adType
	 *            {@link AdType}.
	 * @param campaignId
	 */
	public AdNetworkKey(AdxUser adxUser, String publisher, Device device,
			AdType adType, int campaignId) {
		super();
		this.age = adxUser.getAge();
		this.income = adxUser.getIncome();
		this.gender = adxUser.getGender();
		this.publisher = publisher;
		this.device = device;
		this.adType = adType;
		this.campaignId = campaignId;
	}

	public AdNetworkKey() {
	}

	/**
	 * @return the age
	 */
	public Age getAge() {
		return age;
	}

	/**
	 * @param age
	 *            the age to set
	 */
	public void setAge(Age age) {
		this.age = age;
	}

	/**
	 * @return the income
	 */
	public Income getIncome() {
		return income;
	}

	/**
	 * @param income
	 *            the income to set
	 */
	public void setIncome(Income income) {
		this.income = income;
	}

	/**
	 * @return the gender
	 */
	public Gender getGender() {
		return gender;
	}

	/**
	 * @param gender
	 *            the gender to set
	 */
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	/**
	 * @return the publisher
	 */
	public String getPublisher() {
		return publisher;
	}

	/**
	 * @param publisher
	 *            the publisher to set
	 */
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	/**
	 * @return the device
	 */
	public Device getDevice() {
		return device;
	}

	/**
	 * @param device
	 *            the device to set
	 */
	public void setDevice(Device device) {
		this.device = device;
	}

	/**
	 * @return the adType
	 */
	public AdType getAdType() {
		return adType;
	}

	/**
	 * @param adType
	 *            the adType to set
	 */
	public void setAdType(AdType adType) {
		this.adType = adType;
	}

	/**
	 * @return the campaignId
	 */
	public int getCampaignId() {
		return campaignId;
	}

	/**
	 * @param campaignId
	 *            the campaignId to set
	 */
	public void setCampaignId(int campaignId) {
		this.campaignId = campaignId;
	}

	/**
	 * @see se.sics.isl.transport.Transportable#getTransportName()
	 */
	@Override
	public String getTransportName() {
		return getClass().getSimpleName();
	}

	/**
	 * @see se.sics.isl.transport.Transportable#read(se.sics.isl.transport.TransportReader)
	 */
	@Override
	public void read(TransportReader reader) throws ParseException {
		publisher = reader.getAttribute(PUBLISHER_KEY, null);
		device = Device.valueOf(reader.getAttribute(DEVICE_KEY, null));
		adType = AdType.valueOf(reader.getAttribute(AD_TYPE_KEY, null));
		gender = Gender.valueOf(reader.getAttribute(GENDER_TYPE_KEY, null));
		income = Income.valueOf(reader.getAttribute(INCOME_TYPE_KEY, null));
		age = Age.valueOf(reader.getAttribute(AGE_TYPE_KEY, null));
		campaignId = reader.getAttributeAsInt(CMAPAIGN_ID_TYPE_KEY, -1);

	}

	/**
	 * @see se.sics.isl.transport.Transportable#write(se.sics.isl.transport.TransportWriter)
	 */
	@Override
	public void write(TransportWriter writer) {
		if (publisher != null) {
			writer.attr(PUBLISHER_KEY, publisher);
		}
		if (device != null) {
			writer.attr(DEVICE_KEY, device.toString());
		}
		if (adType != null) {
			writer.attr(AD_TYPE_KEY, adType.toString());
		}
		if (gender != null) {
			writer.attr(GENDER_TYPE_KEY, gender.toString());
		}
		if (income != null) {
			writer.attr(INCOME_TYPE_KEY, income.toString());
		}
		if (age != null) {
			writer.attr(AGE_TYPE_KEY, age.toString());
		}
		writer.attr(CMAPAIGN_ID_TYPE_KEY, campaignId);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((adType == null) ? 0 : adType.hashCode());
		result = prime * result + ((age == null) ? 0 : age.hashCode());
		result = prime * result + campaignId;
		result = prime * result + ((device == null) ? 0 : device.hashCode());
		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
		result = prime * result + ((income == null) ? 0 : income.hashCode());
		result = prime * result
				+ ((publisher == null) ? 0 : publisher.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdNetworkKey other = (AdNetworkKey) obj;
		if (adType != other.adType)
			return false;
		if (age != other.age)
			return false;
		if (campaignId != other.campaignId)
			return false;
		if (device != other.device)
			return false;
		if (gender != other.gender)
			return false;
		if (income != other.income)
			return false;
		if (publisher == null) {
			if (other.publisher != null)
				return false;
		} else if (!publisher.equals(other.publisher))
			return false;
		return true;
	}

}
