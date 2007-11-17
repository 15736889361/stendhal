package games.stendhal.server.entity.npc.condition;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Was one of theses trigger phrases said? (Use with a ""-trigger in npc.add)
 */
public class TriggerInListCondition extends SpeakerNPC.ChatCondition {
	private List<String> triggers = new LinkedList<String>();

	/**
	 * Creates a new TriggerInListCondition
	 *
	 * @param trigger list of trigger
	 */
	public TriggerInListCondition(String... trigger) {
		this(Arrays.asList(trigger));
	}

	/**
	 * Creates a new TriggerInListCondition
	 *
	 * @param trigger list of trigger
	 */
	public TriggerInListCondition(List<String> trigger) {
		for (String trig : trigger) {
			this.triggers.add(trig.trim().toLowerCase());
		}
	}

	@Override
	public boolean fire(Player player, String text, SpeakerNPC engine) {
		return triggers.contains(text.trim().toLowerCase());
	}

	@Override
	public String toString() {
		return "trigger <" + triggers.toString() + ">";
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false, QuestStartedCondition.class);
	}
}