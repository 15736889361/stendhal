package games.stendhal.client.actions;

import games.stendhal.client.StendhalClient;
import marauroa.common.game.RPAction;

/**
 * Add a player to the ignore list.
 */
class GrumpyAction implements SlashAction {

	/**
	 * Execute an ignore command.
	 * 
	 * @param params
	 *            The formal parameters.
	 * @param remainder
	 *            Line content after parameters.
	 * 
	 * @return <code>true</code> if command was handled.
	 */
	public boolean execute(final String[] params, final String remainder) {
		final RPAction action = new RPAction();

		action.put("type", "grumpy");

		if (remainder.length() != 0) {
			action.put("reason", remainder);
		}

		StendhalClient.get().send(action);

		return true;
	}

	/**
	 * Get the maximum number of formal parameters.
	 * 
	 * @return The parameter count.
	 */
	public int getMaximumParameters() {
		return 0;
	}

	/**
	 * Get the minimum number of formal parameters.
	 * 
	 * @return The parameter count.
	 */
	public int getMinimumParameters() {
		return 0;
	}
}
