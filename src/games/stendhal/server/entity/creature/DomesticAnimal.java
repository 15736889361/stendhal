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
package games.stendhal.server.entity.creature;

import games.stendhal.server.StendhalRPWorld;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Food;
import games.stendhal.server.entity.player.Player;

import javax.management.AttributeNotFoundException;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.game.RPObject;

/**
 * A domestic animal can be owned by a player; each player can't own more
 * than one domestic animal. It has a weight; when it dies, it leaves an
 * amount of meat, depending on its weight.
 */
public abstract class DomesticAnimal extends Creature {

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(DomesticAnimal.class);

	protected int weight;

	/**
	 * The player who owns the domestic animal, or null if the animal is wild.
	 */
	protected Player owner;

	/**
	 * Creates a new wild DomesticAnimal.
	 * @throws AttributeNotFoundException
	 */
	public DomesticAnimal() {
		this(null);
		put("title_type", "friend");
	}

	/**
	 * Creates a new DomesticAnimal that is owned by a player.
	 * @throws AttributeNotFoundException
	 */
	public DomesticAnimal(Player owner) {
		this.owner = owner;
		put("title_type", "friend");

		put("x", 0);
		put("y", 0);

		setWidth(1);
		setHeight(1);
	}

	/**
	 * Creates a wild DomesticAnimal based on an existing RPObject.
	 * @param object
	 * @throws AttributeNotFoundException
	 */
	public DomesticAnimal(RPObject object) {
		super(object);
		put("title_type", "friend");

		if (object.has("title_type")) {
			put("title_type", object.get("title_type"));
		}

		if (!has("width")) {
			setWidth(1);
		}

		if (!has("height")) {
			setHeight(1);
		}
	}

	/**
	 * Creates a wild DomesticAnimal based on an existing RPObjec, and assigns
	 * it to a player.
	 * @param object
	 * @param owner The player who should own the sheep
	 * @throws AttributeNotFoundException
	 */
	public DomesticAnimal(RPObject object, Player owner) {
		this(object);
		this.owner = owner;
	}


	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public Player getOwner() {
		return owner;
	}

	@Override
	public void update() {
		super.update();
		if (has("weight")) {
			weight = getInt("weight");
		}
	}

	public void setWeight(int weight) {
		this.weight = weight;
		put("weight", weight);
	}

	public int getWeight() {
		return weight;
	}

	protected void moveToOwner() {
		logger.debug("Domestic animal (owner) moves to owner");
		setIdea("follow");
		setMovement(owner, 0, 0, 20);
		//setAsynchonousMovement(owner,0,0);
	}

	protected void moveRandomly() {
		setIdea("walk");
		setRandomPathFrom(getX(), getY(), 10);
	}

	/**
	 * Can be called when the sheep dies. Puts meat onto its corpse; the
	 * amount of meat depends on the domestic animal's weight.
	 * @param corpse The corpse on which to put the meat
	 */
	@Override
	protected void dropItemsOn(Corpse corpse) {
		Food food = (Food) StendhalRPWorld.get().getRuleManager().getEntityManager().getItem("meat");
		food.setQuantity(getWeight() / 10 + 1);
		corpse.add(food);
	}

	@Override
	protected void handleObjectCollision() {
		stop();
		clearPath();
	}

	@Override
	protected void handleSimpleCollission(int nx, int ny) {
		stop();
		clearPath();
	}

}
