/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.parser;

import games.stendhal.common.Grammar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import marauroa.common.Log4J;

import org.apache.log4j.Logger;

/**
 * WordList stores a list of words recognized by the ConversationParser. Words
 * are categorized by type (noun, verb, adjective, preposition) and optionally
 * sub-types (animals, food, fluids, ...).
 * 
 * @author Martin Fuchs
 */

public class WordList {

	private static final Logger logger = Logger.getLogger(WordList.class);

	// ExpressionTypes for dynamic registration
	public static final String SUBJECT_NAME_DYNAMIC = ExpressionType.SUBJECT_NAME
			+ ExpressionType.SUFFIX_DYNAMIC;
	public static final String VERB_DYNAMIC = ExpressionType.VERB
			+ ExpressionType.SUFFIX_DYNAMIC;

	public static final String WORDS_FILENAME = "words.txt";

	static final String HASH_KEYWORD = "@Hash";

	protected Map<String, WordEntry> words = new TreeMap<String, WordEntry>();

	Set<Sentence> compoundNames = new HashSet<Sentence>();

	protected String hash = "";

	/** instance variable, with package protection because of FindBugs hint */
	static /*protected*/ WordList instance;

	// We keep house holding the usage of registered subject names (see registerSubjectName).
	private Map<String, Integer> subjectRefCount = new HashMap<String, Integer>();

	/**
	 * Take over the content of the other WordList object.
	 * @param other
	 */
	protected void takeOver(WordList other)
	{
		words = instance.words;
		compoundNames = instance.compoundNames;
		hash = instance.hash;
		subjectRefCount = other.subjectRefCount;
	}

	// initialize the word list by querying the database or reading from the
	// input file "words.txt" in the class path
	static {
		Log4J.init();

		initInstance();
	}

	/**
	 * Initializes the WordList instance.
	 */
	private static void initInstance() {
		// read word list from "words.txt"
		instance = new WordList();

		instance.readFromResources();
	}

	/**
	 * Reads the word list from the resource file "words.txt".
	 */
	private void readFromResources() {
		final InputStream str = WordList.class.getResourceAsStream(WORDS_FILENAME);

		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(str));

