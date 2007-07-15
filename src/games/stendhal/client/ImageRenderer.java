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

import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.Tileset;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * This class renders a layer based on a complete image
 */
public class ImageRenderer extends LayerRenderer {

	Sprite mySprite = null;

	public ImageRenderer(URL url) {
		try {
			BufferedImage myImage = ImageIO.read(url);
			width = myImage.getWidth();
			height = myImage.getHeight();
			mySprite = new ImageSprite(myImage);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	/**
	 * Render the layer to screen. We assume that game screen will clip.
	 *
	 * @param	screen		The screen to draw on.
	 */
	@Override
	public void draw(GameScreen screen) {
		if (mySprite != null) {
			screen.draw(mySprite, 0, 0);
		}
	}

	/**
	 * Render the layer to screen. We assume that game screen will clip.
	 *
	 * @param	screen		The screen to draw on.
	 * @param	x		The view X world coordinate.
	 * @param	y		The view Y world coordinate.
	 * @param	w		The view world width.
	 * @param	h		The view world height.
	 */
	@Override
	public void draw(GameScreen screen, int x, int y, int w, int h) {
		draw(screen);
	}


	@Override
	public void setTileset(Tileset tilset) {
	}
}
