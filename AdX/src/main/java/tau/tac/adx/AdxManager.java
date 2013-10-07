/**
 * 
 */
package tau.tac.adx;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import tau.tac.adx.bids.Bidder;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.demand.UserClassificationService;
import tau.tac.adx.messages.CampaignNotification;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.sim.TACAdxSimulation;

import com.google.common.eventbus.Subscribe;

/**
 * @author greenwald
 * 
 */
public class AdxManager {

	/**
	 * {@link Map} between publisher names and the coresponding
	 * {@link AdxPublisher}s.
	 */
	private final Map<String, AdxPublisher> publishersNamingMap = new HashMap<String, AdxPublisher>();
	private final Map<Integer, Campaign> campaignMap = new HashMap<Integer, Campaign>();
	private final Map<String, Bidder> bidderMap = new HashMap<String, Bidder>();
	private TACAdxSimulation simulation;
	private static AdxManager instance;
	private UserClassificationService userClassificationService;

	/**
	 * @return the userClassificationService
	 */
	public UserClassificationService getUserClassificationService() {
		return userClassificationService;
	}

	/**
	 * @param userClassificationService
	 *            the userClassificationService to set
	 */
	public void setUserClassificationService(
			UserClassificationService userClassificationService) {
		this.userClassificationService = userClassificationService;
	}

	private final Logger log = Logger.getLogger(AdxManager.class.getName());
	private String adxAgentAddress;

	/**
	 * @return the adxAgentAddress
	 */
	public String getAdxAgentAddress() {
		return adxAgentAddress;
	}

	private AdxManager() {
	}

	public static AdxManager getInstance() {
		if (instance == null) {
			instance = new AdxManager();
		}
		return instance;
	}

	public void setup() {
		simulation.getEventBus().register(this);
	}

	/**
	 * @param publisherName
	 *            {@link AdxPublisher}'s name.
	 * @return Corresponding {@link AdxPublisher}.
	 */
	public AdxPublisher getPublisher(String publisherName) {
		return publishersNamingMap.get(publisherName);
	}

	/**
	 * @return {@link Collection} of all {@link AdxPublisher publishers}.
	 */
	public Collection<AdxPublisher> getPublishers() {
		return publishersNamingMap.values();
	}

	/**
	 * @param publisher
	 *            {@link AdxPublisher} to map.
	 */
	public void addPublisher(AdxPublisher publisher) {
		publishersNamingMap.put(publisher.getName(), publisher);
	}

	@Subscribe
	public void addCampaign(CampaignNotification campaign) {
		campaignMap.put(campaign.getCampaign().getId(), campaign.getCampaign());
	}

	public Campaign getCampaign(int campaignId) {
		return campaignMap.get(campaignId);
	}

	public void addBidder(Bidder bidder) {
		bidderMap.put(bidder.getName(), bidder);
	}

	public Bidder getBidder(int advertiserId) {
		return bidderMap.get(advertiserId);
	}

	public TACAdxSimulation getSimulation() {
		return simulation;
	}

	/**
	 * @param simulation
	 *            the simulation to set
	 */
	public void setSimulation(TACAdxSimulation simulation) {
		this.simulation = simulation;
	}

	public void setAdxAgentAddress(String adxAgentAddress) {
		this.adxAgentAddress = adxAgentAddress;
	}

}