			try {
				read(reader, null);
			} catch (final IOException e) {
				logger.error("error while reading resource file 'words.txt'", e);
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("error while closing reader stream for 'words.txt'", e);
				}
			}
		} finally {
			try {
				str.close();
			} catch (IOException e) {
				logger.warn("exception on closing resource stream", e);
			}
		}
	}

	/**
	 * Returns a reference to the global word list instance.
	 * 
	 * @return WordList
	 */
	public static WordList getInstance() {
		return instance;
	}

	/**
	 * Returns the WordList version number.
	 * 
	 * @return MD5 hash code
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * Updates the MD5 hash code.
	 * 
	 * @return true on success
	 */
	boolean calculateHash() {
		MessageDigest md;

		try {
			md = MessageDigest.getInstance("MD5");
		} catch (final NoSuchAlgorithmException e) {
			return false;
		}

		for (final WordEntry e : words.values()) {
			String s = e.getNormalized();
			if (s != null) {
				md.update(s.getBytes());
			}

			s = e.getPlurSing();
			if (s != null) {
				md.update(s.getBytes());
			}

			s = e.getTypeString();
			if (s != null) {
				md.update(s.getBytes());
			}

			// md.update(e.getValue().getBytes());
		}

		final byte[] buffer = md.digest();

		final StringBuffer sb = new StringBuffer();

		for (final byte b : buffer) {
			sb.append(Integer.toHexString(b & 0xFF).toUpperCase());
		}

		hash = sb.toString();

		return true;
	}

	/**
	 * Reads word list from reader object.
	 * 
	 * @param reader
	 * @param comments
	 * @throws IOException
	 */
	public void read(final BufferedReader reader, final List<String> comments)
			throws IOException {
		while (true) {
			final String line = reader.readLine();
			if (line == null) {
				break;
			}

			if (line.startsWith("#")) {
				if (comments != null) {
					comments.add(line);
				}
			} else {
				final StringTokenizer tk = new StringTokenizer(line);

				if (!tk.hasMoreTokens()) {
					continue;
				}

				String key = tk.nextToken();

				key = trimWord(key);
				final WordEntry entry = new WordEntry();
				entry.setNormalized(key);

				readEntryLine(key, tk, entry);
				addEntry(key, entry);
			}
		}

		// calculate the hash value from all word entries
		calculateHash();
	}

	/**
	 * Reads one line of the word list and adds the new entry.
	 * 
	 * @param key
	 * 
	 * @param tk
	 * @param entry
	 */
	private void readEntryLine(final String key, final StringTokenizer tk,
			final WordEntry entry) {
		if (tk.hasMoreTokens()) {
			entry.setType(new ExpressionType(tk.nextToken()));

			if (tk.hasMoreTokens()) {
				String s = tk.nextToken();

				if (s.charAt(0) == '=') {
					entry.setNormalized(trimWord(s.substring(1)));
					if (tk.hasMoreTokens()) {
						s = tk.nextToken();
					} else {
						s = null;
					}
				}

				if (s != null) {
					if (entry.getType().isNumeral()) {
						entry.setValue(Integer.valueOf(s));
					} else {
						entry.setPlurSing(s);
					}
				}
			}

			final String normalized = entry.getNormalized();

			if (Character.isLowerCase(entry.getTypeString().charAt(0))) {
				// Type identifiers are always upper case, so a word in
				// lower case must be a plural.
				entry.setType(new ExpressionType(ExpressionType.OBJECT));
				entry.setPlurSing(trimWord(entry.getTypeString()));
			} else if ((entry.getPlurSing() == null)
					&& entry.getType().isObject()) {
				// complete missing plural expressions using the
				// Grammar.plural() function
				final String plural = Grammar.plural(normalized);

				// only store single word plurals
				if (plural.indexOf(' ') == -1) {
					entry.setPlurSing(plural);
				}
			} else if (entry.getPlurSing() != null) {
				// check plural strings using the Grammar.plural() function
				if (!entry.getType().isPronoun() && !normalized.equals("is")) {
					String plural = Grammar.plural(key);

					if ((plural.indexOf(' ') == -1)
							&& !plural.equals(entry.getPlurSing())) {
						// retry with normalized in case it differs from key
						plural = Grammar.plural(normalized);

						if ((plural.indexOf(' ') == -1)
								&& !plural.equals(entry.getPlurSing())) {
							logger.warn(String.format(
									"suspicious plural: %s -> %s (%s?)", key,
									entry.getPlurSing(), plural));
						}
					}
				}
			}

			while (tk.hasMoreTokens()) {
				logger.warn("superfluous trailing word in words.txt: "
						+ tk.nextToken());
			}
		}
	}

	/**
	 * Add one entry to the word list.
	 * 
	 * @param key
	 * @param entry
	 */
	protected void addEntry(final String key, final WordEntry entry) {
		words.put(trimWord(key), entry);

		// store plural and associate with singular form
		if ((entry.getPlurSing() != null)
				&& !entry.getPlurSing().equals(entry.getNormalized())) {
			final WordEntry pluralEntry = new WordEntry();

			pluralEntry.setNormalized(entry.getPlurSing());
			pluralEntry.setType(new ExpressionType(entry.getTypeString()
					+ ExpressionType.SUFFIX_PLURAL));
			pluralEntry.setPlurSing(entry.getNormalized());
			pluralEntry.setValue(entry.getValue());

			final WordEntry prev = words.put(entry.getPlurSing(), pluralEntry);

			if (prev != null) {
				logger.debug(String.format("ambiguous plural: %s/%s -> %s",
						pluralEntry.getPlurSing(), prev.getPlurSing(),
						entry.getPlurSing()));

				pluralEntry.setPlurSing(null);
				prev.setPlurSing(null);
			}
		}
	}

	/**
	 * Print all words of a given (main-)type.
	 * 
	 * @param writer
	 * @param type
	 */
	void printWordType(final PrintWriter writer, final String type) {
		for (final String key : words.keySet()) {
			final WordEntry entry = words.get(key);
			boolean matches;

			if (type == null) {
				// match all entries with empty type specifier
				matches = entry.getType() == null;
			} else {
				// all real (no plural) entries with matching type string
				matches = entry.getTypeString().startsWith(type)
						&& !entry.isPlural();
			}

			if (matches) {
				entry.print(writer, key);

				writer.println();
			}
		}
	}

	/**
	 * Transform the given word to lower case and trim special characters at
	 * beginning and end to use this normalized form as key in the word list.
	 * 
	 * @param word
	 * @return the trimmed word
	 */
	public static String trimWord(final String word) {
		String tempword = word.toLowerCase();

		// Currently we only need to trim "'" characters.
		while (tempword.length() > 0) {
			final char c = tempword.charAt(0);

			if (c == '\'') {
				tempword = tempword.substring(1);
			} else {
				break;
			}
		}

		while (tempword.length() > 0) {
			final char c = tempword.charAt(tempword.length() - 1);

			if (c == '\'') {
				tempword = tempword.substring(0, tempword.length() - 1);
			} else {
				break;
			}
		}

		return tempword;
	}

	/**
	 * Find an entry for a given word.
	 * 
	 * @param str
	 * @return WordEntry
	 */
	public WordEntry find(final String str) {
		final WordEntry entry = words.get(trimWord(str));

		return entry;
	}

	/**
	 * Lookup the plural form of the given word from the word list.
	 * 
	 * @param word
	 * @return plural string
	 */
	public String plural(final String word) {
		final WordEntry entry = words.get(trimWord(word));

		if (entry != null) {
			if ((entry.getType() != null) && !entry.getType().isPlural()) {
				// return the associated singular from the word list
				return entry.getPlurSing();
			} else {
				// The word is already in singular form.
				return entry.getNormalized();
			}
		} else {
			// fall back: call Grammar.plural()
			return Grammar.plural(word);
		}
	}

	/**
	 * Lookup the singular form of the given word from the word list.
	 * 
	 * @param word
	 * @return singular string
	 */
	public String singular(final String word) {
		final WordEntry entry = words.get(trimWord(word));

		if (entry != null) {
			if ((entry.getType() != null) && entry.getType().isPlural()) {
				// return the associated singular from the word list
				return entry.getPlurSing();
			} else {
				// The word is already in singular form.
				return entry.getNormalized();
			}
		} else {
			// fall back: call Grammar.singular()
			return Grammar.singular(word);
		}
	}

	/**
	 * Try to normalize the given verb.
	 * 
	 * @param word
	 * 
	 * @return WordEntry
	 */
	WordEntry normalizeVerb(final String word) {
		String trimmedWord = trimWord(word);

		final String normalized = Grammar.normalizeRegularVerb(trimmedWord);

		if (normalized != null) {
			WordEntry entry = words.get(normalized);

			// try and re-append "e" if it was removed by
			// normalizeRegularVerb()
			if ((entry == null) && trimmedWord.endsWith("e")
					&& !normalized.endsWith("e")) {
				entry = words.get(normalized + "e");
			}

			return entry;
		} else {
			return null;
		}
	}

	/**
	 * Try to find a matching word for a derived adjective.
	 * 
	 * @param word
	 * @return WordEntry
	 */
	WordEntry normalizeAdjective(final String word) {
		final String normalized = Grammar
				.normalizeDerivedAdjective(trimWord(word));

		if (normalized != null) {
			final WordEntry entry = words.get(normalized);

			return entry;
		} else {
			return null;
		}
	}

	/**
	 * Register a name to be recognized by the conversation parser.
	 * 
	 * @param name
	 */
	public void registerSubjectName(final String name) {
		final String key = trimWord(name);

		Integer usageCount = subjectRefCount.get(key);
		if ((usageCount != null) && (usageCount > 0)) {
			// For already known names, we only have to increment the
			// usage counter.
			subjectRefCount.put(key, ++usageCount);
			return;
		}

		// register the new subject name
		final WordEntry entry = words.get(key);

		if ((entry == null) || (entry.getType() == null)
				|| entry.getType().isEmpty()) {
			final WordEntry newEntry = new WordEntry();

			newEntry.setNormalized(key);
			newEntry.setType(new ExpressionType(SUBJECT_NAME_DYNAMIC));

			words.put(key, newEntry);
			subjectRefCount.put(key, 1);
		} else if (!checkNameCompatibleLastType(entry.getType(),
				ExpressionType.SUBJECT)) {
			logger.warn("subject name already registered with incompatible expression type: "
					+ entry.getNormalizedWithTypeString());
		}
	}

	/**
	 * De-register a subject.
	 * 
	 * @param name
	 */
	public void unregisterSubjectName(final String name) {
		final String key = trimWord(name);
		final WordEntry entry = words.get(key);

		if ((entry != null)
				&& entry.getTypeString().equals(SUBJECT_NAME_DYNAMIC)) {
			Integer usageCount = subjectRefCount.get(key);

			if (usageCount != null) {
				// decrement the usage counter
				subjectRefCount.put(key, --usageCount);

				if (usageCount == 0) {
					subjectRefCount.remove(key);
					words.remove(key);
				}
			}
		}
	}

	/**
	 * Register an item or creature name to be recognized by the conversation
	 * parser.
	 * 
	 * @param name
	 * @param typeString
	 */
	public void registerName(final String name, final String typeString) {
		// parse item name without merging Expression entries
		final ConversationContext ctx = new ConversationContext();
		ctx.setMergeExpressions(false);
		final Sentence item = ConversationParser.parse(name, ctx);

		Expression lastExpr = null;
		boolean prepositionSeen = false;

		for (final Expression expr : item) {
			if ((expr.getType() == null) || expr.getType().isEmpty()) {
				// register the unknown word as new object entry
				final WordEntry entry = words.get(expr.getNormalized());

				// set the type to the given one with added "DYN" suffix
				final ExpressionType type = new ExpressionType(typeString
						+ ExpressionType.SUFFIX_DYNAMIC);
				entry.setType(type);
				expr.setType(type);
			} else if (expr.isQuestion()) {
				logger.warn("object name already registered with incompatible expression type while registering name '"
						+ name
						+ "': "
						+ expr.getNormalizedWithTypeString()
						+ " expected type: " + typeString);
			}

			if (expr.isPreposition()) {
				prepositionSeen = true;
			} else if (!prepositionSeen) {
				lastExpr = expr;
			}
		}

		if (lastExpr != null) {
			final ExpressionType lastType = lastExpr.getType();

			if (!checkNameCompatibleLastType(lastType, typeString)) {
				 // ugly special case for "mill" as VERB and OBJECT
				if (!lastType.isVerb() || !typeString.equals(ExpressionType.OBJECT) ||
					!lastExpr.getNormalized().equals("mill")) {
						logger.warn("last word of name '" + name
							+ "' has unexpected type: "
							+ lastExpr.getNormalizedWithTypeString()
							+ " expected type: " + typeString);
					}
			}
		}

		// register compound item names to use them later when merging words
		if (item.getExpressions().size() > 1) {
			compoundNames.add(item);
		}
	}

	private static boolean checkNameCompatibleLastType(
			final ExpressionType lastType, final String typeString) {
		if (lastType.getTypeString().startsWith(typeString)) {
			return true;
		}

		if (lastType.isNumeral()) {
			return true;
		}

		if (lastType.isDynamic()) {
			return true;
		}

		// Ignore words like "chicken", "cat" and "incorporeal armor", which are
		// registered as objects, but also used as subjects.
		if (lastType.isObject() && typeString.equals(ExpressionType.SUBJECT)) {
			return true;
		}
		if (lastType.isSubject() && typeString.equals(ExpressionType.OBJECT)) {
			return true;
		}

		return false;
	}

	/**
	 * Register a verb to be recognized by the conversation parser.
	 * 
	 * @param verb
	 */
	public void registerVerb(final String verb) {
		final String key = trimWord(verb);
		final WordEntry entry = words.get(key);

		if ((entry == null) || (entry.getType() == null)
				|| entry.getType().isEmpty()) {
			final WordEntry newEntry = new WordEntry();

			newEntry.setNormalized(key);
			newEntry.setType(new ExpressionType(VERB_DYNAMIC));

			words.put(key, newEntry);
//		} else if (!checkNameCompatibleLastType(entry.getType(), ExpressionType.VERB)) {
//	 		logger.warn("verb name already registered with incompatible expression type: " +
//			entry.getNormalizedWithTypeString());
		}
	}

	/**
	 * Add a new word to the list in order to remember it later.
	 * 
	 * @param str
	 * @param persist
	 *            if true word will be written to database
	 * @return the added entry
	 */
	public WordEntry addNewWord(final String str, final boolean persist) {
		final String key = trimWord(str);
		WordEntry entry = words.get(key);

		if (entry == null) {
			entry = new WordEntry();

			// add the new entry
			entry.setNormalized(key);
			words.put(key, entry);

			if (persist) {
				persistNewWord(key, entry);
			}
		} else {
			logger.warn("word already known: " + str + " -> "
					+ entry.getNormalized());
		}

		return entry;
	}

	/**
	 * Store the new word in the database. This base class implementation does
	 * nothing, it is overriden by the DBWordList method.
	 * @param key
	 * @param entry
	 * @return success flag
	 */
	protected boolean persistNewWord(final String key, final WordEntry entry) {
		return false;
	}

}
