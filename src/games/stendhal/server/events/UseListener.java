package games.stendhal.server.events;

import games.stendhal.server.entity.RPEntity;

/**
 * Implementing classes will be called back when a player uses them. 
 */
public interface UseListener {

	/**
	 * Invoken when the object is used
	 *
	 * @param user the RPEntity who uses the object
	 */
	public void onUsed(RPEntity user);
}
