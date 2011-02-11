/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.map;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.IEntity;
import games.stendhal.common.CollisionDetection;

public class MapPanelController {
	private static MapPanelController instance;
	private MapPanel panel;
	
	public MapPanelController(final StendhalClient client) {
		panel = new MapPanel(client);
		instance = this;
	}
	
	/**
	 * Get the instance
	 * @return the controller instance
	 */
	public static MapPanelController get() {
		return instance;
	}
	
	/**
	 * Get the map panel component
	 * @return component
	 */
	public MapPanel getComponent() {
		return panel;
	}
	
	/**
	 * Add an entity to the map entity list.
	 * The map may ignore entities that it does 
	 * not need to draw.
	 *  
	 * @param entity the entity to be added
	 */
	public void addEntity(final IEntity entity) {
		panel.addEntity(entity);
	}
	
	/**
	 * Remove an entity from the map entity list.
	 * 
	 * @param entity the entity to be removed
	 */
	public void removeEntity(final IEntity entity) {
		panel.removeEntity(entity);
	}
	
	/**
	 * Request redrawing the map screen if the needed.
	 */
	public void refresh() {
		panel.refresh();
	}
	
	/**
	 * Update the map with new data.
	 * 
	 * @param cd
	 *            The collision map.
	 * @param pd  
	 *      	  The protection map.
	 * @param zone
	 *            The zone name.
	 */
	public void update(final CollisionDetection cd, final CollisionDetection pd, final String zone) {
		panel.update(cd, pd, zone);
	}
}
