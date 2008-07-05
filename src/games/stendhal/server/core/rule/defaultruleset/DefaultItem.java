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
package games.stendhal.server.core.rule.defaultruleset;

import games.stendhal.server.entity.item.Item;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * All default items which can be reduced to stuff that increase the attack
 * point and stuff that increase the defense points.
 * 
 * @author Matthias Totz, chad3f
 */
public class DefaultItem {

	private static final Logger logger = Logger.getLogger(DefaultItem.class);

	/** items class. */
	private String clazz;

	/** items sub class. */
	private String subclazz;

	/** items type. */
	private String name;

	/** optional item description. */
	private String description;

	// weight system is not yet implemented.
	@SuppressWarnings("unused")
	private double weight;

	/** slots where this item can be equipped. */
	private List<String> slots;

	/** Map Tile Id. */
	private int tileid;

	/** Attributes of the item .*/
	private Map<String, String> attributes;

	/** Implementation creator. */
	protected Creator creator;

	private Class< ? > implementation;

	private int value;

	public DefaultItem(String clazz, String subclazz, String name, int tileid) {
		this.clazz = clazz;
		this.subclazz = subclazz;
		this.name = name;
		this.tileid = tileid;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public void setEquipableSlots(List<String> slots) {
		this.slots = slots;
	}

	public List<String> getEquipableSlots() {
		return slots;
	}

	public void setDescription(String text) {
		this.description = text;
	}

	public String getDescription() {
		return description;
	}

	public void setImplementation(Class< ? > implementation) {
		this.implementation = implementation;
		creator = buildCreator(implementation);
	}

	public Class< ? > getImplementation() {
		return implementation;
	}

	/**
	 * Build a creator for the class. It uses the following constructor search
	 * order:<br>
	 * 
	 * <ul>
	 * <li><em>Class</em>(<em>name</em>, <em>clazz</em>,
	 * <em>subclazz</em>, <em>attributes</em>)
	 * <li><em>Class</em>(<em>attributes</em>)
	 * <li><em>Class</em>()
	 * </ul>
	 * 
	 * @param implementation
	 *            The implementation class.
	 * 
	 * @return A creator, or <code>null</code> if none found.
	 */
	protected Creator buildCreator(Class< ? > implementation) {
		Constructor< ? > construct;

		/*
		 * <Class>(name, clazz, subclazz, attributes)
		 */
		try {
			construct = implementation.getConstructor(new Class[] {
					String.class, String.class, String.class, Map.class });

			return new FullCreator(construct);
		} catch (NoSuchMethodException ex) {
			// ignore and continue
		}

		/*
		 * <Class>(attributes)
		 */
		try {
			construct = implementation.getConstructor(new Class[] { Map.class });

			return new AttributesCreator(construct);
		} catch (NoSuchMethodException ex) {
			// ignore and continue
		}

		/*
		 * <Class>()
		 */
		try {
			construct = implementation.getConstructor(new Class[] {});

			return new DefaultCreator(construct);
		} catch (NoSuchMethodException ex) {
			// ignore and continue
		}

		return null;
	}

	/**
	 * Returns an item-instance.
	 * 
	 * @return An item, or <code>null</code> on error.
	 */
	public Item getItem() {

		/*
		 * Just in case - Really should generate fatal error up front (in
		 * ItemXMLLoader).
		 */
		if (creator == null) {
			return null;
		}
		Item item = creator.createItem();
		if (item != null) {
			item.setEquipableSlots(slots);
			item.setDescription(description);
		}

		return item;
	}

	/** @return the tile id .*/
	public int getTileId() {
		return tileid;
	}

	public void setTileId(int val) {
		tileid = val;
	}

	public void setValue(int val) {
		value = val;
	}

	public int getValue() {
		return value;
	}

	/** @return the class. */
	public String getItemClass() {
		return clazz;
	}

	public void setItemClass(String val) {
		clazz = val;
	}

	/** @return the subclass. */
	public String getItemSubClass() {
		return subclazz;
	}

	public void setItemSubClass(String val) {
		subclazz = val;
	}

	public String getItemName() {
		return name;
	}

	public void setItemName(String val) {
		name = val;
	}

	public String toXML() {
		StringBuilder os = new StringBuilder();
		os.append("  <item name=\"" + name + "\">\n");
		os.append("    <type class=\"" + clazz + "\" subclass=\"" + subclazz
				+ "\" tileid=\"" + tileid + "\"/>\n");
		if (description != null) {
			os.append("    <description>" + description + "</description>\n");
		}
		os.append("    <implementation class-name=\""
				+ implementation.getCanonicalName() + "\"/>");
		os.append("    <attributes>\n");
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			os.append("      <" + entry.getKey() + " value=\""
					+ entry.getValue() + "\"/>\n");
		}

		os.append("    </attributes>\n");
		os.append("    <weight value=\"" + weight + "\"/>\n");
		os.append("    <value value=\"" + value + "\"/>\n");
		os.append("    <equipable>\n");
		for (String slot : slots) {
			os.append("      <slot name=\"" + slot + "\"/>\n");
		}
		os.append("    </equipable>\n");
		os.append("  </item>\n");
		return os.toString();
	}

	//
	//

	/**
	 * Base item creator (using a constructor).
	 */
	protected abstract class Creator {

		protected Constructor< ? > construct;

		public Creator(Constructor< ? > construct) {
			this.construct = construct;
		}

		protected abstract Object create() throws IllegalAccessException,
				InstantiationException, InvocationTargetException;

		public Item createItem() {
			try {
				return (Item) create();
			} catch (IllegalAccessException ex) {
				logger.error("Error creating item: " + name, ex);
			} catch (InstantiationException ex) {
				logger.error("Error creating item: " + name, ex);
			} catch (InvocationTargetException ex) {
				logger.error("Error creating item: " + name, ex);
			} catch (ClassCastException ex) {
				/*
				 * Wrong type (i.e. not [subclass of] Item)
				 */
				logger.error("Implementation for " + name
						+ " is not an Item class");
			}

			return null;
		}
	}

	/**
	 * Create an item class via the <em>attributes</em> constructor.
	 */
	protected class AttributesCreator extends Creator {

		public AttributesCreator(Constructor< ? > construct) {
			super(construct);
		}

		@Override
		protected Object create() throws IllegalAccessException,
				InstantiationException, InvocationTargetException {
			return construct.newInstance(new Object[] { attributes });
		}
	}

	/**
	 * Create an item class via the default constructor.
	 */
	protected class DefaultCreator extends Creator {

		public DefaultCreator(Constructor< ? > construct) {
			super(construct);
		}

		@Override
		protected Object create() throws IllegalAccessException,
				InstantiationException, InvocationTargetException {
			return construct.newInstance(new Object[] {});
		}
	}

	/**
	 * Create an item class via the full arguments (<em>name, clazz,
	 * subclazz, attributes</em>)
	 * constructor.
	 */
	protected class FullCreator extends Creator {

		public FullCreator(Constructor< ? > construct) {
			super(construct);
		}

		@Override
		protected Object create() throws IllegalAccessException,
				InstantiationException, InvocationTargetException {
			return construct.newInstance(new Object[] { name, clazz, subclazz,
					attributes });
		}
	}
}
