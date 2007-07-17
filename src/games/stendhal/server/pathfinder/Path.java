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
package games.stendhal.server.pathfinder;

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.ActiveEntity;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.GuidedEntity;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.Logger;

public abstract class Path {

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(Path.class);

	//
	// Path
	//

	/**
	 * Follow this path.
	 * 
	 * @param entity
	 *            The entity to direct along the path.
	 * 
	 * @return <code>true</code> if something to follow, <code>false</code>
	 *         if complete.
	 */
	public abstract boolean follow(ActiveEntity entity);

	/**
	 * Get the final destination point.
	 * 
	 * @return The destination node, or <code>null</code> if there is none
	 *         (i.e. no path, or unbound/infinite movement).
	 */
	public abstract Node getDestination();

	/**
	 * Determine if the path has finished.
	 * 
	 * @return <code>true</code> if there is no more path to follow.
	 */
	public abstract boolean isFinished();

	/**
	 * Finds a path for the Entity <code>entity</code>.
	 * 
	 * @param entity
	 *            the Entity
	 * @param x
	 *            start x
	 * @param y
	 *            start y
	 * @return a list with the path nodes or an empty list if no path is found
	 */
	public static List<Node> searchPath(Entity entity, int x, int y,
			Rectangle2D destination) {
		return searchPath(entity, x, y, destination, -1.0);
	}

	public static List<Node> searchPath(Entity entity, int ex, int ey) {
		return searchPath(entity, entity.getX(), entity.getY(), entity.getArea(
				ex, ey), -1.0);
	}

	/**
	 * Finds a path for the Entity <code>entity</code>.
	 * 
	 * @param entity
	 *            the Entity
	 * @param x
	 *            start x
	 * @param y
	 *            start y
	 * @param destination
	 *            the destination area
	 * @param maxDistance
	 *            the maximum distance (air line) a possible path may be
	 * @return a list with the path nodes or an empty list if no path is found
	 */
	public static List<Node> searchPath(Entity entity, int x, int y,
			Rectangle2D destination, double maxDistance) {
		return searchPath(entity, null, x, y, destination, maxDistance, true);
	}

	/**
	 * Finds a path for the Entity <code>entity</code>.
	 * 
	 * @param entity
	 *            the Entity
	 * @param zone
	 *            the zone, if null the current zone of entity is used.
	 * @param x
	 *            start x
	 * @param y
	 *            start y
	 * @param destination
	 *            the destination area
	 * @param maxDistance
	 *            the maximum distance (air line) a possible path may be
	 * @return a list with the path nodes or an empty list if no path is found
	 */
	public static List<Node> searchPath(Entity sourceEntity,
			StendhalRPZone zone, int x, int y, Rectangle2D destination,
			double maxDistance, boolean withEntities) {

		if (zone == null) {
			zone = sourceEntity.getZone();
		}

		//
		// long startTimeNano = System.nanoTime();
		long startTime = System.currentTimeMillis();

		Pathfinder pathfinder = new Pathfinder(sourceEntity, zone, x, y,
				destination, maxDistance, withEntities);

		List<Node> resultPath = pathfinder.getPath();
		if (logger.isDebugEnabled()
				&& pathfinder.getStatus() == Pathfinder.PATH_NOT_FOUND) {
			logger.debug("Pathfinding aborted: " + zone.getID() + " "
					+ sourceEntity.get("name") + " (" + x + ", " + y + ") "
					+ destination + " Pathfinding time: "
					+ (System.currentTimeMillis() - startTime));
		}

		return resultPath;
	}

	/**
	 * Finds a path for the Entity <code>entity</code> to (or next to) the
	 * other Entity <code>dest</code>.
	 * 
	 * @param entity
	 *            the Entity (also start point)
	 * @param dest
	 *            the destination Entity
	 * @return a list with the path nodes or an empty list if no path is found
	 */
	public static List<Node> searchPath(Entity entity, Entity dest) {
		return searchPath(entity, dest, -1.0);
	}

	/**
	 * Finds a path for the Entity <code>entity</code> to the other Entity
	 * <code>dest</code>.
	 * 
	 * @param entity
	 *            the Entity (also start point)
	 * @param dest
	 *            the destination Entity
	 * @param maxDistance
	 *            the maximum distance (air line) a possible path may be
	 * @return a list with the path nodes or an empty list if no path is found
	 */
	public static List<Node> searchPath(Entity entity, Entity dest,
			double maxDistance) {
		Rectangle2D area = dest.getArea(dest.getX(), dest.getY());

		/*
		 * Expand area by surounding tiles.
		 */
		return searchPath(entity, entity.getX(), entity.getY(), new Rectangle(
				((int) area.getX()) - 1, ((int) area.getY()) - 1, ((int) area
						.getWidth()) + 2, ((int) area.getHeight()) + 2),
				maxDistance);
	}

	/**
	 * Follow the current path (if any) by pointing the direction toward the
	 * next destination point.
	 * 
	 * @param entity
	 *            The entity to point.
	 */
	public static boolean followPath(final GuidedEntity entity) {
		List<Node> path = entity.getPathList();

		if (path == null) {
			return true;
		}

		int pos = entity.getPathPosition();
		Node actual = path.get(pos);

		if ((actual.getX() == entity.getX())
				&& (actual.getY() == entity.getY())) {
			logger.debug("Completed waypoint(" + pos + ")(" + actual.getX()
					+ "," + actual.getY() + ") on Path");
			pos++;
			if (pos < path.size()) {
				entity.setPathPosition(pos);
				actual = path.get(pos);
				logger.debug("Moving to waypoint(" + pos + ")(" + actual.getX()
						+ "," + actual.getY() + ") on Path from ("
						+ entity.getX() + "," + entity.getY() + ")");
				entity.faceto(actual.getX(), actual.getY());
				return false;
			} else {
				if (entity.isPathLoop()) {
					entity.setPathPosition(0);
				} else {
					entity.stop();
					entity.clearPath();
				}

				return true;
			}
		} else {
			logger.debug("Moving to waypoint(" + pos + ")(" + actual.getX()
					+ "," + actual.getY() + ") on Path from (" + entity.getX()
					+ "," + entity.getY() + ")");
			entity.faceto(actual.getX(), actual.getY());
			return false;
		}
	}
}
