package tau.tac.adx.demand;

import java.util.Iterator;

/**
 * 
 * @author Mariano Schain
 * 
 * @param <A>
 */
public class AccumulatorImpl {
	public static <A> A accumulate(final Accumulator<A> accumulator,
			final Iterable<? extends A> i, final A init) {
		A result = init;
		final Iterator<? extends A> iter = i.iterator();
		while (iter.hasNext()) {
			result = accumulator.accumulate(result, iter.next());
		}
		return result;
	}
}
