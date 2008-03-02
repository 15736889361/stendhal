package games.stendhal.server.entity.npc.parser;

import games.stendhal.common.ErrorBuffer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * ConversationParser returns the parsed sentence in this class. The Sentence
 * class stores the sentence content in a list of parsed and classified
 * expressions composed of the words forming the sentence. Words belonging
 * to each other are merged into common Expression objects.
 * 
 * @author Martin Fuchs
 */
public class Sentence extends ErrorBuffer implements Iterable<Expression> {

	public enum SentenceType {
		UNDEFINED,
		STATEMENT,
		IMPERATIVE,
		QUESTION
	};

	protected SentenceType sentenceType = SentenceType.UNDEFINED;

	/** JOKER is a joker String used in pattern matches. */
	protected static final String JOKER = "*";

	List<Expression> expressions = new ArrayList<Expression>();

	protected final ConversationContext context;

	protected String originalText;

	/**
	 * Create a Sentence object.
	 *
	 * @param ctx
	 */
	protected Sentence(ConversationContext ctx) {
		context = ctx;
	}

	/**
	 * Set sentence type as STATEMENT, IMPERATIVE or QUESTION.
	 * 
	 * @param type
	 */
	void setType(SentenceType type) {
		this.sentenceType = type;
	}

	/**
	 * Return sentence type.
	 * 
	 * @return
	 */
	public SentenceType getType() {
		return sentenceType;
	}

	/**
	 * Return the list of expressions.
	 *
	 * @return Expression iterator
	 */
	public List<Expression> getExpressions() {
		return expressions;
	}

	/**
	 * Return an iterator over all expressions.
	 *
	 * @return Expression iterator
	 */
	public Iterator<Expression> iterator() {
		return expressions.iterator();
	}

	/**
	 * Count the number of Expressions matching the given type string.
	 * 
	 * @param typePrefix
	 * @return number of matching Expressions
	 */
	private int countExpressions(String typePrefix) {
		int count = 0;

		for (Expression w : expressions) {
			if (w.getTypeString().startsWith(typePrefix)) {
				++count;
			}
		}

		return count;
	}

	/**
	 * Return verb [i] of the sentence.
	 *
	 * @param i 
	 * @param typePrefix 
	 * 
	 * @return verb
	 */
	public Expression getExpression(int idx, String typePrefix) {
		int i = 0;

		for (Expression w : expressions) {
			if (w.getTypeString().startsWith(typePrefix)) {
				if (i == idx) {
					return w;
				}

				++i;
			}
		}

		return null;
	}

	/**
	 * Count the number of Expressions with unknown type.
	 * 
	 * @return number of Expressions with unknown type
	 */
	public int getUnknownTypeCount() {
		int count = 0;

		for (Expression w : expressions) {
			if (w.getTypeString().length() == 0) {
				++count;
			}
		}

		return count;
	}

	/**
	 * Return unknown word [i] of the sentence.
	 * @param i 
	 * 
	 * @return Expression with unknown type
	 */
	public Expression getUnknownTypeExpression(int idx) {
		int i = 0;

		for (Expression w : expressions) {
			if (w.getTypeString().length() == 0) {
				if (i == idx) {
					return w;
				}

				++i;
			}
		}

		return null;
	}

	/**
	 * Return trigger Expression for the FSM engine.
	 * TODO mf - replace by sentence matching.
	 * 
	 * @return trigger string
	 */
	public Expression getTriggerExpression() {
		// just return the first expression
		if (expressions.size() > 0) {
			return expressions.get(0);
		} else {
			return Expression.emptyExpression;
		}
	}

	/**
	 * Return the number of Expression objects of type "VER" in the sentence.
	 * 
	 * @return number of subjects
	 */
	public int getVerbCount() {
		return countExpressions(ExpressionType.VERB);
	}

	/**
	 * Return verb [i] of the sentence.
	 * @param i 
	 * 
	 * @return subject
	 */
	public Expression getVerb(int i) {
		return getExpression(i, ExpressionType.VERB);
	}

	/**
	 * Return verb as Expression object for the special case of
	 * sentences with only one verb.
	 * 
	 * @return normalized verb string
	 */
	public Expression getVerb() {
		if (getVerbCount() == 1) {
			return getVerb(0);
		} else {
			return null;
		}
	}

	/**
	 * Return verb as String for the special case of
	 * sentences with only one verb.
	 * 
	 * @return normalized verb string
	 */
	public String getVerbString() {
		if (getVerbCount() == 1) {
			return getVerb(0).getNormalized();
		} else {
			return null;
		}
	}

	/**
	 * Return the number of subjects.
	 * 
	 * @return number of subjects
	 */
	public int getSubjectCount() {
		return countExpressions(ExpressionType.SUBJECT);
	}

