package games.stendhal.server.entity.slot;

import games.stendhal.server.entity.Entity;

/**
 * Slots of players which contain items
 *
 * @author hendrik 
 */
public class PlayerSlot extends EntitySlot {

	/**
	 * Creates a new PlayerSlot
	 *
	 * @param name name of slot
	 */
	public PlayerSlot(String name) {
		super(name);
	}

	@Override
	public boolean isReachableForTakingThingsOutOfBy(Entity entity) {
		return super.hasAsAncestor(entity);
	}
}
