/*
 * @(#) src/games/stendhal/server/entity/area/DamagingAreaFactory.java
 *
 * $Id$
 */

package games.stendhal.server.entity.area;

import games.stendhal.server.config.factory.ConfigurableFactoryContext;

/**
 * A base factory for <code>DamagingArea</code> objects.
 */
public class DamagingAreaFactory extends OccupantAreaFactory {

	/**
	 * Extract the damage amount from a context.
	 *
	 * @param	ctx		The configuration context.
	 * @return	The damage amount.
	 * @throws	IllegalArgumentException If the attribute is missing.
	 */
	protected int getDamage(ConfigurableFactoryContext ctx) {
		return ctx.getRequiredInt("damage");
	}

	/**
	 * Extract the moving damage probability (as percent) from a context.
	 *
	 * @param	ctx		The configuration context.
	 *
	 * @return	The damage probability (0.0 - 1.0).
	 *
	 * @throws	IllegalArgumentException
	 *				If the attribute is invalid.
	 */
	protected double getProbability(ConfigurableFactoryContext ctx) {
		return ctx.getInt("probability", 0) / 100.0;
	}

	@Override
	protected OccupantArea createArea(ConfigurableFactoryContext ctx) {
		return new DamagingArea(getName(ctx), getWidth(ctx), getHeight(ctx), getDamage(ctx), getInterval(ctx), getProbability(ctx));
	}
}
