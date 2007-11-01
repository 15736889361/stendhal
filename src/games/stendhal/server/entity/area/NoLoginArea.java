/*
 * @(#) src/games/stendhal/server/entity/area/LifeDrainArea.java
 *
 *$Id$
 */

package games.stendhal.server.entity.area;

import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.LoginListener;
import games.stendhal.server.events.LoginNotifier;

import org.apache.log4j.Logger;

/**
 * An area prevents login to an area by moving the player somewhere else.
 */
public class NoLoginArea extends AreaEntity implements LoginListener {
	/**
	 * The logger instance.
	 */
	private static final Logger logger = Logger.getLogger(NoLoginArea.class);

	/**
	 * The new player X coordinate.
	 */
	private int newX;

	/**
	 * The new player Y coordinate.
	 */
	private int newY;

	/**
	 * The message to send to the user when repositioned.
	 */
	private String message;

	/**
	 * Create a nologin area.
	 *
	 * @param width
	 *            Width of this area
	 * @param height
	 *            Height of this area
	 * @param newX
	 *            x position to place the player at
	 * @param newY
	 *            y position to place the player at
	 */
	public NoLoginArea(int width, int height, int newX, int newY) {
		this(width, height, newX, newY, null);
	}

	/**
	 * Create a nologin area.
	 *
	 * @param width
	 *            Width of this area
	 * @param height
	 *            Height of this area
	 * @param newX
	 *            x position to place the player at
	 * @param newY
	 *            y position to place the player at
	 * @param message
	 *            The message to send to the user when repositioned.
	 */
	public NoLoginArea(final int width, final int height, final int newX,
			final int newY, final String message) {
		super(width, height);

		this.newX = newX;
		this.newY = newY;
		this.message = message;

		super.put("server-only", 1);

		LoginNotifier.get().addListener(this);
	}

	/**
	 * Set the message to send to the user when repositioned.
	 *
	 * @param message
	 *            The message to send.
	 */
	public void setMessage(final String message) {
		this.message = message;
	}

	public void onLoggedIn(Player player) {
		if (player.getZone().equals(this.getZone())) {
			if (this.getArea().contains(player.getX(), player.getY())) {
				logger.warn("Login in NoLoginArea, moving player to new location");
				player.setPosition(newX, newY);

				if (message != null) {
					player.sendPrivateText(message);
				}
			}
		}
	}

}
