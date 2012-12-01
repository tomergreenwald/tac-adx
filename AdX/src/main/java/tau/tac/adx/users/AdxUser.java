package tau.tac.adx.users;

import edu.umich.eecs.tac.user.User;
import lombok.Data;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;
import tau.tac.adx.users.properties.UserState;

/**
 * Defines a user in the Ad-Exchange system.<br>
 * A user is defined by three properties: <li>{@link Age age}</li> <li>
 * {@link Gender gender}</li> <li>{@link Income Income}</li>
 * 
 * @author greenwald
 * 
 */
@lombok.NoArgsConstructor
@Data
@lombok.EqualsAndHashCode(exclude = "userState", callSuper=false)
public class AdxUser extends User implements Cloneable {

	/** Users's {@link Age age}. */
	private Age age;
	/** Users's {@link Gender gender}. */
	private Gender gender;
	/** Users's {@link Income Income}. */
	private Income income;
	/**
	 * The probability an {@link AdxUser} is likely to continue visiting this
	 * site after visiting it once in a given day.
	 */
	private double pContinue;
	/** User's activity {@link UserState state}. */
	private UserState userState;

	/**
	 * @param age
	 *            Users's {@link Age age}.
	 * @param gender
	 *            Users's {@link Gender gender}
	 * @param income
	 *            Users's {@link Income Income}.
	 * @param pContinue
	 *            The probability an {@link AdxUser} is likely to continue
	 *            visiting this site after visiting it once in a given day.
	 */
	public AdxUser(Age age, Gender gender, Income income, double pContinue) {
		super();
		this.age = age;
		this.gender = gender;
		this.income = income;
		this.pContinue = pContinue;
		this.userState = UserState.IDLE;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new AdxUser(age, gender, income, pContinue);
	}

	/**
	 * Sets minor attributes to a default value.
	 * 
	 * @return The {@link AdxUser} itself after the change.
	 */
	public AdxUser ignoreMinorAttributes() {
		pContinue = Double.NaN;
		userState = UserState.IDLE;
		return this;
	}

}
