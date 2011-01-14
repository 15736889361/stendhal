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
package games.stendhal.client.gui;

import games.stendhal.client.entity.EntityChangeListener;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Inspector;
import games.stendhal.client.entity.factory.EntityFactory;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import org.apache.log4j.Logger;

import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * A view of an RPSlot in a grid of ItemPanels.
 */
public class SlotGrid extends JComponent implements EntityChangeListener {
	/**
	 * serial version uid
	 */
	private static final long serialVersionUID = -1822952960582728997L;

	private static final int PADDING = 1;
	private static final Logger logger = Logger.getLogger(SlotGrid.class);
	
	/** All shown item panels */
	private final List<ItemPanel> panels;
	/** The parent entity of the shown slot */
	private IEntity parent;
	/** Name of the shown slot */
	private String slotName;
	/** A slot containing the shown entities */
	private RPSlot shownSlot;
	
	public SlotGrid(int width, int height) {
		setLayout(new GridLayout(height, width, PADDING, PADDING));
		panels = new ArrayList<ItemPanel>();
		
		for (int i = 0; i < width * height; i++) {
			ItemPanel panel = new ItemPanel(null, null);
			panels.add(panel);
			add(panel);
		}
	}
	
	/**
	 * Sets the parent entity of the window.
	 * 
	 * @param parent
	 * @param slot
	 */
	public void setSlot(final IEntity parent, final String slot) {
		this.parent = parent;
		this.slotName = slot;

		/*
		 * Reset the container info for all holders
		 */
		for (final ItemPanel panel : panels) {
			panel.setParent(parent);
			panel.setName(slot);
		}

		parent.addChangeListener(this);
		shownSlot = null;
		rescanSlotContent();
	}
	
	public void entityChanged(IEntity entity, Object property) {
		if (property == IEntity.PROP_CONTENT) {
			rescanSlotContent();
		}
	}
	
	/**
	 * Set the inspector the contained entities should use.
	 * 
	 * @param inspector
	 */
	void setInspector(Inspector inspector) {
		for (ItemPanel panel : panels) {
			panel.setInspector(inspector);
		}
	}
	
	/**
	 * Rescans the content of the slot.
	 */
	private void rescanSlotContent() {
		if ((parent == null) || (slotName == null)) {
			return;
		}

		final RPSlot rpslot = parent.getSlot(slotName);
		
		// Skip if not changed
		if ((shownSlot != null) && shownSlot.equals(rpslot)) {
			return;
		}

		final Iterator<ItemPanel> iter = panels.iterator();

		/*
		 * Fill from contents
		 */
		if (rpslot != null) {
			RPSlot newSlot = (RPSlot) rpslot.clone();

			for (final RPObject object : newSlot) {
				if (!iter.hasNext()) {
					logger.error("More objects than slots: " + slotName);
					break;
				}

				IEntity entity = EntityFactory.createEntity(object);

				if (entity == null) {
					logger.warn("Unable to find entity for: " + object,
							new Throwable("here"));
					continue;
				}

				iter.next().setEntity(entity);
			}
			
			shownSlot = newSlot;
		} else {
			shownSlot = null;
			logger.error("No slot found: " + slotName);
		}

		/*
		 * Clear remaining holders
		 */
		while (iter.hasNext()) {
			iter.next().setEntity(null);
		}
	}
}
