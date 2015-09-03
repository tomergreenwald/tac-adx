package tau.tac.adx.users;

import tau.tac.adx.Adx;
import tau.tac.adx.users.properties.Age;
import tau.tac.adx.users.properties.Gender;
import tau.tac.adx.users.properties.Income;
import tau.tac.adx.users.properties.UserState;
import edu.umich.eecs.tac.user.User;

/**
 * Defines a user in the Ad-Exchange system.<br>
 * A user is defined by three properties: <li>{@link Age age}</li> <li>
 * {@link Gender gender}</li> <li>{@link Income Income}</li>
 * 
 * @author greenwald
 * 
 */
public class AdxUser extends User implements Cloneable, TacUser<Adx> {

	/** Users's {@link Age age}. */
	private final Age age;
	/** Users's {@link Gender gender}. */
	private final Gender gender;
	/** Users's {@link Income Income}. */
	private final Income income;
	/**
	 * The probability an {@link AdxUser} is likely to continue visiting this
	 * site after visiting it once in a given day.
	 */
	private double pContinue;
	/** User's activity {@link UserState state}. */
	private UserState userState;
	/** Unique user id. */
	private int uniqueId;

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
	 * @param uniqueId
	 *            Unique user id.
	 */
	public AdxUser(Age age, Gender gender, Income income, double pContinue,
			int uniqueId) {
		super();
		this.age = age;
		this.gender = gender;
		this.income = income;
		this.pContinue = pContinue;
		this.userState = UserState.IDLE;
		this.uniqueId = uniqueId;
	}

	/**
	 * @return the pContinue
	 */
	public double getpContinue() {
		return pContinue;
	}

	/**
	 * @param pContinue
	 *            the pContinue to set
	 */
	public void setpContinue(double pContinue) {
		this.pContinue = pContinue;
	}

	/**
	 * @return the userState
	 */
	public UserState getUserState() {
		return userState;
	}

	/**
	 * @param userState
	 *            the userState to set
	 */
	public void setUserState(UserState userState) {
		this.userState = userState;
	}

	/**
	 * @return the age
	 */
	public Age getAge() {
		return age;
	}

	/**
	 * @return the gender
	 */
	public Gender getGender() {
		return gender;
	}

	/**
	 * @return the income
	 */
	public Income getIncome() {
		return income;
	}

	/**
	 * @return the uniqueId
	 */
	public int getUniqueId() {
		return uniqueId;
	}

	/**
	 * @param uniqueId the uniqueId to set
	 */
	public void setUniqueId(int uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new AdxUser(age, gender, income, pContinue, uniqueId);
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

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdxUser other = (AdxUser) obj;
		if (age != other.age)
			return false;
		if (gender != other.gender)
			return false;
		if (income != other.income)
			return false;
		if (Double.doubleToLongBits(pContinue) != Double
				.doubleToLongBits(other.pContinue))
			return false;
		return true;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((age == null) ? 0 : age.hashCode());
		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
		result = prime * result + ((income == null) ? 0 : income.hashCode());
		long temp;
		temp = Double.doubleToLongBits(pContinue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return "AdxUser [age=" + age + ", gender=" + gender + ", income="
				+ income + ", pContinue=" + pContinue + ", userState="
				+ userState + ", uniqueId=" + uniqueId + "]";
	}

}
