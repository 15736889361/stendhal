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
package games.stendhal.server.entity.mapstuff.portal;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.UseListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

import org.apache.log4j.Logger;
import marauroa.common.game.RPClass;
import marauroa.common.game.SyntaxException;
import marauroa.common.game.Definition.Type;

/**
 * A portal which teleports the player to another portal if used.
 */
public class Portal extends Entity implements UseListener {
	/**
	 * The hidden flags attribute name.
	 */
	protected static final String ATTR_HIDDEN = "hidden";

	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(Portal.class);

	private boolean settedDestination;

	private Object identifier;

	private String destinationZone;

	private Object destinationReference;

	public static void generateRPClass() {
		try {
			final RPClass portal = new RPClass("portal");
			portal.isA("entity");
			portal.addAttribute(ATTR_HIDDEN, Type.FLAG);
		} catch (final SyntaxException e) {
			logger.error("cannot generate RPClass", e);
		}
	}

	/**
	 * Creates a new portal.
	 */
	public Portal() {
		setRPClass("portal");
		put("type", "portal");

		settedDestination = false;
	}

	/**
	 * Set the portal reference to identify this specific portal with-in a zone.
	 * This value is opaque and requires a working equals(), but typically uses
	 * a String or Integer.
	 * 
	 * @param reference
	 *            A reference tag.
	 */
	public void setIdentifier(final Object reference) {
		this.identifier = reference;
	}

	/**
	 * gets the identifier of this portal.
	 * 
	 * @return identifier
	 */
	public Object getIdentifier() {
		return identifier;
	}

	/**
	 * Set the destination portal zone and reference. The reference should match
	 * the same type/value as that passed to setReference() in the corresponding
	 * portal.
	 * 
	 * @param zone
	 *            The target zone.
	 * @param reference
	 *            A reference tag.
	 */
	public void setDestination(final String zone, final Object reference) {
		this.destinationReference = reference;
		this.destinationZone = zone;
		this.settedDestination = true;
	}

	public Object getDestinationReference() {
		return destinationReference;
	}

	public String getDestinationZone() {
		return destinationZone;
	}

	/**
	 * Determine if this portal is hidden from players.
	 * 
	 * @return <code>true</code> if hidden.
	 */
	@Override
	public boolean isHidden() {
		return has(ATTR_HIDDEN);
	}

	public boolean loaded() {
		return settedDestination;
	}

	@Override
	public String toString() {
		final StringBuilder sbuf = new StringBuilder();
		sbuf.append("Portal");

		final StendhalRPZone zone = getZone();

		if (zone != null) {
			sbuf.append(" at ");
			sbuf.append(zone.getName());
		}

		sbuf.append('[');
		sbuf.append(getX());
		sbuf.append(',');
		sbuf.append(getX());
		sbuf.append(']');

		if (isHidden()) {
			sbuf.append(", hidden");
		}

		return sbuf.toString();
	}

	/**
	 * Use the portal.
	 * 
	 * @param player
	 *            the Player who wants to use this portal
	 * @return <code>true</code> if the portal worked, <code>false</code>
	 *         otherwise.
	 */
	protected boolean usePortal(final Player player) {
		if (!nextTo(player)) {
			// Too far to use the portal
			return false;
		}

		if (getDestinationZone() == null) {
			// This portal is incomplete
			logger.error(this + " has no destination.");
			return false;
		}

		final StendhalRPZone destZone = SingletonRepository.getRPWorld().getZone(
				getDestinationZone());

		if (destZone == null) {
			logger.error(this + " has invalid destination zone: "
					+ getDestinationZone());
			return false;
		}

		final Portal dest = destZone.getPortal(getDestinationReference());

		if (dest == null) {
			// This portal is incomplete
			logger.error(this + " has invalid destination identitifer: "
					+ getDestinationReference());
			return false;
		}

		if (player.teleport(destZone, dest.getX(), dest.getY(), null, null)) {
			player.stop();
			dest.onUsedBackwards(player);
		}
		return true;
	}

	public boolean onUsed(final RPEntity user) {
		return usePortal((Player) user);
	}

	/**
	 * if this portal is the destination of another portal used.
	 * 
	 * @param user
	 *            the player who used the other portal teleporting to us
	 */
	public void onUsedBackwards(final RPEntity user) {
		// do nothing
	}
}
