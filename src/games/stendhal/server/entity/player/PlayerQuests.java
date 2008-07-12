package games.stendhal.server.entity.player;

import games.stendhal.server.core.engine.SingletonRepository;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * Accesses the player quest states.
 *
 * @author hendrik
 */
class PlayerQuests {
	private final Player player;
	
	public PlayerQuests(final Player player) {
		this.player = player;
	}

	/**
	 * Checks whether the player has completed the given quest or not.
	 * 
	 * @param name
	 *            The quest's name
	 * @return true iff the quest has been completed by the player
	 */
	public boolean isQuestCompleted(final String name) {
		final String info = player.getKeyedSlot("!quests", name);

		if (info == null) {
			return false;
		}

		return info.equals("done") || info.startsWith("done;");
	}

	/**
	 * Checks whether the player has made any progress in the given quest or
	 * not. For many quests, this is true right after the quest has been
	 * started.
	 * 
	 * @param name
	 *            The quest's name
	 * @return true iff the player has made any progress in the quest
	 */
	public boolean hasQuest(final String name) {
		return (player.getKeyedSlot("!quests", name) != null);
	}

	/**
	 * Gets the player's current status in the given quest.
	 * 
	 * @param name
	 *            The quest's name
	 * @return the player's status in the quest
	 */
	public String getQuest(final String name) {
		return player.getKeyedSlot("!quests", name);
	}

	/**
	 * Allows to store the player's current status in a quest in a string. This
	 * string may, for instance, be "started", "done", a semicolon- separated
	 * list of items that need to be brought/NPCs that need to be met, or the
	 * number of items that still need to be brought. Note that the string
	 * "done" has a special meaning: see isQuestComplete().
	 * 
	 * @param name
	 *            The quest's name
	 * @param status
	 *            the player's status in the quest. Set it to null to completely
	 *            reset the player's status for the quest.
	 */
	public void setQuest(final String name, final String status) {
		final String oldStatus = player.getKeyedSlot("!quests", name);
		player.setKeyedSlot("!quests", name, status);
		if ((status == null) || !status.equals(oldStatus)) {
			SingletonRepository.getRuleProcessor().addGameEvent(player.getName(), "quest",
					name, status);
		}
	}

	public List<String> getQuests() {
		final RPSlot slot = player.getSlot("!quests");
		final RPObject quests = slot.iterator().next();

		final List<String> questsList = new LinkedList<String>();
		for (final String quest : quests) {
			if (!quest.equals("id") && !quest.equals("zoneid")) {
				questsList.add(quest);
			}
		}
		return questsList;
	}

	public void removeQuest(final String name) {
		player.setKeyedSlot("!quests", name, null);
	}

	/**
	 * Is the named quest in one of the listed states?
	 * 
	 * @param name
	 *            quest
	 * @param states
	 *            valid states
	 * @return true, if the quest is in one of theses states, false otherwise
	 */
	public boolean isQuestInState(final String name, final String... states) {
		final String questState = getQuest(name);

		if (questState != null) {
			for (final String state : states) {
				if (questState.equals(state)) {
					return true;
				}
			}
		}

		return false;
	}

}
