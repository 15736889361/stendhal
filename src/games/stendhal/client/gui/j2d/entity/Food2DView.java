/*
 * @(#) games/stendhal/client/gui/j2d/entity/Food2DView.java
 *
 * $Id$
 */

package games.stendhal.client.gui.j2d.entity;

//
//

import games.stendhal.client.GameScreen;
import games.stendhal.client.entity.Entity;
import games.stendhal.client.entity.Food;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

import java.util.Map;

/**
 * The 2D view of food.
 */
public class Food2DView extends StateEntity2DView {
	/**
	 * The food entity.
	 */
	protected Food food;

	/**
	 * The number of states.
	 */
	protected int states;

	/**
	 * Create a 2D view of food.
	 *
	 * @param entity
	 *            The entity to render.
	 * @param states
	 *            The number of states.
	 */
	public Food2DView(final Food food, int states) {
		super(food);

		this.food = food;
		this.states = states;
	}

	//
	// StateEntity2DView
	//

	/**
	 * Populate named state sprites.
	 *
	 * @param map
	 *            The map to populate.
	 */
	@Override
	protected void buildSprites(final Map<Object, Sprite> map) {
		SpriteStore store = SpriteStore.get();
		Sprite tiles = store.getSprite(translate(entity.getType()));

		int theight = tiles.getHeight();
		int i = 0;

		// TODO: Allow animated frames
		for (int y = 0; y < theight; y += GameScreen.SIZE_UNIT_PIXELS) {
			map.put(new Integer(i++), store.getTile(tiles, 0, y,
					GameScreen.SIZE_UNIT_PIXELS, GameScreen.SIZE_UNIT_PIXELS));
		}
	}

	/**
	 * Get the current entity state.
	 *
	 * @return The current state.
	 */
	@Override
	protected Object getState() {
		return new Integer(food.getAmount());
	}

	//
	// Entity2DView
	//

	/**
	 * Determines on top of which other entities this entity should be drawn.
	 * Entities with a high Z index will be drawn on top of ones with a lower Z
	 * index.
	 *
	 * Also, players can only interact with the topmost entity.
	 *
	 * @return The drawing index.
	 */
	@Override
	public int getZIndex() {
		return 6000;
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
	public void entityChanged(final Entity entity, final Object property) {
		super.entityChanged(entity, property);

		if (property == Food.PROP_AMOUNT) {
			stateChanged = true;
		}
	}
}
