package games.stendhal.client.actions;

import games.stendhal.client.StendhalClient;
import marauroa.common.game.RPAction;

/**
 * Set the admin level of a player.
 */
class AdminLevelAction implements SlashAction {

	/**
	 * Execute a chat command.
	 * 
	 * @param params
	 *            The formal parameters.
	 * @param remainder
	 *            Line content after parameters.
	 * 
	 * @return <code>true</code> if was handled.
	 */
	public boolean execute(String[] params, String remainder) {
		if (params == null) {
			return false;
		}
		RPAction adminlevel = new RPAction();

		adminlevel.put("type", "adminlevel");
		adminlevel.put("target", params[0]);

		if (params.length > 1 && params[1] != null) {
			adminlevel.put("newlevel", params[1]);
		}

		StendhalClient.get().send(adminlevel);

		return true;
	}

	/**
	 * Get the maximum number of formal parameters.
	 * 
	 * @return The parameter count.
	 */
	public int getMaximumParameters() {
		return 2;
	}

	/**
	 * Get the minimum number of formal parameters.
	 * 
	 * @return The parameter count.
	 */
	public int getMinimumParameters() {
		return 1;
	}
}
