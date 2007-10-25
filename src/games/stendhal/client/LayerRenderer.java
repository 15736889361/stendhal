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

//
//

import games.stendhal.client.sprite.Tileset;

/**
 * This is a helper base class to render a layer
 */
public abstract class LayerRenderer {

	protected int width;

	protected int height;

	public LayerRenderer() {
		width = 0;
		height = 0;
	}

	/** Returns the widht in world units */
	public int getWidth() {
		return width;
	}

	/** Returns the height in world units */
	public int getHeight() {
		return height;
	}

	/**
	 * Render the layer to screen. We assume that game screen will clip.
	 * 
	 * @param screen
	 *            The screen to draw on.
	 */
	public void draw(IGameScreen screen) {
		int x = (int) screen.getViewX();
		int y = (int) screen.getViewY();
		int w = (int) screen.getViewWidth();
		int h = (int) screen.getViewHeight();

		draw(screen, x, y, w, h);
	}

	/**
	 * Render the layer to screen. We assume that game screen will clip.
	 * 
	 * @param screen
	 *            The screen to draw on.
	 * @param x
	 *            The view X world coordinate.
	 * @param y
	 *            The view Y world coordinate.
	 * @param w
	 *            The view world width.
	 * @param h
	 *            The view world height.
	 */
	public abstract void draw(IGameScreen screen, int x, int y, int w, int h);

	// TODO: Cleanly remove from this level
	public abstract void setTileset(Tileset tileset);
}
