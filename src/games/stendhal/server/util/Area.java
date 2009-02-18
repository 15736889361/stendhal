package games.stendhal.server.util;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.player.Player;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.game.IRPZone;

/**
 * An area is a specified place on a specified zone like (88, 78) to (109, 98)
 * in 0_ados_wall_n.
 * 
 * @author hendrik
 */
public class Area {

	private final IRPZone zone;

	private final Shape shape;

	/**
	 * Creates a new Area.
	 * 
	 * @param zone
	 *            name of the map
	 * @param shape
	 *            shape on that map
	 */
	public Area(final IRPZone zone, final Rectangle2D shape) {
		this.zone = zone;
		this.shape = shape;
	}

	/**
	 * Checks whether an entity is in this area (e. g. on this zone and inside of
	 * the shape)
	 * 
	 * @param entity
	 *            An entity to check
	 * @return true, if and only if the entity is in this area.
	 */
	public boolean contains(final Entity entity) {
		if (entity == null) {
			return false;
		}
		final IRPZone entityZone = entity.getZone();

		// We have ask the zone whether it knows about the entity because
		// player-objects stay alive some time after logout.
		return zone.equals(entityZone) && zone.has(entity.getID())
				&& shape.contains(entity.getX(), entity.getY());
	}

	/**
	 * Gets the shape.
	 * 
	 * @return shape
	 */
	public Shape getShape() {
		return shape;
	}

	/**
	 * Gets a list of players in the area
	 * 
	 * @return  A list of all players in the area.
	 */
    public List<Player> getPlayers() {
	// interface marauroa.common.game.IRPZone doesn't have a method getPlayers, so cast it to StendhalRPZone
        final List<Player> playersInZone = ((StendhalRPZone) zone).getPlayers();
	// for each of the players in the zone, check contains(player)
	final List<Player> result = new LinkedList<Player>();
	for (Player player : playersInZone) {
	    if (this.contains(player)) {
		result.add(player);
	    }
	}
	return result;
    }

}
