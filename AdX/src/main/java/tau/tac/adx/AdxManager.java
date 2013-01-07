/**
 * 
 */
package tau.tac.adx;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import tau.tac.adx.publishers.AdxPublisher;
import tau.tac.adx.sim.TACAdxConstants;
import tau.tac.adx.sim.TACAdxSimulation;

import com.google.common.eventbus.EventBus;

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
	private static TACAdxSimulation simulation;

	/**
	 * The system's main event bus.
	 */
	private static EventBus eventBus = new EventBus(
			TACAdxConstants.ADX_EVENT_BUS_NAME);

	/**
	 * @return the eventBus
	 */
	public static EventBus getEventBus() {
		return eventBus;
	}

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
