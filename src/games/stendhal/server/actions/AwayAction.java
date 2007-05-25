/*
 * @(#) src/games/stendhal/server/actions/AwayAction.java
 *
 * $Id$
 */

package games.stendhal.server.actions;

//
//

import marauroa.common.Logger;

import marauroa.common.Log4J;
import marauroa.common.game.RPAction;

import games.stendhal.server.StendhalRPRuleProcessor;
import games.stendhal.server.entity.player.Player;

/**
 * Process /away commands.
 */
public class AwayAction implements ActionListener {

	/**
	 * Logger.
	 */
	private static final Logger logger = Log4J.getLogger(AwayAction.class);

	/**
	 * Registers action.
	 */
	public static void register() {
		StendhalRPRuleProcessor.register("away", new AwayAction());
	}

	/**
	 * Handle an away action.
	 *
	 * @param	player		The player.
	 * @param	action		The action.
	 */
	protected void onAway(Player player, RPAction action) {
		if (action.has("message")) {
			player.put("away", action.get("message"));
		} else if (player.has("away")) {
			player.remove("away");
		}

		player.resetAwayReplies();
		player.notifyWorldAboutChanges();
	}

	/**
	 * Handle client action.
	 *
	 * @param	player		The player.
	 * @param	action		The action.
	 */
	public void onAction(Player player, RPAction action) {
		if (action.get("type").equals("away")) {
			onAway(player, action);
		}
	}
}
