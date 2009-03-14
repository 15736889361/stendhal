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
package games.stendhal.server.entity.creature;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

import java.util.LinkedList;

import org.apache.log4j.Logger;

/**
 * An ItemGuardCreature is a creature that is responsible for guarding a special
 * item (e.g. a key). Once it is killed, a copy of this special item is given to
 * the player who killed it.
 *
 * If a quest is specified then the player only gets the item if the quest isn't completed.
 */
public class ItemGuardCreature extends Creature {

	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(ItemGuardCreature.class);

	private final String itemType;

	private final String questSlot;

	/**                             
	 * Creates an ItemGuardCreature.
	 * @param copy
	 *            base creature
	 * @param itemType  
	 *            the quest item to drop on death   
	 */
	public ItemGuardCreature(final Creature copy, final String itemType) {
		this(copy, itemType, null);
	}

	/**
	 * Creates an ItemGuardCreature.
	 * 
	 * @param copy
	 *            base creature
	 * @param itemType
	 *            the quest item to drop on death
	 * @param questSlot
	 *            the quest slot for the active quest
	 */
	public ItemGuardCreature(final Creature copy, final String itemType, final String questSlot) {
		super(copy);

		this.itemType = itemType;
		this.questSlot = questSlot;

		noises = new LinkedList<String>(noises);
		noises.add("Thou shall not obtain the " + itemType + "!");
	
		if (!SingletonRepository.getEntityManager().isItem(
				itemType)) {
			logger.error(copy.getName() + " drops unexisting item " + itemType);
		}
	}

	@Override
	public Creature getNewInstance() {
		return new ItemGuardCreature(this, itemType, questSlot);
	}

	@Override
	public void onDead(final Entity killer, final boolean remove) {
		
		if (killer instanceof Player) {
			final Player killerPlayer = (Player) killer;
			if (!killerPlayer.isEquipped(itemType) && !killerPlayer.isQuestCompleted(questSlot)) {
				final Item item = SingletonRepository.getEntityManager().getItem(
						itemType);
				item.setBoundTo(killerPlayer.getName());
				killerPlayer.equipOrPutOnGround(item);
			}
		}
		super.onDead(killer, remove);
	}
}
