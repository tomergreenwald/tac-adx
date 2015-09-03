package tau.tac.adx.report.adn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import tau.tac.adx.users.AdxUser;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;

/**
 * A Set of MarketSegment elements is used to define the target of an 
 * advertising campaign. As detailed below such a set contains two elements.
 * Also, a Set of MarketSegment elements is used to define a bidding
 * policy entry in a bid bundle - such set may contain any number of elements.    
 * 
 * A set of marketing segments is defined by partitioning each attribute
 * range to two (that is, the user's Age range to Younger = {44-} and Older =
 * {45+} and the user's income range to Low = {60-} and High = {60+}). Now, a
 * market segment is any choice of ranges of one, two, or all of the three attributes. 
 * For example, if we designate each range by its initial letter (e.g. Female by F and 
 * Younger by Y) we get the following 12 dual-attribute market segments: 
 * FY, FO, MY, MO, YL, YH, OL, OH, FL, FH, ML, MH. 
 * Note that the segments may overlap (i.e., a user may belong
 * to multiple segments). The segments serve as the key elements of defining the
 * advertising campaign targeting goals.
 * 
 * @author greenwald
 * 
 */
public enum MarketSegment {

	MALE, FEMALE, YOUNG, OLD, LOW_INCOME, HIGH_INCOME;
	
	private static final Random RANDOM = new Random();

	public static Set<MarketSegment> compundMarketSegment1(MarketSegment s1) {
		List<MarketSegment> marketSegments = new ArrayList<MarketSegment>();
		marketSegments.add(s1);
		return new HashSet<MarketSegment>(marketSegments);
	}
	
	public static Set<MarketSegment> compundMarketSegment2(MarketSegment s1, MarketSegment s2) {
		List<MarketSegment> marketSegments = new ArrayList<MarketSegment>();
		marketSegments.add(s1);
		marketSegments.add(s2);
		return new HashSet<MarketSegment>(marketSegments);
	}

	public static Set<MarketSegment> compundMarketSegment3(MarketSegment s1, MarketSegment s2, MarketSegment s3) {
		List<MarketSegment> marketSegments = new ArrayList<MarketSegment>();
		marketSegments.add(s1);
		marketSegments.add(s2);
		marketSegments.add(s3);
		return new HashSet<MarketSegment>(marketSegments);
	}
	//                                         0:MLY                   1:MLO                    2:FLY               3:FLO                    4:MHY                5:MHO                   6:FHY            7:FHO	
	private static int[] uc = {526+263+371+71+322+283, 290+284+461+280+245+235, 546+460+403+52+264+255,457+450+827+275+228+164, 11+140+185+5+51+125, 197+157+103+163+121+67,6+75+104+3+21+47, 122+109+53+57+48+18};
	
	public static Map<Set<MarketSegment>,Integer> usersInMarketSegments() {
		//List<Set<MarketSegment>> cmarketSegments = new ArrayList<Set<MarketSegment>>();
		Map<Set<MarketSegment>, Integer> cmarketSegments = new HashMap<Set<MarketSegment>, Integer>();
		cmarketSegments.put(compundMarketSegment1(FEMALE), uc[2]+uc[3]+uc[6]+uc[7]);
		cmarketSegments.put(compundMarketSegment1(MALE), uc[0]+uc[1]+uc[4]+uc[5]);
		cmarketSegments.put(compundMarketSegment1(YOUNG),uc[0]+uc[2]+uc[4]+uc[6]);
		cmarketSegments.put(compundMarketSegment1(OLD),uc[1]+uc[3]+uc[5]+uc[7]);
		cmarketSegments.put(compundMarketSegment1(LOW_INCOME),uc[0]+uc[1]+uc[2]+uc[3]);
		cmarketSegments.put(compundMarketSegment1(HIGH_INCOME),uc[4]+uc[5]+uc[6]+uc[7]);
		cmarketSegments.put(compundMarketSegment2(FEMALE,YOUNG), uc[2]+uc[6]);
		cmarketSegments.put(compundMarketSegment2(FEMALE,OLD),uc[3]+uc[7]);
		cmarketSegments.put(compundMarketSegment2(MALE,YOUNG),uc[0]+uc[4]);
		cmarketSegments.put(compundMarketSegment2(MALE,OLD),uc[1]+uc[5]);
		cmarketSegments.put(compundMarketSegment2(FEMALE,LOW_INCOME),uc[2]+uc[3]);
		cmarketSegments.put(compundMarketSegment2(FEMALE,HIGH_INCOME),uc[6]+uc[7]);
		cmarketSegments.put(compundMarketSegment2(MALE,LOW_INCOME),uc[0]+uc[1]);
		cmarketSegments.put(compundMarketSegment2(MALE,HIGH_INCOME),uc[4]+uc[5]);
		cmarketSegments.put(compundMarketSegment2(YOUNG,LOW_INCOME),uc[0]+uc[2]);
		cmarketSegments.put(compundMarketSegment2(YOUNG,HIGH_INCOME),uc[4]+uc[6]);
		cmarketSegments.put(compundMarketSegment2(OLD,LOW_INCOME),uc[1]+uc[3]);
		cmarketSegments.put(compundMarketSegment2(OLD,HIGH_INCOME),uc[5]+uc[7]);
		cmarketSegments.put(compundMarketSegment3(FEMALE,LOW_INCOME,YOUNG),uc[2]);
		cmarketSegments.put(compundMarketSegment3(FEMALE,LOW_INCOME,OLD),uc[3]);
		cmarketSegments.put(compundMarketSegment3(MALE,LOW_INCOME, YOUNG),uc[0]);
		cmarketSegments.put(compundMarketSegment3(MALE,LOW_INCOME, OLD),uc[1]);
		cmarketSegments.put(compundMarketSegment3(FEMALE,HIGH_INCOME,YOUNG),uc[6]);
		cmarketSegments.put(compundMarketSegment3(FEMALE,HIGH_INCOME,OLD),uc[7]);
		cmarketSegments.put(compundMarketSegment3(MALE,HIGH_INCOME, YOUNG),uc[4]);
		cmarketSegments.put(compundMarketSegment3(MALE,HIGH_INCOME, OLD),uc[5] );
		return cmarketSegments;
	}

