/*
 * Ranking.java
 *
 * COPYRIGHT  2008
 * THE REGENTS OF THE UNIVERSITY OF MICHIGAN
 * ALL RIGHTS RESERVED
 *
 * PERMISSION IS GRANTED TO USE, COPY, CREATE DERIVATIVE WORKS AND REDISTRIBUTE THIS
 * SOFTWARE AND SUCH DERIVATIVE WORKS FOR NONCOMMERCIAL EDUCATION AND RESEARCH
 * PURPOSES, SO LONG AS NO FEE IS CHARGED, AND SO LONG AS THE COPYRIGHT NOTICE
 * ABOVE, THIS GRANT OF PERMISSION, AND THE DISCLAIMER BELOW APPEAR IN ALL COPIES
 * MADE; AND SO LONG AS THE NAME OF THE UNIVERSITY OF MICHIGAN IS NOT USED IN ANY
 * ADVERTISING OR PUBLICITY PERTAINING TO THE USE OR DISTRIBUTION OF THIS SOFTWARE
 * WITHOUT SPECIFIC, WRITTEN PRIOR AUTHORIZATION.
 *
 * THIS SOFTWARE IS PROVIDED AS IS, WITHOUT REPRESENTATION FROM THE UNIVERSITY OF
 * MICHIGAN AS TO ITS FITNESS FOR ANY PURPOSE, AND WITHOUT WARRANTY BY THE
 * UNIVERSITY OF MICHIGAN OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT
 * LIMITATION THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE REGENTS OF THE UNIVERSITY OF MICHIGAN SHALL NOT BE LIABLE FOR ANY
 * DAMAGES, INCLUDING SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WITH
 * RESPECT TO ANY CLAIM ARISING OUT OF OR IN CONNECTION WITH THE USE OF THE SOFTWARE,
 * EVEN IF IT HAS BEEN OR IS HEREAFTER ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package edu.umich.eecs.tac.props;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;
import tau.tac.adx.props.AdLink;

/**
 * Ranking represents the slots allocated to the advertisers' {@link AdLink ad
 * links} in an auction.
 * 
 * @author Patrick Jordan
 * @see <a href="http://aa.tradingagents.org/documentation">TAC
 *      Documentation</a>
 */
public class Ranking extends AbstractTransportable {
	/**
	 * The slots indexed by position.
	 */
	private final List<Slot> slots;

	/**
	 * Create an empty ranking.
	 */
	public Ranking() {
		slots = new ArrayList<Slot>();
	}

	/**
	 * Adds the {@link AdLink ad link} to the alots and set its promoted status.
	 * 
	 * @param adLink
	 *            the ad link
	 * @param promoted
	 *            <code>true</code> if the ad link is promoted.
	 * @see <a href="http://aa.tradingagents.org/documentation">TAC
	 *      Documentation</a>
	 * @throws IllegalStateException
	 *             if the ranking is locked.
	 */
	public final void add(final AdLink adLink, final boolean promoted)
			throws IllegalStateException {
		add(new Slot(adLink, promoted));
	}

	/**
	 * Adds the {@link AdLink ad link} to the alots and set its promoted status
	 * to <code>false</code>.
	 * 
	 * @param adLink
	 *            the ad link.
	 * @throws IllegalStateException
	 *             if the ranking is locked.
	 */
	public final void add(final AdLink adLink) throws IllegalStateException {
		add(adLink, false);
	}

	/**
	 * Adds the slot to the slot list.
	 * 
	 * @param slot
	 *            the slot to add.
	 * @throws IllegalStateException
	 *             if the ranking is locked.
	 */
	protected final void add(final Slot slot) throws IllegalStateException {
		lockCheck();
		slots.add(slot);
	}

	/**
	 * Set the {@link AdLink ad link} and promoted status for the slot.
	 * 
	 * @param position
	 *            the slot position.
	 * @param adLink
	 *            the ad link.
	 * @param promoted
	 *            the promoted status.
	 * @throws IllegalStateException
	 *             if the ranking is locked.
	 */
	public final void set(final int position, final AdLink adLink,
			final boolean promoted) throws IllegalStateException {
		lockCheck();
		slots.set(position, new Slot(adLink, promoted));
	}

	/**
	 * Returns the {@link AdLink ad link} at the slot position.
	 * 
	 * @param position
	 *            the slot position.
	 * @return the {@link AdLink ad link} at the slot position.
	 */
	public final AdLink get(final int position) {
		return slots.get(position).getAdLink();
	}

