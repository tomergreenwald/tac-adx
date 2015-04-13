package tau.tac.adx.props;

import java.text.ParseException;

import edu.umich.eecs.tac.props.AbstractTransportable;
import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;

public class ReservePriceInfo extends AbstractTransportable {

	private static final long serialVersionUID = -6576269032654384128L;

	private ReservePriceType reservePriceType;

	public ReservePriceInfo() {
	}

	public ReservePriceInfo(ReservePriceType reservePriceType) {
		this.reservePriceType = reservePriceType;
	}

	public final ReservePriceType getReservePriceType() {
		return reservePriceType;
	}

	public final void setReservePriceType(ReservePriceType reservePriceType) {
		lockCheck();
		this.reservePriceType = reservePriceType;
	}

	/**
	 * Creates a string with the account balance.
	 * 
	 * @return a string with the account balance.
	 */
	@Override
	public final String toString() {
		return String.format("%s[%s]", getTransportName(), reservePriceType);
	}

	/**
	 * Read the balance parameter.
	 * 
	 * @param reader
	 *            the reader to read from
	 * @throws ParseException
	 *             if a parse exception occurs reading the balance.
	 */
	@Override
	protected final void readWithLock(final TransportReader reader)
			throws ParseException {
		reservePriceType = ReservePriceType.values()[reader.getAttributeAsInt(
				"reservePriceType", 0)];
	}

	/**
	 * Write the balance parameter.
	 * 
	 * @param writer
	 *            the writer to write to.
	 */
	@Override
	protected final void writeWithLock(final TransportWriter writer) {
		writer.attr("reservePriceType", reservePriceType.ordinal());
	}

}
