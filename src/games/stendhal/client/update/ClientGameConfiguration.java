package games.stendhal.client.update;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * read the configuration file for the client.
 *
 * @author hendrik
 */
public class ClientGameConfiguration {

	private static ClientGameConfiguration instance = null;

	private Properties gameConfig;

	private ClientGameConfiguration() {
		// Singleton pattern, hide constructor
		try {
			Properties temp = new Properties();
			InputStream is = ClientGameConfiguration.class.getResourceAsStream("game-default.properties");
			temp.load(is);
			is.close();

			gameConfig = new Properties(temp);
			is = ClientGameConfiguration.class.getResourceAsStream("game.properties");
			if (is != null) {
				gameConfig.load(is);
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void init() {
		if (instance == null) {
			instance = new ClientGameConfiguration();
		}
	}

	/**
	 * gets a configuration value, in case it is undefined, the default
	 * of game-default.properties is returned. If this is undefined, too,
	 * the return value is null
	 *
	 * @param key key
	 * @return configured value
	 */
	public static String get(String key) {
		init();
		return instance.gameConfig.getProperty(key);
	}
}