	/**
	 * Returns the promoted status of the {@link AdLink ad link} at the slot
	 * position.
	 * 
	 * @param position
	 *            the slot position.
	 * @return the promoted status of the {@link AdLink ad link} at the slot
	 *         position.
	 */
	public final boolean isPromoted(final int position) {
		return slots.get(position).isPromoted();
	}

	/**
	 * Returns the slot position for the {@link AdLink ad link} in question and
	 * <code>-1</code> if the {@link AdLink ad link} is not in the ranking.
	 * 
	 * @param adLink
	 *            the ad link.
	 * @return the slot position for the {@link AdLink ad link} in question and
	 *         <code>-1</code> if the {@link AdLink ad link} is not in the
	 *         ranking.
	 */
	public final int positionForAd(final AdLink adLink) {
		for (int i = 0; i < size(); i++) {
			if (get(i).equals(adLink)) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Returns the number of slots in the ranking.
	 * 
	 * @return the number of slots in the ranking.
	 */
	public final int size() {
		return slots.size();
	}

	/**
	 * Returns a string representation of the ranking.
	 * 
	 * @return a string representation of the ranking.
	 */
	@Override
	public final String toString() {
		StringBuffer sb = new StringBuffer().append('[');
		for (int i = 0, n = size(); i < n; i++) {
			sb.append('[').append(i).append(": ").append(get(i)).append(']');
		}
		sb.append(']');

		return sb.toString();
	}

	/**
	 * Reads the slots from the reader.
	 * 
	 * @param reader
	 *            the reader to read data from.
	 * @throws ParseException
	 *             if an exception occurs while reading the slots.
	 */
	@Override
	protected final void readWithLock(final TransportReader reader)
			throws ParseException {
		while (reader.nextNode(Slot.class.getSimpleName(), false)) {
			add((Slot) reader.readTransportable());
		}
	}

	/**
	 * Writes the slots to the writer.
	 * 
	 * @param writer
	 *            the writer to write data to.
	 */
	@Override
	protected final void writeWithLock(final TransportWriter writer) {
		for (Slot slot : slots) {
			writer.write(slot);
		}
	}

	/**
	 * The slot for the ranking. The slot contains the {@link AdLink ad link}
	 * and the promoted status.
	 * 
	 * @see <a href="http://aa.tradingagents.org/documentation">TAC
	 *      Documentation</a>
	 */
	public static class Slot extends AbstractTransportable {
		/**
		 * The serial version id.
		 */
		private static final long serialVersionUID = -2489798409612047493L;
		/**
		 * The ad link.
		 */
		private AdLink adLink;
		/**
		 * The promoted status.
		 */
		private boolean promoted;

		/**
		 * Creates an empty slot.
		 */
		public Slot() {
		}

		/**
		 * Creates a slot with an {@link AdLink ad link} and promoted status.
		 * 
		 * @param adLink
		 *            the ad link.
		 * @param promoted
		 *            the promoted status.
		 */
		public Slot(final AdLink adLink, final boolean promoted) {
			this.adLink = adLink;
			this.promoted = promoted;
		}

		/**
		 * Returns the {@link AdLink ad link}.
		 * 
		 * @return the {@link AdLink ad link}.
		 */
		public final AdLink getAdLink() {
			return adLink;
		}

		/**
		 * Sets the {@link AdLink ad link}.
		 * 
		 * @param adLink
		 *            the ad link.
		 */
		public final void setAdLink(final AdLink adLink) {
			this.adLink = adLink;
		}

		/**
		 * Returns the promoted status.
		 * 
		 * @return the promoted status.
		 */
		public final boolean isPromoted() {
			return promoted;
		}

		/**
		 * Sets the promoted status.
		 * 
		 * @param promoted
		 *            the promoted status.
		 */
		public final void setPromoted(final boolean promoted) {
			this.promoted = promoted;
		}

		/**
		 * Read the {@link AdLink ad link} and the promoted status from the
		 * reader.
		 * 
		 * @param reader
		 *            the reader to read data from.
		 * @throws ParseException
		 *             if an exception occurs while reading the promoted status
		 *             and ad link.
		 */
		@Override
		protected final void readWithLock(final TransportReader reader)
				throws ParseException {
			promoted = reader.getAttributeAsInt("promoted", 0) > 0;

			if (reader.nextNode(AdLink.class.getSimpleName(), false)) {
				adLink = (AdLink) reader.readTransportable();
			}
		}

		/**
		 * Write the {@link AdLink ad link} and the promoted status to the
		 * writer.
		 * 
		 * @param writer
		 *            the writer to write data to.
		 */
		@Override
		protected final void writeWithLock(final TransportWriter writer) {
			if (promoted) {
				writer.attr("promoted", 1);
			}

			if (adLink != null) {
				writer.write(adLink);
			}
		}
	}
}
