/*
 * @(#) src/games/stendhal/server/entity/WalkBlocker.java
 *
 * $Id$
 */

package games.stendhal.server.entity;

import java.awt.geom.Rectangle2D;

import marauroa.common.game.AttributeNotFoundException;

/**
 * An entity that just acts as an obsticle. This is a temporary workaround
 * to allow items to be placed, but not players/entities, until multi-level
 * collisions can be added.
 */
public class WalkBlocker extends Entity {

	/**
	 * Create a walk blocker.
	 */
	public WalkBlocker() throws AttributeNotFoundException {
		put("type", "walk_blocker");
	}

	
	/**
	 * Determine if this is an obstacle for another entity.
	 *
	 * @param	entity		The entity to check against.
	 *
	 * @return	<code>true</code> if the other entity is an RPEntity.
	 */
	@Override
	public boolean isObstacle(Entity entity) {
		return (entity instanceof RPEntity);
	}
}
