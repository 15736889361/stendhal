package games.stendhal.server.entity.npc.condition;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Does the quest state start with the specified String?
 */
public class QuestStateStartsWithCondition extends SpeakerNPC.ChatCondition {

	private final String questname;
	private final String state;

	/**
	 * Creates a new QuestStateStartsWithCondition.
	 * 
	 * @param questname
	 *            name of quest-slot
	 * @param state
	 *            start of state-string
	 */
	public QuestStateStartsWithCondition(final String questname, final String state) {
		this.questname = questname;
		this.state = state;
	}

	@Override
	public boolean fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
		return (player.hasQuest(questname) && player.getQuest(questname).startsWith(
				state));
	}

	@Override
	public String toString() {
		return "QuestStateStartsWith <" + questname + "," + state + ">";
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false,
				QuestStartedCondition.class);
	}
}