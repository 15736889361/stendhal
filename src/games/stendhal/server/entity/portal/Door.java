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
package games.stendhal.server.entity.portal;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.TurnListener;
import games.stendhal.server.events.TurnNotifier;

import marauroa.common.game.Definition;
import marauroa.common.game.RPClass;
import marauroa.common.game.Definition.Type;

/**
 * A door is a special kind of portal which can be open or closed.
 *
 * Note that you can link a door with a portal; that way, people only
 * require the key when walking in one direction and can walk in the
 * other direction without any key.
 */
public abstract class Door extends Portal implements TurnListener {

	/**
	 * How many turns it takes until door automatically closes itself
	 * after somebody walked through it.
	 */
	private static final int TURNS_TO_STAY_OPEN = 9; /* 3 seconds */

	/**
	 * Whether or not the door is currently open
	 */
	private boolean open;

	public static void generateRPClass() {
		RPClass door = new RPClass("door");
		door.isA("entity");
		door.addAttribute("class", Type.STRING);
		door.addAttribute("locked", Type.STRING, Definition.HIDDEN);
		door.addAttribute("open", Type.FLAG);
	}

	/**
	 * Creates a new door.
	 *
	 * @param clazz The class. Responsible for how this door looks like.
	 */
	public Door(String clazz) {
		setRPClass("door");
		put("type", "door");
		put("class", clazz);

		open = false;
	}


	@Override
	public void update() {
		super.update();
		open = has("open");
	}

	/**
	 * Opens the door.
	 */
	public void open() {
		open = true;
		put("open", "");
		notifyWorldAboutChanges();
	}

	/**
	 * Closes the door.
	 */
	protected void close() {
		this.open = false;
		if (has("open")) {
			remove("open");
		}
		notifyWorldAboutChanges();
	}

	/**
	 * Is the door open?
	 *
	 * @return true, if opened; false otherwise
	 */
	protected boolean isOpen() {
		return open;
	}

	/**
	 * May this door be used?
	 *
	 * @param user user of the door
	 * @return true, if it can be used; and false otherwise
	 */
	protected abstract boolean mayBeOpened(Player user);

	/**
	 * teleport (if the door is now open)
	 */
	@Override
	public boolean onUsed(RPEntity user) {
		if (mayBeOpened((Player) user)) {
			TurnNotifier turnNotifier = TurnNotifier.get();
			if (isOpen()) {
				// The door is still open because another player just used it.
				// Thus, it is scheduled to auto-close soon. We delay this
				// auto-closing.
				turnNotifier.dontNotify(this);
			} else {
				open();
			}

			// register automatic close
			turnNotifier.notifyInTurns(TURNS_TO_STAY_OPEN, this);

			// use it
			return super.onUsed(user);

		} else { // player may not use it
			if (isOpen()) {
				// close now to make visible that the entity is not allowed
				// to pass
				close();
				return false;
			}
		}
		return false;
	}

	@Override
	public void onUsedBackwards(RPEntity user) {
		open();
		notifyWorldAboutChanges();
	}

	@Override
	public String describe() {
		String text = "You see a door.";
		if (hasDescription()) {
			text = getDescription();
		}
		text += " It is " + (isOpen() ? "open" : "closed") + ".";
		return (text);
	}

	public void onTurnReached(int currentTurn, String message) {
		close();
		notifyWorldAboutChanges();
	}
}
