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

import games.stendhal.common.CollisionDetection;
import games.stendhal.tools.tiled.LayerDefinition;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.net.InputSerializer;

/** This class stores the layers that make the floor and the buildings */

public class StaticGameLayers {

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(StaticGameLayers.class);

	/**
	 * Area collision maps.
	 */
	private Map<String, CollisionDetection> collisions;

	/**
	 * The current collision map.
	 */
	private CollisionDetection collision;

	/**
	 * Named layers.
	 */
	private Map<String, LayerRenderer> layers;

	/**
	 * Area tilesets.
	 */
	private Map<String, TileStore> tilesets;

	/**
	 * The current area height.
	 */
	private double	height;

	/**
	 * The current area width.
	 */
	private double	width;

	/** Name of the layers set that we are rendering right now */
	private String area;

	/** true when the area has been changed */
	private boolean areaChanged;

	/**
	 * Whether the internal state is valid
	 */
	private boolean valid;

	public StaticGameLayers() {
		collisions = new HashMap<String, CollisionDetection>();
		layers = new HashMap<String, LayerRenderer>();
		tilesets = new HashMap<String, TileStore>();

		height = 0.0;
		width = 0.0;
		area = null;
		areaChanged = true;
		valid = true;
	}

	/** Returns width in world units */
	public double getWidth() {
		validate();

		return width;
	}

	/** Returns the height in world units */
	public double getHeight() {
		validate();

		return height;
	}

	/** Add a new Layer to the set 
	 * @throws ClassNotFoundException */
	public void addLayer(String name, InputStream in) throws IOException, ClassNotFoundException {
		logger.debug("Layer name: " + name);

		int i = name.indexOf('.');

		if(i == -1) {
			logger.fatal("Old server, please upgrade");
			return;
		}

		String area = name.substring(0, i);
		String layer = name.substring(i + 1);
		
		/**
		 * TODO: 
		 * Encode area name into the data sent from server, so it is simpler to encode the 
		 * area name.
		 */

		if (layer.equals("collision")) {
			/*
			 * Add a collision layer.
			 */
			if(collisions.containsKey(area)) {
				// Repeated layers should be ignored.
				return;
			}

			CollisionDetection collision = new CollisionDetection();
			collision.setCollisionData(LayerDefinition.decode(in));

			collisions.put(area, collision);
		} else if (layer.equals("tilesets")) {
			/*
			 * Add tileset
			 */
			TileStore tileset = new TileStore();
			tileset.addTilesets(new InputSerializer(in));

			tilesets.put(area, tileset);
		} else if (layer.endsWith("_map")) {
			/*
			 * It is the minimap image for this zone.
			 */
		} else {
			/*
			 * It is a tile layer.
			 */
			if(layers.containsKey(name)) {
				// Repeated layers should be ignored.
				return;
			}

			LayerRenderer content = null;

			URL url = getClass().getClassLoader().getResource("data/layers/" + area + "/" + layer + ".jpg");

			if (url != null) {
				content = new ImageRenderer(url);
			}

			if (content == null) {
				//TODO: XXX
				content = new TileRenderer();
				((TileRenderer) content).setMapData(in);
			}

			layers.put(name, content);
		}

		valid = false;
	}

	public boolean collides(Rectangle2D shape) {
		validate();

		if(collision != null) {
			return collision.collides(shape);
		}

		return false;
	}

	/** Removes all layers */
	public void clear() {
		layers.clear();
		tilesets.clear();
		collision = null;
		area = null;
	}

	/** Set the set of layers that is going to be rendered */
	public void setRPZoneLayersSet(String area) {
		logger.debug("Area: "+area);

		this.area = area;
		this.areaChanged = true;
		valid = false;
	}
	

	protected void validate() {
		if(valid == true) {
			return;
		}

		if(area == null) {
			height = 0.0;
			width = 0.0;
			collision = null;

			valid = true;
			return;
		}

		/*
		 * Set collision map
		 */
		collision = collisions.get(area);

		if(collision != null) {
			collisions.put(area, collision);
		}


		/*
		 * Get maximum layer size.
		 * Assign tileset to layers.
		 */
		TileStore tileset = tilesets.get(area);
		height = 0.0;
		width = 0.0;

		String prefix = area + ".";

		for(Map.Entry<String, LayerRenderer> entry : layers.entrySet()) {
			if(entry.getKey().startsWith(prefix)) {
				LayerRenderer lr = entry.getValue();

				lr.setTileset(tileset);
				height = Math.max(height, lr.getHeight());
				width = Math.max(width, lr.getWidth());
			}
		}

		valid = true;
	}


	public String getRPZoneLayerSet() {
		return area;
	}


	public void draw(GameScreen screen, String area, String layer, int x, int y, int width, int height) {
		validate();

		LayerRenderer lr = getLayer(area, layer);

		if(lr != null) {
			lr.draw(screen, x, y, width, height);
		}
	}

	/**
	 * 
	 * @return the CollisionDetection Layer for the current map
	 * 
	 */
	public CollisionDetection getCollisionDetection() {
		validate();

		return collision;
	}

	/**
	 * 
	 * @return the current area/map
	 * 
	 */
	public String getArea() {
		return area;
	}


	/**
	 * Get a layer renderer.
	 *
	 * @return	A layer renderer, or <code>null</code>,
	 */
	public LayerRenderer getLayer(String area, String layer) {
		return layers.get(area + "." + layer);
	}


	/**
	 * @return true if the area has changed since the last
	 */
	public boolean changedArea() {
		return areaChanged;
	}

	/**
	 * resets the areaChanged flag
	 */
	public void resetChangedArea() {
		areaChanged = false;
	}
}
