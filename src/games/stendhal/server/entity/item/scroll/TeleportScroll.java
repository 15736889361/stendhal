/* $Id$
 /***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item.scroll;

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.player.Player;

import java.util.Map;

/**
 * Represents a general teleport scroll.
 */
public abstract class TeleportScroll extends InfoStringScroll {

	/**
	 * Creates a new teleport scroll
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public TeleportScroll(String name, String clazz, String subclass, Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * copy constructor
	 *
	 * @param item item to copy
	 */
	public TeleportScroll(TeleportScroll item) {
		super(item);
	}

	/**
	 * Is invoked when a teleporting scroll is actually used.
	 *
	 * @param player The player who used the scroll and who will be
	 * teleported
	 *
	 * @return true iff teleport was successful
	 */
	protected abstract boolean useTeleportScroll(Player player);

	/**
	 * Is invoked when a teleporting scroll is used. Tries to put the
	 * player on the scroll's destination, or near it. 
	 * @param player The player who used the scroll and who will be teleported
	 * @return true iff teleport was successful
	 */
	@Override
	protected boolean useScroll(Player player) {
		StendhalRPZone zone = (StendhalRPZone) StendhalRPWorld.get().getRPZone(player.getID());
		if (!zone.isTeleportAllowed()) {
			player.sendPrivateText("The strong anti magic aura in this area prevents the scroll from working!");
			return false;
		}

		return useTeleportScroll(player);
	}
}
