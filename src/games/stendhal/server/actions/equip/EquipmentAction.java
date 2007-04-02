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
package games.stendhal.server.actions.equip;

import games.stendhal.server.StendhalRPRuleProcessor;
import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.entity.Chest;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Stackable;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

import org.apache.log4j.Logger;

/**
 * This listener handles all entity movements from a slot to
 * either another slot or the ground.
 *  
 *  The source can be:
 *    baseitem   - object id of the item which should be moved
 *    
 *  (optional, only when the source is inside a slot)    
 *    baseobject - (only when the item is in a slot) object id of the object containing the slot where the item is in
 *    baseslot   - (only when the item is in a slot) slot name where the item is in
 *  (/optional)    
 *    
 *    
 *  The target can be either an 'equip':
 *    type         - "equip"
 *    targetobject - object id of the container object 
 *    targetslot   - slot name where the item should be moved to
 *    
 *  or a 'drop':
 *    type         - "drop"
 *    x            - the x-coordinate on the ground  
 *    y            - the y-coordinate on the ground  
 */
public class EquipmentAction implements ActionListener {

	static final Logger logger = Log4J.getLogger(EquipmentAction.class);

	/** the list of valid container classes */
	private static final Class[] validContainerClasses = new Class[] { Player.class, Chest.class, Corpse.class };


	/** List of the valid container classes for easy access */
	private List<Class> validContainerClassesList;

	/**
	 * registers "equip" and "drop" action processors
	 */
	public static void register() {
		EquipmentAction equip = new EquipmentAction();
		StendhalRPRuleProcessor.register("equip", equip);
		StendhalRPRuleProcessor.register("drop", equip);
	}

	/** constuctor */
	public EquipmentAction() {
		validContainerClassesList = Arrays.asList(validContainerClasses);
	}

	public void onAction(Player player, RPAction action) {

		// HACK: No item transfer in jail (we don't want a jailed player to
		//       create a new free character and give it all items.
		if (StendhalRPWorld.get().getRPZone(player.getID()).getID().getID().endsWith("_jail")) {
			player.sendPrivateText("For security reasons, items may not be moved around in jail.");
			return;
		}

		if (action.get(EquipActionConsts.TYPE).equals("equip")) {
			onEquip(player, action);
		} else {
			onDrop(player, action);
		}
	}

	/** callback for the equip action */
	private void onEquip(Player player, RPAction action) {
		Log4J.startMethod(logger, "equip");

		// get source and check it
		SourceObject source = new SourceObject(action, player);
		if (!source.isValid() || !source.checkDistance(player, EquipActionConsts.MAXDISTANCE)
		        || !source.checkClass(validContainerClassesList)) {
			// source is not valid
			logger.debug("Source is not valid");
			return;
		}

		// is the entity unbound or bound to the right player?
		Entity entity = source.getEntity();
		String itemName = "entity";
		if (entity.has("name")) {
			itemName = entity.get("name").replace("_", " ");
		} else if (entity instanceof Item) {
			itemName = "item";
		}
		if (entity.has("bound") && !player.getName().equals(entity.get("bound"))) {
			player.sendPrivateText("This " + itemName + " is a special reward for " + entity.get("bound")
			        + ". You do not deserve to use it.");
			return;
		}

		// get destination and check it
		DestinationObject dest = new DestinationObject(action, player);
		if (!dest.isValid() || !dest.checkDistance(player, EquipActionConsts.MAXDISTANCE) || !dest.checkClass(validContainerClassesList)) {
			// destination is not valid
			logger.debug("Destination is not valid");
			return;
		}

		// looks good
		source.moveTo(dest, player);
		int amount = 1;
		if (entity instanceof StackableItem) {
			amount = ((StackableItem) entity).getQuantity();
		}
		StendhalRPRuleProcessor.get().addGameEvent(player.getName(), "equip", itemName, source.getSlot(), dest.getSlot(), Integer.toString(amount));

		player.updateItemAtkDef();

		Log4J.finishMethod(logger, "equip");
	}

