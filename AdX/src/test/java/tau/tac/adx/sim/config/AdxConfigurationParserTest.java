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
		ConfigManager config = mock(ConfigManager.class);
		when(config.getPropertyAsArray("publishers.list.1")).thenReturn(new String[]{"0","1","2","3","4","5"});
		when(config.getPropertyAsArray("publishers.list.2")).thenReturn(new String[]{"6","7","8","9","10","11"});
		when(config.getPropertyAsArray("publishers.list.3")).thenReturn(new String[]{"12","13","14","15","16","17"});
		when(config.getPropertyAsInt("publishers.subset.size", 2)).thenReturn(2);
		
		AdxConfigurationParser parser = new AdxConfigurationParser(config);
		PublisherCatalog publisherCatalog = parser.createPublisherCatalog();
		List<PublisherCatalogEntry> publishers = publisherCatalog.getPublishers();
		Assert.assertEquals(6, publishers.size());
		Set<String> names = new HashSet<String>();
		for(PublisherCatalogEntry entry : publishers) {
			assertTrue(names.add(entry.getPublisherName()));
		}
	}

}
