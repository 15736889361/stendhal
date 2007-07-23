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
package games.stendhal.server.entity.item;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.UseListener;

import java.util.Map;

import marauroa.common.game.RPObject;

/*
 * represents everything that can be consumed by RPentity.
 * Including food, poison, antidote ..
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 */
public class ConsumableItem extends StackableItem implements UseListener ,Comparable<ConsumableItem>{

	/** How much of this item has not yet been consumed. */
	private int left;

	public ConsumableItem(String name, String clazz, String subclass, Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
		left = getAmount();
	}

	/**
	 * copy constructor
	 *
	 * @param item item to copy
	 */
	public ConsumableItem(ConsumableItem item) {
		super(item);
		this.left = item.left;
	}


	public int getAmount() {
		return getInt("amount");
	}

	public int getFrecuency() {
		return getInt("frequency");
	}

	public int getRegen() {
		return getInt("regen");
	}

	/**
	 * Consumes a part of this item.
	 * @return The amount that has been consumed
	 */
	public int consume() {
		// note that amount and regen are negative for poison
		int consumedAmount;
		if (Math.abs(left) < Math.abs(getRegen())) {
			consumedAmount = left;
			left = 0;
		} else {
			consumedAmount = getRegen();
			left -= getRegen();
		}
		return consumedAmount;
	}

	/**
	 * Checks whether this item has already been fully consumed.
	 * @return true iff this item has been consumed
	 */
	public boolean consumed() {
		return left == 0;
	}

	/**
	 * verifies item is near to player.
	 * if so splits one single item of and calls consumeItem of the player
	 *
	 **/
	public boolean onUsed(RPEntity user) {
		Player player = (Player) user;
		if (isContained()) {
			// We modify the base container if the object change.
			RPObject base = getContainer();

			while (base.isContained()) {
				base = base.getContainer();
			}

			if (!user.nextTo((Entity) base)) {
				user.sendPrivateText("Consumable item is too far");
				return false;
			}
		} else {
			if (!nextTo(user)) {
				user.sendPrivateText("Consumable item is too far");
				return false;
			}
		}
		player.consumeItem((ConsumableItem) splitOff(1));
		player.notifyWorldAboutChanges();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ConsumableItem other) {

		float result=(float)other.getRegen()/(float)other.getFrecuency()-(float)getRegen()/(float)getFrecuency();
		return (int)Math.signum(result);
	}
}