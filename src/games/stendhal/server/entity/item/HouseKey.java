package games.stendhal.server.entity.item;

import games.stendhal.common.Grammar;

import java.util.Map;

/**
 * A key that matches if the identifier and lock number are the right. 
 */
public class HouseKey extends Item {
	private static String[] imageNames = {
			"purple",
			"turquoise",
			"yellow",
			"lime",
			"pink",
			"red",
			"orange",
			"navy",
			"brown",
			"green",
			"grey",
			"lilac"
	};
	
	public HouseKey(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
		
		setInfoString("nowhere;0;");
		setPersistent(true);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param key the key to be copied.
	 */
	public HouseKey(final HouseKey key) {
		super(key);
	}
	
	@Override
	public String describe() {
		final String[] info = getInfoString().split(";", -1);
		if (info[2].length() > 0) {
			return "You see a key to " + Grammar.suffix_s(info[2]) + " property, " + info[0] + ".";
		} else {
			return "You see a key to " + info[0] + ".";
		}
	}
	
	/**
	 * Set the paramaters of the key.
	 * 
	 * @param id the portal identifier
	 * @param lockNumber the number of the lock
	 * @param owner the owner of the house
	 */
	public void setup(final String id, final int lockNumber, String owner) {
		if (owner == null) {
			owner = "";
		}
		setInfoString(id + ";" + lockNumber + ";" + owner);
		chooseImage();
	}
	
	/**
	 * Check if the key matches a portal.
	 * 
	 * @param houseId identifier of the portal
	 * @param number number of the lock
	 * @return true if the key fits the lock, false otherwise
	 */
	public boolean matches(final String houseId, final int number) {
		final String[] info = getInfoString().split(";");
		
		int keyNumber = Integer.parseInt(info[1]);
		return (info[0].equals(houseId) && keyNumber == number);
	}
	
	/**
	 * Choose an image for the key, depending on door identifier and lock number.
	 * Ignores the owner, even if it's set.
	 */
	private void chooseImage() {
		final String[] info = getInfoString().split(";");
		
		put("subclass", imageNames[Math.abs((info[0].hashCode() + info[1].hashCode()) % imageNames.length)]);
	}
}
