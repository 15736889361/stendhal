/*
 * @(#) src/games/stendhal/client/gui/ManagedWindow.java
 *
 * $Id$
 */

package games.stendhal.client.gui;

//
//

import games.stendhal.client.gui.wt.core.WtCloseListener;

/**
 * A managed window.
 */
public interface ManagedWindow {

	/**
	 * Get the managed window name.
	 *
	 *
	 */
	String getName();

	/**
	 * Get X coordinate of the window.
	 *
	 * @return	A value sutable for passing to <code>moveTo()</code>.
	 */
	int getX();

	/**
	 * Get Y coordinate of the window.
	 *
	 * @return	A value sutable for passing to <code>moveTo()</code>.
	 */
	int getY();

	/**
	 * Determine if the window is minimized.
	 *
	 * @return	<code>true</code> if the window is minimized.
	 */
	boolean isMinimized();

	/**
	 * Determine if the window is visible.
	 *
	 * @return	<code>true</code> if the window is visible.
	 */
	boolean isVisible();

	/**
	 * Move to a location. This may be subject to internal representation,
	 * and should only use what was passed from <code>getX()</code> and
	 * <code>getY()</code>.
	 *
	 * @param	x		The X coordinate;
	 * @param	y		The Y coordinate;
	 *
	 * @return	<code>true</code> if the move was allowed.
	 */
	boolean moveTo(int x, int y);

	/**
	 * Register a close listener.
	 *
	 * @param	listener	A close listener.
	 */
	void registerCloseListener(WtCloseListener listener);

	/**
	 * Unregister a close listener.
	 *
	 * @param	listener	A close listener.
	 */
	void removeCloseListener(WtCloseListener listener);

	/**
	 * Set the window as minimized.
	 *
	 * @param	minimized	Whether the window should be minimized.
	 */
	void setMinimized(boolean minimized);

	/**
	 * Set the window as visible (or hidden).
	 *
	 * @param	visible		Whether the window should be visible.
	 */
	void setVisible(boolean visible);
}
