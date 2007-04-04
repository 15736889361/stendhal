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
package games.stendhal.client.entity;

import marauroa.common.game.*;
import games.stendhal.common.Direction;
import games.stendhal.client.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

public class Door extends AnimatedEntity {

	private boolean open;

	private int orientation;

	public Door() {
	    super();
	  
    }

	public Door(RPObject base) throws AttributeNotFoundException {
		super(base);

	}

	@Override
	protected void buildAnimations(RPObject base) {
		SpriteStore store = SpriteStore.get();

		String clazz = base.get("class");
		String direction = null;

		orientation = base.getInt("dir");
		switch (orientation) {
			case 4:
				direction = "w";
				break;
			case 2:
				direction = "e";
				break;
			case 1:
				direction = "n";
				break;
			case 3:
				direction = "s";
				break;
		}

		int width;
		int height;
		if (direction.equals("n") || direction.equals("s")) {
			width = 3;
			height = 2;
		} else {
			width = 2;
			height = 3;
		}
		sprites.put("open", store.getAnimatedSprite("data/sprites/doors/" + clazz + "_" + direction + ".png", 0, 1,
		        width, height));
		sprites.put("close", store.getAnimatedSprite("data/sprites/doors/" + clazz + "_" + direction + ".png", 1, 1,
		        width, height));
	}

	@Override
	protected Sprite defaultAnimation() {
		animation = "close";
		return sprites.get("close")[0];
	}

	// When rpentity moves, it will be called with the data.
	@Override
	public void onMove(int x, int y, Direction direction, double speed) {
		if ((orientation == 1) || (orientation == 3)) {
			this.x = x - 1;
			this.y = y;
		} else {
			this.x = x;
			this.y = y - 1;
		}
	}

	@Override
	public void onChangedAdded(RPObject base, RPObject diff) throws AttributeNotFoundException {
		super.onChangedAdded(base, diff);

		if (diff.has("open")) {
			open = true;
			animation = "open";
		}
	}

	@Override
	public void onChangedRemoved(RPObject base, RPObject diff) throws AttributeNotFoundException {
		super.onChangedRemoved(base, diff);

		if (diff.has("open")) {
			open = false;
			animation = "close";
		}
	}

	@Override
	public Rectangle2D getArea() {
		return new Rectangle.Double(x, y, 1, 1);
	}

	@Override
	public Rectangle2D getDrawedArea() {
		return new Rectangle.Double(x, y, 1, 1);
	}

	@Override
	public ActionType defaultAction() {
		if (open) {
			return ActionType.CLOSE;
		} else {
			return ActionType.OPEN;

		}
	}

	@Override
	public void onAction(ActionType at, String... params) {
		// ActionType at =handleAction(action);
		switch (at) {
			case OPEN:
			case CLOSE:
				RPAction rpaction = new RPAction();
				rpaction.put("type", at.toString());
				int id = getID().getObjectID();
				rpaction.put("target", id);
				at.send(rpaction);
				break;

			default:
				super.onAction(at, params);
				break;
		}

	}

	@Override
	public int getZIndex() {
		return 5000;
	}

	@Override
	protected void buildOfferedActions(List<String> list) {

		super.buildOfferedActions(list);
		if (open) {
			list.add(ActionType.CLOSE.getRepresentation());
		} else {
			list.add(ActionType.OPEN.getRepresentation());

		}
	}

}
