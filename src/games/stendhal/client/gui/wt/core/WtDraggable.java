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
/*
 * Draggable.java
 * Created on 17. Oktober 2005, 17:50
 */

package games.stendhal.client.gui.wt.core;

import games.stendhal.client.IGameScreen;

import java.awt.Graphics;
import java.awt.Point;

/**
 * Every Object that is draggable must implement this interface.
 * 
 * @author mtotz
 */
public interface WtDraggable {

	/**
	 * this object has been started to drag around.
	 * @param gameScreen 
	 * 			 The gameScreen to paint on.
	 * @return true when this item can be dragged, false otherwise
	 */
	boolean dragStarted(IGameScreen gameScreen);

	/**
	 * This object has been started to drag around.
	 * 
	 * @param p
	 *            the point where the mouse cursor is at the moment (relative to
	 *            the drag startpoint)
	 * @param gameScreen 
	 * 			 The gameScreen to paint on.
	 * @return true when this item can be dropped on point p, false otherwise
	 */
	boolean dragFinished(Point p, IGameScreen gameScreen);

	/**
	 * This object is dragged around the screen. When this method returns false
	 * the ui should move it back to it's start point to show the user that it
	 * is not allowed to drop the object here.
	 * 
	 * @param p
	 *            the point where the mouse cursor is at the moment (relative to
	 *            the drag start point)
	 * @return true when this item can be moved to this point, false otherwise
	 */
	boolean dragMoved(Point p);

	/**
	 * Draws the dragged item.
	 * 
	 * @param g
	 */
	void drawDragged(Graphics g);
}
