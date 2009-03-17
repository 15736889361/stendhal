/*
 * @(#) games/stendhal/client/gui/j2d/entity/ActiveEntity2DView.java
 *
 * $Id$
 */

package games.stendhal.client.gui.j2d.entity;

//
//

import games.stendhal.client.entity.ActiveEntity;
import games.stendhal.client.entity.IEntity;
import games.stendhal.common.Direction;

/**
 * The 2D view of an animated entity.
 */
abstract class ActiveEntity2DView extends StateEntity2DView {


	//
	// ActiveEntity2DView
	//

	/**
	 * Get the appropriate named state for a direction.
	 * 
	 * @param direction
	 *            The direction.
	 * 
	 * @return A named state.
	 */
	protected Direction getDirectionState(final Direction direction) {
		if (direction == Direction.STOP) {
			return Direction.DOWN;
		}
		return direction;
	}

	//
	// StateEntity2DView
	//

	/**
	 * Get the current model state.
	 * 
	 * @return The model state.
	 */
	@Override
	protected Direction getState() {
		return getDirectionState(((ActiveEntity) entity).getDirection());
	}

	//
	// Entity2DView
	//

	/**
	 * Determine if this view is currently animatable.
	 * 
	 * @return <code>true</code> if animating enabled.
	 */
	@Override
	protected boolean isAnimating() {
		return !((ActiveEntity) entity).stopped();
	}

	//
	// EntityChangeListener
	//

	/**
	 * An entity was changed.
	 * 
	 * @param entity
	 *            The entity that was changed.
	 * @param property
	 *            The property identifier.
	 */
	@Override
	public void entityChanged(final IEntity entity, final Object property) {
		super.entityChanged(entity, property);

		if (property == ActiveEntity.PROP_DIRECTION) {
			proceedChangedState();
		} else if (property == ActiveEntity.PROP_SPEED) {
			animatedChanged = true;
		}
	}
}