	private void onDrop(Player player, RPAction action) {
		Log4J.startMethod(logger, "drop");

		final boolean USE_NEW_CODE = false;

		// New Code Starts Here
		// FIXME: This code does not work.

		if (USE_NEW_CODE) {
			// get source and check it
			SourceObject source = new SourceObject(action, player);
			if (!source.isValid() || !source.checkDistance(player, EquipActionConsts.MAXDISTANCE)
			        || !source.checkClass(validContainerClassesList)) {
				// source is not valid
				return;
			}

			// get destination and check it
			DestinationObject dest = new DestinationObject(action, player);
			if (!dest.isValid() || !dest.checkDistance(player, 5.0) || !dest.checkClass(validContainerClassesList)) {
				logger.warn("destination is invalid. action is: " + action);
				// destination is not valid
				return;
			}

			Entity entity = source.getEntity();
			String itemName = "entity";
			if (entity.has("name")) {
				itemName = entity.get("name").replace("_", " ");
			} else if (entity instanceof Item) {
				itemName = "item";
			}
			
			// FIXME: This will remove the item from the slot, but it does not
			// reappear
			// on the ground. See DestinationObject.addToWorld()#600
			source.moveTo(dest, player);
			if (entity.has("bound")) {
				player.sendPrivateText("You put a valuable item on the ground. Please note that it will expire in " + (Item.DEGRADATION_TIMEOUT / 60) + " minutes, as all items do. But in this case there is no way to restore it.");
			}
			int amount = 1;
			if (entity instanceof StackableItem) {
				amount = ((StackableItem) entity).getQuantity();
			}
			StendhalRPRuleProcessor.get().addGameEvent(player.getName(), "drop", itemName, source.getSlot(), dest.getSlot(), Integer.toString(amount));
			return;
		}
		// Old Code Starts Here

		if (action.has("baseobject") && action.has("baseslot") && action.has("x") && action.has("y")
		        && action.has("baseitem")) {
			StendhalRPZone zone = (StendhalRPZone) StendhalRPWorld.get().getRPZone(player.getID());

			int baseObject = action.getInt("baseobject");

			RPObject.ID baseobjectid = new RPObject.ID(baseObject, zone.getID());
			if (!zone.has(baseobjectid)) {
				return;
			}

			RPObject base = zone.get(baseobjectid);
			if (!((base instanceof Player) || (base instanceof Corpse) || (base instanceof Chest))) {
				// Only allow to drop objects from players, corpses or chests
				return;
			}

			if ((base instanceof Player) && !player.getID().equals(base.getID())) {
				// Only allowed to drop item of our own player.
				return;
			}

			Entity baseEntity = (Entity) base;

			if (baseEntity.hasSlot(action.get("baseslot"))) {
				RPSlot slot = baseEntity.getSlot(action.get("baseslot"));

				if (slot.size() == 0) {
					return;
				}

				RPObject object = null;
				int item = action.getInt("baseitem");
				// scan through the slot to find the requested item
				for (RPObject rpobject : slot) {
					if (rpobject.getID().getObjectID() == item) {
						object = rpobject;
						break;
					}
				}

				// no item found...we take the first one
				if (object == null) {
					object = slot.iterator().next();
				}

				// It is always an entity
				Entity entity = (Entity) object;

				int x = action.getInt("x");
				int y = action.getInt("y");

				int quantity = 0;

				if ((entity instanceof Stackable) && action.has(EquipActionConsts.QUANTITY)) {
					int entityQuantity = ((Stackable) entity).getQuantity();

					quantity = action.getInt(EquipActionConsts.QUANTITY);
					if ((entityQuantity < 1) || (quantity < 1) || (quantity >= entityQuantity)) {
						quantity = 0; // quantity == 0 performs a regular move
						// of the entire item
					}
				}

				if (player.nextTo(baseEntity) && (baseEntity.squaredDistance(x, y) < 8 * 8)
				        && !zone.simpleCollides(entity, x, y)) {
					if (quantity != 0) {
						StackableItem newItem = ((StackableItem) entity).splitOff(quantity);

						zone.assignRPObjectID(newItem);
						newItem.set(x, y);
						zone.add(newItem, player);
					} else {
						slot.remove(entity.getID());

						entity.setX(x);
						entity.setY(y);

						zone.assignRPObjectID(entity);
						zone.add(entity, player);

						// HACK: Avoid a problem on database
						if (entity.has("#db_id")) {
							entity.remove("#db_id");
						}
					}
					if (entity.has("bound")) {
						player.sendPrivateText("You put a valuable item on the ground. Please note that it will expire in " + (Item.DEGRADATION_TIMEOUT / 60) + " minutes, as all items do. But in this case there is no way to restore it.");
					}

					String itemName = "entity";
					if (entity.has("name")) {
						itemName = entity.get("name").replace("_", " ");
					} else if (entity instanceof Item) {
						itemName = "item";
					}
					
					int amount = 1;
					if (entity instanceof StackableItem) {
						amount = ((StackableItem) entity).getQuantity();
					}
					StendhalRPRuleProcessor.get().addGameEvent(player.getName(), "drop", itemName, slot.getName(), "ground", Integer.toString(amount));

					
					baseEntity.notifyWorldAboutChanges();
				}
			}
		}

		player.updateItemAtkDef();

		Log4J.finishMethod(logger, "drop");
	}



}
