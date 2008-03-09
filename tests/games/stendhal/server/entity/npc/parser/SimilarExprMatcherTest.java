package games.stendhal.server.entity.npc.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test the SimilarExprMatcher class.
 * 
 * @author Martin Fuchs
 */
public class SimilarExprMatcherTest {

	@Test
	public final void testIsSimilar() {
		assertEquals(true, SimilarExprMatcher.isSimilar(null, null, 0.5));
		assertEquals(true, SimilarExprMatcher.isSimilar("", "", 0.5));

		assertEquals(true, SimilarExprMatcher.isSimilar("A", "A", 0.1));
		assertEquals(true, SimilarExprMatcher.isSimilar("A", "a", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("A", "B", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("A", "AB", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("A", "BA", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("AB", "CD", 0.5));

		assertEquals(true, SimilarExprMatcher.isSimilar("hello", "hallo", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("hello", "hi", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("bus", "taxi", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("heart", "haert", 0.1));
		assertEquals(true, SimilarExprMatcher.isSimilar("heart", "haart", 0.1));

		assertEquals(true, SimilarExprMatcher.isSimilar("hello", "hallo", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("hello", "hi", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("telephone", "taxi", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("taxi", "bus", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("bus", "taxi", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("heart", "haert", 0.1));
		assertEquals(true, SimilarExprMatcher.isSimilar("heart", "haart", 0.1));
		assertEquals(true, SimilarExprMatcher.isSimilar("abcdefgh12345-", "-abcdefgh12345", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("abcdefgh-ABCDEFGHIJKLMN", "ABCDEFGHIJKLMN-abcdefgh", 0.1));
		assertEquals(false, SimilarExprMatcher.isSimilar("abcabcabcabc-123", "abc-123-abcabcabcabc", 0.1));
		assertEquals(true, SimilarExprMatcher.isSimilar("abcabcabcabc-abc", "abc-abcabcabcabc", 0.1));
	}

	@Test
	public final void testSimilarMatching() {
		ExpressionMatcher matcher = new SimilarExprMatcher();

		Expression e1 = new Expression("aBc", "VER");
		Expression e2 = new Expression("abc", "VER");
		Expression e3 = new Expression("ab", "VER");
		Expression e4 = new Expression("abc", "SUB");
		Expression e5 = new Expression("X", "SUB");
		assertTrue(matcher.match(e1, e2));
		assertFalse(matcher.match(e1, e3));
		assertTrue(matcher.match(e1, e4));
		assertFalse(matcher.match(e1, e5));
		assertFalse(matcher.match(e4, e5));

		Expression e6 = new Expression("hello", "VER");
		Expression e7 = new Expression("hallo", "VER");
		Expression e8 = new Expression("hailo", "VER");
		assertTrue(matcher.match(e6, e7));
		assertFalse(matcher.match(e6, e8));
		assertTrue(matcher.match(e7, e8));
	}

	@Test
	public final void testSentenceMatching() {
		Sentence m1 = ConversationParser.parseForMatching("|SIMILAR|hello");
		assertFalse(m1.hasError());
		assertEquals("|SIMILAR|hello", m1.toString());

		assertEquals(true, ConversationParser.parse("hello").matchesFull(m1));
		assertEquals(true, ConversationParser.parse("hallo").matchesFull(m1));
		assertEquals(false, ConversationParser.parse("hailo").matchesFull(m1));
	}

}