	/**
	 * Return subject [i] of the sentence.
	 * @param i 
	 * 
	 * @return subject
	 */
	public Expression getSubject(int i) {
		return getExpression(i, ExpressionType.SUBJECT);
	}

	/**
	 * Return the subject as String for the special
	 * case of sentences with only one subject.
	 * 
	 * @return normalized subject string
	 */
	public String getSubjectName() {
		if (getSubjectCount() == 1) {
			return getSubject(0).getNormalized();
		} else {
			return null;
		}
	}

	/**
	 * Return the number of objects.
	 * 
	 * @return number of objects
	 */
	public int getObjectCount() {
		return countExpressions(ExpressionType.OBJECT);
	}

	/**
	 * Return object [i] of the parsed sentence (e.g. item to be bought).
	 * @param i 
	 * 
	 * @return object
	 */
	public Expression getObject(int i) {
		return getExpression(i, ExpressionType.OBJECT);
	}

	/**
	 * Return the object as String for the special
	 * case of sentences with only one object.
	 * 
	 * @return normalized object name
	 */
	public String getObjectName() {
		if (getObjectCount() == 1) {
			return getObject(0).getNormalized();
		} else {
			return null;
		}
	}

	/**
	 * Return object name [i] of the parsed sentence.
	 * @param i 
	 * 
	 * @return normalized object name
	 */
	public String getObjectName(int i) {
		return getObject(i).getNormalized();
	}

	/**
	 * Return the number of prepositions.
	 * 
	 * @return number of objects
	 */
	public int getPrepositionCount() {
		return countExpressions(ExpressionType.PREPOSITION);
	}

	/**
	 * Return the preposition [i] of the parsed sentence.
	 * @param i 
	 * 
	 * @return object
	 */
	public Expression getPreposition(int i) {
		return getExpression(i, ExpressionType.PREPOSITION);
	}

	/**
	 * Return the number of number Expressions.
	 * 
	 * @return number of number Expressions
	 */
	public int getNumeralCount() {
		return countExpressions(ExpressionType.NUMERAL);
	}

	/**
	 * Return numeral [i] of the parsed sentence.
	 * @param i 
	 * 
	 * @return numeral
	 */
	public Expression getNumeral(int i) {
		return getExpression(i, ExpressionType.NUMERAL);
	}

	/**
	 * Return the single numeral of the parsed sentence.
	 * 
	 * @return numeral
	 */
	public Expression getNumeral() {
		if (getNumeralCount() == 1) {
			return getNumeral(0);
		} else {
			return null;
		}
    }

	/**
	 * Return true if the sentence is empty.
	 * 
	 * @return empty flag
	 */
	public boolean isEmpty() {
		return sentenceType == SentenceType.UNDEFINED && expressions.isEmpty();
	}

	/**
	 * Return the complete text of the sentence with unchanged case, but with
	 * trimmed white space.
	 * 
	 * TODO mf - There should be only as less code places as possible to rely
	 * on this method.
	 * 
	 * @return string
	 */
	public String getTrimmedText() {
		SentenceBuilder builder = new SentenceBuilder();

		for (Expression w : expressions) {
			builder.append(w.getOriginal());
		}

		appendPunctation(builder);

		return builder.toString();
	}

	/**
	 * Return the original parsed text of the sentence.
	 * 
	 * TODO mf - There should be only as less code places as possible to rely
	 * on this method.
	 * 
	 * @return string
	 */
	public String getOriginalText() {
		return originalText;
	}

	/**
	 * Return the sentence with all words normalized.
	 * 
	 * @return string
	 */
	public String getNormalized() {
		SentenceBuilder builder = new SentenceBuilder();

		for (Expression w : expressions) {
			if (w.getType() == null || !isIgnorable(w)) {
				builder.append(w.getNormalized());
			}
		}

		appendPunctation(builder);

		return builder.toString();
	}

	/**
	 * Check if the given Expression should be ignored.
	 *
	 * @param expr
	 * @return
	 */
	protected boolean isIgnorable(Expression expr) {
	    return context.getIgnoreIgnorable() && expr.isIgnore();
    }

	/**
	 * Return the full sentence as lower case string
	 * including type specifiers.
	 * 
	 * @return string
	 */
	@Override
	public String toString() {
		SentenceBuilder builder = new SentenceBuilder();

		for (Expression w : expressions) {
			if (w.getType() != null) {
				if (!isIgnorable(w)) {
					builder.append(w.getNormalizedWithTypeString());
				}
			} else {
				builder.append(w.getOriginal());
			}

			if (w.getBreakFlag()) {
				builder.append(',');
			}
		}

		appendPunctation(builder);

		return builder.toString();
	}

