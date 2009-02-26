package games.stendhal.client.entity;

import games.stendhal.client.StendhalUI;
import games.stendhal.client.World;
import games.stendhal.client.WorldObjects;
import games.stendhal.client.stendhal;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.client.soundreview.HearingArea;
import games.stendhal.client.update.Version;
import games.stendhal.common.Grammar;
import games.stendhal.common.NotificationType;

import java.awt.geom.Rectangle2D;

import marauroa.common.game.RPObject;

public class User extends Player {

	private static User instance;

	private String serverVersion = null;

	public static boolean isNull() {
		return instance == null;
	}

	public static User get() {
		return instance;
	}

	public User() {
		instance = this;
		modificationCount = 0;
	}

	
	 @Override
	public void onPrivateListen(final String texttype, final String text) {
		super.onPrivateListen(texttype, text);
		World.getPlayerList().generateWhoPlayers(text);
	 }
	/**
	 * When the entity's position changed.
	 * 
	 * @param x
	 *            The new X coordinate.
	 * @param y
	 *            The new Y coordinate.
	 */
	@Override
	protected void onPosition(final double x, final double y) {
		super.onPosition(x, y);

		WorldObjects.firePlayerMoved();
		HearingArea.set(x, y);
	}

	private int modificationCount;

	/**
	 * Returns the modificationCount. This counter is increased each time a
	 * perception is received from the server (so all serverside changes
	 * increases the mod-count). This counter's purpose is to be sure that this
	 * entity is modified or not (ie for gui elements).
	 * @return a number representing the amount of changes.
	 */
	public long getModificationCount() {
		return modificationCount;
	}

	@Override
	protected void onAway(final String message) {
		super.onAway(message);

		String text;
		if (message == null) {
			text = "You are no longer marked as being away.";
		} else {
			text = "You have been marked as being away.";
		}
		StendhalUI.get().addEventLine(new HeaderLessEventLine(text, NotificationType.INFORMATION));
	}

	public static boolean isAdmin() {
		if (isNull()) {
			return false;
		}

		final User me = User.get();
		if (me.rpObject == null) {
			return false;
		}

		return me.rpObject.has("adminlevel")
				&& (me.rpObject.getInt("adminlevel") >= 600);
	}

	public static int getPlayerLevel() {
		if (!isNull()) {
			final User me = User.get();

    		if (me.rpObject != null) {
    			return me.getLevel();
    		}
		}

		return 0;
	}

	public int getObjectID() {
		return rpObject.getID().getObjectID();
	}

	public boolean hasSheep() {
		if (rpObject == null) {
			return false;
		}
		return rpObject.has("sheep");
	}

	public boolean hasPet() {
		if (rpObject == null) {
			return false;
		}
		return rpObject.has("pet");
	}
	
	public int getSheepID() {
		return rpObject.getInt("sheep");
	}
	
	public int getPetID() {
		return rpObject.getInt("pet");
	}

	@Override
	public void onHealed(final int amount) {
		super.onHealed(amount);
		StendhalUI.get().addEventLine(new HeaderLessEventLine(
				getTitle() + " heals "
						+ Grammar.quantityplnoun(amount, "health point") + ".",
				NotificationType.POSITIVE));
	}

	/**
	 * The absolute world area (coordinates) where the player can possibly hear.
	 * sounds
	 * 
	 * @return Rectangle2D area
	 */
	public Rectangle2D getHearingArea() {
		final double HEARING_RANGE = 20;
		final double width = HEARING_RANGE * 2;
		return new Rectangle2D.Double(getX() - HEARING_RANGE, getY()
				- HEARING_RANGE, width, width);
	}

	/**
	 * Initialize this entity for an object.
	 * 
	 * @param object
	 *            The object.
	 * 
	 * @see-also #release()
	 */
	@Override
	public void initialize(final RPObject object) {
		super.initialize(object);
	}

	/**
	 * The object added/changed attribute(s).
	 * 
	 * @param object
	 *            The base object.
	 * @param changes
	 *            The changes.
	 */
	@Override
	public void onChangedAdded(final RPObject object, final RPObject changes) {
		super.onChangedAdded(object, changes);
		modificationCount++;

		// The first time we ignore it.
		if (object != null) {
			if (changes.has("online")) {
				final String[] players = changes.get("online").split(",");
				for (final String playerName : players) {
					StendhalUI.get().addEventLine(new HeaderLessEventLine(
							playerName + " has joined Stendhal.",
							NotificationType.INFORMATION));
				}
			}

			if (changes.has("offline")) {
				final String[] players = changes.get("offline").split(",");
				for (final String playername : players) {
					StendhalUI.get().addEventLine(new HeaderLessEventLine(
							playername + " has left Stendhal.",
							NotificationType.INFORMATION));
				}
			}

			if (changes.has("release")) {
				serverVersion = changes.get("release");
				if (!Version.checkCompatibility(serverVersion,
						stendhal.VERSION)) {
					StendhalUI.get().addEventLine(new HeaderLessEventLine(
							"Your client may not function properly.\nThe version of this server is "
									+ serverVersion
									+ " but your client is version "
									+ stendhal.VERSION
									+ ".\nYou can download version " + serverVersion + " from http://arianne.sourceforge.net ",
							NotificationType.ERROR));
				}
			}
		}
	}

	@Override
	public void onChangedRemoved(final RPObject base, final RPObject diff) {
		modificationCount++;
		super.onChangedRemoved(base, diff);
	}

	/**
	 * Returns true when the entity was modified since the
	 * <i>oldModificationCount</i>.
	 * 
	 * @param oldModificationCount
	 *            the old modificationCount
	 * @return true when the entity was modified, false otherwise
	 * @see #getModificationCount()
	 */
	public boolean isModified(final long oldModificationCount) {
		return oldModificationCount != modificationCount;
	}

	/**
	 * Resets the class to uninitialized.
	 */
	public static void setNull() {
		instance = null;
	}

	/**
	 * Query the version of the server we are currently connected to.
	 * 
	 * @return server version string
	 */
	public String getServerVersion() {
		return serverVersion;
	}

	/**
	 * Returns the objectid for the named item.
	 * 
	 * @param slotName
	 *            name of slot to search
	 * @param itemName
	 *            name of item
	 * @return objectid or <code>-1</code> in case there is no such item
	 */
	public int findItem(final String slotName, final String itemName) {
		for (final RPObject item : getSlot(slotName)) {
			if (item.get("name").equals(itemName)) {
				final int itemID = item.getID().getObjectID();

				return itemID;
			}
		}

		return -1;
    }
	
	@Override
	boolean isUser() {
		return true;
	}
	
	public static double squaredDistanceTo(final double x2, final double y2) {
		if (User.isNull()) {
			return Double.POSITIVE_INFINITY;
		}
		return (User.get().getX() - x2) * (User.get().getX() - x2)
				+ (User.get().getY() - y2) * (User.get().getY() - y2);
	}
}
