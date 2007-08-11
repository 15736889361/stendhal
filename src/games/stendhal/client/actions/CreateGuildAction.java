package games.stendhal.client.actions;

import games.stendhal.client.StendhalClient;
import marauroa.common.game.RPAction;

/**
 * Creates a guild & puts player in it.
 */
class CreateGuildAction implements SlashAction  {

	/**
	 * Execute an away command.
	 *
	 * @param	params		The formal parameters.
	 * @param	remainder	Line content after parameters.
	 *
	 * @return	<code>true</code> if command was handled.
	 */
	public boolean execute(String[] params, String remainder) {
		RPAction action = new RPAction();

		action.put("type", "joinguild");
                action.put("guildname", params[0]);
		if (remainder.length() != 0) {
			action.put("guilddescription", remainder);
		}
		

		StendhalClient.get().send(action);

		return true;
	}

	/**
	 * Get the maximum number of formal parameters.
	 *
	 * @return	The parameter count.
	 */
	public int getMaximumParameters() {
		return 2;
	}

	/**
	 * Get the minimum number of formal parameters.
	 *
	 * @return	The parameter count.
	 */
	public int getMinimumParameters() {
		return 1;
	}
}
