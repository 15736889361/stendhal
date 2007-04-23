/* $Id$ */
/***************************************************************************
 *                      (C) Copyright 2005 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
/*
 * Minimap.java
 * Created on 16. Oktober 2005, 13:34
 */
package games.stendhal.client.gui.wt;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.Creature;
import games.stendhal.client.entity.Entity;
import games.stendhal.client.entity.NPC;
import games.stendhal.client.entity.Player;
import games.stendhal.client.entity.Sheep;
import games.stendhal.client.entity.SheepFood;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.wt.core.WtPanel;
import games.stendhal.common.CollisionDetection;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import marauroa.common.game.RPAction;

/**
 * The minimap.
 * 
 * @author mtotz
 */
public class Minimap extends WtPanel {

	/** width of the minimap */
	private static final int MINIMAP_WIDTH = 129;

	/** height of the minimap */
	private static final int MINIMAP_HEIGHT = 129;

	/** minimum scale of the minimap */
	private static final int MINIMAP_MINIMUM_SCALE = 2;

	/** Enable X-ray vision (aka Superman) minimap? */
	private static final boolean mininps = (System.getProperty("stendhal.superman") != null);

	/** scale of map */
	private int scale;

	/** width of (scaled) minimap */
	private int width;

	/** height of (scaled) minimap */
	private int height;

	/** minimap image */
	private BufferedImage image;

	/** list of players */
	private Player player = null;

	private StendhalClient client;


	/** Creates a new instance of Minimap */
	public Minimap(StendhalClient client) {
		super("minimap", 0, 0, 100, 100);

		this.client = client;
	}

