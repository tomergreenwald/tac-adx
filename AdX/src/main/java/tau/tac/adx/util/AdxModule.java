/**
 * 
 */
package tau.tac.adx.util;

import java.util.Collection;

import tau.tac.adx.ads.properties.generators.AdTypeGenerator;
import tau.tac.adx.ads.properties.generators.SimpleAdTypeGenerator;
import tau.tac.adx.auction.AuctionManager;
import tau.tac.adx.auction.SimpleAuctionManager;
import tau.tac.adx.auction.manager.AdxBidManager;
import tau.tac.adx.auction.manager.AdxBidManagerImpl;
import tau.tac.adx.auction.tracker.AdxBidTracker;
import tau.tac.adx.auction.tracker.AdxBidTrackerImpl;
import tau.tac.adx.auction.tracker.AdxSpendTracker;
import tau.tac.adx.auction.tracker.AdxSpendTrackerImpl;
import tau.tac.adx.devices.generators.DeviceGenerator;
import tau.tac.adx.devices.generators.SimpleDeviceGenerator;
import tau.tac.adx.props.generators.AdxQueryGenerator;
import tau.tac.adx.props.generators.SimpleAdxQueryGenerator;
import tau.tac.adx.publishers.generators.AdxPublisherGenerator;
import tau.tac.adx.publishers.generators.SimplePublisherGenerator;
import tau.tac.adx.sim.AdxAuctioneer;
import tau.tac.adx.sim.SimpleAdxAuctioneer;
import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.generators.AdxUserGenerator;
import tau.tac.adx.users.generators.SimpleUserGenerator;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * A simple {@link AbstractModule} implementation used for testing. All vlaues
 * are generated randomly.
 * 
 * @author greenwald
 * 
 */
public class AdxModule extends AbstractModule {

	/**
	 * Generates the {@link Collection} of {@link AdxUser users} that will be
	 * used across the {@link AdX} system.<br>
	 * The collection will be a {@link Singleton}.
	 * 
	 * @return A {@link Collection} of {@link AdxUser users}.
	 */
	@Provides
	@Singleton
	public Collection<AdxUser> getUesrs() {
		SimpleUserGenerator userGenerator = new SimpleUserGenerator();
		Collection<AdxUser> users = userGenerator
				.generate(TestConstants.USER_COUNT);
		return users;
	}

	/**
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(AdxQueryGenerator.class).to(SimpleAdxQueryGenerator.class);
		bind(AdxUserGenerator.class).to(SimpleUserGenerator.class);
		bind(AdxPublisherGenerator.class).to(SimplePublisherGenerator.class);
		bind(AdTypeGenerator.class).to(SimpleAdTypeGenerator.class);
		bind(DeviceGenerator.class).to(SimpleDeviceGenerator.class);
		bind(AdxAuctioneer.class).to(SimpleAdxAuctioneer.class).in(
				Singleton.class);
		bind(AdxBidManager.class).to(AdxBidManagerImpl.class).in(
				Singleton.class);
		bind(AdxSpendTracker.class).to(AdxSpendTrackerImpl.class).in(
				Singleton.class);
		bind(AdxBidTracker.class).to(AdxBidTrackerImpl.class).in(
				Singleton.class);
		bind(AuctionManager.class).to(SimpleAuctionManager.class).in(
				Singleton.class);
	}
}
