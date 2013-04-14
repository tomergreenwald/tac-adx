package tau.tac.adx.demand;

/**
 * 
 * @author mariano Maintains quality rating of a set of advertisers
 */

public interface QualityManager {

	/**
	 * Adds an advertiser to the set of advertisers whose rating is tracked
	 * 
	 * @param advertiser
	 */
	void addAdvertiser(String advertiser);

	/**
	 * Update the quality score with the results of a campaign
	 * 
	 * @param advertiser
	 * @param score
	 * @return updated score
	 */
	double updateQualityScore(String advertiser, Double score);

	/**
	 * Fetch score of advertiser
	 * 
	 * @param advertiser
	 * @return current quality score
	 */
	double getQualityScore(String advertiser);
}
