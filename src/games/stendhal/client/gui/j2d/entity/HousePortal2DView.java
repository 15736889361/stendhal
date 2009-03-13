package games.stendhal.client.gui.j2d.entity;

//
//

import games.stendhal.client.entity.ActionType;
import games.stendhal.client.entity.HousePortal;

import java.util.List;

/**
 * The 2D view of a house portal.
 */

// we don't extend portal because that would remove Look from the list of actions. 
// we want it at the top so we couldn't just add it back in.

class HousePortal2DView extends InvisibleEntity2DView {
	/**
	 * The portal entity.
	 */
	protected HousePortal portal;

	/**
	 * Create a 2D view of a portal.
	 * 
	 * @param portal
	 *            The entity to render.
	 */
	public HousePortal2DView(final HousePortal portal) {
		super(portal);

		this.portal = portal;
	}

	//
	// Entity2DView
	//

	/**
	 * Build a list of entity specific actions. <strong>NOTE: The first entry
	 * should be the default.</strong>
	 * 
	 * @param list
	 *            The list to populate.
	 */
	@Override
	protected void buildActions(final List<String> list) {
		super.buildActions(list);
		list.add(ActionType.USE.getRepresentation());
		list.add(ActionType.KNOCK.getRepresentation());
			
	}

	//
	// EntityView
	//

	/**
	 * Perform the default action.
	 */
	@Override
	public void onAction() {
		
		onAction(ActionType.LOOK);
		
	}

	/**
	 * Perform an action.
	 * 
	 * @param at
	 *            The action.
	 */
	@Override
	public void onAction(final ActionType at) {
		switch (at) {
		case USE:

			at.send(at.fillTargetInfo(portal.getRPObject()));
			break;

		case KNOCK:
			
			at.send(at.fillTargetInfo(portal.getRPObject()));
			break;

		default:
			super.onAction(at);
			break;
		}
	}
}
