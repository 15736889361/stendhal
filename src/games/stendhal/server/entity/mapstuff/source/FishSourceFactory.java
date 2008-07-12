package games.stendhal.server.entity.mapstuff.source;

import games.stendhal.server.core.config.factory.ConfigurableFactory;
import games.stendhal.server.core.config.factory.ConfigurableFactoryContext;

/**
 * A factory for <code>FishSource</code> objects.
 */
public class FishSourceFactory implements ConfigurableFactory {

	/**
	 * Extract the species name from a context.
	 * 
	 * @param ctx
	 *            The configuration context.
	 * @return The species name.
	 * @throws IllegalArgumentException
	 *             If the attribute is invalid.
	 */
	protected String getSpecies(final ConfigurableFactoryContext ctx) {
		return ctx.getRequiredString("species");
	}

	//
	// ConfigurableFactory
	//

	/**
	 * Create a personal fish source.
	 * 
	 * @param ctx
	 *            Configuration context.
	 * 
	 * @return A FishSource.
	 * 
	 * @see FishSource
	 */
	public Object create(final ConfigurableFactoryContext ctx) {
		return new FishSource(getSpecies(ctx));
	}
}
