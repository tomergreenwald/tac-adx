package tau.tac.adx.messages;

import tau.tac.adx.demand.UserClassificationService;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class UserClassificationServiceNotification implements AdxMessage {

	UserClassificationService ucs;

	public UserClassificationServiceNotification(UserClassificationService ucs) {
		super();
		this.ucs = ucs;
	}

	public UserClassificationService getUserClassificationService() {
		return ucs;
	}

}
