/*
 * EntityManager.java
 *
 * Created on 19. August 2005, 22:16
 *
 */

package games.stendhal.server.rule;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.rule.defaultruleset.DefaultCreature;

import java.util.Collection;

/**
 * Ruleset Interface for resolving Entities in Stendhal.
 *
 * @author Matthias Totz
 */
public interface EntityManager {
	/**
	 * returns the entity or <code>null</code> if the class is unknown
	 *
	 * @param clazz
	 *            the creature class, must not be <code>null</code>
	 * @return the entity or <code>null</code>
	 *
	 */
	Entity getEntity(String clazz);

	/**
	 * return true if the Entity is a creature
	 *
	 * @param id
	 *            the tile id
	 * @return true if it is a creature, false otherwise
	 */
	boolean isCreature(String tileset, int id);

	/**
	 * return true if the Entity is a creature
	 *
	 * @param clazz
	 *            the creature class, must not be <code>null</code>
	 * @return true if it is a creature, false otherwise
	 *
	 */
	boolean isCreature(String clazz);

	/**
	 * return true if the Entity is a Item
	 *
	 * @param clazz
	 *            the Item class, must not be <code>null</code>
	 * @return true if it is a Item, false otherwise
	 *
	 */
	boolean isItem(String clazz);

	/**
	 * returns a list of all Creatures that are used at least once
	 */
	Collection<Creature> getCreatures();

	/**
	 * returns the creature or <code>null</code> if the id is unknown.
	 *
	 * @param id
	 *            the tile id
	 * @return the creature or <code>null</code>
	 */
	Creature getCreature(String tileset, int id);

	/**
	 * returns the creature or <code>null</code> if the clazz is unknown
	 *
	 * @param clazz
	 *            the creature class, must not be <code>null</code>
	 * @return the creature or <code>null</code>
	 *
	 */
	Creature getCreature(String clazz);

	/**
	 * returns the DefaultCreature or <code>null</code> if the clazz is unknown
	 *
	 * @param clazz
	 *            the creature class
	 * @return the creature or <code>null</code>
	 * @throws NullPointerException
	 *             if clazz is <code>null</code>
	 */
	DefaultCreature getDefaultCreature(String clazz);

	/**
	 * returns a list of all Items that are being used at least once
	 */
	Collection<Item> getItems();

	/**
	 * returns the item or <code>null</code> if the clazz is unknown
	 *
	 * @param clazz
	 *            the item class, must not be <code>null</code>
	 * @return the item or <code>null</code>
	 *
	 */
	Item getItem(String clazz);
}
