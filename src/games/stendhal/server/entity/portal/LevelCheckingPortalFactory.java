/*
 * @(#) src/games/stendhal/server/entity/portal/LevelCheckingPortalFactory.java
 *
 * $Id$
 */

package games.stendhal.server.entity.portal;

import games.stendhal.server.config.factory.ConfigurableFactoryContext;

/**
 * A factory for <code>LevelCheckingPortal</code> objects.
 */
public class LevelCheckingPortalFactory extends AccessCheckingPortalFactory {
	//
	// LevelCheckingPortalFactory
	//

	/**
	 * Extract the maximum player level from a context.
	 *
	 * @param	ctx		The configuration context.
	 * @return	The level.
	 * @throws	IllegalArgumentException If the level attribute is invalid.
	 */
	protected int getMaximumLevel(ConfigurableFactoryContext ctx) throws IllegalArgumentException {
		return ctx.getInt("maximum-level", LevelCheckingPortal.DEFAULT_MAX);
	}

	/**
	 * Extract the minimum player level from a context.
	 *
	 * @param	ctx		The configuration context.
	 * @return	The level.
	 * @throws	IllegalArgumentException If the level attribute is invalid.
	 */
	protected int getMinimumLevel(ConfigurableFactoryContext ctx) throws IllegalArgumentException {
		return ctx.getInt("maximum-level", LevelCheckingPortal.DEFAULT_MIN);
	}


	//
	// AccessCheckingPortalFactory
	//

	/**
	 * Create a level checking portal.
	 *
	 * @param	ctx	Configuration context.
	 *
	 * @return	A Portal.
	 *
	 * @throws	IllegalArgumentException
	 *				If there is a problem with the
	 *				attributes. The exception message
	 *				should be a value sutable for
	 *				meaningful user interpretation.
	 *
	 * @see		LevelCheckingPortal
	 */
	@Override
	protected AccessCheckingPortal createPortal(ConfigurableFactoryContext ctx) throws IllegalArgumentException {
		return new LevelCheckingPortal(getMinimumLevel(ctx), getMaximumLevel(ctx));
	}
}
