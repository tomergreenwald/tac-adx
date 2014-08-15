/**
 * 
 */
package tau.tac.adx.sim.config;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import se.sics.isl.util.ConfigManager;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.props.PublisherCatalogEntry;

/**
 * @author Tomer
 *
 */
public class AdxConfigurationParserTest {

	/**
	 * Test method for {@link tau.tac.adx.sim.config.AdxConfigurationParser#createPublisherCatalog()}.
	 */
	@Test
	public void testCreatePublisherCatalog() {
		ConfigManager configManager = new ConfigManager();
		configManager.loadConfiguration("adx-server/config/tac13adx_sim.conf");		
		AdxConfigurationParser parser = new AdxConfigurationParser(configManager);
		PublisherCatalog publisherCatalog = parser.createPublisherCatalog();
		List<PublisherCatalogEntry> publishers = publisherCatalog.getPublishers();
		Assert.assertEquals(6, publishers.size());
		Set<String> names = new HashSet<String>();
		for(PublisherCatalogEntry entry : publishers) {
			assertTrue(names.add(entry.getPublisherName()));
		}
	}

}
