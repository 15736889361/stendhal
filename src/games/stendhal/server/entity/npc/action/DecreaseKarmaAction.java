package games.stendhal.server.entity.npc.action;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.parser.Sentence;
import games.stendhal.server.entity.player.Player;

/**
 * Decreases the karma of the current player.
 */
public class DecreaseKarmaAction implements ChatAction {

	private final double karmaDiff;

	/**
	 * Creates a new DecreaseKarmaAction.
	 * 
	 * @param karmaDiff
	 *            amount of karma to subtract
	 */
	public DecreaseKarmaAction(final double karmaDiff) {
		this.karmaDiff = karmaDiff;
	}

	public void fire(final Player player, final Sentence sentence, final SpeakerNPC engine) {
		player.addKarma(-1 * karmaDiff);
	}

	@Override
	public String toString() {
		return "DecreaseKarma<" + karmaDiff + ">";
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(karmaDiff);
		result = PRIME * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DecreaseKarmaAction other = (DecreaseKarmaAction) obj;
		if (Double.doubleToLongBits(karmaDiff) != Double.doubleToLongBits(other.karmaDiff)) {
			return false;
		}
		return true;
	}
}