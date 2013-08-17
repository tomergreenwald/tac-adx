package tau.tac.adx.demand;

import static edu.umich.eecs.tac.auction.AuctionUtils.hardSort;

import java.util.HashMap;
import java.util.Map;

import tau.tac.adx.AdxManager;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class UserClassificationServiceImpl implements UserClassificationService {

	private final static double UCS_PROB = 0.9;

	private final Map<String, UserClassificationServiceAdNetData> advertisersData = new HashMap<String, UserClassificationServiceAdNetData>();

	@Override
	public void updateAdvertiserBid(String advertiser, double ucsBid, int day) {
		UserClassificationServiceAdNetData advData = advertisersData
				.get(advertiser);
		if (advData == null) {
			advData = new UserClassificationServiceAdNetData();
			advertisersData.put(advertiser, advData);
		}
		advData.setBid(ucsBid, day);
	}

	@Override
	public UserClassificationServiceAdNetData getAdNetData(String advertiser) {
		return advertisersData.get(advertiser);
	}

	@Override
	public void auction(int day) {
		int advCount = advertisersData.size();

		if (advCount > 0) {
			String[] advNames = new String[advCount + 1];
			double[] bids = new double[advCount + 1];
			int[] indices = new int[advCount + 1];

			int i = 0;

			for (String advName : advertisersData.keySet()) {
				advNames[i] = new String(advName);
				bids[i] = advertisersData.get(advName).bid;
				indices[i] = i;
				i++;
			}

			advNames[advCount] = "Zero";
			bids[advCount] = 0;
			indices[advCount] = advCount;

			hardSort(bids, indices);

			double ucsProb = 1.0;
			double levelPrice = 0;
			for (int j = 0; j < advCount; j++) {
				UserClassificationServiceAdNetData advData = advertisersData
						.get(advNames[indices[j]]);
				levelPrice = ucsProb * bids[indices[j + 1]];
				advData.setAuctionResult(levelPrice, ucsProb, day);
				AdxManager.getInstance().getSimulation()
						.broadcastUCSWin(advNames[j], levelPrice);
				ucsProb = ucsProb * UCS_PROB;
			}
		}
	}

}
