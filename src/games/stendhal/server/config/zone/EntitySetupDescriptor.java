/*
 * @(#) src/games/stendhal/server/config/zone/EntitySetupDescriptor.java
 *
 * $Id$
 */

package games.stendhal.server.config.zone;

//
//

import games.stendhal.server.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.EntityFactoryHelper;

import java.util.HashMap;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.Logger;

/**
 * A generic entity setup descriptor.
 */
public class EntitySetupDescriptor extends SetupDescriptor {
	/**
	 * Logger
	 */
	private static final Logger logger = Log4J.getLogger(EntitySetupDescriptor.class);

	/**
	 * The entity's X coordinate.
	 */
	protected int x;

	/**
	 * The entity's Y coordinate.
	 */
	protected int y;

	/**
	 * The [logical] class name of the implementation.
	 */
	protected String className;

	/**
	 * The generic entity attributes.
	 */
	protected HashMap<String, String> attributes;


	/**
	 * Create an entity setup descriptor.
	 *
	 * @param	x		The X coordinate.
	 * @param	y		The Y coordinate.
	 */
	public EntitySetupDescriptor(final int x, final int y) {
		this.x = x;
		this.y = y;

		attributes = new HashMap<String, String>();
		className = null;
	}


	//
	// EntitySetupDescriptor
	//

	/**
	 * Get the generic entity attributes.
	 *
	 * @return	A map of entity attributes.
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * Get the implementation class name.
	 *
	 * @return	The [logical] class name for the implementation.
	 */
	public String getImplementation() {
		return className;
	}

	/**
	 * Get the X coordinate.
	 *
	 * @return	The entity's X coordinate.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Get the Y coordinate.
	 *
	 * @return	The entity's Y coordinate.
	 */
	public int getY() {
		return y;
	}

	/**
	 * Set a generic entity attribute.
	 *
	 * @param	name		An attribute name.
	 * @param	value		An attribute value.
	 */
	public void setAttribute(final String name, final String value) {
		attributes.put(name, value);
	}

	/**
	 * Set the implementation class name.
	 *
	 * @param	className	The [logical] class name for the
	 *				implementation.
	 */
	public void setImplementation(final String className) {
		this.className = className;
	}


	//
	// SetupDescriptor
	//

	/**
	 * Do appropriete zone setup.
	 *
	 * @param	zone		The zone.
	 */
	@Override
	public void setup(final StendhalRPZone zone) {
		String className = getImplementation();

		if (className == null) {
			logger.error("Entity without factory at "
				+ zone.getID().getID()
				+ "[" + getX() + "," + getY() + "]");
			return;
		}

		try {
			Entity entity = EntityFactoryHelper.create(className, getParameters(), getAttributes());

			if (entity == null) {
				logger.warn("Unable to create entity: " + className);

				return;
			}

			zone.assignRPObjectID(entity);

			entity.setPosition(getX(), getY());

			zone.add(entity);
		} catch (IllegalArgumentException ex) {
			logger.error("Error with entity factory", ex);
		}
	}
}
