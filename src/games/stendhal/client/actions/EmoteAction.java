package games.stendhal.client.actions;


import games.stendhal.client.StendhalClient;
import marauroa.common.game.RPAction;

/**
 * Send an emote message.
 * 
 * @author raignarok
 */
class EmoteAction implements SlashAction {

	/**
	 * Execute a chat command.
	 * 
	 * @param params
	 *            The formal parameters.
	 * @param remainder
	 *            Line content after parameters.
	 * 
	 * @return <code>true</code> if command was handled.
	 */
	public boolean execute(final String[] params, final String remainder) {
		final RPAction emote = new RPAction();

		emote.put("type", "emote");
		emote.put("text", remainder);

		StendhalClient.get().send(emote);

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
