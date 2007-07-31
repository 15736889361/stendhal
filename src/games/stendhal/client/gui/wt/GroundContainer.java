/* $Id$ */
/***************************************************************************
 *                      (C) Copyright 2005 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

package games.stendhal.client.gui.wt;

import games.stendhal.client.GameScreen;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.StendhalUI;
import games.stendhal.client.entity.ActionType;
import games.stendhal.client.entity.Chest;
import games.stendhal.client.entity.Entity;
import games.stendhal.client.entity.EntityView;
import games.stendhal.client.entity.Entity2DView;
import games.stendhal.client.entity.Inspector;
import games.stendhal.client.entity.Text;
import games.stendhal.client.gui.wt.core.WtDraggable;
import games.stendhal.client.gui.wt.core.WtDropTarget;
import games.stendhal.client.gui.wt.core.WtPanel;

import java.awt.Point;
import java.awt.geom.Point2D;

import marauroa.common.game.RPAction;
import marauroa.common.game.RPSlot;

/**
 * 
 * This container is the ground
 * 
 * @author mtotz
 * 
 */

public class GroundContainer extends WtPanel implements WtDropTarget, Inspector {
	/** the game client */
	private StendhalClient client;

	/**
	 * The UI.
	 */
	private StendhalUI ui;

	/** the game screen */
	private GameScreen screen;


	/** creates a new groundcontainer */
	public GroundContainer(StendhalUI ui) {
		super("ground", 0, 0, ui.getWidth(), ui.getHeight());

		this.ui = ui;

		setMovable(false);
		setCloseable(false);
		setFrame(false);
		setTitleBar(false);

		client = ui.getClient();
		screen = ui.getScreen();
	}


	/** drags an item from the ground */
	@Override
	protected WtDraggable getDragged(int x, int y) {
		WtDraggable other = super.getDragged(x, y);

		if (other != null) {
			return other;
		}

		Point2D point = screen.translate(new Point2D.Double(x, y));
		EntityView view = screen.getMovableEntityViewAt(point.getX(), point.getY());

		// only Items can be dragged
		if (view != null) {
			return new MoveableEntityContainer(view.getEntity());
		}

		return null;
	}

	/**
	 * 
	 * 
	 * 
	 */
	@Override
	public synchronized boolean onMouseClick(Point p) {
		// base class checks if the click is within a child
		if (super.onMouseClick(p)) {
			// yes, click already processed
			return true;
		}

		// get clicked entity
		Point2D point = screen.translate(p);

		// for the text pop up....
		Text text = screen.getTextAt(point.getX(), point.getY());
		if (text != null) {
			screen.removeText(text);
			return true;
		}

		// for the clicked entity....
		EntityView view = screen.getEntityViewAt(point.getX(), point.getY());
		if (view != null) {
			if (ui.isCtrlDown()) {
				view.onAction();
				return true;
			} else if (ui.isShiftDown()) {
				view.onAction(ActionType.LOOK);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public synchronized boolean onMouseDoubleClick(Point p) {
		// base class checks if the click is within a child
		if (super.onMouseDoubleClick(p)) {
			// yes, click already processed
			return true;
		}
		// doubleclick is outside of all windows
		Point2D point = screen.translate(p);

		// for the text pop up....
		Text text = screen.getTextAt(point.getX(), point.getY());
		if (text != null) {
			screen.removeText(text);
			return true;
		}

		EntityView view = screen.getEntityViewAt(point.getX(), point.getY());

		if (view != null) {
			// ... do the default action
			view.onAction();
			return true;
		} else {
			// moveto action
			RPAction action = new RPAction();
			action.put("type", "moveto");
			action.put("x", (int) point.getX());
			action.put("y", (int) point.getY());
			client.send(action);
			// TODO: let action do this
			return true;
		}
	}

	/** process right click */
	@Override
	public synchronized boolean onMouseRightClick(Point p) {
		// base class checks if the click is within a child
		if (super.onMouseRightClick(p)) {
			// yes, click already processed
			return true;
		}
		// doubleclick is outside of all windows
		Point2D point = screen.translate(p);

		Entity2DView view = screen.getEntityViewAt(point.getX(), point.getY());

		if (view != null) {
			// ... show context menu (aka command list)
			String[] actions = view.getActions();

			if (actions.length > 0) {
				Entity entity = view.getEntity();

				CommandList list = new CommandList(entity.getType(), actions, view);
				ui.setContextMenu(list);
			}
			return true;
		}
		
		return false;
	}


	//
	// WtDropTarget
	//

	/** called when an object is dropped. */
	public boolean onDrop(final int x, final int y, WtDraggable droppedObject) {
		// Not an entity?
		if (!(droppedObject instanceof MoveableEntityContainer)) {
			return false;
		}

		MoveableEntityContainer container = (MoveableEntityContainer) droppedObject;

		RPAction action = new RPAction();

		if(container.isContained()) {
			// looks like an drop
			action.put("type", "drop");
		} else {
			// it is a displace
			action.put("type", "displace");
		}

		// HACK: if ctrl is pressed, attempt to split stackables
		if (ui.isCtrlDown()) {
			action.put("quantity", 1);
		}

		// fill 'moved from' parameters
		container.fillRPAction(action);

		// 'move to'
		Point2D point = screen.translate(new Point2D.Double(x, y));
		action.put("x", (int) point.getX());
		action.put("y", (int) point.getY());

		client.send(action);
		return true;
	}


	//
	// Inspector
	//

	public EntityContainer inspectMe(Entity suspect, RPSlot content, EntityContainer container) {
		if ((container == null) || !container.isVisible()) {
			if (suspect instanceof Chest) {
				container = new EntityContainer(client, suspect.getType(), 5, 6);
			} else {
				container = new EntityContainer(client, suspect.getType(), 2, 2);
			}

			addChild(container);

			container.setSlot(suspect, content.getName());
			container.setVisible(true);
		}

		return container;
	}
}
