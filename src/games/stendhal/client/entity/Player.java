/* $Id$ */
/***************************************************************************
 *		      (C) Copyright 2003 - Marauroa		      *
 ***************************************************************************
 ***************************************************************************
 *									 *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.				   *
 *									 *
 ***************************************************************************/
package games.stendhal.client.entity;

import games.stendhal.client.NotificationType;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import marauroa.common.game.RPObject;

/** A Player entity */
public class Player extends RPEntity {
	/**
	 * Away property.
	 */
	public final static Object	PROP_AWAY	= new Object();

	/**
	 * The away message of this player.
	 */
	private String	away;


	/**
	 * Create a player entity.
	 */
	public Player() {
		away = null;
	}


	//
	// Player
	//

	/**
	 * Determine if the player is away.
	 *
	 * @return	<code>true</code> if the player is away.
	 */
	public boolean isAway() {
		return (getAway() != null);
	}


	/**
	 * Get the away message.
	 *
	 * @return	The away text, or <code>null</code> if not away.
	 */
	public String getAway() {
		return away;
	}


	/**
	 * An away message was set/cleared.
	 *
	 * @param	message		The away message, or <code>null</code>
	 *				if no-longer away.
	 */
	protected void onAway(final String message) {
		addTextIndicator(((message != null) ? "Away" : "Back"), NotificationType.INFORMATION);
	}


	//
	// Entity
	//

	/**
	 * Get the area the entity occupies.
	 *
	 * @return	A rectange (in world coordinate units).
	 */
	@Override
	public Rectangle2D getArea() {
		return new Rectangle.Double(getX(), getY() + 1.0, getWidth(), getHeight());
	}


	//
	// RPObjectChangeListener
	//

	/**
	 * The object added/changed attribute(s).
	 *
	 * @param	object		The base object.
	 * @param	changes		The changes.
	 */
	@Override
	public void onChangedAdded(final RPObject object, final RPObject changes) {
		super.onChangedAdded(object, changes);

		if (changes.has("away")) {
			/*
			 * Filter out a player "changing" to the same message
			 */
			if (!object.has("away") || !object.get("away").equals(changes.get("away"))) {
				away = changes.get("away");
				fireChange(PROP_AWAY);
				onAway(away);
			}
		}
	}

	/**
	 * The object removed attribute(s).
	 *
	 * @param	object		The base object.
	 * @param	changes		The changes.
	 */
	@Override
	public void onChangedRemoved(final RPObject object, final RPObject changes) {
		super.onChangedRemoved(object, changes);

		if (changes.has("away")) {
			away = null;
			fireChange(PROP_AWAY);
			onAway(null);
		}
	}
}
