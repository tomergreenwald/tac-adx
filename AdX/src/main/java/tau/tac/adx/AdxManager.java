/**
 * 
 */
package tau.tac.adx;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import tau.tac.adx.bids.Bidder;
import tau.tac.adx.demand.Campaign;
import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.sim.TACAdxSimulation;

/**
 * @author greenwald
 * 
 */
public class AdxManager {

	/**
	 * {@link Map} between publisher names and the coresponding
	 * {@link AdxPublisher}s.
	 */
	private static Map<String, AdxPublisher> publishersNamingMap = new HashMap<String, AdxPublisher>();
	private static Map<Integer, Campaign> campaignMap = new HashMap<Integer, Campaign>();
	private static Map<Integer, Bidder> bidderMap = new HashMap<Integer, Bidder>();
	private static TACAdxSimulation simulation;

	/**
	 * @param publisherName
	 *            {@link AdxPublisher}'s name.
	 * @return Corresponding {@link AdxPublisher}.
	 */
	public static AdxPublisher getPublisher(String publisherName) {
		return publishersNamingMap.get(publisherName);
	}

	/**
	 * @return {@link Collection} of all {@link AdxPublisher publishers}.
	 */
	public static Collection<AdxPublisher> getPublishers() {
		return publishersNamingMap.values();
	}

	/**
	 * @param publisher
	 *            {@link AdxPublisher} to map.
	 */
	public static void addPublisher(AdxPublisher publisher) {
		publishersNamingMap.put(publisher.getName(), publisher);
	}

	public static void addCampaign(Campaign campaign) {
		campaignMap.put(campaign.getId(), campaign);
	}

	public static Campaign getCampaign(int campaignId) {
		return campaignMap.get(campaignId);
	}

	public static void addBidder(Bidder bidder) {
		bidderMap.put(bidder.getId(), bidder);
	}

	public static Bidder getBidder(int advertiserId) {
		return bidderMap.get(advertiserId);
	}

	public static TACAdxSimulation getSimulation() {
		return simulation;
	}

	/**
	 * @param simulation
	 *            the simulation to set
	 */
	public static void setSimulation(TACAdxSimulation simulation) {
		AdxManager.simulation = simulation;
	}

}
