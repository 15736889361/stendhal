package games.stendhal.server.events;

/**
 * Implementing classes can be notified that a certain turn number
 * has been reached.
 * 
 * After registering at the TurnNotifier, the TurnNotifier will wait
 * until the specified turn number has been reached, and notify the
 * TurnListener.
 * 
 * A string can be passed to the TurnNotifier while registering; this
 * string will then be passed back to the TurnListener when the specified
 * turn number has been reached. Using this string, a TurnListener can
 * register itself multiple times at the TurnNotifier. 
 *
 * @author hendrik
 */
public interface TurnListener {

	/**
	 * This method is called when the turn number is reached
	 *
	 * @param currentTurn current turn number
	 * @param message the string that was used 
	 */
	public void onTurnReached(int currentTurn, String message);
}
