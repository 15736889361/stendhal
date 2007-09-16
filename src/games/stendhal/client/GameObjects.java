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
package games.stendhal.client;

import games.stendhal.client.entity.Entity;
import games.stendhal.client.entity.EntityFactory;
import games.stendhal.client.events.RPObjectChangeListener;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/** This class stores the objects that exists on the World right now */
public class GameObjects implements RPObjectChangeListener, Iterable<Entity> {

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(GameObjects.class);

	private Map<FQID, Entity> objects;

	private StaticGameLayers collisionMap;

	/**
	 * holds the reference to the singeton instance
	 */
	private static GameObjects instance;

	/**
	 * @param collisionMap
	 *            =layers that make floor and building
	 * @return singleton instance of Gameobjects
	 */
	public static GameObjects createInstance(StaticGameLayers collisionMap) {
		if (instance == null) {
			instance = new GameObjects(collisionMap);
		}
		return instance;
	}

	/**
	 * @return existing instance of Gameobjects
	 * @throws IllegalStateException
	 *             if instance has not been instanciated
	 */
	public static GameObjects getInstance() {
		if (instance == null) {
			throw new IllegalStateException();
		}

		return instance;
	}

	/**
	 * constructor
	 * 
	 * @param collisionMap
	 *            =layers that make floor and building
	 */
	private GameObjects(StaticGameLayers collisionMap) {
		objects = new HashMap<FQID, Entity>();

		this.collisionMap = collisionMap;
	}

	public Iterator<Entity> iterator() {
		return objects.values().iterator();
	}

	public Entity get(RPObject object) {
		return objects.get(FQID.create(object));
	}

	public Entity get(RPObject.ID id) {
		return objects.get(new FQID(id));
	}

	/** Removes all the object entities */
	public void clear() {
		// SHOULD NEVER HAPPEN!!!
		// TODO: Remove after 0.70 release, if this warning never shows
		if(!objects.isEmpty()) {
			logger.error("Game objects not empty!");

			// invalidate all entity objects
			Iterator<Entity> it = iterator();

			while (it.hasNext()) {
				Entity entity = it.next();
				entity.release();
			}

			objects.clear();
		}
	}