	/**
	 * Append the trailing punctuation depending on the sentence type to the given SentenceBuilder.
	 *
	 * @param builder
	 */
	protected void appendPunctation(SentenceBuilder builder) {
		if (sentenceType == SentenceType.STATEMENT) {
			builder.append('.');
		} else if (sentenceType == SentenceType.IMPERATIVE) {
			builder.append('!');
		} else if (sentenceType == SentenceType.QUESTION) {
			builder.append('?');
		}
    }

	/**
	 * Check if two Sentences consist of identical normalized Expressions.
	 *
	 * @param other
	 * @return
	 */
	public boolean equalsNormalized(Sentence other) {
		if (other == this) {
			return true;
		} else if (other == null) {
			return false;
		}

		// shortcut for sentences with differing lengths
	    if (expressions.size() != other.expressions.size()) {
	    	return false;
	    }

	    // loop over all expressions and compare both sides
	    Iterator<Expression> it1 = expressions.iterator();
	    Iterator<Expression> it2 = other.expressions.iterator();

		while (it1.hasNext() && it2.hasNext()) {
			Expression e1 = it1.next();
			Expression e2 = it2.next();

			if (!e1.matchesNormalized(e2)) {
				return false;
			}
		}

		// Now there should be no more expressions at both sides.
		return (!it1.hasNext() && !it2.hasNext());
    }

	/**
	 * Compare two sentences and return the difference as String.
	 *
	 * @param other
	 * @return difference String
	 */
	public String diffNormalized(Sentence other) {
		SentenceBuilder ret = new SentenceBuilder();

	    // loop over all expressions and match them between both sides
	    Iterator<Expression> it1 = expressions.iterator();
	    Iterator<Expression> it2 = other.expressions.iterator();

		while (true) {
			Expression e1 = nextValid(it1);
			Expression e2 = nextValid(it2);

			if (e1 == null && e2 == null) {
				break;
			} else if (e1 != null && e2 != null) {
    			if (!e1.matchesNormalized(e2)) {
    				ret.append("-[" + e1.getNormalized() + "]");
    				ret.append("+[" + e2.getNormalized() + "]");
    			}
			} else if (e1 != null) {
				ret.append("-[" + e1.getNormalized() + "]");
			} else {
				ret.append("+[" + e2.getNormalized() + "]");
			}
		}

		return ret.toString();
	}

	/**
	 * Advance the iterator and return the next non-ignorable Expression.
	 * 
	 * @param it
	 * @return
	 */
	public Expression nextValid(Iterator<Expression> it) {
		while (it.hasNext()) {
			Expression expr = it.next();

			if (!isIgnorable(expr)) {
				return expr;
			}
		}

	    return null;
    }

	/**
	 * Check if the Sentence matches the given String.
	 * The match Sentence can contain explicit expressions, which
	 * are compared after normalizing, or ExpressionType specifiers
	 * like "VER" or "SUB*" in upper case.
	 *
	 * @param text
	 * @return
	 */
	public boolean matchesNormalized(String text) {
		return matchesFull(ConversationParser.parseForMatching(text));
	}

	/**
	 * Check if the Sentence beginning matches the given String.
	 * The match Sentence can contain explicit expressions, which
	 * are compared after normalizing, or ExpressionType specifiers
	 * like "VER" or "SUB*" in upper case.
	 *
	 * @param text
	 * @return
	 */
	public boolean matchesNormalizedStart(String text) {
		return matchesStart(ConversationParser.parseForMatching(text));
	}

	/**
	 * Check if the Sentence completely matches the given Sentence.
	 * The match Sentence can contain explicit expressions, which
	 * are compared after normalizing, or ExpressionType specifiers
	 * like "VER" or "SUB*" in upper case.
	 *
	 * @param other
	 * @return
	 */
	public boolean matchesFull(Sentence other) {
		return matches(other, false);
	}

	/**
	 * Check if the Sentence start matches the given Sentence.
	 * The match Sentence can contain explicit expressions, which
	 * are compared after normalizing, or ExpressionType specifiers
	 * like "VER" or "SUB*" in upper case.
	 *
	 * @param other
	 * @return
	 */
	public boolean matchesStart(Sentence other) {
		return matches(other, true);
	}

