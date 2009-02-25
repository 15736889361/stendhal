package games.stendhal.server.actions.admin;

import static games.stendhal.common.constants.Actions.TARGET;
import static games.stendhal.common.constants.Actions.TELEPORT;
import static games.stendhal.common.constants.Actions.X;
import static games.stendhal.common.constants.Actions.Y;
import static games.stendhal.common.constants.Actions.ZONE;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;

import java.util.Set;
import java.util.TreeSet;

import marauroa.common.game.IRPZone;
import marauroa.common.game.RPAction;

public class TeleportAction extends AdministrationAction {


	public static void register() {
		CommandCenter.register(TELEPORT, new TeleportAction(), 400);

	}

	@Override
	public void perform(final Player player, final RPAction action) {
		if (action.has(TARGET) && action.has(ZONE) && action.has(X)
				&& action.has(Y)) {
			final String name = action.get(TARGET);
			final Player teleported = SingletonRepository.getRuleProcessor().getPlayer(name);

			if (teleported == null) {
				final String text = "Player \"" + name + "\" not found";
				player.sendPrivateText(text);
				logger.debug(text);
				return;
			}

			// validate the zone-name.
			final IRPZone.ID zoneid = new IRPZone.ID(action.get(ZONE));
			if (!SingletonRepository.getRPWorld().hasRPZone(zoneid)) {
				final String text = "Zone \"" + zoneid + "\" not found.";
				logger.debug(text);

				final Set<String> zoneNames = new TreeSet<String>();
				for (final IRPZone irpZone : SingletonRepository.getRPWorld()) {
					final StendhalRPZone zone = (StendhalRPZone) irpZone;
					zoneNames.add(zone.getName());
				}
				player.sendPrivateText(text + " Valid zones: " + zoneNames);
				return;
			}

			final StendhalRPZone zone = (StendhalRPZone) SingletonRepository.getRPWorld().getRPZone(
					zoneid);
			final int x = action.getInt(X);
			final int y = action.getInt(Y);

			SingletonRepository.getRuleProcessor().addGameEvent(player.getName(),
					TELEPORT, action.get(TARGET), zone.getName(),
					Integer.toString(x), Integer.toString(y));
			teleported.teleport(zone, x, y, null, player);
			
			SingletonRepository.getJail().grantParoleIfPlayerWasAPrisoner(teleported);
		}
	}

}
