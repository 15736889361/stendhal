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

import games.stendhal.server.StendhalRPRuleProcessor;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

import java.util.Set;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.SyntaxException;
import marauroa.common.game.Definition.Type;

/**
 * A pet is a domestic animal that can be owned by a player. It eats chicken
 * from the ground. They move faster than sheep.
 *
 * Pets starve if they are not fed. They can die.
 *
 * TODO: pets attack weak animals for you
 */
/**
 * @author kymara
 *
 */
public abstract class Pet extends DomesticAnimal {

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(Pet.class);

	/**
	 * The amount of hunger that indicates hungry.
	 */
	protected static final int HUNGER_HUNGRY = 50;

	/**
	 * The amount of hunger that indicates starvation.
	 */
	protected static final int HUNGER_STARVATION = 400;

	/**
	 * The weight at which the pet will stop eating.
	 */
	public final int MAX_WEIGHT = 100;

	protected static int HP = 100;

	protected static int incHP = 2;

	protected static int ATK = 10;

	protected static int DEF = 20;

	protected static int XP;

	protected int hunger;

	public static void generateRPClass() {
		try {
			RPClass pet = new RPClass("pet");
			pet.isA("creature");
			pet.addAttribute("weight", Type.BYTE);
			pet.addAttribute("eat", Type.FLAG);
		} catch (SyntaxException e) {
			logger.error("cannot generate RPClass", e);
		}
	}

	/**
	 * Creates a new wild Pet.
	 *
	 * @throws AttributeNotFoundException
	 */
	public Pet() {
		this(null);
	}

	/**
	 * Creates a new Pet that is owned by a player.
	 *
	 * @throws AttributeNotFoundException
	 */
	public Pet(Player owner) {
		super(owner);
		baseSpeed = 0.5;

		setATK(ATK);
		setDEF(DEF);
		setXP(XP);
		setBaseHP(HP);
		setHP(HP);

		hunger = 0;

	}

	/**
	 * Creates a Pet based on an existing pet RPObject, and assigns it to a
	 * player.
	 *
	 * @param object
	 * @param owner
	 *            The player who should own the pet
	 * @throws AttributeNotFoundException
	 */
	public Pet(RPObject object, Player owner) {
		super(object, owner);
		baseSpeed = 0.5;
		hunger = 0;
	}

	/**
	 * Is called when the pet dies. Removes the dead pet from the owner.
	 *
	 * @param killer
	 *            The entity who caused the death
	 */
	@Override
	public void onDead(String killername) {
		if (owner != null) {
			if (owner.hasPet()) {
				owner.removePet(this);
			} else {
				logger.warn("INCOHERENCE: Pet " + this + " isn't owned by "
						+ owner);
			}
		} else {
			StendhalRPRuleProcessor.get().removeNPC(this);
		}

		super.onDead(killername);
	}

	/**
	 * Returns the PetFood that is nearest to the pet's current position. If
	 * there is no PetFood within the given range, returns none.
	 *
	 * @param range
	 *            The maximum distance to a PetFood
	 * @return The nearest PetFood, or null if there is none within the given
	 *         range
	 */
	private Item getNearestFood(double range) {

		Set<Item> items = getZone().getItemsOnGround();
		double squaredDistance = range * range; // This way we save several sqrt
		// operations
		Item chosen = null;
		for (Item i : items) {
			if (canEat(i)) {
				if (this.squaredDistance(i) < squaredDistance) {
					chosen = i;
					squaredDistance = this.squaredDistance(i);
				}
			}

		}

		return chosen;
	}

	boolean canEat(Item i) {

		return "chicken".equals(i.getItemSubclass());

	}

	private void eat(Item food) {
		if (weight < MAX_WEIGHT) {
			setWeight(weight + 1);
		}
		food.removeOne();
		hunger = 0;
		if (getHP() < getBaseHP()) {
			healSelf(incHP, 100);
		}
	}

	//
	// RPEntity
	//

	/**
	 * Determines what the pet shall do next.
	 */
	@Override
	public void logic() {

		if (!isEnemyNear(20) && (owner == null)) {
			// if noone near and noone owns us ....
			stop();
			notifyWorldAboutChanges();
			return;
		}
		setPath(null);
		setIdea(null);
		hunger++;

		Item food = getNearestFood(6);
		// Show 'food' idea whenever hungry
		if (hunger > HUNGER_HUNGRY) {
			setIdea("food");

			if ((food != null)) {
				if (nextTo(food)) {
					logger.debug("Pet eats");
					setIdea("eat");
					eat(food);
					clearPath();
					stop();
				} else {
					logger.debug("Pet moves to food");
					setIdea("food");
					setMovement(food, 0, 0, 20);
					// setAsynchonousMovement(food,0,0);
				}

			} else if (hunger > HUNGER_STARVATION) {
				// move crazy if starving
				moveRandomly();
				setIdea("food");
				hunger /= 2;
				// TODO: Find out how to make it so owner doesn't also get this
				// message every time
				// owner changes zone, then it may be uncommented.
				// if (owner != null){
				// owner.sendPrivateText("Your pet is starving!");
				// }
				logger.debug("Pet starves");
				if (weight > 0) {
					setWeight(weight - 1);
				} else {
					damage(2, "starvation");
					// TODO: URGENT! Cat can die here! and the removePet() call
					// isn't working!
					notifyWorldAboutChanges();
					if (getHP() <= 0) {
						return;
					}
				}
			} else {
				// here, (hunger_hungry < hunger < starvation) && not near food
				// so, here, we follow owner, if we have one
				// and if we don't, we do the other stuff
				if (owner == null) {
					logger.debug("Pet (ownerless and hungry but not starving) moves randomly");
					moveRandomly();
					// unfortunately something in moveRandomly overwrites the
					// hungry idea
					// :( must i change moveRandomly,
					// or is setting it again here after the one in
					// moveRandomly() enough?
					setIdea("food"); // try it!
				} else if ((owner != null) && !nextTo(owner)) {
					moveToOwner();
				} else {
					logger.debug("Pet has nothing to do and is hungry but not starving");
					stop();
					clearPath();
				}
			}
		} else {
			if (owner == null) {
				logger.debug("Pet (ownerless) moves randomly");
				moveRandomly();
			} else if ((owner != null) && !nextTo(owner)) {
				moveToOwner();
			} else {
				logger.debug("Pet has nothing to do");
				setIdea(null);
				stop();
				clearPath();
			}
		}

		// this is from the same code as saying 'sheep' to bring a sheep to you.
		// but people are more likely to try 'cat' than 'pet'. can get type
		// instead?
		if ((owner != null) && owner.has("text")
				&& owner.get("text").contains("pet")) {
			clearPath();
			moveToOwner();
		}

		this.applyMovement();

		notifyWorldAboutChanges();

	}

	// Should never be called
	@Override
	public String describe() {
		String text = "You see a pet; it looks like it weighs about " + weight
				+ ".";
		if (hasDescription()) {
			text = getDescription();
		}
		return (text);
	}

}
