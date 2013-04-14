package tau.tac.adx.demand;

/**
 * 
 * @author Mariano Schain
 * 
 */
public interface UserClassificationService {

	public void updateAdvertiserBid(String advertiser, double ucsBid, int day);

	public UserClassificationServiceAdNetData getAdNetData(String advertiser);

	public void auction(int day);
}
