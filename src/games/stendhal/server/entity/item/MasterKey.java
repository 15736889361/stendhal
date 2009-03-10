package games.stendhal.server.entity.item;

import java.util.Map;

/**
 * A GM only item to help in checking houses.
 * Opens any door that can be used with <code>HouseKey</code>.
 */
public class MasterKey extends HouseKey {
	public MasterKey(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
		
		setInfoString("anywhere;0;");
	}
	
	// Open any door that can be opened with HouseKeys
	public boolean matches(final String houseId, final int number) {
		return true;
	}
}
