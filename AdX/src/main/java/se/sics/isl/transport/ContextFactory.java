package se.sics.isl.transport;

/**
 * @author Patrick Jordan
 */
public interface ContextFactory {
	/**
	 * Add the allowable transports to the context.
	 * 
	 * @return the base context with new transports added.
	 */
	Context createContext();

	/**
	 * Add the allowable transports to the context.
	 * 
	 * @param context
	 *            the parent context
	 * @return the context with new transports added.
	 */
	Context createContext(Context context);
}
