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
package games.stendhal.server.entity.mapstuff.chest;

import games.stendhal.common.Grammar;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.PassiveEntity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.slot.ChestSlot;

import java.util.Iterator;

import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.common.game.Definition.Type;

/**
 * A chest is an unmovable container. It can be opened and closed. While it is
 * open, every player can put items in and take them out later. A player can
 * take out items that another player put in.
 */
public class Chest extends Entity implements UseListener {
	/**
	 * Whether the chest is open.
	 */
	private boolean open;

	public static void generateRPClass() {
		RPClass chest = new RPClass("chest");
		chest.isA("entity");
		chest.addAttribute("open", Type.FLAG);
		chest.addRPSlot("content", 30);
	}

	/**
	 * Creates a new chest.
	 * 
	 * @param object
	 *            RPObject
	 */
	public Chest(RPObject object) {
		super(object);
		setRPClass("chest");
		put("type", "chest");

		if (!hasSlot("content")) {
			RPSlot slot = new ChestSlot(this);
			addSlot(slot);
		}

		update();
	}

	/**
	 * Creates a new chest.
	 */
	public Chest() {
		setRPClass("chest");
		put("type", "chest");
		open = false;

		RPSlot slot = new ChestSlot(this);
		addSlot(slot);
	}
	
	
	//
	// Chest
	//

	@Override
    public String getDescriptionName(boolean definite) {
	    return Grammar.article_noun("chest", definite);
    }

	@Override
	public void update() {
		super.update();
		open = false;
		if (has("open")) {
			open = true;
		}
	}

	/**
	 * Open the chest.
	 */
	public void open() {
		this.open = true;
		put("open", "");
	}

	/**
	 * Close the chest.
	 */
	public void close() {
		this.open = false;

		if (has("open")) {
			remove("open");
		}
	}

	/**
	 * Determine if the chest is open.
	 * 
	 * @return <code>true</code> if the chest is open.
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Adds a passive entity (like an item) to the chest.
	 * 
	 * @param entity
	 *            entity to add
	 */
	public void add(PassiveEntity entity) {
		RPSlot content = getSlot("content");
		content.add(entity);
	}

	@Override
	public int size() {
		return getSlot("content").size();
	}

	/**
	 * Returns the content.
	 * 
	 * @return iterator for the content
	 */
	public Iterator<RPObject> getContent() {
		RPSlot content = getSlot("content");
		return content.iterator();
	}

	//
	// UseListener
	//

	public boolean onUsed(RPEntity user) {
		if (user.nextTo(this)) {
			if (isOpen()) {
				close();
			} else {
				open();
			}

			notifyWorldAboutChanges();
			return true;
		}

		return false;
	}

	//
	// Entity
	//

	@Override
	public String describe() {
		String text = "You see a chest.";

		if (hasDescription()) {
			text = getDescription();
		}

		if (isOpen()) {
			text += " It is open.";
			text += " You can #inspect this item to see its contents.";
		} else {
			text += " It is closed.";
		}

		return (text);
	}
}
