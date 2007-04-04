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
package games.stendhal.client.entity;

import games.stendhal.client.GameScreen;
import games.stendhal.client.Sprite;
import games.stendhal.client.SpriteStore;
import games.stendhal.client.StendhalUI;
import games.stendhal.client.stendhal;
import games.stendhal.client.events.AttributeEvent;
import games.stendhal.client.events.CollisionEvent;
import games.stendhal.client.events.MovementEvent;
import games.stendhal.client.events.ZoneChangeEvent;
import games.stendhal.client.sound.SoundSystem;
import games.stendhal.client.entity.ActionType;
import games.stendhal.common.Direction;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.DataLine;

import marauroa.common.Log4J;
import marauroa.common.game.AttributeNotFoundException;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public abstract class Entity implements MovementEvent, ZoneChangeEvent, AttributeEvent, CollisionEvent,
        Comparable<Entity> {

	/** session wide instance identifier for this class
	 * TODO: get rid of this only used by Soundsystem
	 *  
	**/
public final byte[] ID_Token = new byte[0];

	/** The current x location of this entity */
	protected double x;

	/** The current y location of this entity */
	protected double y;

	
	private Direction direction;

	/** The current speed of this entity horizontally (pixels/sec) */
	protected double dx;

	/** The current speed of this entity vertically (pixels/sec) */
	protected double dy;

	/** The arianne object associated with this game entity */
	protected RPObject rpObject;

	private String type;

	/**
	 * The entity name.
	 */
	protected String name;

	/** The object sprite. Animationless, just one frame */
	protected Sprite sprite;

	/**
	 * defines the distance in which the entity is heard by Player
	 */
	protected double audibleRange = Double.POSITIVE_INFINITY;

	private int modificationCount;

	/**
	 * Quick work-around to prevent fireMovementEvent() from calling
	 * in onChangedAdded() from other onAdded() hack.
	 * TODO: Need to fix it all to work right, but not now.
	 */
	protected boolean inAdd = false;

	public Entity() {
		modificationCount = 0;
	}

	protected Entity(RPObject object) throws AttributeNotFoundException {

		type = object.get("type");

		if (object.has("name")) {
			name = object.get("name");
		} else {
			name = type.replace("_", " ");
		}

		rpObject = object;
		x = 0.0;
		y = 0.0;
		dx = 0.0;
		dy = 0.0;
		direction = Direction.STOP;

		loadSprite(object);
	}


	/** Returns the represented arianne object id */
	public RPObject.ID getID() {
		return rpObject != null ? rpObject.getID() : null;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public Direction getDirection() {
		return direction;
	}

	/** the absolute position on the map of this entity */
	public Point2D getPosition() {
		return new Point2D.Double(x, y);
	}


	public double distance(User user) {
	    
	    return (user.getX() - x) * (user.getX() - x) + (user.getY() - y)
        * (user.getY() - y);
    }
	
	protected static String translate(String type) {
		return "data/sprites/" + type + ".png";
	}

	public Sprite getSprite() {
		return sprite;
	}

	/**
	 * Returns the absolute world area (coordinates) to which audibility of
	 * entity sounds is confined. Returns <b>null</b> if confines do not exist
	 * (audible everywhere).
	 */
	public Rectangle2D getAudibleArea() {
		if (audibleRange == Double.POSITIVE_INFINITY) {
			return null;
		}

		double width = audibleRange * 2;
		return new Rectangle2D.Double(getX() - audibleRange, getY() - audibleRange, width, width);
	}

	/**
	 * Sets the audible range as radius distance from this entity's position,
	 * expressed in coordinate units. This reflects an abstract capacity of this
	 * unit to emit sounds and influences the result of
	 * <code>getAudibleArea()</code>.
	 * 
	 * @param range
	 *            double audibility area radius in coordinate units
	 */
	public void setAudibleRange(double range) {
		audibleRange = range;
	}

	/** Loads the sprite that represent this entity */
	protected void loadSprite(RPObject object) {

		sprite = SpriteStore.get().getSprite(translate(object.get("type")));
	}

	/**
	 * compares to floating point values
	 * 
	 * @param d1
	 *            first value
	 * @param d2
	 *            second value
	 * @param diff
	 *            acceptable diff
	 * @return true if they are within diff
	 */
	private static boolean compareDouble(double d1, double d2, double diff) {
		return Math.abs(d1 - d2) < diff;
	}

	/**
	 * calculates the movement if the server an client are out of sync. for some
	 * miliseconds. (server turns are not exactly 300 ms) Most times this will
	 * slow down the client movement
	 * 
	 * @param clientPos
	 *            the postion the client has calculated
	 * @param serverPos
	 *            the postion the server has reported
	 * @param delta
	 *            the movement based on direction
	 * @return the new delta to correct the movement error
	 */
	public static double calcDeltaMovement(double clientPos, double serverPos, double delta) {
		double moveErr = clientPos - serverPos;
		double moveCorrection = (delta - moveErr) / delta;
		return (delta + delta * moveCorrection) / 2;
	}

	// When rpentity moves, it will be called with the data.
	public void onMove(int x, int y, Direction direction, double speed) {

		this.dx = direction.getdx() * speed;
		this.dy = direction.getdy() * speed;
		

		if ((Direction.LEFT.equals( direction )) || (Direction.RIGHT.equals( direction ))) {
			this.y = y;
			if (compareDouble(this.x, x, 1.0)) {
				// make the movement look more nicely: + this.dx * 0.1
				this.dx = calcDeltaMovement(this.x + this.dx * 0.1, x, direction.getdx()) * speed;
			} else {
				this.x = x;
			}
			this.dy = 0;
		} else if ((Direction.UP.equals( direction )) || (Direction.DOWN.equals( direction ))) {
			this.x = x;
			this.dx = 0;
			if (compareDouble(this.y, y, 1.0)) {
				// make the movement look more nicely: + this.dy * 0.1
				this.dy = calcDeltaMovement(this.y + this.dy * 0.1, y, direction.getdy()) * speed;
			} else {
				this.y = y;
			}
		} else {
			// placing entities
			this.x = x;
			this.y = y;
		}
	}

	// When rpentity stops
	public void onStop(int x, int y) {
		direction = Direction.STOP;
		
		this.dx = 0;
		this.dy = 0;

		// set postion to the one reported by server
		this.x = x;
		this.y = y;
	}

	// When rpentity reachs the [x,y,1,1] area.
	public void onEnter(int x, int y) {

	}

	// When rpentity leaves the [x,y,1,1] area.
	public void onLeave(int x, int y) {
	}

	// Called when entity enters a new zone
	public void onEnterZone(String zone) {
	}

	// Called when entity leaves a zone
	public void onLeaveZone(String zone) {
	}

	public void onAdded(RPObject base) {
		// BUG: Work around for Bugs at 0.45
		inAdd = true;
		onChangedAdded(new RPObject(), base);
		inAdd = false;

		fireMovementEvent(base, null);
		fireZoneChangeEvent(base, null);
	}

	public void onChangedAdded(RPObject base, RPObject diff) {
		modificationCount++;

		if (!inAdd) {
			fireMovementEvent(base, diff);
		}
	}

	public void onChangedRemoved(RPObject base, RPObject diff) {
		modificationCount++;
	}

	public void onRemoved() {
		SoundSystem.stopSoundCycle(ID_Token);

		fireMovementEvent(null, null);
		fireZoneChangeEvent(null, null);
	}

	// Called when entity collides with another entity
	public void onCollideWith(Entity entity) {
	}

	// Called when entity collides with collision layer object.
	public void onCollide(int x, int y) {
	}

	protected void fireZoneChangeEvent(RPObject base, RPObject diff) {
		RPObject.ID id = getID();
		if ((diff == null) && (base == null)) {
			// Remove case
			onLeaveZone(id.getZoneID());
		} else if (diff == null) {
			// First time case.
			onEnterZone(id.getZoneID());
		}
	}

	protected void fireMovementEvent(RPObject base, RPObject diff) {
		if ((diff == null) && (base == null)) {
			// Remove case
		} else if (diff == null) {
			// First time case.
			int x = base.getInt("x");
			int y = base.getInt("y");

			Direction direction = Direction.STOP;
			if (base.has("dir")) {
				direction = Direction.build(base.getInt("dir"));
			}

			double speed = 0;
			if (base.has("speed")) {
				speed = base.getDouble("speed");
			}

			onMove(x, y, direction, speed);
		} else {
			// Real movement case
			int x = base.getInt("x");
			int y = base.getInt("y");

			int oldx = x, oldy = y;

			if (diff.has("x")) {
				x = diff.getInt("x");
			}
			if (diff.has("y")) {
				y = diff.getInt("y");
			}

			Direction direction = Direction.STOP;
			if (base.has("dir")) {
				direction = Direction.build(base.getInt("dir"));
			}
			if (diff.has("dir")) {
				direction = Direction.build(diff.getInt("dir"));
			}

			double speed = 0;
			if (base.has("speed")) {
				speed = base.getDouble("speed");
			}
			if (diff.has("speed")) {
				speed = diff.getDouble("speed");
			}

			onMove(x, y, direction, speed);

			if ((Direction.STOP.equals( direction )) || (speed == 0)) {
				onStop(x, y);
			}

			if ((oldx != x) && (oldy != y)) {
				onLeave(oldx, oldy);
				onEnter(x, y);
			}
		}
	}

	public void draw(GameScreen screen) {
		screen.draw(sprite, x, y);

		if (stendhal.SHOW_COLLISION_DETECTION) {
			Graphics g2d = screen.expose();
			Rectangle2D rect = getArea();
			g2d.setColor(Color.green);
			Point2D p = new Point.Double(rect.getX(), rect.getY());
			p = screen.invtranslate(p);
			g2d.drawRect((int) p.getX(), (int) p.getY(), (int) (rect.getWidth() * GameScreen.SIZE_UNIT_PIXELS),
			        (int) (rect.getHeight() * GameScreen.SIZE_UNIT_PIXELS));

			g2d = screen.expose();
			rect = getDrawedArea();
			g2d.setColor(Color.blue);
			p = new Point.Double(rect.getX(), rect.getY());
			p = screen.invtranslate(p);
			g2d.drawRect((int) p.getX(), (int) p.getY(), (int) (rect.getWidth() * GameScreen.SIZE_UNIT_PIXELS),
			        (int) (rect.getHeight() * GameScreen.SIZE_UNIT_PIXELS));
		}
	}

	public void move(long delta) {
		// update the location of the entity based on move speeds
		x += (delta * dx) / 300;
		y += (delta * dy) / 300;
	}

	public boolean stopped() {
		return (dx == 0) && (dy == 0);
	}

	/**
	 * Makes this entity play a sound on the map, at its current location. The
	 * sound is audible to THE player in relation to distance and hearing or
	 * audibility confines. Occurence of this soundplaying can be subject to
	 * random (<code>chance</code>).
	 * 
	 * @param token
	 *            sound library name of the sound to be played
	 * @param volBot
	 *            bottom volume (0..100)
	 * @param volTop
	 *            top volume (0..100)
	 * @param chance
	 *            chance of being performed (0..100)
	 * @return the sound <code>DataLine</code> that is being played, or
	 *         <b>null</b> if not performing
	 */
	public DataLine playSound(String token, int volBot, int volTop, int chance) {
		return SoundSystem.playMapSound(getPosition(), getAudibleArea(), token, volBot, volTop, chance);
	}

	/**
	 * Makes this entity play a sound on the map, at its current location. The
	 * sound is audible to THE player in relation to distance and hearing or
	 * audibility confines.
	 * 
	 * @param token
	 *            sound library name of the sound to be played
	 * @param volBot
	 *            bottom volume (0..100)
	 * @param volTop
	 *            top volume (0..100)
	 * @return the sound <code>DataLine</code> that is being played, or
	 *         <b>null</b> if not performing
	 */
	public DataLine playSound(String token, int volBot, int volTop) {
		return SoundSystem.playMapSound(getPosition(), getAudibleArea(), token, volBot, volTop, 100);
	}

	/** returns the number of slots this entity has */
	public int getNumSlots() {
		return rpObject.slots().size();
	}

	/**
	 * returns the slot with the specified name or null if the entity does not
	 * have this slot
	 */
	public RPSlot getSlot(String name) {
		if (rpObject.hasSlot(name)) {
			return rpObject.getSlot(name);
		}

		return null;
	}

	/** returns a list of slots */
	public List<RPSlot> getSlots() {
		return new ArrayList<RPSlot>(rpObject.slots());
	}

	/**
	 * returns the modificationCount. This counter is increased each time a
	 * perception is received from the server (so all serverside changes
	 * increases the mod-count). This counters purpose is to be sure that this
	 * entity is modified or not (ie for gui elements).
	 */
	public long getModificationCount() {
		return modificationCount;
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
	public boolean isModified(long oldModificationCount) {
		return oldModificationCount != modificationCount;
	}

	abstract public Rectangle2D getArea();

	abstract public Rectangle2D getDrawedArea();

	public ActionType defaultAction() {
		return ActionType.LOOK;
	}

	final public String[] offeredActions() {
		List<String> list = new ArrayList<String>();
		buildOfferedActions(list);
		list.remove(defaultAction().getRepresentation());
		list.add(0, defaultAction().getRepresentation());
		/*
		 * Special admin options
		 */
		if (User.isAdmin()) {
			list.add(ActionType.ADMIN_INSPECT.getRepresentation());
			list.add(ActionType.ADMIN_DESTROY.getRepresentation());
			list.add(ActionType.ADMIN_ALTER.getRepresentation());
		}

		return list.toArray(new String[list.size()]);
	}

	protected void buildOfferedActions(List<String> list) {
		list.add(ActionType.LOOK.getRepresentation());

	}

	public void onAction(ActionType at, String... params) {
		int id;
		RPAction rpaction;
		switch (at) {
			case LOOK:
				rpaction = new RPAction();
				rpaction.put("type", at.toString());
				id = getID().getObjectID();

				if (params.length > 0) {
					rpaction.put("baseobject", params[0]);
					rpaction.put("baseslot", params[1]);
					rpaction.put("baseitem", id);
				} else {
					rpaction.put("target", id);
				}
				at.send(rpaction);
				break;
			case ADMIN_INSPECT:
				rpaction = new RPAction();
				rpaction.put("type", at.toString());
				id = getID().getObjectID();
				rpaction.put("targetid", id);
				at.send(rpaction);
				break;
			case ADMIN_DESTROY:
				rpaction = new RPAction();
				rpaction.put("type", at.toString());
				id = getID().getObjectID();
				rpaction.put("targetid", id);
				at.send(rpaction);
				break;
			case ADMIN_ALTER:
				id = getID().getObjectID();
				StendhalUI.get().setChatLine("/alter #" + id + " ");
				break;
			default:

				Log4J.getLogger(Entity.class).error(at.toString() + ": Action not processed");
				break;
		}

	}

	/**
	 * Checks if this entity should be drawn on top of the given entity, if the
	 * given entity should be drawn on top, or if it doesn't matter.
	 * 
	 * In the first case, this method returns a positive integer. In the second
	 * case, it returns a negative integer. In the third case, it returns 0.
	 * 
	 * Also, players can only interact with the topmost entity.
	 * 
	 * Note: this comparator imposes orderings that are inconsistent with
	 * equals().
	 * 
	 * @param other
	 *            another entity to compare this one to
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	public int compareTo(Entity other) {
		// commented out until someone fixes bug [ 1401435 ] Stendhal: Fix
		// positions system
		// if (this.getY() < other.getY()) {
		// // this entity is standing behind the other entity
		// return -1;
		// } else if (this.getY() > other.getY()) {
		// // this entity is standing in front of the other entity
		// return 1;
		// } else {
		// one of the two entities is standing on top of the other.
		// find out which one.
		return this.getZIndex() - other.getZIndex();
		// }
	}

	/**
	 * Determines on top of which other entities this entity should be drawn.
	 * Entities with a high Z index will be drawn on top of ones with a lower Z
	 * index.
	 * 
	 * Also, players can only interact with the topmost entity.
	 * 
	 * @return drawing index
	 */
	abstract public int getZIndex();
}
