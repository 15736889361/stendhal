package games.stendhal.server.script;

import games.stendhal.server.StendhalRPRuleProcessor;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.scripting.ScriptImpl;

import java.util.List;

/**
 * Impersonated a NPC to shout a message to all players
 * 
 * @author hendrik
 */
public class NPCShout extends ScriptImpl {

	@Override
	public void execute(Player admin, List<String> args) {
		super.execute(admin, args);

		if (args.size() < 2) {
			admin.sendPrivateText("Usage: /script NPCShout.class npc text");
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(args.get(0));
			sb.append(" shouts: ");
			boolean first = true;
			for (String word : args) {
				if (first) {
					first = false;
				} else {
					sb.append(word);
					sb.append(" ");
				}
			}
			String text = sb.toString();

			List<Player> players = StendhalRPRuleProcessor.get().getPlayers();
			for (Player player : players) {
				player.sendPrivateText(text);
			}
		}
	}

}
