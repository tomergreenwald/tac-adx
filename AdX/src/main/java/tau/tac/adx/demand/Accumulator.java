package tau.tac.adx.demand;


public interface Accumulator<A> {
  A accumulate(A accumulated, A next);
}

