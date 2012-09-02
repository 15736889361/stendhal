/* $Id$ */
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
package games.stendhal.server.actions;

import static games.stendhal.common.constants.Actions.BASEITEM;
import static games.stendhal.common.constants.Actions.BASEOBJECT;
import static games.stendhal.common.constants.Actions.BASESLOT;
import static games.stendhal.common.constants.Actions.TARGET;
import static games.stendhal.common.constants.Actions.TARGET_PATH;
import static games.stendhal.common.constants.Actions.USE;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.chest.Chest;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.EntitySlot;
import games.stendhal.server.util.EntityHelper;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * Uses an item or an other entity that implements Useable
 */
public class UseAction implements ActionListener {

	public static void register() {
		CommandCenter.register(USE, new UseAction());
	}

	public void onAction(final Player player, final RPAction action) {
		if (action.has(TARGET_PATH)) {
			useEntityFromPath(player, action);
		} else if (isItemInSlot(action)) {
			// When use is casted over something in a slot
			// Compatibility code
			useItemInSlot(player, action);
		} else if (action.has(TARGET)) {
			// Compatibility code
			useItemOnGround(player, action);
		}
	}
	
	/**
	 * Use an entity identified by TARGET_PATH.
	 * 
	 * @param player
	 * @param action
	 */
	private void useEntityFromPath(Player player, RPAction action) {
		Entity entity = EntityHelper.getEntityFromPath(player, action.getList(TARGET_PATH));
		
		if (entity.isContained() && !mayAccessContainedEntity(player, entity)) {
			return;
		}
		tryUse(player, entity);
	}

	private boolean isItemInSlot(final RPAction action) {
		return action.has(BASEITEM) && action.has(BASEOBJECT)
				&& action.has(BASESLOT);
	}

	private void useItemOnGround(final Player player, final RPAction action) {
		// use is cast over something on the floor
		// evaluate the target parameter
		final Entity entity = EntityHelper.entityFromTargetName(
				action.get(TARGET), player);

		if (entity != null) {
			tryUse(player, entity);
		}
	}

	private void useItemInSlot(final Player player, final RPAction action) {
		final EntitySlot slot = EntityHelper.getSlot(player, action);
		final Entity object = EntityHelper.entityFromSlot(player, action);
		if ((object != null) && canAccessSlot(player, slot, object.getBaseContainer())) {
			tryUse(player, object);
		}
	}
	
	/**
	 * Check if a player may access the location of a contained  item. Note that
	 * access rights to the item itself is <em>not</em> checked. That is done in
	 * tryUse().
	 * 
	 * @param player
	 * @param entity
	 * @return <code>true</code> if the player may access items in the location
	 * of item
	 */
	private boolean mayAccessContainedEntity(Player player, Entity entity) {
		RPObject parent = entity.getContainer();
		while ((parent != null) && (parent != entity)) {
			if (parent instanceof Item) {
				entity = (Item) parent;
				if (isItemBoundToOtherPlayer(player, entity)) {
					return false;
				}
				EntitySlot slot = getContainingSlot(entity);
				if ((slot == null) || !slot.isReachableForTakingThingsOutOfBy(player)) {
					return false;
				}
				parent = entity.getContainer();
			} else if (parent instanceof Corpse) {
				Corpse corpse = (Corpse) parent;
				if (!corpse.mayUse(player)) {
					player.sendPrivateText("Only " + corpse.getCorpseOwner() + " may access the corpse for now.");
					return false;
				}
				// Corpses are top level objects
				return true;
			} else if (parent instanceof Player) {
				// Only allowed to use item of our own player.
				return player == parent;
			} else if (parent instanceof Chest) {
				// No bound chests
				return true;
			} else {
				// Only allow to use objects from players, corpses, chests or
				// containing items
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Get the slot containing an entity.
	 * 
	 * @param entity
	 * @return containing slot
	 */
	private EntitySlot getContainingSlot(Entity entity) {
		RPSlot slot = entity.getContainerSlot();
		if (slot instanceof EntitySlot) {
			return (EntitySlot) slot;
		}
		return null;
	}

	private boolean canAccessSlot(final Player player, final EntitySlot slot, final RPObject base) {
		if (!((base instanceof Player) || (base instanceof Corpse) || (base instanceof Chest))) {
			// Only allow to use objects from players, corpses or chests
			return false;
		}

		if ((slot != null) && !slot.isReachableForTakingThingsOutOfBy(player)) {
			return false;
		}

		if ((base instanceof Player)
				&& !player.getID().equals(base.getID())) {
			// Only allowed to use item of our own player.
			return false;
		}
		
		if (base instanceof Corpse) {
			Corpse corpse = (Corpse) base;
			if (!corpse.mayUse(player)) {
				player.sendPrivateText("Only " + corpse.getCorpseOwner() + " may access the corpse for now.");
				
				return false;
			}
		}
		
		return true;
	}

	private void tryUse(final Player player, final RPObject object) {
		if (!canUse(player, object)) {
			return;
		}

		if (object instanceof UseListener) {
			final UseListener listener = (UseListener) object;
			logUsage(player, object);
			listener.onUsed(player);
		}
	}

	private boolean canUse(final Player player, final RPObject object) {
		return !isInJailZone(player, object) 
			&& !isItemBoundToOtherPlayer(player, object);
	}

	private boolean isInJailZone(final Player player, final RPObject object) {
		// HACK: No item transfer in jail (we don't want a jailed player to
		// use items like home scroll.
		final String zonename = player.getZone().getName();

		if ((object instanceof Item) && (zonename.endsWith("_jail"))) {
			player.sendPrivateText("For security reasons items may not be used in jail.");
			return true;
		}

		return false;
	}

	/**
	 * Make sure nobody uses items bound to someone else.
	 * @param player 
	 * @param object 
	 * @return true if item is bound false otherwise
	 */
	protected boolean isItemBoundToOtherPlayer(final Player player, final RPObject object) {
		if (object instanceof Item) {
			final Item item = (Item) object;
			if (item.isBound() && !player.isBoundTo(item)) {
				player.sendPrivateText("This "
						+ item.getName()
						+ " is a special reward for " + item.getBoundTo()
						+ ". You do not deserve to use it.");
				return true;
			}
		}
		return false;
	}

	/**
	 * Logs that this entity was used.
	 *
	 * @param player player using the entity
	 * @param object entity being used
	 */
	private void logUsage(final Player player, final RPObject object) {
		String name = object.get("type");
		if (object.has("name")) {
			name = object.get("name");
		}
		String infostring = "";
		if (object.has("infostring")) {
			infostring = object.get("infostring");
		}

		new GameEvent(player.getName(), USE, name, infostring).raise();
		
	}
}
