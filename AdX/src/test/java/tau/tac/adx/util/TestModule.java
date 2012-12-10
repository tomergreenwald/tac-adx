/**
 * 
 */
package tau.tac.adx.util;

import java.util.Collection;

import tau.tac.adx.users.AdxUser;
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
public class TestModule extends AbstractModule {

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

	@Override
	protected void configure() {
		// TODO Auto-generated method stub
		
	}

}