	public boolean collides(Entity entity) {
		Rectangle2D area = entity.getArea();

		// TODO: Ugly, use similar method that server uses
		if (collisionMap.collides(area)) {
			return true;
		}

		for (Entity other : objects.values()) {
			if (other.isObstacle(entity) && area.intersects(other.getArea())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Update objects based on the lapsus of time ellapsed since the last call.
	 * 
	 * @param delta
	 *            The time since last update (in ms).
	 */
	public void update(int delta) {
		for (Entity entity : objects.values()) {
			entity.update(delta);
		}
	}

	/**
	 * Create an add an Entity. This does not add to the screen list.
	 * 
	 * @param object
	 *            The object.
	 * 
	 * @return An entity.
	 */
	protected Entity add(final RPObject object) {
		Entity entity = EntityFactory.createEntity(object);

		if (entity != null) {
			objects.put(FQID.create(object), entity);
		}

		return entity;
	}

	//
	// RPObjectChangeListener
	//

	/**
	 * An object was added.
	 * 
	 * @param object
	 *            The object.
	 */
	public void onAdded(final RPObject object) {
		if (!object.has("server-only")) {
			if (!object.getRPClass().subclassOf("entity")) {
				logger.debug("Skipping non-entity object: " + object);
				return;
			}

			// TODO: Remove once 'type' isn't used anymore
			if (!object.has("type")) {
				logger.error("Entity without type: " + object);
				return;
			}

			Entity entity = add(object);

			if (entity != null) {
				if (entity.isOnGround()) {
					GameScreen.get().addEntity(entity);
				}

				logger.debug("added " + entity);
			} else {
				logger.error("No entity for: " + object);
			}
		} else {
			logger.debug("Discarding object: " + object);
		}
	}

	/**
	 * The object added/changed attribute(s).
	 * 
	 * @param object
	 *            The base object.
	 * @param changes
	 *            The changes.
	 */
	public void onChangedAdded(final RPObject object, final RPObject changes) {
		Entity entity = objects.get(FQID.create(object));

		if (entity != null) {
			entity.onChangedAdded(object, changes);
		}
	}

	/**
	 * An object removed attribute(s).
	 * 
	 * @param object
	 *            The base object.
	 * @param changes
	 *            The changes.
	 */
	public void onChangedRemoved(final RPObject object, final RPObject changes) {
		Entity entity = objects.get(FQID.create(object));

		if (entity != null) {
			entity.onChangedRemoved(object, changes);
		}
	}

	/**
	 * An object was removed.
	 * 
	 * @param object
	 *            The object.
	 */
	public void onRemoved(final RPObject object) {
		RPObject.ID id = object.getID();

		logger.debug("removed " + id);

		Entity entity = objects.remove(FQID.create(object));

		if (entity != null) {
			GameScreen.get().removeEntity(entity);
			entity.release();
		}
	}

	/**
	 * A slot object was added.
	 *
	 * @param	object		The container object.
	 * @param	slotName	The slot name.
	 * @param	sobject		The slot object.
	 */
	public void onSlotAdded(final RPObject object, final String slotName, final RPObject sobject) {
	}

	/**
	 * A slot object added/changed attribute(s).
	 *
	 * @param	object		The base container object.
	 * @param	slotName	The container's slot name.
	 * @param	sobject		The slot object.
	 * @param	schanges	The slot object changes.
	 */
	public void onSlotChangedAdded(final RPObject object, final String slotName, final RPObject sobject, final RPObject schanges) {
		Entity entity = objects.get(FQID.create(object));

		if (entity != null) {
			entity.onSlotChangedAdded(object, slotName, sobject, schanges);
		}
	}

	/**
	 * A slot object removed attribute(s).
	 *
	 * @param	object		The base container object.
	 * @param	slotName	The container's slot name.
	 * @param	sobject		The slot object.
	 * @param	schanges	The slot object changes.
	 */
	public void onSlotChangedRemoved(final RPObject object, final String slotName, final RPObject sobject, final RPObject schanges) {
		Entity entity = objects.get(FQID.create(object));

		if (entity != null) {
			entity.onSlotChangedRemoved(object, slotName, sobject, schanges);
		}
	}

	/**
	 * A slot object was removed.
	 *
	 * @param	object		The container object.
	 * @param	slotName	The slot name.
	 * @param	sobject		The slot object.
	 */
	public void onSlotRemoved(final RPObject object, final String slotName, final RPObject sobject) {
	}

	//
	//

	/**
	 * A fully qualified ID. This will make an nested ID unique, even when in a
	 * slot tree.
	 */
	protected static class FQID {
		/**
		 * The object identification path.
		 */
		protected Object [] path;

		/**
		 * Create a fully qualified ID.
		 * 
		 * @param id
		 *		And object ID.
		 */
		public FQID(RPObject.ID id) {
			this(new Object [] { new Integer(id.getObjectID()) });
		}

		/**
		 * Create a fully qualified ID.
		 * 
		 * @param path
		 *            An identification path.
		 */
		public FQID(Object [] path) {
			this.path = path;
		}

		//
		// FQID
		//

		/**
		 * Create a FQID from an object tree.
		 * 
		 * @param object
		 *            An object.
		 * 
		 * @return A FQID.
		 */
		public static FQID create(final RPObject object) {
			LinkedList<Object> path = new LinkedList<Object>();
			RPObject node = object;

			while (true) {
				path.addFirst(new Integer(node.getID().getObjectID()));

				RPSlot slot = node.getContainerSlot();

				if(slot == null) {
					break;
				}

				path.addFirst(slot.getName());
				node = node.getContainer();
			}

			return new FQID(path.toArray());
		}

		/**
		 * Get the tree path of object identifiers.
		 * 
		 * @return The identifier path.
		 */
		public Object [] getPath() {
			return path;
		}

		//
		// Object
		//

		/**
		 * Check if this equals another object.
		 * 
		 * @param obj
		 *            The object to compare to.
		 */
		@Override
		public boolean equals(final Object obj) {
			if (!(obj instanceof FQID)) {
				return false;
			}

			return Arrays.equals(getPath(), ((FQID) obj).getPath());
		}

		/**
		 * Get the hash code.
		 * 
		 * @return The hash code.
		 */
		@Override
		public int hashCode() {
			int value = 0;

			for (Object obj : getPath()) {
				value ^= obj.hashCode();
			}

			return value;
		}


		/**
		 * Get the string representation.
		 *
		 * @return	The string representation.
		 */
		public String toString() {
			StringBuffer sbuf = new StringBuffer();

			sbuf.append('[');
			sbuf.append(path[0]);

			for(int i = 1; i < path.length; i++) {
				sbuf.append(':');
				sbuf.append(path[i]);
			}

			sbuf.append(']');

			return sbuf.toString();
		}
	}
}
