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

import java.net.URL;
import java.awt.image.BufferedImage;
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
	 * Render the data to screen.
	 */

	@Override
	public void draw(GameScreen screen) {
		if (mySprite != null) {
			screen.draw(mySprite, 0, 0);
		}
	}

}
