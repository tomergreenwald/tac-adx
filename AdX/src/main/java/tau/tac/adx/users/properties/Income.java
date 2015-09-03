package tau.tac.adx.users.properties;

import tau.tac.adx.users.AdxUser;

/**
 * A property of the {@link AdxUser}.
 * 
 * @author greenwald
 * 
 */
public enum Income {

	/**
	 * Low income ($0-$30K).
	 */
	low, /**
	 * Medium income ($30-$60K).
	 */
	medium, /**
	 * High income ($60-$100K).
	 */
	high, /**
	 * High income ($100K+).
	 */
	very_high
}
