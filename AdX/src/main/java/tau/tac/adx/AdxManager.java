/**
 * 
 */
package tau.tac.adx;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import tau.tac.adx.publishers.AdxPublisher;

/**
 * Adx managing class.
 * 
 * @author greenwald
 * 
 */
public class AdxManager {

	/**
	 * {@link Map} between publisher names and the coresponding
	 * {@link AdxPublisher}s.
	 */
	private static Map<String, AdxPublisher> publishersNamingMap = new HashMap<String, AdxPublisher>();

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
}
