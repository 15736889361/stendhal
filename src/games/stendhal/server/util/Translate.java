package games.stendhal.server.util;

import games.stendhal.common.Grammar;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import marauroa.common.Logger;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * A simple translation framework
 *
 * @author hendrik
 */
public class Translate {

	private static Logger logger = Logger.getLogger(Translate.class);

	private static Properties dictionary = new Properties();

	private static Grammar grammar = new Grammar();

	/**
	 * Loads the specified dictionary
	 *
	 * @param language the 2 letter language code
	 */
	public static void initLanguage(String language) {
		try {
			InputStream is = Translate.class.getClassLoader().getResourceAsStream(
			        "data/languages/" + language + ".properties");
			if (is == null) {
				logger.error("No dictionary for language " + language + " on classpath. Check that data/languages/"
				        + language + ".properties exists and that the parent folder of \"data\" is on the classpath.");
			} else {
				dictionary.load(is);
				String className = dictionary.getProperty("_grammar.class");
				if (className != null) {
					grammar = (Grammar) Class.forName(className).newInstance();
				}
			}
		} catch (Exception e) {
			logger.error(e, e);
		}
		try {
			Velocity.init();
		} catch (Exception e) {
			logger.error(e, e);
		}
	}

	/**
	 * Translates a text into the specified language
	 *
	 * @param text text to translate
	 * @param args arguments to integrate
	 * @return translated text
	 */
	public static String _(String text, String... args) {

		// use translated text, if one was specified
		String translated = dictionary.getProperty(text, text);
		;
		if (translated.indexOf("$TODO") > -1) {
			translated = text;
		}

		// write arguments to VelocityContext
		VelocityContext context = new VelocityContext();
		context.put("grammar", grammar);
		int i = 0;
		for (String arg : args) {
			i++;
			context.put(Integer.toString(i), arg);
		}

		// parse arguments and invoke scripts
		Writer writer = new StringWriter();
		try {
			Velocity.evaluate(context, writer, "errors", translated);
		} catch (Exception e) {
			logger.error(e, e);
		}

		// return result
		return writer.toString();
	}
}
