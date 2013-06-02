package tau.tac.adx.report.adn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;

/**
 * #FIXME change documentation
 * A set of 12 marketing segments is defined by partitioning each attribute
 * range to two (that is, the user�s Age range to Younger = {44-} and Older =
 * {45+} and the user�s income range to Low = {60-} and High = {60+}). Now, a
 * market segment is any choice of ranges of two of the three attributes. If we
 * designate each range by its initial letter (e.g. Female by F and Younger by
 * Y) we get the following 12 market segments: FY, FO, MY, MO, YL, YH, OL, OH,
 * FL, FH, ML, MH. Note that the segments may overlap (i.e., a user may belong
 * to multiple segments). The segments serve as the key elements of defining the
 * advertising campaign targeting goals, as detailed below.
 * 
 * @author greenwald
 * 
 */
public enum MarketSegment {

	MALE, FEMALE, YOUNG, OLD, LOW_INCOME, HIGH_INCOME;
	
	private static MarketSegment[] GENDER_SEGMENTS = {MALE, FEMALE};
	private static MarketSegment[] INCOME_SEGMENTS = {LOW_INCOME, HIGH_INCOME};
	private static MarketSegment[] AGE_SEGMENTS = {YOUNG, OLD};

	private static final Random RANDOM = new Random();

	public static Set<MarketSegment> randomMarketSegment() {
		List<MarketSegment> marketSegments = new ArrayList<MarketSegment>();
		marketSegments.add(GENDER_SEGMENTS[RANDOM.nextInt(GENDER_SEGMENTS.length)]);
		marketSegments.add(INCOME_SEGMENTS[RANDOM.nextInt(INCOME_SEGMENTS.length)]);
		marketSegments.add(AGE_SEGMENTS[RANDOM.nextInt(AGE_SEGMENTS.length)]);
		marketSegments.remove(RANDOM.nextInt(3));
		return new HashSet<MarketSegment>(marketSegments);
	}

	/**
	 * Generates a list of matching {@link MarketSegment}s according to a given
	 * {@link AdxUser user}.
	 * 
	 * @param user
	 *            {@link AdxUser}.
	 * @return {@link List} of {@link MarketSegment}s.
	 */
	public static Set<MarketSegment> extractSegment(AdxUser user) {
		Set<MarketSegment> marketSegments = new HashSet<MarketSegment>();
		// #FIXME change matching for new types
		if (user.getGender() == Gender.male) {
			marketSegments.add(MALE);
		} else {
			marketSegments.add(FEMALE);
		}
		if (user.getIncome() == Income.low || user.getIncome() == Income.medium) {
			marketSegments.add(LOW_INCOME);
		} else {
			marketSegments.add(HIGH_INCOME);
		}
		if (user.getAge() == Age.Age_18_24 || user.getAge() == Age.Age_25_34
				|| user.getAge() == Age.Age_35_44) {
			marketSegments.add(MarketSegment.YOUNG);
		} else {
			marketSegments.add(MarketSegment.OLD);
		}
		return marketSegments;
	}

}
