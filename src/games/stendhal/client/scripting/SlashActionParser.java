package games.stendhal.client.scripting;

import games.stendhal.client.actions.SlashActionRepository;
import games.stendhal.common.CommandlineParser;

import java.text.CharacterIterator;

/**
 * Command line parser for the Stendhal client The parser recognizes the
 * registered slash action commands and handles string quoting.
 * 
 * @author Martin Fuchs
 */
public class SlashActionParser extends CommandlineParser {

	SlashActionParser(String text) {
		super(text);
	}

	/**
	 * Extract slash command from the command line start.
	 *
	 * @return
	 */
	protected SlashActionCommand extractCommand() {
		SlashActionCommand command = new SlashActionCommand();

		char ch = ci.current();

		if (ch == CharacterIterator.DONE) {
			command.setError("Missing slash command");
			return command;
		}

		/*
		 * Must be non-space after slash
		 */
		if (Character.isWhitespace(ch)) {
			command.setError("Unexpected space after slash character");
			return command;
		}

		/*
		 * Extract command name
		 */
		if (Character.isLetterOrDigit(ch)) {
			StringBuilder buf = new StringBuilder();

			/*
			 * Word command
			 */
			while ((ch != CharacterIterator.DONE)
					&& !Character.isWhitespace(ch)) {
				buf.append(ch);
				ch = ci.next();
			}

			command.setName(buf.toString());
		} else {
			/*
			 * Special character command
			 */
			command.setName(String.valueOf(ch));

			ci.next();
		}

		return command;
	}

	/**
	 * Parse the given slash command. The text is supposed not to include the
	 * slash character but to start directly after the slash on the client
	 * command line.
	 * 
	 * @param text
	 *            the client command line
	 * @return SlashActionCommand object
	 */
	public static SlashActionCommand parse(final String text) {
		/*
		 * Parse command
		 */
		SlashActionParser parser = new SlashActionParser(text);

		SlashActionCommand command = parser.extractCommand();
		if (command.hasError()) {
			return command;
		}

		/*
		 * Find command handler
		 */
		command.setAction(SlashActionRepository.get(command.getName()));

		int minimum, maximum;

		if (command.getAction() != null) {
			minimum = command.getAction().getMinimumParameters();
			maximum = command.getAction().getMaximumParameters();
		} else {
			/*
			 * Server extension criteria
			 */
			minimum = 0;
			maximum = 1;
		}

		/*
		 * Extract parameters
		 */
		command.setParams(new String[maximum]);

		parser.extractParameters(command, minimum, maximum);

		/*
		 * Remainder text
		 */
		command.setRemainder(parser.getRemainingText());

		return command;
	}

	/**
	 * Return remaining text.
	 *
	 * @return
	 */
	private String getRemainingText() {
		skipWhitespace();

		StringBuilder sbuf = new StringBuilder(ci.getEndIndex() - ci.getIndex() + 1);

		while (ci.current() != CharacterIterator.DONE) {
			sbuf.append(ci.current());
			ci.next();
		}

		return sbuf.toString();
    }

	/**
	 * Read parameters into the given SlashActionCommand object.
	 *
	 * @param command
	 * @param minimum
	 * @param maximum
	 * @return success flag
	 */
	private boolean extractParameters(SlashActionCommand command, int minimum, int maximum) {
		for (int i = 0; i < maximum; i++) {
			skipWhitespace();

			/*
			 * EOL?
			 */
			if (ci.current() == CharacterIterator.DONE) {
				/*
				 * Incomplete parameters?
				 */
				if (i < minimum) {
					command.setError("Missing command parameter for '" + command.getName() + "'");
					return false;
				}

				break;
			}

			/*
			 * Grab parameter
			 */
			command.getParams()[i] = getNextParameter(command);

			if (command.hasError()) {
				return false;
			}
		}

		return true;
    }

}
