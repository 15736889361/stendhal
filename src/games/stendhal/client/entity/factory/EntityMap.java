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

package games.stendhal.client.entity.factory;

import games.stendhal.client.entity.Blood;
import games.stendhal.client.entity.BossCreature;
import games.stendhal.client.entity.Box;
import games.stendhal.client.entity.CarrotGrower;
import games.stendhal.client.entity.Chest;
import games.stendhal.client.entity.Corpse;
import games.stendhal.client.entity.Creature;
import games.stendhal.client.entity.Door;
import games.stendhal.client.entity.Entity;
import games.stendhal.client.entity.Fire;
import games.stendhal.client.entity.FishSource;
import games.stendhal.client.entity.GoldSource;
import games.stendhal.client.entity.GrainField;
import games.stendhal.client.entity.InvisibleEntity;
import games.stendhal.client.entity.Item;
import games.stendhal.client.entity.NPC;
import games.stendhal.client.entity.Pet;
import games.stendhal.client.entity.PlantGrower;
import games.stendhal.client.entity.Player;
import games.stendhal.client.entity.Portal;
import games.stendhal.client.entity.Ring;
import games.stendhal.client.entity.Sheep;
import games.stendhal.client.entity.SheepFood;
import games.stendhal.client.entity.Sign;
import games.stendhal.client.entity.StackableItem;
import games.stendhal.client.entity.UseableItem;
import games.stendhal.client.entity.WellSource;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

class Triple<P, S, T> {
	// they are used in equals and hashcode
	@SuppressWarnings("unused")
	private final P prim;
	@SuppressWarnings("unused")
	private final S sec;
	@SuppressWarnings("unused")
	private final T third;

	public Triple(final P prim, final S sec, final T third) {
		this.prim = prim;
		this.sec = sec;
		this.third = third;
	}

	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}

/**
 * Registers the relationship between Type, eclass and java class of entity
 * Objects.
 * <p>
 * eclass represents a subtype of type
 * <p>
 * EntityMap encapsulates the implementation
 * 
 */
public final class EntityMap {
	private static Map<Triple<String, String, String>, Class< ? extends Entity>> entityMap = new HashMap<Triple<String, String, String>, Class< ? extends Entity>>();

	static {
		register();
	}

	/**
	 * Fills EntityMap with initial values.
	 */
	private static void register() {
		register("player", null, null, Player.class);

		register("creature", "boss", null, BossCreature.class);
		register("creature", null, null, Creature.class);

		register("sheep", null, null, Sheep.class);

		/*
		 * Not sure whether to register individual pets from child classes, or
		 * the whole parent class Pet. suggestions welcome.
		 */
		register("pet", "baby_dragon", null, Pet.class);
		register("pet", "cat", null, Pet.class);
		register("pet", null, null, Pet.class);

		register("npc", null, null, NPC.class);

		register("plant_grower", null, null, PlantGrower.class);
		register("growing_entity_spawner", "items/grower/carrot_grower", null,
				CarrotGrower.class);
		register("growing_entity_spawner", null, null, GrainField.class);

		register("gold_source", null, null, GoldSource.class);
		register("fish_source", null, null, FishSource.class);
		register("well_source", null, null, WellSource.class);

		register("area", null, null, InvisibleEntity.class);

		register("food", null, null, SheepFood.class);
		register("chest", null, null, Chest.class);

		register("corpse", null, null, Corpse.class);

		register("blood", null, null, Blood.class);
		register("sign", null, null, Sign.class);
		register("blackboard", null, null, Sign.class);

		register("item", null, null, Item.class);
		register("item", "box", null, Box.class);
		register("item", "ring", null, Ring.class);
		register("item", "drink", null, UseableItem.class);
		register("item", "food", null, UseableItem.class);
		register("item", "herb", null, StackableItem.class);
		register("item", "misc", null, StackableItem.class);
		register("item", "money", null, StackableItem.class);
		register("item", "missile", null, StackableItem.class);
		register("item", "ammunition", null, StackableItem.class);
		register("item", "container", null, StackableItem.class);
		register("item", "misc", "seed", UseableItem.class);

		register("item", "resource", null, StackableItem.class);

		register("item", "scroll", null, UseableItem.class);
		register("item", "jewellery", null, StackableItem.class);

		register("portal", null, null, Portal.class);
		register("door", null, null, Door.class);

		register("fire", null, null, Fire.class);
	}

	/**
	 * @param type
	 *            the type of the entity to be created, such as Item, creature
	 * @param eclass
	 *            the subtype of type such as book, drink, food , ,
	 *            small_animal, huge_animal
	 * @param entityClazz
	 *            the java class of the Entity
	 */
	private static void register(final String type, final String eclass,
			final String subClass, final Class< ? extends Entity> entityClazz) {
		entityMap.put(
				new Triple<String, String, String>(type, eclass, subClass),
				entityClazz);
	}

	/**
	 * @param type
	 *            the type of the entity to be created, such as Item, creature
	 * @param eclass
	 *            the subtype of type such as book, drink, food , ,
	 *            small_animal, huge_animal
	 * @param subClass
	 * 
	 * @return the java class of the Entity belonging to type and eclass
	 */
	public static Class< ? extends Entity> getClass(final String type,
			final String eclass, final String subClass) {
		// System.out.print(type+" : "+eclass +" : "+subClass +"=");
		Class< ? extends Entity> result = entityMap
				.get(new Triple<String, String, String>(type, eclass, subClass));
		if (result == null) {
			result = entityMap.get(new Triple<String, String, String>(type,
					eclass, null));
		}
		if (result == null) {
			result = entityMap.get(new Triple<String, String, String>(type,
					null, null));
		}
		// System.out.println(result.getName());
		return result;
	}
}