	/**
	 * Check if the Sentence matches the given Sentence.
	 * The match Sentence can contain explicit expressions, which
	 * are compared after normalizing, or ExpressionType specifiers
	 * like "VER" or "SUB*" in upper case.
	 *
	 * @param other
	 * @param matchStart 
	 * @return
	 */
	private boolean matches(Sentence other, boolean matchStart) {
		if (other == null) {
			return false;
		}

	    // loop over all expressions and match them between both sides
	    Iterator<Expression> it1 = expressions.iterator();
	    Iterator<Expression> it2 = other.expressions.iterator();
	    Expression e1, e2;

		while (true) {
			e1 = nextValid(it1);
			e2 = nextValid(it2);

			if (e1 == null || e2 == null) {
				break;
			}

			String matchString = e2.getNormalized();

			if (matchString.contains(JOKER)) {
				if (matchString.equals(JOKER)) {
					// Type string matching is identified by a single "*" as normalized string expression.
					if (!matchesJokerString(e1.getTypeString(), e2.getTypeString())) {
						return false;
					}
				} else {
					// Look for a normalized string match towards the string containing a joker character.
					if (!matchesJokerString(e1.getNormalized(), matchString)) {
						return false;
					}
				}
			} else if (!e1.matchesNormalized(e2)) {
				return false;
			}
		}

		// If we look for a full match, there should be no more expressions at both sides.
		if (e1 == null && e2 == null) {
			return true;
		}
		// If we look for a match at Sentence start, there must be no more expressions at the right side.
		else {
			return (matchStart && e2 == null);
		}
	
	}

	/**
	 * Match the given String towards a pattern String containing JOKER characters.
	 *
	 * @param str
	 * @param matchString
	 * @return
	 */
	private boolean matchesJokerString(String str, String matchString) {
		if (str.equals(JOKER)) {
			// Empty strings do not match the "*" joker.
			return str.length() > 0;
		} else { 
			// Convert the joker string into a regular expression and let the Pattern class do the work.
			return Pattern.compile(matchString.replace(JOKER, ".*")).matcher(str).find();
		}
    }

	/**
	 * Searches for a matching item name in the given Set.
	 * 
	 * @param names
	 * @return item name, or null if no match
	 */
	public NameSearch findMatchingName(Set<String> names) {
		NameSearch ret = new NameSearch(names);

		// check first object of the sentence
		Expression item = getObject(0);

		if (item != null) {
			if (ret.search(item)) {
				return ret;
			}
		}

		if (!ret.found()) {
			// check first subject
			item = getSubject(0);

			if (item != null) {
				if (ret.search(item)) {
					return ret;
    			}
			}
		}

		if (!ret.found()) {
			// check second subject, e.g. in "i buy cat"
			item = getSubject(1);

			if (item != null) {
    			if (ret.search(item)) {
    				return ret;
    			}
			}
		}

		return ret;
    }

	/**
	 * Return a string containing the sentence part referenced by a verb
	 * or simple the single object name.
	 *
	 * @return String or null if nothing found
	 */
	public String getExpressionStringAfterVerb() {
		String ret = null;

		int unkownCount = getUnknownTypeCount();
		int verbCount = getVerbCount();

		// If all words in the Sentence could be recognized, just return the object name, if available.
		if (unkownCount == 0) {
			ret = getObjectName();
		}

		// If we could not find an object, look for the expressions following the single verb.
		if (ret == null && verbCount == 1) {
			ret = stringFromExpressionsAfter(getVerb());
		}

		// If we didn't find anything usable until now, take the first unknown word.
		if (ret == null && unkownCount > 0) {
			Expression unknown = getUnknownTypeExpression(0);

			if (unknown != null) {
				ret = unknown.getNormalized();
			}
		}

		// If this still didn't work, look for the expressions following the first verb.
		if (ret == null && verbCount > 1) {
			ret = stringFromExpressionsAfter(getVerb(0));
		}

		return ret;
    }

	/**
	 * Build a string from the list of expressions following the given one.
	 *
	 * @param verb
	 * @return
	 */
	private String stringFromExpressionsAfter(Expression expr) {
		if (expr == null) {
			return null;
		}

		Iterator<Expression> it = iteratorFromExpression(expr);

		// skip leading amount expressions
		Iterator<Expression> it2 = iteratorFromExpression(expr);
		if (it2.hasNext()) {
			Expression e2 = it2.next();

			if (e2.isNumeral()) {
				it.next();
			}
		}

		// build a string from the expression list
		SentenceBuilder buffer = new SentenceBuilder();

		if (buffer.appendUntilBreak(it) > 0) {
			return buffer.toString();
		} else {
			return null;
		}
    }

	/**
	 * Iterate until we find the given Expression object.
	 *
	 * @param expr
	 * @return
	 */
	private Iterator<Expression> iteratorFromExpression(Expression expr) {
		Iterator<Expression> it = expressions.iterator();

		while (it.hasNext()) {
			// Did we find it?
			if (expr == it.next()) {
				break;
			}
		}

		return it;
    }

}
