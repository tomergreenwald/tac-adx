package tau.tac.adx.demand;

import static edu.umich.eecs.tac.auction.AuctionUtils.hardSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
	private final Map<String, UserClassificationServiceAdNetData> tomorrowsAdvertisersData = new HashMap<String, UserClassificationServiceAdNetData>();

	@Override
	public void updateAdvertiserBid(String advertiser, double ucsBid, int day) {
		UserClassificationServiceAdNetData advData = tomorrowsAdvertisersData
				.get(advertiser);
		if (advData == null) {
			advData = new UserClassificationServiceAdNetData();
			advData.setAuctionResult(0, 1.0, 1);
			tomorrowsAdvertisersData.put(advertiser, advData);
		}
		advData.setBid(ucsBid, day);
	}

	@Override
	public UserClassificationServiceAdNetData getAdNetData(String advertiser) {
		return advertisersData.get(advertiser);
	}

	@Override
	public UserClassificationServiceAdNetData getTomorrowsAdNetData(
			String advertiser) {
		return tomorrowsAdvertisersData.get(advertiser);
	}

	@Override
	public void auction(int day, boolean broadcast) {
		advertisersData.clear();
		for (String advertiser : tomorrowsAdvertisersData.keySet()) {
			advertisersData.put(advertiser,
					tomorrowsAdvertisersData.get(advertiser).clone());
		}

		int advCount = tomorrowsAdvertisersData.size();

		if (advCount > 0) {
			String[] advNames = new String[advCount + 1];
			double[] bids = new double[advCount + 1];
			int[] indices = new int[advCount + 1];

			int i = 0;

			List<String> advNamesList = new ArrayList<String>(
					tomorrowsAdvertisersData.keySet());
			Collections.shuffle(advNamesList);

			for (String advName : advNamesList) {
				advNames[i] = new String(advName);
				bids[i] = tomorrowsAdvertisersData.get(advName).getBid();
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
				String advertiser = advNames[indices[j]];
				UserClassificationServiceAdNetData advData = tomorrowsAdvertisersData
						.get(advertiser);
				levelPrice = ucsProb * bids[indices[j + 1]];
				advData.setAuctionResult(levelPrice, ucsProb, day + 1);
				if (broadcast) {
					AdxManager.getInstance().getSimulation()
							.broadcastUCSWin(advertiser, levelPrice);
				}
				ucsProb = ucsProb * UCS_PROB;
			}
		}
	}

	@Override
	public String logToString() {
		String ret = new String("");
		for (String adv : advertisersData.keySet()) {
			ret = ret + adv + advertisersData.get(adv).logToString();
		}
		return ret;
	}
}
