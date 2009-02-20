package games.stendhal.server.script;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

import java.util.List;

import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * Resets an RPSlot.
 * 
 * @author kymara
 */
public class ResetSlot extends ScriptImpl {

	@Override
	public void execute(final Player admin, final List<String> args) {
		super.execute(admin, args);

		// admin help
		if (args.size() < 2) {
			admin.sendPrivateText("Need player name and slot name as parameter.");
			return;
		}

		// find the player and slot
		final Player player = SingletonRepository.getRuleProcessor().getPlayer(args.get(0));
		final RPSlot slot = player.getSlot(args.get(1));

		// remove old store object
		final RPObject rpObject = slot.iterator().next();
		slot.remove(rpObject.getID());

		// create new store object
		slot.add(new RPObject());

		// notify the player
		player.sendPrivateText("Your " + args.get(1) + " state was reset by "
				+ admin.getTitle());
	}
}
