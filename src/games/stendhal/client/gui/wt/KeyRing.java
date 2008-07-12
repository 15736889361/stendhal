/**
 * @(#) src/games/stendhal/client/gui/wt/KeyRing.java
 *
 * $Id$
 */

package games.stendhal.client.gui.wt;

//
//

import games.stendhal.client.IGameScreen;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.events.FeatureChangeListener;

/**
 * A key ring.
 */
public class KeyRing extends EntityContainer implements FeatureChangeListener {
	/**
	 * Create a key ring.
	 * 
	 * @param gameScreen
	 * 
	 */
	public KeyRing(IGameScreen gameScreen) {
		// Remember if you change these numbers change also a number in
		// src/games/stendhal/server/entity/RPEntity.java
		super("keyring", 2, 4, gameScreen);

		// Disable by default
		disable();
	}

	//
	// KeyRing
	//

	/**
	 * Disable the keyring.
	 */
	protected void disable() {
		if (isMinimizeable()) {
			setMinimizeable(false);
			setMinimized(true);
		}
	}

	//
	// FeatureChangeListener
	//

	/**
	 * A feature was disabled.
	 * 
	 * @param name
	 *            The name of the feature.
	 */
	public void featureDisabled(String name) {
		if (name.equals("keyring")) {
			disable();
		}
	}

	/**
	 * A feature was enabled.
	 * 
	 * @param name
	 *            The name of the feature.
	 * @param value
	 *            Optional feature specific data.
	 */
	public void featureEnabled(String name, String value) {
		if (name.equals("keyring")) {
			if (!isMinimizeable()) {
				setMinimizeable(true);
				setMinimized(false);
			}
		}
	}

	//
	// WtPanel
	//

	/**
	 * Destroy the panel.
	 * 
	 * @param gameScreen
	 */
	@Override
	public void destroy(IGameScreen gameScreen) {
		// TODO: Could be cleaner reference
		StendhalClient.get().removeFeatureChangeListener(this);

		super.destroy(gameScreen);
	}
}
