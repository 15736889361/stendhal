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

package games.stendhal.server.entity.npc;

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.rule.EntityManager;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import marauroa.common.Log4J;
import marauroa.common.game.IRPZone;

/**
 * Represents the behaviour of a NPC who is able to sell items
 * to a player.
 */
public class SellerBehaviour extends MerchantBehaviour {

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(SellerBehaviour.class);

	/**
	 * Creates a new SellerBehaviour with an empty pricelist.
	 */
	public SellerBehaviour() {
		super(new HashMap<String, Integer>());
	}

	/**
	 * Creates a new SellerBehaviour with a pricelist.
	 *
	 * @param priceList list of item names and their prices
	 */
	public SellerBehaviour(Map<String, Integer> priceList) {
		super(priceList);
	}

	/**
	 * Transacts the sale that has been agreed on earlier via
	 * setChosenItem() and setAmount().
	 * @param seller The NPC who sells
	 * @param player The player who buys
	 * @return true iff the transaction was successful, that is when the
	 *              player was able to equip the item(s).
	 */
	@Override
	protected boolean transactAgreedDeal(SpeakerNPC seller, Player player) {
		StendhalRPWorld world = StendhalRPWorld.get();
		EntityManager manager = world.getRuleManager().getEntityManager();

		Item item = manager.getItem(chosenItem);
		if (item == null) {
			logger.error("Trying to sell an nonexistant item: " + chosenItem);
			return false;
		}

		// TODO: When the user tries to buy several of a non-stackable
		// item, he is forced to buy only one.
		if (item instanceof StackableItem) {
			((StackableItem) item).setQuantity(getAmount());
		} else {
			if(getAmount()!=1) {
				player.sendPrivateText("You can only buy StackableItems in amounts bigger than 1. Setting amount to 1.");
			}
			
			setAmount(1);			
		}

		item.put("zoneid", player.get("zoneid"));
		IRPZone zone = world.getRPZone(player.getID());
		zone.assignRPObjectID(item);

		if (player.isEquipped("money", getCharge(player))) {
			if (player.equip(item)) {
				player.drop("money", getCharge(player));
				seller.say("Congratulations! Here is your " + chosenItem + "!");
				return true;
			} else {
				seller.say("Sorry, but you cannot equip the " + chosenItem + ".");
				return false;
			}
		} else {
			seller.say("Sorry, you don't have enough money!");
			return false;
		}
	}
}