	/**
	 * Update the map with new data.
	 *
	 * @param	cd		The collision map.
	 * @param	gc		A graphics configuration.
	 * @param	zone		The zone name.
	 */
	public void update(CollisionDetection cd, GraphicsConfiguration gc, String zone) {
		setTitletext(zone);

		// calculate size and scale
		int w = cd.getWidth();
		int h = cd.getHeight();

		// calculate scale
		scale = MINIMAP_MINIMUM_SCALE;
		while ((w * (scale + 1) < MINIMAP_WIDTH) && (h * (scale + 1) < MINIMAP_HEIGHT)) {
			scale++;
		}

		// calculate size of map
		width = (w * scale < MINIMAP_WIDTH) ? w * scale : MINIMAP_WIDTH;
		height = (h * scale < MINIMAP_HEIGHT) ? h * scale : MINIMAP_HEIGHT;

		// create the image for the minimap
		image = gc.createCompatibleImage(w * scale, h * scale);
		Graphics2D mapgrapics = image.createGraphics();
		Color freeColor = new Color(0.8f, 0.8f, 0.8f);
		// Color freeColor = new Color(0.0f, 1.0f, 0.0f);
		Color blockedColor = new Color(1.0f, 0.0f, 0.0f);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				boolean walkable = cd.walkable(x, y);
				mapgrapics.setColor(walkable ? freeColor : blockedColor);
				mapgrapics.fillRect(x * scale, y * scale, scale, scale);
			}
		}

		/*
		 * XXX - TEMP!!!
		 * Show SheepFood as obsticals until all maps can be fixed.
		 */
		mapgrapics.setColor(blockedColor);

		for (Entity entity : client.getGameObjects()) {
			if (entity instanceof SheepFood) {
				mapgrapics.fillRect(((int) entity.getX()) * scale, ((int) entity.getY()) * scale, scale, scale);
			}
		}

		setTitleBar(true);
		setFrame(true);
		setMovable(true);
		setMinimizeable(true);
		// now resize the panel to match the size of the map
		resizeToFitClientArea(width, height);
	}

	/** we're using the window manager */
	@Override
	protected boolean useWindowManager() {
		return true;
	}

	/**
	 * Draws the minimap.
	 * 
	 * @param g graphics object for the game main window
	 */
	@Override
	public Graphics draw(Graphics g) {
		if (isClosed()) {
			return g;
		}

		// draw frame and title
		Graphics clientg = super.draw(g);

		if ((player == null) || (image == null)) {
			return g;
		}

		// don't draw the minimap when we're miminized
		if (isMinimized()) {
			return clientg;
		}

		// now calculate how to pan the minimap
		int panx = 0;
		int pany = 0;

		int w = image.getWidth();
		int h = image.getHeight();

		int xpos = (int) (player.getX() * scale) - width / 2;
		int ypos = (int) ((player.getY() + 1) * scale) - width / 2;

		if (w > width) {
			// need to pan width
			if ((xpos + width) > w) {
				// x is at the screen border
				panx = w - width;
			} else if (xpos > 0) {
				panx = xpos;
			}
		}

		if (h > height) {
			// need to pan height
			if ((ypos + height) > h) {
				// y is at the screen border
				pany = h - height;
			} else if (ypos > 0) {
				pany = ypos;
			}
		}

		// draw minimap
		clientg.drawImage(image, -panx, -pany, null);

		// Enabled with -Dstendhal.superman=x.
		if (mininps && User.isAdmin()) {
			// draw npcs (and creatures/sheeps)
			clientg.translate(-panx, -pany);

			for (Entity entity : client.getGameObjects()) {
				drawNPC(clientg, entity);
			}

			clientg.translate(panx, pany);
		}

		// draw players
		Color playerColor = Color.WHITE;
		for (Entity entity : client.getGameObjects()) {
			if (entity instanceof Player) {
				Player aPlayer = (Player) entity;
				drawCross(clientg, (int) (aPlayer.getX() * scale) - panx + 1, (int) ((aPlayer.getY() + 1) * scale)
				        - pany + 2, playerColor);
			}
		}

		// draw myself
		playerColor = Color.BLUE;
		drawCross(clientg, (int) (player.getX() * scale) - panx + 1, (int) ((player.getY() + 1) * scale) - pany + 2,
		        playerColor);

		return g;
	}

	/**
	 * draws NPC as rectangle to Minimap selecting color by class of Entity
	 * @param g Graphics
	 * @param entity the entity dto be drawn
	 */
	protected void drawNPC(final Graphics g, final Entity entity) {
		if (entity instanceof Sheep) {
			drawNPC(g, entity, Color.ORANGE);
		} else if (entity instanceof Creature) {
			drawNPC(g, entity, Color.YELLOW);
		} else if (entity instanceof NPC) {
			drawNPC(g, entity, Color.BLUE);
		} else {
			drawNPC(g, entity, new Color(200, 255, 200));
		}
	}

	/**
	 * calculates position of NPC rectangle and draws it to Minimap in the sepecified color
	 * @param g graphics
	 * @param entity the Entity to be drawn
	 * @param color the Color to be used
	 */
	protected void drawNPC(final Graphics g, final Entity entity, final Color color) {
		Rectangle2D area;
		area = entity.getArea();
		g.setColor(color);
		g.drawRect(((int) (area.getX() + 0.5)) * scale, ((int) (area.getY() + 0.5)) * scale,
		        (((int) area.getWidth()) * scale) - 1, (((int) area.getHeight()) * scale) - 1);
	}

	/** draws a cross at the given position */
	private void drawCross(Graphics g, int x, int y, Color color) {
		int size = 2;

		g.setColor(color);
		g.drawLine(x - size, y, x + size, y);
		g.drawLine(x, y + size, x, y - size);
	}

	/**
	 * sets the current Player
	 * @param player 
	 */
	public void setPlayer(final Player player) {
		this.player = player;
	}

	@Override
	public synchronized boolean onMouseDoubleClick(Point p) {
		/*
		 * Missing required data?
		 */
		if ((player == null) || (image == null)) {
			return false;
		}

		// Move the player to p

		// first calculate the world destination coords
		int panx = 0;
		int pany = 0;

		int w = image.getWidth();
		int h = image.getHeight();

		int xpos = (int) (player.getX() * scale) - width / 2;
		int ypos = (int) ((player.getY() + 1) * scale) - width / 2;

		if (w > width) {
			// need to pan width
			if ((xpos + width) > w) {
				// x is at the screen border
				panx = w - width;
			} else if (xpos > 0) {
				panx = xpos;
			}
		}

		if (h > height) {
			// need to pan height
			if ((ypos + height) > h) {
				// y is at the screen border
				pany = h - height;
			} else if (ypos > 0) {
				pany = ypos;
			}
		}

		// Now we have the world destination coords
		int go_toX = (p.x + panx - 4) / scale;
		int go_toY = (p.y + pany - scale - 18) / scale;

		RPAction action = new RPAction();
		action.put("type", "moveto");
		action.put("x", go_toX);
		action.put("y", go_toY);
		client.send(action);
		return true;
	}
}
