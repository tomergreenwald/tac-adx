package tau.tac.adx.demand;

/**
 * 
 * @author Mariano Schain
 * 
 * @param <A>
 */
public interface Accumulator<A> {
	A accumulate(A accumulated, A next);
}
