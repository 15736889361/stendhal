package games.stendhal.client.scripting.command;

import games.stendhal.client.StendhalClient;
import marauroa.common.game.RPAction;

/**
 * Send a support response message.
 */
class SupportAnswerCommand implements SlashCommand {
	/**
	 * Execute a chat command.
	 *
	 * @param	params		The formal parameters.
	 * @param	remainder	Line content after parameters.
	 *
	 * @return	<code>true</code> if command was handled.
	 */
	public boolean execute(String [] params, String remainder) {
		RPAction tell = new RPAction();

		tell.put("type", "supportanswer");
		tell.put("target", params[0]);
		tell.put("text", remainder);

		StendhalClient.get().send(tell);

		return true;
	}


	/**
	 * Get the maximum number of formal parameters.
	 *
	 * @return	The parameter count.
	 */
	public int getMaximumParameters() {
		return 1;
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
