/*
 * @(#) src/games/stendhal/client/events/RPObjectChangeListener.java
 *
 * $Id$
 */

package games.stendhal.client.events;

//
//

import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * A listener of RPObject changes.
 */
public interface RPObjectChangeListener {
	/**
	 * An object was added.
	 *
	 * @param	object		The object.
	 */
	public void onAdded(RPObject object);

	/**
	 * The object added/changed attribute(s).
	 *
	 * @param	object		The base object.
	 * @param	changes		The changes.
	 */
	public void onChangedAdded(RPObject object, RPObject changes);

	/**
	 * The object removed attribute(s).
	 *
	 * @param	object		The base object.
	 * @param	changes		The changes.
	 */
	public void onChangedRemoved(RPObject object, RPObject changes);

	/**
	 * An object was removed.
	 *
	 * @param	object		The object.
	 */
	public void onRemoved(RPObject object);

	/**
	 * A slot object was added.
	 *
	 * @param	object		The container object.
	 * @param	slotName	The slot name.
	 * @param	sobject		The slot object.
	 */
	public void onSlotAdded(RPObject object, String slotName, RPObject sobject);

	/**
	 * A slot object added/changed attribute(s).
	 *
	 * @param	object		The base container object.
	 * @param	slotName	The container's slot name.
	 * @param	sobject		The slot object.
	 * @param	schanges	The slot object changes.
	 */
	public void onSlotChangedAdded(RPObject object, String slotName, RPObject sobject, RPObject schanges);

	/**
	 * A slot object removed attribute(s).
	 *
	 * @param	object		The base container object.
	 * @param	slotName	The container's slot name.
	 * @param	sobject		The slot object.
	 * @param	schanges	The slot object changes.
	 */
	public void onSlotChangedRemoved(RPObject object, String slotName, RPObject sobject, RPObject schanges);

	/**
	 * A slot object was removed.
	 *
	 * @param	object		The container object.
	 * @param	slotName	The slot name.
	 * @param	sobject		The slot object.
	 */
	public void onSlotRemoved(RPObject object, String slotName, RPObject sobject);
}
