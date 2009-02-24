package games.stendhal.server.entity.npc.condition;

import games.stendhal.common.MathHelper;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Has 'delay' time passed since the quest was last done?
 */
public class TimePassedCondition implements ChatCondition {

	private final String questname;
	private final int delay;
	private final int arg;
	
	/**
	 * Creates a new TimePassedCondition 
	 * 
	 * @param questname
	 *            name of quest-slot
	 * @param delay
	 *            delay in minutes
	 * @param arg
	 *            position of the timestamp within the quest slot 'array'
	 */
	public TimePassedCondition(final String questname, final int delay, final int arg) {
		this.questname = questname;
		this.delay = delay;
		this.arg = arg;
	}
	/**
	 * Creates a new TimePassedCondition, where the timestamp alone is stored in the quest state
	 * 
	 * @param questname
	 *            name of quest-slot
	 * @param delay
	 *            delay in minutes
	 */
	public TimePassedCondition(final String questname, final int delay) {
		this.questname = questname;
		this.delay = delay;
		this.arg = 0;
	}

	public boolean fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
		if (!player.hasQuest(questname)) {
			return false;
		} else {
			final String[] tokens = player.getQuest(questname).split(";"); 
			final long delayInMilliseconds = delay * MathHelper.MILLISECONDS_IN_ONE_MINUTE; 
		
			// timeRemaining is ''time when quest was done +
			// delay - time now''
			// if this is > 0, the time has not yet passed
			final long timeRemaining = (Long.parseLong(tokens[arg]) + delayInMilliseconds)
				- System.currentTimeMillis();
			// TODO: return an error if tokens.length < arg 
			// TODO: catch the number format exception in case tokens[arg] is no number? or does parseLong do this?
		return (timeRemaining <= 0L);
		}
	}

	@Override
	public String toString() {
		return delay + " minutes passed since last doing quest " + questname + "?";
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false,
				TimePassedCondition.class);
	}
}
