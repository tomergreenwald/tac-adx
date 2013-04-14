package tau.tac.adx.messages;

import tau.tac.adx.demand.Campaign;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class CampaignNotification implements AdxMessage {
	Campaign campaign;

	public CampaignNotification(Campaign campaign) {
		super();
		this.campaign = campaign;
	}

	public Campaign getCampaign() {
		return campaign;
	}

}
