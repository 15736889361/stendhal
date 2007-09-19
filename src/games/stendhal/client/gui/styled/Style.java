/*
 * @(#) src/games/stendhal/client/gui/styled/Style.java
 *
 * $Id$
 */

package games.stendhal.client.gui.styled;

//
//

import games.stendhal.client.sprite.Sprite;

import java.awt.Color;
import java.awt.Font;

import javax.swing.border.Border;

/**
 * Style information.
 */
public interface Style {

	/**
	 * Get the background texture.
	 *
	 * @return	A texture sprite.
	 */
	Sprite getBackground();

	/**
	 * Get component border.
	 *
	 * @return	A border, or <code>null</code> to use default.
	 */
	Border getBorder();

	/**
	 * Get the normal font.
	 *
	 * @return	A font.
	 */
	Font getFont();

	/**
	 * Get the foreground color appropriete for the background texture.
	 *
	 * @return	A color.
	 */
	Color getForeground();
}