	public static List<Set<MarketSegment>> marketSegments() {
		List<Set<MarketSegment>> cmarketSegments = new ArrayList<Set<MarketSegment>>();
		cmarketSegments.add(compundMarketSegment1(FEMALE));
		cmarketSegments.add(compundMarketSegment1(MALE));
		cmarketSegments.add(compundMarketSegment1(YOUNG));
		cmarketSegments.add(compundMarketSegment1(OLD));
		cmarketSegments.add(compundMarketSegment1(LOW_INCOME));
		cmarketSegments.add(compundMarketSegment1(HIGH_INCOME));
		cmarketSegments.add(compundMarketSegment2(FEMALE,YOUNG));
		cmarketSegments.add(compundMarketSegment2(FEMALE,OLD));
		cmarketSegments.add(compundMarketSegment2(MALE,YOUNG));
		cmarketSegments.add(compundMarketSegment2(MALE,OLD));
		cmarketSegments.add(compundMarketSegment2(FEMALE,LOW_INCOME));
		cmarketSegments.add(compundMarketSegment2(FEMALE,HIGH_INCOME));
		cmarketSegments.add(compundMarketSegment2(MALE,LOW_INCOME));
		cmarketSegments.add(compundMarketSegment2(MALE,HIGH_INCOME));
		cmarketSegments.add(compundMarketSegment2(YOUNG,LOW_INCOME));
		cmarketSegments.add(compundMarketSegment2(YOUNG,HIGH_INCOME));
		cmarketSegments.add(compundMarketSegment2(OLD,LOW_INCOME));
		cmarketSegments.add(compundMarketSegment2(OLD,HIGH_INCOME));
		cmarketSegments.add(compundMarketSegment3(FEMALE,LOW_INCOME,YOUNG));
		cmarketSegments.add(compundMarketSegment3(FEMALE,LOW_INCOME,OLD));
		cmarketSegments.add(compundMarketSegment3(MALE,LOW_INCOME, YOUNG));
		cmarketSegments.add(compundMarketSegment3(MALE,LOW_INCOME, OLD));
		cmarketSegments.add(compundMarketSegment3(FEMALE,HIGH_INCOME,YOUNG));
		cmarketSegments.add(compundMarketSegment3(FEMALE,HIGH_INCOME,OLD));
		cmarketSegments.add(compundMarketSegment3(MALE,HIGH_INCOME, YOUNG));
		cmarketSegments.add(compundMarketSegment3(MALE,HIGH_INCOME, OLD));
		return cmarketSegments;
	}

	private static  Map<Set<MarketSegment>,Integer> segmentsUsersMap = usersInMarketSegments();
	private static  List<Set<MarketSegment>>        segmentsList = marketSegments();
	
	public static Set<MarketSegment> randomMarketSegment() {
		return 	segmentsList.get(RANDOM.nextInt(segmentsList.size()));
	}	

	public static Set<MarketSegment> randomMarketSegment2() {
		return 	segmentsList.get(6+RANDOM.nextInt(12));
	}	

	public static Integer marketSegmentSize(Set<MarketSegment> segment) {
		return 	segmentsUsersMap.get(segment);
	}	
	
	public static String names(Set<MarketSegment> segments) {
		String ret = new String();
		for (MarketSegment segment : segments) {
			ret += " "+segment.name();
		}
		return ret;
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
