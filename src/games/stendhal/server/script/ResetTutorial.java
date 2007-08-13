package games.stendhal.server.script;

import games.stendhal.server.StendhalRPRuleProcessor;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.scripting.ScriptImpl;

import java.util.List;

import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * Resets the tutorial
 *
 * @author hendrik
 */
public class ResetTutorial extends ScriptImpl {

	@Override
	public void execute(Player admin, List<String> args) {
		super.execute(admin, args);

		// admin help
		if (args.size() == 0) {
			admin.sendPrivateText("Need player name as parameter.");
			return;
		}

		// find the player and slot
		Player player = StendhalRPRuleProcessor.get().getPlayer(args.get(0));
		RPSlot slot = player.getSlot("!tutorial");

		// remove old store object
		RPObject rpObject = slot.iterator().next();
		slot.remove(rpObject.getID());

		// create new store object
		slot.add(new RPObject());

		// notify the player
		player.sendPrivateText("Your tutorial state was reset by " + admin.getName());
	}
}
