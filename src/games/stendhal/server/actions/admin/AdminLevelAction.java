package games.stendhal.server.actions.admin;

import static games.stendhal.common.constants.Actions.ADMINLEVEL;
import static games.stendhal.common.constants.Actions.NEWLEVEL;
import static games.stendhal.common.constants.Actions.TARGET;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

public class AdminLevelAction extends AdministrationAction {

	public static void register() {
		CommandCenter.register(ADMINLEVEL, new AdminLevelAction(), 0);

	}

	@Override
	public void perform(final Player player, final RPAction action) {

		if (action.has(TARGET)) {

			final String name = action.get(TARGET);
			final Player target = SingletonRepository.getRuleProcessor().getPlayer(name);

			if ((target == null) || (target.isGhost() && !isAllowedtoSeeGhosts(player))) {
				logger.debug("Player \"" + name + "\" not found");
				player.sendPrivateText("Player \"" + name + "\" not found");
				return;
			}

			final int oldlevel = target.getAdminLevel();
			String response = target.getTitle() + " has adminlevel " + oldlevel;

			if (action.has(NEWLEVEL)) {
				// verify newlevel is a number
				int newlevel;
				try {
					newlevel = Integer.parseInt(action.get(NEWLEVEL));
				} catch (final NumberFormatException e) {
					player.sendPrivateText("The new adminlevel needs to be an Integer");

					return;
				}

				// If level is beyond max level, just set it to max.
				if (newlevel > REQUIRED_ADMIN_LEVEL_FOR_SUPER) {
					newlevel = REQUIRED_ADMIN_LEVEL_FOR_SUPER;
				}

				final int mylevel = player.getAdminLevel();
				if (mylevel < REQUIRED_ADMIN_LEVEL_FOR_SUPER) {
					response = "Sorry, but you need an adminlevel of "
							+ REQUIRED_ADMIN_LEVEL_FOR_SUPER
							+ " to change adminlevel.";
				} else {

					// OK, do the change
					SingletonRepository.getRuleProcessor().addGameEvent(
							player.getName(), ADMINLEVEL, target.getName(),
							ADMINLEVEL, action.get(NEWLEVEL));
					target.setAdminLevel(newlevel);
					target.update();
					target.notifyWorldAboutChanges();

					response = "Changed adminlevel of " + target.getTitle()
							+ " from " + oldlevel + " to " + newlevel + ".";
					target.sendPrivateText(player.getTitle()
							+ " changed your adminlevel from " + +oldlevel
							+ " to " + newlevel + ".");
				}
			}

			player.sendPrivateText(response);
		}
	}

	boolean isAllowedtoSeeGhosts(final Player player) {
		return AdministrationAction.isPlayerAllowedToExecuteAdminCommand(player, "ghostmode", false);
	}

}
