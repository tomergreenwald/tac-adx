package tau.tac.adx.report.adn;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;

/**
 * A set of 12 marketing segments is defined by partitioning each attribute
 * range to two (that is, the user’s Age range to Younger = {44-} and Older =
 * {45+} and the user’s income range to Low = {60-} and High = {60+}). Now, a
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

	FEMALE_YOUNG, FEMALE_OLD, MALE_YOUNG, MALE_OLD, YOUNG_HIGH_INCOME, OLD_HIGH_INCOME, YOUNG_LOW_INCOME, OLD_LOW_INCOME, FEMALE_LOW_INCOME, FEMALE_HIGH_INCOME, MALE_HIGH_INCOME, MALE_LOW_INCOME, NONE;

	/**
	 * A random MarketSegment on demand. The values and the Random are cached.
	 */
	private static final List<MarketSegment> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
	private static final Random RANDOM = new Random();
	
	public static MarketSegment randomMarketSegment()  {
	    return VALUES.get(RANDOM.nextInt(SIZE));
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
		if (user.getGender() == Gender.male) {
			if (user.getIncome() == Income.low
					|| user.getIncome() == Income.medium) {
				marketSegments.add(MarketSegment.MALE_LOW_INCOME);
			} else {
				marketSegments.add(MarketSegment.MALE_HIGH_INCOME);
			}
			if (user.getAge() == Age.Age_18_24
					|| user.getAge() == Age.Age_25_34
					|| user.getAge() == Age.Age_35_44) {
				marketSegments.add(MarketSegment.MALE_YOUNG);
			} else {
				marketSegments.add(MarketSegment.MALE_OLD);
			}
		} else {
			if (user.getIncome() == Income.low
					|| user.getIncome() == Income.medium) {
				marketSegments.add(MarketSegment.FEMALE_LOW_INCOME);
			} else {
				marketSegments.add(MarketSegment.FEMALE_HIGH_INCOME);
			}
			if (user.getAge() == Age.Age_18_24
					|| user.getAge() == Age.Age_25_34
					|| user.getAge() == Age.Age_35_44) {
				marketSegments.add(MarketSegment.FEMALE_YOUNG);
			} else {
				marketSegments.add(MarketSegment.FEMALE_OLD);
			}
		}
		if (user.getIncome() == Income.low || user.getIncome() == Income.medium) {
			if (user.getAge() == Age.Age_18_24
					|| user.getAge() == Age.Age_25_34
					|| user.getAge() == Age.Age_35_44) {
				marketSegments.add(MarketSegment.YOUNG_LOW_INCOME);
			} else {
				marketSegments.add(MarketSegment.OLD_LOW_INCOME);
			}
		} else {
			if (user.getAge() == Age.Age_18_24
					|| user.getAge() == Age.Age_25_34
					|| user.getAge() == Age.Age_35_44) {
				marketSegments.add(MarketSegment.YOUNG_HIGH_INCOME);
			} else {
				marketSegments.add(MarketSegment.OLD_HIGH_INCOME);
			}
		}
		return marketSegments;
	}
	
}
