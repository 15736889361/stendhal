package games.stendhal.server.actions;

import static games.stendhal.common.constants.Actions.LISTQUESTS;
import static games.stendhal.common.constants.Actions.TARGET;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

public class QuestListAction implements ActionListener {
	

	public static void register() {
		CommandCenter.register(LISTQUESTS, new QuestListAction());
	}

	public void onAction(final Player player, final RPAction action) {

		final StringBuilder st = new StringBuilder();
		if (action.has(TARGET)) {
			final String which = action.get(TARGET);
			st.append(SingletonRepository.getStendhalQuestSystem().listQuest(player, which));

		} else {
			st.append(SingletonRepository.getStendhalQuestSystem().listQuests(player));
		}
		player.sendPrivateText(st.toString());
		player.notifyWorldAboutChanges();

	}

}
