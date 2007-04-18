/*
 * @(#) src/games/stendhal/client/events/FeatureChangeListener.java
 *
 * $Id$
 */

package games.stendhal.client.events;

/**
 * A listener of feature changes.
 */
public interface FeatureChangeListener {
	/**
	 * A feature was disabled.
	 *
	 * @param	name		The name of the feature.
	 */
	public void featureDisabled(String name);


	/**
	 * A feature was enabled.
	 *
	 * @param	name		The name of the feature.
	 * @param	value		Optional feature specific data.
	 */
	public void featureEnabled(String name, String value);
}
